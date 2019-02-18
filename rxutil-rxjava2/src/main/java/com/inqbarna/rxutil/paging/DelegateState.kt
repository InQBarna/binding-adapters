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

package com.inqbarna.rxutil.paging

import io.reactivex.disposables.Disposable

internal sealed class DelegateState
internal class Delivering private constructor(val lastDelivered: Boolean, val prefetchTask: Disposable?) : DelegateState() {
    constructor(lastDelivered: Boolean) : this(lastDelivered, null)
    constructor(prefetchTask: Disposable) : this(false, prefetchTask)
    val prefetching: Boolean
        get() = null != prefetchTask

    override fun toString(): String {
        return "Delivering(lastDelivered: $lastDelivered, prefetching: $prefetching)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Delivering

        if (lastDelivered != other.lastDelivered) return false
        if (prefetchTask != other.prefetchTask) return false

        return true
    }

    override fun hashCode(): Int {
        var result = lastDelivered.hashCode()
        result = 31 * result + (prefetchTask?.hashCode() ?: 0)
        return result
    }
}

internal data class Error(val error: Throwable, val recovery: ListenableRetry? = null) : DelegateState() {
    val hasRecovery: Boolean
        get() = null != recovery

    override fun toString(): String {
        return "Error(hasRecovery: $hasRecovery): ${error.message}"
    }
}

internal data class Loading(val task: Disposable) : DelegateState()
internal object Complete : DelegateState()
internal object None : DelegateState()
