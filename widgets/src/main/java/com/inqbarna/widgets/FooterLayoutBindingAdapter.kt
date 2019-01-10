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