package com.mikeyphw.instaactions;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.util.Log;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;
import io.github.libxposed.api.XposedModule;
import io.github.libxposed.api.XposedInterface;

/**
 * Vector API 102 R1B. Diagnostic callbacks are deliberately before any scope
 * guard. Hooks remain limited to Android lifecycle pass-through, actions off.
 */
public final class InstaActionsEntry extends XposedModule {
    private static final String DIAGNOSTIC_TAG = "INA_DIAG";
    private static final AtomicBoolean FIRST_RESUME = new AtomicBoolean(false);
    private String processName = "";
    private String moduleNativeLibDir;

    public InstaActionsEntry() { super(); }

    /** Android logcat and Xposed framework logging are independent evidence channels. */
    private void diagnostic(String event) {
        Log.i(DIAGNOSTIC_TAG, event);
        try {
            // Vector's module journal retains the framework tag "VectorContext",
            // not arbitrary module tags. Passing null invokes the stable
            // VectorContext route while Android logcat keeps INA_DIAG.
            log(Log.INFO, null, event);
        } catch (Throwable loggingUnavailable) {
            // Never interrupt app initialization merely because framework logging failed.
        }
    }

    @Override public void onModuleLoaded(ModuleLoadedParam param) {
        processName = param.getProcessName();
        diagnostic("INA_DIAG_MODULE_LOADED process_present=" + (processName != null)
                + " main=" + ModuleConstants.isMainInstagramProcess(processName));
        try {
            ApplicationInfo module = getModuleApplicationInfo();
            moduleNativeLibDir = module == null ? null : module.nativeLibraryDir;
            diagnostic("INA_DIAG_MODULE_INFO native_library_dir_present="
                    + (moduleNativeLibDir != null && !moduleNativeLibDir.isEmpty()));
        } catch (Throwable failure) {
            moduleNativeLibDir = null;
            diagnostic("INA_DIAG_MODULE_INFO_FAILED type="
                    + failure.getClass().getSimpleName());
        }
    }

    @Override public void onPackageLoaded(PackageLoadedParam param) {
        // Pre-ready callback proves Vector reached the package lifecycle boundary.
        diagnostic("INA_DIAG_PACKAGE_LOADED instagram="
                + ModuleConstants.TARGET_PACKAGE.equals(param.getPackageName())
                + " main=" + ModuleConstants.isMainInstagramProcess(processName));
    }

    @Override public void onPackageReady(PackageReadyParam param) {
        final boolean instagram = ModuleConstants.TARGET_PACKAGE.equals(param.getPackageName());
        final boolean mainProcess = ModuleConstants.isMainInstagramProcess(processName);
        diagnostic("INA_DIAG_PACKAGE_READY instagram=" + instagram
                + " main=" + mainProcess + " process_present=" + (processName != null));
        if (!instagram || !mainProcess) {
            diagnostic("INA_DIAG_GUARD_SKIP reason="
                    + (!instagram ? "package_mismatch" : "process_mismatch"));
            return;
        }
        final ApplicationInfo target = param.getApplicationInfo();
        final String targetApk = target == null ? null : target.sourceDir;
        final ClassLoader instagramLoader = param.getClassLoader();
        diagnostic("INA_DIAG_TARGET_APK_PRESENT value="
                + (targetApk != null && !targetApk.isEmpty()));
        try {
            Method resumed = Activity.class.getDeclaredMethod("onResume");
            hook(resumed).intercept(chain -> {
                // Preserve original behavior, result and exceptions exactly.
                Object result = chain.proceed();
                try {
                    Object owner = chain.getThisObject();
                    if (owner instanceof Activity) {
                        if (FIRST_RESUME.compareAndSet(false, true)) {
                            diagnostic("INA_DIAG_FIRST_ACTIVITY_RESUMED actions=off");
                        }
                        try {
                            R1ProbeController.onActivityResumed((Activity) owner,
                                    targetApk, moduleNativeLibDir);
                        } finally {
                            R1BContextMonitor.resumed((Activity) owner);
                            // Independent, read-only metadata inventory. Not a media binding.
                            R1CDiscoveryController.onActivityResumed((Activity) owner,
                                    targetApk, moduleNativeLibDir, instagramLoader);
                            // D04 installs one passive, exact-build-only method observer.
                            // The interceptor NEVER alters arguments, return values or exceptions.
                            R1CD04PassiveProbe.onActivityResumed((Activity) owner,
                                    targetApk, instagramLoader, (method, counter) -> {
                                        hook(method)
                                                .setId("ina-r1c-d04-reels-passive")
                                                .setExceptionMode(XposedInterface.ExceptionMode.PROTECTIVE)
                                                .intercept(observation -> {
                                                    try {
                                                        return observation.proceed();
                                                    } finally {
                                                        // The original exception propagates unchanged.
                                                        // A probe failure must never escape this finally.
                                                        try {
                                                            counter.observe(observation.getArg(1),
                                                                    R1BContextMonitor.stableScreen());
                                                        } catch (Throwable ignored) {
                                                            // Fail closed: instrumentation cannot affect Instagram.
                                                        }
                                                    }
                                                });
                                    });
                        }
                    }
                } catch (Throwable diagnosticFailure) {
                    diagnostic("INA_DIAG_RESUME_CALLBACK_FAILED type="
                            + diagnosticFailure.getClass().getSimpleName());
                }
                return result;
            });
            for (String method : new String[] {"onPause", "onDestroy"}) {
                Method leaving = Activity.class.getDeclaredMethod(method);
                hook(leaving).intercept(chain -> {
                    Object result = chain.proceed();
                    try {
                        Object owner = chain.getThisObject();
                        if (owner instanceof Activity) R1BContextMonitor.paused((Activity) owner);
                    } catch (Throwable diagnosticFailure) {
                        diagnostic("INA_DIAG_DETACH_CALLBACK_FAILED type="
                                + diagnosticFailure.getClass().getSimpleName());
                    }
                    return result;
                });
            }
            diagnostic("INA_DIAG_HOOKS_INSTALLED lifecycle=resume_pause_destroy actions=off");
            Log.i("INA_R1B", "INA_R1B_LIFECYCLE_READY hooks=resume_pause_destroy"
                    + " action_hooks=0");
            Log.i("INA_R1", "INA_R1_LIFECYCLE_HOOK_INSTALLED target=instagram_main"
                    + " action_hooks=0");
        } catch (Throwable failure) {
            diagnostic("INA_DIAG_HOOKS_FAILED type=" + failure.getClass().getSimpleName());
            Log.w("INA_R1", "INA_R1_LIFECYCLE_HOOK_FAILED type="
                    + failure.getClass().getSimpleName());
        }
    }
}
