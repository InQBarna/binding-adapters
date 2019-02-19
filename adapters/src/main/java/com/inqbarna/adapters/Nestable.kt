package com.inqbarna.adapters

/**
 * @author David García (david.garcia@inqbarna.com)
 * @version 1.0 2019-02-19
 */
interface Nestable<out T : Nestable<T>> {
    val children: List<T>
}