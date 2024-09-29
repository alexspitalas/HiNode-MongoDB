#!/bin/bash

java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster single createschema ../../created/HepThDataset.txt stHT none > results/stHT.txt
sleep 30
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster single createschema ../../created/HepPhDataset.txt stPT none > results/stPT.txt
sleep 30
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster single createschema ../../created/PatentsDataset.txt stPatents none > results/stPatents.txt
sleep 30
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster multiple createschema ../../created/HepThDataset.txt mtHT none > results/mtHT.txt
sleep 30
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster multiple createschema ../../created/HepPhDataset.txt mtPT none > results/mtPT.txt
sleep 30
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster multiple createschema ../../created/PatentsDataset.txt mtPatents none > results/mtPatents.txt
