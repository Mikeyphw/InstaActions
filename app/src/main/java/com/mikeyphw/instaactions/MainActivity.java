package com.mikeyphw.instaactions;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * D07 UX02 ordered information screen. Installed package versions are informational,
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
        add(root, "Personal Instagram quick actions · Feed / Reels / Stories", 16, BLUE, true);
        add(root, "D07 · Native Repost recovery", 16, BLUE, true);
        add(root, "User-confirmed: Reels-tab Repost appeared for 90 seconds and worked. Formal D07C acceptance is still pending.", 14, AMBER, false);

        LinearLayout pickerGuide=card("NEW · One diagnostic at a time");
        add(pickerGuide, "Open IA → choose a diagnostic. Passive overview runs automatically; active modes require Begin.", 15, WHITE, true);
        add(pickerGuide, "Select Reels recovery, Account A/B, Feed Repost, Visible media, Account change, or Friend avatars. Only the chosen tools appear. Finish preserves that report; Abort disarms and ends it. Switching while active is refused.",14,DETAIL,false);
        add(pickerGuide, "For EVERY active feature: Begin → Start A → observe → Stop A → switch manually with recovery OFF → confirm switch → Start B → repeat the SAME observations → Stop B → Finish → Copy/Export ALL. Account labels are still manual, not verified identity.",14,WHITE,false);
        add(pickerGuide, "Reels recovery offers 90s trial or Keep Repost available (foreground session only). This is NOT saved across restarts or account changes; emergency OFF remains visible.",14,AMBER,false);
        add(pickerGuide, "Account-change diagnostic now combines the existing opaque UserSession reference signal with independent Android Activity lifecycle and new-intent dispatch events. Neither proves account identity; missing callbacks mean NOT VERIFIED.",14,DETAIL,false);

        LinearLayout status = card("What is ready?");
        add(status, "D07 SOURCE PRESENT · DEVICE TEST PENDING", 14, GREEN, true);
        add(status, "Device acceptance: NOT RUN (formal D07C). Separately, the user reported a successful native Repost on the dedicated Reels tab; feed posts remain unqualified.", 14, DETAIL, false);
        add(status, "Instagram: " + installedVersion(ModuleConstants.TARGET_PACKAGE), 14, WHITE, false);
        add(status, "InstaEclipse: " + installedVersion("ps.reso.instaeclipse"), 14, WHITE, false);
        add(status, "Vector / LSPosed module enabled: NOT VERIFIED from this screen", 13, AMBER, false);

        LinearLayout diagnosticPicker=card("Read one workflow at a time");
        add(diagnosticPicker,"Choose a diagnostic below. This is guidance only; the interactive Begin / Finish / Abort controls live inside Instagram's IA panel.",14,DETAIL,false);
        Spinner selector=new Spinner(this);
        String[] choices={"Reels Repost", "A/B native comparison", "Feed Repost read-only",
                          "D05/D06 visible-media evidence", "Account-change signal",
                          "Friend-avatar shapes A/B", "Passive overview · auto ON"};
        ArrayAdapter<String> chooserAdapter=new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item,choices) {
            @Override public View getView(int position,View convertView,ViewGroup parent) {
                View v=super.getView(position,convertView,parent);
                if(v instanceof TextView) {
                    ((TextView)v).setTextColor(WHITE);
                    v.setBackgroundColor(CARD);v.setPadding(dp(10),dp(12),dp(10),dp(12));
                }
                return v;
            }
            @Override public View getDropDownView(int position,View convertView,ViewGroup parent){
                View v=super.getDropDownView(position,convertView,parent);
                if(v instanceof TextView) {
                    ((TextView)v).setTextColor(WHITE);
                    v.setBackgroundColor(0xff2a3a4e);
                    v.setPadding(dp(12),dp(14),dp(12),dp(14));
                }
                return v;
            }
        };
        chooserAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        selector.setAdapter(chooserAdapter);diagnosticPicker.addView(selector);

        LinearLayout guide = card("Do these 5 steps IN ORDER");
        add(guide, "STEP 1/5 · PREPARE", 16, BLUE, true);
        add(guide, "In Vector/LSPosed enable InstaActions for Instagram. Keep InstaEclipse ON and restart Instagram. Turn InstaTaskManager's separate Repost recovery OFF. On the affected account, open Instagram's dedicated bottom Reels tab. Wait for native hooks.", 14, WHITE, false);
        add(guide, "STEP 2/5 · START A", 16, BLUE, true);
        add(guide, "Tap the floating IA chip, then Start A ONCE. This only records native eligibility observations. Do not choose B or Stop A/B.", 14, WHITE, false);
        add(guide, "STEP 3/5 · BROWSE AND RETURN", 16, BLUE, true);
        add(guide, "Close IA and swipe through several Reels in the same bottom Reels tab. Reopen IA. If fresh A evidence is still NOT READY, browse more Reels and return. KEEP A RECORDING.", 14, WHITE, false);
        add(guide, "STEP 4/5 · CONFIRM, THEN ENABLE", 16, BLUE, true);
        add(guide, "AFTER fresh A evidence is ready: press 4A (manual ITM recovery OFF confirmation), THEN press 4B (Enable). Start A clears earlier confirmations. Recovery is limited to 90 seconds or 64 changed checks.", 14, WHITE, false);
        add(guide, "STEP 5/5 · TEST, OFF, EXPORT", 16, BLUE, true);
        add(guide, "Close IA. Tap Instagram's ORIGINAL Repost button yourself. Reopen IA immediately, press Recovery OFF BEFORE switching account or tab, then Copy D07 safe Repost report. Record the actual result in separate D07C physical acceptance.", 14, WHITE, false);
        add(guide, "If anything fails: press Recovery OFF anyway and copy the report. Optional Account B and D05/D06 diagnostics are NOT required for this trial.", 14, AMBER, true);

        LinearLayout comparison = card("Optional: compare A and B (separate workflow)");
        add(comparison, "1. Start A, browse the dedicated Reels tab and reopen IA.", 14, WHITE, false);
        add(comparison, "2. Press A2: Stop A (preserves original results). Recovery must be OFF.", 14, WHITE, false);
        add(comparison, "3. Switch Instagram accounts MANUALLY, keeping Instagram running if possible.", 14, WHITE, false);
        add(comparison, "4. Press Start B, browse Reels, then press Stop B.", 14, WHITE, false);
        add(comparison, "5. Press Copy combined A/B report. A process restart loses both in-memory captures.", 14, WHITE, false);
        add(comparison, "These manual labels cannot prove which Instagram account was active. Never use the 90-second Repost recovery during account switching.", 14, AMBER, false);

        add(comparison, "X.02ib and D3r diagnostics: the A/B comparison records native TRUE/FALSE/NULL, ordinal and runtime declaring-class categories. It does not prove why accounts differ.",14,DETAIL,false);
        LinearLayout d08 = card("D08A · Feed Repost · read-only snapshots");
        add(d08, "D08A · Feed Repost labels (not an override)", 16, BLUE, true);
        add(d08, "In Instagram open a normal Feed post, a Feed-opened Reel, and the bottom Reels-tab reference separately. For each, open IA and tap its corresponding D08A Snapshot. The labels and origin are observations/manual claims, not proven media eligibility.", 14, WHITE, false);
        add(d08, "Begin Feed → Start A (manual affected) → take three snapshots → Stop A → switch and confirm → Start B (manual unaffected) → repeat snapshots → Stop B → Finish. No implicit cohort defaults and no Feed Repost action.",14,AMBER,false);
        LinearLayout mediaGuide=card("D08C · Visible-media research (D05/D06)");
        add(mediaGuide, "Begin Media, Start account A, complete D05 four-stage Reel capture (A1/A2/B1/B2 are REELS, not accounts), Stop account A, switch/confirm, Start account B and repeat four-stage capture. Stop B, Finish. No action enabled. Each inconclusive result is broken down by owner/page/pre-menu enum evidence.",14,WHITE,false);
        add(mediaGuide, "Exact visible-media identity remains NOT VERIFIED. Friends avatars: source candidates only; no fake faces, disabled-state bypass, or avatar mutation.",14,AMBER,false);
        LinearLayout accountGuide=card("D09 · Account-change signal");
        add(accountGuide, "Begin account-change diagnostic → Start A → observe → Stop A → switch manually/confirm → Start B → observe → Stop B → Finish. Export All preserves other reports too. The native source-verified callback counts weak UserSession reference transitions only.",14,WHITE,false);
        add(accountGuide, "No callback does NOT prove same account. Changed reference is a signal, not verified account identity. Keep Repost OFF during investigation.",14,AMBER,false);
        final LinearLayout[] visibleGuides={guide,comparison,d08,mediaGuide,accountGuide};
        for(int i=1;i<visibleGuides.length;i++)visibleGuides[i].setVisibility(View.GONE);
        selector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override public void onItemSelected(AdapterView<?> parent,View view,int index,long id){
                for(int i=0;i<visibleGuides.length;i++)
                    visibleGuides[i].setVisibility(i==index?View.VISIBLE:View.GONE);
            }
            @Override public void onNothingSelected(AdapterView<?> parent){}
        });

        LinearLayout passiveGuide=card("Passive diagnostics and ALL reports");
        add(passiveGuide,"Passive is ON automatically in the Instagram process: existing Repost, UFI, media-menu, session and screen-hint callbacks increment bounded anonymous counters without a manual Begin or any new hook.",14,WHITE,false);
        add(passiveGuide,"Copy ALL and Export ALL are always visible in IA. They include completed and aborted mode summaries, current-mode report and passive counters since Instagram opened. Export All shares text through Android; it is not unrestricted logcat, and the archive disappears after Instagram exits.",14,DETAIL,false);
        add(passiveGuide,"Avatar mode: Start A → capture Friends tab, liked-by and reposted-by VIEW candidates → Stop A → switch/confirm → repeat B → Stop B → Finish. Neither generic ImageViews nor labels prove friend identities.",14,AMBER,false);
        LinearLayout originalPlan = card("Original InstaActions goals · R0–R8");
        add(originalPlan, "Today: R1C/D07 Repost research with a user-reported successful Reels-tab action. D06 exact active-media identity remains NOT VERIFIED.", 14, WHITE, false);
        add(originalPlan, "Next: R2 contextual action UI and R3 explicit Like/Save/Repost/Download/Playback actions, only where independently qualified.", 14, DETAIL, false);
        add(originalPlan, "Later: R4 native sharing, R5 preview-and-confirm single comment, R6 retries and cancellation, R7 Android 16 CI/device proof, R8 release.", 14, DETAIL, false);
        add(originalPlan, "Do not duplicate InstaEclipse features or dispatch unqualified Feed/Story actions. Friend avatars and Feed Repost remain separate D08 investigations.", 14, AMBER, false);

        LinearLayout safety = card("What the controls actually do");
        add(safety, "Start A / Start B: read-only native result observations. These are manual labels, NOT verified Instagram account identities.", 14, DETAIL, false);
        add(safety, "Enable: usual 90-second/64-change trial, or optional foreground-session toggle with 4096-call cap. Neither is automatic Repost. OFF is the default.", 14, DETAIL, false);
        add(safety, "Recovery OFF: restores original values for subsequent native calls. It does not undo a Repost already performed in Instagram.", 14, DETAIL, false);
        add(safety, "Advanced D05/D06: optional diagnostics only; media identity remains unverified and unrelated actions remain disabled.", 14, DETAIL, false);
        add(safety, "UserSession weak-reference changes may be observed, but same-Activity account changes cannot be reliably detected. Turn OFF manually before changing accounts.", 14, AMBER, false);

        LinearLayout limits = card("Supported surfaces and privacy");
        add(limits, "Dedicated Reels tab: user-reported InstaActions native Repost success; formal D07C checks still pending.", 14, WHITE, false);
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
