@file:JvmName("Root")
package com.inqbarna.libsamples

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.inqbarna.adapters.*
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty


abstract class ListBaseActivity<T : TypeMarker> : AppCompatActivity() {
    internal lateinit var adapter: BasicBindingAdapter<T>

    @JvmField
    @BindView(R.id.list)
    internal var recycler : RecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.simply_list)
        ButterKnife.bind(this)
        adapter = createAdapter()
        val aRecycler : RecyclerView? = recycler
        if (null != aRecycler) {
            setupRecycler(aRecycler)
            aRecycler.adapter = adapter
        }
    }

    abstract fun setupRecycler(recycler: RecyclerView)

    abstract fun createAdapter(): BasicBindingAdapter<T>
}

open class RootActivity : ListBaseActivity<TargetActivity>(), Launcher {

    private val mList : List<TargetActivity> by OptionsDelegate()

    override fun setupRecycler(recycler: RecyclerView) {
        recycler.layoutManager = GridLayoutManager(this, 2)
    }

    override fun createAdapter(): BasicBindingAdapter<TargetActivity> {
        val adapter  = BasicBindingAdapter<TargetActivity>(ItemBinder { variableBinding, pos, dataAtPos -> variableBinding.bindValue(BR.model, dataAtPos)})
        adapter.setItems(mList)
        return adapter
    }

    override fun launch(intent: Intent) {
        startActivity(intent)
    }
}

open class NumbersActivity : ListBaseActivity<NumberVM>() {

    companion object {
        fun getCallingIntent(context: Context) : Intent {
            return Intent(context, NumbersActivity::class.java)
        }
    }


    val toggler : Toggler = object : Toggler {
        override fun toggleItem(groupItem: GroupIndicator) {
            val head = groupItem as? GroupHead
            if (null != head) {
                val res = GroupController.updateGroupWithColor(itemList, head, head.groupSize(), !head.enabled(), head.attributes().color())
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
        val adapter  = BasicBindingAdapter<NumberVM>(ItemBinder { variableBinding, pos, dataAtPos -> variableBinding.bindValue(BR.model, dataAtPos)})
        adapter.setItems(itemList)
        return adapter
    }


    private fun createItems(max: Int): MutableList<out NumberVM> {
        val mutableList = MutableList<NumberVM>(max) {
            NumberVM(it, toggler)
        }
        var vm = mutableList.get(4)
        mutableList.set(4, HeadNumberVM(Color.GREEN, 8, vm))

        vm = mutableList.get(23)
        mutableList.set(23, HeadNumberVM(Color.BLUE, 11, vm))
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

class HeadNumberVM(color : Int, val size : Int, numberVM : NumberVM) : NumberVM(numberVM.number, numberVM.toggler), GroupHead {
    init {
        attributes().setColor(color)
    }
    override fun groupSize(): Int {
        return size
    }
}

class TargetActivity(val name: String, private val mIntent: Intent, private val mLauncher: Launcher) : TypeMarker {

    fun launch() {
        mLauncher.launch(mIntent)
    }

    override fun getItemType(): Int {
        return R.layout.item_activity
    }
}

interface Toggler {
    fun toggleItem(groupItem : GroupIndicator)
}

interface Launcher {
    fun launch(intent: Intent)
}

class OptionsDelegate : ReadOnlyProperty<RootActivity, List<TargetActivity>> {
    override fun getValue(thisRef: RootActivity, property: KProperty<*>): List<TargetActivity> {
        return listOf(
                TargetActivity("Paging Adapter", MainActivity.getCallingIntent(thisRef), thisRef),
                TargetActivity("Numbers Activity", NumbersActivity.getCallingIntent(thisRef), thisRef),
                TargetActivity("Bottom Bar Progress", TestBottomSheetActivity.getCallingIntent(thisRef), thisRef),
                TargetActivity("Test pager", TestPagerAdapter.getCallingIntent(thisRef), thisRef)
        )
    }
}
