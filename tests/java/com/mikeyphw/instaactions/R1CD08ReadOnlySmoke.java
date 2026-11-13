package com.mikeyphw.instaactions;

/** D08 pure-Java regression: no media/account/key retention or action grants. */
public final class R1CD08ReadOnlySmoke {
    private static int checks;
    private static void ok(boolean cond,String msg){if(!cond)throw new AssertionError(msg);checks++;}
    private static void frame(int cohort, Object key, Object original) {
        R1CD07PredicateTrace.Frame f=R1CD07PredicateTrace.enter(cohort,
                R1CD07InputPresence.NESTED_PRESENT,R1CD07BranchInputs.PRESENT);
        R1CD07PredicateTrace.noteDataReceiver(new Object(), Object.class);
        R1CD07PredicateTrace.noteExactData(key,original);
        R1CD07PredicateTrace.leave(f,new Object()); // other result, synthetic fixture
    }
    public static void main(String[] args) {
        R1CD07Recovery.off("read only smoke");
        R1CD07ExactGateTrial.reset();
        R1CD07PredicateTrace.clear();
        ok(!R1CD08MediaEvidence.classify(false,"REELS", "OWNER_DIRECT_MATCH",
            "VISIBLE_PAGE_DIRECT_MATCH_CANDIDATE", "PREMENU_MEDIA_MATCH_CANDIDATE")
            .equals(R1CD08MediaEvidence.Verdict.VISIBLE_PAGE_CANDIDATE),"capture required");
        ok(R1CD08MediaEvidence.classify(true,"REELS","OWNER_DIRECT_MATCH",
                "NO_VERTICAL_PAGER","PREMENU_MEDIA_MATCH_CANDIDATE")
                == R1CD08MediaEvidence.Verdict.PREMENU_WEAK_CANDIDATE,"weak candidate distinct");
        ok(R1CD08MediaEvidence.classify(true,"REELS","OWNER_DIRECT_MATCH",
                "NO_VERTICAL_PAGER","PAGE_REBOUND")
                == R1CD08MediaEvidence.Verdict.CONTRADICTORY,"stale view revoked");
        ok(R1CD08MediaEvidence.classify(true,"HOME_FEED","OWNER_DIRECT_MATCH",
                "VISIBLE_PAGE_DIRECT_MATCH_CANDIDATE","PREMENU_MEDIA_MATCH_CANDIDATE")
                == R1CD08MediaEvidence.Verdict.NOT_REELS,"wrong surface refuses");
        R1CD09DiagnosticGate.begin(R1CD09DiagnosticGate.Mode.MEDIA);
        R1CD08MediaEvidence.record(true,"REELS","OWNER_DIRECT_MATCH",
                "NO_VERTICAL_PAGER","PREMENU_MEDIA_MATCH_CANDIDATE");
        ok(R1CD08MediaEvidence.report().contains("PREMENU_WEAK_CANDIDATE=1"),"counts classified");
        ok(R1CD08MediaEvidence.report().contains("D08_MEDIA_IDENTITY=NOT_VERIFIED"),"no grant");
        frame(0,0xdf82522e,Boolean.FALSE);
        frame(1,0xdf82522e,Boolean.TRUE);
        frame(0,0xdf82522e,null);
        frame(1,0xdf82522f,Boolean.FALSE); // wrong key result must never be output
        String d=R1CD07PredicateTrace.d08ExactConfigReport();
        ok(d.contains("A other frames=2"),"A frame provenance");
        ok(d.contains("B other frames=2"),"B frame provenance");
        ok(d.contains("original_false=1"),"native A false retained");
        ok(d.contains("original_true=1"),"native B true retained");
        ok(d.contains("original_null=1"),"nullable original retained");
        ok(d.contains("other_key_calls=1"),"other keys counted only");
        ok(d.contains("receiver_other=2"),"receiver only class code category");
        ok(!d.contains("0xdf82522f") && !d.contains("214748") &&
                !d.contains("com.instagram."),"no key values or media exported");
        R1CD07PredicateTrace.clear();
        ok(!R1CD07PredicateTrace.d08ExactConfigReport().contains("other_key_calls=1"),"trace resets");
        R1CD08MediaEvidence.clear();
        ok(!R1CD08MediaEvidence.report().contains("PREMENU_WEAK_CANDIDATE=1"),"identity resets");
        System.out.println("INA_D08_READONLY_NEGATIVES_PASS="+checks);
    }
}
