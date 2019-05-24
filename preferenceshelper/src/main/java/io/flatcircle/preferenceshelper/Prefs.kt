package io.flatcircle.preferenceshelper

import android.content.Context

/**
 * Created by jacquessmuts on 2019-05-24
 * Class for handling an instance with a reference to context. Only use this if you understand when
 * to instantiate and .clear() the Prefs class in the Android lifecycle.
 */
class Prefs(var context: Context?) {

    companion object {
        const val ContextNullMessage = "You cannot use Prefs after clearing it"
    }

    fun checkContext() {
        if (context == null)
            throw IllegalStateException(ContextNullMessage)
    }
    /**
     * Get a given object from sharedpreferences with the given key.
     *
     * @param key SharedPreferences String Key
     * @param default is optional, unless you are getting a custom object
     */
    @Throws(IllegalArgumentException::class)
    inline fun <reified T : Any> get(key: String, default: T? = null): T {
        checkContext()
        return PreferencesHelper.get(context!!, key, default, T::class)
    }

    /**
     * Sets the given key value pair in Preferences. Uses apply, so returns before finishing.
     */
    inline fun <reified T : Any> set(key: String, value: T) {
        checkContext()
        PreferencesHelper.set(context!!, key, value, T::class).apply()
    }

    /**
     * Sets the given key-value pair in Preferences. Uses commit, so only returns when finishing.
     */
    inline fun <reified T : Any> setSync(key: String, value: T) {
        checkContext()
        PreferencesHelper.set(context!!, key, value, T::class).commit()
    }

    /**
     * Encrypts a given string value using the KeyStoreHelper, then saves the result in
     * SharedPreferences.
     */
    fun setAndEncrypt(key: String, value: String) {
        checkContext()
        PreferencesHelper.setAndEncrypt(context!!, key, value)
    }

    /**
     * Gets a saved encrypted string from SharedPreferences, decrypts the string using
     * KeyStoreHelper, and returns the decrypted string.
     */
    fun getEncrypted(key: String): String {
        checkContext()
        return PreferencesHelper.getEncrypted(context!!, key)
    }

    /**
     * This must be called at the end of your activity/app lifecycle
     */
    fun clear() {
        context = null
    }
}