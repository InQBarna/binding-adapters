package com.inqbarna.adapters

/**
 * @author David García (david.garcia@inqbarna.com)
 * @version 1.0 2019-02-19
 */
interface TreeItemVMFactory<ItemType : Nestable<ItemType>, out VMType : TypeMarker> {
    fun createViewModel(item: ItemType): VMType
}