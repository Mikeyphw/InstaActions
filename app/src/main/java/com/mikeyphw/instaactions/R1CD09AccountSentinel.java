package com.mikeyphw.instaactions;

import java.lang.ref.WeakReference;

/** Observes ONLY reference identity of source-verified X.02xy.A04(UserSession,String).
 * Neither user IDs, usernames, token fields, nor the String argument are read.
 * Absence of callbacks is UNVERIFIED, never evidence that the account is stable.
 */
final class R1CD09AccountSentinel {
    private static WeakReference<Object> previous=new WeakReference<>(null);
    private static int observed, same, changed, missing, reclaimed;
    private static boolean seen;
    private R1CD09AccountSentinel() { }
    static synchronized void observe(Object userSession) {
        if(userSession==null){missing++;return;}
        Object old=previous.get();
        observed++;
        if(seen && old!=null) {
            if(old==userSession){same++;}
            else {
                changed++;
                R1CD07Recovery.off("UserSession reference changed; recovery disarmed");
                R1CD09DiagnosticGate.accountReferenceChanged();
            }
        } else if(seen) reclaimed++;
        seen=true;
        previous=new WeakReference<>(userSession);
    }
    static synchronized void clear() {
        previous.clear();previous=new WeakReference<>(null);
        observed=0;same=0;changed=0;missing=0;reclaimed=0;seen=false;
    }
    static synchronized boolean hasObserved(){return observed>0;}
    static synchronized String summary() {
        return "D09 ACCOUNT CHANGE SIGNAL (read-only) observed="+observed+
            " same_ref="+same+" changed_ref="+changed+" missing="+missing+
            " reclaimed="+reclaimed+" status="+(changed>0?"REFERENCE_CHANGED":
                observed==0?"NOT_OBSERVED":"OBSERVED_NO_CHANGE_IN_CALLBACKS")+
            "\nUserSession reference is weak and opaque; reference change is a signal, NOT verified identity. "+
            "No callback does NOT prove same account. OFF manually before switching; hook may not run on every switch.\n";
    }
}
