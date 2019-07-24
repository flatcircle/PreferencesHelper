package io.flatcircle.preferenceslint;

import com.android.tools.lint.client.api.JavaEvaluator;
import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.LintFix;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;
import com.intellij.psi.PsiMethod;

import org.jetbrains.uast.UCallExpression;
import org.jetbrains.uast.UExpression;

import java.util.Arrays;
import java.util.List;

public final class WrongPreferencesUsageDetector extends Detector implements Detector.UastScanner {

    static Issue[] getIssues() {
        return new Issue[] {
                ISSUE_OLD_PREFERENCES, ISSUE_DIRECT_EDIT, ISSUE_LINGERING_EDIT, ISSUE_LINGERING_APPLY
        };
    }

    public static final Issue ISSUE_OLD_PREFERENCES =
            Issue.create("DirectSharedPreferences", "Saving to SharedPreferences " +
                            "directly instead of using library",
                    "Since PreferencesHelper is included in the project, it is likely " +
                            "that calls to PreferencesHelper should instead be going to PrefHelper.",
                    Category.MESSAGES, 5, Severity.WARNING,
                    new Implementation(WrongPreferencesUsageDetector.class, Scope.JAVA_FILE_SCOPE));

    public static final Issue ISSUE_DIRECT_EDIT =
            Issue.create("DirectEditSharedPreferences", "Saving to " +
                            "SharedPreferences directly instead of using library",
                    "Since PreferencesHelper is included in the project, it is likely " +
                            "that calls to PreferencesHelper should instead be going to PrefHelper.",
                    Category.MESSAGES, 5, Severity.WARNING,
                    new Implementation(WrongPreferencesUsageDetector.class, Scope.JAVA_FILE_SCOPE));

    public static final Issue ISSUE_LINGERING_EDIT =
            Issue.create("LingeringEdit", "You cannot edit Prefs",
                    "Probably after doing a quickfix.",
                    Category.MESSAGES, 8, Severity.ERROR,
                    new Implementation(WrongPreferencesUsageDetector.class, Scope.JAVA_FILE_SCOPE));


    public static final Issue ISSUE_LINGERING_APPLY =
            Issue.create("LingeringApply", "You don't need to apply anymore",
                    "Probably after doing a quickfix.",
                    Category.MESSAGES, 8, Severity.ERROR,
                    new Implementation(WrongPreferencesUsageDetector.class, Scope.JAVA_FILE_SCOPE));

    @Override public List<String> getApplicableMethodNames() {
        return Arrays.asList("getDefaultSharedPreferences", "edit", "commit", "apply");
    }

    @Override public void visitMethod(JavaContext context, UCallExpression call, PsiMethod method) {
        String methodName = call.getMethodName();
        JavaEvaluator evaluator = context.getEvaluator();
        String parentname = call.getUastParent().asSourceString();
//        String parentType = call.getUastParent().

        if ("getDefaultSharedPreferences".equals(methodName) && evaluator.isMemberInClass(method, "android.preference.PreferenceManager")) {
            LintFix fix = quickFixIssueOldPreferences(call);
            context.report(ISSUE_OLD_PREFERENCES, call, context.getLocation(call),
                    "Using 'SharedPreferences' instead of 'PreferencesHelper'",
                    fix);
            return;
        }
        if ("edit".equals(methodName) && evaluator.isMemberInClass(method, "io.flatcircle.preferenceshelper.Prefs")) {
            LintFix fix = quickFixDelete(call);
            context.report(ISSUE_LINGERING_EDIT, call, context.getLocation(call),
                    "Attempting to edit Prefs directly'",
                    fix);
            return;
        }

        if ("apply".equals(methodName) && "Unit".equals(parentname)) {
            context.report(ISSUE_LINGERING_APPLY, call, context.getLocation(call), "Attempting to apply a unit");
            return;
        }
    }

    private LintFix quickFixIssueOldPreferences(UCallExpression logCall) {
        List<UExpression> arguments = logCall.getValueArguments();
        UExpression context = arguments.get(0);

        String fixSource = "Prefs(" + context.asSourceString() + ")";
        String logCallSource = "PreferenceManager."+logCall.asSourceString();

        LintFix.GroupBuilder fixGrouper = fix().group();
        fixGrouper.add(fix().replace().text(logCallSource).shortenNames().reformat(true).with(fixSource).build());
        return fixGrouper.build();
    }


    private LintFix quickFixDelete(UCallExpression logCall) {
        String logCallSource = "."+logCall.asSourceString();

        LintFix.GroupBuilder fixGrouper = fix().group();
        fixGrouper.add(fix().replace().text(logCallSource).shortenNames().reformat(true).with("").build());
        return fixGrouper.build();
    }

}