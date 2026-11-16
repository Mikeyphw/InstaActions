package com.mikeyphw.instaactions;

import android.content.Context;
import android.content.SharedPreferences;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** Module-app-private bounded diagnostic journal; not shared with Instagram's UID.
 * Records only category codes and sanitized Android-class/reason hints.
 * Historical lines are intentionally capped rather than promising full logcat.
 */
final class R1CD16LogStore {
    private static final String NAME="ina_fab_diagnostics_v1";
    private static final String LINES="lines";
    private static final String COUNT="events";
    static final int MAX_LINES=180;
    static final int MAX_BYTES=24000;
    static synchronized void append(Context context,String code,String detail){
        if(!R1CD16FabEvents.allowed(code) || !R1CD16FabEvents.validDetail(detail))return;
        SharedPreferences p=context.getSharedPreferences(NAME,Context.MODE_PRIVATE);
        String existing=p.getString(LINES,"");
        String now=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.ROOT).format(new Date());
        String line=now+" "+code+" "+detail;
        String combined=existing.isEmpty()?line:existing+"\n"+line;
        String[] lines=combined.split("\\n");
        int from=Math.max(0,lines.length-MAX_LINES);
        StringBuilder out=new StringBuilder();
        for(int i=from;i<lines.length;i++){
            if(out.length()>0)out.append('\n');
            out.append(lines[i]);
        }
        while(out.length()>MAX_BYTES){
            int first=out.indexOf("\n");
            if(first<0){out.setLength(0);break;}
            out.delete(0,first+1);
        }
        p.edit().putString(LINES,out.toString()).putInt(COUNT,p.getInt(COUNT,0)+1).apply();
    }
    static synchronized String report(Context context){
        SharedPreferences p=context.getSharedPreferences(NAME,Context.MODE_PRIVATE);
        String lines=p.getString(LINES,"");
        int received=p.getInt(COUNT,0);
        int retained=lines.isEmpty()?0:lines.split("\\n").length;
        return "INA D16 FAB DIAGNOSTICS · Android 16\n"
                +"Cross-process Instagram events received="+received
                +" retained="+retained+" cap="+MAX_LINES+"\n"
                +"Source identity: Android sender UID verified per event\n"
                +"Capture is bounded; earlier events may have been evicted. This is not logcat.\n"
                +"No events DOES NOT prove the module hook was never installed; broadcast delivery may fail.\n"
                +"No usernames, media, captions, cookies, account identifiers or view text collected.\n\n"
                +(retained==0?"NO EVENTS RECEIVED. Verify Vector scope and inspect INA_DIAG / INA_FAB in logcat.\n":lines+"\n");
    }
    static synchronized void clear(Context context){
        context.getSharedPreferences(NAME,Context.MODE_PRIVATE).edit().clear().apply();
    }
    private R1CD16LogStore(){}
}
