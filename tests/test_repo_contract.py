import json
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]


class RepoContract(unittest.TestCase):
    def test_project_metadata(self):
        data = json.loads((ROOT / 'project.json').read_text(encoding='utf-8'))
        self.assertEqual(data['name'], 'InstaActions')
        self.assertEqual(data['package_id'], 'com.mikeyphw.instaactions')
        self.assertEqual(data['scope_packages'], ['com.instagram.android'])
        self.assertEqual(data['xposed_api'], 102)
        self.assertEqual(data['ci_build_policy'], 'github-actions-only')
        self.assertEqual(data['instaeclipse_integration'], 'independent-coexistence')

    def test_documents_exist(self):
        required = [
            'README.md', 'ROADMAP.md', 'docs/ARCHITECTURE.md',
            'docs/COMPATIBILITY.md', 'docs/TEST_PLAN.md',
            'docs/HOOK_INVENTORY.md', 'docs/SECURITY.md',
        ]
        for rel in required:
            with self.subTest(path=rel):
                p = ROOT / rel
                self.assertTrue(p.is_file() and p.stat().st_size > 200, rel)

    def test_bootstrap_status_is_explicit(self):
        data = json.loads((ROOT / 'project.json').read_text(encoding='utf-8'))
        if data['phase'] == 'bootstrap':
            self.assertFalse(data['r0_android_project_implemented'])
        else:
            self.assertIn(data['phase'], {'r0', 'r1', 'r2', 'r3', 'r4', 'r5', 'r6', 'r7', 'r8'})


if __name__ == '__main__':
    unittest.main()
