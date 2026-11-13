package com.mikeyphw.instaactions;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.util.Log;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
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
    // D07 is independent of D06: manual opt-in native UI-eligibility recovery.
    private static volatile InstaActionsEntry live;
    private volatile ClassLoader d07Loader;
    private final AtomicBoolean d07Requested = new AtomicBoolean(false);
    private volatile R1CD07ActualDataTarget d07DataTarget;
    private final Set<Method> d07HookedMethods = new HashSet<>();
    private volatile int d07Installed, d07Failed, d07Attempted, d07ResolveMisses;
    private static final int D07_MAX_DATA_HOOKS = 3;

    static void requestD07(Activity activity) {
        InstaActionsEntry module = live;
        if (module == null || activity == null ||
                !ModuleConstants.TARGET_PACKAGE.equals(activity.getPackageName()) ||
                !R1CD04PassiveProbe.isInstalled() || module.d07Loader == null ||
                !module.d07Requested.compareAndSet(false, true)) return;
        Thread worker = new Thread(() -> module.installD07(module.d07Loader),
                "ina-d07-repost-install");
        worker.setDaemon(true);
        try { worker.start(); }
        catch (Throwable failure) {
            R1CD07GateTrace.setStatus("REFUSED: D07 worker failed");
            R1CD07Recovery.onHookFailure();
        }
    }

    static boolean dataGateReady() {
        InstaActionsEntry m = live;
        return m != null && m.d07Installed > 0 && m.d07Failed == 0;
    }
    static String dataGateStatus() {
        InstaActionsEntry m = live;
        if (m == null) return "D07 actual D3r receiver: NOT_LOADED";
        return "D07 actual D3r receiver: attempted=" + m.d07Attempted
                + " installed=" + m.d07Installed + " failures=" + m.d07Failed
                + " unresolved=" + m.d07ResolveMisses + " limit=" + D07_MAX_DATA_HOOKS;
    }

    private synchronized void maybeHookActualD07(Object selectorInstance) {
        R1CD07ActualDataTarget resolver = d07DataTarget;
        if (resolver == null || d07Attempted >= D07_MAX_DATA_HOOKS) return;
        Method method = resolver.resolve(selectorInstance);
        if (method == null) {
            if (d07ResolveMisses < 256) d07ResolveMisses++;
            return;
        }
        if (d07HookedMethods.contains(method)) return;
        d07HookedMethods.add(method);
        int ordinal = d07Attempted++;
        // Don't mutate ART hook tables while the original selector is executing.
        Thread installer = new Thread(() -> {
            try {
                hook(method).setId("ina-d07-verified-d3r-" + ordinal)
                    .setExceptionMode(XposedInterface.ExceptionMode.PROTECTIVE)
                    .intercept(chain -> {
                        Object original = chain.proceed(); // exactly once
                        if (!R1CD07PredicateTrace.inRepostFrame()) return original;
                        try {
                            R1CD07PredicateTrace.note(R1CD07PredicateTrace.ELIGIBILITY, original);
                            R1CD07PredicateTrace.noteDataReceiver(chain.getThisObject());
                            return R1CD07PredicateTrace.noteExactData(chain.getArg(0),original);
                        } catch(Throwable ignored) {
                            R1CD07Recovery.onHookFailure();
                            return original;
                        }
                    });
                d07Installed++;
            } catch(Throwable failed) {
                d07Failed++;
                R1CD07Recovery.onHookFailure();
                Log.w("INA_D07", "INA_D07_DATA_HOOK_FAILED type="
                        + failed.getClass().getSimpleName());
            }
        }, "ina-d07-d3r-" + ordinal);
        installer.setDaemon(true);
        try {installer.start();}
        catch(Throwable failed) {d07Failed++;R1CD07Recovery.onHookFailure();}
    }

    private void installD07(ClassLoader loader) {
        if (!R1CD04PassiveProbe.isInstalled()) {
            R1CD07GateTrace.setStatus("REFUSED: D04 exact-build attestation absent");
            return;
        }
        StringBuilder unavailable = new StringBuilder();
        try {
            R1CD07GateTrace.setInputPresence(R1CD07InputPresence.preflight(loader));
        } catch(Throwable e) {R1CD07GateTrace.setInputPresence(null);unavailable.append("input;");}
        try {
            R1CD07GateTrace.setBranchInputs(R1CD07BranchInputs.preflight(loader));
        } catch(Throwable e) {R1CD07GateTrace.setBranchInputs(null);unavailable.append("data;");}
        try {d07DataTarget=R1CD07ActualDataTarget.preflight(loader);}
        catch(Throwable e) {d07DataTarget=null;unavailable.append("receiver;");}
        try {
            Method ui=R1CD07GateTrace.uiMethod(loader);
            Method ufi=R1CD07GateTrace.ufiMethod(loader);
            hook(ui).setId("ina-d07-repost-ui-selector4")
                .setExceptionMode(XposedInterface.ExceptionMode.PROTECTIVE)
                .intercept(chain -> {
                    R1CD07PredicateTrace.Frame frame=R1CD07GateTrace.enterUi(chain.getThisObject());
                    if(frame != null) {
                        try {maybeHookActualD07(chain.getThisObject());}
                        catch(Throwable ignored) {R1CD07Recovery.onHookFailure();}
                    }
                    try {
                        Object result=chain.proceed(); // original once, preserve throws
                        R1CD07GateTrace.leaveUi(frame,result);
                        frame=null;
                        try {return R1CD07GateTrace.noteUi(chain.getThisObject(),result);}
                        catch(Throwable ignored) {return result;}
                    } finally {R1CD07GateTrace.leaveUi(frame,null);}
                });
            hook(ufi).setId("ina-d07-repost-ufi-selectors11-12")
                .setExceptionMode(XposedInterface.ExceptionMode.PROTECTIVE)
                .intercept(chain -> {
                    Object result=chain.proceed();
                    try {R1CD07GateTrace.noteUfi(chain.getThisObject(),result);}
                    catch(Throwable ignored) { }
                    return result;
                });
        } catch(Throwable failure) {
            R1CD07GateTrace.setStatus("REFUSED: native selector hooks unavailable");
            R1CD07Recovery.onHookFailure();
            Log.w("INA_D07","INA_D07_HOOK_FAILED type="+failure.getClass().getSimpleName());
            return;
        }
        int optional=0;
        try {
            Method factory=R1CD07FactoryTarget.resolve(loader);
            hook(factory).setId("ina-d07-repost-factory-readonly")
                .setExceptionMode(XposedInterface.ExceptionMode.PROTECTIVE)
                .intercept(chain -> {
                    Object original=chain.proceed();
                    try {R1CD07PredicateTrace.noteFactory(original);}
                    catch(Throwable ignored) { }
                    return original;
                });
            optional++;
        } catch(Throwable e) {unavailable.append("factory;");}
        try {
            Method config=R1CD07GateTrace.mobileConfigMethod(loader);
            hook(config).setId("ina-d07-repost-config-readonly")
                .setExceptionMode(XposedInterface.ExceptionMode.PROTECTIVE)
                .intercept(chain -> {
                    Object original=chain.proceed();
                    if (!R1CD07PredicateTrace.inRepostFrame()) return original;
                    try {
                        R1CD07PredicateTrace.note(R1CD07PredicateTrace.MOBILE_CONFIG,original);
                        // Trial cannot be user-armed. Keep first config unmodified.
                    } catch(Throwable ignored) { }
                    return original;
                });
            optional++;
        } catch(Throwable e) {unavailable.append("config;");}
        try {
            Method session=R1CD07GateTrace.sessionCheckMethod(loader);
            hook(session).setId("ina-d07-repost-session-check-readonly")
                .setExceptionMode(XposedInterface.ExceptionMode.PROTECTIVE)
                .intercept(chain -> {
                    Object original=chain.proceed();
                    try {R1CD07PredicateTrace.note(R1CD07PredicateTrace.SESSION_CHECK,original);}
                    catch(Throwable ignored) { }
                    return original;
                });
            optional++;
        } catch(Throwable e) {unavailable.append("session;");}
        boolean deopt=false;
        try {deopt=deoptimize(R1CD07GateTrace.uiMethod(loader));}
        catch(Throwable e) {unavailable.append("deopt;");}
        R1CD07ExactGateTrial.setDeoptimized(deopt);
        if(!deopt)R1CD07Recovery.onHookFailure();
        R1CD07GateTrace.installed(optional,unavailable.toString());
        Log.i("INA_D07", "INA_D07_SOURCE_READY exact_d04=true deopt=" + deopt
                + " recovery=OFF media_identity=NOT_VERIFIED");
    }


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
        live = this;
        d07Loader = instagramLoader;
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
                                    targetApk, moduleNativeLibDir, instagramLoader, (method, counter) -> {
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
                                                                    R1BContextMonitor.stableScreen(),
                                                                    observation.getThisObject(),
                                                                    observation.getArg(0));
                                                        } catch (Throwable ignored) {
                                                            // Fail closed: instrumentation cannot affect Instagram.
                                                        }
                                                    }
                                                });
                                    });
                            // UI-only diagnostic controller. No Instagram action hooks.
                            try { R1CD05FabPanel.onResumed((Activity) owner); }
                            catch (Throwable ignored) { /* FAB is strictly optional. */ }
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
                    R1CD07Recovery.onActivityExit();
                    Object result = chain.proceed();
                    try {
                        Object owner = chain.getThisObject();
                        if (owner instanceof Activity) {
                            try { R1BContextMonitor.paused((Activity) owner); }
                            finally {
                                R1CD04PassiveProbe.paused();
                                try { R1CD05FabPanel.onLeaving((Activity) owner); }
                                catch (Throwable ignored) { /* Optional UI teardown. */ }
                            }
                        }
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
