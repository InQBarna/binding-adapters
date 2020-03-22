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

package com.inqbarna.navigation.base

import android.os.Parcel
import android.os.Parcelable
import com.inqbarna.navigation.internal.readBoolean
import com.inqbarna.navigation.internal.writeBoolean

/**
 * @author David García (david.garcia@inqbarna.com)
 * @version 1.0 20/06/2018
 */
abstract class AppRoute(
    val requestCode: Int = INVALID_REQUEST_CODE,
    val requiredPermissions: Array<String>? = null
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.createStringArray()
    ) {
        aborted = parcel.readBoolean()
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(requestCode)
        dest.writeStringArray(requiredPermissions)
        dest.writeBoolean(aborted)
    }

    override fun describeContents(): Int = 0

    companion object {
        @JvmStatic
        val INVALID_REQUEST_CODE: Int = -1
    }

    var aborted: Boolean = false
        protected set

    var permissionRequested: Boolean = false
        private set

    fun needsPermissionResolution(): Boolean = hasPermissions() && !permissionRequested

    fun hasPermissions() = requiredPermissions?.isNotEmpty() ?: false

    fun requiresResult(): Boolean = requestCode != INVALID_REQUEST_CODE
    fun createResultToken(): ResultToken {
        if (!requiresResult()) {
            throw UnsupportedOperationException("This method should only be called if we expect to have results...")
        }
        return ResultToken(requestCode)
    }

    fun hasPermissionDenied(): Boolean = permissionRequested && aborted

    fun requestedPermissionsGranted() {
        permissionRequested = true
        onPermissionUpdated(true)
    }

    fun requestedPermissionsDenied() {
        permissionRequested = true
        onPermissionUpdated(false)
    }

    open fun onPermissionUpdated(granted: Boolean) {
        if (!granted) {
            // aborts by default
            aborted = true
        }
    }
}
