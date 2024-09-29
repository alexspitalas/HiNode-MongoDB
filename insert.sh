#!/bin/bash

java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster single createschema ../../created/merged_and_sorted_eventsSF3.txt stSF3 none > results/stSF3.txt
sleep 30
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster single createschema ../../created/merged_and_sorted_eventsSF10.txt stSF10 none > results/stSF10.txt
sleep 30

java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster multiple createschema ../../created/merged_and_sorted_eventsSF3.txt mtSF3 none > results/mtSF3.txt
sleep 30
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster multiple createschema ../../created/merged_and_sorted_eventsSF10.txt mtSF10 none > results/mtSF10.txt
