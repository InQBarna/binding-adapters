package com.inqbarna.mvi

import com.inqbarna.navigation.base.AppRoute
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel

interface AsyncMessageProcessor<in MessageType : Any, out CommandType : Any, in StateType : Any> {
    fun CoroutineScope.processMessage(context: StateMachineContext<StateType>, message: MessageType): ReceiveChannel<CommandType>
    fun navigationChannel(): ReceiveChannel<AppRoute> {
        return Channel<AppRoute>(0).also {
            // By default return closed channel
            it.close()
        }
    }
}
