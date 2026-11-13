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
 * D07 UX02. Ordered, high-contrast user guide for existing native
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
    private TextView readinessChecklist;
    private TextView captureStatus;
    private TextView guardStatus;
    private TextView advancedStatus;
    private TextView abStatus;
    private LinearLayout advancedBody;
    private Button advancedToggle;
    private Button primary;
    private Button enableRecovery;
    private Button startA;
    private Button stopA;
    private Button startB;
    private Button stopB;
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
        body.addView(label("D07 · Repost recovery · guided trial", 14, ACCENT));
        hint(body, "EXPERIMENTAL · D07C device qualification NOT RUN. Dedicated Reels tab only — select it from Instagram's bottom bar.");
        hint(body, "DEVICE OBSERVATION: user reported original Repost button visible for 90 seconds and an actual successful Repost in the dedicated Reels tab. Formal D07C evidence review remains pending.");

        LinearLayout stateCard = section(body, "CURRENT STATUS · NEXT STEP", "Follow Steps 1 through 5 in order. Only the original Instagram Repost button performs a Repost.");
        recoveryStatus = label("", 15, SUCCESS);
        recoveryStatus.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        stateCard.addView(recoveryStatus);
        nextStep = label("", 14, FG);
        nextStep.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        stateCard.addView(nextStep);
        readinessChecklist = label("", 13, SECONDARY);
        stateCard.addView(readinessChecklist);
        stopAction(stateCard, "Recovery OFF now · emergency stop", () -> {
            toast(R1CD07GateTrace.stopExactTrial());
            itmRecoveryOffAcknowledged=false;refresh();
        });

        LinearLayout prep = section(body, "STEP 1 OF 5 · PREPARE", "Before touching Start A, check your setup:");
        hint(prep, "1. InstaActions enabled for Instagram in Vector/LSPosed; InstaEclipse stays ON. Restart Instagram after changing scope.");
        hint(prep, "2. Use the affected account. Make sure InstaTaskManager's separate Repost recovery is OFF.");
        hint(prep, "3. Open the dedicated Reels tab using Instagram's bottom bar. Feed, profile Reels and ordinary posts are not qualified.");
        hint(prep, "4. Check CURRENT STATUS above. If exact Instagram 455 hooks are not ready, do not proceed.");
        hint(prep, "Do NOT press the ITM confirmation button yet: Step 2 clears earlier confirmations.");

        LinearLayout startCard = section(body, "STEP 2 OF 5 · START A", "Tap the button ONCE while on the affected account. A is a manual label, not an account-ID check.");
        startA = mainAction(startCard, "Start A · read-only observation", () -> {
            R1CD07GateTrace.stopExactTrial();
            itmRecoveryOffAcknowledged=false;
            toast(R1CD07GateTrace.begin(0));refresh();
        });
        hint(startCard, "For Repost recovery, continue to Step 3 and KEEP A RECORDING. For a separate A/B comparison, use the visible comparison section after Step 5.");

        LinearLayout browse = section(body, "STEP 3 OF 5 · BROWSE, THEN RETURN", "1. Close this IA panel. 2. Swipe through several Reels IN the dedicated Reels tab. 3. Tap IA to reopen it.");
        neutral(browse, "Close IA · browse Reels now", this::close);
        hint(browse, "Wait for fresh A evidence to become ready. If not ready, close IA and browse more Reels, then return. KEEP A RECORDING; do not Stop A/B.");
        captureStatus = label("", 14, SECONDARY);
        browse.addView(captureStatus);

        LinearLayout recovery = section(body, "STEP 4 OF 5 · CONFIRM, THEN ENABLE", "Only after fresh A evidence is ready: first confirm the OTHER module is OFF, then enable THIS module.");
        guardStatus = label("", 14, WARNING);
        recovery.addView(guardStatus);
        neutral(recovery, "4A · Confirm ITM Repost recovery is OFF", () -> {
            itmRecoveryOffAcknowledged=true;
            toast("Manual confirmation noted; cannot verify another module");refresh();
        });
        enableRecovery = mainAction(recovery, "4B · Enable native Repost · 90 s max", () -> {
            if (!itmRecoveryOffAcknowledged) {
                toast("REFUSED: do step 4A after Start A, then step 4B");return;
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
        hint(recovery, "If 4B is unavailable, read CURRENT STATUS and finish Step 3 first. This changes at most 64 eligible results or lasts 90 seconds; it does NOT tap Repost.");

        LinearLayout finish = section(body, "STEP 5 OF 5 · TEST, OFF, EXPORT", "1. Close IA. 2. Tap Instagram's ORIGINAL Repost button on a Reel in the dedicated tab yourself. 3. Reopen IA immediately.");
        neutral(finish, "Close IA · try the original Repost", this::close);
        hint(finish, "4. Press Recovery OFF below BEFORE switching account or tab. 5. Copy the safe report. 6. Confirm the real repost separately in D07C; a button alone is not proof.");
        stopAction(finish, "5A · Recovery OFF · restore native results", () -> {
            toast(R1CD07GateTrace.stopExactTrial());
            itmRecoveryOffAcknowledged=false;refresh();
        });
        mainAction(finish, "5B · Copy D07 safe Repost report", this::copyD07);
        hint(finish, "If no native button appears, still switch OFF and export the report. Before any account/tab change, OFF is mandatory; same-Activity switches are not reliably detected.");

        // Independent diagnostic workflow, deliberately placed AFTER the five-step
        // native recovery path. This is not hidden behind D05/D06 Advanced.
        LinearLayout compare = section(body, "OPTIONAL · COMPARE ACCOUNT A → ACCOUNT B",
                "Use this only for read-only A/B investigation. Recovery must be OFF before changing accounts. This is NOT required for the 90-second Repost trial.");
        abStatus = label("", 14, ACCENT);
        compare.addView(abStatus);
        hint(compare, "A1. Start A above; close IA and browse Reels on manually selected account A.");
        stopA = neutral(compare, "A2 · Stop A · KEEP observations", () -> {
            if (!R1CD07ComparisonFlow.canStopA(R1CD07GateTrace.recording(),
                    R1CD07GateTrace.activeIndex(), R1CD07Recovery.isEnabled())) {
                toast("REFUSED: first Start A, browse Reels, then stop A with recovery OFF");return;
            }
            itmRecoveryOffAcknowledged=false;
            toast(R1CD07GateTrace.stop() + "; A samples preserved");refresh();
        });
        hint(compare, "B1. Once A is STOPPED, switch Instagram to account B manually. Keep the same Instagram process if possible. A process restart loses both captures.");
        startB = mainAction(compare, "Optional · Start B read-only comparison", () -> {
            if (!R1CD07ComparisonFlow.canStartB(R1CD07GateTrace.recording(),
                    R1CD07GateTrace.activeIndex(), R1CD07GateTrace.cohortEvents(0),
                    R1CD07Recovery.isEnabled(), R1CD07GateTrace.ready())) {
                toast("REFUSED: Stop A after observing it, switch manually to B, then Start B; recovery must be OFF");return;
            }
            R1CD07GateTrace.stopExactTrial();
            itmRecoveryOffAcknowledged=false;
            toast(R1CD07GateTrace.begin(1));refresh();
        });
        hint(compare, "B2. Browse several Reels on B, reopen IA, then press Stop B. Do not turn Recovery ON during B.");
        stopB = neutral(compare, "B3 · Stop B · KEEP both samples", () -> {
            if (!R1CD07ComparisonFlow.canStopB(R1CD07GateTrace.recording(),
                    R1CD07GateTrace.activeIndex(), R1CD07Recovery.isEnabled())) {
                toast("REFUSED: Start B before Stop B; recovery must be OFF");return;
            }
            itmRecoveryOffAcknowledged=false;
            toast(R1CD07GateTrace.stop() + "; A and B samples preserved");refresh();
        });
        mainAction(compare, "B4 · Copy combined A/B report", this::copyD07);
        hint(compare, "The same report contains A and B original native-result counts. A/B labels are selected by YOU, not account identity verification. Never reset or restart Instagram before copying if you need both samples.");

        LinearLayout feed = section(body, "D08A · FEED REPOST READ-ONLY AUDIT",
                "These are separate manual snapshots, NOT working Feed Repost. Never run while recovery is ON. Open the target post before taking a snapshot.");
        hint(feed, "1. Go to the Home Feed and open a normal post. Tap the first button. 2. Open a Reel FROM Feed and tap the second. 3. Visit the BOTTOM Reels tab and tap the reference button. Copy D07 safe report afterward.");
        neutral(feed, "D08A 1 · Snapshot ordinary Feed post", () ->
                toast(R1CD08FeedAudit.snapshot(activity, panel, chip, 0)));
        neutral(feed, "D08A 2 · Snapshot Feed-opened Reel", () ->
                toast(R1CD08FeedAudit.snapshot(activity, panel, chip, 1)));
        neutral(feed, "D08A 3 · Snapshot dedicated Reels tab", () ->
                toast(R1CD08FeedAudit.snapshot(activity, panel, chip, 2)));
        hint(feed, "Only exact visible Repost-label counts are retained. A feed Reel versus Reels tab is YOUR claim, not verified media identity. No Feed override, no auto-Repost.");
        LinearLayout media = section(body, "D08C · VISIBLE MEDIA IDENTITY",
                "Still unverified. D05/D06 menu research produces read-only candidate/contradiction counts, never authorizes actions.");
        hint(media, "To investigate: open Advanced D05/D06 tools below, run the existing A1/A2/B1/B2 menu capture, then copy the D07 safe report. The D08C evidence summary is included automatically.");

        LinearLayout diagnostic = section(body, "OPTIONAL · B comparison, reset and D05/D06", "Only open this after the normal 5-step trial, or when intentionally restarting. None of these buttons is required to try Repost.");
        advancedToggle = neutral(diagnostic, "Show D05/D06 diagnostic tools", this::toggleAdvanced);
        advancedBody = vertical();
        advancedBody.setVisibility(View.GONE);
        diagnostic.addView(advancedBody);
        hint(advancedBody, "The A → B comparison controls are visible above. The RESET control below permanently clears BOTH captured label cohorts; Stop A and Stop B preserve the counters.");
        stopAction(advancedBody, "Reset A/B observations · recovery OFF", () -> {
            itmRecoveryOffAcknowledged=false;
            toast(R1CD07GateTrace.clear());refresh();
        });
        hint(advancedBody, "D05/D06 media diagnostics are a separate research workflow; they do not prove media identity or enable actions.");
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
            panel=null; recoveryStatus=null; nextStep=null; readinessChecklist=null;
            enableRecovery=null; startA=null; stopA=null; startB=null; stopB=null;
            abStatus=null; captureStatus=null;
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
            R1CD08MediaEvidence.clear();
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
            copyIfChanged(nextStep, R1CD07UxGuide.next(on,
                    R1CD07GateTrace.ready(), InstaActionsEntry.dataGateReady(),
                    R1BContextMonitor.stableScreen()==R1BNavigationPolicy.Screen.REELS,
                    observingA, R1CD07ExactGateTrial.eligibleForHF13Recovery(),
                    itmRecoveryOffAcknowledged));
        }
        if (readinessChecklist != null) copyIfChanged(readinessChecklist,
                R1CD07UxGuide.checklist(R1CD07GateTrace.ready(),
                        InstaActionsEntry.dataGateReady(),
                        R1BContextMonitor.stableScreen()==R1BNavigationPolicy.Screen.REELS,
                        observingA, R1CD07ExactGateTrial.eligibleForHF13Recovery(),
                        itmRecoveryOffAcknowledged));
        if (enableRecovery != null) enableRecovery.setEnabled(!on
                && R1CD07GateTrace.ready() && InstaActionsEntry.dataGateReady()
                && R1BContextMonitor.stableScreen()==R1BNavigationPolicy.Screen.REELS
                && observingA && R1CD07ExactGateTrial.eligibleForHF13Recovery()
                && itmRecoveryOffAcknowledged && !CAPTURE.isActiveWindow());
        if (startA != null) startA.setEnabled(!on && !observingB && R1CD07GateTrace.ready());
        if (stopA != null) stopA.setEnabled(R1CD07ComparisonFlow.canStopA(
                R1CD07GateTrace.recording(), R1CD07GateTrace.activeIndex(), on));
        if (startB != null) startB.setEnabled(R1CD07ComparisonFlow.canStartB(
                R1CD07GateTrace.recording(), R1CD07GateTrace.activeIndex(),
                R1CD07GateTrace.cohortEvents(0), on, R1CD07GateTrace.ready()));
        if (stopB != null) stopB.setEnabled(R1CD07ComparisonFlow.canStopB(
                R1CD07GateTrace.recording(), R1CD07GateTrace.activeIndex(), on));
        if (abStatus != null) copyIfChanged(abStatus,
                R1CD07ComparisonFlow.progress(R1CD07GateTrace.recording(),
                        R1CD07GateTrace.activeIndex(), R1CD07GateTrace.cohortEvents(0),
                        R1CD07GateTrace.cohortEvents(1), on));
        if (guardStatus != null) copyIfChanged(guardStatus,
                "ITM recovery OFF: " + (itmRecoveryOffAcknowledged
                ? "You confirmed for this session (not verified by IA)"
                : "NOT CONFIRMED · recovery remains guarded"));
        if (captureStatus != null) copyIfChanged(captureStatus,
                (observingA ? "A IS RECORDING · " : observingB ? "B is recording (optional) · " : "A not recording · ")
                + (R1CD07ExactGateTrial.eligibleForHF13Recovery()
                    ? "fresh A evidence ready" : "fresh A evidence not ready")
                + "\nNative data hook: " + InstaActionsEntry.dataGateStatus());
        if (advancedStatus != null && advancedVisible) copyIfChanged(advancedStatus,
                "D07 native trace: " + R1CD07GateTrace.feedback()
                + "\nD05 stage: " + stage + " · Reels callbacks: " + CAPTURE.currentCount()
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
