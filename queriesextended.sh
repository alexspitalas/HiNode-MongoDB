#!/bin/bash




# ST Model with degdistr function for stSF3extended
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster single schemaexists none stSF3extended degdistr all 2010 2012 5 yes > results/stSF3extendeddegDistr100.txt
sleep 30
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster single schemaexists none stSF3extended degdistr all 2010 2011 5 yes > results/stSF3extendeddegDistr66.txt
sleep 30
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster single schemaexists none stSF3extended degdistr all 2010 2010 5 yes > results/stSF3extendeddegDistr33.txt
sleep 30

# MT Model with degdistr function for mtSF3extended
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster multiple schemaexists none mtSF3extended degdistr all 2010 2012 5 yes > results/mtSF3extendeddegDistr100.txt
sleep 30
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster multiple schemaexists none mtSF3extended degdistr all 2010 2011 5 yes > results/mtSF3extendeddegDistr66.txt
sleep 30
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster multiple schemaexists none mtSF3extended degdistr all 2010 2010 5 yes > results/mtSF3extendeddegDistr33.txt
sleep 30



# ST Model with degdistrall function for stSF3extended
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster single schemaexists none stSF3extended degdistrall all 2010 2012 5 yes > results/stSF3extendeddegDistrall100.txt
sleep 30
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster single schemaexists none stSF3extended degdistrall all 2010 2011 5 yes > results/stSF3extendeddegDistrall66.txt
sleep 30
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster single schemaexists none stSF3extended degdistrall all 2010 2010 5 yes > results/stSF3extendeddegDistrall33.txt
sleep 30

# MT Model with degdistrall function for mtSF3extended
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster multiple schemaexists none mtSF3extended degdistrall all 2010 2012 5 yes > results/mtSF3extendeddegDistrall100.txt
sleep 30
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster multiple schemaexists none mtSF3extended degdistrall all 2010 2011 5 yes > results/mtSF3extendeddegDistrall66.txt
sleep 30
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster multiple schemaexists none mtSF3extended degdistrall all 2010 2010 5 yes > results/mtSF3extendeddegDistrall33.txt
sleep 30


# ST Model with onehop function for stSF3extended
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster single schemaexists none stSF3extended onehop 3848292849049 2010 2012 5 yes > results/stSF3extendedonehop100.txt
sleep 30
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster single schemaexists none stSF3extended onehop 3848292849049 2010 2011 5 yes > results/stSF3extendedonehop66.txt
sleep 30
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster single schemaexists none stSF3extended onehop 3848292849049 2010 2010 5 yes > results/stSF3extendedonehop33.txt
sleep 30

# MT Model with onehop function for mtSF3extended
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster multiple schemaexists none mtSF3extended onehop 3848292849049 2010 2012 5 yes > results/mtSF3extendedonehop100.txt
sleep 30
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster multiple schemaexists none mtSF3extended onehop 3848292849049 2010 2011 5 yes > results/mtSF3extendedonehop66.txt
sleep 30
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster multiple schemaexists none mtSF3extended onehop 3848292849049 2010 2010 5 yes > results/mtSF3extendedonehop33.txt
sleep 30




