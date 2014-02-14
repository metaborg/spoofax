#! /bin/bash

ANT_OPTS="-Xss16m -Xmx1024m -server -XX:+UseParallelGC -XX:MaxPermSize=256m" ant -lib strategoxt.jar $@ install

