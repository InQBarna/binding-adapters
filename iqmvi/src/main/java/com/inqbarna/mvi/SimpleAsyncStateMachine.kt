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
