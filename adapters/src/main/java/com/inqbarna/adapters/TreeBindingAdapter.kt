package com.inqbarna.adapters

import android.view.View
import com.google.common.collect.ImmutableList
import com.google.common.collect.TreeTraverser
import java.util.*

/**
 * @author David García (david.garcia@inqbarna.com)
 * @version 1.0 31/1/17
 */

open class TreeBindingAdapter<T : Nestable<T>, VMType : TypeMarker>(
        private val factory: TreeItemVMFactory<T, VMType>
) : BindingAdapter() {

    private val flattened = mutableListOf<TreeNodeImpl<T, VMType>>()
    private val tree = mutableListOf<TreeNodeImpl<T, VMType>>()

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

    @JvmOverloads
    fun addItems(items: List<T>, notify: Boolean = true) {
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

    override fun getDataAt(position: Int): TypeMarker {
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
        val ROOT = TreeNodeImpl.createInvalid<T>()
        return Traverser(ROOT)
                .preOrderTraversal(ROOT)
                .filter { input -> input !== ROOT }
    }

    protected fun breadthFirstIterator(node: TreeNode<*>?): Iterable<TreeExtractedData<T, VMType>> {
        val ROOT = TreeNodeImpl.createInvalid<T>()
        val root: TreeNodeImpl<*, *> = when (node) {
            !is TreeNodeImpl<*, *> -> return ImmutableList.of()
            null -> ROOT
            else -> node
        }

        return Traverser(ROOT)
                .breadthFirstTraversal(root)
                .filter { itm -> itm !== ROOT }
                .transform { itm ->
                    TreeExtractedData(itm as TreeNode<T>, itm.data, itm.viewModel as VMType)
                }
    }

    private inner class Traverser(private val ROOT: TreeNodeImpl<*, *>) : TreeTraverser<TreeNodeImpl<*, *>>() {
        override fun children(root: TreeNodeImpl<*, *>): Iterable<TreeNodeImpl<*, *>> {
            return if (root === ROOT) {
                tree
            } else {
                root.childNodes
            }
        }
    }

    private class TreeNodeImpl<T : Nestable<T>, VMType : TypeMarker> : TreeNode<T> {
        private val factory: TreeItemVMFactory<T, VMType>?
        internal val hasChildren: Boolean
        override var parent: TreeNode<T>?
            private set
        override var isOpened: Boolean = false
            private set
        internal val numChildren: Int
        private val _data: T?
        internal val childNodes: List<TreeNodeImpl<T, VMType>>

        private val adapter: TreeBindingAdapter<T, VMType>?
        private var _viewModel: VMType? = null

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
        }

        override val data: T
            get() = requireNotNull(_data) { "This is an invalid node, do not get data from it (check before)" }

        internal val viewModel: VMType
            get() {
                val factory = requireNotNull(factory) { "This is an invalid node, do not get data from it (check before)" }
                return _viewModel ?: factory.createViewModel(data).also { _viewModel = it }
            }

        @JvmOverloads
        constructor(adapter: TreeBindingAdapter<T, VMType>, item: T, parent: TreeNode<T>? = null) {
            this.parent = parent
            this.adapter = adapter
            // closed state by default
            val children = item.children
            numChildren = children.size
            childNodes = children.map { TreeNodeImpl(adapter, it, this) }
            _data = item
            hasChildren = numChildren > 0
            isOpened = false
            factory = adapter.factory
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

        override fun closeChilds(notify: Boolean): Boolean {
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
            if (other == null || javaClass != other.javaClass) return false

            val treeNode = other as TreeNodeImpl<*, *>

            if (isOpened != treeNode.isOpened) return false
            return if (numChildren != treeNode.numChildren) false else data.identityKey == treeNode.data.identityKey

        }

        override fun hashCode(): Int {
            var result = if (isOpened) 1 else 0
            result = 31 * result + numChildren
            result = 31 * result + data.identityKey.hashCode()
            return result
        }

        companion object {
            internal fun <T : Nestable<T>> createInvalid(): TreeNodeImpl<T, *> = TreeNodeImpl<T, TypeMarker>()
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
        private fun <T : Nestable<T>> itemEqual(aValue: T, bValue: T): Boolean {
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

data class TreeExtractedData<T : Any, VM : TypeMarker>(val node: TreeNode<*>, val data: T, val viewModel: VM)

private object InvalidTypeMarker : TypeMarker {
    override fun getItemType(): Int {
        return View.NO_ID
    }
}