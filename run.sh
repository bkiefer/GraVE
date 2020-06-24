#!/bin/sh
here=`pwd`
scriptdir=`dirname $0`
java -jar "$scriptdir"/target/SCMConvert.jar "$@"
