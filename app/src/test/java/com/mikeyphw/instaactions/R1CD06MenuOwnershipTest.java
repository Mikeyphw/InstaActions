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
    private static final class LotsOfFields {
       private int f01,f02,f03,f04,f05,f06,f07,f08,f09,f10;
       private int f11,f12,f13,f14,f15,f16,f17,f18,f19,f20;
       private int f21,f22,f23,f24,f25,f26,f27,f28,f29,f30;
       private int f31,f32,f33,f34,f35,f36,f37,f38,f39,f40;
       private int f41,f42,f43,f44,f45,f46,f47,f48,f49,f50;
       private int f51,f52,f53,f54,f55,f56,f57,f58,f59,f60;
       private int f61,f62,f63,f64,f65;
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
    @Test public void fieldBudgetRefusesRatherThanIgnoringPossibleAmbiguity() {
        Media m = new Media();
        assertEquals(R1CD06MenuOwnership.Verdict.BUDGET_EXCEEDED,
                R1CD06MenuOwnership.resolve(new LotsOfFields(),new Context(m),m,
                        LotsOfFields.class,Context.class,Media.class,REELS));
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
