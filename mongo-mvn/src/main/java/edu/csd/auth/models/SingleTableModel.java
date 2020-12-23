package edu.csd.auth.models;


import com.mongodb.BasicDBObject;
import com.mongodb.client.*;
import com.mongodb.client.model.*;
import edu.csd.auth.utils.Edge;
import edu.csd.auth.utils.Interval;
import edu.csd.auth.utils.DiaNode;
import edu.csd.auth.utils.Vertex;

import static edu.csd.auth.models.DataModel.getRandomString;
import edu.csd.auth.utils.SnapshotResult;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


import org.bson.Document;

public class SingleTableModel implements DataModel
{
    private String keyspace ;
    private MongoClient client ;
    private MongoDatabase database ;
    
    public static String bb_to_str(ByteBuffer buffer)
    {
        String data;
        Charset charset = Charset.forName("UTF-8");
        CharsetDecoder decoder = charset.newDecoder();
        try
        {
            int old_position = buffer.position();
            data = decoder.decode(buffer).toString();
            // reset buffer's position to its original so it is not altered:
            buffer.position(old_position);
        }
        catch (CharacterCodingException e)
        {
            e.printStackTrace();
            return "";
        }
        return data;
    }

    public static Map<String, List<Edge>> convertToEdgeList(Document edgesUDTList)
    {
        if (edgesUDTList == null)
            return Collections.EMPTY_MAP;
        
        Map<String, List<Edge>> edges = new HashMap<>();
        
        for (String ver : edgesUDTList.keySet())
        {
            List<Document> edgesUDT = (List<Document>)edgesUDTList.get(ver);
            List<Edge> vertex_edges = new ArrayList<>();
            
            for (Document udt : edgesUDT)
            {
                Edge edge = new Edge("temp", udt.get("weight", String.class), udt.get("otherEnd", String.class), udt.get("start", String.class), udt.get("end", String.class));
                vertex_edges.add(edge);
            }
            edges.put(ver, vertex_edges);
        }
        
        return edges;
    }
    public static List<Interval> convertToIntervals(List<Document> nameUDTList)
    {
        List<Interval> list = new ArrayList<>();
        
        for (Document val : nameUDTList)
            list.add(new Interval(val.get("value", String.class),val.get("start", String.class),val.get("end", String.class)));
        
        Collections.sort(list);
        return list;
    }
    
    public SingleTableModel(MongoClient client, String keyspace)
    {
        this.client = client;
        this.keyspace = keyspace;
        this.database = client.getDatabase(keyspace);
    }
    
    @Override
    public void createSchema()
    {
        try
        {
            database = client.getDatabase(keyspace);
            //session.execute("DROP KEYSPACE IF EXISTS "+keyspace+";");
            Thread.sleep(30000);

            database.drop();
            //session.execute("CREATE KEYSPACE IF NOT EXISTS "+keyspace+" WITH replication " + "= {'class':'SimpleStrategy', 'replication_factor':1};");
            Thread.sleep(5000);

            Thread.sleep(10000);
            database.createCollection("dianode");
            BasicDBObject index = new BasicDBObject();
            index.put("vid", 1);
            index.put("start", 1);
            index.put("end", 1);
            database.getCollection("dianode").createIndex(index);
            index.clear();
            index.put("start", 1);
            index.put("end", 1);
            database.getCollection("dianode").createIndex(index);
            index.clear();
            index.put("vid", 1);
            database.getCollection("dianode").createIndex(index);
            Thread.sleep(10000);
        }
        catch (InterruptedException ex)
        {
            Logger.getLogger(SingleTableModel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public HashMap<String, ArrayList<String>> getAllAliveVertices(String first, String last) // Each key is the time instance and its value are the vIDs that are alive.
    {
        long tStart, tEnd, tDelta;
        
        HashMap<String, ArrayList<String>> vertices = new HashMap<>();
        vertices.put("allVertices", new ArrayList<>()); // The "allVertices" key contains a list of all vIDs that are alive at some point in [first, last]
        
        tStart = System.nanoTime();
        FindIterable<Document> cursor  = database.getCollection("dianode").find().projection(Projections.include("vid", "start", "end")).noCursorTimeout(true);
        //List<Document> rows = cursor();
        tEnd = System.nanoTime();
        tDelta = tEnd - tStart;
        double elapsedSeconds = tDelta / 1000000000.0;
        System.out.println("Time required for retrieving [start,end] info for all vertices: " + elapsedSeconds + " seconds, (Query Range: [" + first + ", " + last + "])");
        
        tStart = System.nanoTime();
        for (Document row : cursor)
        {
            int rowstart = Integer.parseInt(row.getString("start"));
            int rowend = Integer.parseInt(row.getString("end"));
            
            if (rowend <= Integer.parseInt(first) || Integer.parseInt(last) < rowstart) // Assumes correct intervals as input
                continue;
            
            String vid = row.getString("vid");
            int start = Math.max(Integer.parseInt(first), rowstart); // Only report values that are after both "first" and the diachronic node's "rowstart"
            int end = Math.min(Integer.parseInt(last), rowend); // Only report values that are before both "last" and the diachronic node's "rowend"
            for (int i = start; i <= end; i++)
            {
                if (!vertices.containsKey("" + i))
                    vertices.put("" + i, new ArrayList<>());
                
                vertices.get("" + i).add(vid);
            }
            vertices.get("allVertices").add(vid);
        }
        List<String> duplicates = vertices.get("allVertices"); // Remove duplicate vIDs from "allVertices"
        Set<String> unique = new HashSet<>(duplicates);
        vertices.put("allVertices", new ArrayList<>(unique));
        tEnd = System.nanoTime();
        tDelta = tEnd - tStart;
        elapsedSeconds = tDelta / 1000000000.0;
        System.out.println("Time required for processing the info into a HashMap: " + elapsedSeconds + " seconds, (Query Range: [" + first + ", " + last + "])");
        return vertices;
    }
    public Map<String, Map<String, Edge>> getAllEdgesOfVertex(String vid, String timestamp) 
    {
        //ResultSet rs = session.execute("SELECT incoming_edges, outgoing_edges FROM "+keyspace+".dianode " + "WHERE vid = '" + vid + "'");
        FindIterable<Document> cursor  = database.getCollection("dianode").find(Filters.eq("vid", vid)).projection(Projections.include("incoming_edges", "outgoing_edges")).noCursorTimeout(true);

        Document vertexRow = cursor.first(); // We only expect one row
        
        Map<String, Map<String, Edge>> allEdges = new HashMap<>();
        Map<String, Edge> incoming_edges_current = new HashMap<>();
        Map<String, Edge> outgoing_edges_current = new HashMap<>();

        Document edgesUDTListIn = (Document) vertexRow.get("incoming_edges");
        Map<String, List<Edge>> incoming_edges_diachronic = convertToEdgeList(edgesUDTListIn);

        for (String sourceID : incoming_edges_diachronic.keySet())
        {
            List<Edge> edges = incoming_edges_diachronic.get(sourceID);
            for (Edge edge : edges)
                if (Integer.parseInt(timestamp) >= Integer.parseInt(edge.start) && Integer.parseInt(timestamp) < Integer.parseInt(edge.end))
                {
                    incoming_edges_current.put(sourceID, edge);
                    break;
                }
        }
        
        Document edgesUDTListOut = (Document) vertexRow.get("outgoing_edges");
        Map<String, List<Edge>> outgoing_edges_diachronic = convertToEdgeList(edgesUDTListOut); 
        
        for (String targetID : outgoing_edges_diachronic.keySet())
        {
            List<Edge> edges = outgoing_edges_diachronic.get(targetID);
            for (Edge edge : edges)
                if (Integer.parseInt(timestamp) >= Integer.parseInt(edge.start) && Integer.parseInt(timestamp) < Integer.parseInt(edge.end))
                {
                    outgoing_edges_current.put(targetID, edge);
                    break;
                }
        }
        
        allEdges.put("incoming_edges", incoming_edges_current);
        allEdges.put("outgoing_edges", outgoing_edges_current);
        
        return allEdges;
    }
    public List<DiaNode> getAllVerticesAndFilterAlive(String first, String last) // Each key is the time instance and its value are the vIDs that are alive.
    {
        long tStart, tEnd, tDelta;
        
        ArrayList<DiaNode> dianodes = new ArrayList<>();
        
        tStart = System.nanoTime();
        FindIterable<Document> cursor = database.getCollection("dianode").find();
        //List<Document> rows = cursor();
        tEnd = System.nanoTime();
        tDelta = tEnd - tStart;
        double elapsedSeconds = tDelta / 1000000000.0;
        System.out.println("Time required for retrieving all the diachronic nodes: " + elapsedSeconds + " seconds");
        
        tStart = System.nanoTime();
        for (Document row : cursor)
        {
            int rowstart = Integer.parseInt(row.getString("start"));
            int rowend = Integer.parseInt(row.getString("end"));
            
            if (rowend < Integer.parseInt(first) || Integer.parseInt(last) < rowstart) // Assumes correct intervals as input
                continue;
            
            //String vid = row.getString("vid");
            //int start = Math.max(Integer.parseInt(first), rowstart); // Only report values that are after both "first" and the diachronic node's "rowstart"
            //int end = Math.min(Integer.parseInt(last), rowend); // Only report values that are before both "last" and the diachronic node's "rowend"
            
            DiaNode dn = new DiaNode(row);
            dn.keepValuesInInterval(first, last);
            dianodes.add(dn);
        }
        tEnd = System.nanoTime();
        tDelta = tEnd - tStart;
        elapsedSeconds = tDelta / 1000000000.0;
        System.out.println("Time required for filtering out the alive diachronic nodes: " + elapsedSeconds + " seconds, (Query Range: [" + first + ", " + last + "])");
        return dianodes;
    }

    @Override
    public List<SnapshotResult> getAvgVertexDegree(String first, String last) // Running example query range: [30,50]
    {
        long tStart, tEnd, tDelta;
        
        HashMap<String, Double> edgeCounts = new HashMap<>(); // Holds the total edge count at any point in the query range (e.g. [(30,2), (31,4), ..., (50,22)]
        HashMap<String, Double> vertexCounts = new HashMap<>(); // Holds the total vertex count at any point in the query range (e.g. [(30,4), (31,3), ..., (50,16)]
        
        HashMap<String, ArrayList<String>> vertices = getAllAliveVertices(first, last);
        ArrayList<String> allVertices = vertices.get("allVertices");
        
        for (int i=Integer.parseInt(first); i<=Integer.parseInt(last); i++)
        {
            ArrayList<String> instanceVIDs = vertices.get(""+i);
            if (instanceVIDs == null)
                vertexCounts.put(""+i, 0.0);
            else
                vertexCounts.put(""+i, (double) instanceVIDs.size());
        }
        
        //PreparedStatement statement = session.prepare("SELECT start, end, outgoing_edges FROM "+keyspace+".dianode WHERE vid = ?");
        
        tStart = System.nanoTime();


        tEnd = System.nanoTime();
        tDelta = tEnd - tStart;
        double elapsedSeconds = tDelta / 1000000000.0;
        System.out.println("Time required for retrieving the relevant alive nodes: " + elapsedSeconds + " seconds.");

        tStart = System.nanoTime();
        for (String vertex : allVertices) {
            FindIterable<Document> cursor = database.getCollection("dianode").find(Filters.eq("vid", vertex)).projection(Projections.include("start","end", "outgoing_edges")).noCursorTimeout(true);

            for (Document row : cursor) // For each diachronic node
            {
                String rowend = row.getString("end");
                if (Integer.parseInt(rowend) < Integer.parseInt(first)) // That means that the diachronic node's "start" and "end" time instances were BOTH before our query instance
                    continue;

                String rowstart = row.getString("start");

                int start = Math.max(Integer.parseInt(first), Integer.parseInt(rowstart)); // Only report values that are after both "first" and the diachronic node's "rowstart"
                int end = Math.min(Integer.parseInt(last), Integer.parseInt(rowend)); // Only report values that are before both "last" and the diachronic node's "rowend"

                //TypeToken<List<Document>> listOfEdges = new TypeToken<>() {};
                Document edgesUDTList = (Document) row.get("outgoing_edges");
                Map<String, List<Edge>> outgoing_edges = SingleTableModel.convertToEdgeList(edgesUDTList); // Fetch all the edges of the diachronic node

                for (String targetID : outgoing_edges.keySet()) {
                    List<Edge> edges = outgoing_edges.get(targetID);
                    for (Edge edge : edges) // For each edge in the diachronic node
                    {
                        int edgeStart = Math.max(start, Integer.parseInt(edge.start));
                        int edgeEnd = Math.min(end, Integer.parseInt(edge.end));

                        for (int i = edgeStart; i <= edgeEnd; i++) // Increase the edge count for any edges found overlapping or intersecting the [start,end] range specified before
                        {
                            if (!edgeCounts.containsKey("" + i))
                                edgeCounts.put("" + i, 0.0);
                            edgeCounts.put("" + i, edgeCounts.get("" + i) + 1.0);
                        }
                    }
                }
            }
        }
        ArrayList<SnapshotResult> results = new ArrayList<>();
        for (int i=Integer.parseInt(first); i<=Integer.parseInt(last); i++)
        {
            Double e_count = edgeCounts.get(""+i);
            Double v_count = vertexCounts.get(""+i);
            
            if (e_count == null || v_count == null || v_count == 0.0 || e_count == 0.0)
            {
                results.add(new SnapshotResult(""+i, 0.0));
                continue;
            }
            
            results.add(new SnapshotResult(""+i, (e_count / v_count)));
        }
        tEnd = System.nanoTime();
        tDelta = tEnd - tStart;
        elapsedSeconds = tDelta / 1000000000.0;
        System.out.println("Time required for processing and evaluating the AvgDeg query: " + elapsedSeconds + " seconds.");
        
        return results;
    }
    @Override
    public List<SnapshotResult> getAvgVertexDegreeFetchAllVertices(String first, String last)
    {
        long tStart, tEnd, tDelta;
        
        HashMap<String, Double> edgeCounts = new HashMap<>(); // Holds the total edge count at any point in the query range (e.g. [(30,2), (31,4), ..., (50,22)]
        HashMap<String, Double> vertexCounts = new HashMap<>(); // Holds the total vertex count at any point in the query range (e.g. [(30,4), (31,3), ..., (50,16)]
        
        List<DiaNode> dianodes = getAllVerticesAndFilterAlive(first, last);
        
        for (int i = Integer.parseInt(first); i <= Integer.parseInt(last); i++)
            vertexCounts.put(""+i, 0.0);
        
        tStart = System.nanoTime();
        for (DiaNode dn : dianodes) // For each diachronic node
        {
            for (int i = Integer.parseInt(dn.getStart()); i <= Integer.parseInt(dn.getEnd()); i++)
                vertexCounts.put(""+i, vertexCounts.get(""+i) + 1.0);
            
            Map<String, List<Edge>> outgoing_edges = dn.getOutgoing_edges();
            
            for (String targetID : outgoing_edges.keySet())
            {
                List<Edge> edges = outgoing_edges.get(targetID);
                for (Edge edge : edges) // For each edge in the diachronic node
                {
                    int edgeStart = Integer.parseInt(edge.start);
                    int edgeEnd = Integer.parseInt(edge.end);
                    
                    for (int i = edgeStart; i <= edgeEnd; i++) // Increase the edge count for any edges found overlapping or intersecting the [start,end] range specified before
                    {
                        if (!edgeCounts.containsKey("" + i))
                            edgeCounts.put("" + i, 0.0);
                        
                        edgeCounts.put("" + i, edgeCounts.get("" + i) + 1.0);
                    }
                }
            }
        }
        
        ArrayList<SnapshotResult> results = new ArrayList<>();
        for (int i = Integer.parseInt(first); i <= Integer.parseInt(last); i++)
        {
            Double e_count = edgeCounts.get("" + i);
            Double v_count = vertexCounts.get("" + i);
            
            if (e_count == null || v_count == null || v_count == 0.0 || e_count == 0.0)
            {
                results.add(new SnapshotResult("" + i, 0.0));
                continue;
            }
            
            results.add(new SnapshotResult("" + i, (e_count / v_count)));
        }
        tEnd = System.nanoTime();
        tDelta = tEnd - tStart;
        double elapsedSeconds = tDelta / 1000000000.0;
        System.out.println("Time required for processing and evaluating the AvgDeg query: " + elapsedSeconds + " seconds.");
        
        return results;
    }
    @Override
    public HashMap<String, HashMap<String, Integer>> getDegreeDistribution(String first, String last)
    {
        long tStart, tEnd, tDelta;
        HashMap<String, HashMap<String, Integer>> results = new HashMap<>();
        //HashMap<String, List<Double>> allDegreesPerTimeInstance = new HashMap<>(); // Holds the total degree count for all vertices that exist in each time instance (e.g. [(0,[2,2,5,1,3,2]), (1,[5,2,1,1,2]), ...])

        HashMap<String, ArrayList<String>> vertices = getAllAliveVertices(first, last);
        ArrayList<String> allVertices = vertices.get("allVertices");

        //PreparedStatement statement = session.prepare("SELECT start, end, outgoing_edges FROM "+keyspace+".dianode WHERE vid = ?");

        tStart = System.nanoTime();

        tEnd = System.nanoTime();
        tDelta = tEnd - tStart;
        double elapsedSeconds = tDelta / 1000000000.0;
        System.out.println("Time required for retrieving the relevant alive nodes: " + elapsedSeconds + " seconds.");

        tStart = System.nanoTime();
        for (String vertex : allVertices) {
            FindIterable<Document> resultSetFuture = database.getCollection("dianode").find(Filters.eq("vid", vertex)).projection(Projections.include("start", "end","outgoing_edges")).noCursorTimeout(true);

            for (Document row : resultSetFuture) // For each diachronic node
            {
                String rowend = row.getString("end");
                if (Integer.parseInt(rowend) < Integer.parseInt(first)) // That means that the diachronic node's "start" and "end" time instances were BOTH before our query instance
                    continue;

                String rowstart = row.getString("start");

                int start = Math.max(Integer.parseInt(first), Integer.parseInt(rowstart)); // Only report values that are after both "first" and the diachronic node's "rowstart"
                int end = Math.min(Integer.parseInt(last), Integer.parseInt(rowend)); // Only report values that are before both "last" and the diachronic node's "rowend"

                //TypeToken<List<Document>> listOfEdges = new TypeToken<List<Document>>() {};
                Document edgesUDTList = (Document) row.get("outgoing_edges");
                Map<String, List<Edge>> outgoing_edges = SingleTableModel.convertToEdgeList(edgesUDTList); // Fetch all the edges of the diachronic node

                HashMap<String, Double> vertexDegreePerTimeInstance = new HashMap<>(); // Holds all the degrees for this particular vertex for all query instances that it exists in

                for (int i = start; i <= end; i++) // The vertex has "0" degree in all time instances contained in the query range
                    vertexDegreePerTimeInstance.put("" + i, 0.0);

                for (String targetID : outgoing_edges.keySet()) {
                    List<Edge> edges = outgoing_edges.get(targetID);
                    for (Edge edge : edges) // For each edge in the diachronic node
                    {
                        int edgeStart = Math.max(start, Integer.parseInt(edge.start));
                        int edgeEnd = Math.min(end, Integer.parseInt(edge.end));

                        for (int i = edgeStart; i <= edgeEnd; i++) // Increase the edge count for any edges found overlapping or intersecting the [start,end] range specified before
                            vertexDegreePerTimeInstance.put("" + i, vertexDegreePerTimeInstance.get("" + i) + 1.0);
                    }
                }

                for (String instance : vertexDegreePerTimeInstance.keySet()) // For each time instance in this vertex's history add the vertex's degree to the corresponding overall degree list for that instance, found in "allDegreesPerTimeInstance"
                {
                    Double degree =vertexDegreePerTimeInstance.get(instance);
                    HashMap<String, Integer> degreeDistr = results.get(instance);
                    if(degreeDistr == null){
                        degreeDistr = new HashMap<>();
                    }
                    Integer count = degreeDistr.get(degree.toString());
                    degreeDistr.put(degree.toString(), (count == null) ? 1 : count + 1);
                    results.put(instance, degreeDistr);
                }
            }
        }
        tEnd = System.nanoTime();
        tDelta = tEnd - tStart;
        elapsedSeconds = tDelta / 1000000000.0;
        System.out.println("Time required for processing and evaluating the DegDistr query: " + elapsedSeconds + " seconds.");
        return results;
    }
    @Override
    public HashMap<String, HashMap<String, Integer>> getDegreeDistributionFetchAllVertices(String first, String last)
    {
        long tStart, tEnd, tDelta;
        
        //HashMap<String, List<Double>> allDegreesPerTimeInstance = new HashMap<>(); // Holds the total degree count for all vertices that exist in each time instance (e.g. [(0,[2,2,5,1,3,2]), (1,[5,2,1,1,2]), ...])
        HashMap<String, HashMap<String, Integer>> results = new HashMap<>();
        List<DiaNode> dianodes = getAllVerticesAndFilterAlive(first, last);
        
        tStart = System.nanoTime();
        for (DiaNode dn : dianodes) // For each diachronic node
        {
            String rowend = dn.getEnd();
            String rowstart = dn.getStart();
            
            int start = Integer.parseInt(rowstart);
            int end = Integer.parseInt(rowend);
            
            Map<String, List<Edge>> outgoing_edges = dn.getOutgoing_edges();
            
            HashMap<String, Double> vertexDegreePerTimeInstance = new HashMap<>(); // Holds all the degrees for this particular vertex for all query instances that it exists in
            
            for (int i = start; i <= end; i++) // The vertex has "0" degree in all time instances contained in the query range
                vertexDegreePerTimeInstance.put("" + i, 0.0);
            
            for (String targetID : outgoing_edges.keySet())
            {
                List<Edge> edges = outgoing_edges.get(targetID);
                for (Edge edge : edges) // For each edge in the diachronic node
                {
                    int edgeStart = Math.max(start, Integer.parseInt(edge.start));
                    int edgeEnd = Math.min(end, Integer.parseInt(edge.end));
                    
                    for (int i = edgeStart; i <= edgeEnd; i++) // Increase the edge count for any edges found overlapping or intersecting the [start,end] range specified before
                        vertexDegreePerTimeInstance.put("" + i, vertexDegreePerTimeInstance.get("" + i) + 1.0);
                }
            }

            for (String instance : vertexDegreePerTimeInstance.keySet()) // For each time instance in this vertex's history add the vertex's degree to the corresponding overall degree list for that instance, found in "allDegreesPerTimeInstance"
            {

                Double degree =vertexDegreePerTimeInstance.get(instance);

                HashMap<String, Integer> degreeDistr = results.get(instance);
                if(degreeDistr == null){
                    degreeDistr = new HashMap<>();
                }
                Integer count = degreeDistr.get(degree.toString());
                degreeDistr.put(degree.toString(), (count == null) ? 1 : count + 1);
                results.put(instance, degreeDistr);
            }

        }
        tEnd = System.nanoTime();
        tDelta = tEnd - tStart;
        double elapsedSeconds = tDelta / 1000000000.0;
        System.out.println("Time required for processing and evaluating the DegDistrAll query: " + elapsedSeconds + " seconds.");
        return results;
    }
    @Override
    public List<String> getOneHopNeighborhood(String vid, String first, String last)
    {
        long tStart, tEnd, tDelta;
        List<String> results = new ArrayList<>();
        
        tStart = System.nanoTime();

        Document row= database.getCollection("dianode").find(Filters.eq("vid",vid)).projection(Projections.include( "outgoing_edges")).first();


        tEnd = System.nanoTime();
        tDelta = tEnd - tStart;
        double elapsedSeconds = tDelta / 1000000000.0;
        System.out.println("Time required for retrieving the outgoing edges of a diachronic node: " + elapsedSeconds + " seconds, (OneHop on VID, Timestamps: [" + vid + ", " + first + " to " + last + "])");
        if (row == null)
            return Collections.EMPTY_LIST;
        
        tStart = System.nanoTime();
        Document edgesUDTList =(Document) row.get("outgoing_edges");
        Map<String, List<Edge>> outgoing_edges = SingleTableModel.convertToEdgeList(edgesUDTList);
        
        for (String targetID : outgoing_edges.keySet())
        {
            List<Edge> edges = outgoing_edges.get(targetID);
            for (Edge edge : edges)
                if ((Integer.parseInt(first) >= Integer.parseInt(edge.start) && Integer.parseInt(first) < Integer.parseInt(edge.end)) ||
                        (Integer.parseInt(last) >= Integer.parseInt(edge.start) && Integer.parseInt(last) < Integer.parseInt(edge.end)))
                {
                    results.add(targetID);
                    break;
                }
        }
        tEnd = System.nanoTime();
        tDelta = tEnd - tStart;
        elapsedSeconds = tDelta / 1000000000.0;
        System.out.println("Time required for extracting the neighbors of a vertex: " + elapsedSeconds + " seconds, (OneHop on VID, Timestamps: [" + vid + ", " + first + " to " + last + "])");
        return results;
    }

    @Override
    public DiaNode getVertexHistory(String vid, String first, String last) 
    {
        // We only expect one row
        Document row = database.getCollection("dianode").find(Filters.eq("vid",vid)).first();
        if (row == null)
            return new DiaNode(vid);
        
        DiaNode dn = new DiaNode(row);   
        dn.keepValuesInInterval(first, last);
        
        return dn;
    }
    
    public Vertex getVertexInstance(String vid, String timestamp)
    {
        Document row = database.getCollection("dianode").find(Filters.eq("vid",vid)).first();

        //Row row = rs.one(); // We only expect one row

        DiaNode dn = new DiaNode(row);   
        Vertex ver = dn.convertToVertex(timestamp);
        
        return ver;
        
    }
    public void insert(DiaNode ver)
    {

        FindIterable<Document> t =  database.getCollection("dianode").find(Filters.eq("vid",ver.getVid()));
        if (!t.cursor().hasNext())
        {
            ArrayList<Document> name = new ArrayList<>();
            for (Interval interval : ver.getAttributes().get("name"))
            {
                name.add(interval.toDoc());
            }
            ArrayList<Document> color = new ArrayList<>();
            for (Interval interval : ver.getAttributes().get("color"))
            {
                name.add(interval.toDoc());
            }
            Document doc = new Document("vid", ver.getVid())
                    .append("start", ver.getStart())
                    .append("end", ver.getEnd())
                    .append("name", name )
                    .append("color", color);
            database.getCollection("dianode").insertOne(doc);
        }

    }

    @Override
    public void insertEdge(String sourceID, String targetID, String start, String end, String label, String weight)
    {
        // First, insert the edge as an outgoing edge
        Document vertexRow = database.getCollection("dianode").find(Filters.eq("vid",sourceID)).first();

       // We only expect one row
        Document edgesUDTList =(Document) vertexRow.get("outgoing_edges");

        Map<String, List<Edge>> edges = convertToEdgeList(edgesUDTList);
        List<Document> out_edges = new ArrayList<>();

        if (edges.isEmpty())
        {
            out_edges.add(new Edge(label, weight, targetID, start, end).toDoc());
            database.getCollection("dianode").findOneAndUpdate(
                    Filters.and(
                            Filters.and(
                                    Filters.eq("vid",sourceID),Filters.eq("start",vertexRow.getString("start"))),
                            Filters.eq("end",vertexRow.getString("end") ))
                            , Updates.set("outgoing_edges", new Document().append(targetID,out_edges)));
        }
        else
        {
            List<Edge> vertex_edges;
            if (edges.get(targetID) != null)
            {
                vertex_edges = edges.get(targetID);
                Edge lastEdge = vertex_edges.get(vertex_edges.size()-1); 
                lastEdge.end = start;
                out_edges.add(lastEdge.toDoc());
            }
            else
                out_edges.add(new Edge(label, weight, targetID, start, end).toDoc());

            edgesUDTList.put(targetID, out_edges);
            database.getCollection("dianode").findOneAndUpdate(
                    Filters.and(
                            Filters.and(
                                    Filters.eq("vid",sourceID),Filters.eq("start",vertexRow.getString("start"))),
                            Filters.eq("end",vertexRow.getString("end") ))
                    , Updates.set("outgoing_edges", edgesUDTList));
        }
        
        // Then, insert the edge as an incoming edge
        vertexRow = database.getCollection("dianode").find(Filters.eq("vid",targetID)).first();
        // We only expect one row
        if (vertexRow != null)
            edgesUDTList = (Document)vertexRow.get("incoming_edges");
        else
            edgesUDTList = null;
        edges = convertToEdgeList(edgesUDTList);

        List<Document> in_edges = new ArrayList<>();

        if (edges.isEmpty())
        {
            in_edges.add(new Edge(label, weight, targetID, start, end).toDoc());
            database.getCollection("dianode").findOneAndUpdate(
                    Filters.and(
                            Filters.and(
                                    Filters.eq("vid",targetID),Filters.eq("start",vertexRow.getString("start"))),
                            Filters.eq("end",vertexRow.getString("end") ))
                    , Updates.set("incoming_edges", new Document().append(sourceID,in_edges)));
        }
        else
        {
            List<Edge> vertex_edges;
            if (edges.get(sourceID) != null)
            {
                vertex_edges = edges.get(sourceID);
                Edge lastEdge = vertex_edges.get(vertex_edges.size() - 1);
                lastEdge.end = start;
                in_edges.add(lastEdge.toDoc());
            }
            else {
                in_edges.add(new Edge(label, weight, targetID, start, end).toDoc());

            }
            edgesUDTList.put(sourceID, in_edges);
            database.getCollection("dianode").findOneAndUpdate(
                    Filters.and(
                            Filters.and(
                                    Filters.eq("vid",targetID),Filters.eq("start",vertexRow.getString("start"))),
                            Filters.eq("end",vertexRow.getString("end") ))
                    , Updates.set("incoming_edges", edgesUDTList));
        }
    }

    @Override
    public void insertVertex(String vid, String name, String start, String end, String color) 
    {      
        DiaNode ver = new DiaNode(vid, start, end);
        List<Interval> namesList = new ArrayList<>();
        namesList.add(new Interval(name,start,end));
        
        List<Interval> colorsList = new ArrayList<>();
        colorsList.add(new Interval(color,start,end));
        
        ver.insertAttribute("name", namesList);
        ver.insertAttribute("color", colorsList);
        
        insert(ver);
    }
    
    private void parseFirstSnapshot(String input, int snap_count) // Used to bulk load data instead of using the typical methods
    {
        try
        {   
            BufferedReader file = new BufferedReader(new FileReader(input));
            String line;
            String[] tokens;
            
            TreeMap<String, Vertex> vertices = new TreeMap<>();
            
            while ((line = file.readLine()) != null)
            {
                if (line.startsWith("mkdir") || line.startsWith("cd") || line.startsWith("time") || line.startsWith("string") || line.startsWith("double") || line.startsWith("shutdown"))
                    continue;
                
                if (line.equals("graph 1 0") || line.equals("graph 1")) // As soon as we've reached the second snapshot, stop
                    break;

                
                else if (line.startsWith("vertex"))
                {
                    tokens = line.split(" ");
                    String verID = tokens[1];
                    String name, color;
                    if (tokens.length >= 3)
                        name = tokens[2].split("=")[1].replaceAll("\"", "");
                    else
                        name = getRandomString(4);
                    if (tokens.length == 4)
                        color = tokens[3].split("=")[1].replaceAll("\"", "");
                    else
                        color = getRandomString(4);
                    Vertex ver = new Vertex();
                    ver.setVid(verID);
                    ver.setTimestamp("00000000");
                    HashMap<String, String> attributes = new HashMap<>();
                    attributes.put("name", name);
                    attributes.put("color", color);
                    ver.setAttributes(attributes);
                    vertices.put(verID, ver);
                }
                else if (line.startsWith("edge"))
                {
                    tokens = line.split(" ");
                    String sourceID = tokens[1];
                    String targetID = tokens[2];
                    String weight;
                    if (tokens.length == 4)
                        weight = tokens[3].split("=")[1];
                    else
                        weight = ""+Math.random();

                    Vertex sVer = vertices.get(sourceID);
                    sVer.addOutgoingEdge(targetID, new Edge("testlabel", weight, targetID, "00000000", DataModel.padWithZeros(""+snap_count)));
                    Vertex tVer = vertices.get(targetID);
                    tVer.addIncomingEdge(sourceID, new Edge("testlabel", weight, sourceID, "00000000", DataModel.padWithZeros(""+snap_count)));
                    vertices.put(sourceID, sVer);
                    vertices.put(targetID, tVer);
                }
            }
            file.close();
            
            for (String vid : vertices.keySet())
            {
                Vertex ver = vertices.get(vid);
                HashMap<String, String> attrs = ver.getAttributes();
                
                String start = ver.getTimestamp();
                String end = DataModel.padWithZeros(""+snap_count);
                String name = attrs.get("name");
                String color = attrs.get("color");
                
                HashMap<String, Edge> allIncEdges = ver.getIncoming_edges();
                HashMap<String, Edge> allOutEdges = ver.getOutgoing_edges();
                Document incoming = new Document();
                Document outgoing = new Document();
                for (String source : allIncEdges.keySet())
                {
                    List<Document> in_edges = new ArrayList<>();
                    in_edges.add(allIncEdges.get(source).toDoc());
                    incoming.append(source,in_edges);
                }
                for (String target : allOutEdges.keySet())
                {
                    List<Document> out_edges = new ArrayList<>();
                    out_edges.add(allOutEdges.get(target).toDoc());
                    outgoing.append(target,out_edges);
                }
                List<Document> nameTemp = new ArrayList<>();
                nameTemp.add(new Document().append("value", name)
                        .append("start","00000000")
                        .append("end", ""+ DataModel.padWithZeros(""+snap_count)));
                List<Document> colorTemp = new ArrayList<>();
                colorTemp.add(new Document().append("value", color)
                        .append("start","00000000")
                        .append("end", ""+ DataModel.padWithZeros(""+snap_count)));

                Document doc = new Document("vid",ver.getVid())
                        .append("start", start)
                        .append("end", end)
                        .append("name", nameTemp)
                        .append("color", colorTemp)
                        .append("incoming_edges",incoming)
                        .append("outgoing_edges",outgoing);
                database.getCollection("dianode").insertOne(doc);
            }
        } catch (IOException ex)
        {
            Logger.getLogger(SingleTableModel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void parseInput(String input)
    {
        try
        {
            int snap_count = DataModel.getCountOfSnapshotsInInput(input);
            
            parseFirstSnapshot(input, snap_count);
            
            BufferedReader file = new BufferedReader(new FileReader(input));
            String line, curVersion = "0";
            String[] tokens;
            int verKcounter = 0;
            int edgeKcounter = 0;

            boolean passedFirstSnapshot = false;
            
            while ((line = file.readLine()) != null)
            {
                if (line.equals("graph 1 0") || line.equals("graph 1"))
                    passedFirstSnapshot = true;
                
                if (!passedFirstSnapshot || line.startsWith("mkdir") || line.startsWith("cd") || line.startsWith("time") || line.startsWith("string") || line.startsWith("double") || line.startsWith("shutdown"))
                    continue;

                if (line.startsWith("graph"))
                {
                    System.out.println(line);
                    tokens = line.split(" ");
                    if (tokens.length == 2) // "graph X" statement
                        curVersion = tokens[1];
                    else if (tokens.length == 3) // "graph X Y" statement
                        curVersion = tokens[1];
                }
                else if (line.startsWith("vertex"))
                {
                    tokens = line.split(" ");
                    String verID = tokens[1];
                    String name, color;
                    if (tokens.length >= 3)
                        name = tokens[2].split("=")[1].replaceAll("\"", "");
                    else
                        name = getRandomString(4);
                    if (tokens.length == 4)
                        color = tokens[3].split("=")[1].replaceAll("\"", "");
                    else
                        color = getRandomString(4);
                    insertVertex(verID, name, DataModel.padWithZeros(curVersion), DataModel.padWithZeros(""+snap_count), color);
                    verKcounter++;
                    if (verKcounter % 1000 == 0)
                        System.out.println("Vertices processed: " + verKcounter);
                }
                else if (line.startsWith("edge"))
                {
                    tokens = line.split(" ");
                    String sourceID = tokens[1];
                    String targetID = tokens[2];
                    String weight;
                    if (tokens.length == 4)
                        weight = tokens[3].split("=")[1];
                    else
                        weight = ""+Math.random();
                    insertEdge(sourceID, targetID, DataModel.padWithZeros(curVersion), DataModel.padWithZeros(""+snap_count), getRandomString(3), weight);
                    edgeKcounter++;
                    if (edgeKcounter % 1000 == 0)
                        System.out.println("Edges processed: " + edgeKcounter);
                }
                else if (line.startsWith("update vertex"))
                {
                    tokens = line.split(" ");
                    String verID = tokens[2];
                    String attrName = tokens[3].split("=")[0];
                    String value = tokens[3].split("=")[1].replaceAll("\"", "");
                    updateVertexAttribute(verID, attrName, value, curVersion);
                }
            }
            file.close();
        } catch (IOException ex)
        {
            Logger.getLogger(SingleTableModel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    //NOT YET IMPLEMENTED
    @Override
    public void updateVertexAttribute(String vid, String attrName, String attrValue, String timestamp)
    {
        //ResultSet rs = session.execute("SELECT * FROM "+keyspace+".dianode " + "WHERE vid = '" + vid + "'");
       // Row vertexRow = rs.one(); // We only expect one row
        Document vertexRow = database.getCollection("dianode").find(Filters.eq("vid",vid)).first();
        List<Document> attrUDTList = vertexRow.getList(attrName, Document.class);
        List<Interval> attrList = convertToIntervals(attrUDTList);
        
        Interval toBeDeleted = attrList.get(attrList.size()-1);

        /*
        // Remove the old interval
        session.execute("UPDATE "+keyspace+".dianode SET " + attrName + " = " + attrName + " - ["
                + toBeDeleted
                + "] WHERE vid = '" + vid + "' AND start = '" + vertexRow.getString("start") + "' AND end = '" + vertexRow.getString("end") + "';");

        // Add the updated old interval and the new interval
        session.execute("UPDATE "+keyspace+".dianode SET " + attrName + " = " + attrName + " + ["
                + new Interval(toBeDeleted.value, toBeDeleted.start, timestamp)
                + "] WHERE vid = '" + vid + "' AND start = '" + vertexRow.getString("start") + "' AND end = '" + vertexRow.getString("end") + "';");

        session.execute("UPDATE "+keyspace+".dianode SET " + attrName + " = " + attrName + " + ["
                + new Interval(attrValue,timestamp,"Infinity")
                + "] WHERE vid = '" + vid + "' AND start = '" + vertexRow.getString("start") + "' AND end = '" + vertexRow.getString("end") + "';");
        */
    }

    @Override
    public void useKeyspace() 
    {
    }

}
