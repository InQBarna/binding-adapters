package com.inqbarna.mvi.viewmodel

/**
 * For future developments maybe better implement split visual updates between "enter to" and "exit from" state
 */
interface ViewState<in ViewModelType, in LogicalStateType> {
    fun onExitState(nextState: LogicalStateType, targetViewModel: ViewModelType)
    fun onEnterState(prevState: LogicalStateType?, targetViewModel: ViewModelType)
}
