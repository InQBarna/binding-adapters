package com.inqbarna.adapters

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by Ricard Aparicio on 11/02/2019.
 * ricard.aparicio@inqbarna.com
 */

/**
 * Method to set OffsetItemDecoration dynamically or inside xml file: (e.g app:addItemOffsets="@{model.offsets}")
 */
@BindingAdapter("addItemOffsets")
fun RecyclerView.addOffsetDecoration(offsetProvider: OffsetProvider) {
    addItemDecoration(OffsetItemDecoration(offsetProvider))
}

/**
 * Class to manage item offsets according its type
 */
class ItemOffsetProvider<T>(
    val items: List<T>,
    private val computeOffsets: T.(Context, Rect) -> Unit
) : OffsetProvider {
    override fun applyOffset(context: Context, idx: Int, outRect: Rect) {
        val any = items[idx]
        any.computeOffsets(context, outRect)
    }
}

interface OffsetProvider {
    fun applyOffset(context: Context, idx: Int, outRect: Rect)
}

/**
 * Base offset item decoration class.
 */
class OffsetItemDecoration(private val offsetProvider: OffsetProvider) :
    RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val idx = parent.getChildAdapterPosition(view)
        if (idx == RecyclerView.NO_POSITION) {
            return
        }
        offsetProvider.applyOffset(view.context, idx, outRect)
    }
}
