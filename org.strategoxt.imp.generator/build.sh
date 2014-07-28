#! /bin/bash

ANT_OPTS="-Xss32m -Xmx1024m -server -XX:+UseParallelGC -XX:MaxPermSize=256m" ant -lib strategoxt-distrib/share/strategoxt/build-lib -lib strategoxt-distrib/share/strategoxt/strategoxt/strategoxt.jar $@

