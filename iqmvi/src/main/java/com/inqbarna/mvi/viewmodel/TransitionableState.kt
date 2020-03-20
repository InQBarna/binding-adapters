package com.inqbarna.mvi.viewmodel

/**
 * For future developments maybe better implement split visual updates between "enter to" and "exit from" state
 */
interface TransitionableState<in ViewModelType, in StateType> {
    fun onExitState(nextState: StateType, targetViewModel: ViewModelType)
    fun onEnterState(prevState: StateType?, targetViewModel: ViewModelType)
}
