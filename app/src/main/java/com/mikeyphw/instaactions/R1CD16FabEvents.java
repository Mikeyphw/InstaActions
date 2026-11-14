package com.mikeyphw.instaactions;

import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;
import java.util.HashSet;
import java.util.Set;

/** Bounded, content-free instrumentation shared with the injected Instagram process.
 * Never transports usernames, media, cookies, tokens, Intent data, or view text.
 * The exported app receiver verifies the sender's actual Android UID.
 */
final class R1CD16FabEvents {
    static final String ACTION="com.mikeyphw.instaactions.action.FAB_EVENT_V1";
    static final String EXTRA_EVENT="event";
    static final String EXTRA_DETAIL="detail";
    static final String RECEIVER="com.mikeyphw.instaactions.R1CD16LogReceiver";
    static final int MAX_EVENTS_PER_PROCESS=256;
    static final int MAX_DETAIL_CHARS=120;
    private static int emitted;
    private static final Set<String> CODES=new HashSet<>();
    static {
        for(String c:new String[]{"RESUME_DISPATCH","FAB_ACTIVITY_SKIP","FAB_ATTEMPT",
                "FAB_STATUS","FAB_ROOT","FAB_VISIBLE","FAB_FAILURE","FAB_DETACH",
                "HOOK_READY","HOOK_ERROR","BRIDGE_READY","FRAMEWORK_RESUME"})CODES.add(c);
    }
    static boolean allowed(String code){return code!=null && CODES.contains(code);}
    static boolean validDetail(String detail){
        return detail!=null && detail.length()>0 && detail.length()<=MAX_DETAIL_CHARS
                && detail.matches("[A-Za-z0-9_.$:=,;\\-]+");
    }
    private static String safe(String data){
        if(data==null || data.isEmpty())return "NONE";
        String s=data.replaceAll("[^A-Za-z0-9_.$:=,;\\-]","_");
        return s.substring(0,Math.min(s.length(),MAX_DETAIL_CHARS));
    }
    static void emit(Context context,String code,String detail){
        if(!allowed(code))return;
        String filtered=safe(detail);
        Log.i("INA_FAB", "INA_FAB_EVIDENCE "+code+" detail="+filtered);
        if(context==null || !ModuleConstants.TARGET_PACKAGE.equals(context.getPackageName()))return;
        synchronized(R1CD16FabEvents.class){
            if(emitted>=MAX_EVENTS_PER_PROCESS)return;
            emitted++;
        }
        try {
            Intent event=new Intent(ACTION);
            event.setClassName("com.mikeyphw.instaactions",RECEIVER);
            event.putExtra(EXTRA_EVENT,code);
            event.putExtra(EXTRA_DETAIL,filtered);
            event.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
            context.sendBroadcast(event);
        }catch(Throwable failure){
            Log.w("INA_FAB","INA_FAB_BRIDGE_SEND_FAILED type="+failure.getClass().getSimpleName());
        }
    }
    private R1CD16FabEvents(){}
}
