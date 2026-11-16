package com.mikeyphw.instaactions;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayDeque;

/** Fail-closed detection of Instagram-owned main UI anchors for changed Activity
 * class names. Never reads content descriptions, View text or user/media data.
 */
final class R1CD16MainSurface {
    private static final int MAX_VIEWS=384;
    static R1CD16AnchorPolicy.Decision inspect(Activity activity){
        if(activity==null || !ModuleConstants.TARGET_PACKAGE.equals(activity.getPackageName()))
            return R1CD16AnchorPolicy.Decision.UNKNOWN;
        boolean known=R1BNavigationPolicy.isMainActivity(activity.getClass().getName());
        if(known)return R1CD16AnchorPolicy.Decision.MAIN_CLASS;
        if(Looper.myLooper()!=Looper.getMainLooper())return R1CD16AnchorPolicy.Decision.UNKNOWN;
        try {
            View root=activity.getWindow()==null?null:activity.getWindow().getDecorView();
            if(root==null || !root.isShown())return R1CD16AnchorPolicy.Decision.UNKNOWN;
            boolean bar=false,feed=false,clips=false,profile=false;
            ArrayDeque<View> queue=new ArrayDeque<>();queue.add(root);int visited=0;
            while(!queue.isEmpty() && visited<MAX_VIEWS){
                View v=queue.removeFirst();visited++;
                if(v==null || !v.isShown())continue;
                int id=v.getId();
                if(id!=View.NO_ID){
                    try {
                        Resources r=v.getResources();
                        if(ModuleConstants.TARGET_PACKAGE.equals(r.getResourcePackageName(id))){
                            String name=r.getResourceEntryName(id);
                            if("main_feed_action_bar".equals(name))bar=true;
                            else if("feed_tab".equals(name))feed=true;
                            else if("clips_tab".equals(name))clips=true;
                            else if("profile_tab".equals(name))profile=true;
                        }
                    }catch(Resources.NotFoundException ignored){}
                }
                // Two independent Instagram-owned anchors suffice; stop scanning
                // immediately instead of consuming the entire busy Feed tree.
                if(bar && (feed || clips || profile))
                    return R1CD16AnchorPolicy.Decision.VERIFIED_MAIN_ANCHORS;
                if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;
                    for(int i=0;i<g.getChildCount();i++){View child=g.getChildAt(i);
                        if(child!=null)queue.addLast(child);}
                }
            }
            return R1CD16AnchorPolicy.qualify(false,bar,feed,clips,profile,
                    !queue.isEmpty());
        }catch(Throwable ignored){return R1CD16AnchorPolicy.Decision.UNKNOWN;}
    }
    private R1CD16MainSurface(){}
}
