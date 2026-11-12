package com.mikeyphw.instaactions;

/** Deterministic Android-free instruction and ordering smoke. */
public final class R1CD07UxGuideSmoke {
    private static int checks;
    private static void contains(String value,String mark) {
        if(!value.contains(mark)) throw new AssertionError(mark + " missing in: " + value);
        checks++;
    }
    private static void eq(int value,int expected) {
        if(value!=expected)throw new AssertionError("step="+value+" expected="+expected);
        checks++;
    }
    private static String next(boolean on,boolean hooks,boolean data,boolean reels,
            boolean a,boolean fresh,boolean ack) {
        return R1CD07UxGuide.next(on,hooks,data,reels,a,fresh,ack);
    }
    private static int step(boolean on,boolean hooks,boolean data,boolean reels,
            boolean a,boolean fresh,boolean ack) {
        return R1CD07UxGuide.step(on,hooks,data,reels,a,fresh,ack);
    }
    public static void main(String[] args) {
        eq(step(false,false,false,false,false,false,false),1);
        eq(step(false,true,false,false,false,false,false),1);
        eq(step(false,true,true,false,false,false,false),1);
        eq(step(false,true,true,true,false,false,false),2);
        eq(step(false,true,true,true,true,false,false),3);
        eq(step(false,true,true,true,true,true,false),4);
        eq(step(false,true,true,true,true,true,true),4);
        eq(step(true,false,false,false,false,false,false),5);
        contains(next(false,false,false,false,false,false,false),"STEP 1 OF 5");
        contains(next(false,true,false,false,false,false,false),"data hook not ready");
        contains(next(false,true,true,false,false,false,false),"dedicated Reels tab");
        contains(next(false,true,true,true,false,false,false),"Start A ONCE");
        contains(next(false,true,true,true,true,false,false),"Keep A recording");
        contains(next(false,true,true,true,true,true,false),"THEN tap Enable");
        contains(next(false,true,true,true,true,true,true),"Tap Enable (4B) NOW");
        contains(next(true,true,true,true,true,true,true),"Recovery OFF");
        contains(R1CD07UxGuide.checklist(true,true,true,true,false,false),"WAIT");
        contains(R1CD07UxGuide.checklist(true,true,true,true,true,true),"YES (manual)");
        contains(R1CD07UxGuide.shortState(true,false,false),"RECOVERY ON");
        contains(R1CD07UxGuide.shortState(false,true,false),"OBSERVING A");
        contains(R1CD07UxGuide.shortState(false,false,true),"OBSERVING B");
        contains(R1CD07UxGuide.shortState(false,false,false),"RECOVERY OFF");
        System.out.println("INA_D07_UX02_GUIDANCE_PASS="+checks);
    }
}
