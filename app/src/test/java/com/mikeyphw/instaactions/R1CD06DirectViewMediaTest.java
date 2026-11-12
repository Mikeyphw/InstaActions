package com.mikeyphw.instaactions;

import org.junit.Test;
import static org.junit.Assert.*;

/** Pure JVM negative/positive tests of live view-holder identity selection. */
public class R1CD06DirectViewMediaTest {
    static final class Media { }
    static final class Holder {
        private final Media media;
        Holder(Media media) { this.media = media; }
    }
    static final class IrrelevantHolder { private final String irrelevant = "ignored"; }
    static final class DoubleHolder {
        private final Media a,b;
        DoubleHolder(Media a,Media b){this.a=a;this.b=b;}
    }

    @Test public void uniqueExactTypedFieldMatchesWithoutReadingContents() {
        Media m = new Media();
        assertEquals(R1CD06DirectViewMedia.Result.MATCH,
                R1CD06DirectViewMedia.compare(new Object[]{new Holder(m)},Media.class,m));
    }
    @Test public void absentOrUnrelatedViewIsInconclusive() {
        Media m = new Media();
        assertEquals(R1CD06DirectViewMedia.Result.ABSENT,
                R1CD06DirectViewMedia.compare(new Object[]{new IrrelevantHolder()},Media.class,m));
        assertEquals(R1CD06DirectViewMedia.Result.ABSENT,
                R1CD06DirectViewMedia.compare(new Object[]{},Media.class,m));
    }
    @Test public void exactFieldValueMustMatchMenuObjectByReference() {
        assertEquals(R1CD06DirectViewMedia.Result.MISMATCH,
                R1CD06DirectViewMedia.compare(new Object[]{new Holder(new Media())},Media.class,new Media()));
    }
    @Test public void twoDifferentViewMediaObjectsRejectAmbiguity() {
        Media a=new Media(),b=new Media();
        assertEquals(R1CD06DirectViewMedia.Result.AMBIGUOUS,
                R1CD06DirectViewMedia.compare(new Object[]{new Holder(a),new Holder(b)},Media.class,a));
        assertEquals(R1CD06DirectViewMedia.Result.AMBIGUOUS,
                R1CD06DirectViewMedia.compare(new Object[]{new DoubleHolder(a,b)},Media.class,a));
    }
    @Test public void aliasedSameMediaAcrossViewsRemainsUnique() {
        Media a=new Media();
        assertEquals(R1CD06DirectViewMedia.Result.MATCH,
                R1CD06DirectViewMedia.compare(new Object[]{new Holder(a),new Holder(a)},Media.class,a));
    }
    @Test public void structuralBudgetCannotTruncateConflictingCandidate() {
        Object[] holders = new Object[R1CD06DirectViewMedia.MAX_VIEWS+1];
        java.util.Arrays.fill(holders,new IrrelevantHolder());
        holders[0]=new Holder(new Media());
        assertEquals(R1CD06DirectViewMedia.Result.VIEW_COUNT_LIMIT,
                R1CD06DirectViewMedia.compare(holders,Media.class,new Media()));
    }
    @Test public void unexpectedNullHolderRejectsRatherThanSelectingStaleReference() {
        Media a=new Media();
        assertEquals(R1CD06DirectViewMedia.Result.REFLECTION_FAILED,
                R1CD06DirectViewMedia.compare(new Object[]{new Holder(a),null},Media.class,a));
    }
}
