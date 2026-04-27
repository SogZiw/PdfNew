package com.word.file.manager.pdf.base.helper

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.core.content.edit
import com.word.file.manager.pdf.app
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class PreferenceStore(name: String) {

    private val sp by lazy(LazyThreadSafetyMode.NONE) { app.getSharedPreferences(name, MODE_PRIVATE) }

    fun boolean(
        key: String? = null,
        defaultValue: Boolean = false,
    ): ReadWriteProperty<Any?, Boolean> = preference(
        key = key,
        reader = { prefKey -> getBoolean(prefKey, defaultValue) },
        writer = { prefKey, value -> putBoolean(prefKey, value) },
    )

    fun int(
        key: String? = null,
        defaultValue: Int = 0,
    ): ReadWriteProperty<Any?, Int> = preference(
        key = key,
        reader = { prefKey -> getInt(prefKey, defaultValue) },
        writer = { prefKey, value -> putInt(prefKey, value) },
    )

    fun long(
        key: String? = null,
        defaultValue: Long = 0L,
    ): ReadWriteProperty<Any?, Long> = preference(
        key = key,
        reader = { prefKey -> getLong(prefKey, defaultValue) },
        writer = { prefKey, value -> putLong(prefKey, value) },
    )

    fun float(
        key: String? = null,
        defaultValue: Float = 0f,
    ): ReadWriteProperty<Any?, Float> = preference(
        key = key,
        reader = { prefKey -> getFloat(prefKey, defaultValue) },
        writer = { prefKey, value -> putFloat(prefKey, value) },
    )

    fun string(
        key: String? = null,
        defaultValue: String = "",
    ): ReadWriteProperty<Any?, String> = preference(
        key = key,
        reader = { prefKey -> getString(prefKey, defaultValue) ?: defaultValue },
        writer = { prefKey, value -> putString(prefKey, value) },
    )

    fun double(
        key: String? = null,
        defaultValue: Double = 0.0,
    ): ReadWriteProperty<Any?, Double> = preference(
        key = key,
        reader = { prefKey -> getString(prefKey, null)?.toDoubleOrNull() ?: defaultValue },
        writer = { prefKey, value -> putString(prefKey, value.toString()) },
    )

    private fun <T> preference(
        key: String?,
        reader: SharedPreferences.(String) -> T,
        writer: SharedPreferences.Editor.(String, T) -> Unit,
    ): ReadWriteProperty<Any?, T> {
        return object : ReadWriteProperty<Any?, T> {
            override fun getValue(thisRef: Any?, property: KProperty<*>): T {
                return sp.reader(key ?: property.name)
            }

            override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
                sp.edit {
                    writer(key ?: property.name, value)
                }
            }
        }
    }
}
