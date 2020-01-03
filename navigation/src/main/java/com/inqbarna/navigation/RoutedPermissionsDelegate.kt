package com.inqbarna.navigation

import android.os.Bundle
import com.inqbarna.navigation.base.AppRoute

/**
 * @author David García (david.garcia@inqbarna.com)
 * @version 1.0 20/07/2018
 */
interface RoutedPermissionsDelegate {
    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ): Boolean

    fun checkPermissionsForDestination(destination: AppRoute)
    fun onSaveState(state: Bundle)
}
