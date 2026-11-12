package com.mikeyphw.instaactions;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * D07 UX01. Activity-local, high-contrast user guide for existing native
 * Repost recovery. No new hooks, content access, Vector permissions or state
 * transitions. Exact-gate and recovery decisions live in D04/D07, not here.
 */
final class R1CD05FabPanel {
    // The foreground/background contrast is deliberately high for Android dark mode.
    private static final int BG = Color.rgb(12, 19, 32);
    private static final int CARD = Color.rgb(27, 39, 57);
    private static final int BORDER = Color.rgb(81, 102, 125);
    private static final int FG = Color.rgb(249, 251, 255);
    private static final int SECONDARY = Color.rgb(215, 225, 237);
    private static final int MUTED = Color.rgb(187, 202, 222);
    private static final int ACCENT = Color.rgb(116, 195, 255);
    private static final int PRIMARY_BG = Color.rgb(38, 109, 188);
    private static final int SUCCESS = Color.rgb(112, 236, 187);
    private static final int WARNING = Color.rgb(255, 213, 133);
    private static final int DANGER = Color.rgb(255, 178, 182);
    private static final int DANGER_BG = Color.rgb(108, 35, 47);
    private static final int SECONDARY_BG = Color.rgb(42, 58, 78);
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private static final R1CD05FabSession CAPTURE = new R1CD05FabSession();
    private static R1CD05FabPanel current;
    private static int savedTopDp = 220;

    private final Activity activity;
    private final FrameLayout decor;
    private TextView chip;
    private ScrollView panel;
    private TextView recoveryStatus;
    private TextView nextStep;
    private TextView captureStatus;
    private TextView guardStatus;
    private TextView advancedStatus;
    private LinearLayout advancedBody;
    private Button advancedToggle;
    private Button primary;
    private Runnable ticker;
    private boolean detached;
    private boolean advancedVisible;
    // Manual assertion only; cannot inspect another module's state.
    private boolean itmRecoveryOffAcknowledged;

    private R1CD05FabPanel(Activity activity, FrameLayout decor) {
        this.activity = activity;
        this.decor = decor;
    }

    static void onResumed(Activity activity) {
        if (activity == null || activity.isFinishing() || activity.isDestroyed()
                || !ModuleConstants.TARGET_PACKAGE.equals(activity.getPackageName())
                || !R1BNavigationPolicy.isMainActivity(activity.getClass().getName())) return;
        // No overlay on unqualified Instagram versions. The exact APK SHA is
        // verified separately by D04 before either observer can collect data.
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
        CAPTURE.observe(screen, result, typed, ownership, visible, preMenu,
                SystemClock.elapsedRealtime());
    }

    private int dp(float value) {
        return (int) (value * activity.getResources().getDisplayMetrics().density + .5f);
    }
    private GradientDrawable shape(int color, int radiusDp, int edge) {
        GradientDrawable s = new GradientDrawable();
        s.setColor(color);
        s.setCornerRadius(dp(radiusDp));
        s.setStroke(dp(1), edge);
        return s;
    }
    private RippleDrawable clickableBackground(int bg, int border) {
        return new RippleDrawable(ColorStateList.valueOf(0x55ffffff),
                shape(bg, 12, border), null);
    }
    private TextView label(String value, int sp, int color) {
        TextView text = new TextView(activity);
        text.setText(value);
        text.setTextSize(sp);
        text.setTextColor(color);
        text.setGravity(Gravity.START);
        text.setLineSpacing(dp(2), 1f);
        text.setPadding(0, dp(3), 0, dp(3));
        return text;
    }
    private void bold(LinearLayout parent, String value, int sp, int color) {
        TextView view = label(value, sp, color);
        view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        parent.addView(view);
    }
    private void copyIfChanged(TextView view, String content) {
        if (view != null && !content.contentEquals(view.getText())) view.setText(content);
    }
    private LinearLayout vertical() {
        LinearLayout layout = new LinearLayout(activity);
        layout.setOrientation(LinearLayout.VERTICAL);
        return layout;
    }
    private LinearLayout section(LinearLayout body, String heading, String explanation) {
        LinearLayout content = vertical();
        content.setBackground(shape(CARD, 14, BORDER));
        content.setPadding(dp(14), dp(12), dp(14), dp(12));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = dp(12);
        body.addView(content, params);
        bold(content, heading, 16, FG);
        if (!explanation.isEmpty()) content.addView(label(explanation, 14, SECONDARY));
        return content;
    }
    private void hint(LinearLayout parent, String explanation) {
        parent.addView(label(explanation, 13, MUTED));
    }
    private Button action(LinearLayout parent, String title, int fill, int fg, Runnable callback) {
        Button button = new Button(activity);
        button.setText(title);
        button.setAllCaps(false);
        button.setTextSize(14);
        button.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        button.setTextColor(fg);
        button.setGravity(Gravity.CENTER);
        button.setMinHeight(dp(50));
        button.setPadding(dp(10), dp(10), dp(10), dp(10));
        button.setBackground(clickableBackground(fill, BORDER));
        button.setOnClickListener(view -> callback.run());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = dp(8);
        parent.addView(button, params);
        return button;
    }
    private Button neutral(LinearLayout parent, String title, Runnable callback) {
        return action(parent, title, SECONDARY_BG, FG, callback);
    }
    private Button mainAction(LinearLayout parent, String title, Runnable callback) {
        return action(parent, title, PRIMARY_BG, FG, callback);
    }
    private Button stopAction(LinearLayout parent, String title, Runnable callback) {
        return action(parent, title, DANGER_BG, FG, callback);
    }

    private void attach() {
        chip = label("IA", 14, FG);
        chip.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        chip.setGravity(Gravity.CENTER);
        chip.setBackground(clickableBackground(BG, ACCENT));
        chip.setElevation(dp(6));
        chip.setContentDescription("InstaActions: tap to open guided controls, drag to move");
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(dp(46), dp(46),
                Gravity.TOP | Gravity.END);
        lp.topMargin = dp(savedTopDp);
        lp.rightMargin = dp(12);
        decor.addView(chip, lp);
        decor.post(this::positionChip);
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
                            int max = Math.max(dp(24), decor.getHeight() - dp(58));
                            p.topMargin = Math.max(dp(12), Math.min(max,
                                    margin0 + (int) (e.getRawY() - y0)));
                            chip.setLayoutParams(p);
                        }
                        return true;
                    case MotionEvent.ACTION_UP:
                        if (!dragged) chip.performClick();
                        else {
                            savedTopDp = Math.round(((FrameLayout.LayoutParams) chip.getLayoutParams()).topMargin
                                    / activity.getResources().getDisplayMetrics().density);
                            positionChip();
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
                if (CAPTURE.isActiveWindow()) {
                    R1CD06ViewPreflight.Snapshot snapshot = R1CD06ViewPreflight.inspect(activity);
                    if (CAPTURE.needsPreflight())
                        CAPTURE.notePreflight(snapshot, SystemClock.elapsedRealtime());
                }
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
    private void open() {
        if (detached || panel != null) return;
        ScrollView scroll = new ScrollView(activity);
        scroll.setFillViewport(false);
        scroll.setVerticalScrollBarEnabled(true);
        scroll.setScrollbarFadingEnabled(false);
        scroll.setClipToPadding(false);
        scroll.setBackground(shape(BG, 16, BORDER));
        scroll.setElevation(dp(9));
        LinearLayout body = vertical();
        body.setPadding(dp(12), dp(12), dp(12), dp(18));
        scroll.addView(body);

        bold(body, "InstaActions", 20, FG);
        body.addView(label("Repost recovery · D07 experimental", 14, ACCENT));
        hint(body, "Dedicated Reels tab only. This version has NOT passed physical D07C acceptance.");
        neutral(body, "Close panel · return to Reels", this::close);

        LinearLayout stateCard = section(body, "CURRENT STATUS", "Recovery changes only one verified native eligibility result. It never presses Repost for you.");
        recoveryStatus = label("", 15, SUCCESS);
        recoveryStatus.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        stateCard.addView(recoveryStatus);
        nextStep = label("", 14, SECONDARY);
        stateCard.addView(nextStep);

        LinearLayout prep = section(body, "1 · Prepare your Reels tab", "Open the Reels tab from Instagram's bottom navigation on the affected account (manual A). Feed Reels, regular posts and profile Reels are NOT supported.");
        hint(prep, "InstaEclipse stays ON. InstaTaskManager's Repost recovery must be OFF before enabling this one.");
        guardStatus = label("", 14, WARNING);
        prep.addView(guardStatus);
        neutral(prep, "Confirm ITM Repost recovery is OFF", () -> {
            itmRecoveryOffAcknowledged=true;
            toast("Manual confirmation noted; cannot verify another module");refresh();
        });

        LinearLayout evidence = section(body, "2 · Collect native evidence", "Tap Start A, close IA and browse multiple Reels within the dedicated tab. Reopen IA after a few seconds. Keep A recording until recovery is enabled.");
        mainAction(evidence, "Start A · observe Repost (no changes)", () -> {
            R1CD07GateTrace.stopExactTrial();
            itmRecoveryOffAcknowledged=false;
            toast(R1CD07GateTrace.begin(0));refresh();
        });
        captureStatus = label("", 14, SECONDARY);
        evidence.addView(captureStatus);
        hint(evidence, "Account A/B are labels you choose; InstaActions does not identify accounts. If observations aren't ready, continue browsing. Do not press Stop A/B before arming.");
        neutral(evidence, "Optional · Start B read-only comparison", () -> {
            R1CD07GateTrace.stopExactTrial();
            itmRecoveryOffAcknowledged=false;
            toast(R1CD07GateTrace.begin(1));refresh();
        });
        hint(evidence, "B is diagnostic only; to try recovery afterward, start A again and collect fresh A evidence.");
        stopAction(evidence, "Stop A/B observation · recovery OFF", () -> {
            itmRecoveryOffAcknowledged=false;
            toast(R1CD07GateTrace.stop());refresh();
        });

        LinearLayout recovery = section(body, "3 · Try the original Repost button", "When the status says evidence is ready, confirm ITM recovery is OFF, then enable. Close IA, use Instagram's normal Repost button yourself and return here.");
        mainAction(recovery, "Enable native Repost · Reels TAB (90 s)", () -> {
            if (!itmRecoveryOffAcknowledged) {
                toast("REFUSED: first confirm ITM Repost recovery OFF");return;
            }
            if (CAPTURE.isActiveWindow()) {
                toast("REFUSED: stop the advanced D05 capture first");return;
            }
            // REELS is navigation evidence, NOT proof of Reels-tab origin.
            if (R1BContextMonitor.stableScreen() != R1BNavigationPolicy.Screen.REELS) {
                toast("REFUSED: anchored Reels viewer not detected");return;
            }
            toast(R1CD07GateTrace.armHF13Recovery());refresh();
        });
        hint(recovery, "Manually expires after 90 seconds or 64 eligible changes. It must be OFF before switching accounts or tabs; same-Activity switches are not reliably detected.");

        LinearLayout finish = section(body, "4 · Finish safely and export", "Press OFF even if the native Repost button did not appear. A screenshot or a visible button alone does NOT qualify D07C; check the actual user-initiated action.");
        stopAction(finish, "Recovery OFF · restore native results", () -> {
            toast(R1CD07GateTrace.stopExactTrial());
            itmRecoveryOffAcknowledged=false;refresh();
        });
        mainAction(finish, "Copy D07 safe Repost report", this::copyD07);
        hint(finish, "The D07C acceptance runner also asks what you saw on the device. Reports contain aggregated gates, not account or media IDs.");

        LinearLayout diagnostic = section(body, "Advanced · D05/D06 media diagnostics", "Optional and separate from Repost. These experiments do NOT prove the visible media identity and do not enable other actions.");
        advancedToggle = neutral(diagnostic, "Show D05/D06 diagnostic tools", this::toggleAdvanced);
        advancedBody = vertical();
        advancedBody.setVisibility(View.GONE);
        diagnostic.addView(advancedBody);
        advancedStatus = label("", 13, SECONDARY);
        advancedBody.addView(advancedStatus);
        hint(advancedBody, "A1: menu on Reel A → Next. A2: same Reel → Next. B1: another Reel → Next. B2: same Reel B → Finish. Each stage lasts up to 60 seconds.");
        primary = mainAction(advancedBody, "Start A1", this::primaryAction);
        neutral(advancedBody, "Cancel capture", () -> {
            CAPTURE.cancel(false);
            R1CD06PreMenuReconciliation.clear();refresh();
        });
        neutral(advancedBody, "Copy safe report", this::copy);
        neutral(advancedBody, "Share safe report", this::share);
        hint(advancedBody, "D06_MEDIA_IDENTITY=NOT_VERIFIED · ACTIONS=OFF. Keep InstaEclipse enabled for this diagnostic.");

        neutral(body, "Close panel", this::close);
        panel = scroll;
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(0, 0,
                Gravity.TOP | Gravity.END);
        decor.addView(panel, lp);
        positionPanel();
        refresh();
    }

    private void toggleAdvanced() {
        advancedVisible=!advancedVisible;
        if (advancedBody != null) advancedBody.setVisibility(advancedVisible ? View.VISIBLE : View.GONE);
        if (advancedToggle != null) advancedToggle.setText(advancedVisible
                ? "Hide D05/D06 diagnostic tools" : "Show D05/D06 diagnostic tools");
        refresh();
    }

    private int[] safePadding() {
        int top=dp(10), right=dp(10), bottom=dp(10);
        // Android 16 edge-to-edge and landscape cutouts can put a panel under
        // the status/nav bars; derive margins from real insets when present.
        if (Build.VERSION.SDK_INT >= 30 && decor.getRootWindowInsets() != null) {
            android.graphics.Insets bars = decor.getRootWindowInsets()
                    .getInsets(WindowInsets.Type.systemBars());
            top = Math.max(top, bars.top + dp(6));
            right = Math.max(right, bars.right + dp(6));
            bottom = Math.max(bottom, bars.bottom + dp(6));
        }
        return new int[]{top,right,bottom};
    }
    private void positionChip() {
        if (chip == null || decor.getHeight() <= 0) return;
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) chip.getLayoutParams();
        int[] margins = safePadding();
        int max = Math.max(margins[0], decor.getHeight() - margins[2] - dp(52));
        int bounded = Math.max(margins[0], Math.min(max, lp.topMargin));
        if (bounded != lp.topMargin) {
            lp.topMargin = bounded;
            chip.setLayoutParams(lp);
            savedTopDp = Math.round(bounded / activity.getResources().getDisplayMetrics().density);
        }
    }
    private void positionPanel() {
        if (panel == null) return;
        // Portrait / landscape: scrollable, inset-safe, with no 400dp cutoff.
        int width = decor.getWidth(), height = decor.getHeight();
        if (width <= 0 || height <= 0) {
            decor.post(this::positionPanel);
            return;
        }
        int[] margins = safePadding();
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) panel.getLayoutParams();
        lp.width = Math.min(dp(432), Math.max(1, width - margins[1] - dp(10)));
        lp.height = Math.max(1, height - margins[0] - margins[2]);
        lp.topMargin = margins[0];
        lp.rightMargin = margins[1];
        panel.setLayoutParams(lp);
    }

    private void close() {
        if (panel != null) {
            decor.removeView(panel);
            panel=null; recoveryStatus=null; nextStep=null; captureStatus=null;
            guardStatus=null; advancedStatus=null; advancedBody=null;
            advancedToggle=null; primary=null; advancedVisible=false;
        }
    }
    private void toast(String text) {
        Toast.makeText(activity.getApplicationContext(), text, Toast.LENGTH_LONG).show();
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
                toast("D05 hook not ready, or capture limit reached. Restart Instagram if needed.");
                return;
            }
        } else {
            CAPTURE.advance(SystemClock.elapsedRealtime());
            R1CD06PreMenuReconciliation.clear();
        }
        refresh();
        if (CAPTURE.step() != R1CD05FabSession.Step.DONE) {
            toast("D05 " + CAPTURE.step() + ": use the Reel menu, then tap IA for the next stage");
            close();
        } else toast("D05 capture finished. Copy or share the safe report.");
    }
    private void refresh() {
        if (chip == null) return;
        R1CD05FabSession.Step stage = CAPTURE.step();
        boolean d05 = stage == R1CD05FabSession.Step.A1 || stage == R1CD05FabSession.Step.A2
                || stage == R1CD05FabSession.Step.B1 || stage == R1CD05FabSession.Step.B2;
        boolean on = R1CD07Recovery.isEnabled();
        boolean observingA = R1CD07GateTrace.recording() && R1CD07GateTrace.activeIndex()==0;
        boolean observingB = R1CD07GateTrace.recording() && R1CD07GateTrace.activeIndex()==1;
        String chipText = on ? "ON" : d05 ? stage.name() : observingA ? "A" : observingB ? "B" : "IA";
        copyIfChanged(chip, chipText);
        chip.setTextColor(on ? SUCCESS : d05 || observingA || observingB ? ACCENT : FG);
        chip.setContentDescription(on ? "InstaActions native Repost recovery ON, tap to open OFF control"
                : d05 ? "InstaActions D05 capturing " + stage.name()
                : "InstaActions guided controls, recovery OFF");

        if (recoveryStatus != null) {
            copyIfChanged(recoveryStatus, R1CD07UxGuide.shortState(on, observingA, observingB)
                    + "\n" + R1CD07Recovery.status());
            recoveryStatus.setTextColor(on ? SUCCESS : observingA || observingB ? ACCENT : WARNING);
        }
        if (nextStep != null) {
            copyIfChanged(nextStep, "NEXT: " + R1CD07UxGuide.next(on,
                    R1CD07GateTrace.ready(), InstaActionsEntry.dataGateReady(),
                    R1BContextMonitor.stableScreen()==R1BNavigationPolicy.Screen.REELS,
                    observingA, R1CD07ExactGateTrial.eligibleForHF13Recovery(),
                    itmRecoveryOffAcknowledged));
        }
        if (guardStatus != null) copyIfChanged(guardStatus,
                "ITM recovery OFF: " + (itmRecoveryOffAcknowledged
                ? "You confirmed for this session (not verified by IA)"
                : "NOT CONFIRMED · recovery remains guarded"));
        if (captureStatus != null) copyIfChanged(captureStatus,
                R1CD07GateTrace.feedback() + "\nNative data hook: "
                + InstaActionsEntry.dataGateStatus());
        if (advancedStatus != null && advancedVisible) copyIfChanged(advancedStatus,
                "D05 stage: " + stage + " · Reels callbacks: " + CAPTURE.currentCount()
                + " · total: " + CAPTURE.total() + "\n" + CAPTURE.prompt()
                + "\nD04 hook: " + (R1CD04PassiveProbe.isInstalled() ? "installed" : "unverified")
                + "\nD06 owner: " + CAPTURE.currentOwnership()
                + "\nD06 page: " + CAPTURE.currentVisiblePage()
                + "\nPre-menu: " + CAPTURE.currentPreMenu()
                + "\nViewer: " + CAPTURE.currentPreflight()
                + "\nInstaEclipse: " + R1CD06UpstreamConformance.summary());
        if (primary != null) {
            copyIfChanged(primary, !d05 ? "Start A1" : stage == R1CD05FabSession.Step.B2
                    ? "Finish capture" : "Next D05 stage");
            primary.setEnabled(d05 || R1CD04PassiveProbe.hasCaptureCapacity());
        }
    }
    private void copy() {
        ClipboardManager manager = (ClipboardManager)
                activity.getSystemService(Context.CLIPBOARD_SERVICE);
        if (manager == null) {toast("Clipboard unavailable");return;}
        manager.setPrimaryClip(ClipData.newPlainText("INA D05 safe diagnostic report", CAPTURE.report()));
        toast("Content-free D05 report copied");
    }
    private void share() {
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_TEXT, CAPTURE.report());
        try { activity.startActivity(Intent.createChooser(send, "Share D05 diagnostic report")); }
        catch (RuntimeException unavailable) {toast("No share target available");}
    }
    private void detach() {
        R1CD07Recovery.onActivityExit();
        itmRecoveryOffAcknowledged=false;
        detached=true;
        if(ticker!=null) MAIN.removeCallbacks(ticker);
        CAPTURE.cancel(true);
        R1CD06PreMenuReconciliation.clear();
        close();
        if(chip!=null) {decor.removeView(chip);chip=null;}
    }
}
