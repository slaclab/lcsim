#! /usr/bin/env jython

###
# mainLoop.py
# Wrapper to enable running outside of JAS3
# 03-AUG-2005 Jan Strube
###
from java.io import File
from org.lcsim.util.aida import AIDA
from org.lcsim.util.loop import LCSimLoop
## importing the Java analysis module
import Analysis101
## if Analysis102 cannot be found, please uncomment and modify 
## the following two lines to tell Jython where to find it
# import sys
# sys.path.append('full path to Python module')
# importing the Analysis102 class in the Jython module Analysis102
from Analysis102 import Analysis102

loop = LCSimLoop()
input = File("psiMuMu.slcio")
loop.setLCIORecordSource(input)
loop.add(Analysis101())
loop.add(Analysis102())
# loop over all events with -1 or over any other positive number
loop.loop(-1)
loop.dispose()
    
