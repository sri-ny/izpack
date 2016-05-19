/*
 * IzPack - Copyright 2001-2016 The IzPack project team.
 * All Rights Reserved.
 *
 * http://izpack.org/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
