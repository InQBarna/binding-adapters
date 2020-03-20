package com.inqbarna.mvi

interface AsyncStateReducer<in MessageType, StateType> {
    val initialState: StateType
    suspend fun generateNextState(asContext: StateMachineContext<StateType>, inputMsg: MessageType): StateType
}
