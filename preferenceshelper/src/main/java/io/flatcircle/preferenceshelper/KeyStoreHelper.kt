package io.flatcircle.preferenceshelper

import android.content.Context
import android.security.KeyPairGeneratorSpec
import android.util.Base64
import android.util.Log
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.math.BigInteger
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.PrivateKey
import java.util.Calendar
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.security.auth.x500.X500Principal

object KeyStoreHelper {

    private var keyStoreAlias = ""
    fun setAlias(nuAlias: String) {
        require(nuAlias.isNotEmpty())
        keyStoreAlias = nuAlias
    }

    private const val KEY_STORE_PROVIDER = "AndroidKeyStore"

    private var keyStore: KeyStore? = null

    fun encryptString(context: Context, inputValue: String): String {
        require(inputValue.isNotEmpty())
        getKeyStore(context)

        try {
            val privateKeyEntry = keyStore!!.getEntry(keyStoreAlias, null) as KeyStore.PrivateKeyEntry
            val publicKey = privateKeyEntry.certificate.publicKey

            val input = Cipher.getInstance("RSA/ECB/PKCS1Padding", "AndroidOpenSSL")
            input.init(Cipher.ENCRYPT_MODE, publicKey)

            val outputStream = ByteArrayOutputStream()
            val cipherOutputStream = CipherOutputStream(
                outputStream, input)
            cipherOutputStream.write(inputValue.toByteArray(charset("UTF-8")))
            cipherOutputStream.close()

            val vals = outputStream.toByteArray()
            return Base64.encodeToString(vals, Base64.DEFAULT)
        } catch (e: Exception) {
            Log.e("KeyStoreHelper", "$e")
        }
        return ""
    }

    fun decryptString(context: Context, encryptedText: String): String {
        require(encryptedText.isNotEmpty())
        getKeyStore(context)

        if (encryptedText.isNotEmpty()) {
            try {
                val privateKey = keyStore?.getKey(keyStoreAlias, null) as PrivateKey

                val output = Cipher.getInstance("RSA/ECB/PKCS1Padding")
                output.init(Cipher.DECRYPT_MODE, privateKey)

                val cipherInputStream = CipherInputStream(
                    ByteArrayInputStream(Base64.decode(encryptedText, Base64.DEFAULT)), output)

                val valuesByteArray = cipherInputStream.readBytes()

                return String(valuesByteArray, 0, valuesByteArray.size, Charsets.UTF_8)
            } catch (e: Exception) {
                Log.e("KeyStoreHelper", "$e")
            }
        } else {
            Log.w("KeyStoreHelper", "decryptString() : encryptedText is null or empty.")
        }

        return ""
    }

    @Throws(java.lang.IllegalStateException::class)
    fun getKeyStore(context: Context): KeyStore {
        if (keyStore == null) {
            try {
                keyStore = KeyStore.getInstance(KEY_STORE_PROVIDER)
                keyStore!!.load(null)
                createNewKeyIfNecessary(context, keyStore!!)
            } catch (e: KeyStoreException) {
                e.printStackTrace()
            } catch (e: IllegalStateException) {
                throw e
            } catch (e: Exception) {
                Log.e("PreferencesHelper", "keyStore failed $e")
            }
        }

        return keyStore!!
    }

    private fun createNewKeyIfNecessary(context: Context, keyStore: KeyStore?) {

        if (keyStoreAlias.isEmpty()) {
            throw IllegalStateException("You need to set the keyStoreAlias when using encryption")
        }

        try {
            // Create new key if needed
            if (keyStore == null || !keyStore.containsAlias(keyStoreAlias)) {
                val start = Calendar.getInstance()
                val end = Calendar.getInstance()
                end.add(Calendar.YEAR, 1)
                val spec = KeyPairGeneratorSpec.Builder(context)
                    .setAlias(keyStoreAlias)
                    .setSubject(X500Principal("CN=Sample Name, O=Android Authority"))
                    .setSerialNumber(BigInteger.ONE)
                    .setStartDate(start.time)
                    .setEndDate(end.time)
                    .build()
                val generator = KeyPairGenerator.getInstance("RSA", KEY_STORE_PROVIDER)
                generator.initialize(spec)

                generator.generateKeyPair()
            }
        } catch (e: Exception) {
            Log.e("KeyStoreHelper", "$e")
        }
    }

}