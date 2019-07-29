package io.flatcircle.preferenceslint

import com.android.tools.lint.checks.infrastructure.TestFiles.kotlin
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import org.junit.Test

/**
 * Created by jacquessmuts on 2019-07-24
 */
class WrongPreferencesUsageDetectorTest {

    private val STUB_OLD_PREFS = kotlin("""
      |package io.flatcircle.preferenceshelper
      |
      |  import android.preference.PreferenceManager
      |
      |private fun oldPrefs() {
      |
      |     val prefs = PreferenceManager.getDefaultSharedPreferences(context).edit()
      |}""".trimMargin())

    @Test
    fun usingOldSharedPreferences() {

        lint()
            .files(STUB_OLD_PREFS)
            .issues(WrongPreferencesUsageDetector.ISSUE_OLD_PREFERENCES)
            .run()
            .expect("""
                |src/io/flatcircle/preferenceshelper/test.kt:7: Warning: Using 'SharedPreferences' instead of 'PreferencesHelper' [DirectSharedPreferences]
                |     val prefs = PreferenceManager.getDefaultSharedPreferences(context).edit()
                |                 ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                |0 errors, 1 warnings""".trimMargin())
            .expectFixDiffs("""
                |Fix for src/io/flatcircle/preferenceshelper/test.kt line 7: Replace with Prefs(context):
                |@@ -7 +7
                |-      val prefs = PreferenceManager.getDefaultSharedPreferences(context).edit()
                |+      val prefs = Prefs(context).edit()
            |""".trimMargin())
    }

    private val PREFERENCES_STUB_2 = kotlin("""
    |package io.flatcircle.preferenceshelper
    |
    |class Prefs {
    |
    |    fun putBoolean(key: String, value: Boolean) {}
    |    fun edit(){} 
    |    
    |    fun doWrongs() {
    |
    |        Prefs().edit()
    |    }
    |}""".trimMargin())

    @Test
    fun editingPrefsDirectly() {

        lint()
            .files(PREFERENCES_STUB_2)
            .issues(WrongPreferencesUsageDetector.ISSUE_LINGERING_EDIT)
            .run()
            .expect("""
                |src/io/flatcircle/preferenceshelper/Prefs.kt:10: Warning: Attempting to edit Prefs directly' [LingeringEdit]
                |        Prefs().edit()
                |        ~~~~~~~~~~~~~~
                |0 errors, 1 warnings""".trimMargin())
            .expectFixDiffs("""
                |Fix for src/io/flatcircle/preferenceshelper/Prefs.kt line 10: Delete ".edit()":
                |@@ -10 +10
                |-         Prefs().edit()
                |+         Prefs()
                |""".trimMargin())
    }

    private val STUB_PUT_BOOL = kotlin("""
    |package io.flatcircle.preferenceshelper
    |
    |class Prefs {
    |
    |    fun putBoolean(key: String, value: Boolean) {}
    |    
    |    fun doWrongs() {
    |
    |        Prefs().putBoolean("abc", true)
    |    }
    |}""".trimMargin())

    @Test
    fun usingPutBoolean() {

        lint()
            .files(STUB_PUT_BOOL)
            .issues(WrongPreferencesUsageDetector.ISSUE_PUT_BOOLEAN)
            .run()
            .expect("""
                |src/io/flatcircle/preferenceshelper/Prefs.kt:9: Warning: Don't use putPrimitive [NotUsingSet]
                |        Prefs().putBoolean("abc", true)
                |        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                |0 errors, 1 warnings""".trimMargin())
            .expectFixDiffs("""
                |Fix for src/io/flatcircle/preferenceshelper/Prefs.kt line 9: Replace with set("abc", true):
                |@@ -9 +9
                |-         Prefs().putBoolean("abc", true)
                |+         Prefs().set("abc", true)
            |""".trimMargin())
    }
}