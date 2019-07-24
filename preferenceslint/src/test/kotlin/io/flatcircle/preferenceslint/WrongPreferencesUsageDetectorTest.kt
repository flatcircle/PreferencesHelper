package io.flatcircle.preferenceslint

import com.android.tools.lint.checks.infrastructure.TestFiles.kotlin
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import org.junit.Test

/**
 * Created by jacquessmuts on 2019-07-24
 */
class WrongPreferencesUsageDetectorTest {

    private val PREFERENCES_STUB = kotlin("""
      |package io.flatcircle.preferenceshelper
      |
      |import android.preference.PreferenceManager
      |
      |object PreferencesHelper {
      |  private fun oldPrefs() {
      |     val unit = Prefs(this).set("hi", true).apply()
      |     val prefs = PreferenceManager.getDefaultSharedPreferences(context).edit()
      |     
      |     val fixedPrefs1 = Prefs(this).edit()
      |  }
      |}""".trimMargin())

    @Test
    fun usingOldSharedPreferences() {

        lint()
            .files(PREFERENCES_STUB)
            .issues(WrongPreferencesUsageDetector.ISSUE_OLD_PREFERENCES)
            .run()
            .expect("""
                |src/io/flatcircle/preferenceshelper/PreferencesHelper.kt:7: Warning: Using 'SharedPreferences' instead of 'PreferencesHelper' [DirectSharedPreferences]
                |     val prefs = PreferenceManager.getDefaultSharedPreferences(context).edit()
                |                 ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                |0 errors, 1 warnings""".trimMargin())
            .expectFixDiffs("""
                |Fix for src/io/flatcircle/preferenceshelper/PreferencesHelper.kt line 7: Replace with Prefs(context):
                |@@ -7 +7
                |-      val prefs = PreferenceManager.getDefaultSharedPreferences(context)
                |+      val prefs = Prefs(context)
            |""".trimMargin())
    }

    @Test
    fun editingPrefsDirectly() {

        lint()
            .files(PREFERENCES_STUB)
            .issues(WrongPreferencesUsageDetector.ISSUE_OLD_PREFERENCES)
            .run()
            .expect("""
                |src/io/flatcircle/preferenceshelper/PreferencesHelper.kt:7: Warning: Using 'SharedPreferences' instead of 'PreferencesHelper' [DirectSharedPreferences]
                |     val prefs = PreferenceManager.getDefaultSharedPreferences(context).edit()
                |                 ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                |0 errors, 1 warnings""".trimMargin())
            .expectFixDiffs("""
                |Fix for src/io/flatcircle/preferenceshelper/PreferencesHelper.kt line 7: Replace with Prefs(context):
                |@@ -7 +7
                |-      val prefs = PreferenceManager.getDefaultSharedPreferences(context)
                |+      val prefs = Prefs(context)
            |""".trimMargin())

    }




}