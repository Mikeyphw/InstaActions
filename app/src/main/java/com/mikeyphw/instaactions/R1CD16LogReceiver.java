package com.mikeyphw.instaactions;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Process;
import android.util.Log;

/** Minimal explicit-broadcast sink. An exported receiver is necessary to cross
 * Instagram's UID boundary, but only that UID can contribute diagnostic events.
 * No receiver path can enable actions, load account data, or start UI.
 */
public final class R1CD16LogReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context,Intent intent){
        if(context==null || intent==null || Build.VERSION.SDK_INT<34
                || !R1CD16FabEvents.ACTION.equals(intent.getAction()))return;
        try {
            int sender=getSentFromUid();
            int instagram=context.getPackageManager().getApplicationInfo(
                    ModuleConstants.TARGET_PACKAGE,0).uid;
            if(sender==Process.INVALID_UID || sender!=instagram)return;
            String from=getSentFromPackage();
            if(from!=null && !ModuleConstants.TARGET_PACKAGE.equals(from))return;
            String code=intent.getStringExtra(R1CD16FabEvents.EXTRA_EVENT);
            String detail=intent.getStringExtra(R1CD16FabEvents.EXTRA_DETAIL);
            if(!R1CD16FabEvents.allowed(code) || !R1CD16FabEvents.validDetail(detail))return;
            R1CD16LogStore.append(context,code,detail);
        }catch(Throwable ignored){
            Log.w("INA_FAB","INA_FAB_LOG_RECEIVER_REFUSED");
        }
    }
}
