#!/bin/bash




java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster single schemaexists none stSF3 degdistr all 2010 2012 5 yes > results/stSF3degDistr100.txt
sleep 30

java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster single schemaexists none stSF3 degdistr all 2010 2011 5 yes > results/stSF3degDistr66.txt
sleep 30

java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster single schemaexists none stSF3 degdistr all 2010 2010 5 yes > results/stSF3degDistr33.txt
sleep 30


java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster single schemaexists none stSF10 degdistr all 2010 2012 5 yes > results/stSF10degDistr100.txt
sleep 30

java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster single schemaexists none stSF10 degdistr all 2010 2011 5 yes > results/stSF10degDistr66.txt
sleep 30

java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster single schemaexists none stSF10 degdistr all 2010 2010 5 yes > results/stSF10degDistr33.txt
sleep 30


java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster multiple schemaexists none mtSF3 degdistr all 2010 2012 5 yes > results/mtSF3degDistr100.txt
sleep 30

java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster multiple schemaexists none mtSF3 degdistr all 2010 2011 5 yes > results/mtSF3degDistr66.txt
sleep 30

java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster multiple schemaexists none mtSF3 degdistr all 2010 2010 5 yes > results/mtSF3degDistr33.txt
sleep 30


java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster multiple schemaexists none mtSF10 degdistr all 2010 2012 5 yes > results/mtSF10degDistr100.txt
sleep 30

java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster multiple schemaexists none mtSF10 degdistr all 2010 2011 5 yes > results/mtSF10degDistr66.txt
sleep 30

java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster multiple schemaexists none mtSF10 degdistr all 2010 2010 5 yes > results/mtSF10degDistr33.txt
sleep 30






# ST Model with degdistrall function for sf3
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster single schemaexists none stSF3 degdistrall all 2010 2012 5 yes > results/stSF3degDistrall100.txt
sleep 30
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster single schemaexists none stSF3 degdistrall all 2010 2011 5 yes > results/stSF3degDistrall66.txt
sleep 30
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster single schemaexists none stSF3 degdistrall all 2010 2010 5 yes > results/stSF3degDistrall33.txt
sleep 30

# ST Model with degdistrall function for sf10
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster single schemaexists none stSF10 degdistrall all 2010 2012 5 yes > results/stSF10degDistrall100.txt
sleep 30
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster single schemaexists none stSF10 degdistrall all 2010 2011 5 yes > results/stSF10degDistrall66.txt
sleep 30
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster single schemaexists none stSF10 degdistrall all 2010 2010 5 yes > results/stSF10degDistrall33.txt
sleep 30

# MT Model with degdistrall function for sf3
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster multiple schemaexists none mtSF3 degdistrall all 2010 2012 5 yes > results/mtSF3degDistrall100.txt
sleep 30
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster multiple schemaexists none mtSF3 degdistrall all 2010 2011 5 yes > results/mtSF3degDistrall66.txt
sleep 30
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster multiple schemaexists none mtSF3 degdistrall all 2010 2010 5 yes > results/mtSF3degDistrall33.txt
sleep 30

# MT Model with degdistrall function for sf10
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster multiple schemaexists none mtSF10 degdistrall all 2010 2012 5 yes > results/mtSF10degDistrall100.txt
sleep 30
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster multiple schemaexists none mtSF10 degdistrall all 2010 2011 5 yes > results/mtSF10degDistrall66.txt
sleep 30
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster multiple schemaexists none mtSF10 degdistrall all 2010 2010 5 yes > results/mtSF10degDistrall33.txt



# ST Model with onehop function for sf3
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster single schemaexists none stSF3 onehop 30786325602804 2010 2012 5 yes > results/stSF3onehop100.txt
sleep 30
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster single schemaexists none stSF3 onehop 30786325602804 2010 2011 5 yes > results/stSF3onehop66.txt
sleep 30
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster single schemaexists none stSF3 onehop 30786325602804 2010 2010 5 yes > results/stSF3onehop33.txt
sleep 30

# ST Model with onehop function for sf10
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster single schemaexists none stSF10 onehop 30786325611063 2010 2012 5 yes > results/stSF10onehop100.txt
sleep 30
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster single schemaexists none stSF10 onehop 30786325611063 2010 2011 5 yes > results/stSF10onehop66.txt
sleep 30
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster single schemaexists none stSF10 onehop 30786325611063 2010 2010 5 yes > results/stSF10onehop33.txt
sleep 30

# MT Model with onehop function for sf3
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster multiple schemaexists none mtSF3 onehop 30786325602804 2010 2012 5 yes > results/mtSF3onehop100.txt
sleep 30
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster multiple schemaexists none mtSF3 onehop 30786325602804 2010 2011 5 yes > results/mtSF3onehop66.txt
sleep 30
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster multiple schemaexists none mtSF3 onehop 30786325602804 2010 2010 5 yes > results/mtSF3onehop33.txt
sleep 30

# MT Model with onehop function for sf10
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster multiple schemaexists none mtSF10 onehop 30786325611063 2010 2012 5 yes > results/mtSF10onehop100.txt
sleep 30
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster multiple schemaexists none mtSF10 onehop 30786325611063 2010 2011 5 yes > results/mtSF10onehop66.txt
sleep 30
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster multiple schemaexists none mtSF10 onehop 30786325611063 2010 2010 5 yes > results/mtSF10onehop33.txt
sleep 30

