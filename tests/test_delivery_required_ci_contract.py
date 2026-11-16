"""RCA01 CI repair: Devtool requires a committed, exact repository CI policy.

These checks are additive: they do not weaken or replace Android/JVM/runtime tests.
"""
import json
from pathlib import Path
import unittest

ROOT=Path(__file__).resolve().parents[1]
POLICY=ROOT/'config/delivery-required-ci.json'
EXPECTED={
    'R0 / R1 Android API 102': (411517720, '.github/workflows/android-r0.yml'),
    'Repository contract': (411509546, '.github/workflows/repository-check.yml'),
}

class RequiredCIPolicyContract(unittest.TestCase):
    def setUp(self):
        self.data=json.loads(POLICY.read_text(encoding='utf-8'))

    def test_strict_devtool_schema_and_repository_binding(self):
        d=self.data
        self.assertEqual(set(d), {'schema_version','repository','base_branch',
                                  'event','required_workflows','release_qualification'})
        self.assertIs(type(d['schema_version']),int)
        self.assertEqual(d['schema_version'],1)
        self.assertEqual(d['repository'],'Mikeyphw/InstaActions')
        self.assertEqual(d['base_branch'],'main')
        self.assertEqual(d['event'],'pull_request')
        self.assertEqual(d['release_qualification'],'separate')
        self.assertTrue(1<=len(d['required_workflows'])<=25)

    def test_existing_exact_workflow_ids_and_sources(self):
        rows=self.data['required_workflows']
        self.assertEqual({row['name'] for row in rows},set(EXPECTED))
        self.assertEqual(len({row['workflow_id'] for row in rows}),len(rows))
        for row in rows:
            self.assertEqual(set(row),{'name','workflow_id'})
            self.assertIs(type(row['workflow_id']),int)
            self.assertGreater(row['workflow_id'],0)
            ident,path=EXPECTED[row['name']]
            self.assertEqual(row['workflow_id'],ident)
            source=(ROOT/path).read_text(encoding='utf-8')
            # GitHub accepts an unquoted `on` key; no YAML dependency needed.
            self.assertIn('name: '+row['name'],source)
            self.assertIn('pull_request:',source)
            self.assertIn('jobs:',source)
            self.assertIn('actions/checkout@',source)

    def test_no_fake_pass_or_release_qualification(self):
        self.assertEqual(self.data['event'],'pull_request')
        self.assertEqual(self.data['release_qualification'],'separate')
        self.assertNotIn('success',POLICY.read_text(encoding='utf-8').lower())
        self.assertNotIn('skip',POLICY.read_text(encoding='utf-8').lower())

if __name__=='__main__':
    unittest.main()
