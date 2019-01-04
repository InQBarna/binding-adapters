import urllib.request
import csv
import io
import re
from logger import *


class Correspondences:
    def __init__(self, url):
        self.url = url
        self._mapping = {}
        self._package_mapping = []
        self._do_create_correspondences()

    def _do_create_correspondences(self):
        with urllib.request.urlopen(self.url) as response:
            with io.TextIOWrapper(response) as content:
                mapping = csv.DictReader(content)
                fromKey = None
                toKey = None
                for row in mapping:
                    if not fromKey or not toKey:
                        fromKey = mapping.fieldnames[0]
                        toKey = mapping.fieldnames[1]
                        pdbg("FromKey = %s" % fromKey)
                        pdbg("ToKey = %s" % toKey)
                    praw("rowFrom = %s" % row[fromKey])
                    self._mapping[row[fromKey]] = row[toKey]

        for f,t in self._mapping.items():
            pdbg("From '%s' -> '%s'" % (f, t))
            srcPkg = Correspondences._get_package(f)
            if srcPkg not in self._package_mapping:
                self._package_mapping.append(_PackageReplacement(srcPkg, Correspondences._get_package(t)))

        for repl in self._package_mapping:
            pdbg(str(repl))

    def fix_line(self, srcline):
        result = srcline
        for repl in self._package_mapping:
            result = repl.do_replace(result)

        return result

    def _get_package(val):
        return ".".join(val.split('.')[:-1]) + "."


class _PackageReplacement:
    def __init__(self, srcPackage, dstPackage):
        escaped = re.escape(srcPackage)
        self._matcher = re.compile(f"{escaped}(\w+)", re.ASCII)
        self._dstPackage = dstPackage

    def __str__(self):
        return f"Matcher={self._matcher} ==> Replaces to: {self._dstPackage}"

    def _target_replace(self, matchobj):
        replacement = self._dstPackage + matchobj.group(1)
        praw("Changed '%s' with '%s'" % (matchobj.group(0), replacement))
        return replacement

    def do_replace(self, srcLine):
        return self._matcher.sub(self._target_replace, srcLine)
