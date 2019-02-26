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

import com.google.common.collect.ImmutableList
import com.google.common.graph.SuccessorsFunction
import com.google.common.graph.Traverser
import java.util.*

/**
 * @author David García (david.garcia@inqbarna.com)
 * @version 1.0 31/1/17
 */

open class TreeBindingAdapter<T : Any, VMType : TypeMarker> @JvmOverloads constructor(
        private val dataExtractor: DataExtractor<T>,
        private var factory: TreeItemVMFactory<T, VMType>? = null
) : BindingAdapter() {

    private val flattened = mutableListOf<TreeNodeImpl<T, VMType>>()
    private val tree  = mutableListOf<TreeNodeImpl<T, VMType>>()
    private val ROOT = TreeNodeImpl.createInvalid<T>()

    protected val toplevelItemsData: List<T>
        get() = tree.map { it.data }

    protected val flattenedItemsData: List<T>
        get() = flattened.map { it.data }

    protected val toplevelItems: List<TreeNode<T>>
        get() = tree.toList()

    protected val flattenedItems: List<TreeNode<T>>
        get() = flattened.toList()

    fun setItems(items: List<T>) {
        flattened.clear()
        tree.clear()
        addItems(items, false)
        notifyDataSetChanged()
    }

    fun setItemFactory(factory: TreeItemVMFactory<T, VMType>) {
        this.factory = factory
        preOrder().onEach { (it as TreeNodeImpl<T, VMType>).updateFactory(factory) }
        notifyDataSetChanged()
    }

    @JvmOverloads
    fun addItems(items: List<T>, notify: Boolean = true) {
        ensureFactory()
        val firstIdx = flattened.size
        for (item in items) {
            val node = TreeNodeImpl(this, item) // create node, by default closed...
            flattened.add(node) // input items, are considered top-level, thus inserted directly to flattened list
            tree.add(node) // top-level nodes, never can go away
            // wen collapsed flattened and tree are equal
        }
        if (notify) {
            notifyItemRangeInserted(firstIdx, items.size)
        }
    }

    private fun ensureFactory() {
        requireNotNull(factory) { "You need to provide with TreeItemVMFactory before you try this operation" }
    }

    final override fun getDataAt(position: Int): VMType {
        return flattened[position].viewModel
    }

    override fun getItemCount(): Int {
        return flattened.size
    }

    protected fun commonParent(a: TreeNode<T>, b: TreeNode<T>): TreeNode<T>? {
        val aParents = HashSet<TreeNode<T>>()
        val bParents = HashSet<TreeNode<T>>()
        var aParent: TreeNode<T>? = a
        var bParent: TreeNode<T>? = b
        while (true) {
            aParent = aParent?.parent
            bParent = bParent?.parent
            if (null == aParent && null == bParent) {
                return null // no common parents
            }
            if (null != aParent && aParent === bParent) {
                return aParent
            }
            if (null != aParent) {
                if (bParents.contains(aParent)) {
                    return aParent
                }
                aParents.add(aParent)
            }
            if (null != bParent) {
                if (aParents.contains(bParent)) {
                    return bParent
                }
                bParents.add(bParent)
            }
        }
    }

    protected fun preOrder(): Iterable<TreeNode<*>> {
        return Traverser.forTree(ChildrenFunction()).depthFirstPreOrder(ROOT).filter { it !== ROOT }
    }

    protected fun breadthFirstExtractorIterator(node: TreeNode<*>? = null): Iterable<TreeExtractedData<T, VMType>> {
        return breadthFirstNodeIterator(node)
                .map { it as TreeNodeImpl<T, VMType> }
                .map { TreeExtractedData(it as TreeNode<T>, it.data, it.tryGetViewModel()) }
    }

    protected fun breadthFirstNodeIterator(node: TreeNode<*>?): Iterable<TreeNode<T>> {
        val root: TreeNodeImpl<*, *> = when (node) {
            null -> ROOT
            !is TreeNodeImpl<*, *> -> return ImmutableList.of()
            else -> node
        }

        return Traverser.forTree(ChildrenFunction()).breadthFirst(root)
                .filter { it !== ROOT }
                .map { it as TreeNode<T> }
    }

    protected fun findNodeForItem(item: T): TreeNode<T>? {
        return breadthFirstNodeIterator(null).firstOrNull { itemEqual(item, it.data)}
    }

    protected fun extractDataAtPos(visiblePos: Int, noCreate: Boolean = false): TreeExtractedData<T, VMType>? {
        if (visiblePos >= 0 && visiblePos < flattened.size) {
            val node = flattened[visiblePos]
            return TreeExtractedData(node, node.data, if (noCreate) node.tryGetViewModel() else node.viewModel)
        }
        return null
    }

    private inner class ChildrenFunction : SuccessorsFunction<TreeNodeImpl<*, *>> {

        override fun successors(node: TreeNodeImpl<*, *>): Iterable<TreeNodeImpl<*, *>> {
            return if (node === ROOT) {
                tree
            } else {
                node.childNodes
            }
        }
    }

    private class TreeNodeImpl<T : Any, VMType : TypeMarker> : TreeNode<T> {
        private var factory: TreeItemVMFactory<T, VMType>?
        override val hasChildren: Boolean
        override var parent: TreeNode<T>?
            private set
        override var isOpened: Boolean = false
            private set
        internal val numChildren: Int
        private val _data: T?
        internal val childNodes: List<TreeNodeImpl<T, VMType>>

        private val adapter: TreeBindingAdapter<T, VMType>?
        private var _viewModel: VMType? = null

        private var _dataExtractor: DataExtractor<T>? = null

        private val dataExtractor: DataExtractor<T>
            get() = requireNotNull(_dataExtractor) { "Illegal operation on an invalid node" }

        override fun toString(): String {
            val sb = StringBuilder("TreeNode{")
            sb.append("opened=").append(isOpened)
            sb.append(", children=").append(numChildren)
            sb.append(", data=").append(data)
            sb.append('}')
            return sb.toString()
        }

        private constructor() {
            // invalid node!
            adapter = null
            _data = null
            numChildren = 0
            parent = null
            hasChildren = false
            childNodes = emptyList()
            factory = null
            _dataExtractor = null
        }

        internal fun updateFactory(factory: TreeItemVMFactory<T, VMType>) {
            this.factory = factory
            _viewModel = null
        }

        override val data: T
            get() = requireNotNull(_data) { "This is an invalid node, do not get data from it (check before)" }

        internal val viewModel: VMType
            get() {
                val factory = requireNotNull(factory) { "This is an invalid node, do not get factory from it (check before)" }
                return _viewModel ?: factory.createViewModel(data).also { _viewModel = it }
            }

        internal fun tryGetViewModel(): VMType? = _viewModel

        @JvmOverloads
        constructor(adapter: TreeBindingAdapter<T, VMType>, item: T, parent: TreeNode<T>? = null) {
            this.parent = parent
            this.adapter = adapter
            // closed state by default
            _dataExtractor = adapter.dataExtractor
            factory = adapter.factory

            val children = with (dataExtractor) { item.children }
            numChildren = children.size
            childNodes = children.map { TreeNodeImpl(adapter, it, this) }
            _data = item
            hasChildren = numChildren > 0
            isOpened = false
        }

        internal fun open(yourIdxInFlat: Int, notify: Boolean): Boolean {
            if (adapter == null)
                return false

            if (!isOpened && hasChildren) {
                adapter.flattened.addAll(yourIdxInFlat + 1, childNodes)
                if (notify) {
                    adapter.notifyItemRangeInserted(yourIdxInFlat + 1, numChildren)
                }
                isOpened = true
                return true
            }
            return false
        }

        override fun openToChild(child: TreeNode<T>, notify: Boolean): Boolean {
            val fifo = ArrayDeque<TreeNode<T>>()
            var parent: TreeNode<T>? = child
            var isChild = false
            while (null != parent) {
                fifo.push(parent)
                if (this === parent) {
                    isChild = true
                    break
                }
                parent = parent.parent
            }

            if (!isChild) {
                throw IllegalArgumentException("The given child item is not a child of this node. Child = $child, parent = $this")
            }

            var allOpened = true
            while (!fifo.isEmpty()) {
                val toOpen = fifo.pop()
                toOpen.open(notify)
                allOpened = allOpened and toOpen.isOpened // we don't use ret value of open() because it return false if it was already open
            }

            return allOpened
        }

        override fun root(): TreeNode<T> {
            val parent = parent
            return parent?.root() ?: this
        }

        private fun findIndexOf(node: TreeNode<T>): Int {
            val mFlattened1 = adapter?.flattened ?: emptyList<TreeNode<T>>()
            var i = 0
            val mFlattened1Size = mFlattened1.size
            while (i < mFlattened1Size) {
                val n = mFlattened1[i]
                if (itemEqual(n.data, node.data)) {
                    return i
                }
                i++
            }
            return -1
        }

        internal fun close(yourIdxInFlat: Int, notify: Boolean): Boolean {
            if (adapter == null)
                return false

            if (isOpened && hasChildren) {

                val numContributingChild = countContributing()

                val childNodes = adapter.flattened.subList(yourIdxInFlat + 1, yourIdxInFlat + 1 + numContributingChild)
                for (tn in childNodes) {
                    tn.isOpened = false
                }
                childNodes.clear()
                if (notify) {
                    adapter.notifyItemRangeRemoved(yourIdxInFlat + 1, numContributingChild)
                }
                isOpened = false
                return true
            }
            return false
        }

        private fun countContributing(): Int {
            var count = 0
            if (isOpened) {
                count += numChildren
                for (node in childNodes) {
                    count += node.countContributing()
                }
            }
            return count
        }

        override fun open(notify: Boolean): Boolean {
            val indexOf = findIndexOf(this)
            return if (indexOf >= 0) {
                open(indexOf, notify)
            } else false
        }

        override fun close(notify: Boolean): Boolean {
            val indexOf = findIndexOf(this)
            return if (indexOf >= 0) {
                close(indexOf, notify)
            } else false
        }

        override fun closeChildren(notify: Boolean): Boolean {
            var retVal = false
            for (n in childNodes) {
                retVal = retVal or n.close(notify)
            }
            return retVal
        }

        override fun isChild(other: TreeNode<T>, findClosed: Boolean): Boolean {
            var parent: TreeNode<T>? = other.parent
            while (null != parent) {
                if (!findClosed && !parent.isOpened)
                    break

                if (this === parent) {
                    return true
                }
                parent = parent.parent
            }
            return false
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || other !is TreeNodeImpl<*, *>) return false

            val treeNode = other as TreeNodeImpl<T, VMType>

            if (isOpened != treeNode.isOpened) return false
            val (myKey, otherKey) = with (dataExtractor) {
                arrayOf(data.identityKey, treeNode.data.identityKey)
            }
            return if (numChildren != treeNode.numChildren) false else myKey == otherKey

        }

        override fun hashCode(): Int {
            val itemKey = with (dataExtractor) { data.identityKey }
            var result = if (isOpened) 1 else 0
            result = 31 * result + numChildren
            result = 31 * result + itemKey.hashCode()
            return result
        }

        companion object {
            internal fun <T : Any> createInvalid(): TreeNodeImpl<T, *> = TreeNodeImpl<T, TypeMarker>()
        }
    }

    fun isExpanded(item: T): Boolean {
        var i = 0
        val sz = flattened.size
        while (i < sz) {
            val treeNode = flattened[i]
            if (itemEqual(treeNode.data, item)) {
                return treeNode.isOpened
            }
            i++
        }
        return false
    }

    fun openAt(visibleIndex: Int, notify: Boolean): Boolean {
        if (visibleIndex >= 0 && visibleIndex < flattened.size) {
            val tTreeNode = flattened[visibleIndex]
            return tTreeNode.open(visibleIndex, notify)
        }
        return false
    }

    open fun open(visibleItem: T, notify: Boolean): Boolean {
        var i = 0
        val sz = flattened.size
        while (i < sz) {
            if (itemEqual(flattened[i].data, visibleItem)) {
                return openAt(i, notify)
            }
            i++
        }
        return false
    }

    fun closeAt(visibleIndex: Int, notify: Boolean): Boolean {
        if (visibleIndex >= 0 && visibleIndex < flattened.size) {
            val tTreeNode = flattened[visibleIndex]
            return tTreeNode.close(visibleIndex, notify)
        }
        return false
    }

    open fun close(visibleItem: T, notify: Boolean): Boolean {
        var i = 0
        val sz = flattened.size
        while (i < sz) {
            if (itemEqual(flattened[i].data, visibleItem)) {
                return closeAt(i, notify)
            }
            i++
        }
        return false
    }

    companion object {

        @Suppress("UNCHECKED_CAST")
        private fun <T : Any> itemEqual(aValue: T, bValue: T): Boolean {
            if (aValue === bValue) {
                return true
            }

            return if (aValue is Comparable<*>) {
                (aValue as Comparable<T>).compareTo(bValue) == 0
            } else {
                aValue == bValue
            }
        }
    }

}


interface DataExtractor<T : Any> {
    val T.identityKey: Any
    val T.children: List<T>
}

data class TreeExtractedData<T : Any, VM : TypeMarker>(val node: TreeNode<*>, val data: T, val viewModel: VM?)