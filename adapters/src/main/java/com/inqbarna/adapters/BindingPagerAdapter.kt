package com.inqbarna.adapters

import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.support.v4.view.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlin.properties.Delegates

/**
 * @author David García <david.garcia@inqbarna.com>
 * @version 1.0 11/10/2017
 */

open class BasicPagerAdapter<T : TypeMarker> @JvmOverloads constructor(varId : Int, items : List<T> = emptyList(), bindingComponent : android.databinding.DataBindingComponent?
 = null) : BindingPagerAdapter<T>(varId, bindingComponent) {
    var items : List<T> by Delegates.observable(items) { _, _, _ -> notifyDataSetChanged() }

    fun <D> setData(items : Iterable<D>, conv : (D) -> T) {
        setData(items.map(conv))
    }

    fun setData(items: Iterable<T>) {
        this.items = items.toList()
    }

    override fun onItemBound(item : T) {
        /* no-op */
    }

    override fun getCount() : Int {
        return items.size
    }

    override fun getDataAt(pos : Int) : T {
        return items[pos]
    }

    fun itemIndex(predicate : (T) -> Boolean) : Int {
        return items.indexOfFirst(predicate)
    }

    override fun onBindingDestroyed(destroyedBinding : ViewDataBinding) {
        /* no-op */
    }

    companion object {
        @JvmStatic
        fun <R, T : TypeMarker> ofItems(varId : Int, items : List<R>, conv : (R) -> T) = BasicPagerAdapter(varId, items.map(conv).toList())
    }
}

abstract class BindingPagerAdapter<T : TypeMarker>() : PagerAdapter() {
    private val helper : PagerAdapterHelper = PagerAdapterHelper()

    @JvmOverloads constructor(varId : Int, bindingComponent : android.databinding.DataBindingComponent? = null) : this() {
        setBinder(BasicItemBinder(varId))
        bindingComponent?.also { setBindingComponent(it) }
    }

    protected fun setBinder(binder : ItemBinder) {
        helper.binder = binder
    }

    protected fun setBindingComponent(bindingComponent : android.databinding.DataBindingComponent) {
        helper.bindingComponent = bindingComponent
    }

    final override fun getItemPosition(`object` : Any) : Int {
        val viewDataBinding = `object` as? ViewDataBinding
        return getPositionOf(viewDataBinding.recoverData())
    }

    override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
        super.setPrimaryItem(container, position, `object`)
    }

    protected open fun getPositionOf(data : T?) : Int {
        return PagerAdapter.POSITION_NONE
    }

    final override fun isViewFromObject(view : View, `object` : Any) : Boolean = helper.isViewFromObject(view, `object`)

    final override fun instantiateItem(container : ViewGroup, position : Int) : Any {
        val dataAt = getDataAt(position)
        val instantiateItem = helper.instantiateItem(container, position, dataAt)
        onItemBound(dataAt)
        return instantiateItem
    }

    abstract fun getDataAt(pos : Int) : T
    abstract fun onItemBound(item : T)
    abstract fun onBindingDestroyed(destroyedBinding : ViewDataBinding)


    final override fun destroyItem(container : ViewGroup, position : Int, `object` : Any) {
        helper.destroyItem(container, `object`)?.let { onBindingDestroyed(it) }
    }

    companion object {
        @JvmStatic
        fun <T : TypeMarker> ViewDataBinding?.storeData(data : T) = this?.let { root.setTag(R.id.bindingData, data) }

        @Suppress("UNCHECKED_CAST")
        @JvmStatic
        fun <T : TypeMarker> ViewDataBinding?.recoverData() : T? = this?.let { root.getTag(R.id.bindingData) as? T }
    }
}


internal class PagerAdapterHelper(var bindingComponent : android.databinding.DataBindingComponent? = null) {
    internal lateinit var binder : ItemBinder
    fun isViewFromObject(view : View, any : Any) : Boolean = if (any is ViewDataBinding) any == DataBindingUtil.getBinding(view) else false

    fun instantiateItem(container : ViewGroup, position : Int, dataAt : TypeMarker) : ViewDataBinding {
        val viewDataBinding = DataBindingUtil.inflate<ViewDataBinding>(LayoutInflater.from(container.context), dataAt.itemType, container, true, bindingComponent)
        with(BindingPagerAdapter) {
            viewDataBinding.storeData(dataAt)
        }
        binder.bindVariables(VariableBinding { variable, value -> viewDataBinding.setVariable(variable, value) }, position, dataAt)
        viewDataBinding.executePendingBindings()
        return viewDataBinding
    }

    fun destroyItem(container : ViewGroup, any : Any) : ViewDataBinding? {
        var i = 0
        val sz = container.childCount
        while (i < sz) {
            val childAt = container.getChildAt(i)
            val binding = DataBindingUtil.getBinding<ViewDataBinding>(childAt)
            if (binding === any) {
                container.removeView(childAt)
                return binding
            }
            i++
        }
        return null
    }
}
