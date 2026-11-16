package com.mikeyphw.instaactions;

import android.app.Activity;
import android.database.Cursor;
import android.util.Log;

/** Read-only IPC from Instagram's scoped injected process to the standalone app.
 * Fail closed on unavailable provider, disabled toggle, missing cursor or any exception.
 */
final class R1CD20FabVersionSwitch {
    static boolean enabled(Activity instagram){
        if(instagram==null || !ModuleConstants.TARGET_PACKAGE.equals(instagram.getPackageName()))return false;
        try(Cursor cursor=instagram.getContentResolver().query(
                R1CD20FabPolicyProvider.URI,
                new String[]{R1CD20FabPolicyProvider.COLUMN},null,null,null)){
            if(cursor==null || !cursor.moveToFirst())return false;
            int column=cursor.getColumnIndex(R1CD20FabPolicyProvider.COLUMN);
            return column>=0 && cursor.getInt(column)==1;
        }catch(Throwable unavailable){
            Log.w("INA_FAB","INA_FAB_VERSION_OVERRIDE_QUERY_FAILED type="
                    +unavailable.getClass().getSimpleName());
            return false;
        }
    }
    private R1CD20FabVersionSwitch(){}
}
