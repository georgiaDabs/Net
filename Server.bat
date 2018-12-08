#!/bin/bash
clear
echo "compiling server"
javac -cp ./ ./Server.java
echo "running server"
java Server