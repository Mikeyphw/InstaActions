package com.mikeyphw.instaactions;

/** Exact two-base-APK allowlist and negative nonmutating policy. */
public final class R1CRCA01PolicySmoke {
    private static int checks;
    private static void ok(boolean v,String reason){if(!v)throw new AssertionError(reason);checks++;}
    public static void main(String[] args) {
        String old="fb6ca8c21d909d95892e67339746b8fe43f409a9dea11f87d9ab0cee8e97c5b6";
        String apk="4f5bc1bd5e7b2be6e0634200d01926ddea1096c6fc780312fe0452ef66d28c0a";
        ok(R1CD04Policy.validBuild("455.0.0.8.72",old),"reference accepted");
        ok(R1CD04Policy.validBuild("455.0.0.8.72",apk),"APKM base accepted");
        ok(!R1CD04Policy.validBuild("456.0.0.8.72",apk),"other version refused");
        ok(!R1CD04Policy.validBuild("455.0.0.8.72","0".repeat(64)),"other digest refused");
        ok(!R1CD04Policy.validBuild(null,apk),"null version refused");
        ok(!R1CD04Policy.validBuild("455.0.0.8.72",null),"null digest refused");
        ok(!R1CD04Policy.ACTIONS_ENABLED,"general actions remain off");
        ok(!R1CD04Policy.MEDIA_IDENTITY_VERIFIED,"media identity unqualified");
        ok(R1CD04Policy.TARGET_APK_BYTES==238346353L,"exact base size pinned");
        System.out.println("INA_RCA01_EXACT_APK_NEGATIVES_PASS="+checks);
    }
}
