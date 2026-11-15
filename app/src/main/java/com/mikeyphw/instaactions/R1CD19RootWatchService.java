package com.mikeyphw.instaactions;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ServiceInfo;
import android.os.IBinder;
import android.os.Build;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/** D19: short, automatic foreground service captures while the USER opens Instagram.
 * Foreground notification is required to survive the app-to-Instagram transition.
 * The root shell only reads Android process state and tagged logcat; it never
 * changes Instagram, module configuration, permissions or private user data.
 * No raw log lines are persisted. No repeated consent in the app; KernelSU rules apply.
 */
public final class R1CD19RootWatchService extends Service {
    private static final String PREFS="ina_d19_live_fab_v1";
    private static final String REPORT="report", LAST="last_run_at";
    private static final long MIN_RESTART_MS=210_000L;
    private static final long MAX_CAPTURE_MS=174_000L;
    private static final int NOTIFICATION_ID=19019;
    private static final String CHANNEL="ina_d19_diagnostics";
    private static final Object LOCK=new Object();
    private static boolean busy;

    // Explicitly fixed, read-only commands and a finite 150-second diagnostic.
    // kill applies ONLY to the logcat child created by this shell, never Instagram.
    private static final String MONITOR_COMMAND =
            "printf 'INA_D19_UID\\n'; id -u; " +
            "printf 'INA_D19_MONITOR_BEGIN\\n'; " +
            "logcat -v threadtime -T 1 -s INA_FAB:V INA_DIAG:V INA_R1B:V INA_R1C:V '*:S' 2>/dev/null & logger=$!; " +
            "i=0; while [ \"$i\" -lt 75 ]; do " +
            "i=$((i+1)); printf 'INA_D19_POLL\\n'; " +
            "for p in $(pidof com.instagram.android 2>/dev/null); do printf 'INA_D19_MAIN_PID=%s\\n' \"$p\"; done; " +
            "ps -A -o PID,NAME 2>/dev/null | while read p name; do " +
            "case \"$name\" in com.instagram.android) printf 'INA_D19_MAIN_PID=%s\\n' \"$p\";; " +
            "com.instagram.android:*) printf 'INA_D19_AUX_PROCESS\\n';; esac; done; " +
            "sleep 2; done; " +
            "kill \"$logger\" 2>/dev/null; wait \"$logger\" 2>/dev/null; " +
            "printf 'INA_D19_MONITOR_END\\n'";

    static boolean shouldStart(Context context) {
        SharedPreferences p=context.getSharedPreferences(PREFS,Context.MODE_PRIVATE);
        long previous=p.getLong(LAST,0);
        long now=System.currentTimeMillis();
        synchronized(LOCK){
            if(busy || (previous>0 && now>=previous && now-previous<MIN_RESTART_MS))return false;
            p.edit().putLong(LAST,now).apply();
            return true;
        }
    }
    static String report(Context context) {
        String previous=context.getSharedPreferences(PREFS,Context.MODE_PRIVATE)
                .getString(REPORT,"INA D19 LIVE ROOT FAB MONITOR\nstatus=NOT_STARTED\n");
        synchronized(LOCK){return busy?"D19_CAPTURE_RUNNING_BACKGROUND\n"+previous:previous;}
    }
    static void startupFailure(Context context,Throwable problem) {
        save(context,"INA D19 LIVE ROOT FAB MONITOR\nstatus=FOREGROUND_SERVICE_START_FAILED type="
                +problem.getClass().getSimpleName()+"\nNo Instagram change attempted.\n");
    }
    static void clear(Context context) {
        context.getSharedPreferences(PREFS,Context.MODE_PRIVATE).edit().remove(REPORT).apply();
    }
    private static void save(Context c,String content) {
        c.getSharedPreferences(PREFS,Context.MODE_PRIVATE).edit().putString(REPORT,content).apply();
    }

    @Override public IBinder onBind(Intent intent){return null;}
    @Override public int onStartCommand(Intent intent,int flags,int startId) {
        try {beginForeground();}
        catch(RuntimeException notAllowed) {
            startupFailure(this,notAllowed);stopSelfResult(startId);return START_NOT_STICKY;
        }
        synchronized(LOCK){
            if(busy){stopSelfResult(startId);return START_NOT_STICKY;}
            busy=true;
        }
        final Context context=getApplicationContext();
        Thread worker=new Thread(()->{
            try {capture(context);}
            catch(Throwable unexpected){startupFailure(context,unexpected);}
            finally {
                synchronized(LOCK){busy=false;}
                stopSelfResult(startId);
            }
        },"ina-d19-live-root-fab-monitor");
        worker.setDaemon(true);
        worker.start();
        return START_NOT_STICKY;
    }
    private void beginForeground() {
        NotificationManager nm=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        if(nm!=null && Build.VERSION.SDK_INT>=26) {
            NotificationChannel ch=new NotificationChannel(CHANNEL,
                    "InstaActions diagnostic capture",NotificationManager.IMPORTANCE_LOW);
            nm.createNotificationChannel(ch);
        }
        Notification.Builder builder=Build.VERSION.SDK_INT>=26
                ?new Notification.Builder(this,CHANNEL):new Notification.Builder(this);
        builder.setSmallIcon(R.drawable.ic_instaactions)
               .setContentTitle("InstaActions FAB diagnostics")
               .setContentText("Monitoring Instagram startup for up to 150 seconds")
               .setOngoing(true);
        Notification notification=builder.build();
        if(Build.VERSION.SDK_INT>=34)
            startForeground(NOTIFICATION_ID,notification,ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
        else startForeground(NOTIFICATION_ID,notification);
    }
    private void capture(Context context) {
        R1CD19LiveTrace trace=new R1CD19LiveTrace(true);
        Process shell=null;
        boolean timedOut=false;
        int exit=-1;
        Thread reader=null;
        try {
            shell=new ProcessBuilder("su","-c",MONITOR_COMMAND)
                    .redirectErrorStream(true).start();
            final Process p=shell;
            reader=new Thread(()->{
                try(BufferedReader input=new BufferedReader(new InputStreamReader(
                        p.getInputStream(), StandardCharsets.UTF_8),4096)){
                    String line;
                    int lines=0;
                    while((line=input.readLine())!=null && lines++<20000){
                        // Only fixed event codes and process-seen counters survive parsing.
                        trace.accept(line);
                        if(lines%16==0 || line.equals("INA_D19_POLL"))
                            save(context,trace.report("CAPTURE_RUNNING"));
                    }
                }catch(Exception ignored){ /* Reader errors become partial diagnostics. */ }
            },"ina-d19-sanitizing-stream-reader");
            reader.setDaemon(true);
            reader.start();
            if(!shell.waitFor(MAX_CAPTURE_MS,TimeUnit.MILLISECONDS)) {
                timedOut=true;shell.destroyForcibly();
            } else exit=shell.exitValue();
            reader.join(1200L);
            String state=timedOut?"CAPTURE_TIMEOUT_PARTIAL":
                    !trace.rootGranted()?"ROOT_DENIED_OR_UNAVAILABLE":
                    exit!=0?"ROOT_COMMAND_FAILED_EXIT_"+exit:
                    trace.sawInstagram()?"COMPLETE_INSTAGRAM_OBSERVED":"COMPLETE_NO_INSTAGRAM_SEEN";
            save(context,trace.report(state));
        }catch(java.io.IOException missing){
            save(context,trace.report("SU_UNAVAILABLE_OR_DENIED"));
        }catch(InterruptedException stopped){
            Thread.currentThread().interrupt();save(context,trace.report("CAPTURE_INTERRUPTED"));
        }finally{if(shell!=null)shell.destroy();}
    }
}
