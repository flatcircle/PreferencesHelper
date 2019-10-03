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
inline fun <reified T : Any> ObservablePreference(prefs: Prefs, key: String, initialDefault: T):
    ReadWriteProperty<Any?, T> =
    object : ObservableProperty<T>(prefs.get(key, initialDefault)) {
        override fun afterChange(property: KProperty<*>, oldValue: T, newValue: T) {
            if (newValue != oldValue) {
                prefs.set<T>(key, newValue)
            }
        }
    }


/**
 * Returns a property delegate for a read/write property that automatically saves the property to
 * SharedPreferences whenever it changes. It also initializes the variable with the value from
 * SharedPreferences or initialDefault if null.
 *
 * Furthermore it encrypts the saved string via the KeyStoreHelper that must be initialized.
 *
 * @param prefs an instance of Prefs which has been initialized
 * @param key the key string used to fetch the SharedPreferences
 * @param initialDefault the initial value of the property, if nothing is in SharedPreferences
 */
fun ObservableEncryptedPreference(prefs: Prefs, key: String, initialDefault: String):
    ReadWriteProperty<Any?, String> =
    object : ObservableProperty<String>(prefs.getEncrypted(key)) {
        override fun afterChange(property: KProperty<*>, oldValue: String, newValue: String) {
            if (newValue != oldValue) {
                prefs.setAndEncrypt(key, newValue)
            }
        }
    }

