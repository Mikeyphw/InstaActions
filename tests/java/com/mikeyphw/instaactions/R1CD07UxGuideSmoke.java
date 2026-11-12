package com.mikeyphw.instaactions;

/** Android-free UX decision model, no Repost mutation paths. */
public final class R1CD07UxGuideSmoke {
    private static int checks;
    private static void contains(String actual, String expected) {
        if (!actual.contains(expected)) throw new AssertionError(expected + " absent from " + actual);
        checks++;
    }
    private static String next(boolean on, boolean hook, boolean data, boolean reels,
            boolean a, boolean fresh, boolean acknowledged) {
        return R1CD07UxGuide.next(on, hook, data, reels, a, fresh, acknowledged);
    }
    public static void main(String[] args) {
        contains(next(true,false,false,false,false,false,false),"Recovery is ON");
        contains(next(false,false,false,false,false,false,false),"Open Instagram 455");
        contains(next(false,true,false,false,false,false,false),"Native data hook not ready");
        contains(next(false,true,true,false,false,false,false),"dedicated Reels tab");
        contains(next(false,true,true,true,false,false,false),"Tap Start A");
        contains(next(false,true,true,true,true,false,false),"Do not press Stop A/B");
        contains(next(false,true,true,true,true,true,false),"Turn OFF InstaTaskManager");
        contains(next(false,true,true,true,true,true,true),"Tap Enable native Repost");
        contains(R1CD07UxGuide.shortState(true,false,false),"RECOVERY ON");
        contains(R1CD07UxGuide.shortState(false,true,false),"OBSERVING A");
        contains(R1CD07UxGuide.shortState(false,false,true),"OBSERVING B");
        contains(R1CD07UxGuide.shortState(false,false,false),"RECOVERY OFF");
        System.out.println("INA_D07_UX01_GUIDANCE_PASS="+checks);
    }
}
