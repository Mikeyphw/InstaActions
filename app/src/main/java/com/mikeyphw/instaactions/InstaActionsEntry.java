package com.mikeyphw.instaactions;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.util.Log;
import java.lang.reflect.Method;
import io.github.libxposed.api.XposedModule;

/** Vector API 102: one transparent Android lifecycle interceptor, no Instagram method hooks. */
public final class InstaActionsEntry extends XposedModule {
    private String processName = "";
    private String moduleNativeLibDir;

    public InstaActionsEntry() { super(); }

    @Override public void onModuleLoaded(ModuleLoadedParam param) {
        processName = param.getProcessName();
        ApplicationInfo module = getModuleApplicationInfo();
        moduleNativeLibDir = module == null ? null : module.nativeLibraryDir;
    }

    @Override public void onPackageReady(PackageReadyParam param) {
        if (!ModuleConstants.TARGET_PACKAGE.equals(param.getPackageName())) return;
        if (!ModuleConstants.isMainInstagramProcess(processName)) return;
        final ApplicationInfo target = param.getApplicationInfo();
        final String targetApk = target == null ? null : target.sourceDir;
        try {
            Method resumed = Activity.class.getDeclaredMethod("onResume");
            hook(resumed).intercept(chain -> {
                // Preserve original behavior, result, and exceptions exactly.
                Object result = chain.proceed();
                try {
                    Object owner = chain.getThisObject();
                    if (owner instanceof Activity) {
                        try {
                            R1ProbeController.onActivityResumed((Activity) owner,
                                    targetApk, moduleNativeLibDir);
                        } finally {
                            // Independent of R1A's scan success/failure.
                            R1BContextMonitor.resumed((Activity) owner);
                        }
                    }
                } catch (Throwable diagnosticFailure) {
                    Log.w("INA_R1", "INA_R1_CALLBACK_FAILURE type="
                            + diagnosticFailure.getClass().getSimpleName());
                }
                return result;
            });
            // Observe only Activity lifecycle; no Instagram-internal callbacks are hooked.
            for (String method : new String[] {"onPause", "onDestroy"}) {
                Method leaving = Activity.class.getDeclaredMethod(method);
                hook(leaving).intercept(chain -> {
                    Object result = chain.proceed();
                    try {
                        Object owner = chain.getThisObject();
                        if (owner instanceof Activity) R1BContextMonitor.paused((Activity) owner);
                    } catch (Throwable diagnosticFailure) {
                        Log.w("INA_R1B", "INA_R1B_DETACH_FAILED type="
                                + diagnosticFailure.getClass().getSimpleName());
                    }
                    return result;
                });
            }
            Log.i("INA_R1B", "INA_R1B_LIFECYCLE_READY hooks=resume_pause_destroy"
                    + " action_hooks=0");
            Log.i("INA_R1", "INA_R1_LIFECYCLE_HOOK_INSTALLED target=instagram_main"
                    + " action_hooks=0");
        } catch (Throwable failure) {
            Log.w("INA_R1", "INA_R1_LIFECYCLE_HOOK_FAILED type="
                    + failure.getClass().getSimpleName());
        }
    }
}
