import urllib.request

# 
# Copyright 2014 InQBarna Kenkyuu Jo SL 
# 
# Licensed under the Apache License, Version 2.0 (the "License"); 
# you may not use this file except in compliance with the License. 
# You may obtain a copy of the License at 
# 
#     http://www.apache.org/licenses/LICENSE-2.0 
# 
# Unless required by applicable law or agreed to in writing, software 
# distributed under the License is distributed on an "AS IS" BASIS, 
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
# See the License for the specific language governing permissions and 
# limitations under the License. 
#  

import csv
import io
import re
from walker import SourceFile
from logger import *
from collections.abc import MutableMapping


class ProcessingError(Exception):
    def __init__(self, msg):
        self.message = msg

class Correspondences:
    def __init__(self, url, artifact_url):
        self.url = url
        self.artifact_url = artifact_url
        self._mapping = {}
        self._package_mapping = []
        self._artifact_replacements = []
        self._version_replacements = []
        self._star_projections = _StarProjections()
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
                    srcClass = row[fromKey]
                    dstClass = row[toKey]
                    self._mapping[srcClass] = dstClass
                    srcPkg = _get_package(srcClass)
                    dstPkg = _get_package(dstClass)
                    self._star_projections.add(srcPkg, dstPkg)

        with urllib.request.urlopen(self.artifact_url) as response:
            with io.TextIOWrapper(response) as content:
                mapping = csv.DictReader(content)
                fromKey = None
                toKey = None
                for row in mapping:
                    if not fromKey or not toKey:
                        fromKey = mapping.fieldnames[0]
                        toKey = mapping.fieldnames[1]
                    self._artifact_replacements.append(_ArtifactReplacement(row[fromKey], row[toKey]))

        for key,repl in self._mapping.items():
            pdbg("%s -> %s" %(key, repl))

        for key, destinations in self._star_projections:
            if (len(destinations) > 1):
                pdbg(f"Source package '{key}' maps to more than one destination: {destinations}")
            else:
                pdbg(f"Source package '{key}*' can be replaced with {destinations[0]}*")

        for repl in self._artifact_replacements:
            pdbg(str(repl))

    def extend_with_buildfiles(self, buildfiles):
        for s in buildfiles:
            for idx, line in enumerate(s.lines()):
                versionVar = self._extract_version_var(line, idx + 1, s)
                if versionVar:
                    self._version_replacements.append(versionVar)

        for repl in self._version_replacements:
            pdbg("Found version replacement: %s" % repl)

    def _extract_version_var(self, line, lineno, srcfile):
        for ar in self._artifact_replacements:
           vrepl = ar.matches(srcfile, line, lineno)
           if vrepl:
               return vrepl
        return None

    def _package_match_replacement(self, matchobj):
        matched = matchobj[0]
        subst = self._mapping.get(matched)
        if subst:
            praw(f"Subst '{matched}' with '{subst}'")
            return subst

        star_replacement = self._star_projections.unique_star_projection(matched)
        if star_replacement:
            praw(f"Star-Subst '{matched}' with '{star_replacement}'")
            return star_replacement

        return matched

    _qname_regex = re.compile(r"(?:\w[\w0-9]*\.)+(?:\w[\w0-9]*|\*{1})")
    def fix_source_line(self, srcline, srcType):
        result = srcline
        if srcType == SourceFile.TYPE_SRC or srcType == SourceFile.TYPE_UI:
            return Correspondences._qname_regex.sub(self._package_match_replacement, result)
        elif srcType == SourceFile.TYPE_BUILDFILE:
            res = None
            for repl in self._artifact_replacements:
                out = repl.do_replace(srcline)
                if out:
                    res = out
                    break
            return out if out else srcline
        elif srcType == SourceFile.TYPE_CONFIG:
            pdbg("We're not changing any line of config files")
            return srcline
        else:
            raise ValueError("Invalid file type %s" % srcType)

def _get_package(val):
    return ".".join(val.split('.')[:-1]) + "."

class _StarProjections():
    def __init__(self):
        self._data = {}

    def add(self, srcPkg, dstPkg):
        tgt = self._data.get(srcPkg)
        if not tgt:
            tgt = []
            self._data[srcPkg] = tgt

        if dstPkg not in tgt:
            tgt.append(dstPkg)

    def unique_star_projection(self, source):
        if not source.endswith("*"):
            return None
        src = _get_package(source)
        items = self._data.get(src)
        if items:
            if len(items) == 1:
                return items[0] + "*"
            else:
                raise ProcessingError(f"package projection '{source}' has multiple targets on AndroidX namespace")

        return None

    def __iter__(self):
        yield from self._data.items()

class _ArtifactReplacement:
    def __init__(self, artifactSrc, artifactDst):
        self._src_artifact = artifactSrc.strip()
        idx = artifactDst.rfind(":")
        self._dst_artifact = artifactDst[:idx].strip()
        self._dst_version = artifactDst[(idx + 1):].strip()
        self._src_match_form1 = re.compile(r"(?P<form1>" + re.escape(artifactSrc) + r"):\$\{?(?:\w+\.)*(?P<version>[^\}\"'\s]+)\}?")
        parts = artifactDst.split(":")
        self._targetVersionVar = _ArtifactReplacement._extract_ver_name(parts[1])
        eparts = [re.escape(part) for part in artifactSrc.split(":")]
        self._src_match_form2 = re.compile(r"group\s*:\s*(?P<qg>[\"'])(?P<gname>" + eparts[0] + r")(?P=qg)\s*,\s*name\s*:\s*(?P<qn>[\"'])(?P<modname>" + eparts[1] + r")(?P=qn)\s*,\s*version\s*:\s*(?P<quote>[\"'])?\$?(?P<ob>\{)?(?:\w+\.)*(?P<version>[^\"'}\s]+)(?(ob)\})(?(quote)(?P=quote))")

    def _extract_ver_name(value):
        words = [w.lower() if idx == 0 else w.capitalize() for idx, w in enumerate(re.split(r"\W+", value))] + ["Version"]
        return "".join(words)

    def __str__(self):
        return "From '%s' to '%s' version: %s=%s" % (self._src_artifact, self._dst_artifact, self._targetVersionVar, self._dst_version)

    def matches(self, srcFile, line, lineno):
        matcho = self._src_match_form1.search(line)
        if matcho:
            versionVar = matcho.group("version")
            result = _VersionReplacement(srcFile, lineno, versionVar, self._dst_version)
            praw("Found match V1: %s" % result)
            return result
        
        matcho = self._src_match_form2.search(line)
        if matcho:
            versionVar = matcho.group("version")
            result = _VersionReplacement(srcFile, lineno, versionVar, self._dst_version)
            praw("Found match V2: %s" % result)
            return result

        return None

    def _v1_replace(self, matchobj):
        form1_start, form1_end = matchobj.span("form1")
        ver_start, ver_end = matchobj.span("version")
        absStart = matchobj.start(0)
        form1_start -= absStart
        form1_end -= absStart
        ver_start -= absStart
        ver_end -= absStart
        src = matchobj[0]
        res = src[:form1_start] + self._dst_artifact + src[form1_end:ver_start] + self._targetVersionVar + src[ver_end:]
        praw("Replace(V1) '%s' with '%s'" % (src.strip(), res.strip()))
        return res

    def _v2_replace(self, matchobj):
        gname_start, gname_end = matchobj.span("gname")
        modname_start, modname_end = matchobj.span("modname")
        ver_start, ver_end = matchobj.span("version")

        absStart = matchobj.start(0)
        gname_start -= absStart
        gname_end -= absStart
        modname_start -= absStart
        modname_end -= absStart
        ver_start -= absStart
        ver_end -= absStart
        whole = matchobj[0]

        parts = self._dst_artifact.split(":")

        res = whole[:gname_start] + parts[0] + whole[gname_end:modname_start] + parts[1] + whole[modname_end:ver_start] + self._targetVersionVar + whole[ver_end:]
        praw("Replace(V2) '%s' with '%s'" % (whole.strip(), res.strip()))
        return res

    def _append_latest_version(self, tgt):
        verStr = " // Latest version: %s" % self._dst_version
        if not tgt.endswith('\n'):
            return tgt + verStr
        else:
            return tgt[:-1] + verStr + "\n"

    def do_replace(self, srcLine):
        out, nSubs = self._src_match_form1.subn(self._v1_replace, srcLine)
        if nSubs > 0:
            return self._append_latest_version(out)

        out, nSubs = self._src_match_form2.subn(self._v2_replace, srcLine)
        if nSubs > 0:
            return self._append_latest_version(out)
        return None

class _VersionReplacement:
    def __init__(self, srcFile, lineNo, varName, substitution):
        self._srcFile = srcFile
        self._lineNo = lineNo
        self._varName = varName
        self._substitution = substitution

    def __str__(self):
        return "Version replacement on '%s:%d'. Varname = %s, Substitution: %s" % (self._srcFile._path, self._lineNo, self._varName, self._substitution)

class _PackageReplacement:
    def __init__(self, srcClass, dstClass):
        escaped = re.escape(_get_package(srcClass))
        self._matcher = re.compile(f"{escaped}(\w+)", re.ASCII)
        self._dstPackage = dstClass

    def __str__(self):
        return f"Matcher={self._matcher} ==> Replaces to: {self._dstPackage}"

    def _target_replace(self, matchobj):
        replacement = self._dstPackage + matchobj.group(1)
        praw("Changed '%s' with '%s'" % (matchobj.group(0), replacement))
        return replacement

    def do_replace(self, srcLine):
        return self._matcher.sub(self._target_replace, srcLine)
