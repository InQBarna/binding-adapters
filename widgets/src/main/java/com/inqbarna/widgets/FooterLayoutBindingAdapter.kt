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

import androidx.databinding.*

/**
 * @author David García (david.garcia@inqbarna.com)
 * @version 1.0 26/03/2018
 */
class FooterLayoutBindingAdapter {

    @BindingMethods(
            BindingMethod(type = FooterLayout::class, attribute = "footerEnabled", method = "setEnabled"),
            BindingMethod(type = FooterLayout::class, attribute = "footer_hidden", method = "setHidden")
    )
    @InverseBindingMethods(
            InverseBindingMethod(type = FooterLayout::class, attribute = "footerEnabled", method = "isEnabled"),
            InverseBindingMethod(type = FooterLayout::class, attribute = "footer_hidden", method = "getHidden")
    )
    companion object {

        @JvmStatic
        @BindingAdapter("enabledListener", "footerEnabledAttrChanged", requireAll = false)
        fun footerEnable(footerLayout: FooterLayout, onFooterEnabledListener: FooterLayout.OnFooterEnabledListener?, inverseBindingListener: InverseBindingListener?) {
            footerLayout.onFooterEnabledListener = object : FooterLayout.OnFooterEnabledListener {
                override fun onFooterEnabledState(enabled: Boolean) {
                    onFooterEnabledListener?.onFooterEnabledState(enabled)
                    inverseBindingListener?.onChange()
                }
            }
        }

        @JvmStatic
        @BindingAdapter("footerHiddenListener", "footer_hiddenAttrChanged", requireAll = false)
        fun footerHidden(footerLayout: FooterLayout, onFooterHideStateChangeListener: FooterLayout.OnFooterHideStateChangeListener?, inverseBindingListener: InverseBindingListener?) {
            footerLayout.onFooterHideStateChangeListener = object : FooterLayout.OnFooterHideStateChangeListener {
                override fun onFooterHideStateChanged(hidden: Boolean) {
                    onFooterHideStateChangeListener?.onFooterHideStateChanged(hidden)
                    inverseBindingListener?.onChange()
                }
            }
        }

    }
}