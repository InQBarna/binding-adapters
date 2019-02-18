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
package com.inqbarna.libsamples

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Rect
import android.util.TypedValue
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.inqbarna.adapters.BasicBindingAdapter
import com.inqbarna.adapters.ItemBinder
import com.inqbarna.adapters.ItemOffsetProvider
import com.inqbarna.adapters.TypeMarker
import com.inqbarna.adapters.addOffsetDecoration

class OffsetsActivity : ListBaseActivity<OffsetItem>() {
    private val items = createItems()

    private val offsetsProvider =
        ItemOffsetProvider(items) { context: Context, rect: Rect ->
            val res = context.resources
            var horizontalDps = 10
            var verticalDps = 5
            when (this) {
                is OffsetItem.Highlighted -> verticalDps = 30
                is OffsetItem.Standard -> horizontalDps = 20
                is OffsetItem.Special -> {
                    horizontalDps = 60
                    verticalDps = 40
                }
            }

            with(rect) {
                left = horizontalDps.toPx(res)
                top = verticalDps.toPx(res)
                right = horizontalDps.toPx(res)
                bottom = verticalDps.toPx(res)
            }
        }

    private fun Int.toPx(resources: Resources) = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        toFloat(),
        resources.displayMetrics
    ).toInt()

    override fun setupRecycler(recycler: RecyclerView) {
        recycler.run {
            addOffsetDecoration(offsetsProvider)
            layoutManager = LinearLayoutManager(recycler.context)
        }
    }


    override fun createAdapter() =
        BasicBindingAdapter<OffsetItem>(ItemBinder { variableBinding, pos, dataAtPos ->
            variableBinding.bindValue(BR.model, dataAtPos)
        }).apply {
            setItems(items)
        }

    private fun createItems() = listOf(
        OffsetItem.Highlighted("Highlighted 1"),
        OffsetItem.Standard("Standard 1"),
        OffsetItem.Standard("Standard 3"),
        OffsetItem.Standard("Standard 4"),
        OffsetItem.Special("Special 1")
    )

    companion object {
        fun getCallingIntent(context: Context): Intent {
            return Intent(
                context,
                OffsetsActivity::class.java
            )
        }
    }

}

sealed class OffsetItem(
    val label: String
) : TypeMarker {
    class Highlighted(text: String) : OffsetItem(text)
    class Standard(text: String) : OffsetItem(text)
    class Special(text: String) : OffsetItem(text)

    override fun getItemType() = R.layout.offset_item
}