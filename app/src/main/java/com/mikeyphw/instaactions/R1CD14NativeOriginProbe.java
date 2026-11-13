package com.mikeyphw.instaactions;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;

/**
 * D14 independent source-category evidence using the *already installed* D04
 * IG455 X.07EF.A05 observer. The IG455 DEX shows X.07EF.A08:ClipsViewerConfig
 * and ClipsViewerConfig.A00:ClipsViewerSource are read by that exact builder.
 * No new hook, no method invocation on Instagram objects, no UI mutation,
 * no source instance, account identifier, Media or raw enum value retained.
 */
final class R1CD14NativeOriginProbe {
    enum Source {
        CLIPS_TAB_SOURCE, REEL_FEED_TIMELINE_SOURCE, FEED_TIMELINE_SOURCE,
        OTHER_FEED_FAMILY_SOURCE, OTHER_NATIVE_SOURCE, SOURCE_NULL, UNAVAILABLE
    }
    private static final int MAX = 4096;
    private static final int[][] AB = new int[2][Source.values().length];
    private static final int[] TOTALS = new int[Source.values().length];
    private static Field configField, sourceField;
    private static Class<?> builderClass, sourceType;
    private static String status="NOT_PREPARED_EXACT_APK";
    private static String last="NO_NATIVE_BUILDER_CALLBACK";
    private static int screenReels, screenOther, failures, overflow;
    private R1CD14NativeOriginProbe() { }

    static synchronized void prepare(ClassLoader loader) {
        if(!"NOT_PREPARED_EXACT_APK".equals(status))return;
        // Called only from the exact-SHA D04 installer after successful APK gate.
        try {
            if(loader==null)throw new IllegalArgumentException("no loader");
            Class<?> builder=Class.forName("X.07EF",false,loader);
            Class<?> config=Class.forName("com.instagram.clips.intf.ClipsViewerConfig",false,loader);
            Class<?> source=Class.forName("com.instagram.clips.intf.ClipsViewerSource",false,loader);
            Field outer=builder.getDeclaredField("A08");
            Field inner=config.getDeclaredField("A00");
            if(Modifier.isStatic(outer.getModifiers()) || Modifier.isStatic(inner.getModifiers())
                    || outer.getType()!=config || inner.getType()!=source || !source.isEnum())
                throw new IllegalStateException("source field shape mismatch");
            outer.setAccessible(true);
            inner.setAccessible(true);
            builderClass=builder;configField=outer;sourceField=inner;sourceType=source;
            status="EXACT_IG455_FIELD_SHAPES_READY_READ_ONLY";
        }catch(Throwable unavailable){
            builderClass=null;configField=null;sourceField=null;sourceType=null;
            status="EXACT_IG455_SOURCE_FIELDS_UNAVAILABLE_FAIL_CLOSED";
        }
    }

    /** Only published enum *code names* are classified, never stored or exported. */
    static Source classify(String code) {
        if(code==null)return Source.SOURCE_NULL;
        if("CLIPS_TAB".equals(code) || "CLIPS_TAB_HOMECOMING".equals(code))
            return Source.CLIPS_TAB_SOURCE;
        if("REEL_FEED_TIMELINE".equals(code))return Source.REEL_FEED_TIMELINE_SOURCE;
        if("FEED_TIMELINE".equals(code))return Source.FEED_TIMELINE_SOURCE;
        if(code.startsWith("FEED_TIMELINE_") || code.startsWith("REEL_FEED_TIMELINE_")
                || code.startsWith("FEED_CONTEXTUAL_"))return Source.OTHER_FEED_FAMILY_SOURCE;
        return Source.OTHER_NATIVE_SOURCE;
    }

    /** The original builder has already run exactly once before this callback. */
    static void observe(Object builder, String stableScreen) {
        final Field outer,inner;
        final Class<?> expectedBuilder,expectedSource;
        synchronized(R1CD14NativeOriginProbe.class){
            outer=configField;inner=sourceField;
            expectedBuilder=builderClass;expectedSource=sourceType;
        }
        if(outer==null || inner==null || expectedBuilder==null || builder==null
                || builder.getClass()!=expectedBuilder){
            failure();return;
        }
        try {
            Object config=outer.get(builder);
            Object source=config==null?null:inner.get(config);
            if(source!=null && (expectedSource==null || source.getClass()!=expectedSource
                    || !(source instanceof Enum<?>))){failure();return;}
            // Enum.name() is code metadata, not a user, Media or config value.
            Source category=source==null?Source.SOURCE_NULL:
                classify(((Enum<?>)source).name());
            record(category,stableScreen);
        }catch(Throwable readFailure){failure();}
    }
    private static synchronized void failure() {
        if(failures<MAX)failures++;
        record(Source.UNAVAILABLE,"UNKNOWN");
    }
    static synchronized void record(Source source, String screen) {
        int all=0;for(int x:TOTALS)all+=x;
        if(all>=MAX){overflow++;return;}
        TOTALS[source.ordinal()]++;
        boolean viewer="REELS".equals(screen);
        if(viewer)screenReels++;else screenOther++;
        int cohort=R1CD10ComparisonFlow.cohort();
        if(cohort>=0 && cohort<2) {
            AB[cohort][source.ordinal()]++;
            if(R1CD09DiagnosticGate.active(R1CD09DiagnosticGate.Mode.AUTO)){
                R1CD10ComparisonFlow.sample();
                R1CD10ComparisonFlow.recordFeature(0,viewer && source!=Source.UNAVAILABLE);
            }
        }
        last=source.name()+(viewer?"_REELS_SCREEN_HINT":"_SCREEN_UNVERIFIED");
    }
    static synchronized String brief(){return "D14_NATIVE_REEL_SOURCE="+last+" status="+status;}
    static synchronized String report() {
        StringBuilder b=new StringBuilder("D14 INSTA ECLIPSE / IG455 NATIVE REEL-ORIGIN CORRELATION (PASSIVE, READ ONLY)\n");
        b.append("IG455_APK_SHA256=fb6ca8c21d909d95892e67339746b8fe43f409a9dea11f87d9ab0cee8e97c5b6\n")
            .append("UPSTREAM_SOURCE=ReSo7200/InstaEclipse ee1c81cadfbc.. (relevant files identical to e19ab0def..)\n")
            .append("HOOK=REUSE_EXISTING_D04_X07EF_A05 NONE_ADDED status=").append(status).append('\n')
            .append("NATIVE_SOURCE_COMES_FROM_X07EF_A08_CONFIG_A00_ENUM; ONLY OBSERVED WHEN REELS OPTIONS BUILDER RUNS\n")
            .append("SCREEN_REELS_HINT=").append(screenReels).append(" SCREEN_OTHER_OR_UNKNOWN=").append(screenOther)
            .append(" READ_FAILURES=").append(failures).append(" CAP_OVERFLOW=").append(overflow).append('\n');
        for(Source source:Source.values()){
            int k=source.ordinal();
            b.append(' ').append(source.name()).append(" total=").append(TOTALS[k])
                .append(" manual_A=").append(AB[0][k])
                .append(" manual_B=").append(AB[1][k]).append('\n');
        }
        b.append("ORIGIN=SOURCE_ENUM_HINT_ONLY; FULL_SCREEN_TRANSITION_NOT_VERIFIED; ")
            .append("ACCOUNT_IDENTITY=NOT_VERIFIED MEDIA_IDENTITY=NOT_VERIFIED ACTIONS=OFF AUTO_REARM=FORBIDDEN\n")
            .append("InstaEclipse may also hook this builder. Neither hook ordering nor mutable menu rows are assumed.\n");
        return b.toString();
    }
    static synchronized void resetForTest(){
        Arrays.fill(TOTALS,0);for(int[] row:AB)Arrays.fill(row,0);
        screenReels=screenOther=failures=overflow=0;
        last="NO_NATIVE_BUILDER_CALLBACK";
    }
}
