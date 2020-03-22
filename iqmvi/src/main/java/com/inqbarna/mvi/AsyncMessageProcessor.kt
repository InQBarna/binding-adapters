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

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel

interface AsyncMessageProcessor<in MessageType : Any, out CommandType : Any, in StateType : Any, out SideEffectType : Any> {

    fun CoroutineScope.processMessage(
        context: StateMachineContext<StateType>,
        message: MessageType
    ): ReceiveChannel<CommandType>

    fun sideEffectsChannel(): ReceiveChannel<SideEffectType> {
        return Channel<SideEffectType>(0).also {
            // By default return closed channel
            it.close()
        }
    }
}
