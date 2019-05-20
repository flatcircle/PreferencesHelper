package io.flatcircle.preferencehelperexample

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import io.flatcircle.preferenceshelper.PreferencesHelper
import io.flatcircle.preferenceshelper.KeyStoreHelper

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson



class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        val stringKey = "key_string"
        val string = "hello"
        PreferencesHelper.set(this, stringKey, string)
        val stringAgain = PreferencesHelper.get<String>(this, stringKey)
        val matchString = string == stringAgain

        KeyStoreHelper.setAlias("ExampleAlias")
        val encryptStringKey = "key_encrypt_string"
        val encryptString = "allo allo this is nighthawk"
        PreferencesHelper.setAndEncrypt(this, encryptStringKey, encryptString)
        val encryptAgain = PreferencesHelper.getEncrypted(this, encryptStringKey)
        val matchEncryptedStorage = encryptString == encryptAgain

        val integerKey = "key_integer"
        val integer = 451
        PreferencesHelper.set(this, integerKey, integer)
        val integerAgain = PreferencesHelper.get<Int>(this, integerKey)
        val matchInt = integer == integerAgain

        val longKey = "key_long"
        val long = 6942069L
        PreferencesHelper.set(this, longKey, long)
        val longAgain = PreferencesHelper.get<Long>(this, longKey)
        val matchLong = long == longAgain

        val booleanKey = "key_boolean"
        val boolean = true
        PreferencesHelper.set(this, booleanKey, boolean)
        val booleanAgain = PreferencesHelper.get<Boolean>(this, booleanKey)
        val matchBoolean = boolean == booleanAgain

        val customKey = "key_custom_class"
        val customClass = CustomClass(string, integer)
        PreferencesHelper.set(this, customKey, customClass)
        val customAgain = PreferencesHelper.get<CustomClass>(this, customKey)
        val matchCustom = customClass == customAgain

        val customierKey = "key_customier_class"
        val customierClass = CustomierCustomClass("AA", 2)
        PreferencesHelper.addCustomAdapter<CustomierCustomClass>(CustomierAdapter())
        PreferencesHelper.set(this, customierKey, customierClass)
        val customierAgain = PreferencesHelper.get<CustomierCustomClass>(this, customierKey)
        val matchCustomier = customierClass == customierAgain

        val stringUnencrypted = "U357 is on the way"
        val stringEncrypted = KeyStoreHelper.encryptString(this, stringUnencrypted)
        val stringDecrypted = KeyStoreHelper.decryptString(this, stringEncrypted)
        val matchEncryption = stringUnencrypted == stringDecrypted

        val matchAll = matchString && matchEncryptedStorage && matchInt && matchLong &&
            matchBoolean && matchCustom && matchCustomier && matchEncryption

        textView.text = "All classes have been stored and obtained successfully? \n $matchAll"

    }

    data class CustomClass(val string: String, val integer: Int)
    data class CustomierCustomClass(val string: String, val integer: Int)

    class CustomierAdapter {
        @ToJson
        fun toJson(customierClass: CustomierCustomClass): String {
            return customierClass.string + customierClass.integer
        }

        @FromJson
        fun fromJson(jsonSource: String): CustomierCustomClass {
            if (jsonSource.length != 3) throw JsonDataException("Invalid input")

            val string = jsonSource.substring(0,2)
            val integer = jsonSource.substring(2,3)
            return CustomierCustomClass(string, integer.toInt())
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
