package com.mikeyphw.instaactions;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/** R0 companion: informative only, no Vector enable-state claims. */
public final class MainActivity extends Activity {
    private LinearLayout root;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.rgb(15, 23, 42));
        getWindow().setNavigationBarColor(Color.rgb(15, 23, 42));

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(Color.rgb(15, 23, 42));
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int horizontal = dp(24);
        int vertical = dp(20);
        root.setPadding(horizontal, vertical, horizontal, vertical);
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
            root.setPadding(horizontal, vertical + top, horizontal, vertical + bottom);
            return insets;
        });
        setContentView(scroll);
        title("InstaActions", 26);
        label("R0  •  Foundation and diagnostics", 14, 0xffa5b4fc);
        section("Installation");
        label("Instagram: " + installedVersion(ModuleConstants.TARGET_PACKAGE), 16, Color.WHITE);
        label("InstaEclipse: " + installedVersion("ps.reso.instaeclipse"), 16, Color.WHITE);
        label("Vector module enabled: NOT VERIFIED HERE", 15, 0xfffbbf24);
        section("Safety / scope");
        label("Target: com.instagram.android (main process only)", 15, Color.WHITE);
        label("Action hooks: OFF (R0)", 15, 0xff86efac);
        label("No network, storage or account permissions requested.", 15, Color.LTGRAY);
        section("Device acceptance");
        label("1. Install this APK and enable only Instagram in Vector.", 15, Color.WHITE);
        label("2. Keep InstaEclipse enabled and force-stop Instagram.", 15, Color.WHITE);
        label("3. Open Instagram and look for INA_R0_PACKAGE_READY in logcat.", 15, Color.WHITE);
        label("4. Confirm Instagram UI stays unchanged and both modules remain active.",
                15, Color.WHITE);
        label("Note: installed package detection is not proof that a module loaded.",
                13, Color.LTGRAY);
    }

    private String installedVersion(String pkg) {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(pkg, 0);
            return info.versionName == null ? "installed" : info.versionName;
        } catch (PackageManager.NameNotFoundException error) {
            return "not visible / not installed";
        }
    }

    private void section(String value) {
        title(value, 18);
    }

    private void title(String value, float size) {
        label(value, size, Color.WHITE).setTypeface(null, Typeface.BOLD);
    }

    private TextView label(String value, float size, int color) {
        TextView text = new TextView(this);
        text.setText(value);
        text.setTextSize(size);
        text.setTextColor(color);
        text.setGravity(Gravity.START);
        text.setPadding(0, dp(9), 0, dp(9));
        root.addView(text, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return text;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
