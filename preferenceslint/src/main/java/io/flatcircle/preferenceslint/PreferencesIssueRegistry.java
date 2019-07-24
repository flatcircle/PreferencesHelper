package io.flatcircle.preferenceslint;

import com.android.tools.lint.client.api.IssueRegistry;
import com.android.tools.lint.detector.api.ApiKt;
import com.android.tools.lint.detector.api.Issue;

import java.util.Arrays;
import java.util.List;

public final class PreferencesIssueRegistry extends IssueRegistry {
    @Override public List<Issue> getIssues() {
        return Arrays.asList(WrongPreferencesUsageDetector.getIssues());
    }

    @Override public int getApi() {
        return ApiKt.CURRENT_API;
    }
}
