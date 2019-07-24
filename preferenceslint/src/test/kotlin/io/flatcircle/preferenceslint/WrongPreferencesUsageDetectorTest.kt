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
      |object PreferencesHelper {
      |  private fun oldPrefs() {
      |     val prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(context)
      |  }
      |}""".trimMargin())

    @Test
    fun usingOldSharedPreferences() {

        lint()
            .files(PREFERENCES_STUB)
            .issues(WrongPreferencesUsageDetector.ISSUE_OLD_PREFERENCES)
            .run()
            .expect("""
                |src/io/flatcircle/preferenceshelper/PreferencesHelper.kt:4: Warning: Using 'SharedPreferences' instead of 'PreferencesHelper' [OldSharedPreferences]
                |     val prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(context)
                |                 ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
0 errors, 1 warnings""".trimMargin())
//            .expectFixDiffs("""
//            |Fix for src/foo/Example.java line 4: Replace with Timber.tag("TAG").d("msg"):
//            |@@ -5 +5
//            |-     Log.d("TAG", "msg");
//            |+     Timber.tag("TAG").d("msg");
//            |Fix for src/foo/Example.java line 4: Replace with Timber.d("msg"):
//            |@@ -5 +5
//            |-     Log.d("TAG", "msg");
//            |+     Timber.d("msg");
//            |""".trimMargin())
    }



}