"""HF03: prove the test suite never resolves Hermes/Termux's `tools` package."""
from pathlib import Path
import subprocess
import sys
import unittest

ROOT = Path(__file__).resolve().parents[1]
POLICY = ROOT / 'tools' / 'r1c_d05_evidence_policy.py'
TEST = ROOT / 'tests' / 'test_r1c_d05_evidence_policy.py'

class HF03ExternalToolsCollision(unittest.TestCase):
    def test_source_is_pinned_to_repository_not_site_packages(self):
        self.assertIn('importlib.util.spec_from_file_location', TEST.read_text())
        self.assertNotIn('from tools import r1c_d05_evidence_policy', TEST.read_text())
        self.assertTrue(POLICY.is_file())

    def test_import_succeeds_with_foreign_tools_preloaded(self):
        # Reproduces the Termux Hermes failure: a different `tools` module is
        # already imported before unittest discovery sees the repository.
        script = r"""
import importlib.util, pathlib, sys, types
root = pathlib.Path(sys.argv[1]).resolve()
shadow = types.ModuleType('tools')
shadow.__path__ = ['/data/data/com.termux/files/usr/share/hermes-agent/tools']
shadow.__file__ = '/data/data/com.termux/files/usr/share/hermes-agent/tools/__init__.py'
sys.modules['tools'] = shadow
path = root/'tests'/'test_r1c_d05_evidence_policy.py'
spec = importlib.util.spec_from_file_location('_ina_collision_test', path)
module = importlib.util.module_from_spec(spec)
sys.modules[spec.name] = module
spec.loader.exec_module(module)
expected = (root/'tools'/'r1c_d05_evidence_policy.py').resolve()
assert pathlib.Path(module.ep.__file__).resolve() == expected
marker = module.reference()
parsed = module.ep.parse_logcat(module.log_line(marker))
assert len(parsed.records) == 1 and parsed.rejected_markers == 0
assert sys.modules['tools'] is shadow
print('INA_R1C_D05_HF03_FOREIGN_TOOLS_SHADOW_PASS')
"""
        p = subprocess.run([sys.executable, '-c', script, str(ROOT)],
                           capture_output=True, text=True, timeout=20, cwd=ROOT)
        self.assertEqual(p.returncode, 0, p.stdout + '\n' + p.stderr)
        self.assertIn('INA_R1C_D05_HF03_FOREIGN_TOOLS_SHADOW_PASS', p.stdout)

if __name__ == '__main__':
    unittest.main()
