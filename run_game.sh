#!/bin/sh

javac MyBot.java
javac ReferenceBot.java

find . -name \*.hlt -delete
find . -name \*.log -delete

./halite -d "240 160" "java MyBot" "java ReferenceBot"

find . -name \*.class -delete