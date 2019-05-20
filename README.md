# ConnectivityHelper
Functions to help with checking your app's online status

[![CircleCI](https://circleci.com/gh/flatcircle/PreferencesHelper.svg?style=svg)](https://circleci.com/gh/flatcircle/PreferencesHelper) [ ![Download](https://api.bintray.com/packages/flatcircle/PreferencesHelper/preferenceshelper/images/download.svg) ](https://bintray.com/flatcircle/PreferencesHelper/preferenceshelper/_latestVersion)

Installation
--------

```groovy
implementation 'io.flatcircle:preferenceshelper:{version}'
```

Usage
-----

You can set and get any primitive or basic custom class into your sharedPreferences via PreferencesHelper. The supported classes are the same as [Moshi's supported classes](https://github.com/square/moshi#built-in-type-adapters) since it uses moshi to serialized. The supported functions are:

| Function  | Description | Example |
| ------------- | ------------- | ------------- |
| PreferencesHelper.set(context, key, value) | Saves a given value to sharedPreferences, where [value] is any primitive, or a custom class that can be serialized by Moshi into Json | [Example](https://github.com/flatcircle/PreferencesHelper/blob/master/app/src/main/java/io/flatcircle/preferencehelperexample/MainActivity.kt#L24)  |
| PreferencesHelper.setSync(context, key, value) | Same as setSync, but uses preferencesEditor.commit() to keep the writing operation onto the same thread. | [Example](https://github.com/flatcircle/PreferencesHelper/blob/master/app/src/main/java/io/flatcircle/preferencehelperexample/MainActivity.kt#L24)  |
| PreferencesHelper.get<Any?>(context, key, defaultValue?)  | Returns any value you've saved, with optional DefaultValue. | [Example](https://github.com/flatcircle/PreferencesHelper/blob/master/app/src/main/java/io/flatcircle/preferencehelperexample/MainActivity.kt#L26)  |
| PreferencesHelper.setAndEncrypt(context, key, value) | Saves a given string as an encrypted string using the Android KeyStore to perform the encryption. Requires you to set a KeyStore Alias as below | [Example](https://github.com/flatcircle/PreferencesHelper/blob/master/app/src/main/java/io/flatcircle/preferencehelperexample/MainActivity.kt#L32)  |
| PreferencesHelper.getEncrypted(context, key) | Returns a decrypted string which was saved as an encrypted string. | [Example](https://github.com/flatcircle/PreferencesHelper/blob/master/app/src/main/java/io/flatcircle/preferencehelperexample/MainActivity.kt#L32)  |
| PreferencesHelper.addCustomAdapter<Class>(adapter) | Adds a [custom moshi adapter](https://github.com/square/moshi#custom-type-adapters) for a given class | [Example](https://github.com/flatcircle/PreferencesHelper/blob/master/app/src/main/java/io/flatcircle/preferencehelperexample/MainActivity.kt#L72)  |


The PreferencesHelper library also comes with basic KeyStore management for encryption/decryption purposes. The actual encryption works, but uses a currently deprecated method of generating a keypair that needs to be double-checked. You can encrypt and decrypt strings as follows:

| Function  | Description | Example |
| ------------- | ------------- | ------------- |
| KeyStoreHelper.setAlias("YourAppAlias") | Sets a KeyStore alias for your app, in order to use encryption. Do this in your Application{onCreate(){}} | [Example](https://github.com/flatcircle/PreferencesHelper/blob/master/app/src/main/java/io/flatcircle/preferencehelperexample/MainActivity.kt#L32)  |
| KeyStoreHelper.encryptString(context, string) | Returns an encrypted version of a given string | [Example](https://github.com/flatcircle/PreferencesHelper/blob/master/app/src/main/java/io/flatcircle/preferencehelperexample/MainActivity.kt#L32)  |
| KeyStoreHelper.decryptString(context, string) | Decrypts a given string that was encrypted with above method. | [Example](https://github.com/flatcircle/PreferencesHelper/blob/master/app/src/main/java/io/flatcircle/preferencehelperexample/MainActivity.kt#L32)  |


I know it's tempting to think you can use this as a replacement for a database, but please don't. When you start to serialize lists of custom classes you should really just create a database module to handle that.