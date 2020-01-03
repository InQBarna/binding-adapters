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
