package com.inqbarna.mvi.viewmodel

import com.inqbarna.mvi.viewmodel.TransitionableState

interface ViewState<in ViewModelType> :
    TransitionableState<ViewModelType, Any> {
    fun applyTo(viewModel: ViewModelType)

    // to have retro compatible code, we just map enterState to applyTo, and exit is not implemented
    override fun onEnterState(prevState: Any?, targetViewModel: ViewModelType) = applyTo(targetViewModel)
    override fun onExitState(nextState: Any, targetViewModel: ViewModelType) {
        // noop by default so that old code is retro compatible
    }
}
