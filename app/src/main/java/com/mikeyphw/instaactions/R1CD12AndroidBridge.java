package com.mikeyphw.instaactions;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

/** Android framework ActivityLifecycleCallbacks, independent of Instagram
 * Repost and deliberately blind to account/session details.
 * Register once in the verified Instagram main process. No Activity retained.
 */
final class R1CD12AndroidBridge implements Application.ActivityLifecycleCallbacks {
    private static final R1CD12AndroidBridge INSTANCE=new R1CD12AndroidBridge();
    private static boolean registered;
    private R1CD12AndroidBridge() { }
    static synchronized void install(Activity activity) {
        if(registered || activity==null ||
           !ModuleConstants.TARGET_PACKAGE.equals(activity.getPackageName()))return;
        try {
            activity.getApplication().registerActivityLifecycleCallbacks(INSTANCE);
            registered=true;
            R1CD12AccountLifecycle.setLifecycleStatus(true);
        } catch(Throwable ignored) {
            R1CD12AccountLifecycle.setLifecycleStatus(false);
        }
    }
    private boolean target(Activity activity) {
        return activity!=null && ModuleConstants.TARGET_PACKAGE.equals(activity.getPackageName());
    }
    @Override public void onActivityResumed(Activity activity) {
        if(target(activity))R1CD12AccountLifecycle.lifecycleResumed();
    }
    @Override public void onActivityPaused(Activity activity) {
        if(target(activity))R1CD12AccountLifecycle.lifecyclePaused();
    }
    @Override public void onActivityDestroyed(Activity activity) {
        if(target(activity))R1CD12AccountLifecycle.lifecycleDestroyed();
    }
    @Override public void onActivityCreated(Activity activity,Bundle state) { }
    @Override public void onActivityStarted(Activity activity) { }
    @Override public void onActivityStopped(Activity activity) { }
    @Override public void onActivitySaveInstanceState(Activity activity,Bundle state) { }
}
