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
