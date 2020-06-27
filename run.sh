#!/bin/sh
here=`pwd`
scrdir=`dirname $0`

if test -f "logback.xml"; then
    logconf="-Dlogback.configurationFile=file:logback.xml"
fi
java $logconf -jar "$scrdir/target/GraVE.jar" "$@"
