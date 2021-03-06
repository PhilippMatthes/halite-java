#!/bin/sh

javac MyBot.java
javac StarterBot.java

find . -name \*.hlt -delete
find . -name \*.log -delete

./halite -d "240 160" "java MyBot" "java StarterBot"

find . -name \*.class -delete