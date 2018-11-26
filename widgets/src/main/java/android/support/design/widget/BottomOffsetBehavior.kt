package android.support.design.widget

import android.content.Context
import android.graphics.Rect
import android.support.v4.view.GravityCompat
import android.support.v4.view.ViewCompat
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import com.inqbarna.widgets.R
import timber.log.Timber

/**
 * @author David García (david.garcia@inqbarna.com)
 * @version 1.0 24/03/2018
 */
class BottomOffsetBehavior : CoordinatorLayout.Behavior<View> {

    private val targetId: Int
    private val tempRect1 = Rect()
    private val tempRect2 = Rect()
    private var spaceUsed = 0
    private var tempTopBottomOffset = 0
    private var tempLeftRightOffset = 0

    private var offsetHelper: ViewOffsetHelper? = null

    constructor() {
        targetId = 0
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.BottomOffsetBehavior)
        targetId = ta.getResourceId(R.styleable.BottomOffsetBehavior_behavior_scrolling_view_id, 0)
        if (targetId != 0) {
            val resName = context.resources.getResourceEntryName(targetId)
            Timber.d("Will use '%s' as reference", resName)
        }
        ta.recycle()
    }
    
    override fun layoutDependsOn(parent: CoordinatorLayout, child: View, dependency: View): Boolean {
        return targetId != 0 && targetId == dependency.id
    }

    override fun onDependentViewChanged(parent: CoordinatorLayout, child: View, dependency: View): Boolean {
        spaceUsed = (dependency.measuredHeight - dependency.translationY).toInt()
        child.requestLayout()
        return true
    }

    override fun onDependentViewRemoved(parent: CoordinatorLayout, child: View, dependency: View) {
        layoutChild(parent, child, ViewCompat.getLayoutDirection(parent))
    }

    override fun onMeasureChild(parent: CoordinatorLayout, child: View,
                                parentWidthMeasureSpec: Int, widthUsed: Int, parentHeightMeasureSpec: Int,
                                heightUsed: Int): Boolean {
        val childLpHeight = child.layoutParams.height
        if (childLpHeight == ViewGroup.LayoutParams.MATCH_PARENT || childLpHeight == ViewGroup.LayoutParams.WRAP_CONTENT) {
            // If the menu's height is set to match_parent/wrap_content then measure it
            // with the maximum visible height

            val dependencies = parent.getDependencies(child)
            val footer = findFirstDependency(dependencies)
            if (footer != null) {
                var availableHeight = View.MeasureSpec.getSize(parentHeightMeasureSpec)
                if (availableHeight == 0) {
                    // If the measure spec doesn't specify a size, use the current height
                    availableHeight = parent.height
                }

                val height = availableHeight - spaceUsed
                val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(height,
                        if (childLpHeight == ViewGroup.LayoutParams.MATCH_PARENT)
                            View.MeasureSpec.EXACTLY
                        else
                            View.MeasureSpec.AT_MOST)

                // Now measure the scrolling view with the correct height
                parent.onMeasureChild(child, parentWidthMeasureSpec,
                        widthUsed, heightMeasureSpec, heightUsed)

                return true
            }
        }
        return false
    }

    private fun findFirstDependency(dependencies: List<View>): View? {
        return dependencies.firstOrNull()
    }

    private fun getOrCreate(child: View): ViewOffsetHelper {
        if (null == offsetHelper) {
            offsetHelper = ViewOffsetHelper(child)
        }
        return offsetHelper!!
    }

    override fun onLayoutChild(parent: CoordinatorLayout, child: View, layoutDirection: Int): Boolean {

        layoutChild(parent, child, layoutDirection)

        val offsetHelper = getOrCreate(child)

        offsetHelper.onViewLayout()

        if (tempTopBottomOffset != 0) {
            offsetHelper.topAndBottomOffset = tempTopBottomOffset
            tempTopBottomOffset = 0
        }
        if (tempLeftRightOffset != 0) {
            offsetHelper.leftAndRightOffset = tempLeftRightOffset
            tempLeftRightOffset = 0
        }

        return true
    }

    private fun layoutChild(parent: CoordinatorLayout, child: View,
                            layoutDirection: Int) {
        val dependencies = parent.getDependencies(child)
        val footer = findFirstDependency(dependencies)

        if (footer != null) {
            val lp = child.layoutParams as CoordinatorLayout.LayoutParams
            val available = tempRect1
            available.set(parent.paddingLeft + lp.leftMargin,
                    parent.paddingTop + lp.topMargin,
                    parent.width - parent.paddingRight - lp.rightMargin,
                    parent.height - spaceUsed
                            - parent.paddingBottom - lp.bottomMargin)

            val out = tempRect2
            GravityCompat.apply(resolveGravity(lp.gravity), child.measuredWidth,
                    child.measuredHeight, available, out, layoutDirection)

            child.layout(out.left, out.top, out.right, out.bottom)
        } else {
            // If we don't have a dependency, let parent handle it
            parent.onLayoutChild(child, layoutDirection)
        }
    }

    private fun resolveGravity(gravity: Int): Int {
        return if (gravity == Gravity.NO_GRAVITY) GravityCompat.START or Gravity.TOP else gravity
    }
}