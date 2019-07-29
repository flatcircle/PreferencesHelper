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
                ISSUE_OLD_PREFERENCES, ISSUE_DIRECT_EDIT, ISSUE_LINGERING_EDIT, ISSUE_PUT_BOOLEAN
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
                    Category.MESSAGES, 6, Severity.WARNING,
                    new Implementation(WrongPreferencesUsageDetector.class, Scope.JAVA_FILE_SCOPE));


    public static final Issue ISSUE_PUT_BOOLEAN =
            Issue.create("NotUsingSet", "Don't use putPrimitive",
                    "Use .set() instead.",
                    Category.MESSAGES, 6, Severity.WARNING,
                    new Implementation(WrongPreferencesUsageDetector.class, Scope.JAVA_FILE_SCOPE));

    @Override public List<String> getApplicableMethodNames() {
        return Arrays.asList("getDefaultSharedPreferences", "edit", "commit", "apply", "putBoolean", "putString");
    }

    @Override public void visitMethod(JavaContext context, UCallExpression call, PsiMethod method) {
        String methodName = call.getMethodName();
        JavaEvaluator evaluator = context.getEvaluator();
        String parentname = call.getUastParent().asSourceString();
//        String parentType = call.getUastParent().

        if ("edit".equals(methodName) && evaluator.isMemberInClass(method, "io.flatcircle.preferenceshelper.Prefs")) {
            LintFix fix = quickFixDelete(call);
            context.report(ISSUE_LINGERING_EDIT,  call, context.getLocation(call),
                    "Attempting to edit Prefs directly'",
                    fix);
            return;
        }

        if (("putBoolean".equals(methodName) || "putString".equals(methodName))
                && evaluator.isMemberInClass(method, "io.flatcircle.preferenceshelper.Prefs")) {
            LintFix fix = quickFixPut(call);
            context.report(ISSUE_PUT_BOOLEAN, call, context.getLocation(call),
                    "Don't use putPrimitive",
                    fix);
            return;
        }

        if ("getDefaultSharedPreferences".equals(methodName) && evaluator.isMemberInClass(method, "android.preference.PreferenceManager")) {
            LintFix fix = quickFixIssueOldPreferences(call);
            context.report(ISSUE_OLD_PREFERENCES, call, context.getLocation(call),
                    "Using 'SharedPreferences' instead of 'PreferencesHelper'",
                    fix);
            return;
        }
    }

    private LintFix quickFixIssueOldPreferences(UCallExpression logCall) {
        List<UExpression> arguments = logCall.getValueArguments();
        UExpression context = arguments.get(0);

        String fixSource = "Prefs(" + context.asSourceString() + ")";
        String logCallSource = "PreferenceManager." + logCall.asSourceString();

        LintFix.GroupBuilder fixGrouper = fix().group();
        fixGrouper.add(fix().replace().text(logCallSource).shortenNames().reformat(true).with(fixSource).build());
        return fixGrouper.build();
    }

    private LintFix quickFixPut(UCallExpression logCall) {
        List<UExpression> arguments = logCall.getValueArguments();
        UExpression key = arguments.get(0);
        UExpression value = arguments.get(1);

        String fixSource = "set(" + key.asSourceString() + ", " + value.asSourceString() + ")";
        String withApply = logCall.asSourceString() + ".apply()";
        String withCommit = logCall.asSourceString() + ".commit()";
        String withNeither = logCall.asSourceString();

        LintFix.GroupBuilder fixGrouper = fix().group();
//        fixGrouper.add(fix().replace().text(withApply).shortenNames().reformat(true).with(fixSource).build());
//        fixGrouper.add(fix().replace().text(withCommit).shortenNames().reformat(true).with(fixSource).build());
        fixGrouper.add(fix().replace().text(withNeither).shortenNames().reformat(true).with(fixSource).build());
        return fixGrouper.build();
    }


    private LintFix quickFixDelete(UCallExpression logCall) {
        String logCallSource = "."+logCall.asSourceString();

        LintFix.GroupBuilder fixGrouper = fix().group();
        fixGrouper.add(fix().replace().text(logCallSource).shortenNames().reformat(true).with("").build());
        return fixGrouper.build();
    }

}