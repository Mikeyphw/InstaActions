package com.mikeyphw.instaactions;

/** Pure, testable conservative pre-menu snapshot freshness/position policy. */
final class R1CD06PreMenuPolicy {
    private R1CD06PreMenuPolicy() { }
    static boolean expired(long captured,long now,long maxAge) {
        return captured < 0 || now < captured || maxAge <= 0 || now-captured > maxAge;
    }
    static boolean samePosition(int sampled,int live,int adapter) {
        return sampled >= 0 && sampled == live && sampled == adapter;
    }
}
