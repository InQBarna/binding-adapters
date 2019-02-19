package com.inqbarna.adapters

/**
 * Created by David García <david.garcia></david.garcia>@inqbarna.com> on 3/02/17.
 */

interface TreeNode<T : NestableMarker<T>> {
    val parent: TreeNode<T>?

    val isOpened: Boolean

    val data: T

    fun open(notify: Boolean): Boolean

    fun close(notify: Boolean): Boolean

    fun closeChilds(notify: Boolean): Boolean

    fun isChild(other: TreeNode<T>, findClosed: Boolean): Boolean

    fun openToChild(child: TreeNode<T>, notify: Boolean): Boolean

    fun root(): TreeNode<T>
}
