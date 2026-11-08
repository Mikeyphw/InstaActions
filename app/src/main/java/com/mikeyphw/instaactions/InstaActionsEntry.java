package com.mikeyphw.instaactions;

import android.util.Log;
import io.github.libxposed.api.XposedModule;

/**
 * Vector API 102 entry point. R0 is read-only and intentionally installs NO hooks.
 * Instagram UI and InstaEclipse hooks remain untouched.
 */
public final class InstaActionsEntry extends XposedModule {
    private String processName = "";

    public InstaActionsEntry() {
        super();
    }

    @Override
    public void onModuleLoaded(ModuleLoadedParam param) {
        processName = param.getProcessName();
        // The class can load in other scoped processes, but never acts there.
    }

    @Override
    public void onPackageReady(PackageReadyParam param) {
        if (!ModuleConstants.TARGET_PACKAGE.equals(param.getPackageName())) return;
        if (!ModuleConstants.isMainInstagramProcess(processName)) return;
        // For device acceptance, filter: adb logcat -d -s INA_R0:I '*:S'
        // No user handles, media IDs, URLs, comments, recipient data or credentials.
        Log.i(ModuleConstants.ENTRY_LOG_TAG,
                "INA_R0_PACKAGE_READY package=com.instagram.android process=main hooks=0");
    }
}
