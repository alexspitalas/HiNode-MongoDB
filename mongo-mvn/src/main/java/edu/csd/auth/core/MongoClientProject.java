package edu.csd.auth.core;
//MongoDB implementaion of HiNode model
//working models are SingleTableModel and MultipleTableModel
import com.mongodb.MongoClientSettings;
import com.mongodb.ReadConcern;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.connection.ClusterConnectionMode;
import com.mongodb.connection.ServerDescription;
import edu.csd.auth.models.DataModel;
import edu.csd.auth.models.MultipleTablesModel;
import edu.csd.auth.models.SingleTableModel;

import java.io.*;
import java.util.*;
import edu.csd.auth.utils.DiaNode;
import java.util.logging.Level;
import java.util.logging.Logger;
import edu.csd.auth.utils.SnapshotResult;
public class MongoClientProject
{
    private static MongoClient mongoClient = null;
    private static MongoDatabase database = null;

    public static void main(String[] args)
    {
        // java -Xmx3g -jar mongo-mvn.jar [local/cluster] [single/multiple/base] [createschema/schemaexists] [input/none] [keyspacename] [onehop/avgdeg/avgdegall/degdistr/none] (VID/all, FirstGID, LastGID) #nIterations [yes/no]

        String place = args[0]; // Place of execution (changes the "host" IP address). Available values: "local" or "cluster"
        String flag = args[1]; // Available values: "single" model or "multiple" model
        String createSchema = args[2]; // Should the schema be created first? Available values: "createschema" or "schemaexists"
        String input = args[3]; // The input file.(can also be "none")
        String keyspace = args[4]; // The keyspace name. Usually it's "histgraph" for ST and "histgraphMT" for MT
        String specialOp = args[5]; // Specifies a special operation not included in the basic ones. Available values: "batch", "onehop" or "none"
        String inputVID = null;
        String inputFirstGID = null;
        String inputLastGID = null;
        String nIterations = null;
        String debug = null;
        if (!specialOp.equals("none")) // A special operation has been specified
        {
            inputVID = args[6]; // The vIDs it will be applied on (can also be "all")
            inputFirstGID = args[7]; // The first snapshot it will be applied on
            inputLastGID = args[8]; // The last snapshot it will be applied on - Total snapshot range [inputFirstGID, inputLastGID]
            nIterations = args[9]; // Number of iterations the experiment will be executed
            debug = args[10]; //if it will show the results of the operation or not
        }

        init(place);

        @SuppressWarnings("UnusedAssignment")
        DataModel model = null;

        switch (flag)
        {
            case "multiple":
                model = new MultipleTablesModel(mongoClient, keyspace);
                break;
            case "single":
                model = new SingleTableModel(mongoClient, keyspace);
                break;
            case "base":
                //model = new BaselineModel(mongoClient, keyspace);
                break;
            default:
                System.out.println("Please specify a valid model (single/multiple/base). Exiting...");
                System.exit(1);
        }

        if (createSchema.equals("createschema"))
        {
            long tStart = System.nanoTime();
            model.createSchema();
            long tEnd = System.nanoTime();
            long tDelta = tEnd - tStart;
            double elapsedSeconds = tDelta / 1000000000.0;
            System.out.println("Time required to create the model: " + elapsedSeconds);
        }
        else
        {
            model.useKeyspace();
        }

        if (!input.equals("none"))
        {
            long tStart = System.nanoTime();
            model.parseInput(input);
            long tEnd = System.nanoTime();
            long tDelta = tEnd - tStart;
            double elapsedSeconds = tDelta / 1000000000.0;
            System.out.println("Time required to input all data for file \"" + input + "\": " + (elapsedSeconds / 60.0) + " minutes.");
        }

        if (!specialOp.equals("none"))
        {
            long tStart, tEnd, tDelta;
            tDelta = 0;
            int iterations = Integer.parseInt(nIterations) + 1;
            for (int i=0; i<iterations; i++)
            {
                tStart = System.nanoTime();
                evaluateSpecialOperation(model, specialOp, inputVID, inputFirstGID, inputLastGID, debug);
                tEnd = System.nanoTime();
                if (i != 0) // Ignoring first iteration (to ignore warm-up costs etc.)
                    tDelta = tDelta + (tEnd - tStart);
                else
                    System.out.println("***FIRST ITERATION. IGNORING TIME.***");
            }
            double elapsedSeconds = (tDelta / (iterations-1)) / 1000000000.0;
            System.out.println("Average time required for performing the '" + specialOp + "' special operation in the '" + flag + "' model (over " + (iterations-1) + " iterations): " + elapsedSeconds);
        }

        mongoClient.close();
    }

    public static void init(String place)
    {
        // Check if the cluster has already been initialized
        if (mongoClient != null)
            return;

        @SuppressWarnings("UnusedAssignment")
        String host = "none";
        try
        {
            switch (place)
            {
                case "local":
                    host = "127.0.0.1";
                    break;
                case "cluster":
                    host = "";
                    break;
                default:
                    System.out.println("Please specify a valid execution setting (local/cluster). Exiting...");
                    System.exit(1);
            }

            //final String[] hosts = host.split(",");
            Integer port = 27020;
            /*
            PoolingOptions opts = new PoolingOptions();
            opts.setMaxRequestsPerConnection(HostDistance.LOCAL, 32768).setMaxRequestsPerConnection(HostDistance.REMOTE, 2000);


            QueryOptions qopts = new QueryOptions();
            qopts.setConsistencyLevel(ConsistencyLevel.ONE);
            cluster = Cluster.builder().withPort(Integer.valueOf(port)).withPoolingOptions(opts).addContactPoints(hosts).withLoadBalancingPolicy(new RoundRobinPolicy()).withCredentials("cassandra", "cassandra").withQueryOptions(qopts).build();

            // Update number of connections based on threads
            int threadcount = 1000;
            cluster.getConfiguration().getPoolingOptions().setConnectionsPerHost(HostDistance.LOCAL, threadcount, threadcount).setMaxQueueSize(1024);

            // Set connection timeout 10min (default is 5s)
            cluster.getConfiguration().getSocketOptions().setConnectTimeoutMillis(10 * 60 * 1000);
            // Set read (execute) timeout 10min (default is 12s)
            cluster.getConfiguration().getSocketOptions().setReadTimeoutMillis(10 * 60 * 1000);
            */
            //mongoClient = MongoClients.create("mongodb://localhost:27017");
            ArrayList<ServerAddress> hosts = new ArrayList();
            for (String address : host.split(",")){
                hosts.add(new ServerAddress(address, port));
            }

            if (place == "local"){
                mongoClient = MongoClients.create();
            }else{
                mongoClient = MongoClients.create(
                    MongoClientSettings.builder()
                            .applyToClusterSettings(builder ->
                                    builder.hosts(hosts)
                                    .mode(ClusterConnectionMode.MULTIPLE)
                                    )
                            .writeConcern(WriteConcern.MAJORITY)
                            .readConcern(ReadConcern.MAJORITY)
                            .build());
            }


            //database = mongoClient.getDatabase("test");
            // Update number of connections based on threads
            //int threadcount = 1000;

            //Metadata metadata = mongoClient.getMetadata();

            for (ServerDescription discoveredHost : mongoClient.getClusterDescription().getServerDescriptions())
                System.out.printf("Datacenter: %s; Host: %s; Rack: %s\n", discoveredHost.getHosts(), discoveredHost.getAddress(), discoveredHost.getArbiters());

        }
        catch (NumberFormatException e)
        {
            e.printStackTrace();
        }
    }

    public static void transformDataset(String input) // Can also be used for datasets that don't have increasing numbers as vertex ID (e.g. "vertex sgffg name=temp color=blue")
    {
        try
        {
            BufferedReader file = new BufferedReader(new FileReader(input));
            BufferedWriter transformedFile = new BufferedWriter(new FileWriter("transformed_" + input));
            String tokens[];
            String line;
            int vID = 0;
            HashMap<String, Integer> vertexMap = new HashMap<String, Integer>();

            while ((line = file.readLine()) != null)
            {
                if (line.startsWith("mkdir") || line.startsWith("cd") || line.startsWith("time") || line.startsWith("string") ||
                        line.startsWith("double") || line.startsWith("shutdown") || line.startsWith("graph") || line.startsWith("use"))
                {
                    transformedFile.write(line);
                    transformedFile.newLine();
                }
                else if (line.startsWith("vertex"))
                {
                    tokens = line.split(" ");
                    vertexMap.put(tokens[1], vID);
                    transformedFile.write("vertex " + vID);
                    transformedFile.newLine();
                    vID++;
                }
                else if (line.startsWith("edge"))
                {
                    tokens = line.split(" ");
                    transformedFile.write("edge " + vertexMap.get(tokens[1]) + " " + vertexMap.get(tokens[2]));
                    transformedFile.newLine();
                }
            }
            file.close();
            transformedFile.close();
        }
        catch (FileNotFoundException ex)
        {
            Logger.getLogger(MongoClientProject.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (IOException ex)
        {
            Logger.getLogger(MongoClientProject.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void evaluateSpecialOperation(DataModel model, String specialOp, String inputVID, String inputFirstGID, String inputLastGID, String debug)
    {
        switch (specialOp)
        {
            case "onehop":
            {
                long tStart, tEnd, tDelta;
                tDelta = 0;
                tStart = System.nanoTime();
                List<String> result = null;
                result =  model.getOneHopNeighborhood(inputVID, inputFirstGID, inputLastGID);
                tEnd = System.nanoTime();
                tDelta = tDelta + (tEnd - tStart);
                Collections.sort(result);
                double elapsedSeconds = (tDelta / 1000000000.0);
                System.out.println("Average time required for One Hop Neighborhood (over vertex '" + inputVID + "'): " + elapsedSeconds + " seconds, (Query Range: [" + inputFirstGID + ", " + inputLastGID + "])");
                if(debug.equals("yes")){
                    System.out.println("Average time required for One Hop Neighborhood (over vertex '" + inputVID + "'): " + elapsedSeconds + " seconds, (Query Range: [" + inputFirstGID + ", " + inputLastGID + "])" +"result:"+ result);
                }else{
                    System.out.println("Average time required for One Hop Neighborhood (over vertex '" + inputVID + "'): " + elapsedSeconds + " seconds, (Query Range: [" + inputFirstGID + ", " + inputLastGID + "])");
                }
                break;
            }
            case "verhist":
            {
                long tStart, tEnd, tDelta;
                tStart = System.nanoTime();
                //model.getVertexHistory(inputVID, inputFirstGID, inputLastGID);
                DiaNode result = null;
                if(debug.equals("yes")){
                    result = model.getVertexHistory(inputVID, inputFirstGID, inputLastGID);
                }else{
                    model.getVertexHistory(inputVID, inputFirstGID, inputLastGID);
                }
                tEnd = System.nanoTime();
                tDelta = tEnd - tStart;
                double elapsedSeconds = tDelta / 1000000000.0;
                System.out.println("Average time required for fetching the history of vertex '" + inputVID + "': " + elapsedSeconds + " seconds, (Query Range: [" + inputFirstGID + ", " + inputLastGID + "])");
                if(debug.equals("yes")){
                    System.out.println("Average time required for fetching the history of vertex '" + inputVID + "': " + elapsedSeconds + " seconds, (Query Range: [" + inputFirstGID + ", " + inputLastGID + "])" +"result:"+ result);
                }else{
                    System.out.println("Average time required for fetching the history of vertex '" + inputVID + "': " + elapsedSeconds + " seconds, (Query Range: [" + inputFirstGID + ", " + inputLastGID + "])");
                }
                break;
            }
            case "avgdeg":
            {
                long tStart, tEnd, tDelta;
                String first = inputFirstGID;
                String last = inputLastGID;
                tStart = System.nanoTime();
                //model.getAvgVertexDegree(first, last);
                ArrayList<SnapshotResult> results = null;
                if(debug.equals("yes")){
                    results = (ArrayList<SnapshotResult>) model.getAvgVertexDegree(first, last);
                }else{
                    model.getAvgVertexDegree(first, last);
                }
                tEnd = System.nanoTime();
                tDelta = tEnd - tStart;
                double elapsedSeconds = tDelta / 1000000000.0;
                //System.out.println("Time elapsed for the whole AvgDeg query: " + elapsedSeconds + " seconds, (Query Range: [" + inputFirstGID + ", " + inputLastGID + "])");
                if(debug.equals("yes")){
                    System.out.println("Time elapsed for the whole AvgDeg query: " + elapsedSeconds + " seconds, (Query Range: [" + inputFirstGID + ", " + inputLastGID + "])"+ " result:"+results);
                }else{
                    System.out.println("Time elapsed for the whole AvgDeg query: " + elapsedSeconds + " seconds, (Query Range: [" + inputFirstGID + ", " + inputLastGID + "])");
                }
                break;
            }
            case "avgdegall":
            {
                long tStart, tEnd, tDelta;
                String first = inputFirstGID;
                String last = inputLastGID;
                tStart = System.nanoTime();
                ArrayList<SnapshotResult> results = null;
                if(debug.equals("yes")){
                    results = (ArrayList<SnapshotResult>) model.getAvgVertexDegreeFetchAllVertices(first, last);
                }else{
                    model.getAvgVertexDegreeFetchAllVertices(first, last);
                }
                tEnd = System.nanoTime();
                tDelta = tEnd - tStart;
                double elapsedSeconds = tDelta / 1000000000.0;
                if(debug.equals("yes")){
                    System.out.println("Time elapsed for the whole AvgDegAll query: " + elapsedSeconds + " seconds, (Query Range: [" + inputFirstGID + ", " + inputLastGID + "])"+ " result:"+results);
                }else{
                    System.out.println("Time elapsed for the whole AvgDegAll query: " + elapsedSeconds + " seconds, (Query Range: [" + inputFirstGID + ", " + inputLastGID + "])");
                }
                break;
            }
            case "degdistr":
            {
                long tStart, tEnd, tDelta;
                String first = inputFirstGID;
                String last = inputLastGID;
                tStart = System.nanoTime();
                HashMap<String, HashMap<String, Integer>> result = null;
                if(debug.equals("yes")){
                    result = model.getDegreeDistribution(first, last);
                }else{
                    model.getDegreeDistribution(first, last);
                }
                tEnd = System.nanoTime();
                tDelta = tEnd - tStart;
                double elapsedSeconds = tDelta / 1000000000.0;
                if(debug.equals("yes")){
                    System.out.println("Time elapsed for the DegDistr query: " + elapsedSeconds + " seconds, (Query Range: [" + inputFirstGID + ", " + inputLastGID + "])"+ " result:"+result);
                }else{
                    System.out.println("Time elapsed for the DegDistr query: " + elapsedSeconds + " seconds, (Query Range: [" + inputFirstGID + ", " + inputLastGID + "])");
                }
                break;
            }
            case "degdistrall":
            {
                long tStart, tEnd, tDelta;
                String first = inputFirstGID;
                String last = inputLastGID;
                tStart = System.nanoTime();
                HashMap<String, HashMap<String, Integer>>result = null;
                if(debug.equals("yes")){
                    result = model.getDegreeDistributionFetchAllVertices(first, last);
                }else{
                    model.getDegreeDistributionFetchAllVertices(first, last);
                }
                tEnd = System.nanoTime();
                tDelta = tEnd - tStart;
                double elapsedSeconds = tDelta / 1000000000.0;
                if(debug.equals("yes")){
                    System.out.println("Time elapsed for the DegDistrAll query: " + elapsedSeconds + " seconds, (Query Range: [" + inputFirstGID + ", " + inputLastGID + "])"+ " result:"+result);
                }else{
                    System.out.println("Time elapsed for the DegDistrAll query: " + elapsedSeconds + " seconds, (Query Range: [" + inputFirstGID + ", " + inputLastGID + "])");
                }
                break;
            }
            default:
                break;
        }
    }
}
