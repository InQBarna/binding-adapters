
VERBOSITY=0
_V_WARN=1
_V_INFO=2
_V_DBG=3
_V_RAW=4

__all__ = ["pinfo", "pdbg", "praw", "pwarn", "VERBOSITY"]

def pinfo(msg):
    __print_lvl(msg, _V_INFO)

def pdbg(msg):
    __print_lvl(msg, _V_DBG)

def praw(msg):
    __print_lvl(msg, _V_RAW)

def pwarn(msg):
    __print_lvl(msg, _V_WARN)

def __print_lvl(msg, targetLevel):
    if (VERBOSITY >= targetLevel):
        print(msg)
