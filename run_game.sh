#!/bin/sh

javac MyBot.java
javac MyBotOld.java

find . -name \*.hlt -delete
find . -name \*.log -delete

./halite -d "240 160" "java MyBot" "java MyBotOld"

find . -name \*.class -delete