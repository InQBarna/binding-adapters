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

package com.inqbarna.navigation.routes

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import com.inqbarna.navigation.base.AppRoute
import com.inqbarna.navigation.internal.parcelableCreator
import com.inqbarna.navigation.internal.readTypedObjectCompat
import com.inqbarna.navigation.internal.writeTypedObjectCompat

/**
 * @author David García (david.garcia@inqbarna.com)
 * @version 1.0 07/01/2020
 */
sealed class SimpleIntentRoute : AppRoute {
    constructor(requestCode: Int, requiredPermissions: Array<String>?) : super(
        requestCode,
        requiredPermissions
    )

    constructor(parcel: Parcel) : super(parcel)
}

class IntentRoute : SimpleIntentRoute {
    internal val intent: Intent
    internal val extras: Bundle
    constructor(intent: Intent, requestCode: Int, extras: Bundle = Bundle(), requiredPermissions: Array<String>? = null) : super(
        requestCode,
        requiredPermissions
    ) {
        this.intent = intent
        this.extras = extras
    }

    private constructor(parcel: Parcel) : super(parcel) {
        intent = requireNotNull(parcel.readTypedObjectCompat(Intent.CREATOR)) { "Shouldn't be possible, we've written it" }
        extras = parcel.readBundle(javaClass.classLoader) ?: Bundle()
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        super.writeToParcel(dest, flags)
        dest.writeTypedObjectCompat(intent, 0)
        dest.writeBundle(extras)
    }

    companion object {
        @JvmField val CREATOR = parcelableCreator(::IntentRoute)
    }
}

class PendingIntentRoute : SimpleIntentRoute {
    val pendingIntent: PendingIntent

    constructor(pendingIntent: PendingIntent) : super(INVALID_REQUEST_CODE, null) {
        this.pendingIntent = pendingIntent
    }

    private constructor(parcel: Parcel) : super(parcel) {
        pendingIntent = requireNotNull(parcel.readTypedObjectCompat(PendingIntent.CREATOR)) { "This shouldn't be possible, we've written it before" }
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        super.writeToParcel(dest, flags)
        dest.writeTypedObjectCompat(pendingIntent, 0)
    }

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<PendingIntentRoute> = parcelableCreator(::PendingIntentRoute)
    }
}
