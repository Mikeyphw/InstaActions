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
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.Switch;
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
    private Button compareStartA;
    private Spinner modePicker;
    private Spinner cohortPicker;
    private TextView modeStatus;
    private Switch keepRepost;
    private boolean settingPicker;
    private R1CD09DiagnosticGate.Mode selectedMode=R1CD09DiagnosticGate.Mode.REELS;
    private LinearLayout reelsGroup, compareGroup, feedGroup, mediaGroup, accountGroup, avatarGroup, passiveGroup, autoGroup, comparisonStepGroup;
    private TextView autoStatus;
    private TextView comparisonStatus;
    private final String[] modeNames={
        "Reels Repost · recovery", "Account A/B · native comparison",
        "Feed Repost · visible labels", "Visible media · D05/D06",
        "Account change · passive signal", "Friend avatars · read-only A/B", "Passive overview · auto ON",
        "AUTO discovery · Feed / Reels / Story / account A→B"
    };
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
        R1CD10PassiveLedger.observe(R1CD10PassiveLedger.Signal.ACTIVITY_RESUME);
        next.attach();
    }

    static void onLeaving(Activity activity) {
        if (current != null && current.activity == activity) {
            R1CD10PassiveLedger.observe(R1CD10PassiveLedger.Signal.ACTIVITY_LEAVE);
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

    private ArrayAdapter<String> highContrastChoices(String[] choices) {
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(activity,
                android.R.layout.simple_spinner_item,choices) {
            @Override public View getView(int position,View convertView,ViewGroup parent) {
                View view=super.getView(position,convertView,parent);
                if(view instanceof TextView) {
                    ((TextView)view).setTextColor(FG);
                    ((TextView)view).setTextSize(15);
                    view.setBackgroundColor(CARD);view.setPadding(dp(12),dp(12),dp(12),dp(12));
                }
                return view;
            }
            @Override public View getDropDownView(int position,View convertView,ViewGroup parent) {
                View view=super.getDropDownView(position,convertView,parent);
                if(view instanceof TextView) {
                    ((TextView)view).setTextColor(FG);
                    ((TextView)view).setTextSize(15);
                    view.setBackgroundColor(SECONDARY_BG);
                    view.setPadding(dp(12),dp(14),dp(12),dp(14));
                }
                return view;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapter;
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
                R1CD10PassiveLedger.observeScreen(R1BContextMonitor.stableScreen().name());
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
        hint(body, "Dedicated Reels tab only for Repost recovery. Reels-tab Repost is user-confirmed working. Feed/avatars/media identity remain UNQUALIFIED. Choose one diagnostic below, then Begin.");
        hint(body, "RECOVERY: user-confirmed native Reels-tab Repost for 90 seconds; formal D07C signoff still pending. New foreground-session toggle is NOT qualified and is never saved across activities.");

        LinearLayout select = section(body, "CHOOSE ONE DIAGNOSTIC", "Select a mode below. Only one can run at once. Finish preserves its results; Abort stops it. You must end the current diagnostic before switching.");
        modePicker = new Spinner(activity);
        ArrayAdapter<String> modes=highContrastChoices(modeNames);
        modePicker.setAdapter(modes);
        select.addView(modePicker);
        modeStatus=label("",14,ACCENT);select.addView(modeStatus);
        mainAction(select,"Begin selected diagnostic",this::beginSelectedMode);
        neutral(select,"Finish current diagnostic · preserve report",this::finishSelectedMode);
        stopAction(select,"Abort current diagnostic · Recovery OFF",this::abortSelectedMode);
        mainAction(select,"Copy this diagnostic's report",this::copySelectedReport);
        mainAction(select,"Copy ALL diagnostics since Instagram opened",this::copyAllDiagnostics);
        neutral(select,"Export ALL diagnostics · Android share sheet",this::exportAllDiagnostics);
        hint(select,"PASSIVE IS AUTO ON: read-only screen discovery and native session/lifecycle signals collect without Begin. AUTO mode adds controlled A→B evidence. Copy/Export All includes completed/aborted sessions and the bounded passive timeline; only the current Instagram process.");
        stopAction(select,"Recovery OFF · always available",()->{
            toast(R1CD07GateTrace.stopExactTrial());itmRecoveryOffAcknowledged=false;refresh();
        });
        modePicker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override public void onItemSelected(AdapterView<?> parent,View view,int position,long id){
                if(settingPicker)return;
                if(R1CD09DiagnosticGate.running() && position!=selectedMode.ordinal()){
                    settingPicker=true;modePicker.setSelection(selectedMode.ordinal());settingPicker=false;
                    toast("REFUSED: Finish or Abort the current diagnostic before changing modes");return;
                }
                selectedMode=R1CD09DiagnosticGate.Mode.values()[position];refresh();
            }
            @Override public void onNothingSelected(AdapterView<?> parent){}
        });
        R1CD09DiagnosticGate.Mode remembered=R1CD09DiagnosticGate.selected();
        if(R1CD09DiagnosticGate.running() && remembered!=null)selectedMode=remembered;
        settingPicker=true;modePicker.setSelection(selectedMode.ordinal());settingPicker=false;

        // Universal A/B controls go ABOVE the selected feature, never below
        // its snapshots. In REELS/AB the original feature-specific controls remain.
        comparisonStepGroup=vertical();body.addView(comparisonStepGroup);
        reelsGroup=vertical();body.addView(reelsGroup);
        compareGroup=vertical();body.addView(compareGroup);
        feedGroup=vertical();body.addView(feedGroup);
        mediaGroup=vertical();body.addView(mediaGroup);
        accountGroup=vertical();body.addView(accountGroup);
        avatarGroup=vertical();body.addView(avatarGroup);
        passiveGroup=vertical();body.addView(passiveGroup);
        autoGroup=vertical();body.addView(autoGroup);
        LinearLayout compareStepper=section(comparisonStepGroup,"A → B · SAME ORDER FOR EVERY DIAGNOSTIC",
                "A1 Start A → A2 observe feature → A3 Stop A → switch account manually with recovery OFF → confirm → B1 Start B → B2 observe the SAME feature → B3 Stop B → Finish. Accounts are manual labels, not identities.");
        comparisonStatus=label("",14,ACCENT);compareStepper.addView(comparisonStatus);
        mainAction(compareStepper,"A1 · Start A (manual affected)",this::runStartA);
        neutral(compareStepper,"A3 · Stop A · preserve result",this::runStopA);
        neutral(compareStepper,"Confirm account switched to B (manual)",this::runConfirmSwitch);
        mainAction(compareStepper,"B1 · Start B (manual unaffected)",this::runStartB);
        neutral(compareStepper,"B3 · Stop B · preserve both",this::runStopB);
        hint(compareStepper,"For native Repost, Start A is Step 2 inside Reels mode; its B handoff is shown after Step 5. In A/B mode the existing buttons below use the same safety sequence. Passive monitoring never requires Begin.");

        LinearLayout stateCard = section(reelsGroup, "CURRENT STATUS · NEXT STEP", "Follow the instructions for the SELECTED MODE only. No automated Repost, likes or comments.");
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

        LinearLayout prep = section(reelsGroup, "STEP 1 OF 5 · PREPARE", "Before touching Start A, check your setup:");
        hint(prep, "1. InstaActions enabled for Instagram in Vector/LSPosed; InstaEclipse stays ON. Restart Instagram after changing scope.");
        hint(prep, "2. Use the affected account. Make sure InstaTaskManager's separate Repost recovery is OFF.");
        hint(prep, "3. Open the dedicated Reels tab using Instagram's bottom bar. Feed, profile Reels and ordinary posts are not qualified.");
        hint(prep, "4. Check CURRENT STATUS above. If exact Instagram 455 hooks are not ready, do not proceed.");
        hint(prep, "Do NOT press the ITM confirmation button yet: Step 2 clears earlier confirmations.");

        LinearLayout startCard = section(reelsGroup, "STEP 2 OF 5 · START A", "Tap the button ONCE while on the affected account. A is a manual label, not an account-ID check.");
        startA = mainAction(startCard, "Start A · read-only observation", () -> {
            if(!requireMode(R1CD09DiagnosticGate.Mode.REELS))return;
            runStartA();
        });
        hint(startCard, "Keep A recording for Repost trial. For a separate read-only A/B comparison, Finish/Abort this mode, select A/B in the dropdown, and Begin.");

        LinearLayout browse = section(reelsGroup, "STEP 3 OF 5 · BROWSE, THEN RETURN", "1. Close this IA panel. 2. Swipe through several Reels IN the dedicated Reels tab. 3. Tap IA to reopen it.");
        neutral(browse, "Close IA · browse Reels now", this::close);
        hint(browse, "Wait for fresh A evidence to become ready. If not ready, close IA and browse more Reels, then return. KEEP A RECORDING; do not Stop A/B.");
        captureStatus = label("", 14, SECONDARY);
        browse.addView(captureStatus);

        LinearLayout recovery = section(reelsGroup, "STEP 4 OF 5 · CONFIRM, THEN ENABLE", "Only after fresh A evidence is ready: first confirm the OTHER module is OFF, then enable THIS module.");
        guardStatus = label("", 14, WARNING);
        recovery.addView(guardStatus);
        neutral(recovery, "4A · Confirm ITM Repost recovery is OFF", () -> {
            if(!requireMode(R1CD09DiagnosticGate.Mode.REELS))return;
            itmRecoveryOffAcknowledged=true;
            toast("Manual confirmation noted; cannot verify another module");refresh();
        });
        enableRecovery = mainAction(recovery, "4B · Enable native Repost · 90 s max", () -> {
            if(!requireMode(R1CD09DiagnosticGate.Mode.REELS))return;
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
        keepRepost=new Switch(activity);
        keepRepost.setText("Keep Repost available · foreground session (no 90s timer)");
        keepRepost.setTextColor(FG);
        keepRepost.setTextSize(14);
        keepRepost.setPadding(dp(10),dp(10),dp(10),dp(10));
        recovery.addView(keepRepost);
        keepRepost.setOnCheckedChangeListener(this::toggleSessionRepost);
        hint(recovery,"SESSION TOGGLE: bounded to 4,096 exact calls, while Instagram's Reels Activity remains foreground. It is NOT permanently saved, is NOT account-bound, and must be turned OFF before changing accounts or tabs. If UserSession changes are observed, it disarms automatically; absent observations cannot prove account stability.");
        hint(recovery, "If 4B is unavailable, read CURRENT STATUS and finish Step 3 first. This changes at most 64 eligible results or lasts 90 seconds; it does NOT tap Repost.");

        LinearLayout finish = section(reelsGroup, "STEP 5 OF 5 · TEST, OFF, EXPORT", "1. Close IA. 2. Tap Instagram's ORIGINAL Repost button on a Reel in the dedicated tab yourself. 3. Reopen IA immediately.");
        neutral(finish, "Close IA · try the original Repost", this::close);
        hint(finish, "4. Press Recovery OFF below BEFORE switching account or tab. 5. Copy the safe report. 6. Confirm the real repost separately in D07C; a button alone is not proof.");
        stopAction(finish, "5A · Recovery OFF · restore native results", () -> {
            toast(R1CD07GateTrace.stopExactTrial());
            itmRecoveryOffAcknowledged=false;refresh();
        });
        mainAction(finish, "5B · Copy D07 safe Repost report", this::copySelectedReport);
        hint(finish, "If no native button appears, still switch OFF and export the report. Before any account/tab change, OFF is mandatory; same-Activity switches are not reliably detected.");
        LinearLayout reelsAB=section(reelsGroup,"OPTIONAL B COMPARISON · AFTER REPOST OFF",
                "You already Started A in Step 2. After Step 5, press Stop A, manually switch to B with Repost OFF, confirm, Start B, browse Reels, Stop B, Finish. B is read-only; NEVER arm Repost on B.");
        neutral(reelsAB,"A3 · Stop A (after Repost OFF)",this::runStopA);
        neutral(reelsAB,"Confirm manual account switch to B",this::runConfirmSwitch);
        mainAction(reelsAB,"B1 · Start B · read-only",this::runStartB);
        neutral(reelsAB,"B3 · Stop B · preserve both",this::runStopB);

        // Independent diagnostic workflow, deliberately placed AFTER the five-step
        // native recovery path. This is not hidden behind D05/D06 Advanced.
        LinearLayout compare = section(compareGroup, "OPTIONAL · COMPARE ACCOUNT A → ACCOUNT B",
                "Use this only for read-only A/B investigation. Recovery must be OFF before changing accounts. This is NOT required for the 90-second Repost trial.");
        abStatus = label("", 14, ACCENT);
        compare.addView(abStatus);
        hint(compare, "A1. Press Start A below, browse Reels on the manually selected affected account, then press Stop A. Recovery must stay OFF.");
        compareStartA=mainAction(compare,"A1 · Start A · read-only",()->{
            if(!requireMode(R1CD09DiagnosticGate.Mode.AB))return;
            if(R1CD07GateTrace.recording() || R1CD07GateTrace.cohortEvents(0)>0){
                toast("REFUSED: A already recorded. Finish/Abort before restarting.");return;
            }
            runStartA();
        });
        stopA = neutral(compare, "A2 · Stop A · KEEP observations", () -> {
            if(!requireMode(R1CD09DiagnosticGate.Mode.AB))return;
            if (!R1CD07ComparisonFlow.canStopA(R1CD07GateTrace.recording(),
                    R1CD07GateTrace.activeIndex(), R1CD07Recovery.isEnabled())) {
                toast("REFUSED: first Start A, browse Reels, then stop A with recovery OFF");return;
            }
            runStopA();
        });
        neutral(compare,"A3 · Confirm manual account switch to B",this::runConfirmSwitch);
        hint(compare, "B1. Once A is STOPPED, switch Instagram to account B manually. Keep the same Instagram process if possible. A process restart loses both captures.");
        startB = mainAction(compare, "Optional · Start B read-only comparison", () -> {
            if(!requireMode(R1CD09DiagnosticGate.Mode.AB))return;
            if (!R1CD07ComparisonFlow.canStartB(R1CD07GateTrace.recording(),
                    R1CD07GateTrace.activeIndex(), R1CD07GateTrace.cohortEvents(0),
                    R1CD07Recovery.isEnabled(), R1CD07GateTrace.ready())) {
                toast("REFUSED: Stop A after observing it, switch manually to B, then Start B; recovery must be OFF");return;
            }
            runStartB();
        });
        hint(compare, "B2. Browse several Reels on B, reopen IA, then press Stop B. Do not turn Recovery ON during B.");
        stopB = neutral(compare, "B3 · Stop B · KEEP both samples", () -> {
            if(!requireMode(R1CD09DiagnosticGate.Mode.AB))return;
            if (!R1CD07ComparisonFlow.canStopB(R1CD07GateTrace.recording(),
                    R1CD07GateTrace.activeIndex(), R1CD07Recovery.isEnabled())) {
                toast("REFUSED: Start B before Stop B; recovery must be OFF");return;
            }
            runStopB();
        });
        mainAction(compare, "B4 · Copy combined A/B report", this::copySelectedReport);
        hint(compare,"Optional reset only after Finish/Abort of A/B. Reset permanently discards both cohorts; Stop A/B does not.");
        stopAction(compare, "Reset A/B observations · recovery OFF", () -> {
            if(R1CD09DiagnosticGate.running() ||
                    R1CD09DiagnosticGate.selected()!=R1CD09DiagnosticGate.Mode.AB){
                toast("REFUSED: Finish or Abort A/B first");return;
            }
            itmRecoveryOffAcknowledged=false;
            toast(R1CD07GateTrace.clear());refresh();
        });
        hint(compare, "The same report contains A and B original native-result counts. A/B labels are selected by YOU, not account identity verification. Never reset or restart Instagram before copying if you need both samples.");

        LinearLayout feed = section(feedGroup, "D08A · FEED REPOST READ-ONLY AUDIT",
                "These are separate manual snapshots, NOT working Feed Repost. Never run while recovery is ON. Open the target post before taking a snapshot.");
        hint(feed,"A/B now uses the shared stepper. Begin Feed → Start A → capture Feed surfaces → Stop A → manually switch/confirm → Start B → same surfaces → Stop B → Finish. No implicit affected label.");
        hint(feed, "1. Go to the Home Feed and open a normal post. Tap the first button. 2. Open a Reel FROM Feed and tap the second. 3. Visit the BOTTOM Reels tab and tap the reference button. Copy D07 safe report afterward.");
        neutral(feed, "D08A 1 · Snapshot ordinary Feed post", () ->
                toast(R1CD08FeedAudit.snapshot(activity, panel, chip, 0)));
        neutral(feed, "D08A 2 · Snapshot Feed-opened Reel", () ->
                toast(R1CD08FeedAudit.snapshot(activity, panel, chip, 1)));
        neutral(feed, "D08A 3 · Snapshot dedicated Reels tab", () ->
                toast(R1CD08FeedAudit.snapshot(activity, panel, chip, 2)));
        hint(feed, "EVIDENCE QUALITY: requires one explicit snapshot of EACH surface on A and B. Generic callbacks DO NOT count as Feed evidence. Scan is whole visible screen, NOT post-bound; label absence is not button absence.");
        hint(feed, "Only exact visible Repost-label counts are retained. A feed Reel versus Reels tab is YOUR claim, not verified media identity. No Feed override, no auto-Repost.");
        LinearLayout media = section(mediaGroup, "D08C · VISIBLE MEDIA IDENTITY",
                "Still unverified. D05/D06 menu research produces read-only candidate/contradiction counts, never authorizes actions.");
        hint(media, "Begin Media, Start A manually, then run the four-stage D05 Reel capture INSIDE account A. Those A1/A2/B1/B2 stage names refer to different REELS, not accounts. Stop A, switch and confirm, Start B, repeat the four-stage capture, Stop B, Finish. Missing page identity stays NOT_VERIFIED.");
        hint(media, "To investigate: open Advanced D05/D06 tools below, run the existing A1/A2/B1/B2 menu capture, then copy the D07 safe report. The D08C evidence summary is included automatically.");

        LinearLayout diagnostic = section(mediaGroup, "D05/D06 · ADVANCED MEDIA CAPTURE", "Only D05/D06 source-backed investigation tools. A/B reset and Feed controls belong to their own dropdown modes.");
        advancedToggle = neutral(diagnostic, "Show D05/D06 diagnostic tools", this::toggleAdvanced);
        advancedBody = vertical();
        advancedBody.setVisibility(View.GONE);
        diagnostic.addView(advancedBody);
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

        LinearLayout accounts=section(accountGroup,"ACCOUNT CHANGE · PASSIVE DIAGNOSTIC",
                "Begin this mode, inspect the signal, optionally switch accounts manually, then Finish. Recovery stays OFF. The verified UserSession callback may not execute on every transition.");
        hint(accounts,"Use shared A/B: Start A → Stop A → switch manually → Confirm → Start B → Stop B → Finish. Compare the independent Android Activity lifecycle/new-intent dispatch signal with the existing Repost UserSession reference. Neither source verifies account identity. New Intent disarms Repost; no automatic re-enable.");
        hint(accounts,"D12 read-only: Android ActivityLifecycleCallbacks and framework Instrumentation new-intent (API 1 / 35 overloads). No Intent contents are read. A same-Activity account switch may yield no lifecycle or new-intent callback; ALWAYS turn Repost OFF yourself before switching.");
        neutral(accounts,"Copy account-change signal report",this::copySelectedReport);
        LinearLayout avatars=section(avatarGroup,"FRIEND AVATARS · A → B VISUAL CANDIDATES",
                "Use Begin → shared Start A/B and Stop A/B. On each account, visit the same 3 surfaces, then tap the matching snapshot. Images are NOT identified as friends. Zero counts do not prove feature absence.");
        neutral(avatars,"Avatar 1 · Friends tab",()->toast(R1CD10AvatarAudit.snapshot(activity,panel,chip,0)));
        neutral(avatars,"Avatar 2 · Liked-by facepile",()->toast(R1CD10AvatarAudit.snapshot(activity,panel,chip,1)));
        neutral(avatars,"Avatar 3 · Reposted-by facepile",()->toast(R1CD10AvatarAudit.snapshot(activity,panel,chip,2)));
        hint(avatars,"EVIDENCE QUALITY: capture each of the three categories on BOTH A and B. These are FULL-SCREEN ImageView candidates, NOT verified facepiles. The same ImageViews can be counted in all three scans.");
        hint(avatars,"No avatar pixels, usernames, identifiers or synthetic images. Only bounded ImageView shape counts and fixed-label hints. Renderer identity remains UNQUALIFIED.");
        LinearLayout passive=section(passiveGroup,"PASSIVE DIAGNOSTICS · ALREADY RUNNING",
                "These counters update automatically through existing source-qualified hooks while Instagram is open, even with no active diagnostic. They are never an account/media identity verifier.");
        neutral(passive,"Copy passive-only report",this::copySelectedReport);
        hint(passive,"D12 also collects Android lifecycle / new-intent delivery and original UserSession reference transitions, independently labeled and ordered. These are transition hints, NEVER authenticated account identity.");
        hint(passive,"Use Copy ALL or Export ALL above to include passive counters AND every archived active diagnostic from this Instagram process. No raw Android logcat access and no automatic actions.");
        hint(passive,"D13 automatic screen discoveries and D14 native Reels-source menu observations also run here. HOME_FEED, STORY and REELS are R1B anchored hints; Reel origin and account identity are NOT VERIFIED.");
        LinearLayout auto=section(autoGroup,"D13 · AUTOMATIC SURFACE DISCOVERY · A → B",
                "Read-only discovery while Instagram is open. No manual surface snapshot buttons. Start A, browse Home Feed, bottom Reels tab, a Reel opened FROM Feed, and Stories; Stop A, manually switch accounts with Repost OFF, confirm, then repeat in B. Finish and Copy All.");
        autoStatus=label("",14,ACCENT);auto.addView(autoStatus);
        hint(auto,"Instagram view IDs distinguish an explicitly selected Feed/Clips tab when visible. Full-screen Reel origin is often ambiguous and must remain UNRESOLVED or CANDIDATE. Native UserSession changes are signals, not account identity.");
        neutral(auto,"Copy auto-discovery + account-change details",this::copySelectedReport);

        neutral(body, "Close panel", this::close);
        panel = scroll;
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(0, 0,
                Gravity.TOP | Gravity.END);
        decor.addView(panel, lp);
        positionPanel();
        refresh();
    }

    private String modeNextAction() {
        if(selectedMode==R1CD09DiagnosticGate.Mode.PASSIVE)return "PASSIVE AUTO ON: no Begin required. View counters or Copy/Export ALL.";
        if(!R1CD09DiagnosticGate.running())return "NEXT: select a mode and Begin. Then Start A, Stop A, confirm manual switch, Start B, Stop B and Finish.";
        switch(selectedMode) {
            case REELS: return "NEXT: Start A, browse Reels, confirm ITM OFF, then 90s Enable OR session toggle. Press OFF before Finish.";
            case AB: {
                if(R1CD07GateTrace.recording())return R1CD07GateTrace.activeIndex()==0 ?
                        "NEXT: browse A, then Stop A." : "NEXT: browse B, then Stop B.";
                return R1CD07GateTrace.cohortEvents(0)==0 ?
                        "NEXT: Start A." : R1CD07GateTrace.cohortEvents(1)==0 ?
                        "NEXT: manually switch to B, then Start B." :
                        "NEXT: Finish, Copy A/B report, then select another mode.";
            }
            case FEED: return "NEXT: compare the same Feed surfaces in A and B; use shared A/B buttons. No automatic account label.";
            case AVATAR: return "NEXT: snapshot Friends, liked-by and reposted-by on A and B. ImageView shapes are not avatar proof.";
            case MEDIA: return "NEXT: D05 A1→A2→B1→B2; Finish the capture, then Finish diagnostic.";
            case ACCOUNT: return "NEXT: compare UserSession callback coverage A and B; no callback after switch means UNVERIFIED.";
            case AUTO: return "NEXT: Start A and navigate through Feed, Reels tab, Feed Reel and Story; Stop A, switch manually, Start B, repeat, then Finish. Uncertain origins remain candidates.";
            default: return "NEXT: Finish or Abort before switching modes.";
        }
    }
    private boolean requireMode(R1CD09DiagnosticGate.Mode expected) {
        if(!R1CD09DiagnosticGate.active(expected)) {
            toast("REFUSED: choose "+expected+" in dropdown and Begin first; Finish or Abort other modes");
            return false;
        }
        return true;
    }
    private void beginSelectedMode() {
        if(R1CD09DiagnosticGate.running()) {
            toast("REFUSED: Finish or Abort current diagnostic first");return;
        }
        if(CAPTURE.isActiveWindow()) {
            toast("REFUSED: cancel active D05 capture first");return;
        }
        if(R1CD07GateTrace.recording()) {
            toast("REFUSED: stop A/B recording first");return;
        }
        if(selectedMode==R1CD09DiagnosticGate.Mode.PASSIVE){
            toast("Passive diagnostics are already ON. No Begin needed.");return;
        }
        String reply=R1CD09DiagnosticGate.begin(selectedMode);
        if(!R1CD09DiagnosticGate.active(selectedMode)){toast(reply);return;}
        itmRecoveryOffAcknowledged=false;
        R1CD10ComparisonFlow.begin(selectedMode);
        R1CD07Recovery.off("New diagnostic session");
        if(selectedMode==R1CD09DiagnosticGate.Mode.REELS ||
                selectedMode==R1CD09DiagnosticGate.Mode.AB) {
            R1CD09AccountSentinel.clear();
            R1CD07GateTrace.clear();
        }
        if(selectedMode==R1CD09DiagnosticGate.Mode.FEED)R1CD08FeedAudit.clear();
        if(selectedMode==R1CD09DiagnosticGate.Mode.AVATAR)R1CD10AvatarAudit.clear();
        if(selectedMode==R1CD09DiagnosticGate.Mode.MEDIA) {
            R1CD08MediaEvidence.clear();R1CD06PreMenuReconciliation.clear();
        }
        if(selectedMode==R1CD09DiagnosticGate.Mode.ACCOUNT)R1CD09AccountSentinel.clear();
        toast(reply);refresh();
    }
    private void finishSelectedMode() {
        if(!R1CD09DiagnosticGate.running()){toast("REFUSED: no active diagnostic");return;}
        if(R1CD07Recovery.isEnabled()){
            toast("REFUSED: press Recovery OFF first");return;
        }
        if(CAPTURE.isActiveWindow()){
            toast("REFUSED: Finish D05 A1/A2/B1/B2 first, or Abort");return;
        }
        if(R1CD07GateTrace.recording()){
            if(selectedMode==R1CD09DiagnosticGate.Mode.REELS &&
                    R1CD07GateTrace.activeIndex()==0){
                R1CD07GateTrace.stop(); // explicit Finish safely ends Reels A
            } else {
                toast("REFUSED: Stop A/B recording before Finish");return;
            }
        }
        if(!R1CD10ComparisonFlow.complete()){
            toast("REFUSED: Stop B to complete A → B; or Abort incomplete diagnostic");return;
        }
        String quality=R1CD10ComparisonFlow.quality();
        R1CD10ReportArchive.capture(selectedMode.name(),"FINISHED_"+quality,reportForSelectedMode());
        R1CD10ComparisonFlow.finish();
        R1CD08FeedAudit.stopCohort();R1CD10AvatarAudit.stop();
        R1CD08MediaEvidence.setManualCohort(-1);
        toast(R1CD09DiagnosticGate.finish()+" · "+quality+
            (quality.startsWith("INCOMPLETE")?" · missing "+R1CD10ComparisonFlow.missingFeatureHint():""));
        refresh();
    }
    private void abortSelectedMode() {
        if(!R1CD09DiagnosticGate.running()){toast("REFUSED: no active diagnostic");return;}
        R1CD07Recovery.off("User aborted diagnostic");
        if(R1CD07GateTrace.recording())R1CD07GateTrace.stop();
        CAPTURE.cancel(false);R1CD06PreMenuReconciliation.clear();
        R1CD10ReportArchive.capture(selectedMode.name(),"ABORTED_PARTIAL_NOT_QUALIFIED",reportForSelectedMode());
        R1CD10ComparisonFlow.abort();
        R1CD08FeedAudit.stopCohort();R1CD10AvatarAudit.stop();R1CD08MediaEvidence.setManualCohort(-1);
        itmRecoveryOffAcknowledged=false;
        toast(R1CD09DiagnosticGate.abort("Explicit user Abort; recovered state OFF"));refresh();
    }
    private void toggleSessionRepost(CompoundButton control, boolean enabled) {
        if(settingPicker)return;
        if(!enabled){
            if(R1CD07Recovery.isSessionMode())R1CD07GateTrace.stopExactTrial();
            itmRecoveryOffAcknowledged=false;refresh();return;
        }
        if(!requireMode(R1CD09DiagnosticGate.Mode.REELS) ||
                !itmRecoveryOffAcknowledged || CAPTURE.isActiveWindow() ||
                !R1CD07GateTrace.recording() || R1CD07GateTrace.activeIndex()!=0 ||
                R1BContextMonitor.stableScreen()!=R1BNavigationPolicy.Screen.REELS ||
                !R1CD07ExactGateTrial.eligibleForHF13Recovery()) {
            settingPicker=true;control.setChecked(false);settingPicker=false;
            toast("REFUSED: finish Reels Steps 1-4, confirm ITM OFF and fresh A evidence first");return;
        }
        toast(R1CD07GateTrace.armHF13Session());refresh();
    }
    private String reportForSelectedMode(){
        String data;
        switch(selectedMode){
            case REELS: data=R1CD07GateTrace.reportCore();break;
            case AB: data=R1CD07GateTrace.reportCore()+R1CD09AccountSentinel.summary();break;
            case FEED: data=R1CD08FeedAudit.report();break;
            case MEDIA: data=CAPTURE.report()+R1CD08MediaEvidence.report();break;
            case ACCOUNT: data=R1CD09AccountSentinel.summary()+R1CD12AccountLifecycle.report();break;
            case AVATAR: data=R1CD10AvatarAudit.report();break;
            case PASSIVE: data=R1CD10PassiveLedger.report()+R1CD12AccountLifecycle.report()+R1CD13AutoSurface.report()+R1CD14NativeOriginProbe.report();break;
            case AUTO: data=R1CD13AutoSurface.report()+R1CD14NativeOriginProbe.report()+R1CD09AccountSentinel.summary()+R1CD12AccountLifecycle.report();break;
            default:data="UNAVAILABLE";
        }
        return R1CD09DiagnosticGate.status()+"\n"+R1CD10ComparisonFlow.summary()+
            "D06_MEDIA_IDENTITY=NOT_VERIFIED\nACTIONS=OFF\n"+
            "ITM_REPOST_OFF=MANUAL_ASSERTION_NOT_VERIFIED\n"+data;
    }
    private void copyText(String title,String data){
        ClipboardManager clip=(ClipboardManager)activity.getSystemService(Context.CLIPBOARD_SERVICE);
        if(clip==null){toast("Clipboard unavailable");return;}
        clip.setPrimaryClip(ClipData.newPlainText(title,data));
        toast("Copied content-free diagnostic text to clipboard");
    }
    private void copySelectedReport(){copyText("INA single diagnostic",reportForSelectedMode());}
    private void copyAllDiagnostics(){
        copyText("INA ALL diagnostics · current Instagram process",
            R1CD10ReportArchive.all(selectedMode.name(),
                R1CD09DiagnosticGate.running()?reportForSelectedMode():"" )+"\n--- AUTOMATIC PASSIVE DISCOVERY ---\n"+R1CD13AutoSurface.report()+R1CD14NativeOriginProbe.report());
    }
    private void exportAllDiagnostics(){
        String data=R1CD10ReportArchive.all(selectedMode.name(),
            R1CD09DiagnosticGate.running()?reportForSelectedMode():"")+
            "\n--- AUTOMATIC PASSIVE DISCOVERY ---\n"+R1CD13AutoSurface.report()+R1CD14NativeOriginProbe.report();
        Intent send=new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_SUBJECT,"InstaActions ALL diagnostic session report");
        send.putExtra(Intent.EXTRA_TEXT,data);
        try{activity.startActivity(Intent.createChooser(send,"Export InstaActions diagnostics"));}
        catch(RuntimeException error){toast("No Android text export target available; use Copy ALL");}
    }
    private void runStartA(){runStartCohort(0);}
    private void runStartB(){runStartCohort(1);}
    private void runStartCohort(int index){
        if(!R1CD09DiagnosticGate.active(selectedMode)){
            toast("REFUSED: Begin the selected diagnostic first");return;
        }
        if(CAPTURE.isActiveWindow()){
            toast("REFUSED: Finish/Cancel D05 media capture first");return;
        }
        if(R1CD07Recovery.isEnabled()){
            toast("REFUSED: turn Repost OFF before changing account");return;
        }
        String state=index==0?R1CD10ComparisonFlow.startA():R1CD10ComparisonFlow.startB();
        if(state.startsWith("REFUSED")){toast(state);return;}
        if(selectedMode==R1CD09DiagnosticGate.Mode.REELS ||
                selectedMode==R1CD09DiagnosticGate.Mode.AB){
            if(!R1CD07GateTrace.ready()){
                R1CD10ComparisonFlow.abort();
                toast("REFUSED: exact Repost hooks not ready. Abort this session.");return;
            }
            toast(R1CD07GateTrace.begin(index));
        }
        if(selectedMode==R1CD09DiagnosticGate.Mode.FEED)R1CD08FeedAudit.beginCohort(index);
        if(selectedMode==R1CD09DiagnosticGate.Mode.AVATAR)R1CD10AvatarAudit.begin(index);
        if(selectedMode==R1CD09DiagnosticGate.Mode.MEDIA)R1CD08MediaEvidence.setManualCohort(index);
        itmRecoveryOffAcknowledged=false;
        toast(state);refresh();
    }
    private void runStopA(){runStopCohort(0);}
    private void runStopB(){runStopCohort(1);}
    private void runStopCohort(int index){
        if(!R1CD09DiagnosticGate.active(selectedMode)){
            toast("REFUSED: Begin this diagnostic first");return;
        }
        if(CAPTURE.isActiveWindow()){
            toast("REFUSED: Finish the D05 Reel capture before stopping this account");return;
        }
        if(R1CD07Recovery.isEnabled()){
            toast("REFUSED: Repost recovery OFF before account handoff");return;
        }
        if(R1CD10ComparisonFlow.cohort()!=index){
            toast("REFUSED: wrong A/B phase");return;
        }
        if(selectedMode==R1CD09DiagnosticGate.Mode.REELS ||
                selectedMode==R1CD09DiagnosticGate.Mode.AB){
            if(R1CD07GateTrace.cohortEvents(index)>0){
                R1CD10ComparisonFlow.recordFeature(0,true);
                R1CD10ComparisonFlow.sample();
            }
            R1CD07GateTrace.stop();
        }
        String outcome=index==0?R1CD10ComparisonFlow.stopA():R1CD10ComparisonFlow.stopB();
        R1CD08FeedAudit.stopCohort();R1CD10AvatarAudit.stop();
        R1CD08MediaEvidence.setManualCohort(-1);
        if(index==0){
            R1CD09DiagnosticGate.expectManualABSwitch();
            R1CD09AccountSentinel.markManualSwitch();
        }
        R1CD10ReportArchive.capture(selectedMode.name(),index==0?"STOP_A":"STOP_B",
                reportForSelectedMode());
        itmRecoveryOffAcknowledged=false;
        toast(outcome);refresh();
    }
    private void runConfirmSwitch(){
        if(!R1CD09DiagnosticGate.active(selectedMode)){
            toast("REFUSED: Begin this diagnostic first");return;
        }
        toast(R1CD10ComparisonFlow.confirmSwitch());refresh();
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
            modePicker=null;cohortPicker=null;modeStatus=null;keepRepost=null;
            avatarGroup=null;passiveGroup=null;autoGroup=null;autoStatus=null;comparisonStepGroup=null;comparisonStatus=null;
            reelsGroup=null;compareGroup=null;feedGroup=null;mediaGroup=null;accountGroup=null;
            compareStartA=null;
        }
    }
    private void toast(String text) {
        Toast.makeText(activity.getApplicationContext(), text, Toast.LENGTH_LONG).show();
    }
    private void primaryAction() {
        if(!requireMode(R1CD09DiagnosticGate.Mode.MEDIA))return;
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
        if(R1CD09DiagnosticGate.state()==R1CD09DiagnosticGate.State.ABORTED &&
                R1CD07GateTrace.recording())R1CD07GateTrace.stop();
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
        if(modeStatus!=null)copyIfChanged(modeStatus,
                R1CD09DiagnosticGate.status()+"\n"+modeNextAction());
        if(comparisonStatus!=null)copyIfChanged(comparisonStatus,R1CD10ComparisonFlow.summary());
        if(reelsGroup!=null)reelsGroup.setVisibility(selectedMode==R1CD09DiagnosticGate.Mode.REELS?View.VISIBLE:View.GONE);
        if(compareGroup!=null)compareGroup.setVisibility(selectedMode==R1CD09DiagnosticGate.Mode.AB?View.VISIBLE:View.GONE);
        if(feedGroup!=null)feedGroup.setVisibility(selectedMode==R1CD09DiagnosticGate.Mode.FEED?View.VISIBLE:View.GONE);
        if(mediaGroup!=null)mediaGroup.setVisibility(selectedMode==R1CD09DiagnosticGate.Mode.MEDIA?View.VISIBLE:View.GONE);
        if(accountGroup!=null)accountGroup.setVisibility(selectedMode==R1CD09DiagnosticGate.Mode.ACCOUNT?View.VISIBLE:View.GONE);
        if(avatarGroup!=null)avatarGroup.setVisibility(selectedMode==R1CD09DiagnosticGate.Mode.AVATAR?View.VISIBLE:View.GONE);
        if(passiveGroup!=null)passiveGroup.setVisibility(selectedMode==R1CD09DiagnosticGate.Mode.PASSIVE?View.VISIBLE:View.GONE);
        if(autoGroup!=null)autoGroup.setVisibility(selectedMode==R1CD09DiagnosticGate.Mode.AUTO?View.VISIBLE:View.GONE);
        if(autoStatus!=null)copyIfChanged(autoStatus,R1CD13AutoSurface.brief());
        if(comparisonStepGroup!=null)comparisonStepGroup.setVisibility(
                selectedMode!=R1CD09DiagnosticGate.Mode.PASSIVE &&
                selectedMode!=R1CD09DiagnosticGate.Mode.REELS &&
                selectedMode!=R1CD09DiagnosticGate.Mode.AB?View.VISIBLE:View.GONE);
        if(keepRepost!=null)keepRepost.setEnabled(R1CD07Recovery.isSessionMode() ||
                (!on && R1CD09DiagnosticGate.active(R1CD09DiagnosticGate.Mode.REELS)
                && observingA && itmRecoveryOffAcknowledged &&
                R1CD07ExactGateTrial.eligibleForHF13Recovery() &&
                R1BContextMonitor.stableScreen()==R1BNavigationPolicy.Screen.REELS));
        if(keepRepost!=null && keepRepost.isChecked()!=R1CD07Recovery.isSessionMode()) {
            settingPicker=true;keepRepost.setChecked(R1CD07Recovery.isSessionMode());settingPicker=false;
        }
        // Cohort is chosen only by explicit Start A/B; never silently defaults to affected.
        if(compareStartA!=null)compareStartA.setEnabled(R1CD09DiagnosticGate.active(R1CD09DiagnosticGate.Mode.AB)
                && !R1CD07GateTrace.recording() && R1CD07GateTrace.cohortEvents(0)==0
                && !on && R1CD07GateTrace.ready());
        if (enableRecovery != null) enableRecovery.setEnabled(!on && R1CD09DiagnosticGate.active(R1CD09DiagnosticGate.Mode.REELS)
                && R1CD07GateTrace.ready() && InstaActionsEntry.dataGateReady()
                && R1BContextMonitor.stableScreen()==R1BNavigationPolicy.Screen.REELS
                && observingA && R1CD07ExactGateTrial.eligibleForHF13Recovery()
                && itmRecoveryOffAcknowledged && !CAPTURE.isActiveWindow());
        if (startA != null) startA.setEnabled(R1CD09DiagnosticGate.active(R1CD09DiagnosticGate.Mode.REELS) && !on && !observingB && R1CD07GateTrace.ready());
        if (stopA != null) stopA.setEnabled(R1CD09DiagnosticGate.active(R1CD09DiagnosticGate.Mode.AB) && R1CD07ComparisonFlow.canStopA(
                R1CD07GateTrace.recording(), R1CD07GateTrace.activeIndex(), on));
        if (startB != null) startB.setEnabled(R1CD09DiagnosticGate.active(R1CD09DiagnosticGate.Mode.AB) && R1CD07ComparisonFlow.canStartB(
                R1CD07GateTrace.recording(), R1CD07GateTrace.activeIndex(),
                R1CD07GateTrace.cohortEvents(0), on, R1CD07GateTrace.ready()));
        if (stopB != null) stopB.setEnabled(R1CD09DiagnosticGate.active(R1CD09DiagnosticGate.Mode.AB) && R1CD07ComparisonFlow.canStopB(
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
            primary.setEnabled(R1CD09DiagnosticGate.active(R1CD09DiagnosticGate.Mode.MEDIA)
                    && (d05 || R1CD04PassiveProbe.hasCaptureCapacity()));
        }
    }
    private void copy() {
        if(!requireMode(R1CD09DiagnosticGate.Mode.MEDIA))return;
        ClipboardManager manager = (ClipboardManager)
                activity.getSystemService(Context.CLIPBOARD_SERVICE);
        if (manager == null) {toast("Clipboard unavailable");return;}
        manager.setPrimaryClip(ClipData.newPlainText("INA D05 safe diagnostic report", CAPTURE.report()));
        toast("Content-free D05 report copied");
    }
    private void share() {
        if(!requireMode(R1CD09DiagnosticGate.Mode.MEDIA))return;
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_TEXT, CAPTURE.report());
        try { activity.startActivity(Intent.createChooser(send, "Share D05 diagnostic report")); }
        catch (RuntimeException unavailable) {toast("No share target available");}
    }
    private void detach() {
        R1CD07Recovery.onActivityExit();
        // Activity changes are not proof of account switching. Preserve diagnostic
        // session across ordinary Instagram Activity transitions, but disarm recovery.
        if(R1CD09DiagnosticGate.running() &&
                R1CD09DiagnosticGate.active(R1CD09DiagnosticGate.Mode.REELS)){
            R1CD10ReportArchive.capture("REELS","ACTIVITY_EXIT_PARTIAL",reportForSelectedMode());
            R1CD10ComparisonFlow.abort();
            R1CD09DiagnosticGate.abort("Activity left foreground");
        }
        itmRecoveryOffAcknowledged=false;
        detached=true;
        if(ticker!=null) MAIN.removeCallbacks(ticker);
        CAPTURE.cancel(true);
        R1CD06PreMenuReconciliation.clear();
        close();
        if(chip!=null) {decor.removeView(chip);chip=null;}
    }
}
