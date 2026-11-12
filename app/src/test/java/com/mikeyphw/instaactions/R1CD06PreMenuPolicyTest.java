package com.mikeyphw.instaactions;
import org.junit.Test;
import static org.junit.Assert.*;
public class R1CD06PreMenuPolicyTest {
    @Test public void emptyOrFutureSnapshotFailsClosed(){
        assertTrue(R1CD06PreMenuPolicy.expired(-1,100,3000));
        assertTrue(R1CD06PreMenuPolicy.expired(200,100,3000));
        assertTrue(R1CD06PreMenuPolicy.expired(0,100,0));
    }
    @Test public void threeSecondLimitIsInclusive(){
        assertFalse(R1CD06PreMenuPolicy.expired(100,3100,3000));
        assertTrue(R1CD06PreMenuPolicy.expired(100,3101,3000));
    }
    @Test public void reusedOrSwipedAdapterPositionIsRejected(){
        assertTrue(R1CD06PreMenuPolicy.samePosition(5,5,5));
        assertFalse(R1CD06PreMenuPolicy.samePosition(5,6,5));
        assertFalse(R1CD06PreMenuPolicy.samePosition(5,5,6));
        assertFalse(R1CD06PreMenuPolicy.samePosition(-1,-1,-1));
    }
}
