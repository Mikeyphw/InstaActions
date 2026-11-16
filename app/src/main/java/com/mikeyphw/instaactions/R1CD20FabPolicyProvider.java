package com.mikeyphw.instaactions;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Process;
import android.util.Log;

/** Instagram can read ONLY a single non-sensitive UI visibility bit.
 * Verified by actual calling UID, not by a caller-provided package extra.
 * All mutations and unrelated URI paths are refused.
 */
public final class R1CD20FabPolicyProvider extends ContentProvider {
    static final String AUTHORITY="com.mikeyphw.instaactions.fabpolicy";
    static final Uri URI=Uri.parse("content://"+AUTHORITY+"/v1");
    static final String COLUMN="ignore_version_for_fab";

    @Override public boolean onCreate(){return true;}
    private boolean trustedCaller(){
        int uid=Binder.getCallingUid();
        if(uid==Process.myUid())return true;
        if(uid<=0)return false;
        try {
            PackageManager pm=getContext().getPackageManager();
            String[] packages=pm.getPackagesForUid(uid);
            if(packages!=null)for(String pkg:packages)
                if(ModuleConstants.TARGET_PACKAGE.equals(pkg))return true;
        }catch(RuntimeException failure){
            Log.w("INA_FAB","INA_FAB_POLICY_UID_LOOKUP_FAILED");
        }
        return false;
    }
    @Override public Cursor query(Uri uri,String[] projection,String selection,
                                   String[] selectionArgs,String sortOrder){
        if(!URI.equals(uri) || !trustedCaller()){
            Log.w("INA_FAB","INA_FAB_POLICY_QUERY_REFUSED");
            throw new SecurityException("FAB policy unavailable for caller or path");
        }
        // Fixed single-column output, no client-controlled projection or data exposure.
        MatrixCursor result=new MatrixCursor(new String[]{COLUMN},1);
        result.addRow(new Object[]{R1CD20FabOverrideStore.enabled(getContext())?1:0});
        return result;
    }
    @Override public String getType(Uri uri){
        if(!URI.equals(uri) || !trustedCaller())throw new SecurityException("Unavailable");
        return "vnd.android.cursor.item/vnd."+AUTHORITY;
    }
    @Override public Uri insert(Uri uri,ContentValues values){throw new UnsupportedOperationException();}
    @Override public int update(Uri uri,ContentValues values,String selection,String[] selectionArgs){
        throw new UnsupportedOperationException();
    }
    @Override public int delete(Uri uri,String selection,String[] selectionArgs){
        throw new UnsupportedOperationException();
    }
}
