import urllib.request
import csv
import io
from logger import *


class Correspondences:
    def __init__(self, url):
        self.url = url
        self._mapping = {}
        self._package_mapping = {}
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
                self._package_mapping[srcPkg] = Correspondences._get_package(t)

        for f,t in self._package_mapping.items():
            pdbg("Package '%s' => '%s'" % (f, t))

    def fix_line(self, srcline):
        return "TODO: %s" % srcline

    def _get_package(val):
        return ".".join(val.split('.')[:-1])

