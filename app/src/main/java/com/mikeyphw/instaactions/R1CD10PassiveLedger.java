package com.mikeyphw.instaactions;

/** Always-on, process-lifetime aggregate of ALREADY INSTALLED passive callbacks.
 * Does not install hooks or inspect a user/media/object. No background polling.
 */
final class R1CD10PassiveLedger {
    enum Signal { UI_REPOST, UFI_REPOST, UFI_COUNT, MEDIA_MENU, SESSION_CALLBACK, ACTIVITY_RESUME, ACTIVITY_LEAVE, REELS_HINT, FEED_HINT, OTHER_HINT }
    private static final long[] counts=new long[Signal.values().length];
    private static final long MAX=100000;
    private static final long start=System.nanoTime();
    private static long overflow;
    private static String lastScreen="";
    private R1CD10PassiveLedger() { }
    static synchronized void observe(Signal type){
        int i=type.ordinal();if(counts[i]>=MAX){overflow++;return;}counts[i]++;
    }
    static synchronized void observeScreen(String screen){
        if(screen==null || screen.equals(lastScreen))return;
        lastScreen=screen;
        observe("REELS".equals(screen)?Signal.REELS_HINT:
            "HOME_FEED".equals(screen)?Signal.FEED_HINT:Signal.OTHER_HINT);
    }
    static synchronized String report(){
        StringBuilder out=new StringBuilder("D10 PASSIVE DIAGNOSTICS · AUTO ON (process session)\n");
        out.append("Uptime_seconds=").append((System.nanoTime()-start)/1_000_000_000L)
            .append("; callbacks are aggregate metadata, NOT account/media proof\n");
        for(Signal s:Signal.values())out.append(' ').append(s.name()).append('=')
            .append(counts[s.ordinal()]).append('\n');
        out.append(" dropped_due_to_cap=").append(overflow)
           .append("\nPassive monitoring uses existing hooks; NO auto-repost, NO auto-click, NO usernames, media IDs or captions.\n");
        return out.toString();
    }
}
