package com.mikeyphw.instaactions;

import org.junit.Test;
import static org.junit.Assert.*;

/** Source-derived InstaEclipse uniqueness rules plus negative ambiguity cases. */
public class R1CD06MenuOwnershipTest {
    private static final R1BNavigationPolicy.Screen REELS = R1BNavigationPolicy.Screen.REELS;
    private static final class Media { }
    private static class Owner {
        private Media selected;
        private Owner(Media media) { selected = media; }
    }
    private static final class OwnerWithDuplicate extends Owner {
        private Media alias;
        private OwnerWithDuplicate(Media selected, Media alias) {super(selected);this.alias=alias;}
    }
    private static class Context { private Media selected; Context(Media value) {selected = value;} }
    private static final class Nested {
        private Context hidden;
        private Nested(Media value) {hidden = new Context(value);}
    }
    private static final class StaticOnly {
        private static Media cached;
    }
    private static class LotsOfFields {
       private int f01,f02,f03,f04,f05,f06,f07,f08,f09,f10;
       private int f11,f12,f13,f14,f15,f16,f17,f18,f19,f20;
       private int f21,f22,f23,f24,f25,f26,f27,f28,f29,f30;
       private int f31,f32,f33,f34,f35,f36,f37,f38,f39,f40;
       private int f41,f42,f43,f44,f45,f46,f47,f48,f49,f50;
       private int f51,f52,f53,f54,f55,f56,f57,f58,f59,f60;
       private int f61,f62,f63,f64,f65;
       private int f66,f67,f68,f69,f70,f71,f72,f73,f74,f75;
       private int f76,f77,f78,f79,f80,f81,f82,f83,f84,f85;
       private int f86,f87,f88,f89,f90;
    }
    private static R1CD06MenuOwnership.Verdict inspect(Object owner,Object context,Media media){
        return R1CD06MenuOwnership.resolve(owner,context,media,owner.getClass(),
                context.getClass(),Media.class,REELS);
    }

    @Test public void directOwnerSelectionResolvesUniqueTypedField() {
        Media m = new Media();
        assertEquals(R1CD06MenuOwnership.Verdict.OWNER_DIRECT_MATCH,
                inspect(new Owner(m),new Context(null),m));
    }
    @Test public void exactContextSelectionResolvesUniqueTypedField() {
        Media m = new Media();
        assertEquals(R1CD06MenuOwnership.Verdict.CONTEXT_DIRECT_MATCH,
                inspect(new Owner(null),new Context(m),m));
    }
    @Test public void bothMatchingSourcesAreAcceptedOnlyAsMenuOwnership() {
        Media m = new Media();
        assertEquals(R1CD06MenuOwnership.Verdict.OWNER_AND_CONTEXT_MATCH,
                inspect(new Owner(m),new Context(m),m));
    }
    @Test public void repeatedAliasedFieldsAreNotAmbiguous() {
        Media m = new Media();
        assertEquals(R1CD06MenuOwnership.Verdict.OWNER_DIRECT_MATCH,
                inspect(new OwnerWithDuplicate(m,m),new Context(null),m));
    }
    @Test public void distinctDirectFieldsAreAmbiguous() {
        Media m = new Media();
        assertEquals(R1CD06MenuOwnership.Verdict.AMBIGUOUS,
                inspect(new OwnerWithDuplicate(m,new Media()),new Context(null),m));
    }
    @Test public void mismatchedOwnerDoesNotGetOverriddenByMatchingContext() {
        Media m = new Media();
        assertEquals(R1CD06MenuOwnership.Verdict.MISMATCH,
                inspect(new Owner(new Media()),new Context(m),m));
    }
    @Test public void nestedOrPreloadedObjectIsNeverMistakenForOwner() {
        Media m = new Media();
        assertEquals(R1CD06MenuOwnership.Verdict.NO_DIRECT_OWNER,
                R1CD06MenuOwnership.resolve(new Nested(m),new Nested(null),m,
                        Nested.class,Nested.class,Media.class,REELS));
    }
    @Test public void staticFieldsAreIgnored() {
        Media m = new Media(); StaticOnly.cached = m;
        assertEquals(R1CD06MenuOwnership.Verdict.NO_DIRECT_OWNER,
                R1CD06MenuOwnership.resolve(new StaticOnly(),new Context(null),m,
                        StaticOnly.class,Context.class,Media.class,REELS));
    }
    @Test public void unstableAndWrongTypesRefuseEarly() {
        Media m = new Media();
        assertEquals(R1CD06MenuOwnership.Verdict.SCREEN_UNSTABLE,
                R1CD06MenuOwnership.resolve(new Owner(m),new Context(m),m,
                        Owner.class,Context.class,Media.class,R1BNavigationPolicy.Screen.UNKNOWN));
        assertEquals(R1CD06MenuOwnership.Verdict.WRONG_MEDIA_TYPE,
                R1CD06MenuOwnership.resolve(new Owner(m),new Context(m),"not media",
                        Owner.class,Context.class,Media.class,REELS));
    }
    @Test public void exactOwnerAndContextTypesMustMatch() {
        Media m = new Media();
        assertEquals(R1CD06MenuOwnership.Verdict.WRONG_OWNER_TYPE,
                R1CD06MenuOwnership.resolve(new Object(),new Context(m),m,
                        Owner.class,Context.class,Media.class,REELS));
    }
    private static final class WideOwnerWithMedia extends LotsOfFields {
        private Media selected;
        WideOwnerWithMedia(Media item) { selected = item; }
    }
    private static class DeepOwner0 { Media selected; DeepOwner0(Media m){ selected=m; } }
    private static class DeepOwner1 extends DeepOwner0 { DeepOwner1(Media m){ super(m); } }
    private static class DeepOwner2 extends DeepOwner1 { DeepOwner2(Media m){ super(m); } }
    private static class DeepOwner3 extends DeepOwner2 { DeepOwner3(Media m){ super(m); } }
    private static class DeepOwner4 extends DeepOwner3 { DeepOwner4(Media m){ super(m); } }
    private static class DeepOwner5 extends DeepOwner4 { DeepOwner5(Media m){ super(m); } }
    private static class DeepOwner6 extends DeepOwner5 { DeepOwner6(Media m){ super(m); } }
    private static class DeepOwner7 extends DeepOwner6 { DeepOwner7(Media m){ super(m); } }
    private static class DeepOwner8 extends DeepOwner7 { DeepOwner8(Media m){ super(m); } }
    private static class DeepOwner9 extends DeepOwner8 { DeepOwner9(Media m){ super(m); } }
    private static class DeepOwner10 extends DeepOwner9 { DeepOwner10(Media m){ super(m); } }
    private static class DeepOwner11 extends DeepOwner10 { DeepOwner11(Media m){ super(m); } }
    private static class DeepOwner12 extends DeepOwner11 { DeepOwner12(Media m){ super(m); } }
    private static class DeepOwner13 extends DeepOwner12 { DeepOwner13(Media m){ super(m); } }
    private static final class MediaDenseOwner {
        Media a01,a02,a03,a04,a05,a06,a07,a08,a09,a10;
        Media a11,a12,a13,a14,a15,a16,a17;
    }
    @Test public void irrelevantObfuscatedFieldsDoNotSpendMediaBudget() {
        Media m = new Media();
        assertEquals(R1CD06MenuOwnership.Verdict.OWNER_DIRECT_MATCH,
                R1CD06MenuOwnership.resolve(new WideOwnerWithMedia(m),new Context(null),m,
                        WideOwnerWithMedia.class,Context.class,Media.class,REELS));
    }
    @Test public void matchingMediaFieldBudgetStillRejectsTruncation() {
        Media m = new Media();
        assertEquals(R1CD06MenuOwnership.Verdict.OWNER_MEDIA_FIELD_LIMIT,
                R1CD06MenuOwnership.resolve(new MediaDenseOwner(),new Context(null),m,
                        MediaDenseOwner.class,Context.class,Media.class,REELS));
    }
    @Test public void deepOwnerHierarchyFailsWithPreciseReason() {
        Media m = new Media();
        assertEquals(R1CD06MenuOwnership.Verdict.OWNER_CLASS_DEPTH_LIMIT,
                R1CD06MenuOwnership.resolve(new DeepOwner13(m),new Context(null),m,
                        DeepOwner13.class,Context.class,Media.class,REELS));
    }
    @Test public void contextBudgetIsDistinctFromOwnerBudget() {
        Media m = new Media();
        assertEquals(R1CD06MenuOwnership.Verdict.CONTEXT_MEDIA_FIELD_LIMIT,
                R1CD06MenuOwnership.resolve(new Owner(m),new MediaDenseOwner(),m,
                        Owner.class,MediaDenseOwner.class,Media.class,REELS));
    }
    @Test public void fieldBudgetRefusesRatherThanIgnoringPossibleAmbiguity() {
        Media m = new Media();
        assertEquals(R1CD06MenuOwnership.Verdict.OWNER_CLASS_DEPTH_LIMIT,
                R1CD06MenuOwnership.resolve(new DeepOwner13(m),new Context(m),m,
                        DeepOwner13.class,Context.class,Media.class,REELS));
    }
    @Test public void optionalNullContextDoesNotHideDirectOwner() {
        Media m = new Media();
        assertEquals(R1CD06MenuOwnership.Verdict.OWNER_DIRECT_MATCH,
                R1CD06MenuOwnership.resolve(new Owner(m),null,m,
                        Owner.class,Context.class,Media.class,REELS));
        assertEquals(R1CD06MenuOwnership.Verdict.WRONG_OWNER_TYPE,
                R1CD06MenuOwnership.resolve(null,new Context(m),m,
                        Owner.class,Context.class,Media.class,REELS));
    }
}
