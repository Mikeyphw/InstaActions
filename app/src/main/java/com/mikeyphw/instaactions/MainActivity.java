package com.mikeyphw.instaactions;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * D07 UX01 information screen. Installed package versions are informational,
 * not claims about Vector scope, hook readiness or D07C device acceptance.
 */
public final class MainActivity extends Activity {
    private static final int BACKGROUND = 0xff0c1320;
    private static final int CARD = 0xff1b2739;
    private static final int BORDER = 0xff51667d;
    private static final int WHITE = 0xfff9fbff;
    private static final int DETAIL = 0xffd7e1ed;
    private static final int BLUE = 0xff74c3ff;
    private static final int AMBER = 0xffffd585;
    private static final int GREEN = 0xff70ecbb;
    private LinearLayout root;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(BACKGROUND);
        getWindow().setNavigationBarColor(BACKGROUND);
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BACKGROUND);
        root = column();
        final int h = dp(18), v = dp(14);
        root.setPadding(h, v, h, v);
        scroll.addView(root);
        scroll.setOnApplyWindowInsetsListener((view, insets) -> {
            int top, bottom;
            if (Build.VERSION.SDK_INT >= 30) {
                android.graphics.Insets bars = insets.getInsets(WindowInsets.Type.systemBars());
                top = bars.top;
                bottom = bars.bottom;
            } else {
                top = insets.getSystemWindowInsetTop();
                bottom = insets.getSystemWindowInsetBottom();
            }
            root.setPadding(h, v + top, h, v + bottom);
            return insets;
        });
        setContentView(scroll);

        add(root, "InstaActions", 25, WHITE, true);
        add(root, "D07 · Native Repost recovery", 16, BLUE, true);
        add(root, "Experimental. Not yet qualified on this Android device.", 14, AMBER, false);

        LinearLayout status = card("What is ready?");
        add(status, "D07 SOURCE PRESENT · DEVICE TEST PENDING", 14, GREEN, true);
        add(status, "Device acceptance: NOT RUN. A successful build does not prove the button appeared or that your Repost went through.", 14, DETAIL, false);
        add(status, "Instagram: " + installedVersion(ModuleConstants.TARGET_PACKAGE), 14, WHITE, false);
        add(status, "InstaEclipse: " + installedVersion("ps.reso.instaeclipse"), 14, WHITE, false);
        add(status, "Vector / LSPosed module enabled: NOT VERIFIED from this screen", 13, AMBER, false);

        LinearLayout guide = card("How to try Repost");
        add(guide, "1 · Enable InstaActions for Instagram in Vector, and leave InstaEclipse enabled. Restart Instagram.", 14, WHITE, false);
        add(guide, "2 · In Instagram, open the dedicated Reels tab at the bottom. Do not use Feed Reels or ordinary posts.", 14, WHITE, false);
        add(guide, "3 · Tap the floating IA chip. Choose Start A, close the panel and browse a few Reels on the affected account.", 14, WHITE, false);
        add(guide, "4 · Reopen IA. When evidence is ready, confirm InstaTaskManager's Repost recovery is OFF; then enable the short Repost recovery.", 14, WHITE, false);
        add(guide, "5 · Close IA and tap Instagram's ORIGINAL Repost button yourself. Return to IA and press Recovery OFF before switching accounts or tabs.", 14, WHITE, false);
        add(guide, "6 · Copy the D07 safe report. Use the separate D07C acceptance runner to record what happened on the physical device.", 14, WHITE, false);
        add(guide, "Keep A recording until recovery is enabled. Stop A/B also switches recovery OFF.", 14, AMBER, true);

        LinearLayout safety = card("What the controls actually do");
        add(safety, "Start A / Start B: read-only native result observations. These are manual labels, NOT verified Instagram account identities.", 14, DETAIL, false);
        add(safety, "Enable: at most 90 seconds or 64 eligible changes to one verified native Boolean result. OFF is the default.", 14, DETAIL, false);
        add(safety, "Recovery OFF: restores original values for subsequent native calls. It does not undo a Repost already performed in Instagram.", 14, DETAIL, false);
        add(safety, "Advanced D05/D06: optional diagnostics only; media identity remains unverified and unrelated actions remain disabled.", 14, DETAIL, false);
        add(safety, "Same-Activity account changes cannot be reliably detected. Turn OFF manually before changing accounts.", 14, AMBER, false);

        LinearLayout limits = card("Supported surfaces and privacy");
        add(limits, "Dedicated Reels tab: previous HF12 success in InstaTaskManager; InstaActions transfer still needs device validation.", 14, WHITE, false);
        add(limits, "Reels in Feed, profiles, and ordinary posts: NOT QUALIFIED.", 14, AMBER, false);
        add(limits, "Reports summarize native return types/counts, not account names, media IDs or captions. No automatic Repost actions.", 14, DETAIL, false);
    }

    private LinearLayout column() {
        LinearLayout result = new LinearLayout(this);
        result.setOrientation(LinearLayout.VERTICAL);
        return result;
    }
    private LinearLayout card(String title) {
        LinearLayout card = column();
        card.setPadding(dp(16), dp(12), dp(16), dp(14));
        GradientDrawable background = new GradientDrawable();
        background.setColor(CARD);
        background.setCornerRadius(dp(14));
        background.setStroke(dp(1), BORDER);
        card.setBackground(background);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(14);
        root.addView(card, lp);
        add(card, title, 18, WHITE, true);
        return card;
    }
    private TextView add(LinearLayout container, String value, int sp, int color, boolean bold) {
        TextView text = new TextView(this);
        text.setText(value);
        text.setTextSize(sp);
        text.setTextColor(color);
        text.setGravity(Gravity.START);
        text.setLineSpacing(dp(2), 1f);
        text.setPadding(0, dp(5), 0, dp(5));
        if (bold) text.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        container.addView(text, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return text;
    }
    private String installedVersion(String pkg) {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(pkg, 0);
            return info.versionName == null ? "installed (version unknown)" : info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "not visible or not installed";
        }
    }
    private int dp(int number) {
        return Math.round(number * getResources().getDisplayMetrics().density);
    }
}
