package com.inqbarna.adapters

/**
 * @author David García <david.garcia></david.garcia>@inqbarna.com>
 * @version 1.0 31/1/17
 */

interface NestableMarker<out T : NestableMarker<T>> : TypeMarker {
    val key: Any
    fun children(): List<T>
}


