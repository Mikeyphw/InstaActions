package com.mikeyphw.instaactions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** D18: same secure D17 live PID matching PLUS distinct historical, unverified
 * marker categories. Never stores raw logcat, foreign-PID details or account data.
 */
final class R1CD17RootLogParser {
    static final int MAX_REPORT_EVENTS = 72;
    static final int MAX_HISTORICAL_MARKERS = 16;
    private static final Pattern THREADTIME = Pattern.compile(
            "^\\d\\d-\\d\\d\\s+\\d\\d:\\d\\d:\\d\\d\\.\\d{3}\\s+(\\d{1,10})\\s+\\d{1,10}\\s+[VDIWEF]\\s+(INA_FAB|INA_DIAG|INA_R1B|INA_R1C)\\s*:\\s*(.*)$");
    private static final Pattern MARKER = Pattern.compile(
            "\\b(INA_(?:FAB|DIAG|R1B|R1C)_[A-Z][A-Z0-9_]{2,60})\\b");
    private static final Pattern FAB_EVENT = Pattern.compile(
            "\\b(FAB_ATTEMPT|FAB_STATUS|FAB_ROOT|FAB_VISIBLE|FAB_FAILURE|FAB_DETACH|FAB_ACTIVITY_SKIP|HOOK_READY|HOOK_ERROR|BRIDGE_READY|FRAMEWORK_RESUME|RESUME_DISPATCH)\\b");
    private static final Pattern SAFE_DETAIL = Pattern.compile("\\bdetail=([A-Za-z0-9_.$:=,;\\-]{1,120})");
    private static final Pattern SAFE_TYPE = Pattern.compile("\\btype=([A-Za-z][A-Za-z0-9_.$]{0,60})");
    private static final Pattern PS_NAME = Pattern.compile(
            "^\\s*([0-9]{1,9})\\s+(com\\.instagram\\.android(?:[:][A-Za-z0-9._-]+)?)\\s*$");

    static final class Result {
        final boolean rootGranted;
        final boolean processSeen;
        final int accepted, otherPid, unclassified;
        final String report;
        Result(boolean rootGranted, boolean processSeen, int accepted, int otherPid,
               int unclassified, String report) {
            this.rootGranted=rootGranted; this.processSeen=processSeen;
            this.accepted=accepted; this.otherPid=otherPid;
            this.unclassified=unclassified; this.report=report;
        }
    }

    static Result parse(String raw, boolean timedOut, int exitCode, boolean truncated) {
        if (raw == null) raw="";
        String[] lines=raw.split("\\r?\\n");
        boolean root=false, uidSeen=false, inUid=false, inPids=false, inPs=false, inLogs=false;
        boolean end=false;
        Set<Integer> pids=new HashSet<>(); // live/main pids from either command
        Set<Integer> pidofPids=new HashSet<>();
        Set<Integer> psMainPids=new HashSet<>();
        Set<Integer> psAuxPids=new HashSet<>();
        int accepted=0, foreign=0, unclassified=0, fab=0, hook=0, historical=0;
        List<String> records=new ArrayList<>();
        List<String> historicalMarkers=new ArrayList<>();
        for(String line:lines) {
            if("INA_D17_UID".equals(line)) {inUid=true;inPids=false;inPs=false;inLogs=false;continue;}
            if("INA_D17_PID".equals(line)) {inUid=false;inPids=true;inPs=false;inLogs=false;continue;}
            if("INA_D18_PS".equals(line)) {inUid=false;inPids=false;inPs=true;inLogs=false;continue;}
            if("INA_D17_LOGCAT".equals(line)) {inUid=false;inPids=false;inPs=false;inLogs=true; pids.addAll(pidofPids);pids.addAll(psMainPids);continue;}
            if("INA_D17_END".equals(line)) {inLogs=false;end=true;continue;}
            if(inUid) {uidSeen=true;root="0".equals(line.trim());inUid=false;continue;}
            if(inPids) {
                for(String token:line.trim().split("\\s+")) {
                    if(token.matches("[0-9]{1,9}")) {
                        try {int pid=Integer.parseInt(token);if(pid>1)pidofPids.add(pid);}
                        catch(NumberFormatException ignored) { }
                    }
                }
                inPids=false;
                continue;
            }
            if(inPs) {
                Matcher m=PS_NAME.matcher(line);
                if(m.matches()) {
                    try {
                        int pid=Integer.parseInt(m.group(1));
                        if(pid>1) {
                            if("com.instagram.android".equals(m.group(2)))psMainPids.add(pid);
                            else psAuxPids.add(pid);
                        }
                    }catch(NumberFormatException ignored) { }
                }
                continue;
            }
            if(!inLogs || !root)continue;
            Matcher log=THREADTIME.matcher(line);
            if(!log.matches()) {
                if(line.contains("INA_FAB")||line.contains("INA_DIAG"))unclassified++;
                continue;
            }
            int pid;
            try{pid=Integer.parseInt(log.group(1));}
            catch(NumberFormatException ignored){unclassified++;continue;}
            String msg=log.group(3);
            Matcher marker=MARKER.matcher(msg);
            if(!marker.find()){unclassified++;continue;}
            String event=marker.group(1);
            if("INA_FAB_EVIDENCE".equals(event)) {
                Matcher specific=FAB_EVENT.matcher(msg);
                if(specific.find()) event="INA_FAB_"+specific.group(1);
            }
            // A foreign PID can be an old Instagram process, or another app.
            // Preserve only the fixed marker code, NEVER its detail/type or PID.
            if(!pids.contains(pid)) {
                foreign++; historical++;
                if(historicalMarkers.size()>=MAX_HISTORICAL_MARKERS)historicalMarkers.remove(0);
                historicalMarkers.add(event);
                continue;
            }
            accepted++;
            if(event.startsWith("INA_FAB_"))fab++;
            if(event.startsWith("INA_DIAG_") || event.contains("HOOK"))hook++;
            if(records.size()>=MAX_REPORT_EVENTS)records.remove(0);
            Matcher detail=SAFE_DETAIL.matcher(msg);
            Matcher type=SAFE_TYPE.matcher(msg);
            records.add(event+(detail.find()?" detail="+detail.group(1):
                    type.find()?" type="+type.group(1):""));
        }
        pids.addAll(pidofPids);pids.addAll(psMainPids);
        String state;
        if(timedOut)state="ROOT_PROBE_TIMEOUT";
        else if(!root || !uidSeen)state="ROOT_NOT_GRANTED_OR_SU_UNAVAILABLE";
        else if(!end)state="ROOT_OUTPUT_INCOMPLETE";
        else if(pids.isEmpty())state="INSTAGRAM_PROCESS_NOT_RUNNING";
        else if(accepted==0)state="NO_MATCHING_INSTACTIONS_MARKERS_IN_LOG_WINDOW";
        else if(fab==0)state="NO_FAB_MARKERS_FROM_CURRENT_INSTAGRAM_PID";
        else state="READ_ONLY_ROOT_CAPTURE_AVAILABLE";
        StringBuilder b=new StringBuilder(4096);
        b.append("INA D18 AUTOMATIC ROOT FAB DIAGNOSTICS\n")
         .append("status=").append(state).append('\n')
         .append("root_uid_zero=").append(root).append(" instagram_main_pid_count=").append(pids.size()).append('\n')
         .append("pidof_main_count=").append(pidofPids.size())
         .append(" ps_main_count=").append(psMainPids.size())
         .append(" ps_aux_count=").append(psAuxPids.size()).append('\n')
         .append("pid_discovery_mismatch=").append(!pidofPids.equals(psMainPids)).append('\n')
         .append("matching_pid_events=").append(accepted).append(" fab_events=").append(fab)
         .append(" hook_related_events=").append(hook).append('\n')
         .append("other_pid_tag_lines_ignored=").append(foreign)
         .append(" historical_unverified_markers=").append(historical)
         .append(" unclassified_lines=").append(unclassified)
         .append(" raw_capture_truncated=").append(truncated).append('\n')
         .append("su_exit_code=").append(exitCode).append(" root_timeout=").append(timedOut).append('\n')
         .append("event_details=FIXED_MARKERS_ONLY_AND_PID_MATCHED\n")
         .append("No media, account IDs, usernames, tokens, URLs or raw logcat are stored.\n")
         .append("No matching markers does NOT prove hooks were never loaded; log rotation and timing matter.\n");
        if(!pids.isEmpty() && pidofPids.isEmpty() && !psMainPids.isEmpty())
            b.append("ROOT_DIAGNOSTIC=PIDOF_MISSED_PROCESS_PS_FALLBACK\n");
        if(pids.isEmpty() && !psAuxPids.isEmpty())
            b.append("ROOT_DIAGNOSTIC=AUXILIARY_PROCESSES_ONLY_MAIN_NOT_FOUND\n");
        if(!records.isEmpty()){
            b.append("Live PID-verified sanitized events (oldest to newest; cap=")
             .append(MAX_REPORT_EVENTS).append("):\n");
            for(String e:records)b.append(e).append('\n');
        }
        if(!historicalMarkers.isEmpty()){
            b.append("HISTORICAL_UNVERIFIED_MARKERS_ONLY (not proof of Instagram origin, last ")
             .append(MAX_HISTORICAL_MARKERS).append("):\n");
            for(String e:historicalMarkers)b.append(e).append('\n');
        }
        return new Result(root,pids.size()>0,accepted,foreign,unclassified,b.toString());
    }
    private R1CD17RootLogParser() { }
}
