"""RCA02 current-runtime provenance, privacy, fail-closed and A1/B/A2 checks."""
from pathlib import Path
import json
import shutil
import subprocess
import tempfile
import unittest

ROOT=Path(__file__).resolve().parents[1]
J=ROOT/'app/src/main/java/com/mikeyphw/instaactions'

class RCA02Contract(unittest.TestCase):
    def test_source_verified_caller_method_shapes_and_passthrough(self):
        entry=(J/'InstaActionsEntry.java').read_text(encoding='utf-8')
        for token in ('Class.forName("X.06nv",false,loader)',
                      'Class.forName("X.06ck",false,loader)',
                      'Class.forName("X.043F",false,loader)',
                      'getDeclaredMethod("A03",arg,boolean.class)',
                      'getDeclaredMethod("A00",owner)',
                      'java.lang.reflect.Modifier.isStatic',
                      'getReturnType()!=media', 'getReturnType()!=void.class',
                      'ina-rca02-source-x06nv-A03',
                      'ina-rca02-source-x043f-A00',
                      'R1CRCA02LineageLedger.enterWriter(',
                      'R1CRCA02LineageLedger.leaveWriter(previous)',
                      'R1CRCA02LineageLedger.writerStatus(delta,legacy)'):
            self.assertIn(token,entry)
        self.assertEqual(entry.count('try {return chain.proceed();}'),2)
        self.assertNotIn('chain.setResult(',entry)
        self.assertNotIn('R1CD07Recovery.arm(',entry)

    def test_original_media_calls_and_early_branch_are_not_mutated(self):
        entry=(J/'InstaActionsEntry.java').read_text(encoding='utf-8')
        gate=(J/'R1CD07GateTrace.java').read_text(encoding='utf-8')
        for token in ('R1CRCA02LineageLedger.mediaGet(chain.getThisObject(),original)',
                      'R1CRCA02LineageLedger.mediaSet(chain.getThisObject(),chain.getArg(0))',
                      'R1CRCA02LineageLedger.d3r(original)',
                      'R1CRCA02LineageLedger.config(original)',
                      'R1CRCA02LineageLedger.enterUi(',
                      'R1CRCA02LineageLedger.finishUi(rca02Frame,',
                      'R1CRCA02LineageLedger.cancelUi(rca02Frame)',
                      'Object original=chain.proceed()',
                      'return original;'):
            self.assertIn(token,entry)
        self.assertIn('R1CD07BranchInputs.UNAVAILABLE:probe.classify(selector)',gate)
        self.assertIn('R1CD07BranchInputs probe=branchInputs',gate)
        self.assertIn('R1CD04PassiveProbe.isInstalled()',entry)

    def test_privacy_bounds_and_fail_closed_under_missing_identity(self):
        ledger=(J/'R1CRCA02LineageLedger.java').read_text(encoding='utf-8')
        for token in ('WeakReference<Object>', 'live==ref','MAX_OBJECTS=256',
                      'MAX_EVENTS=4096','MAX_TIMELINE=112','ThreadLocal<Integer>',
                      'ThreadLocal<UiFrame>', 'MEDIA_GRAPHQL_LINK=UNVERIFIED',
                      'SERVER_SOURCE=UNVERIFIED','SAME_REEL=UNVERIFIED',
                      'ACCOUNT_IDENTITY=MANUAL_UNVERIFIED',
                      'A_B_A2_EXACT_D3R=', 'UI_SINGLETON_DATA_MISSING',
                      'UI_SINGLETON_NEGATIVE_D3R','UI_SINGLETON_UNRESOLVED'):
            self.assertIn(token,ledger)
        for forbidden in ('getUsername(', 'getMediaId(', 'getToken(',
                          'getCaption(', 'getAccountId(', 'performClick(',
                          'setResult(', 'Runtime.getRuntime(', 'Socket('):
            self.assertNotIn(forbidden,ledger)
        self.assertNotIn('IdentityHashMap',ledger)

    def test_single_active_current_diagnostic_and_manual_confirmation(self):
        fab=(J/'R1CRCA01Fab.java').read_text(encoding='utf-8')
        entry=(J/'InstaActionsEntry.java').read_text(encoding='utf-8')
        bridge=(J/'R1CD12AndroidBridge.java').read_text(encoding='utf-8')
        self.assertIn('chip.setText("RCA02")',fab)
        self.assertIn('Confirm manual switch to B',fab)
        self.assertIn('Confirm manual return to A',fab)
        self.assertIn('Copy RCA02 A1/B/A2 report',fab)
        self.assertIn('R1CRCA02LineageLedger.begin(pass)',fab)
        self.assertIn('R1CRCA02LineageLedger.stop()',fab)
        self.assertIn('NATIVE_SNAPSHOT[pass]',fab)
        self.assertIn('R1CD07Recovery.off("RCA02 diagnostic-only UI',fab)
        self.assertIn('if(phase!=3)',fab)
        self.assertIn('if(phase!=6)',fab)
        self.assertIn('R1CRCA01Fab.onResumed(',entry+bridge)
        self.assertNotIn('R1CD05FabPanel.onResumed(',entry+bridge)
        self.assertNotIn('R1CD07Recovery.arm(',fab)
        self.assertNotIn('performClick(',fab)

    def test_policy_and_current_only_app_guide(self):
        flags=json.loads((ROOT/'project.json').read_text(encoding='utf-8'))
        self.assertIs(flags['rca02_source_implemented'],True)
        self.assertIs(flags['rca02_old_fab_active'],False)
        self.assertIs(flags['rca02_graphql_to_media_link_verified'],False)
        self.assertIs(flags['rca02_server_origin_verified'],False)
        self.assertEqual(flags['rca02_device_acceptance'],'NOT_RUN')
        self.assertIs(flags['rca02_hosted_ci_qualified'],False)
        guide=(J/'MainActivity.java').read_text(encoding='utf-8')
        self.assertIn('RCA02 · A1 → B → A2 · active diagnostics',guide)
        self.assertIn('legacy.setVisibility(View.GONE)',guide)
        self.assertLess(guide.index('RCA02 · A1 → B → A2'),guide.index('pickerGuide=card('))

    def test_apk_callsite_audit_is_reproducible_and_strictly_pinned(self):
        script=(ROOT/'tools/rca02_verify_afv_callers.py').read_text(encoding='utf-8')
        for x in ('--base-apk','4f5bc1bd5e7b2be6e0634200d01926ddea1096c6fc780312fe0452ef66d28c0a',
                  'classes.dex','LX/06nv;','A03', 'classes6.dex','LX/043F;','A00',
                  'RCA02_IG455_AFV_DIRECT_CALLSITES_PASS','unsupported Instagram base.apk'):
            self.assertIn(x,script)
        self.assertNotIn('urlopen(',script)
        self.assertNotIn('requests.',script)

    @unittest.skipUnless(shutil.which('javac') and shutil.which('java'),'JDK unavailable')
    def test_native_jvm_39_provenance_negatives(self):
        with tempfile.TemporaryDirectory(prefix='ina-rca02-jvm-') as td:
            args=['javac','-encoding','UTF-8','-d',td,
                  str(J/'R1CRCA02LineageLedger.java'),
                  str(ROOT/'tests/java/com/mikeyphw/instaactions/R1CRCA02LineageSmoke.java')]
            p=subprocess.run(args,capture_output=True,text=True,timeout=40)
            self.assertEqual(p.returncode,0,p.stdout+p.stderr)
            p=subprocess.run(['java','-cp',td,'com.mikeyphw.instaactions.R1CRCA02LineageSmoke'],
                capture_output=True,text=True,timeout=40)
            self.assertEqual(p.returncode,0,p.stdout+p.stderr)
            self.assertIn('INA_RCA02_LINEAGE_NEGATIVES_PASS=39',p.stdout)

if __name__=='__main__':unittest.main()
