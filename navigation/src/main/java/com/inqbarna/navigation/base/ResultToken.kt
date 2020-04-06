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
import com.inqbarna.navigation.internal.parcelableCreator

class ResultToken(private val requestCode: Int) : Parcelable {

    constructor(parcel: Parcel) : this(parcel.readInt())

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(requestCode)
    }

    override fun describeContents(): Int = 0

    fun shouldRespondTo(requestCode: Int): Boolean {
        return this.requestCode == requestCode
    }

    companion object {
        @JvmField val CREATOR = parcelableCreator(::ResultToken)
    }
}