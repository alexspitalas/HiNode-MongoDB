#!/bin/bash

java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster single schemaexists none stHT degdistr all 0 156 5 yes > results/stHTdegDistr100.txt
sleep 30

java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster single schemaexists none stHT degdistr all 0 78 5 yes > results/stHTdegDistr50.txt
sleep 30

java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster single schemaexists none stHT degdistr all 0 39 5 yes > results/stHTdegDistr25.txt
sleep 30

java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster single schemaexists none stHT degdistr all 0 1 5 yes > results/stHTdegDistr1.txt


java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster single schemaexists none stPT degdistr all 0 132 5 yes > results/stPHdegDistr100.txt
sleep 30

java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster single schemaexists none stPT degdistr all 0 66 5 yes > results/stPHdegDistr50.txt
sleep 30

java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster single schemaexists none stPT degdistr all 0 33 5 yes > results/stPHdegDistr25.txt
sleep 30

java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster single schemaexists none stPT degdistr all 0 1 5 yes > results/stPHdegDistr1.txt


java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster single schemaexists none stPatents degdistr all 0 444 5 yes > results/stPatentsdegDistr100.txt
sleep 30

java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster single schemaexists none stPatents degdistr all 0 222 5 yes > results/stPatentsdegDistr50.txt
sleep 30

java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster single schemaexists none stPatents degdistr all 0 111 5 yes > results/stPatentsdegDistr25.txt
sleep 30

java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster single schemaexists none stPatents degdistr all 0 1 5 yes > results/stPatentsdegDistr1.txt





java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster single schemaexists none stHT degdistrall all 0 156 5 yes > results/stHTdegDistrall100.txt
sleep 30
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster single schemaexists none stHT degdistrall all 0 78 5 yes > results/stHTdegDistrall50.txt
sleep 30
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster single schemaexists none stHT degdistrall all 0 39 5 yes > results/stHTdegDistrall25.txt
sleep 30
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster single schemaexists none stHT degdistrall all 0 1 5 yes > results/stHTdegDistrall1.txt
sleep 30

java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster single schemaexists none stPT degdistrall all 0 132 5 yes > results/stPHdegDistrall100.txt
sleep 30
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster single schemaexists none stPT degdistrall all 0 66 5 yes > results/stPHdegDistrall50.txt
sleep 30
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster single schemaexists none stPT degdistrall all 0 33 5 yes > results/stPHdegDistrall25.txt
sleep 30
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster single schemaexists none stPT degdistrall all 0 1 5 yes > results/stPHdegDistrall1.txt
sleep 30

java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster single schemaexists none stPatents degdistrall all 0 444 5 yes > results/stPatentsdegDistrall100.txt
sleep 30
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster single schemaexists none stPatents degdistrall all 0 222 5 yes > results/stPatentsdegDistrall50.txt
sleep 30
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster single schemaexists none stPatents degdistrall all 0 111 5 yes > results/stPatentsdegDistrall25.txt
sleep 30
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster single schemaexists none stPatents degdistrall all 0 1 5 yes > results/stPatentsdegDistrall1.txt
sleep 30



java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster multiple schemaexists none mtHT degdistr all 0 156 5 yes > results/mtHTdegDistr100.txt
sleep 30
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster multiple schemaexists none mtHT degdistr all 0 78 5 yes > results/mtHTdegDistr50.txt
sleep 30
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster multiple schemaexists none mtHT degdistr all 0 39 5 yes > results/mtHTdegDistr25.txt
sleep 30
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster multiple schemaexists none mtHT degdistr all 0 1 5 yes > results/mtHTdegDistr1.txt
sleep 30

java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster multiple schemaexists none mtPT degdistr all 0 132 5 yes > results/mtPHdegDistr100.txt
sleep 30
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster multiple schemaexists none mtPT degdistr all 0 66 5 yes > results/mtPHdegDistr50.txt
sleep 30
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster multiple schemaexists none mtPT degdistr all 0 33 5 yes > results/mtPHdegDistr25.txt
sleep 30
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster multiple schemaexists none mtPT degdistr all 0 1 5 yes > results/mtPHdegDistr1.txt
sleep 30

java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster multiple schemaexists none mtPatents degdistr all 0 444 5 yes > results/mtPatentsdegDistr100.txt
sleep 30
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster multiple schemaexists none mtPatents degdistr all 0 222 5 yes > results/mtPatentsdegDistr50.txt
sleep 30
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster multiple schemaexists none mtPatents degdistr all 0 111 5 yes > results/mtPatentsdegDistr25.txt
sleep 30
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster multiple schemaexists none mtPatents degdistr all 0 1 5 yes > results/mtPatentsdegDistr1.txt
sleep 30




java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster multiple schemaexists none mtHT degdistrall all 0 156 5 yes > results/mtHTdegDistrall100.txt
sleep 30
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster multiple schemaexists none mtHT degdistrall all 0 78 5 yes > results/mtHTdegDistrall50.txt
sleep 30
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster multiple schemaexists none mtHT degdistrall all 0 39 5 yes > results/mtHTdegDistrall25.txt
sleep 30
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster multiple schemaexists none mtHT degdistrall all 0 1 5 yes > results/mtHTdegDistrall1.txt
sleep 30

java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster multiple schemaexists none mtPT degdistrall all 0 132 5 yes > results/mtPHdegDistrall100.txt
sleep 30
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster multiple schemaexists none mtPT degdistrall all 0 66 5 yes > results/mtPHdegDistrall50.txt
sleep 30
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster multiple schemaexists none mtPT degdistrall all 0 33 5 yes > results/mtPHdegDistrall25.txt
sleep 30
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster multiple schemaexists none mtPT degdistrall all 0 1 5 yes > results/mtPHdegDistrall1.txt
sleep 30

java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster multiple schemaexists none mtPatents degdistrall all 0 444 5 yes > results/mtPatentsdegDistrall100.txt
sleep 30
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster multiple schemaexists none mtPatents degdistrall all 0 222 5 yes > results/mtPatentsdegDistrall50.txt
sleep 30
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster multiple schemaexists none mtPatents degdistrall all 0 111 5 yes > results/mtPatentsdegDistrall25.txt
sleep 30
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster multiple schemaexists none mtPatents degdistrall all 0 1 5 yes > results/mtPatentsdegDistrall1.txt
sleep 30







#ONEHOP

java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster single schemaexists none stHT onehop 19187 0 156 5 yes > results/stHTonehop100.txt
sleep 30
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster single schemaexists none stHT onehop 19187 0 78 5 yes > results/stHTonehop50.txt
sleep 30
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster single schemaexists none stHT onehop 19187 0 39 5 yes > results/stHTonehop25.txt
sleep 30
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster single schemaexists none stHT onehop 19187 0 1 5 yes > results/stHTonehop1.txt
sleep 30

java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster single schemaexists none stPT onehop 18904 0 132 5 yes > results/stPTHonehop100.txt
sleep 30
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster single schemaexists none stPT onehop 18904 0 66 5 yes > results/stPTHonehop50.txt
sleep 30
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster single schemaexists none stPT onehop 18904 0 33 5 yes > results/stPTHonehop25.txt
sleep 30
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster single schemaexists none stPT onehop 18904 0 1 5 yes > results/stPTHonehop1.txt
sleep 30

java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster single schemaexists none stPatents onehop 2365843 0 444 5 yes > results/stPatentsonehop100.txt
sleep 30
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster single schemaexists none stPatents onehop 2365843 0 222 5 yes > results/stPatentsonehop50.txt
sleep 30
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster single schemaexists none stPatents onehop 2365843 0 111 5 yes > results/stPatentsonehop25.txt
sleep 30
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster single schemaexists none stPatents onehop 2365843 0 1 5 yes > results/stPatentsonehop1.txt
sleep 30

# MT Model with onehop function
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster multiple schemaexists none mtHT onehop 19187 0 156 5 yes > results/mtHTonehop100.txt
sleep 30
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster multiple schemaexists none mtHT onehop 19187 0 78 5 yes > results/mtHTonehop50.txt
sleep 30
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster multiple schemaexists none mtHT onehop 19187 0 39 5 yes > results/mtHTonehop25.txt
sleep 30
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster multiple schemaexists none mtHT onehop 19187 0 1 5 yes > results/mtHTonehop1.txt
sleep 30

java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster multiple schemaexists none mtPT onehop 18904 0 132 5 yes > results/mtPTHonehop100.txt
sleep 30
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster multiple schemaexists none mtPT onehop 18904 0 66 5 yes > results/mtPTHonehop50.txt
sleep 30
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster multiple schemaexists none mtPT onehop 18904 0 33 5 yes > results/mtPTHonehop25.txt
sleep 30
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster multiple schemaexists none mtPT onehop 18904 0 1 5 yes > results/mtPTHonehop1.txt
sleep 30

java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster multiple schemaexists none mtPatents onehop 2365843 0 444 5 yes > results/mtPatentsonehop100.txt
sleep 30
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster multiple schemaexists none mtPatents onehop 2365843 0 222 5 yes > results/mtPatentsonehop50.txt
sleep 30
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster multiple schemaexists none mtPatents onehop 2365843 0 111 5 yes > results/mtPatentsonehop25.txt
sleep 30
java -Xmx3g -jar mongo-mvn/target/mongo-mvn-1.0-SNAPSHOT-jar-with-dependencies.jar cluster multiple schemaexists none mtPatents onehop 2365843 0 1 5 yes > results/mtPatentsonehop1.txt
sleep 30




