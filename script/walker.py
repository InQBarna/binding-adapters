import os
import os.path
import re
from logger import *
import io

class SourceTree:
    __path_name_regex = re.compile(r".*\.(?:java|kt)")
    def __init__(self, root, excludes):
        self._root = root
        self._excludes = excludes

    def __iter__(self):
        return self.scan()

    def _should_discard(self, tgt):
        for ex_expression in self._excludes:
            if ex_expression.search(tgt):
                pinfo("Discarding source dir: %s" % tgt)
                return True
        return False

    def scan(self):
        for root, dirs, files in os.walk(self._root):
            source_candidates = [f for f in files if SourceTree.__path_name_regex.match(f) != None]
            dirs[:] = [d for d in dirs if not self._should_discard(os.path.join(root, d))]

            for s in source_candidates:
                yield SourceFile(os.path.join(root, s))

class SourceFile:
    def __init__(self, path):
        self._path = path

    def __str__(self):
        return "Source file at '%s'" % self._path

    def processor(self):
        with io.BytesIO() as _byteBuffer:
            with io.FileIO(self._path, mode="r") as _file:
                readBytes = _byteBuffer.write(_file.read())
            
            _byteBuffer.seek(0, io.SEEK_SET)
            with io.BufferedReader(_byteBuffer) as _buffer:
                with io.TextIOWrapper(io.BufferedWriter(io.FileIO(self._path, mode="w"))) as _out:
                    with io.TextIOWrapper(_buffer, encoding="utf-8", errors='strict') as _src:
                        for line in _src:
                            replacement = yield line
                            if replacement:
                                _out.write(replacement)
