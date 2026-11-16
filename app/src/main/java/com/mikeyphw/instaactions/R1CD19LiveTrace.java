package com.mikeyphw.instaactions;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** D19 read-only stream parser: live process observation + bounded log marker categories.
 * No usernames, account/media IDs, raw log lines, Intent payload, or PIDs in output.
 * Every claim of a matching event requires an Instagram PID SEEN DURING this capture,
 * unlike the D17/D18 after-the-fact current-PID snapshot.
 */
final class R1CD19LiveTrace {
    static final int MAX_EVENTS = 72;
    private static final Pattern PID = Pattern.compile("^INA_D19_MAIN_PID=([0-9]{1,9})$");
    private static final Pattern THREADTIME = Pattern.compile(
            "^\\d\\d-\\d\\d\\s+\\d\\d:\\d\\d:\\d\\d\\.\\d{3}\\s+([0-9]{1,10})\\s+[0-9]{1,10}\\s+[VDIWEF]\\s+(INA_FAB|INA_DIAG|INA_R1B|INA_R1C)\\s*:\\s*(.*)$");
    private static final Pattern MARKER = Pattern.compile(
            "\\b(INA_(?:FAB|DIAG|R1B|R1C)_[A-Z][A-Z0-9_]{2,60})\\b");
    private static final Pattern FAB_DETAIL = Pattern.compile(
            "\\b(FAB_ATTEMPT|FAB_STATUS|FAB_ROOT|FAB_VISIBLE|FAB_FAILURE|FAB_DETACH|FAB_ACTIVITY_SKIP|HOOK_READY|HOOK_ERROR|BRIDGE_READY|FRAMEWORK_RESUME|RESUME_DISPATCH)\\b");
    private final Set<Integer> observedPids=new HashSet<>();
    private final Set<Integer> pollPids=new HashSet<>();
    private final ArrayDeque<String> events=new ArrayDeque<>();
    private boolean uidHeader, uidZero, started, ended, everSawMain, lastPollMain;
    private int polls, mainPolls, disappearances, mainPidChanges, auxPolls;
    private int observedPidEvents, unverifiedMarkers, unclassifiedLines, totalLines, eventEvictions;
    private int acceptedFab, acceptedHooks;

    synchronized void accept(String line) {
        if(line==null)return;
        if(totalLines < 1000000)totalLines++;
        if("INA_D19_UID".equals(line)){uidHeader=true;return;}
        if(uidHeader){uidHeader=false;uidZero="0".equals(line.trim());return;}
        if("INA_D19_MONITOR_BEGIN".equals(line)){started=true;return;}
        if("INA_D19_MONITOR_END".equals(line)){ended=true;return;}
        if("INA_D19_POLL".equals(line)){
            if(polls>0 && lastPollMain && pollPids.isEmpty())disappearances++;
            if(polls>0 && !pollPids.isEmpty() && !pollPids.equals(observedPids))mainPidChanges++;
            lastPollMain=!pollPids.isEmpty();
            pollPids.clear();
            if(polls<1000)polls++;
            return;
        }
        Matcher id=PID.matcher(line);
        if(id.matches()) {
            try {
                int value=Integer.parseInt(id.group(1));
                if(value>1) {
                    if(pollPids.isEmpty() && mainPolls < 1000)mainPolls++;
                    observedPids.add(value);pollPids.add(value);everSawMain=true;
                }
            }catch(NumberFormatException ignored) { }
            return;
        }
        if("INA_D19_AUX_PROCESS".equals(line)){if(auxPolls<1000)auxPolls++;return;}
        Matcher log=THREADTIME.matcher(line);
        if(!log.matches())return;
        if(!uidZero)return;
        int pid;
        try{pid=Integer.parseInt(log.group(1));}catch(NumberFormatException ignored){unclassifiedLines++;return;}
        Matcher marker=MARKER.matcher(log.group(3));
        if(!marker.find()){unclassifiedLines++;return;}
        String event=marker.group(1);
        if("INA_FAB_EVIDENCE".equals(event)) {
            Matcher subtype=FAB_DETAIL.matcher(log.group(3));
            if(subtype.find())event="INA_FAB_"+subtype.group(1);
        }
        boolean verified=observedPids.contains(pid);
        if(verified) {
            observedPidEvents++;
            if(event.startsWith("INA_FAB_"))acceptedFab++;
            if(event.startsWith("INA_DIAG_")||event.contains("HOOK"))acceptedHooks++;
        } else unverifiedMarkers++;
        if(events.size()>=MAX_EVENTS){events.removeFirst();eventEvictions++;}
        events.addLast((verified?"OBSERVED_INSTAGRAM_PID ":"UNVERIFIED_PID ")+event);
    }

    synchronized String report(String state) {
        StringBuilder b=new StringBuilder(2048);
        b.append("INA D19 LIVE ROOT FAB MONITOR\nstatus=").append(state).append('\n')
         .append("root_uid_zero=").append(uidZero).append(" capture_started=").append(started)
         .append(" capture_ended=").append(ended).append('\n')
         .append("instagram_main_seen_during_capture=").append(everSawMain)
         .append(" polls=").append(polls).append(" polls_with_main=").append(mainPolls)
         .append(" main_process_exit_observations=").append(disappearances)
         .append(" auxiliary_process_observations=").append(auxPolls).append('\n')
         .append("observed_pid_tag_events=").append(observedPidEvents)
         .append(" fab_events=").append(acceptedFab)
         .append(" hook_events=").append(acceptedHooks)
         .append(" unverified_pid_markers=").append(unverifiedMarkers)
         .append(" events_evicted=").append(eventEvictions).append('\n')
         .append("PID attribution means the PID was observed during this window; it does not establish account or media identity.\n")
         .append("Only event categories are saved; no PIDs, names, URLs, tokens, raw logcat or view content.\n");
        if(!everSawMain)b.append("NEXT=Open Instagram's Home Feed while automatic monitor is running.\n");
        else if(observedPidEvents==0)b.append("NEXT=Instagram appeared, but no INA diagnostic markers were correlated. Check Vector module scope and hook installation.\n");
        for(String event:events)b.append(event).append('\n');
        return b.toString();
    }
    synchronized boolean rootGranted(){return uidZero;}
    synchronized boolean sawInstagram(){return everSawMain;}
    synchronized int observedEvents(){return observedPidEvents;}
    private R1CD19LiveTrace() { }
    R1CD19LiveTrace(boolean ignored) { }
}
