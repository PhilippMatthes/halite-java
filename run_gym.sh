#!/bin/sh

javac MyBot.java
javac StarterBot.java

find . -name \*.hlt -delete
find . -name \*.log -delete

hlt_client/hlt_client/client.py gym -r "java /Users/philippmatthes/desktop/halite-java/MyBot" -r "java /Users/philippmatthes/desktop/halite-java/StarterBot" -b "/Users/philippmatthes/desktop/halite-java/halite" -i 100 -H 160 -W 240

find . -name \*.log -delete
find . -name \*.class -delete