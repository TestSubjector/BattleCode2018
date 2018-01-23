#!/bin/sh
# build the java files.
# there will eventually be a separate build step, but for now the build counts against your time.

# Compile our code.
echo javac $(find . -name '*.java') -classpath ../battlecode/java
javac $(find . -name '*.java') -classpath ../battlecode/java

# Run our code.
echo java -Xmx40m -classpath .:../battlecode/java Player
java -Xmx40m -classpath .:../battlecode/java Player