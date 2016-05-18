package com.izforge.izpack.compiler.util.graph;

import java.util.*;

/**
 * Iterator for a breadth-first (BFS) search in a graph.
 * @param <Vertex> the generic type of vertexes
 */
public class BreadthFirstIterator<Vertex> implements Iterator<Vertex>
{
    private Set<Vertex> visited = new HashSet<Vertex>();
    private Queue<Vertex> queue = new LinkedList<Vertex>();
    private DirectedGraph<Vertex> graph;

    public BreadthFirstIterator(DirectedGraph<Vertex> g, Vertex startingVertex)
    {
        this.graph = g;
        this.queue.add(startingVertex);
        this.visited.add(startingVertex);
    }

    @Override
    public void remove()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasNext()
    {
        return !this.queue.isEmpty();
    }

    @Override
    public Vertex next()
    {
        Vertex next = queue.remove();
        for (Vertex neighbor : this.graph.adjacentTo(next))
        {
            if (!this.visited.contains(neighbor))
            {
                this.queue.add(neighbor);
                this.visited.add(neighbor);
            }
        }
        return next;
    }
}
