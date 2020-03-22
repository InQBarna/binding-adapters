package com.inqbarna.mvi.viewmodel

import androidx.lifecycle.ViewModel
import com.inqbarna.mvi.AsyncMessageProcessor
import com.inqbarna.mvi.AsyncStateReducer
import com.inqbarna.mvi.SimpleAsyncStateMachine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.whileSelect

/**
 * @author David García (david.garcia@inqbarna.com)
 * @version 1.0 18/12/2018
 */
@ExperimentalCoroutinesApi
abstract class AsyncStateViewModel<in MessageType, in CommandType, out SideEffectType : Any, LogicalStateType, ViewStateType>(
    reducer: AsyncStateReducer<CommandType, LogicalStateType>,
    processor: AsyncMessageProcessor<MessageType, CommandType, LogicalStateType, SideEffectType>
) : ViewModel()
    where MessageType : Any, CommandType : Any, LogicalStateType : ViewState<ViewStateType, LogicalStateType>, ViewStateType : Any {
    val viewState: ViewStateType by lazy { createViewState() }
    private val stateMachine: SimpleAsyncStateMachine<MessageType, CommandType, LogicalStateType, SideEffectType> =
        SimpleAsyncStateMachine(reducer, processor)
    protected val currentState: LogicalStateType
        get() = stateMachine.currentState

    private val viewModelScope by lazy {
        CoroutineScope(SupervisorJob() + Dispatchers.Main)
    }

    init {
        viewModelScope.launch {
            innerApplyStateChange(reducer.initialState,
                EnterCaller(null)
            )
        }
        startReceiving()
    }

    protected abstract fun createViewState(): ViewStateType

    private fun startReceiving() {
        val statesChannel = with(stateMachine) { viewModelScope.outputs }
        viewModelScope.launch {
            whileSelect {
                statesChannel.onReceive {
                    // We will only receive when there's been an actual state change
                    innerApplyStateChange(
                        it.oldState,
                        ExitCaller(it.newState)
                    )
                    innerApplyStateChange(
                        it.newState,
                        EnterCaller(it.oldState)
                    )
                    onStateUpdated(it.oldState, it.newState)
                    true
                }
            }
        }
    }

    private fun innerApplyStateChange(logicalStateType: LogicalStateType, applyMode: LogicalStateType.(ViewStateType) -> Unit) {
        logicalStateType.applyMode(viewState)
    }

    override fun onCleared() {
        super.onCleared()
        stateMachine.close()
        viewModelScope.cancel()
    }

    val sideEffects: ReceiveChannel<SideEffectType>
        get() = stateMachine.sideEffects

    protected open fun onStateUpdated(oldState: LogicalStateType, newState: LogicalStateType) {
        // allow inheritors to react to the change also, besides the default ViewState update performed on the base class
    }

    protected fun postMessage(message: MessageType) {
        with(stateMachine) {
            viewModelScope.submitMessage(message)
        }
    }
}

private class EnterCaller<StateType : ViewState<ViewModelType, StateType>, in ViewModelType>(private val prevState: StateType?) : (StateType, ViewModelType) -> Unit {
    override fun invoke(state: StateType, viewModel: ViewModelType) {
        state.onEnterState(prevState, viewModel)
    }
}

private class ExitCaller<StateType : ViewState<ViewModelType, StateType>, in ViewModelType>(private val nextState: StateType) : (StateType, ViewModelType) -> Unit {
    override fun invoke(state: StateType, viewModel: ViewModelType) {
        state.onExitState(nextState, viewModel)
    }
}
