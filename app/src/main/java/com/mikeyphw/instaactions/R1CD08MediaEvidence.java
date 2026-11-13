package com.mikeyphw.instaactions;

/**
 * D08C: evidence accounting for D06's original media/menu/pager observations.
 * This is NOT a media identity verifier: no Media references or identifiers
 * are retained, and a weak pre-menu match cannot authorize actions.
 */
final class R1CD08MediaEvidence {
    enum Verdict {
        NO_CAPTURE, NOT_REELS, OWNER_UNQUALIFIED, MENU_PAGER_MISSING,
        PREMENU_WEAK_CANDIDATE, VISIBLE_PAGE_CANDIDATE, CONTRADICTORY, INCONCLUSIVE
    }
    private static final int LIMIT=64;
    private static final int[] COUNTS=new int[Verdict.values().length];
    private static int total, capped;
    private static final java.util.Map<String,Integer> REASONS=new java.util.TreeMap<>();
    private static final int MAX_REASON_CLASSES=32;
    private R1CD08MediaEvidence() { }
    static Verdict classify(boolean capture, String screen, String owner,
            String visible, String premenu) {
        if (!capture) return Verdict.NO_CAPTURE;
        if (!"REELS".equals(screen)) return Verdict.NOT_REELS;
        boolean own="OWNER_DIRECT_MATCH".equals(owner)
                || "CONTEXT_DIRECT_MATCH".equals(owner)
                || "OWNER_AND_CONTEXT_MATCH".equals(owner);
        if (!own) return Verdict.OWNER_UNQUALIFIED;
        if ("MEDIA_MISMATCH".equals(visible) || "MEDIA_AMBIGUOUS".equals(visible)
                || "MEDIA_MISMATCH".equals(premenu)
                || "MEDIA_AMBIGUOUS".equals(premenu)
                || "PAGE_REBOUND".equals(premenu)
                || "PAGE_INDEX_CHANGED".equals(premenu)) return Verdict.CONTRADICTORY;
        if ("VISIBLE_PAGE_DIRECT_MATCH_CANDIDATE".equals(visible))
            return Verdict.VISIBLE_PAGE_CANDIDATE;
        if ("PREMENU_MEDIA_MATCH_CANDIDATE".equals(premenu))
            return Verdict.PREMENU_WEAK_CANDIDATE;
        if ("NO_VERTICAL_PAGER".equals(visible)) return Verdict.MENU_PAGER_MISSING;
        return Verdict.INCONCLUSIVE;
    }
    static synchronized void record(boolean capture,String screen,String owner,
            String visible,String premenu) {
        if (!capture || !R1CD09DiagnosticGate.active(R1CD09DiagnosticGate.Mode.MEDIA))return;
        Verdict verdict=classify(true,screen,owner,visible,premenu);
        if(total>=LIMIT){capped++;return;}
        total++;COUNTS[verdict.ordinal()]++;
        // Enum names only: no Media, raw callback args, UI strings or IDs.
        String reason="owner="+bounded(owner)+" page="+bounded(visible)+" pre="+bounded(premenu);
        if(REASONS.containsKey(reason))REASONS.put(reason,REASONS.get(reason)+1);
        else if(REASONS.size()<MAX_REASON_CLASSES)REASONS.put(reason,1);
    }
    private static String bounded(String value) {
        if(value==null)return "NULL";
        // Must be an uppercase enum token, NOT arbitrary text.
        return value.matches("[A-Z][A-Z0-9_]{0,48}")?value:"UNCLASSIFIED";
    }
    static synchronized void clear(){total=0;capped=0;java.util.Arrays.fill(COUNTS,0);REASONS.clear();}
    static synchronized String report(){
        StringBuilder out=new StringBuilder("D08C VISIBLE MEDIA IDENTITY EVIDENCE (read-only)\n");
        out.append("D08_MEDIA_IDENTITY=NOT_VERIFIED actions=OFF total=")
           .append(total).append(" capped_extra=").append(capped).append('\n');
        for(Verdict v:Verdict.values())if(COUNTS[v.ordinal()]>0)
            out.append(" ").append(v.name()).append('=').append(COUNTS[v.ordinal()]).append('\n');
        for(java.util.Map.Entry<String,Integer> e:REASONS.entrySet())
            out.append(" reason ").append(e.getKey()).append(" count=").append(e.getValue()).append('\n');
        out.append("Unique visible Media, stable pager owner and callback identity NOT established; no Media retained.\n");
        return out.toString();
    }
}
