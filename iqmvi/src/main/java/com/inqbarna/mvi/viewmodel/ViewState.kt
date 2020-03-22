/*
 * Copyright 2014 InQBarna Kenkyuu Jo SL 
 *  
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *     http://www.apache.org/licenses/LICENSE-2.0 
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package com.inqbarna.mvi.viewmodel

/**
 * For future developments maybe better implement split visual updates between "enter to" and "exit from" state
 */
interface ViewState<in ViewModelType, in LogicalStateType> {
    fun onExitState(nextState: LogicalStateType, targetViewModel: ViewModelType)
    fun onEnterState(prevState: LogicalStateType?, targetViewModel: ViewModelType)
}
