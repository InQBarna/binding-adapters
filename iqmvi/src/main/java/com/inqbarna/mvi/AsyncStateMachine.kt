package com.inqbarna.mvi

import com.inqbarna.navigation.base.AppRoute
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.whileSelect
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import kotlin.coroutines.CoroutineContext

private const val DEBUG = false

interface AsideCommandProcessor<out CommandType, in StateType> {
    fun setAsideChannel(asideChannelContext: CoroutineContext, channel: SendChannel<CommandType>, stateMachineContext: StateMachineContext<StateType>)
}

@ExperimentalCoroutinesApi
abstract class AsyncStateMachine<in MessageType : Any, in CommandType : Any, StateType : Any, out OutStateType : Any>(
    private val reducer: AsyncStateReducer<CommandType, StateType>,
    private val messageProcessor: AsyncMessageProcessor<MessageType, CommandType, StateType>
) {

    var currentState = reducer.initialState
        private set

    private val inputChannel = Channel<MessageType>(Channel.UNLIMITED)
    private val mutex = Mutex()

    private val stateMachineContext = object :
        StateMachineContext<StateType> {
        override suspend fun getCurrentState(): StateType {
            return mutex.withLock { currentState }
        }
    }

    private fun CoroutineScope.beginMessageProcessing(message: MessageType): ReceiveChannel<CommandType> {
        return with(messageProcessor) {
            processMessage(stateMachineContext, message)
        }
    }

    val navigation: ReceiveChannel<AppRoute>
        get() = messageProcessor.navigationChannel()

    @ExperimentalCoroutinesApi
    private suspend fun ProducerScope<OutStateType>.innerPublishResults(nextState: StateType) {
        val oldState = mutex.withLock {
            val oldState = currentState
            currentState = nextState
            oldState
        }

        publishStateUpdate(oldState, nextState)
    }

    @ExperimentalCoroutinesApi
    protected abstract suspend fun ProducerScope<OutStateType>.publishStateUpdate(oldState: StateType, newState: StateType)

    private fun ProducerScope<OutStateType>.setupAsideChannel() {
        val channel = Channel<CommandType>()

        @Suppress("UNCHECKED_CAST")
        if (messageProcessor is AsideCommandProcessor<*, *>) {
            printDebug("Configured aside command channel")
            (messageProcessor as AsideCommandProcessor<CommandType, StateType>).setAsideChannel(coroutineContext, channel, stateMachineContext)
        }

        launch {
            whileSelect {
                channel.onReceive {
                    printDebug("---> ASIDE command: $it")
                    val nextState = reducer.generateNextState(stateMachineContext, it)
                    printDebug("==== ASIDE state: %s", nextState)
                    innerPublishResults(nextState)
                    true
                }
            }
        }
    }

    @UseExperimental(ObsoleteCoroutinesApi::class)
    @ExperimentalCoroutinesApi
    val CoroutineScope.outputs: ReceiveChannel<OutStateType>
        get() = produce(CoroutineName("states-producer")) {

            setupAsideChannel()

            while (isActive && !inputChannel.isClosedForReceive) {
                val currentMsg = inputChannel.receive()
                printDebug("<~~~~ MESSAGE input: $currentMsg")
                val currentChannel: ReceiveChannel<CommandType> = beginMessageProcessing(currentMsg)
                for (cmd in currentChannel) {
                    printDebug("---> COMMAND generated: %s", cmd)
                    val nextState = reducer.generateNextState(stateMachineContext, cmd)
                    printDebug("==== COMPUTED state: %s", nextState)
                    innerPublishResults(nextState)
                    printDebug("After result is published, job active = %s", isActive)
                }
            }
            coroutineContext.cancelChildren()
            printDebug("Producer coroutine finished!!")
        }

    fun CoroutineScope.submitMessage(message: MessageType) {
        launch {
            inputChannel.send(message)
        }
    }

    fun close() {
        inputChannel.close()
    }

    protected fun printDebug(fmt: String, vararg fmtArgs: Any) {
        if (DEBUG) {
            Timber.tag("ASM").d(fmt, *fmtArgs)
        }
    }
}
