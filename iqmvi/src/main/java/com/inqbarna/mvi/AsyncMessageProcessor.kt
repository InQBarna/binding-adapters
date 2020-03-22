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
