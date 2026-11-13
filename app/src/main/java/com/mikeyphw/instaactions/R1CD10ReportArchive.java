package com.mikeyphw.instaactions;

import java.util.ArrayDeque;

/** In-memory archive of bounded, sanitized diagnostic summaries for one Instagram process.
 * No logcat access, disk persistence, remote uploads or account identifiers.
 */
final class R1CD10ReportArchive {
    private static final int MAX_RECORDS=28, MAX_TOTAL_CHARS=180_000, MAX_RECORD_CHARS=16_000;
    private static final ArrayDeque<String> records=new ArrayDeque<>();
    private static int chars, saved, dropped;
    private R1CD10ReportArchive() { }
    static synchronized void capture(String mode,String phase,String evidence) {
        String data=evidence==null?"":evidence;
        if(data.length()>MAX_RECORD_CHARS)data=data.substring(0,MAX_RECORD_CHARS)+
                "\nTRUNCATED_RECORD_LIMIT\n";
        String v="\n--- ARCHIVED #"+(++saved)+" mode="+mode+" phase="+phase+" ---\n"+data+"\n";
        while(!records.isEmpty()&&(records.size()>=MAX_RECORDS || chars+v.length()>MAX_TOTAL_CHARS)) {
            chars-=records.removeFirst().length();dropped++;
        }
        if(v.length()>MAX_TOTAL_CHARS){dropped++;return;}
        records.addLast(v);chars+=v.length();
    }
    static synchronized String all(String currentMode,String currentReport) {
        StringBuilder out=new StringBuilder("INSTAACTIONS ALL DIAGNOSTICS · current Instagram process only\n");
        out.append("This includes module-collected bounded reports, NOT full Android logcat. ")
           .append("No persistence across process restarts.\n")
           .append("ARCHIVE entries=").append(records.size()).append(" lost_oldest=").append(dropped)
           .append(" chars_stored=").append(chars).append(" total_created=").append(saved).append("\n")
           .append("D06_MEDIA_IDENTITY=NOT_VERIFIED ACTIONS=OFF\n")
           .append(R1CD10PassiveLedger.report());
        for(String v:records)out.append(v);
        if(currentReport!=null && !currentReport.isEmpty()) {
            out.append("\n--- LIVE diagnostic ").append(currentMode).append(" (unarchived) ---\n")
               .append(currentReport);
        }
        return out.toString();
    }
}
