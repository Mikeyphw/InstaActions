package com.mikeyphw.instaactions;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity-local InstaActions diagnostic FAB. Never overlays other apps,
 * mutates Instagram content, reads Instagram view text, or changes Vector scope.
 * It coexists with InstaTaskManager's upper-right chip and InstaEclipse menus.
 */
final class R1CD05FabPanel {
    private static final int BG = Color.rgb(24, 30, 42);
    private static final int FG = Color.rgb(240, 247, 249);
    private static final int ACCENT = Color.rgb(83, 208, 183);
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private static final R1CD05FabSession CAPTURE = new R1CD05FabSession();
    private static R1CD05FabPanel current;
    private static int savedTopDp = 220;

    private final Activity activity;
    private final FrameLayout decor;
    private TextView chip;
    private ScrollView panel;
    private TextView status;
    private Button primary;
    private Runnable ticker;
    private boolean detached;

    private R1CD05FabPanel(Activity activity, FrameLayout decor) {
        this.activity = activity;
        this.decor = decor;
    }

    static void onResumed(Activity activity) {
        if (activity == null || activity.isFinishing() || activity.isDestroyed()
                || !ModuleConstants.TARGET_PACKAGE.equals(activity.getPackageName())
                || !R1BNavigationPolicy.isMainActivity(activity.getClass().getName())) return;
        // Unknown Instagram versions receive no injected UI at all.
        // SHA-256/method verification is separately mandatory before capture.
        try {
            android.content.pm.PackageInfo info = activity.getPackageManager().getPackageInfo(
                    ModuleConstants.TARGET_PACKAGE, 0);
            if (!R1CD04Policy.TARGET_VERSION.equals(info.versionName)) return;
        } catch (Throwable unqualified) { return; }
        View root = activity.getWindow() == null ? null : activity.getWindow().getDecorView();
        if (!(root instanceof FrameLayout)) return;
        if (current != null && current.activity == activity) return;
        if (current != null) current.detach();
        R1CD05FabPanel next = new R1CD05FabPanel(activity, (FrameLayout) root);
        current = next;
        next.attach();
    }

    static void onLeaving(Activity activity) {
        if (current != null && current.activity == activity) {
            current.detach();
            current = null;
        }
    }

    static void record(R1BNavigationPolicy.Screen screen,
            R1CD05ReferenceContinuity.Result result, boolean typed,
            R1CD06MenuOwnership.Verdict ownership) {
        // No Android view work on Instagram's hooked callback thread.
        CAPTURE.observe(screen, result, typed, ownership, SystemClock.elapsedRealtime());
    }

    private int dp(float value) {
        return (int) (value * activity.getResources().getDisplayMetrics().density + .5f);
    }
    private GradientDrawable bg(int color, int radiusDp) {
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(color);
        shape.setCornerRadius(dp(radiusDp));
        shape.setStroke(dp(1), ACCENT);
        return shape;
    }

    private void attach() {
        chip = new TextView(activity);
        chip.setText("IA");
        chip.setTextColor(FG);
        chip.setTextSize(14);
        chip.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        chip.setGravity(Gravity.CENTER);
        chip.setElevation(dp(7));
        chip.setBackground(bg(BG, 24));
        chip.setContentDescription("InstaActions diagnostics - tap to open, drag to move");
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(dp(46), dp(46),
                Gravity.TOP | Gravity.END);
        lp.topMargin = dp(savedTopDp);
        lp.rightMargin = dp(12);
        decor.addView(chip, lp);
        chip.setOnClickListener(v -> toggle());
        chip.setOnTouchListener(new View.OnTouchListener() {
            private float y0;
            private int margin0;
            private boolean dragged;
            @Override public boolean onTouch(View v, MotionEvent e) {
                switch (e.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        y0 = e.getRawY();
                        margin0 = ((FrameLayout.LayoutParams) chip.getLayoutParams()).topMargin;
                        dragged = false;
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        if (Math.abs(e.getRawY() - y0) > dp(8)) dragged = true;
                        if (dragged) {
                            FrameLayout.LayoutParams p = (FrameLayout.LayoutParams) chip.getLayoutParams();
                            int max = Math.max(dp(32), decor.getHeight() - dp(62));
                            p.topMargin = Math.max(dp(32), Math.min(max,
                                    margin0 + (int) (e.getRawY() - y0)));
                            chip.setLayoutParams(p);
                        }
                        return true;
                    case MotionEvent.ACTION_UP:
                        if (!dragged) {
                            chip.performClick();
                        } else {
                            savedTopDp = Math.round(((FrameLayout.LayoutParams) chip.getLayoutParams()).topMargin
                                    / activity.getResources().getDisplayMetrics().density);
                            positionPanel();
                        }
                        return true;
                    case MotionEvent.ACTION_CANCEL: return true;
                    default: return false;
                }
            }
        });
        ticker = new Runnable() {
            @Override public void run() {
                if (detached || current != R1CD05FabPanel.this) return;
                refresh();
                MAIN.postDelayed(this, 1000);
            }
        };
        refresh();
        MAIN.postDelayed(ticker, 1000);
    }

    private void toggle() { if (panel == null) open(); else close(); }
    private TextView label(String text, int size) {
        TextView v = new TextView(activity);
        v.setText(text);
        v.setTextColor(FG);
        v.setTextSize(size);
        v.setPadding(dp(3), dp(5), dp(3), dp(5));
        return v;
    }
    private Button button(String title, Runnable action) {
        Button b = new Button(activity);
        b.setText(title);
        b.setAllCaps(false);
        b.setTextSize(13);
        b.setOnClickListener(v -> action.run());
        return b;
    }

    private void open() {
        if (detached || panel != null) return;
        ScrollView scroll = new ScrollView(activity);
        scroll.setBackground(bg(BG, 16));
        scroll.setElevation(dp(9));
        LinearLayout body = new LinearLayout(activity);
        body.setPadding(dp(12), dp(10), dp(12), dp(10));
        body.setOrientation(LinearLayout.VERTICAL);
        TextView title = label("InstaActions · D06 Native Menu Ownership", 16);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        body.addView(title);
        body.addView(label("Read-only · no action hooks · InstaEclipse stays ON", 11));
        status = label("", 13);
        body.addView(status);
        primary = button("Start A1", this::primaryAction);
        body.addView(primary);
        body.addView(button("Cancel capture", () -> {
            CAPTURE.cancel(false);
            refresh();
        }));
        body.addView(button("Copy safe report", this::copy));
        body.addView(button("Share safe report", this::share));
        body.addView(button("Close", this::close));
        scroll.addView(body);
        panel = scroll;
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(dp(300),
                Math.min(dp(400), Math.max(dp(260), decor.getHeight() - dp(70))),
                Gravity.TOP | Gravity.END);
        lp.rightMargin = dp(12);
        decor.addView(panel, lp);
        positionPanel();
        refresh();
    }

    private void positionPanel() {
        if (panel == null || chip == null) return;
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) panel.getLayoutParams();
        int chipTop = ((FrameLayout.LayoutParams) chip.getLayoutParams()).topMargin;
        int available = Math.max(dp(20), decor.getHeight() - lp.height - dp(18));
        lp.topMargin = Math.max(dp(12), Math.min(available, chipTop + dp(52)));
        panel.setLayoutParams(lp);
    }
    private void close() {
        if (panel != null) { decor.removeView(panel); panel = null; status = null; primary = null; }
    }
    private void toast(String text) {
        Toast.makeText(activity.getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }

    private void primaryAction() {
        R1CD05FabSession.Step step = CAPTURE.step();
        if (step == R1CD05FabSession.Step.IDLE || step == R1CD05FabSession.Step.DONE
                || step == R1CD05FabSession.Step.CANCELLED) {
            if (!R1CD04PassiveProbe.beginFabCapture(CAPTURE, SystemClock.elapsedRealtime())) {
                toast("Hook not ready or event limit reached; restart Instagram if needed.");
                return;
            }
        } else CAPTURE.advance(SystemClock.elapsedRealtime());
        refresh();
        if (CAPTURE.step() != R1CD05FabSession.Step.DONE) {
            toast("D05 " + CAPTURE.step() + ": use the Reel menu, then tap IA to advance");
            close();
        } else toast("D05 capture finished. Copy or share the safe report.");
    }
    private void refresh() {
        if (chip == null) return;
        R1CD05FabSession.Step step = CAPTURE.step();
        boolean active = step == R1CD05FabSession.Step.A1 || step == R1CD05FabSession.Step.A2
                || step == R1CD05FabSession.Step.B1 || step == R1CD05FabSession.Step.B2;
        chip.setText(active ? step.name() : "IA");
        chip.setTextColor(active ? ACCENT : FG);
        chip.setContentDescription(active ? "InstaActions capturing " + step.name()
                + " - " + CAPTURE.currentCount() + " stable Reel callbacks" : "InstaActions diagnostics");
        if (status != null) {
            status.setText("Hook: " + (R1CD04PassiveProbe.isInstalled() ? "INSTALLED" : "VERIFYING / REFUSED")
                    + "\nOwner: " + CAPTURE.currentOwnership()
                    + "\nStage: " + step + " · stable Reel callbacks: " + CAPTURE.currentCount()
                    + " · total: " + CAPTURE.total() + "\n" + CAPTURE.prompt()
                    + "\n\nOwner match is not visible-Reel identity.");
        }
        if (primary != null) {
            primary.setText(!active ? "Start A1" : step == R1CD05FabSession.Step.B2
                    ? "Finish capture" : "Next stage");
            primary.setEnabled(active || R1CD04PassiveProbe.hasCaptureCapacity());
        }
    }
    private void copy() {
        ClipboardManager manager = (ClipboardManager)
                activity.getSystemService(Context.CLIPBOARD_SERVICE);
        if (manager == null) { toast("Clipboard unavailable"); return; }
        manager.setPrimaryClip(ClipData.newPlainText("INA D05 safe diagnostic report", CAPTURE.report()));
        toast("Content-free D05 report copied");
    }
    private void share() {
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_TEXT, CAPTURE.report());
        try { activity.startActivity(Intent.createChooser(send, "Share D05 diagnostic report")); }
        catch (RuntimeException unavailable) { toast("No share target available"); }
    }
    private void detach() {
        detached = true;
        if (ticker != null) MAIN.removeCallbacks(ticker);
        CAPTURE.cancel(true);
        close();
        if (chip != null) { decor.removeView(chip); chip = null; }
    }
}
