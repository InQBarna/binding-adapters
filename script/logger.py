

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
