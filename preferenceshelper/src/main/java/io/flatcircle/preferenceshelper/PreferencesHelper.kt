@file:Suppress("unused")

package io.flatcircle.preferenceshelper

import android.content.Context
import android.content.SharedPreferences
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonEncodingException
import com.squareup.moshi.Moshi
import kotlin.reflect.KClass

object PreferencesHelper {

    private val customAdapters: MutableList<Pair<KClass<out Any>, Any>> = mutableListOf()

    /**
     * This function adds a custom adapter for a given class. Adapter is applied to the Moshi serializer.
     */
    inline fun <reified T : Any> addCustomAdapter(adapter: Any) {
        addCustomAdapter(adapter, T::class)
    }
    @PublishedApi
    internal fun <T : Any> addCustomAdapter(adapter: Any, clazz: KClass<T>) {
        val nuAdapterPair = Pair(clazz, adapter)
        val indexOfExistingAdapter = customAdapters.indexOfFirst { it.first == clazz }
        if (indexOfExistingAdapter < 0) {
            customAdapters.add(nuAdapterPair)
        } else {
            customAdapters[indexOfExistingAdapter] = nuAdapterPair
        }
    }

    /**
     * Get a given object from sharedpreferences with the given key.
     *
     * @param context any context
     * @param key SharedPreferences String Key
     * @param default is optional, unless you are getting a custom object
     */
    @Throws(IllegalArgumentException::class)
    inline fun <reified T : Any> get(context: Context, key: String, default: T? = null): T {
        return get(context, key, default, T::class)
    }

    /**
     * Same as get, but enforces null safety on custom objects via the default parameter
     */
    inline fun <reified T : Any> getSafely(context: Context, key: String, default: T): T {
        return get(context, key, default, T::class)
    }

    @Suppress("UNCHECKED_CAST")
    @PublishedApi
    internal fun <T : Any> get(context: Context, key: String, default: T?, clazz: KClass<T>): T {

        return when (clazz) {
            Long::class -> getLong(context, key, default as Long? ?: 0L) as T
            Int::class -> getInt(context, key, default as Int? ?: 0) as T
            String::class -> getString(context, key, default as String? ?: "") as T
            Boolean::class -> getBoolean(context, key, false) as T
            else -> {
                if (default == null) throw IllegalArgumentException("Custom objects require a default parameter to be passed")
                serializeFromString(getString(context, key, ""), clazz, default)
            }
        }
    }

    /**
     * Sets the given key value pair in Preferences. Uses apply, so returns before finishing.
     */
    inline fun <reified T : Any> set(context: Context, key: String, value: T) {
        set(context, key, value, T::class).apply()
    }

    /**
     * Sets the given key-value pair in Preferences. Uses commit, so only returns when finishing.
     */
    inline fun <reified T : Any> setSync(context: Context, key: String, value: T) {
        set(context, key, value, T::class).commit()
    }

    @PublishedApi
    internal fun <T : Any> set(context: Context, key: String, value: T, clazz: KClass<T>): SharedPreferences.Editor {

        return when (clazz) {
            Long::class -> setLong(context, key, value as Long)
            Int::class -> setInt(context, key, value as Int)
            String::class -> setString(context, key, value as String)
            Boolean::class -> setBoolean(context, key, value as Boolean)
            else -> setString(context, key, serializeIntoString(value, clazz))
        }
    }

    /**
     * Encrypts a given string value using the KeyStoreHelper, then saves the result in
     * SharedPreferences.
     */
    fun setAndEncrypt(context: Context, key: String, value: String) {
        val encryptedString = KeyStoreHelper.encryptString(context, value)
        set(context, key, encryptedString)
    }

    /**
     * Gets a saved encrypted string from SharedPreferences, decrypts the string using
     * KeyStoreHelper, and returns the decrypted string.
     */
    fun getEncrypted(context: Context, key: String): String {
        val encryptedString = get<String>(context, key)
        return KeyStoreHelper.decryptString(context, encryptedString)
    }

    /**
     * Takes a given class and serializes it into a JSON String using Moshi. May require setting a
     * custom adapter via addCustomAdapter
     */
    inline fun <reified T : Any> serializeIntoString(input: T): String {
        return serializeIntoString(input, T::class)
    }
    @PublishedApi
    internal fun <T : Any> serializeIntoString(input: T, clazz: KClass<T>): String {
        val moshiBuilder = Moshi.Builder()
        val indexOfAdapter = customAdapters.indexOfFirst { it.first == clazz }
        if (indexOfAdapter >= 0) {
            moshiBuilder.add(customAdapters[indexOfAdapter].second)
        }
        val moshi = moshiBuilder.build()
        val jsonAdapter: JsonAdapter<T> = moshi.adapter(clazz.java)
        return jsonAdapter.toJson(input)
    }

    /**
     * Takes a given Json String and serializes it into the required object.
     *
     * @param default is required in case of invalid string or serialization errors
     */
    inline fun <reified T : Any> serializeFromString(input: String, default: T): T {
        return serializeFromString(input, T::class, default)
    }

    @PublishedApi
    @Throws(IllegalArgumentException::class)
    internal fun <T : Any> serializeFromString(input: String, clazz: KClass<T>, default: T): T {
        if (input.isEmpty())
            return default

        val moshiBuilder = Moshi.Builder()
        val indexOfAdapter = customAdapters.indexOfFirst { it.first == clazz }
        if (indexOfAdapter >= 0) {
            moshiBuilder.add(customAdapters[indexOfAdapter].second)
        }
        val moshi = moshiBuilder.build()
        val jsonAdapter: JsonAdapter<T> = moshi.adapter(clazz.java)
        try {
            return jsonAdapter.fromJson(input) ?: default ?: clazz.objectInstance!!
        } catch (e: JsonEncodingException) {
            throw java.lang.IllegalArgumentException("Unable to serialize $input into $clazz due to... ${e.localizedMessage}")
        }
    }

    /**
     * Determines if the sharedPreferences contains a value for the given key
     */
    fun contains(context: Context, key: String): Boolean {
        return android.preference.PreferenceManager.getDefaultSharedPreferences(context).contains(key)
    }

    internal fun setLong(context: Context, key: String, value: Long): SharedPreferences.Editor {
        return android.preference.PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putLong(key, value)
    }

    internal fun getLong(context: Context, key: String, fallbackVal: Long): Long {
        return android.preference.PreferenceManager
            .getDefaultSharedPreferences(context)
            .getLong(key, fallbackVal)
    }

    internal fun setInt(context: Context, key: String, value: Int): SharedPreferences.Editor {
        return android.preference.PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putInt(key, value)
    }

    internal fun getInt(context: Context, key: String, fallbackVal: Int): Int {
        return android.preference.PreferenceManager
            .getDefaultSharedPreferences(context)
            .getInt(key, fallbackVal)
    }

    internal fun setString(context: Context, key: String, value: String): SharedPreferences.Editor {
        return android.preference.PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putString(key, value)
    }

    internal fun getString(context: Context, key: String, fallbackVal: String): String {
        return android.preference.PreferenceManager
            .getDefaultSharedPreferences(context)
            .getString(key, fallbackVal) ?: fallbackVal
    }

    internal fun setBoolean(context: Context, key: String, value: Boolean): SharedPreferences.Editor {
        return android.preference.PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putBoolean(key, value)
    }

    internal fun getBoolean(context: Context, key: String, fallbackVal: Boolean): Boolean {
        return android.preference.PreferenceManager
            .getDefaultSharedPreferences(context)
            .getBoolean(key, fallbackVal)
    }
}