package com.inqbarna.mvi

interface StateMachineContext<out StateType> {
    suspend fun getCurrentState(): StateType
}
