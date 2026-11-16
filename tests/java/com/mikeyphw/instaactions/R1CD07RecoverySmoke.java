package com.mikeyphw.instaactions;

/** HF13 evidence, lifecycle, races, strict original-keys and rollback suite. */
public final class R1CD07RecoverySmoke {
    private static int checks;
    private static long now=100_000_000_000L;
    private static void ok(boolean b,String msg){if(!b)throw new AssertionError(msg);checks++;}
    private static void frame(int a,int code,int term){
        R1CD07ExactGateTrial.recordFrame(a,R1CD07BranchInputs.PRESENT,new int[]{-1,code},term);
    }
    private static void setup() {
        R1CD07Recovery.off("new baseline");
        R1CD07ExactGateTrial.reset();
        R1CD07ExactGateTrial.setDeoptimized(true);
        R1CD07ExactGateTrial.onCaptureStart(0);
        for(int i=0;i<12;i++)frame(0,1,0);
        ok(R1CD07ExactGateTrial.eligibleForHF13Recovery(),"fresh A-only evidence from proven HF12");
    }
    private static boolean falseValue(Object x) {return Boolean.FALSE.equals(x);}
    private static boolean trueValue(Object x) {return Boolean.TRUE.equals(x);}
    private static Object exact(int cohort,Object key,Object original) {
        R1CD07PredicateTrace.Frame f=R1CD07PredicateTrace.enter(cohort,R1CD07InputPresence.NESTED_PRESENT,R1CD07BranchInputs.PRESENT);
        Object changed=R1CD07PredicateTrace.noteExactData(key,original);
        // Synthetic Object is not a real IG455 UI variant; do not allow it to
        // contaminate the exact-native eligibility baseline under test.
        f.changedByTrial=true;
        R1CD07PredicateTrace.leave(f,new Object());
        return changed;
    }
    public static void main(String[] args) throws Exception {
        R1CD07Recovery.setClockForTest(()->now);
        setup();
        ok(falseValue(exact(0,0xdf82522e,Boolean.FALSE)),"default OFF");
        ok(R1CD07Recovery.arm(1,true,true).startsWith("REFUSED"),"B cannot arm");
        ok(R1CD07Recovery.arm(0,false,true).startsWith("REFUSED"),"unverified exact APK");
        ok(R1CD07Recovery.arm(0,true,false).startsWith("REFUSED"),"missing native receiver");
        ok(R1CD07Recovery.arm(0,true,true).startsWith("ENABLED"),"manual Reels-tab consent");
        ok(exact(0,0xdf82522e,null)==null,"nullable getter remains nullable");
        ok(trueValue(exact(0,0xdf82522e,Boolean.TRUE)),"native true unchanged");
        ok(falseValue(exact(0,0xdf82522f,Boolean.FALSE)),"wrong int key unchanged");
        ok(falseValue(R1CD07Recovery.maybeOverride(R1CD07Recovery.epoch(),0,
            R1CD07BranchInputs.MISSING,false,Boolean.FALSE)),"native A03 missing fail closed");
        ok(falseValue(R1CD07Recovery.maybeOverride(R1CD07Recovery.epoch(),0,
            R1CD07BranchInputs.UNAVAILABLE,false,Boolean.FALSE)),"unavailable data fail closed");
        ok(falseValue(exact(1,0xdf82522e,Boolean.FALSE)),"B never changed");
        ok(trueValue(exact(0,0xdf82522e,Boolean.FALSE)),"HF12 native Repost gate works");
        // Nested / cross-generation work must not leak into any new capture.
        R1CD07PredicateTrace.Frame parent=R1CD07PredicateTrace.enter(0,R1CD07InputPresence.NESTED_PRESENT,R1CD07BranchInputs.PRESENT);
        R1CD07PredicateTrace.Frame nested=R1CD07PredicateTrace.enter(0,R1CD07InputPresence.NESTED_PRESENT,R1CD07BranchInputs.PRESENT);
        ok(falseValue(R1CD07PredicateTrace.noteExactData(0xdf82522e,Boolean.FALSE)),"nested frame refused");
        nested.changedByTrial=true;
        parent.changedByTrial=true;
        R1CD07PredicateTrace.leave(nested,null);
        R1CD07PredicateTrace.leave(parent,null);
        long prior=R1CD07Recovery.epoch();
        R1CD07Recovery.onCaptureTransition();
        ok(falseValue(R1CD07Recovery.maybeOverride(prior,0,R1CD07BranchInputs.PRESENT,false,Boolean.FALSE)),
            "capture/account switch disarms in-flight frame");
        ok(!R1CD07Recovery.isEnabled(),"switch OFF");
        setup();R1CD07Recovery.arm(0,true,true);
        R1CD07Recovery.onActivityExit();
        ok(falseValue(exact(0,0xdf82522e,Boolean.FALSE)),"pause/destroy immediately OFF");
        setup();R1CD07Recovery.arm(0,true,true);
        now+=R1CD07Recovery.LEASE_NS;
        ok(falseValue(exact(0,0xdf82522e,Boolean.FALSE)),"lease timeout fail closed");
        setup();R1CD07Recovery.arm(0,true,true);
        for(int i=0;i<R1CD07Recovery.LIMIT;i++)ok(trueValue(exact(0,0xdf82522e,Boolean.FALSE)),
            "within changed-call cap #"+i);
        ok(!R1CD07Recovery.isEnabled(),"limit auto OFF");
        ok(falseValue(exact(0,0xdf82522e,Boolean.FALSE)),"after limit original result");
        setup();R1CD07Recovery.arm(0,true,true);
        R1CD07ExactGateTrial.setDeoptimized(false);
        ok(falseValue(exact(0,0xdf82522e,Boolean.FALSE)),"deoptimization loss fail closed");
        setup();R1CD07Recovery.arm(0,true,true);
        frame(0,0,0);
        ok(falseValue(exact(0,0xdf82522e,Boolean.FALSE)),"A contradictory native true revokes");
        setup();R1CD07Recovery.arm(0,true,true);
        R1CD07Recovery.onHookFailure();
        ok(falseValue(exact(0,0xdf82522e,Boolean.FALSE)),"hook installation failure revokes");
        setup();frame(1,0,1);frame(1,1,0);
        ok(!R1CD07ExactGateTrial.eligibleForHF13Recovery(),"B evidence if present must fully qualify");
        setup();R1CD07ExactGateTrial.onCaptureStart(0);
        ok(!R1CD07ExactGateTrial.eligibleForHF13Recovery(),"no stale A eligibility on new recording");
        for(int i=0;i<5;i++)frame(0,1,0);
        ok(R1CD07ExactGateTrial.eligibleForHF13Recovery(),"fresh five A observations requalify");
        R1CD07ExactGateTrial.onCaptureStop();
        ok(!R1CD07ExactGateTrial.eligibleForHF13Recovery(),"Stop A revokes fresh window");
        R1CD07ExactGateTrial.onCaptureStart(1);
        ok(!R1CD07ExactGateTrial.eligibleForHF13Recovery(),"Start B revokes A window");
        setup();R1CD07Recovery.arm(0,true,true);
        java.util.concurrent.CountDownLatch gate=new java.util.concurrent.CountDownLatch(1);
        java.util.concurrent.atomic.AtomicInteger modifications=new java.util.concurrent.atomic.AtomicInteger();
        java.util.concurrent.atomic.AtomicReference<Throwable> error=new java.util.concurrent.atomic.AtomicReference<>();
        Thread[] workers=new Thread[12];
        for(int i=0;i<workers.length;i++){
            workers[i]=new Thread(()->{try {gate.await();for(int j=0;j<16;j++){
                if(trueValue(exact(0,0xdf82522e,Boolean.FALSE)))modifications.incrementAndGet();
            }}catch(Throwable e){error.compareAndSet(null,e);}});
            workers[i].start();
        }
        gate.countDown();for(Thread th:workers)th.join();
        ok(error.get()==null,"parallel callbacks safe");
        ok(modifications.get()==R1CD07Recovery.LIMIT,"strict global cap under concurrency");
        ok(!R1CD07Recovery.isEnabled(),"concurrent exhaustion OFF");
        R1CD07Recovery.off("user disabled");
        ok(falseValue(exact(0,0xdf82522e,Boolean.FALSE)),"explicit OFF reversible");
        R1CD07Recovery.restoreClockForTest();
        System.out.println("INA_D07_RECOVERY_SAFETY_NEGATIVES_PASS="+checks);
    }
}
