package com.inqbarna.mvi

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ProducerScope

data class StateUpdate<StateType : Any>(val oldState: StateType, val newState: StateType)

@ExperimentalCoroutinesApi
class SimpleAsyncStateMachine<in MessageType : Any, in CommandType : Any, StateType : Any, out SideEffectType : Any>(
    reducer: AsyncStateReducer<CommandType, StateType>,
    messageProcessor: AsyncMessageProcessor<MessageType, CommandType, StateType, SideEffectType>
) : AsyncStateMachine<MessageType, CommandType, StateType, StateUpdate<StateType>, SideEffectType>(reducer, messageProcessor) {

    override suspend fun ProducerScope<StateUpdate<StateType>>.publishStateUpdate(oldState: StateType, newState: StateType) {
        if (oldState != newState) {
            printDebug("States are different, emit state update!!")
            send(StateUpdate(oldState, newState))
        } else {
            printDebug("States didn't change, won't emit sate update")
        }
    }
}
