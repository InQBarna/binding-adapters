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

package com.inqbarna.navigation

import android.app.Activity
import android.view.View
import android.view.ViewParent
import androidx.fragment.app.Fragment
import com.inqbarna.navigation.base.Navigator
import com.inqbarna.navigation.routes.ActivityLauncher
import com.inqbarna.navigation.routes.FragmentLauncher
import com.inqbarna.navigation.routes.IntentLauncher

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

    @JvmStatic fun Activity.createIntentLauncher(): IntentLauncher =
        ActivityLauncher(this)
    @JvmStatic fun Fragment.createIntentLauncher(): IntentLauncher =
        FragmentLauncher(this)
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
