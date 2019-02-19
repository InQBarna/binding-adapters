package com.inqbarna.adapters

/**
 * @author David García (david.garcia@inqbarna.com)
 * @version 1.0 2019-02-19
 */
open class LegacyTreeBindingAdapter<T : NestableMarker<T>> : TreeBindingAdapter<NestableMarkerToNestableAdapter<T>, T>(NestableMarkerWrappedFactory()) {


    protected val legacyToplevelItemsData: List<T>
        get() = toplevelItemsData.map { it.item }

    protected val legacyFlattenedItemsData: List<T>
        get() = flattenedItemsData.map { it.item }

    fun setLegacyItems(items: List<T>) {
        setItems(items.map { NestableMarkerToNestableAdapter(it) })
    }

    fun isExpanded(item: T): Boolean = super.isExpanded(NestableMarkerToNestableAdapter(item))

    @JvmOverloads fun close(item: T, notify: Boolean = true): Boolean = close(NestableMarkerToNestableAdapter(item), notify)
    @JvmOverloads fun open(item: T, notify: Boolean = true): Boolean = open(NestableMarkerToNestableAdapter(item), notify)


    protected fun findTopLevelNodeThat(predicate: (T) -> Boolean): TreeNode<*>? {
        return toplevelItems.firstOrNull { predicate(it.data.item) }
    }

    protected fun findFlattenedNodeThat(predicate: (T) -> Boolean): TreeNode<*>? {
        return flattenedItems.firstOrNull { predicate(it.data.item) }
    }

    protected fun NestableMarkerToNestableAdapter<T>.legacyGetItem(): T = this.item
}

class NestableMarkerToNestableAdapter<T : NestableMarker<T>>(internal val item: T)
    : Nestable<NestableMarkerToNestableAdapter<T>>
{
    override val children: List<NestableMarkerToNestableAdapter<T>> = item.children().map { NestableMarkerToNestableAdapter(it) }
    override val identityKey: Any
        get() = item.key

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NestableMarkerToNestableAdapter<*>) return false

        if (item.key != other.item.key) return false

        return true
    }

    override fun hashCode(): Int {
        return item.key.hashCode()
    }
}

private class NestableMarkerWrappedFactory<T : NestableMarker<T>> : TreeItemVMFactory<NestableMarkerToNestableAdapter<T>, T> {
    override fun createViewModel(item: NestableMarkerToNestableAdapter<T>): T {
        return item.item
    }
}