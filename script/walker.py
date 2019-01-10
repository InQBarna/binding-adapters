import os
import os.path
import re
from logger import *
import io
import itertools

class SourceTree:
    __srcfile_path_name_regex = re.compile(r".*\.(?:java|kt)\b$")
    __buildfile_path_name_regex = re.compile(r".*\.gradle\b$")
    __configfile_path_name_regex = re.compile(r"gradle\.properties")
    def __init__(self, root, excludes):
        self._root = root
        self._excludes = excludes

    def _should_discard(self, tgt):
        for ex_expression in self._excludes:
            if ex_expression.search(tgt):
                pinfo("Discarding source dir: %s" % tgt)
                return True
        return False

    def files(self, *types):
        res = None
        regexes = []
        if SourceFile.TYPE_SRC in types:
            pdbg("Adding files iterator")
            regexes.append((SourceTree.__srcfile_path_name_regex, SourceFile.TYPE_SRC))

        if SourceFile.TYPE_BUILDFILE in types:
            pdbg("Adding buildfiles iterator")
            regexes.append((SourceTree.__buildfile_path_name_regex, SourceFile.TYPE_BUILDFILE))

        if SourceFile.TYPE_CONFIG in types:
            pdbg("Adding configs iterator")
            regexes.append((SourceTree.__configfile_path_name_regex, SourceFile.TYPE_CONFIG))

        praw("Will return: " + repr(res))
        return self._iterate_over_sources(regexes) if len(regexes) > 0 else []

    def sourcefiles(self):
        return self._iterate_over_sources((SourceTree.__srcfile_path_name_regex, SourceFile.TYPE_SRC))

    def _iterate_over_sources(self, regex_type_combo):
        if not isinstance(regex_type_combo, list):
            rtc = [regex_type_combo]
        else:
            rtc = regex_type_combo

        regexes = [r[0] for r in regex_type_combo]
        for root, dirs, files in os.walk(self._root):
            source_candidates = [(f,t[1]) for t in rtc for f in files if t[0].match(f)]
            dirs[:] = [d for d in dirs if not self._should_discard(os.path.join(root, d))]

            for s in source_candidates:
                yield SourceFile(os.path.join(root, s[0]), s[1])

    def buildfiles(self):
        return self._iterate_over_sources((SourceTree.__buildfile_path_name_regex, SourceFile.TYPE_BUILDFILE))

class SourceFile:
    TYPE_SRC="source"
    TYPE_BUILDFILE="buildfile"
    TYPE_CONFIG="config"

    def __init__(self, path, fileType):
        self._path = path
        self.fileType = fileType

    def __str__(self):
        return "{FilePath:'%s' type = %s}" % (self._path, self.fileType)

    def _open_write_path(self, dryRun):
        if dryRun:
            return _CustomDBGIO()
        else:
            return io.TextIOWrapper(io.BufferedWriter(io.FileIO(self._path, mode="w")))

    def lines(self):
        praw("Getting lines of: %s" % self._path)
        with open(self._path, "r") as f:
            yield from f.readlines()

    def processor(self, dryRun = False):
        with io.BytesIO() as _byteBuffer:
            with io.FileIO(self._path, mode="r") as _file:
                readBytes = _byteBuffer.write(_file.read())
            
            _byteBuffer.seek(0, io.SEEK_SET)
            with io.BufferedReader(_byteBuffer) as _buffer:
                with self._open_write_path(dryRun) as _out:
                    with io.TextIOWrapper(_buffer, encoding="utf-8", errors='strict') as _src:
                        for line in _src:
                            replacement = yield line
                            if replacement:
                                _out.write(replacement)
                        if self.fileType == SourceFile.TYPE_CONFIG:
                            _out.write("\nandroid.useAndroidX=true\n")
                            _out.write("android.enableJetifier=true\n")


class _CustomDBGIO(io.TextIOBase):
    def __init__(self):
        pass

    def write(self, txt):
        print(txt.strip())
        return len(txt)
