package com.mikeyphw.instaactions;

/** Testable, fail-closed decision using ONLY Android resource-name presence.
 * Feed/Reels navigation must have an independently visible main action bar.
 */
final class R1CD16AnchorPolicy {
    enum Decision { MAIN_CLASS, VERIFIED_MAIN_ANCHORS, UNKNOWN }
    static Decision qualify(boolean exactMainClass,boolean feedBar,boolean feedTab,
            boolean clipsTab,boolean profileTab,boolean truncated){
        if(exactMainClass)return Decision.MAIN_CLASS;
        if(truncated)return Decision.UNKNOWN;
        if(feedBar && (feedTab||clipsTab||profileTab))return Decision.VERIFIED_MAIN_ANCHORS;
        return Decision.UNKNOWN;
    }
    private R1CD16AnchorPolicy(){}
}
