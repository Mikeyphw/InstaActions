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
    // Manual admission only: we cannot read/control another Vector module's toggle.
    private boolean itmRecoveryOffAcknowledged;

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

    static boolean hasActiveWindow() { return CAPTURE.isActiveWindow(); }
    static R1CD05FabSession.Step activeStage() { return CAPTURE.step(); }

    static void record(R1BNavigationPolicy.Screen screen,
            R1CD05ReferenceContinuity.Result result, boolean typed,
            R1CD06MenuOwnership.Verdict ownership,
            R1CD06VisiblePageBinding.Verdict visible,
            R1CD06PreMenuReconciliation.Verdict preMenu) {
        // All Android view work has already been bounded on the callback main thread;
        // only enums and counts enter the FAB's report session.
        CAPTURE.observe(screen, result, typed, ownership, visible, preMenu,
                SystemClock.elapsedRealtime());
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
                // Capture a separate bounded, content-free live viewer snapshot.
                // Even when the menu callback is off-main-thread, the report
                // still diagnoses the pager/page structure independently.
                // Keep the weak pre-menu view snapshot fresh throughout the
                // active window, even after 12 reportable structural samples.
                // The extra samples are not added to the bounded raw report.
                if (CAPTURE.isActiveWindow()) {
                    R1CD06ViewPreflight.Snapshot snapshot =
                            R1CD06ViewPreflight.inspect(activity);
                    if (CAPTURE.needsPreflight())
                        CAPTURE.notePreflight(snapshot, SystemClock.elapsedRealtime());
                }
                // D07 starts only after D04's existing exact APK hash passed.
                InstaActionsEntry.requestD07(activity);
                if (R1CD07Recovery.isEnabled()
                        && R1BContextMonitor.stableScreen() != R1BNavigationPolicy.Screen.REELS) {
                    R1CD07Recovery.off("Anchored Reels navigation lost; native eligibility restored");
                    itmRecoveryOffAcknowledged=false;
                }
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
        TextView title = label("InstaActions · D06 Visible Page", 16);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        body.addView(title);
        body.addView(label("Read-only · InstaEclipse stays ON · source-first confirmation", 11));
        body.addView(label("IE source anchors are checked against the exact IG455 APK once at startup; no menu test required for this step.", 11));
        status = label("", 13);
        body.addView(status);
        primary = button("Start A1", this::primaryAction);
        body.addView(primary);
        body.addView(button("Cancel capture", () -> {
            CAPTURE.cancel(false);
            R1CD06PreMenuReconciliation.clear();
            refresh();
        }));
        body.addView(label("D07 · Native Repost recovery · dedicated Reels tab only", 14));
        body.addView(label("Manual A/B labels are not account IDs. Start A while browsing Reels in the Reels tab; wait for 5+ original FALSE/singleton results. OFF before changing accounts/tabs. ITM recovery must be OFF.", 11));
        body.addView(button("Start A · Reels tab (read-only)", () -> {
            R1CD07GateTrace.stopExactTrial();
            itmRecoveryOffAcknowledged=false;
            toast(R1CD07GateTrace.begin(0));refresh();
        }));
        body.addView(button("Start B · comparison (read-only)", () -> {
            R1CD07GateTrace.stopExactTrial();
            itmRecoveryOffAcknowledged=false;
            toast(R1CD07GateTrace.begin(1));refresh();
        }));
        body.addView(button("Stop A/B · recovery OFF", () -> {
            itmRecoveryOffAcknowledged=false;
            toast(R1CD07GateTrace.stop());refresh();
        }));
        body.addView(button("Confirm ITM Repost recovery is OFF", () -> {
            itmRecoveryOffAcknowledged = true;
            toast("Manual claim noted (not independently verified)");refresh();
        }));
        body.addView(button("Enable native Repost · Reels TAB (90 s)", () -> {
            if (!itmRecoveryOffAcknowledged) {
                toast("REFUSED: first confirm ITM Repost recovery OFF");return;
            }
            if (CAPTURE.isActiveWindow()) {
                toast("REFUSED: stop D05 capture first");return;
            }
            // REELS is a supporting navigation hint, NEVER Reels-tab proof.
            // The separate button explicitly records the user's tab assertion.
            if (R1BContextMonitor.stableScreen() != R1BNavigationPolicy.Screen.REELS) {
                toast("REFUSED: anchored Reels viewer not detected");return;
            }
            toast(R1CD07GateTrace.armHF13Recovery());refresh();
        }));
        body.addView(button("Recovery OFF (before account/tab change)", () -> {
            toast(R1CD07GateTrace.stopExactTrial());
            itmRecoveryOffAcknowledged=false;refresh();
        }));
        body.addView(button("Copy D07 safe Repost report", this::copyD07));
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

    private void copyD07() {
        ClipboardManager manager=(ClipboardManager)
                activity.getSystemService(Context.CLIPBOARD_SERVICE);
        if(manager==null){toast("Clipboard unavailable");return;}
        manager.setPrimaryClip(ClipData.newPlainText("INA D07 safe Repost report",
                R1CD07GateTrace.report() +
                "D06_MEDIA_IDENTITY=NOT_VERIFIED\nACTIONS=OFF\n"
                + "ITM_REPOST_OFF=MANUAL_ASSERTION_NOT_VERIFIED\n"));
        toast("D07 content-free report copied");
    }

    private void primaryAction() {
        R1CD07Recovery.onCaptureTransition();
        itmRecoveryOffAcknowledged=false;
        R1CD05FabSession.Step step = CAPTURE.step();
        if (step == R1CD05FabSession.Step.IDLE || step == R1CD05FabSession.Step.DONE
                || step == R1CD05FabSession.Step.CANCELLED) {
            R1CD06PreMenuReconciliation.clear();
            if (!R1CD04PassiveProbe.beginFabCapture(CAPTURE, SystemClock.elapsedRealtime())) {
                toast("Hook not ready or event limit reached; restart Instagram if needed.");
                return;
            }
        } else {
            CAPTURE.advance(SystemClock.elapsedRealtime());
            R1CD06PreMenuReconciliation.clear();
        }
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
            status.setText("IE upstream: " + R1CD06UpstreamConformance.summary()
                    + "\nHook: " + (R1CD04PassiveProbe.isInstalled() ? "INSTALLED" : "VERIFYING / REFUSED")
                    + "\nOwner: " + CAPTURE.currentOwnership()
                    + "\nVisible page: " + CAPTURE.currentVisiblePage()
                    + "\nPre-menu: " + CAPTURE.currentPreMenu()
                    + "\nViewer preflight: " + CAPTURE.currentPreflight()
                    + "\nStage: " + step + " · stable Reel callbacks: " + CAPTURE.currentCount()
                    + " · total: " + CAPTURE.total() + "\n" + CAPTURE.prompt()
                    + "\n\nD07 Repost: " + R1CD07GateTrace.feedback()
                    + "\nD07 gate: " + InstaActionsEntry.dataGateStatus()
                    + "\nITM recovery OFF assertion: " + itmRecoveryOffAcknowledged
                    + "\nD06 identity NOT VERIFIED · other actions OFF.");
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
        R1CD07Recovery.onActivityExit();
        itmRecoveryOffAcknowledged=false;
        detached = true;
        if (ticker != null) MAIN.removeCallbacks(ticker);
        CAPTURE.cancel(true);
        R1CD06PreMenuReconciliation.clear();
        close();
        if (chip != null) { decor.removeView(chip); chip = null; }
    }
}
