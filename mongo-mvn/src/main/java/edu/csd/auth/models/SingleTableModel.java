package edu.csd.auth.models;

import com.mongodb.BasicDBObject;
import com.mongodb.WriteConcern;
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

public class SingleTableModel implements DataModel {
    private String keyspace;
    private MongoClient client;
    private MongoDatabase database;

    public static String bb_to_str(ByteBuffer buffer) {
        String data;
        Charset charset = Charset.forName("UTF-8");
        CharsetDecoder decoder = charset.newDecoder();
        try {
            int old_position = buffer.position();
            data = decoder.decode(buffer).toString();
            // reset buffer's position to its original so it is not altered:
            buffer.position(old_position);
        } catch (CharacterCodingException e) {
            e.printStackTrace();
            return "";
        }
        return data;
    }

    public static Map<String, List<Edge>> convertToEdgeList(Document edgesUDTList) {
        if (edgesUDTList == null)
            return Collections.EMPTY_MAP;

        Map<String, List<Edge>> edges = new HashMap<>();

        for (String ver : edgesUDTList.keySet()) {
            List<Document> edgesUDT = (List<Document>) edgesUDTList.get(ver);
            List<Edge> vertex_edges = new ArrayList<>();

            for (Document udt : edgesUDT) {
                Edge edge = new Edge("temp", udt.get("weight", String.class), udt.get("otherEnd", String.class),
                        udt.get("start", String.class), udt.get("end", String.class));
                vertex_edges.add(edge);
            }
            edges.put(ver, vertex_edges);
        }

        return edges;
    }

    public static List<Interval> convertToIntervals(List<Document> nameUDTList) {
        List<Interval> list = new ArrayList<>();

        for (Document val : nameUDTList)
            list.add(new Interval(val.get("value", String.class), val.get("start", String.class),
                    val.get("end", String.class)));

        Collections.sort(list);
        return list;
    }

    public SingleTableModel(MongoClient client, String keyspace) {
        this.client = client;
        this.keyspace = keyspace;
        this.database = client.getDatabase(keyspace);
    }

    @Override
    public void createSchema() {
        try {
            database = client.getDatabase(keyspace);
            Thread.sleep(30000);

            database.drop();
            Thread.sleep(5000);

            Thread.sleep(10000);
            database.createCollection("dianode");
            BasicDBObject index = new BasicDBObject();
            index.put("_id.vid", 1);
            database.getCollection("dianode").createIndex(index);

            index.clear();
            index.put("_id.start", 1);
            index.put("_id.end", 1);
            database.getCollection("dianode").createIndex(index);

            index.clear();
            index.put("_id.vid", 1);
            index.put("_id.start", 1);
            index.put("_id.end", 1);
            database.getCollection("dianode").createIndex(index);

            Thread.sleep(10000);
        } catch (InterruptedException ex) {
            Logger.getLogger(SingleTableModel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public HashMap<String, ArrayList<String>> getAllAliveVertices(String first, String last) // Each key is the time
                                                                                             // instance and its value
                                                                                             // are the vIDs that are
                                                                                             // alive.
    {
        long tStart, tEnd, tDelta;

        HashMap<String, ArrayList<String>> vertices = new HashMap<>();
        vertices.put("allVertices", new ArrayList<>()); // The "allVertices" key contains a list of all vIDs that are
                                                        // alive at some point in [first, last]

        tStart = System.currentTimeMillis();
        FindIterable<Document> cursor = database.getCollection("dianode").find().noCursorTimeout(true);
        tEnd = System.currentTimeMillis();
        tDelta = tEnd - tStart;
        double elapsedSeconds = tDelta / 1000.0;
        System.out.println("Time required for retrieving [start,end] info for all vertices: " + elapsedSeconds
                + " seconds, (Query Range: [" + first + ", " + last + "])");

        tStart = System.currentTimeMillis();
        for (Document row : cursor) {
            Document id = (Document) row.get("_id");
            //TODO
            int rowstart = Integer.parseInt(id.getString("start").substring(0,4));

            int rowend = Integer.parseInt(id.getString("end").substring(0,4));

            if (rowend <= Integer.parseInt(first) || Integer.parseInt(last) < rowstart) // Assumes correct intervals as
                                                                                        // input
                continue;

            String vid = id.getString("vid");
            int start = Math.max(Integer.parseInt(first), rowstart); // Only report values that are after both "first"
                                                                     // and the diachronic node's "rowstart"
            int end = Math.min(Integer.parseInt(last), rowend); // Only report values that are before both "last" and
                                                                // the diachronic node's "rowend"
            for (int i = start; i <= end; i++) {
                if (!vertices.containsKey("" + i))
                    vertices.put("" + i, new ArrayList<>());

                vertices.get("" + i).add(vid);
            }
            vertices.get("allVertices").add(vid);
        }
        List<String> duplicates = vertices.get("allVertices"); // Remove duplicate vIDs from "allVertices"
        Set<String> unique = new HashSet<>(duplicates);
        vertices.put("allVertices", new ArrayList<>(unique));
        tEnd = System.currentTimeMillis();
        tDelta = tEnd - tStart;
        elapsedSeconds = tDelta / 1000.0;
        System.out.println("Time required for processing the info into a HashMap: " + elapsedSeconds
                + " seconds, (Query Range: [" + first + ", " + last + "])");
        return vertices;
    }

    public Map<String, Map<String, Edge>> getAllEdgesOfVertex(String vid, String timestamp) {
        FindIterable<Document> cursor = database.getCollection("dianode").find(Filters.eq("_id.vid", vid));

        Document vertexRow = cursor.first(); // We only expect one row

        Map<String, Map<String, Edge>> allEdges = new HashMap<>();
        Map<String, Edge> incoming_edges_current = new HashMap<>();
        Map<String, Edge> outgoing_edges_current = new HashMap<>();

        Document edgesUDTListIn = (Document) vertexRow.get("incoming_edges");
        Map<String, List<Edge>> incoming_edges_diachronic = convertToEdgeList(edgesUDTListIn);

        for (String sourceID : incoming_edges_diachronic.keySet()) {
            List<Edge> edges = incoming_edges_diachronic.get(sourceID);
            for (Edge edge : edges)
                if (Integer.parseInt(timestamp) >= Integer.parseInt(edge.start)
                        && Integer.parseInt(timestamp) < Integer.parseInt(edge.end)) {
                    incoming_edges_current.put(sourceID, edge);
                    break;
                }
        }

        Document edgesUDTListOut = (Document) vertexRow.get("outgoing_edges");
        Map<String, List<Edge>> outgoing_edges_diachronic = convertToEdgeList(edgesUDTListOut);

        for (String targetID : outgoing_edges_diachronic.keySet()) {
            List<Edge> edges = outgoing_edges_diachronic.get(targetID);
            for (Edge edge : edges)
                if (Integer.parseInt(timestamp) >= Integer.parseInt(edge.start)
                        && Integer.parseInt(timestamp) < Integer.parseInt(edge.end)) {
                    outgoing_edges_current.put(targetID, edge);
                    break;
                }
        }

        allEdges.put("incoming_edges", incoming_edges_current);
        allEdges.put("outgoing_edges", outgoing_edges_current);

        return allEdges;
    }

    public List<DiaNode> getAllVerticesAndFilterAlive(String first, String last) // Each key is the time instance and
                                                                                 // its value are the vIDs that are
                                                                                 // alive.
    {
        long tStart, tEnd, tDelta;

        ArrayList<DiaNode> dianodes = new ArrayList<>();

        tStart = System.currentTimeMillis();
        FindIterable<Document> cursor = database.getCollection("dianode").find().noCursorTimeout(true);
        tEnd = System.currentTimeMillis();
        tDelta = tEnd - tStart;
        double elapsedSeconds = tDelta / 1000.0;
        System.out.println("Time required for retrieving all the diachronic nodes: " + elapsedSeconds + " seconds");

        tStart = System.currentTimeMillis();
        for (Document row : cursor) {
            Document id = (Document) row.get("_id");
            int rowstart = Integer.parseInt(id.getString("start"));
            int rowend = Integer.parseInt(id.getString("end"));

            if (rowend < Integer.parseInt(first) || Integer.parseInt(last) < rowstart) // Assumes correct intervals as
                                                                                       // input
                continue;

            DiaNode dn = new DiaNode(row);
            dn.keepValuesInInterval(first, last);
            dianodes.add(dn);
        }
        tEnd = System.currentTimeMillis();
        tDelta = tEnd - tStart;
        elapsedSeconds = tDelta / 1000.0;
        System.out.println("Time required for filtering out the alive diachronic nodes: " + elapsedSeconds
                + " seconds, (Query Range: [" + first + ", " + last + "])");
        return dianodes;
    }

    @Override
    public List<SnapshotResult> getAvgVertexDegree(String first, String last) // Running example query range: [30,50]
    {
        long tStart, tEnd, tDelta;

        HashMap<String, Double> edgeCounts = new HashMap<>(); // Holds the total edge count at any point in the query
                                                              // range (e.g. [(30,2), (31,4), ..., (50,22)]
        HashMap<String, Double> vertexCounts = new HashMap<>(); // Holds the total vertex count at any point in the
                                                                // query range (e.g. [(30,4), (31,3), ..., (50,16)]

        HashMap<String, ArrayList<String>> vertices = getAllAliveVertices(first, last);
        ArrayList<String> allVertices = vertices.get("allVertices");

        for (int i = Integer.parseInt(first); i <= Integer.parseInt(last); i++) {
            ArrayList<String> instanceVIDs = vertices.get("" + i);
            if (instanceVIDs == null)
                vertexCounts.put("" + i, 0.0);
            else
                vertexCounts.put("" + i, (double) instanceVIDs.size());
        }

        tStart = System.currentTimeMillis();

        tEnd = System.currentTimeMillis();
        tDelta = tEnd - tStart;
        double elapsedSeconds = tDelta / 1000.0;
        System.out.println("Time required for retrieving the relevant alive nodes: " + elapsedSeconds + " seconds.");

        tStart = System.currentTimeMillis();
        for (String vertex : allVertices) {
            FindIterable<Document> cursor = database.getCollection("dianode").find(Filters.eq("_id.vid", vertex));

            for (Document row : cursor) // For each diachronic node
            {
                Document id = (Document) row.get("_id");
                String rowend = id.getString("end");
                if (Integer.parseInt(rowend) < Integer.parseInt(first)) // That means that the diachronic node's "start"
                                                                        // and "end" time instances were BOTH before our
                                                                        // query instance
                    continue;

                String rowstart = id.getString("start");

                int start = Math.max(Integer.parseInt(first), Integer.parseInt(rowstart)); // Only report values that
                                                                                           // are after both "first" and
                                                                                           // the diachronic node's
                                                                                           // "rowstart"
                int end = Math.min(Integer.parseInt(last), Integer.parseInt(rowend)); // Only report values that are
                                                                                      // before both "last" and the
                                                                                      // diachronic node's "rowend"

                Document edgesUDTList = (Document) row.get("outgoing_edges");
                Map<String, List<Edge>> outgoing_edges = SingleTableModel.convertToEdgeList(edgesUDTList); // Fetch all
                                                                                                           // the edges
                                                                                                           // of the
                                                                                                           // diachronic
                                                                                                           // node

                for (String targetID : outgoing_edges.keySet()) {
                    List<Edge> edges = outgoing_edges.get(targetID);
                    for (Edge edge : edges) // For each edge in the diachronic node
                    {
                        int edgeStart = Math.max(start, Integer.parseInt(edge.start));
                        int edgeEnd = Math.min(end, Integer.parseInt(edge.end));

                        for (int i = edgeStart; i <= edgeEnd; i++) // Increase the edge count for any edges found
                                                                   // overlapping or intersecting the [start,end] range
                                                                   // specified before
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
        for (int i = Integer.parseInt(first); i <= Integer.parseInt(last); i++) {
            Double e_count = edgeCounts.get("" + i);
            Double v_count = vertexCounts.get("" + i);

            if (e_count == null || v_count == null || v_count == 0.0 || e_count == 0.0) {
                results.add(new SnapshotResult("" + i, 0.0));
                continue;
            }

            results.add(new SnapshotResult("" + i, (e_count / v_count)));
        }
        tEnd = System.currentTimeMillis();
        tDelta = tEnd - tStart;
        elapsedSeconds = tDelta / 1000.0;
        System.out.println(
                "Time required for processing and evaluating the AvgDeg query: " + elapsedSeconds + " seconds.");

        return results;
    }

    @Override
    public List<SnapshotResult> getAvgVertexDegreeFetchAllVertices(String first, String last) {
        long tStart, tEnd, tDelta;

        HashMap<String, Double> edgeCounts = new HashMap<>(); // Holds the total edge count at any point in the query
                                                              // range (e.g. [(30,2), (31,4), ..., (50,22)]
        HashMap<String, Double> vertexCounts = new HashMap<>(); // Holds the total vertex count at any point in the
                                                                // query range (e.g. [(30,4), (31,3), ..., (50,16)]

        List<DiaNode> dianodes = getAllVerticesAndFilterAlive(first, last);

        for (int i = Integer.parseInt(first); i <= Integer.parseInt(last); i++)
            vertexCounts.put("" + i, 0.0);

        tStart = System.currentTimeMillis();
        for (DiaNode dn : dianodes) // For each diachronic node
        {
            for (int i = Integer.parseInt(dn.getStart()); i <= Integer.parseInt(dn.getEnd()); i++)
                vertexCounts.put("" + i, vertexCounts.get("" + i) + 1.0);

            Map<String, List<Edge>> outgoing_edges = dn.getOutgoing_edges();

            for (String targetID : outgoing_edges.keySet()) {
                List<Edge> edges = outgoing_edges.get(targetID);
                for (Edge edge : edges) // For each edge in the diachronic node
                {
                    int edgeStart = Integer.parseInt(edge.start);
                    int edgeEnd = Integer.parseInt(edge.end);

                    for (int i = edgeStart; i <= edgeEnd; i++) // Increase the edge count for any edges found
                                                               // overlapping or intersecting the [start,end] range
                                                               // specified before
                    {
                        if (!edgeCounts.containsKey("" + i))
                            edgeCounts.put("" + i, 0.0);

                        edgeCounts.put("" + i, edgeCounts.get("" + i) + 1.0);
                    }
                }
            }
        }

        ArrayList<SnapshotResult> results = new ArrayList<>();
        for (int i = Integer.parseInt(first); i <= Integer.parseInt(last); i++) {
            Double e_count = edgeCounts.get("" + i);
            Double v_count = vertexCounts.get("" + i);

            if (e_count == null || v_count == null || v_count == 0.0 || e_count == 0.0) {
                results.add(new SnapshotResult("" + i, 0.0));
                continue;
            }

            results.add(new SnapshotResult("" + i, (e_count / v_count)));
        }
        tEnd = System.currentTimeMillis();
        tDelta = tEnd - tStart;
        double elapsedSeconds = tDelta / 1000.0;
        System.out.println(
                "Time required for processing and evaluating the AvgDeg query: " + elapsedSeconds + " seconds.");

        return results;
    }

    @Override
    public HashMap<String, HashMap<String, Integer>> getDegreeDistribution(String first, String last) {
        long tStart, tEnd, tDelta;

        HashMap<String, HashMap<String, Integer>> results = new HashMap<>();
        HashMap<String, ArrayList<String>> vertices = getAllAliveVertices(first, last);
        ArrayList<String> allVertices = vertices.get("allVertices");

        tStart = System.currentTimeMillis();

        tEnd = System.currentTimeMillis();
        tDelta = tEnd - tStart;
        double elapsedSeconds = tDelta / 1000.0;
        System.out.println("Time required for retrieving the relevant alive nodes: " + elapsedSeconds + " seconds.");

        tStart = System.currentTimeMillis();
        for (String vertex : allVertices) {
            FindIterable<Document> resultSetFuture = database.getCollection("dianode")
                    .find(Filters.eq("_id.vid", vertex)).noCursorTimeout(true);

            for (Document row : resultSetFuture) // For each diachronic node
            {
                Document id = (Document) row.get("_id");
                String rowend = id.getString("end").substring(0,4);
                if (Integer.parseInt(rowend) < Integer.parseInt(first)) // That means that the diachronic node's "start"
                                                                        // and "end" time instances were BOTH before our
                                                                        // query instance
                    continue;

                String rowstart = id.getString("start").substring(0,4);

                int start = Math.max(Integer.parseInt(first), Integer.parseInt(rowstart)); // Only report values that
                                                                                           // are after both "first" and
                                                                                           // the diachronic node's
                                                                                           // "rowstart"
                int end = Math.min(Integer.parseInt(last), Integer.parseInt(rowend)); // Only report values that are
                                                                                      // before both "last" and the
                                                                                      // diachronic node's "rowend"

                Document edgesUDTList = (Document) row.get("outgoing_edges");
                Map<String, List<Edge>> outgoing_edges = SingleTableModel.convertToEdgeList(edgesUDTList); // Fetch all
                                                                                                           // the edges
                                                                                                           // of the
                                                                                                           // diachronic
                                                                                                           // node

                HashMap<String, Double> vertexDegreePerTimeInstance = new HashMap<>(); // Holds all the degrees for this
                                                                                       // particular vertex for all
                                                                                       // query instances that it exists
                                                                                       // in

                for (int i = start; i <= end; i++) // The vertex has "0" degree in all time instances contained in the
                                                   // query range
                    vertexDegreePerTimeInstance.put("" + i, 0.0);

                for (String targetID : outgoing_edges.keySet()) {
                    List<Edge> edges = outgoing_edges.get(targetID);
                    for (Edge edge : edges) // For each edge in the diachronic node
                    {
                        int edgeStart = Math.max(start, Integer.parseInt(edge.start.substring(0,4)));
                        int edgeEnd = Math.min(end, Integer.parseInt(edge.end.substring(0,4)));

                        for (int i = edgeStart; i <= edgeEnd; i++) // Increase the edge count for any edges found
                                                                   // overlapping or intersecting the [start,end] range
                                                                   // specified before
                            vertexDegreePerTimeInstance.put("" + i, vertexDegreePerTimeInstance.get("" + i) + 1.0);
                    }
                }

                for (String instance : vertexDegreePerTimeInstance.keySet()) // For each time instance in this vertex's
                                                                             // history add the vertex's degree to the
                                                                             // corresponding overall degree list for
                                                                             // that instance, found in
                                                                             // "allDegreesPerTimeInstance"
                {
                    Double degree = vertexDegreePerTimeInstance.get(instance);
                    HashMap<String, Integer> degreeDistr = results.get(instance);
                    if (degreeDistr == null) {
                        degreeDistr = new HashMap<>();
                    }
                    Integer count = degreeDistr.get(degree.toString());
                    degreeDistr.put(degree.toString(), (count == null) ? 1 : count + 1);
                    results.put(instance, degreeDistr);
                }
            }
        }

        tEnd = System.currentTimeMillis();
        tDelta = tEnd - tStart;
        elapsedSeconds = tDelta / 1000.0;
        System.out.println(
                "Time required for processing and evaluating the DegDistr query: " + elapsedSeconds + " seconds.");
        return results;
    }

    @Override
    public HashMap<String, HashMap<String, Integer>> getDegreeDistributionFetchAllVertices(String first, String last)
    {
        long tStart, tEnd, tDelta;
        HashMap<String, HashMap<String, Integer>> results = new HashMap<>();

        tStart = System.nanoTime();
        FindIterable<Document> dianodes = database.getCollection("dianode").
                find(Filters.or(Filters.gte("_id.end",first) , Filters.lte("_id.start",last )))
                .projection(Projections.include("_id.start", "_id.end","outgoing_edges"))
                .noCursorTimeout(true);
        DiaNode dn;
        for (Document row : dianodes) // For each diachronic node
        {
            dn = new DiaNode(row);
            dn.keepValuesInInterval(first, last);

            String rowend = dn.getEnd().substring(0,4);
            String rowstart = dn.getStart().substring(0,4);
            
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
    public List<String> getOneHopNeighborhood(String vid, String first, String last) {
        long tStart, tEnd, tDelta;
        List<String> results = new ArrayList<>();

        tStart = System.currentTimeMillis();

        Document row = database.getCollection("dianode").find(Filters.eq("_id.vid", vid)).first();

        tEnd = System.currentTimeMillis();
        tDelta = tEnd - tStart;
        double elapsedSeconds = tDelta / 1000.0;
        System.out.println("Time required for retrieving the outgoing edges of a diachronic node: " + elapsedSeconds
                + " seconds, (OneHop on VID, Timestamps: [" + vid + ", " + first + " to " + last + "])");
        if (row == null)
            return Collections.EMPTY_LIST;

        Document edgesUDTList = (Document) row.get("outgoing_edges");
        Map<String, List<Edge>> outgoing_edges = SingleTableModel.convertToEdgeList(edgesUDTList);

        for (String targetID : outgoing_edges.keySet()) {
            List<Edge> edges = outgoing_edges.get(targetID);
            for (Edge edge : edges)
                if ((Integer.parseInt(last) >= Integer.parseInt(edge.start.substring(0,4))
                        && Integer.parseInt(first) < Integer.parseInt(edge.end.substring(0,4)))) {
                    results.add(targetID);
                    break;
                }
        }
        tEnd = System.currentTimeMillis();
        tDelta = tEnd - tStart;
        elapsedSeconds = tDelta / 1000.0;
        System.out.println("Time required for extracting the neighbors of a vertex: " + elapsedSeconds
                + " seconds, (OneHop on VID, Timestamps: [" + vid + ", " + first + " to " + last + "])");
        return results;
    }

    @Override
    public DiaNode getVertexHistory(String vid, String first, String last) {
        // We only expect one row
        Document row = database.getCollection("dianode").find(Filters.eq("_id.vid", vid)).first();
        if (row == null)
            return new DiaNode(vid);

        DiaNode dn = new DiaNode(row);
        dn.keepValuesInInterval(first, last);

        return dn;
    }

    public Vertex getVertexInstance(String vid, String timestamp) {

        Document row = database.getCollection("dianode").find(Filters.eq("_id.vid", vid)).first();
        // We only expect one row

        DiaNode dn = new DiaNode(row);
        Vertex ver = dn.convertToVertex(timestamp);

        return ver;

    }
    /*
     * public void updateVertex(DiaNode ver)
     * {
     * FindIterable<Document> t =
     * database.getCollection("dianode").find(Filters.eq("_id.vid",ver.getVid()));
     * if (!t.cursor().hasNext())
     * {
     * DiaNode current = new DiaNode(t.first());
     * 
     * // Specify the query to find the document you want to update
     * Document query = new Document("_id.vid", ver.getVid());
     * // Iterate through each key in the map
     * 
     * for (String attr : ver.getAttributes().keySet()) {
     * 
     * List<Interval> tempAttr = current.getAttributes().get(attr);
     * 
     * // If you want to access the associated list of intervals
     * List<Interval> intervals = ver.getAttributes().get(attr);
     * for (Interval interval : intervals) {
     * tempAttr.add(interval.toDoc());
     * }
     * // Specify the update operation
     * Document update = new Document("$set", new Document(attr, tempAttr));
     * database.getCollection("dianode").withWriteConcern(WriteConcern.MAJORITY).
     * updateOne(query, update);
     * }
     * }
     * 
     * //*
     * ArrayList<Document> name = new ArrayList<>();
     * for (Interval interval : ver.getAttributes().get("name"))
     * {
     * name.add(interval.toDoc());
     * }
     * ArrayList<Document> color = new ArrayList<>();
     * for (Interval interval : ver.getAttributes().get("color"))
     * {
     * name.add(interval.toDoc());
     * }
     * Document doc = new Document("_id", new Document().append("vid",ver.getVid())
     * .append("start", ver.getStart())
     * .append("end", ver.getEnd()))
     * .append("name", name )
     * .append("color", color);
     * 
     * 
     * 
     * }
     */
@Override
    public void insertAttribute(String id, String attribute, Interval interv, String label) {
        FindIterable<Document> t = database.getCollection("dianode").find(Filters.eq("_id.vid", id));
        if (t.cursor().hasNext()) {
            Document current = t.first();

            // Specify the query to find the document you want to update
            Document query = new Document("_id.vid", id);
            // Iterate through each key in the map

            List<Document> tempAttr = current.getList(attribute, Document.class);
            // List<Interval> tempAttr = current.getAttributes().get(attr);

            // if it adds new Interval we need to set the last one, if it is end
            if (tempAttr != null) {
                Document temp = tempAttr.get(tempAttr.size() - 1);
                temp.put("end", interv.start);
            }else{
                tempAttr = new ArrayList<Document>();
            }

            tempAttr.add(interv.toDoc());
            // Specify the update operation
            Document update = new Document("$set", new Document(attribute, tempAttr));
            database.getCollection("dianode").withWriteConcern(WriteConcern.MAJORITY).updateOne(query, update);
        }

    }

    @Override
    public void deleteAttribute(String id, String attribute, String end, String label) {
        FindIterable<Document> t = database.getCollection("dianode").find(Filters.eq("_id.vid", id));
        if (t.cursor().hasNext()) {
            Document current = t.first();

            // Specify the query to find the document you want to update
            Document query = new Document("_id.vid", id);
            // Iterate through each key in the map

            List<Document> tempAttr = current.getList(attribute, Document.class);
            // List<Interval> tempAttr = current.getAttributes().get(attr);

            // if it adds new Interval we need to set the last one, if it is end
            if (tempAttr != null) {
                Document temp = tempAttr.get(tempAttr.size() - 1);
                temp.put("end", end);
            
                // Specify the update operation
                Document update = new Document("$set", new Document(attribute, tempAttr));
                database.getCollection("dianode").withWriteConcern(WriteConcern.MAJORITY).updateOne(query, update);
            }
        }

    }

    @Override
    public void insertEdge(String sourceID, String targetID, String start, String label, String weight) {
        // First, insert the edge as an outgoing edge
        Document vertexRow = database.getCollection("dianode").find(Filters.eq("_id.vid", sourceID)).first();
        Document id = (Document) vertexRow.get("_id");
        Document edgesUDTList = (Document) vertexRow.get("outgoing_edges");

        Map<String, List<Edge>> edges = convertToEdgeList(edgesUDTList);
        List<Document> out_edges = new ArrayList<>();

        if (edges.isEmpty()) {
            out_edges.add(new Edge(label, weight, targetID, start, "2099").toDoc());
            database.getCollection("dianode").withWriteConcern(WriteConcern.MAJORITY).findOneAndUpdate(
                    Filters.and(
                            Filters.and(
                                    Filters.eq("_id.vid", sourceID), Filters.eq("_id.start", id.getString("start"))),
                            Filters.eq("_id.end", id.getString("end"))),
                    Updates.set("outgoing_edges", new Document().append(targetID, out_edges)));

        } else {
            List<Edge> vertex_edges = new ArrayList<>();

            // if an edge already exist from source to target we might need to change its
            // end depending on the semantics
            if (edges.get(targetID) != null) {
                vertex_edges = edges.get(targetID);
                // Edge lastEdge = vertex_edges.get(vertex_edges.size()-1);
                // lastEdge.end = start;
                // out_edges.add(lastEdge.toDoc());
            }
            for (Edge e : vertex_edges) {
                out_edges.add(e.toDoc());
            }
            out_edges.add(new Edge(label, weight, targetID, start, "2099").toDoc());
            edgesUDTList.put(targetID, out_edges);

            database.getCollection("dianode").findOneAndUpdate(
                    Filters.and(
                            Filters.and(
                                    Filters.eq("_id.vid", sourceID), Filters.eq("_id.start", id.getString("start"))),
                            Filters.eq("_id.end", id.getString("end"))),
                    Updates.set("outgoing_edges", edgesUDTList));
        }

        // Then, insert the edge as an incoming edge
        vertexRow = database.getCollection("dianode").find(Filters.eq("_id.vid", targetID)).first();
        if (vertexRow != null)
            edgesUDTList = (Document) vertexRow.get("incoming_edges");
        else
            edgesUDTList = null;
        edges = convertToEdgeList(edgesUDTList);

        List<Document> in_edges = new ArrayList<>();

        if (edges.isEmpty()) {
            in_edges.add(new Edge(label, weight, targetID, start, "2099").toDoc());
            database.getCollection("dianode").withWriteConcern(WriteConcern.MAJORITY).findOneAndUpdate(
                    Filters.and(
                            Filters.and(
                                    Filters.eq("_id.vid", targetID), Filters.eq("_id.start", id.getString("start"))),
                            Filters.eq("_id.end", id.getString("end"))),
                    Updates.set("incoming_edges", new Document().append(sourceID, in_edges)));
        } else {
            List<Edge> vertex_edges = new ArrayList<Edge>();

            if (edges.get(sourceID) != null) {
                vertex_edges = edges.get(sourceID);
                // Edge lastEdge = vertex_edges.get(vertex_edges.size() - 1);
                // lastEdge.end = start;
                // in_edges.add(lastEdge.toDoc());
            }
            for (Edge e : vertex_edges) {
                in_edges.add(e.toDoc());
            }
            in_edges.add(new Edge(label, weight, targetID, start, "2099").toDoc());

            edgesUDTList.put(sourceID, in_edges);

            database.getCollection("dianode").withWriteConcern(WriteConcern.MAJORITY).findOneAndUpdate(
                    Filters.and(
                            Filters.and(
                                    Filters.eq("_id.vid", targetID), Filters.eq("_id.start", id.getString("start"))),
                            Filters.eq("_id.end", id.getString("end"))),
                    Updates.set("incoming_edges", edgesUDTList));

        }
    }

    @Override
    public void deleteEdge(String sourceID, String targetID, String endTime, String label) {
        // First, insert the edge as an outgoing edge
        Document vertexRow = database.getCollection("dianode").find(Filters.eq("_id.vid", sourceID)).first();
        Document id = (Document) vertexRow.get("_id");
        Document edgesUDTList = (Document) vertexRow.get("outgoing_edges");

        Map<String, List<Edge>> edges = convertToEdgeList(edgesUDTList);
        List<Document> out_edges = new ArrayList<>();

        if (edges.isEmpty()) {
            // out_edges.add(new Edge(label, weight, targetID, start, end).toDoc());
            /*
             * database.getCollection("dianode").withWriteConcern(WriteConcern.MAJORITY).
             * findOneAndUpdate(
             * Filters.and(
             * Filters.and(
             * Filters.eq("_id.vid",sourceID),Filters.eq("_id.start",id.getString("start")))
             * ,
             * Filters.eq("_id.end",id.getString("end") ))
             * , Updates.set("outgoing_edges", new Document().append(targetID,out_edges)));
             */
        } else {
            List<Edge> vertex_edges = new ArrayList<Edge>();

            if (edges.get(targetID) != null) {
                vertex_edges = edges.get(targetID);
                for (Edge e : vertex_edges) {
                    if (e.label == label ) {
                        e.end = endTime;
                        break;
                    }
                    out_edges.add(e.toDoc());
                }

                // out_edges.add(lastEdge.toDoc());
            }

            edgesUDTList.put(targetID, out_edges);

            database.getCollection("dianode").findOneAndUpdate(
                    Filters.and(
                            Filters.and(
                                    Filters.eq("_id.vid", sourceID), Filters.eq("_id.start", id.getString("start"))),
                            Filters.eq("_id.end", id.getString("end"))),
                    Updates.set("outgoing_edges", edgesUDTList));
        }

        // Then, insert the edge as an incoming edge
        vertexRow = database.getCollection("dianode").find(Filters.eq("_id.vid", targetID)).first();
        if (vertexRow != null)
            edgesUDTList = (Document) vertexRow.get("incoming_edges");
        else
            edgesUDTList = null;
        edges = convertToEdgeList(edgesUDTList);

        List<Document> in_edges = new ArrayList<>();

        if (edges.isEmpty()) {
            /*
             * in_edges.add(new Edge(label, weight, targetID, start, end).toDoc());
             * database.getCollection("dianode").withWriteConcern(WriteConcern.MAJORITY).
             * findOneAndUpdate(
             * Filters.and(
             * Filters.and(
             * Filters.eq("_id.vid",targetID),Filters.eq("_id.start",id.getString("start")))
             * ,
             * Filters.eq("_id.end",id.getString("end") ))
             * , Updates.set("incoming_edges", new Document().append(sourceID,in_edges)));
             */
        } else {
            List<Edge> vertex_edges = new ArrayList<Edge>();

            if (edges.get(sourceID) != null) {
                for (Edge e : vertex_edges) {
                    if (e.label == label) {
                        e.end = endTime;
                        break;
                    }
                    in_edges.add(e.toDoc());
                }
            }

            edgesUDTList.put(sourceID, in_edges);

            database.getCollection("dianode").withWriteConcern(WriteConcern.MAJORITY).findOneAndUpdate(
                    Filters.and(
                            Filters.and(
                                    Filters.eq("_id.vid", targetID), Filters.eq("_id.start", id.getString("start"))),
                            Filters.eq("_id.end", id.getString("end"))),
                    Updates.set("incoming_edges", edgesUDTList));

        }
    }

    @Override
    public void insertVertex(String vid, String start, String label) {
        // DiaNode ver = new DiaNode(vid, start, end);

        FindIterable<Document> t = database.getCollection("dianode").find(Filters.eq("_id.vid", vid));
        if (!t.cursor().hasNext()) {

            Document doc = new Document("_id", new Document().append("vid", vid)
                    .append("start", start)
                    .append("end", "2099"));
            database.getCollection("dianode").withWriteConcern(WriteConcern.MAJORITY).insertOne(doc);

        }

    }

    @Override
    public void deleteVertex(String vid, String end, String label) {
        // DiaNode ver = new DiaNode(vid, start, end);

        FindIterable<Document> t = database.getCollection("dianode").find(Filters.eq("_id.vid", vid));
        if (t.cursor().hasNext()) {
            database.getCollection("dianode").findOneAndUpdate(
                    Filters.eq("_id.vid", vid), Updates.set("end", end));

        }
        // either then we change all attributes end, or we don't care and check first
        // for vid end

    }

    public void parseInput(String input) {
        try {
            //int snap_count = DataModel.getCountOfSnapshotsInInput(input);

            BufferedReader file = new BufferedReader(new FileReader(input));
            String line;
            String[] tokens;
            int verKcounter = 0;
            int edgeKcounter = 0;

            while ((line = file.readLine()) != null) {
                if (line.startsWith("vertex")) {
                    tokens = line.split(" ");
                    
                    if (tokens.length != 5)
                    {
                        System.out.println("Wrong number of attributes (insert vertex)");
                        System.out.println(line);
                        break;
                    }
                    
                    String verID = tokens[1];
                    String label = tokens[2];
                    String start = tokens[4];

                    insertVertex(verID, start, label);
                    verKcounter++;
                    if (verKcounter % 1000 == 0)
                        System.out.println("Vertices processed: " + verKcounter);
                }else if (line.startsWith("delete vertex")) {
                    tokens = line.split(" ");
                    
                    if (tokens.length != 5)
                    {
                        System.out.println("Wrong number of attributes (delete vertex)");
                        System.out.println(line);
                        break;
                    }
                    String verID = tokens[2];
                    String label = tokens[3];
                    String end = tokens[4];
                    
                    deleteVertex(verID, end, label);
                    verKcounter++;
                    if (verKcounter % 1000 == 0)
                        System.out.println("Vertices processed: " + verKcounter);
                } else if (line.startsWith("edge")) {
                    tokens = line.split(" ");
                    if (tokens.length != 6)
                    {
                        System.out.println("Wrong number of attributes (insert edge)");
                        System.out.println(line);
                        break;
                    }
                    String label = tokens[1];
                    String sourceID = tokens[2];
                    String targetID = tokens[3];
                    String weight = "1";
                    String start = tokens[5];
                    
                    insertEdge(sourceID, targetID, start, label, weight);
                    edgeKcounter++;
                    if (edgeKcounter % 1000 == 0)
                        System.out.println("Edges processed: " + edgeKcounter);
                }else if (line.startsWith("delete edge")) {
                    tokens = line.split(" ");
                    if (tokens.length != 6)
                    {
                        System.out.println("Wrong number of attributes (delete edge)");
                        System.out.println(line);
                        break;
                    }
                    String label = tokens[2];
                    String sourceID = tokens[3];
                    String targetID = tokens[4];
                    String end = tokens[5];
                    
                    deleteEdge(sourceID, targetID, end, label);
                    edgeKcounter++;
                    if (edgeKcounter % 1000 == 0)
                        System.out.println("Edges processed: " + edgeKcounter);
                } else if (line.startsWith("Add attribute")) {
                    tokens = line.split(" ");
                    
                    if (tokens.length < 7)
                    {
                        System.out.println("Wrong number of attributes (add attribute)");
                        System.out.println(line);
                        break;
                    }
                    
                    String verID = tokens[2];
                    String label = tokens[3];
                    String attr = tokens[4];
                    String attrV = String.join(",", Arrays.copyOfRange(tokens, 5, tokens.length - 1));
                    String start = tokens[tokens.length -1];
                    
                    Interval temp = new Interval(attrV, start, "2099");
                    insertAttribute(verID, attr, temp, label);
                    verKcounter++;
                    if (verKcounter % 1000 == 0)
                        System.out.println("Vertices processed: " + verKcounter);
                }
                else if (line.startsWith("Delete attribute")) {
                    tokens = line.split(" ");
                    
                    if (tokens.length != 7)
                    {
                        System.out.println("Wrong number of attributes (delete attribute)");
                        System.out.println(line);
                        break;
                    }
                    
                    String verID = tokens[2];
                    String label = tokens[3];
                    String attr = tokens[4];
                    String end = tokens[5];

                    deleteAttribute(verID, attr, end, label);
                    verKcounter++;
                    if (verKcounter % 1000 == 0)
                        System.out.println("Vertices processed: " + verKcounter);
                }
            }
            file.close();
        } catch (IOException ex) {
            Logger.getLogger(SingleTableModel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // NOT YET IMPLEMENTED
    @Override
    public void updateVertexAttribute(String vid, String attrName, String attrValue, String timestamp) {

        Document vertexRow = database.getCollection("dianode").find(Filters.eq("_id.vid", vid)).first();
        List<Document> attrUDTList = vertexRow.getList(attrName, Document.class);
        List<Interval> attrList = convertToIntervals(attrUDTList);

        Interval toBeDeleted = attrList.get(attrList.size() - 1);

        /*
         * // Remove the old interval
         * session.execute("UPDATE "+keyspace+".dianode SET " + attrName + " = " +
         * attrName + " - ["
         * + toBeDeleted
         * + "] WHERE vid = '" + vid + "' AND start = '" + vertexRow.getString("start")
         * + "' AND end = '" + vertexRow.getString("end") + "';");
         * 
         * // Add the updated old interval and the new interval
         * session.execute("UPDATE "+keyspace+".dianode SET " + attrName + " = " +
         * attrName + " + ["
         * + new Interval(toBeDeleted.value, toBeDeleted.start, timestamp)
         * + "] WHERE vid = '" + vid + "' AND start = '" + vertexRow.getString("start")
         * + "' AND end = '" + vertexRow.getString("end") + "';");
         * 
         * session.execute("UPDATE "+keyspace+".dianode SET " + attrName + " = " +
         * attrName + " + ["
         * + new Interval(attrValue,timestamp,"Infinity")
         * + "] WHERE vid = '" + vid + "' AND start = '" + vertexRow.getString("start")
         * + "' AND end = '" + vertexRow.getString("end") + "';");
         */
    }

    @Override
    public void useKeyspace() {
    }

}
