package com.mikeyphw.instaactions;

import android.app.Activity;
import android.os.Looper;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayDeque;
import java.util.Locale;

/** Manual A/B avatar-renderer discovery: only VIEW SHAPES and exact fixed labels.
 * Visible square ImageViews are not proof of friends or user avatars.
 * No pixel/bitmap/username/ID access. No UI changes.
 */
final class R1CD10AvatarAudit {
    private static final String[] SURFACES={"friends_tab","liked_by_facepile","reposted_by_facepile"};
    private static final int[][] samples=new int[2][3], images=new int[2][3], squareCandidates=new int[2][3],
           labelHints=new int[2][3], truncated=new int[2][3];
    private static int cohort=-1;
    private R1CD10AvatarAudit() { }
    static synchronized void begin(int manual){if(manual<0||manual>1)throw new IllegalArgumentException();cohort=manual;}
    static synchronized void stop(){cohort=-1;}
    static synchronized void clear(){cohort=-1;for(int a=0;a<2;a++)for(int s=0;s<3;s++){
        samples[a][s]=images[a][s]=squareCandidates[a][s]=labelHints[a][s]=truncated[a][s]=0;
    }}
    static String snapshot(Activity activity,View excludedPanel,View excludedChip,int surface){
        int active;
        synchronized(R1CD10AvatarAudit.class){active=cohort;}
        if(!R1CD09DiagnosticGate.active(R1CD09DiagnosticGate.Mode.AVATAR)||active<0)
            return "REFUSED: begin Avatar A/B and Start A or B first";
        if(surface<0||surface>=3)return "REFUSED: unsupported avatar surface";
        if(R1CD07Recovery.isEnabled())return "REFUSED: turn Repost recovery OFF";
        if(!R1CD04PassiveProbe.isInstalled()||activity==null||Looper.myLooper()!=Looper.getMainLooper())
            return "REFUSED: exact APK attestation and UI thread required";
        int all=0,square=0,hints=0,visited=0;boolean cap=false;
        final float density=activity.getResources().getDisplayMetrics().density;
        try {
            ArrayDeque<View> pending=new ArrayDeque<>();pending.add(activity.getWindow().getDecorView());
            long deadline=SystemClock.uptimeMillis()+30;
            while(!pending.isEmpty()) {
                if(visited>=512 || SystemClock.uptimeMillis()>deadline){cap=true;break;}
                View v=pending.removeFirst();
                if(v==excludedPanel||v==excludedChip||!v.isShown())continue;
                visited++;
                if(v instanceof ImageView){
                    all++;int w=v.getWidth(),h=v.getHeight();
                    if(w>density*16 && w<density*70 && h>density*16 && h<density*70
                            && Math.abs(w-h)<=density*8)square++;
                }
                CharSequence t=v.getContentDescription();
                if(t==null && v instanceof TextView)t=((TextView)v).getText();
                if(t!=null){String s=t.toString().trim().toLowerCase(Locale.ROOT);
                    if("friends".equals(s)||"amigos".equals(s)||"liked by".equals(s)
                        ||"reposted by".equals(s)||"curtido por".equals(s))hints++;
                }
                if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;
                    for(int i=0;i<g.getChildCount();i++)pending.addLast(g.getChildAt(i));}
            }
        }catch(Throwable e){return "REFUSED: avatar UI inspection failed";}
        synchronized(R1CD10AvatarAudit.class){
            if(cohort!=active)return "REFUSED: manual cohort changed during scan";
            if(samples[active][surface]>=20)return "REFUSED: cap reached";
            samples[active][surface]++;images[active][surface]+=all;
            squareCandidates[active][surface]+=square;labelHints[active][surface]+=hints;
            if(cap)truncated[active][surface]++;
        }
        R1CD10ComparisonFlow.sample();
        return "Avatar "+SURFACES[surface]+" manual="+(active==0?"A":"B")+
            " images="+all+" square_image_candidates="+square+" label_hints="+hints+
            "; exact avatar renderer and friend identity NOT_VERIFIED";
    }
    static synchronized String report(){
        StringBuilder b=new StringBuilder("D10 AVATAR A/B · Friends / liked-by / reposted-by · READ ONLY, SHAPE HINTS NOT AVATAR IDENTITY\n");
        for(int a=0;a<2;a++)for(int i=0;i<3;i++) b.append(a==0?'A':'B').append(' ')
            .append(SURFACES[i]).append(" snapshots=").append(samples[a][i])
            .append(" imageviews=").append(images[a][i])
            .append(" small_square_candidate=").append(squareCandidates[a][i])
            .append(" exact_label_hints=").append(labelHints[a][i])
            .append(" scan_truncated=").append(truncated[a][i]).append('\n');
        b.append("These are broad VIEW candidates, not verified friend facepiles. NO image pixels or account data retained.\n");
        return b.toString();
    }
}
