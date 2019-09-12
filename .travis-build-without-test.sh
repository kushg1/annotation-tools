#!/bin/bash

echo Entering "$(cd "$(dirname "$0")" && pwd -P)/$(basename "$0")"

# Fail the whole script if any command fails
set -e

export SHELLOPTS

export JAVA_HOME=${JAVA_HOME:-$(dirname $(dirname $(dirname $(readlink -f $(/usr/bin/which java)))))}
echo JAVA_HOME=$JAVA_HOME
export AFU="${AFU:-$(cd annotation-file-utilities && pwd -P)}"
export PATH=$AFU/scripts:$JAVA_HOME/bin:$PATH

## Compile
(cd ${AFU} && ./gradlew assemble)

echo Exiting "$(cd "$(dirname "$0")" && pwd -P)/$(basename "$0")"