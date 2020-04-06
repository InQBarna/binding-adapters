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

package com.inqbarna.navigation.internal

import android.os.Parcel
import android.os.Parcelable
import java.math.BigDecimal
import java.math.BigInteger
import java.util.*

// Credits to https://medium.com/@BladeCoder/reducing-parcelable-boilerplate-code-using-kotlin-741c3124a49a

// Creator factory functions
internal inline fun <reified T> parcelableCreator(
    crossinline create: (Parcel) -> T
) =
    object : Parcelable.Creator<T> {
        override fun createFromParcel(source: Parcel) = create(source)
        override fun newArray(size: Int) = arrayOfNulls<T>(size)
    }

internal inline fun <reified T> parcelableClassLoaderCreator(
    crossinline create: (Parcel, ClassLoader) -> T
) =
    object : Parcelable.ClassLoaderCreator<T> {
        override fun createFromParcel(source: Parcel, loader: ClassLoader) =
            create(source, loader)

        override fun createFromParcel(source: Parcel) =
            createFromParcel(source, T::class.java.classLoader)

        override fun newArray(size: Int) = arrayOfNulls<T>(size)
    }

// Parcel extensions

internal fun Parcel.readBoolean() = readInt() != 0

internal fun Parcel.writeBoolean(value: Boolean) = writeInt(if (value) 1 else 0)

inline fun <reified T : Enum<T>> Parcel.readEnum() =
    readInt().let { if (it >= 0) enumValues<T>()[it] else null }

internal fun <T : Enum<T>> Parcel.writeEnum(value: T?) =
    writeInt(value?.ordinal ?: -1)

internal inline fun <T> Parcel.readNullable(reader: () -> T) =
    if (readInt() != 0) reader() else null

internal inline fun <T> Parcel.writeNullable(value: T?, writer: (T) -> Unit) {
    if (value != null) {
        writeInt(1)
        writer(value)
    } else {
        writeInt(0)
    }
}

internal fun Parcel.readDate() =
    readNullable { Date(readLong()) }

internal fun Parcel.writeDate(value: Date?) =
    writeNullable(value) { writeLong(it.time) }

internal fun Parcel.readBigInteger() =
    readNullable { BigInteger(createByteArray()) }

internal fun Parcel.writeBigInteger(value: BigInteger?) =
    writeNullable(value) { writeByteArray(it.toByteArray()) }

internal fun Parcel.readBigDecimal() =
    readNullable { BigDecimal(BigInteger(createByteArray()), readInt()) }

internal fun Parcel.writeBigDecimal(value: BigDecimal?) = writeNullable(value) {
    writeByteArray(it.unscaledValue().toByteArray())
    writeInt(it.scale())
}

internal fun <T : Parcelable> Parcel.readTypedObjectCompat(c: Parcelable.Creator<T>) =
    readNullable { c.createFromParcel(this) }

internal fun <T : Parcelable> Parcel.writeTypedObjectCompat(value: T?, parcelableFlags: Int) =
    writeNullable(value) { it.writeToParcel(this, parcelableFlags) }