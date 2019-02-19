package com.inqbarna.adapters

/**
 * @author David García (david.garcia@inqbarna.com)
 * @version 1.0 2019-02-19
 */
open class LegacyTreeBindingAdapter<T : NestableMarker<T>> : TreeBindingAdapter<T, T>(LegacyNestableDataExtractor<T>(), IdentityItemFactory())

private class LegacyNestableDataExtractor<T : NestableMarker<T>> : DataExtractor<T> {
    override val T.identityKey: Any
        get() = key
    override val T.children: List<T>
        get() = children()
}

private class IdentityItemFactory<T : NestableMarker<T>> : TreeItemVMFactory<T, T> {
    override fun createViewModel(item: T): T = item
}