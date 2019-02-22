package com.inqbarna.adapters

/**
 * Created by David García (david.garcia@inqbarna.com)
 */

interface TreeNode<T : Any> {
    val parent: TreeNode<T>?

    val isOpened: Boolean

    val data: T

    val hasChildren: Boolean

    fun open(notify: Boolean = true): Boolean

    fun close(notify: Boolean = true): Boolean

    fun closeChildren(notify: Boolean = true): Boolean

    fun isChild(other: TreeNode<T>, findClosed: Boolean): Boolean

    fun openToChild(child: TreeNode<T>, notify: Boolean): Boolean

    fun root(): TreeNode<T>
}
