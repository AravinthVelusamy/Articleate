package website.jonreynolds.jreynolds.articleate;

import java.util.Map;

/**
 * Created by jonathanreynolds on 3/17/16.
 */
public class TextGraph {
    String[] nodes;
    int[][] edges;

    /**
     * Size-based constructor for graph
     * @param size number of nodes in graph
     */
    public TextGraph(int size){
        nodes = new String[size];
        edges = new int[size][size];
    }

    /**
     * Constructor from array of Strings, each String representing
     * a node's data in the graph.
     * @param strings
     */
    public TextGraph(String[] strings){
        nodes = strings;
        int size = strings.length;
        edges = new int[size][size];
    }

    /**
     * Add a weighted directed edge to the graph
     * @param from beginning node
     * @param to end node
     * @param weight edge weight
     */
    public void addEdge(int from, int to, int weight){
        edges[from][to] = weight;
    }

    /**
     * Returns the edge weight between two nodes
     * @param from starting node
     * @param to ending node
     * @return edge weight between them, if any. If not, -1.
     */
    public int getEdge(int from, int to) {
        return edges[from][to];
    }

    /**
     * Returns edges emitting from a certain vertex
     * @param from the starting vertex
     * @return an array of ints representing edge weights.
     */
    public int[] getEdges(int from){
        return edges[from];
    }

    /**
     * Sets the data of a node to the passed value
     * @param index the index of the node
     */
    public void setNode(int index, String s){
        nodes[index] = s;
    }

}
