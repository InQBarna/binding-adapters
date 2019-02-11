package com.inqbarna.libsamples

import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.inqbarna.adapters.*

open class NumbersActivity : ListBaseActivity<NumberVM>() {

    companion object {
        fun getCallingIntent(context: Context) : Intent {
            return Intent(
                context,
                NumbersActivity::class.java
            )
        }
    }


    val toggler : Toggler = object : Toggler {
        override fun toggleItem(groupItem: GroupIndicator) {
            val head = groupItem as? GroupHead
            if (null != head) {
                val res = GroupController.updateGroupWithColor(
                    itemList,
                    head,
                    head.groupSize(),
                    !head.enabled(),
                    head.attributes().color()
                )
                res.notifyOn(adapter)
            }
        }
    }

    override fun setupRecycler(recycler: RecyclerView) {
        recycler.layoutManager = GridLayoutManager(this, 4)
        recycler.addItemDecoration(GroupDecorator())
    }

    private val itemList: List<NumberVM> = createItems(1000)

    override fun createAdapter(): BasicBindingAdapter<NumberVM> {
        val adapter  =
            BasicBindingAdapter<NumberVM>(ItemBinder { variableBinding, pos, dataAtPos ->
                variableBinding.bindValue(
                    BR.model,
                    dataAtPos
                )
            })
        adapter.setItems(itemList)
        return adapter
    }


    private fun createItems(max: Int): MutableList<out NumberVM> {
        val mutableList = MutableList<NumberVM>(max) {
            NumberVM(it, toggler)
        }
        var vm = mutableList.get(4)
        mutableList.set(4,
            HeadNumberVM(Color.GREEN, 8, vm)
        )

        vm = mutableList.get(23)
        mutableList.set(23,
            HeadNumberVM(Color.BLUE, 11, vm)
        )
        return mutableList
    }
}

open class NumberVM(val number : Int, internal val toggler: Toggler) : TypeMarker, GroupIndicator by BasicIndicatorDelegate() {

    val numberStr: String
        get() = number.toString()

    override fun getItemType(): Int {
        return R.layout.number_item
    }

    fun toggle(indicator: GroupIndicator) {
        toggler.toggleItem(indicator)
    }
}

class HeadNumberVM(color : Int, val size : Int, numberVM : NumberVM) : NumberVM(numberVM.number, numberVM.toggler),
    GroupHead {
    init {
        attributes().setColor(color)
    }
    override fun groupSize(): Int {
        return size
    }
}

interface Toggler {
    fun toggleItem(groupItem : GroupIndicator)
}