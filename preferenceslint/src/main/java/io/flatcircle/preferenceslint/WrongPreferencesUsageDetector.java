package io.flatcircle.preferenceslint;

import com.android.tools.lint.client.api.JavaEvaluator;
import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.JavaContext;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;
import com.intellij.psi.PsiMethod;

import org.jetbrains.uast.UCallExpression;

import java.util.Arrays;
import java.util.List;

public final class WrongPreferencesUsageDetector extends Detector implements Detector.UastScanner {

    static Issue[] getIssues() {
        return new Issue[] {
                ISSUE_OLD_PREFERENCES
        };
    }

    public static final Issue ISSUE_OLD_PREFERENCES =
            Issue.create("OldSharedPreferences", "Saving to SharedPreferences " +
                            "directly instead of using library",
                    "Since PreferencesHelper is included in the project, it is likely " +
                            "that calls to PreferencesHelper should instead be going to PrefHelper.",
                    Category.MESSAGES, 5, Severity.WARNING,
                    new Implementation(WrongPreferencesUsageDetector.class, Scope.JAVA_FILE_SCOPE));

    @Override public List<String> getApplicableMethodNames() {
        return Arrays.asList("getDefaultSharedPreferences", "edit");
    }

    @Override public void visitMethod(JavaContext context, UCallExpression call, PsiMethod method) {
        String methodName = call.getMethodName();
        JavaEvaluator evaluator = context.getEvaluator();

        if ("getDefaultSharedPreferences".equals(methodName) && evaluator.isMemberInClass(method, "android.preference.PreferenceManager")) {
//            LintFix fix = quickFixIssueOldPreferences(call);
            context.report(ISSUE_OLD_PREFERENCES, call, context.getLocation(call), "Using 'SharedPreferences' instead of 'PreferencesHelper'");
            return;
        }
    }

//    private LintFix quickFixIssueOldPreferences(UCallExpression logCall) {
//        List<UExpression> arguments = logCall.getValueArguments();
//        String methodName = logCall.getMethodName();
//        UExpression tag = arguments.get(0);
//
//        // 1st suggestion respects author's tag preference.
//        // 2nd suggestion drops it (Timber defaults to calling class name).
//        String fixSource1 = "Timber.tag(" + tag.asSourceString() + ").";
//        String fixSource2 = "Timber.";
//
//        int numArguments = arguments.size();
//        if (numArguments == 2) {
//            UExpression msgOrThrowable = arguments.get(1);
//            fixSource1 += methodName + "(" + msgOrThrowable.asSourceString() + ")";
//            fixSource2 += methodName + "(" + msgOrThrowable.asSourceString() + ")";
//        } else if (numArguments == 3) {
//            UExpression msg = arguments.get(1);
//            UExpression throwable = arguments.get(2);
//            fixSource1 +=
//                    methodName + "(" + throwable.asSourceString() + ", " + msg.asSourceString() + ")";
//            fixSource2 +=
//                    methodName + "(" + throwable.asSourceString() + ", " + msg.asSourceString() + ")";
//        } else {
//            throw new IllegalStateException("android.util.Log overloads should have 2 or 3 arguments");
//        }
//
//        String logCallSource = logCall.asSourceString();
//        LintFix.GroupBuilder fixGrouper = fix().group();
//        fixGrouper.add(
//                fix().replace().text(logCallSource).shortenNames().reformat(true).with(fixSource1).build());
//        fixGrouper.add(
//                fix().replace().text(logCallSource).shortenNames().reformat(true).with(fixSource2).build());
//        return fixGrouper.build();
//    }

}