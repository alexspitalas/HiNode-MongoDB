#!/bin/bash

java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster multiple createschema ../../created/merged_and_sorted_eventsSF3_extended.txt mtSF3extended none > results/mtSF3extended.txt
sleep 30

java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster single createschema ../../created/merged_and_sorted_eventsSF3_extended.txt stSF3extended none > results/stSF3extended.txt



