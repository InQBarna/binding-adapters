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
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableBoolean
import android.os.Bundle
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.ViewPropertyAnimatorListener
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import android.util.AttributeSet
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import com.inqbarna.adapters.BasicBindingAdapter
import com.inqbarna.adapters.BasicItemBinder
import com.inqbarna.libsamples.databinding.ActivityBottomSheetBinding
import com.inqbarna.libsamples.vm.TestVM
import com.inqbarna.widgets.FooterLayout

/**
 * @author David García (david.garcia@inqbarna.com)
 * @version 1.0 24/03/2018
 */
class TestBottomSheetActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBottomSheetBinding
    private lateinit var model: TestBottomVM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_bottom_sheet)
        model = TestBottomVM(this)
        binding.model = model
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.bottom_test, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        super.onPrepareOptionsMenu(menu)
        val item = menu.findItem(R.id.re_enable)
        item.isEnabled = model.enabled.get()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.re_enable -> {
                model.enabled.set(true)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        fun getCallingIntent(context: Context): Intent {
            return Intent(context, TestBottomSheetActivity::class.java)
        }
    }
}


class TestBottomVM(private val context: Context) {
    val adapter: RecyclerView.Adapter<*>
        get() = _adapter

    private val _adapter: BasicBindingAdapter<TestVM> = BasicBindingAdapter(BasicItemBinder(BR.model))

    val enabled: ObservableBoolean = ObservableBoolean(true)

    fun onTakeAction(parentFooter: FooterLayout) {
        Toast.makeText(context, "Action Taken", Toast.LENGTH_LONG).show()
        parentFooter.dismiss()
    }

    init {
        _adapter.setItems((0..50).map { TestVM(it) })
    }
}


class LinearLayoutBehavior(context: Context, attrs: AttributeSet) : CoordinatorLayout.Behavior<LinearLayout>() {
    private var mIsAnimatingOut = false
    private val mInvert: Boolean = false

    protected val isInvert: Boolean
        get() = false

    override fun onStartNestedScroll(
            coordinatorLayout: CoordinatorLayout, child: LinearLayout, directTargetChild: View, target: View, nestedScrollAxes: Int, type: Int): Boolean {
        return (nestedScrollAxes or ViewCompat.SCROLL_AXIS_VERTICAL) != 0 || super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, nestedScrollAxes)
    }

    override fun onNestedScroll(
            coordinatorLayout: CoordinatorLayout, child: LinearLayout, target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int,
            dyUnconsumed: Int, type: Int) {
        if (type == ViewCompat.TYPE_TOUCH) {
            if (dyConsumed > 0 && !this.mIsAnimatingOut && child.getVisibility() == View.VISIBLE) {
                // User scrolled down and the view is currently visible -> hide the view
                animateOut(child)
            } else if (dyConsumed < 0 && child.getVisibility() != View.VISIBLE) {
                // User scrolled up and the view is currently not visible -> show the view
                animateIn(child)
            }
        } else {
            super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type)
        }
    }

    protected fun animateOut(view: View) {
        val measuredHeight = view.measuredHeight
        if (measuredHeight == 0) {
            return
        }
        ViewCompat.animate(view).translationY((if (isInvert) -measuredHeight else measuredHeight).toFloat()).alpha(0.0f).setInterpolator(INTERPOLATOR).withLayer()
                .setListener(object : ViewPropertyAnimatorListener {
                    override fun onAnimationStart(view: View) {
                        this@LinearLayoutBehavior.mIsAnimatingOut = true
                    }

                    override fun onAnimationCancel(view: View) {
                        this@LinearLayoutBehavior.mIsAnimatingOut = false
                    }

                    override fun onAnimationEnd(view: View) {
                        this@LinearLayoutBehavior.mIsAnimatingOut = false
                        view.visibility = View.INVISIBLE
                    }
                }).start()
    }

    private fun animateIn(linearLayout: LinearLayout) {
        linearLayout.setVisibility(View.VISIBLE)
        ViewCompat.animate(linearLayout).translationY(0f).alpha(1.0f)
                .setInterpolator(INTERPOLATOR).withLayer().setListener(null)
                .start()
    }

    companion object {
        private val INTERPOLATOR = FastOutSlowInInterpolator()
    }
}
