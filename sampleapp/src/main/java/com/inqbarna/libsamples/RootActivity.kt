@file:JvmName("Root")
package com.inqbarna.libsamples

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

class TargetActivity(val name: String, private val mIntent: Intent, private val mLauncher: Launcher) : TypeMarker {

    fun launch() {
        mLauncher.launch(mIntent)
    }

    override fun getItemType(): Int {
        return R.layout.item_activity
    }
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
                TargetActivity("Dynamic Offset Items", OffsetsActivity.getCallingIntent(thisRef), thisRef),
                TargetActivity("Test pager", TestPagerAdapter.getCallingIntent(thisRef), thisRef)
        )
    }
}
