package io.flatcircle.preferenceshelper

import kotlin.properties.ObservableProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Created by jacquessmuts on 2019-10-03
 */

/**
 * Returns a property delegate for a read/write property that automatically saves the property to
 * SharedPreferences whenever it changes. It also initializes the variable with the value from
 * SharedPreferences or initialDefault if null.
 *
 * @param prefs an instance of Prefs which has been initialized
 * @param key the key string used to fetch the SharedPreferences
 * @param initialDefault the initial value of the property, if nothing is in SharedPreferences
 */
inline fun <reified T : Any> observablePreference(prefs: Prefs, key: String, initialDefault: T):
    ReadWriteProperty<Any?, T> =
    object : ObservableProperty<T>(prefs.get(key, initialDefault)) {
        override fun afterChange(property: KProperty<*>, oldValue: T, newValue: T) {
            if (newValue != oldValue) {
                prefs.set<T>(key, newValue)
            }
        }
    }