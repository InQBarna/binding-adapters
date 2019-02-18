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

package com.inqbarna.widgets

import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout

@CoordinatorLayout.DefaultBehavior(FooterLayout.FooterLayoutBehavior::class)
class FooterLayout : FrameLayout {
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        initializeComponent(context, attrs, defStyleAttr, defStyleRes)
    }
    @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : super(context, attrs, defStyleAttr) {
        initializeComponent(context, attrs, defStyleAttr, 0)
    }

    private fun initializeComponent(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.FooterLayout, defStyleAttr, defStyleRes)
        val hidden = typedArray.getBoolean(R.styleable.FooterLayout_footer_hidden, false)
        typedArray.recycle()

        this.hidden = hidden
        needsUpdate = true
    }

    private var showRatio: Float = 0.0f
        set(v) {
            field = maxOf(0.0f, minOf(v, 1.0f))
            applyTranslation(field)
            if (field == 1.0f) {
                hidden = false
            } else if (field == 0.0f) {
                hidden = true
            }
        }

    private fun applyTranslation(ratio: Float) {
        if (isEnabled) {
            if (measuredHeight != 0) {
                translationY = measuredHeight * (1 - ratio)
            }
        }
    }

    var hidden: Boolean = false
        set(newVal) {
            if (newVal != field) {
                field = newVal
                showRatio = if (field) 0.0f else 1.0f
                onFooterHideStateChangeListener?.onFooterHideStateChanged(field)
            }
        }

    private var needsUpdate: Boolean = false
    private var animator: Animator? = null
    var onFooterEnabledListener: OnFooterEnabledListener? = null
    var onFooterHideStateChangeListener: OnFooterHideStateChangeListener? = null

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val initialHeight = measuredHeight
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (initialHeight != measuredHeight) {
            applyTranslation(showRatio)
            needsUpdate = false
        }
    }

    private fun cancelAnimation() {
        animator?.cancel()
        animator = null
    }

    fun dismiss() {
        val initialValue = translationY
        cancelAnimation()
        val animator = ValueAnimator.ofFloat(initialValue, measuredHeight.toFloat())
        animator.addUpdateListener { a -> translationY = a.animatedValue as Float }
        animator.interpolator = LinearInterpolator()
        isEnabled = false
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationEnd(animation: Animator?) {
                translationY = measuredHeight.toFloat()

            }

            override fun onAnimationCancel(animation: Animator?) {
                showRatio = initialValue
                isEnabled = true
            }

            override fun onAnimationRepeat(animation: Animator?) {

            }

            override fun onAnimationStart(animation: Animator?) {
                this@FooterLayout.animator = animation
            }
        })
        animator.setTarget(this)
        animator.start()
    }

    override fun setEnabled(enabled: Boolean) {
        if (this.isEnabled != enabled) {
            super.setEnabled(enabled)
            applyTranslation(showRatio)
            requestLayout()
            onFooterEnabledListener?.onFooterEnabledState(enabled)
        }
    }

    interface OnFooterEnabledListener {
        fun onFooterEnabledState(enabled: Boolean)
    }

    interface OnFooterHideStateChangeListener {
        fun onFooterHideStateChanged(hidden: Boolean)
    }

    class FooterLayoutBehavior : CoordinatorLayout.Behavior<FooterLayout> {

        constructor()
        constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

        override fun onNestedPreScroll(coordinatorLayout: CoordinatorLayout, child: FooterLayout, target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
            // If we're scrolling down, then steal movement from target
            val measuredHeight = child.measuredHeight
            if (dy < 0 && measuredHeight != 0) {
                val currentRatio = child.showRatio
                var diffRatio = dy diffAsRatioOf measuredHeight
                if (currentRatio > 0f) {
                    diffRatio = minOf(currentRatio, diffRatio)
                    doFooterDisplacement(child, diffRatio)
                    consumed[0] = 0
                    consumed[1] = diffRatio ratioDiffAsDisplacement measuredHeight
                }
            }
        }



        private fun doFooterDisplacement(child: FooterLayout, diffRatio: Float) {
            child.showRatio -= diffRatio
        }

        override fun onNestedScroll(coordinatorLayout: CoordinatorLayout, child: FooterLayout, target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, type: Int) {
            val measuredHeight = child.measuredHeight
            if (dyUnconsumed > 0 && measuredHeight != 0) {
                val currentRatio = child.showRatio
                if (currentRatio < 1.0f) {
                    // user is trying to scroll up, and target view is not doing so..., move footer up
                    var diffRatio = dyUnconsumed diffAsRatioOf measuredHeight
                    diffRatio = maxOf(diffRatio, currentRatio - 1.0f)
                    doFooterDisplacement(child, diffRatio)
                }
            }
        }

        override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout, child: FooterLayout, directTargetChild: View, target: View, axes: Int, type: Int): Boolean {
            return (axes or ViewCompat.SCROLL_AXIS_VERTICAL) != 0
        }

        companion object {
            private infix fun Int.diffAsRatioOf(height: Int): Float {
                check(height > 0) { "Invalid height, expected value greater than 0" }
                return (-1.0f * this) / height
            }

            private infix fun Float.ratioDiffAsDisplacement(height: Int): Int {
                return ((this * height) * -1.0).toInt()
            }
        }
    }
}