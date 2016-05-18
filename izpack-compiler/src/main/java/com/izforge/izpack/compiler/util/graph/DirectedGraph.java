package com.izforge.izpack.compiler.util.graph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Generic directed graph.
 * @param <Vertex> the generic type of vertexes
 */
public class DirectedGraph<Vertex>
{

    private Map<Vertex, Set<Vertex>> st;

    /**
     * Create an empty directed graph.
     */
    public DirectedGraph()
    {
        st = new HashMap<Vertex, Set<Vertex>>();
    }

    /**
     * Add vertex to to from's list of neighbors; self-loops allowed
     * @param from source vertex
     * @param to target vertex
     */
    public void addEdge(Vertex from, Vertex to)
    {
        if (!st.containsKey(from)) addVertex(from);
        if (!st.containsKey(to)) addVertex(to);
        st.get(from).add(to);
    }

    /**
     * Add a new vertex with no neighbors if vertex does not yet exist
     * @param v vertex to be added
     */
    public void addVertex(Vertex v)
    {
        if (!st.containsKey(v)) st.put(v, new HashSet<Vertex>());
    }

    /**
     * Return an iterator of vertices incident to a given vertex (neighbours).
     * @return the iterator of vertices
     */
    public Iterable<Vertex> adjacentTo(Vertex v)
    {
        if (!st.containsKey(v)) return new HashSet<Vertex>();
        else return st.get(v);
    }

    @Override
    public String toString()
    {
        StringBuilder s = new StringBuilder();
        for (Vertex v : st.keySet())
        {
            s.append(v + ": ");
            for (Vertex w : st.get(v))
            {
                s.append(w + " ");
            }
            s.append("\n");
        }
        return s.toString();
    }
}
