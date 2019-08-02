package com.inqbarna.adapters

import android.graphics.Canvas
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by Ricard Aparicio on 2019-08-02.
 * ricard.aparicio@inqbarna.com
 */
@BindingAdapter("addStickyHeader")
fun RecyclerView.addStickyHeader(resolver: StickyHeaderResolver) {
    addItemDecoration(StickyHeaderItemDecoration(resolver))
}

class StickyHeaderItemDecoration(@param:NonNull private val headerResolver: StickyHeaderResolver) :
    RecyclerView.ItemDecoration() {

    private var currentHeaderPair: Pair<View, TypeMarker>? = null

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)

        val topChild = parent.getChildAt(0) ?: return

        val topChildPosition = parent.getChildAdapterPosition(topChild)
        if (topChildPosition == RecyclerView.NO_POSITION) return

        // Header
        val header = getHeaderViewForPos(topChildPosition, parent)
        fixLayoutSize(parent, header)

        // Contact point
        val childInContact = getChildInContact(parent, header.bottom) ?: return

        if (this.headerResolver.isHeader(parent.getChildAdapterPosition(childInContact))) {
            onMoveHeader(c, header, childInContact)
            return
        }

        onDrawHeader(c, header)
    }

    private fun getHeaderViewForPos(itemPosition: Int, parent: RecyclerView): View {
        val item = headerResolver.getItemHeaderForPos(itemPosition)

        // Return same view instance if necessary, avoiding multiple bindings since #onDrawOver is called multiple times
        currentHeaderPair?.let {
            if (item == it.second) return it.first
        }

        val binding = DataBindingUtil.inflate<ViewDataBinding>(
            LayoutInflater.from(parent.context),
            item.itemType,
            parent,
            false
        )

        // Binding item
        with(headerResolver) { binding.bindHeader(item) }
        binding.executePendingBindings()

        val view = binding.root
        currentHeaderPair = view to item

        return view
    }

    private fun onDrawHeader(c: Canvas, header: View) {
        c.save()
        c.translate(0f, 0f)
        header.draw(c)
        c.restore()
    }

    private fun onMoveHeader(c: Canvas, currentHeader: View, nextHeader: View) {
        c.save()
        c.translate(0f, (nextHeader.top - currentHeader.height).toFloat())
        currentHeader.draw(c)
        c.restore()
    }

    private fun getChildInContact(parent: RecyclerView, contactPoint: Int): View? {
        var childInContact: View? = null
        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            if (child.bottom > contactPoint) {
                if (child.top <= contactPoint) {
                    // This child overlaps the contactPoint
                    childInContact = child
                    break
                }
            }
        }
        return childInContact
    }

    private fun fixLayoutSize(parent: ViewGroup, view: View) {
        // Specs for parent (RecyclerView)
        val widthSpec = View.MeasureSpec.makeMeasureSpec(parent.width, View.MeasureSpec.EXACTLY)
        val heightSpec =
            View.MeasureSpec.makeMeasureSpec(parent.height, View.MeasureSpec.UNSPECIFIED)

        // Specs for children (headers)
        val childWidthSpec = ViewGroup.getChildMeasureSpec(
            widthSpec,
            parent.paddingLeft + parent.paddingRight,
            view.layoutParams.width
        )
        val childHeightSpec = ViewGroup.getChildMeasureSpec(
            heightSpec,
            parent.paddingTop + parent.paddingBottom,
            view.layoutParams.height
        )

        view.measure(childWidthSpec, childHeightSpec)
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
    }
}

interface StickyHeaderResolver {
    fun getItemHeaderForPos(idx: Int): TypeMarker
    fun ViewDataBinding.bindHeader(item: TypeMarker)
    fun isHeader(itemPosition: Int): Boolean
}

class StickyHeaderProvider<T : TypeMarker>(
    private val items: List<T>,
    private val varBind: Int,
    private val isHeader: T.() -> Boolean
) : StickyHeaderResolver {

    override fun getItemHeaderForPos(idx: Int): TypeMarker {
        var header: T? = null
        for ((i, item) in items.withIndex()) {
            if (isHeader(item)) {
                header = item
            }
            if (i == idx) {
                break
            }
        }
        return header
            ?: throw IllegalArgumentException("You must provide at least one item as header if you want to use StickyHeaders")
    }


    override fun ViewDataBinding.bindHeader(item: TypeMarker) {
        setVariable(varBind, item)
    }

    override fun isHeader(itemPosition: Int) = isHeader(items[itemPosition])
}