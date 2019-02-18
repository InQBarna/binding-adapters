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
