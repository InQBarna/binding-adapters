package com.inqbarna.navigation

import android.app.Activity
import android.view.View
import android.view.ViewParent
import androidx.fragment.app.Fragment
import com.inqbarna.navigation.base.Navigator

/**
 * @author David García (david.garcia@inqbarna.com)
 * @version 1.0 05/07/2018
 */
object Navigation {

    @JvmStatic
    fun View.installNavigation(navigator: Navigator) = installItemWithTag(R.id.iq_tag_navigator, navigator)

    @JvmStatic
    fun Activity.installNavigation(navigator: Navigator) {
        findViewById<View>(android.R.id.content).installNavigation(navigator)
    }

    @JvmStatic
    fun Activity.getRootView(): View = findViewById(android.R.id.content)

    @JvmStatic
    fun Activity.findNavigation(): Navigator {
        return findViewById<View>(android.R.id.content).findNavigation()
    }

    @JvmStatic
    fun View.findNavigation(): Navigator = searchItemWithTag(View::getNavigation)

    @JvmStatic fun Activity.createIntentLauncher(): IntentLauncher = ActivityLauncher(this)
    @JvmStatic fun Fragment.createIntentLauncher(): IntentLauncher = FragmentLauncher(this)
}

private fun <T> View.searchItemWithTag(getter: View.() -> T?): T {
    var view: View? = this
    while (view != null) {
        val navigator = view.getter()
        if (null != navigator) {
            return navigator
        }
        view = view.searchParent()
    }
    throw IllegalStateException("This view hierarchy hasn't a navigation installed")
}

private fun View.searchParent(): View? {
    var viewParent: ViewParent? = parent
    while (viewParent != null && viewParent !is View) {
        viewParent = viewParent.parent
    }
    return viewParent as? View
}

private fun View.getNavigation(): Navigator? = getTag(R.id.iq_tag_navigator) as? Navigator
private fun <T> View.installItemWithTag(tagId: Int, item: T) = setTag(tagId, item)
