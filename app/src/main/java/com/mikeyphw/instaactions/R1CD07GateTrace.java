package com.mikeyphw.instaactions;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.util.ArrayDeque;
import java.util.Locale;

/**
 * P04 exact-APK diagnostic only. No Instagram account identity is read or stored.
 * An observer may be installed in only two exact verified IG455 lambda methods.
 * Their selectors come from static IG455 DEX packed-switch analysis (not guesswork):
 *  X.0Ie0.invoke() $t=4 (RepostButtonUseCase getUiState/isEligible)
 *  X.0F2B.invoke(Object) $t=11,12 (UfiUseCase Repost button/count)
 * They are multipurpose. Passive A/B capture never changes return values.
 * HF03 disables the legacy Boolean trial; selectors return ORIGINAL values.
 * Primitive getter observations are captured only within selector 4 calls.
 */
final class R1CD07GateTrace {
    private static final String IG_PACKAGE = "com.instagram.android";
    private static final String IG_VERSION = "455.0.0.8.72";
    private static final long IG_SIZE = 238346353L;
    private static final String IG_SHA256 = "fb6ca8c21d909d95892e67339746b8fe43f409a9dea11f87d9ab0cee8e97c5b6";
    private static final R1CD07TraceSummary STATE = new R1CD07TraceSummary();
    private static volatile String installStatus = "NOT REQUESTED (open InstaActions FAB)";
    private static volatile boolean ready;
    private static Field uiSelector, ufiSelector;
    private static volatile R1CD07InputPresence inputPresence;
    private static volatile R1CD07BranchInputs branchInputs;

    private R1CD07GateTrace() { }
    static String status() {return installStatus;}
    static boolean ready() {return ready;}
    static int activeIndex() {return STATE.activeIndex();}
    static boolean recording() {return STATE.recording();}
    static int cohortEvents(int index) {return STATE.eventsFor(index);}
    static String feedback() {
        if (!ready) return "P04 · " + installStatus;
        String capture=STATE.feedback();
        String trial=R1CD07Recovery.status();
        return trial.startsWith("ENABLED") ? capture+"\n⚡ HF13 RECOVERY: "+trial : capture;
    }
    static void setStatus(String status) {installStatus=status;}

    // The existing D04 exact-build gate performs the one APK hash.
    // D07 installs only after R1CD04PassiveProbe.isInstalled().

    static Method uiMethod(ClassLoader loader) throws ReflectiveOperationException {
        Class<?> c=Class.forName("X.0Ie0",false,loader);
        Field f=c.getDeclaredField("$t");
        if (f.getType()!=int.class) throw new NoSuchFieldException("selector type");
        Method m=c.getDeclaredMethod("invoke");
        if (m.getReturnType()!=Object.class) throw new NoSuchMethodException("UI result type");
        uiSelector=f;
        return m;
    }
    static Method ufiMethod(ClassLoader loader) throws ReflectiveOperationException {
        Class<?> c=Class.forName("X.0F2B",false,loader);
        Field f=c.getDeclaredField("$t");
        if (f.getType()!=int.class) throw new NoSuchFieldException("selector type");
        Method m=c.getDeclaredMethod("invoke",Object.class);
        if (m.getReturnType()!=Object.class) throw new NoSuchMethodException("UFI result type");
        ufiSelector=f;
        return m;
    }
    static Method mobileConfigMethod(ClassLoader loader) throws ReflectiveOperationException {
        // HF09: X.01ym.A02's *actual* runtime return class was X.02nr
        // across 56/56 recorded selector-4 factory callbacks. IG455 classes.dex
        // declares the concrete BdA(long):boolean directly on X.02nr.
        // Retire the X.02ja and X.0T4C guesses, without calling the getter.
        return R1CD07ActualConfigTarget.resolve(loader);
    }
    static Method nullableConfigMethod(ClassLoader loader) throws ReflectiveOperationException {
        // X.0ApG.D3r is an interface method; hook the verified concrete X.02ib.
        Class<?> c=Class.forName("X.02ib",false,loader);
        Method m=c.getDeclaredMethod("D3r",int.class);
        if(m.getReturnType()!=Boolean.class)throw new NoSuchMethodException("D3r result type");
        return m;
    }
    static Method sessionCheckMethod(ClassLoader loader) throws ReflectiveOperationException {
        Class<?> c=Class.forName("X.02xy",false,loader);
        Class<?> session=Class.forName("com.instagram.common.session.UserSession",false,loader);
        Method m=c.getDeclaredMethod("A04",session,String.class);
        if(m.getReturnType()!=boolean.class)throw new NoSuchMethodException("A04 result type");
        return m;
    }
    static void setInputPresence(R1CD07InputPresence probe) {inputPresence=probe;}
    static void setBranchInputs(R1CD07BranchInputs probe) {branchInputs=probe;}
    static void installed(int optionalInstalled,String unavailable) {
        // Core state hooks are already installed, so allow A/B even if an
        // optional configuration observer is unavailable on this IG455 build.
        ready=true;
        installStatus="READY: Repost A/B core (2/2), optional " + optionalInstalled + "/3; " +
                "HF09 X.02nr actual-config A/B read-only; HF07 factory-dispatch A/B read-only" +
                (unavailable.isEmpty()?"":"; unavailable: "+unavailable);
    }
    static R1CD07PredicateTrace.Frame enterUi(Object instance) {
        if(!ready || !STATE.recording() || uiSelector==null || instance==null)return null;
        try {if(uiSelector.getInt(instance)!=4)return null;
             R1CD07InputPresence probe=inputPresence;
             int presence=probe==null?R1CD07InputPresence.UNAVAILABLE:probe.classify(instance);
             R1CD07BranchInputs extra=branchInputs;
             int repost=extra==null?R1CD07BranchInputs.UNAVAILABLE:extra.classify(instance);
             return R1CD07PredicateTrace.enter(STATE.activeIndex(),presence,repost);}
        catch(Throwable ignored){return null;}
    }
    static void leaveUi(R1CD07PredicateTrace.Frame frame,Object originalResult) {
        R1CD07PredicateTrace.leave(frame,originalResult);
    }
    /** Passive result classification; caller always receives the exact original object. */
    static Object noteUi(Object instance, Object result) {
        if (!STATE.recording() || uiSelector==null || instance==null) return result;
        try {if(uiSelector.getInt(instance)==4)STATE.record(4,result);}
        catch(Throwable ignored) { }
        return result;
    }
    static void noteUfi(Object instance, Object result) {
        if (!STATE.recording() || ufiSelector==null || instance==null) return;
        try {int selector=ufiSelector.getInt(instance);if(selector==11 || selector==12)STATE.record(selector,result);}
        catch (Throwable ignored) { }
    }
    static String begin(int index) {
        if (!ready) return "REFUSED: P04 hooks not ready ("+status()+")";
        R1CD07Recovery.onCaptureTransition();
        R1CD07ExactGateTrial.disarm();
        STATE.start(index);
        R1CD07ExactGateTrial.onCaptureStart(index);
        return "INA D07 recording manual account "+(index==0?"A":"B")+" (no identity read)";
    }
    static String stop() {R1CD07Recovery.onCaptureTransition();R1CD07ExactGateTrial.disarm();R1CD07ExactGateTrial.onCaptureStop();STATE.stop();return "P04 stopped (HF13 OFF)";}
    static String armExactTrial() {return "REFUSED: HF12 is now regression-only; use HF13 recovery control";}
    static String armHF13Recovery() {return R1CD07Recovery.arm(STATE.activeIndex(),ready,
        InstaActionsEntry.dataGateReady());}
    static String armHF13Session() {return R1CD07Recovery.armSession(STATE.activeIndex(),ready,
        InstaActionsEntry.dataGateReady());}
    static String stopExactTrial() {R1CD07Recovery.off("User pressed Recovery OFF");
        R1CD07ExactGateTrial.disarm();return "HF13 native Repost UI recovery OFF (future calls native)";}
    static String clear() {R1CD07Recovery.off("Clear A/B");R1CD07ExactGateTrial.reset();STATE.clear();R1CD07PredicateTrace.clear();return "A/B observations cleared; recovery OFF";}
    static String reportCore() {
        return "INA_D07_REPOST_EXACT_APK_A_B_TRACE\nHook: "+status()+"\n"+STATE.render()
            + InstaActionsEntry.dataGateStatus() + "\n"
            + R1CD07PredicateTrace.report()
            + R1CD07PredicateTrace.d08ExactConfigReport()
            + "HF13 native Repost recovery: " + R1CD07Recovery.status() + "\n"
            + "Reels TAB is a user assertion, not an automatically verified selector. Feed Reel and ordinary post NOT QUALIFIED; D06 media identity NOT VERIFIED.\n"
            + "HF12 legacy trial (regression only): " + R1CD07ExactGateTrial.status() + "\n";
    }

    static String report(){return reportCore()+R1CD08MediaEvidence.report()+R1CD08FeedAudit.report()
            + R1CD09AccountSentinel.summary();}

    static String countVisibleLabels(Activity activity,View excludedPanel,View excludedChip) {
        if (!ready || !STATE.recording()) return "REFUSED: begin A or B and wait for hooks";
        // Transient exact-word comparison only: no UI text or content descriptions are stored.
        View root=activity.getWindow().getDecorView();
        ArrayDeque<View> queue=new ArrayDeque<>(); queue.add(root);
        int visited=0,repost=0,friends=0;
        while (!queue.isEmpty() && visited<1400) {
            View v=queue.removeFirst();
            if(v==excludedPanel || v==excludedChip || v.getVisibility()!=View.VISIBLE)continue;
            visited++;
            String desc=v.getContentDescription()==null?"":v.getContentDescription().toString();
            String word="";
            if (v instanceof TextView) {
                CharSequence t=((TextView)v).getText();
                word=t==null?"":t.toString();
            }
            if (labelIs(desc,"repost","repostar","republicar") ||
                labelIs(word,"repost","repostar","republicar")) repost++;
            if (labelIs(desc,"friends","amigos") || labelIs(word,"friends","amigos")) friends++;
            if(v instanceof ViewGroup) {
                ViewGroup parent=(ViewGroup)v;
                for(int i=0;i<parent.getChildCount() && queue.size()<1400;i++) queue.addLast(parent.getChildAt(i));
            }
        }
        String aggregate="visited="+visited+" repost_labels="+repost+" friends_labels="+friends;
        STATE.uiCheck(aggregate);
        return "UI labels counted: "+aggregate+" (Litho/unrendered elements may be absent)";
    }
    private static boolean labelIs(String text,String... expected) {
        String s=text.trim().toLowerCase(Locale.ROOT);
        for(String candidate:expected)if(candidate.equals(s))return true;
        return false;
    }
}
