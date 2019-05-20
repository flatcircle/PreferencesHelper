package io.flatcircle.preferenceshelper

import android.content.Context
import android.content.SharedPreferences
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi

object PreferencesHelper {

    private val customAdapters: MutableList<Pair<Class<*>, Any>> = mutableListOf()

    inline fun <reified T> addCustomAdapter(adapter: Any) {
        addCustomAdapter(adapter, T::class.java)
    }
    @PublishedApi
    internal fun <T> addCustomAdapter(adapter: Any, clazz: Class<T>) {
        val nuAdapterPair = Pair(clazz, adapter)
        val indexOfExistingAdapter = customAdapters.indexOfFirst { it.first == clazz }
        if (indexOfExistingAdapter < 0) {
            customAdapters.add(nuAdapterPair)
        } else {
            customAdapters[indexOfExistingAdapter] = nuAdapterPair
        }
    }

    inline fun <reified T> get(context: Context, key: String, default: T? = null): T? {
        return get(context, key, default, T::class.java)
    }

    @Suppress("UNCHECKED_CAST")
    @PublishedApi
    internal fun <T> get(context: Context, key: String, default: T?, clazz: Class<T>): T? {

        return when (clazz) {
            Long::class -> getLong(context, key, default as Long? ?: 0L) as T
            Int::class -> getInt(context, key, default as Int? ?: 0) as T
            String::class -> getString(context, key, default as String? ?: "") as T
            Boolean::class -> getBoolean(context, key, false) as T
            else -> serializeFromString(getString(context, key, ""), clazz) ?: default
        }
    }

    /**
     * Sets the given key value pair in Preferences. Uses apply, so returns before finishing.
     */
    inline fun <reified T> set(context: Context, key: String, value: T) {
        set(context, key, value, T::class.java).apply()
    }

    /**
     * Sets the given key-value pair in Preferences. Uses commit, so only returns when finishing.
     */
    inline fun <reified T> setSync(context: Context, key: String, value: T) {
        set(context, key, value, T::class.java).commit()
    }

    @PublishedApi
    internal fun <T> set(context: Context, key: String, value: T, clazz: Class<T>): SharedPreferences.Editor {
        return when (clazz) {
            Long::class -> setLong(context, key, value as Long)
            Int::class -> setInt(context, key, value as Int)
            String::class -> setString(context, key, value as String)
            Boolean::class -> setBoolean(context, key, value as Boolean)
            else -> setString(context, key, serializeIntoString(value, clazz))
        }
    }

    @PublishedApi
    internal fun <T> serializeIntoString(input: T, clazz: Class<T>): String {
        val moshiBuilder = Moshi.Builder()
        val indexOfAdapter = customAdapters.indexOfFirst { it.first == clazz }
        if (indexOfAdapter >= 0) {
            moshiBuilder.add(customAdapters[indexOfAdapter].second)
        }
        val moshi = moshiBuilder.build()
        val jsonAdapter: JsonAdapter<T> = moshi.adapter(clazz)
        return jsonAdapter.toJson(input)
    }

    @PublishedApi
    internal fun <T> serializeFromString(input: String, clazz: Class<T>): T? {
        val moshiBuilder = Moshi.Builder()
        val indexOfAdapter = customAdapters.indexOfFirst { it.first == clazz }
        if (indexOfAdapter >= 0) {
            moshiBuilder.add(customAdapters[indexOfAdapter].second)
        }
        val moshi = moshiBuilder.build()
        val jsonAdapter: JsonAdapter<T> = moshi.adapter(clazz)
        return jsonAdapter.fromJson(input)
    }

    fun setAndEncrypt(context: Context, key: String, value: String) {
        val encryptedString = KeyStoreHelper.encryptString(context, value)
        set(context, key, encryptedString)
    }

    fun getEncrypted(context: Context, key: String): String? {
        val encryptedString = get<String>(context, key)
        if (encryptedString == null)
            return null
        return KeyStoreHelper.decryptString(context, encryptedString)
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