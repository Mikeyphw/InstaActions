package com.mikeyphw.instaactions;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemClock;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/** Automatically attempts a bounded read-only su diagnostic on app resume.
 * Root authorization remains exclusively under Android's KernelSU/root manager.
 * Never runs in the injected Instagram process, the UI thread, or a service.
 * No arbitrary command execution, application changes, or raw logcat persistence.
 */
final class R1CD17AutoRootDiagnostics {
    private static final Object LOCK = new Object();
    private static final String PREFERENCES="ina_d17_root_report_v1";
    private static final String REPORT="report";
    private static final long MIN_INTERVAL_MS=20_000L;
    private static final int TIMEOUT_SECONDS=25;
    private static final int MAX_RAW_CHARS=160_000;
    private static boolean running;
    private static long lastRequestMs=-MIN_INTERVAL_MS;

    // No user input enters this literal. All operations are read-only.
    private static final String READ_ONLY_COMMAND =
            "printf 'INA_D17_UID\\n'; id -u; " +
            "printf 'INA_D17_PID\\n'; pidof com.instagram.android; " +
            "printf 'INA_D18_PS\\n'; ps -A -o PID,NAME 2>/dev/null | grep -E '[c]om[.]instagram[.]android(:[a-zA-Z0-9._-]+)?$' | head -n 32; " +
            "printf 'INA_D17_LOGCAT\\n'; " +
            "logcat -d -t 1000 -v threadtime -s INA_FAB:V INA_DIAG:V INA_R1B:V INA_R1C:V '*:S'; " +
            "printf 'INA_D17_END\\n'";

    static void onAppResumed(Activity activity,Runnable updateUi){
        if(activity==null)return;
        final long now=SystemClock.elapsedRealtime();
        synchronized(LOCK){
            if(running || now-lastRequestMs<MIN_INTERVAL_MS)return;
            running=true;lastRequestMs=now;
        }
        Context context=activity.getApplicationContext();
        Thread worker=new Thread(()->{
            String report;
            try {report=capture();}
            catch(Throwable unexpected){
                report="INA D17 AUTOMATIC ROOT FAB DIAGNOSTICS\n"
                        +"status=ROOT_DIAGNOSTIC_FAILED type="+unexpected.getClass().getSimpleName()+"\n"
                        +"No raw logcat or Instagram private contents retained.\n";
            }
            context.getSharedPreferences(PREFERENCES,Context.MODE_PRIVATE)
                    .edit().putString(REPORT,report).apply();
            synchronized(LOCK){running=false;}
            if(updateUi!=null && !activity.isFinishing()){
                activity.runOnUiThread(updateUi);
            }
        },"ina-d17-automatic-root-readonly");
        worker.setDaemon(true);
        worker.start();
    }

    private static String capture() {
        Process process=null;
        Thread reader=null;
        StringBuilder output=new StringBuilder();
        final boolean[] limited={false};
        boolean timeout=false;
        int exit=-1;
        try {
            // su may trigger the normal KernelSU authorization prompt; we never bypass it.
            process=new ProcessBuilder("su","-c",READ_ONLY_COMMAND)
                    .redirectErrorStream(true).start();
            InputStream stream=process.getInputStream();
            reader=new Thread(()->{
                try(InputStream input=stream){
                    byte[] buf=new byte[2048];int n;
                    while((n=input.read(buf))!=-1){
                        synchronized(output){
                            int available=MAX_RAW_CHARS-output.length();
                            if(available<=0){limited[0]=true;continue;}
                            String part=new String(buf,0,n,StandardCharsets.UTF_8);
                            if(part.length()>available){part=part.substring(0,available);limited[0]=true;}
                            output.append(part);
                        }
                    }
                }catch(Exception ignored){ }
            },"ina-d17-root-stream-reader");
            reader.setDaemon(true);
            reader.start();
            if(!process.waitFor(TIMEOUT_SECONDS,TimeUnit.SECONDS)){
                timeout=true;
                process.destroy();
                process.destroyForcibly();
            }else exit=process.exitValue();
            reader.join(900L);
            String raw;
            synchronized(output){raw=output.toString();}
            return R1CD17RootLogParser.parse(raw,timeout,exit,limited[0]).report;
        }catch(java.io.IOException missing){
            return "INA D17 AUTOMATIC ROOT FAB DIAGNOSTICS\nstatus=SU_UNAVAILABLE_OR_DENIED\n"
                    +"KernelSU/root manager controls authorization; non-root FAB logs remain available.\n";
        }catch(InterruptedException interruption){
            Thread.currentThread().interrupt();
            return "INA D17 AUTOMATIC ROOT FAB DIAGNOSTICS\nstatus=ROOT_DIAGNOSTIC_INTERRUPTED\n";
        }finally{
            if(process!=null)process.destroy();
        }
    }

    static String report(Context context){
        String saved=context.getSharedPreferences(PREFERENCES,Context.MODE_PRIVATE)
                .getString(REPORT,"INA D17 AUTOMATIC ROOT FAB DIAGNOSTICS\nstatus=AWAITING_FIRST_AUTOMATIC_RUN\n");
        synchronized(LOCK){
            return running?"ROOT_PROBE=RUNNING_IN_BACKGROUND\n"+saved:saved;
        }
    }
    static void clear(Context context){
        context.getSharedPreferences(PREFERENCES,Context.MODE_PRIVATE).edit().clear().apply();
    }
    private R1CD17AutoRootDiagnostics() { }
}
