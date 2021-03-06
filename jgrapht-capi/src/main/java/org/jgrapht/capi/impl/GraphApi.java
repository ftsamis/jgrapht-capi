/*
 * (C) Copyright 2020, by Dimitrios Michail.
 *
 * JGraphT C-API
 *
 * See the CONTRIBUTORS.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the
 * GNU Lesser General Public License v2.1 or later
 * which is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1-standalone.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR LGPL-2.1-or-later
 */
package org.jgrapht.capi.impl;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.graalvm.nativeimage.IsolateThread;
import org.graalvm.nativeimage.ObjectHandle;
import org.graalvm.nativeimage.ObjectHandles;
import org.graalvm.nativeimage.c.function.CEntryPoint;
import org.graalvm.nativeimage.c.type.CDoublePointer;
import org.graalvm.nativeimage.c.type.CIntPointer;
import org.graalvm.nativeimage.c.type.WordPointer;
import org.jgrapht.Graph;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.alg.util.Triple;
import org.jgrapht.capi.Constants;
import org.jgrapht.capi.JGraphTContext.Status;
import org.jgrapht.capi.error.StatusReturnExceptionHandler;
import org.jgrapht.capi.graph.SafeEdgeSupplier;
import org.jgrapht.capi.graph.SafeVertexSupplier;
import org.jgrapht.graph.AsUndirectedGraph;
import org.jgrapht.graph.AsUnmodifiableGraph;
import org.jgrapht.graph.AsUnweightedGraph;
import org.jgrapht.graph.EdgeReversedGraph;
import org.jgrapht.graph.builder.GraphTypeBuilder;
import org.jgrapht.opt.graph.sparse.SparseIntDirectedGraph;
import org.jgrapht.opt.graph.sparse.SparseIntDirectedWeightedGraph;
import org.jgrapht.opt.graph.sparse.SparseIntUndirectedGraph;
import org.jgrapht.opt.graph.sparse.SparseIntUndirectedWeightedGraph;

/**
 * Basic graph operations
 */
public class GraphApi {

	private static ObjectHandles globalHandles = ObjectHandles.getGlobal();

	/**
	 * Create a graph and return its handle.
	 *
	 * @param thread the thread isolate
	 * @return the graph handle
	 */
	@CEntryPoint(name = Constants.LIB_PREFIX + "graph_create", exceptionHandler = StatusReturnExceptionHandler.class)
	public static int createGraph(IsolateThread thread, boolean directed, boolean allowingSelfLoops,
			boolean allowingMultipleEdges, boolean weighted, WordPointer res) {

		SafeVertexSupplier vSupplier = new SafeVertexSupplier();
		SafeEdgeSupplier eSupplier = new SafeEdgeSupplier();

		Graph<Integer, Integer> graph;
		if (directed) {
			graph = GraphTypeBuilder.directed().weighted(weighted).allowingMultipleEdges(allowingMultipleEdges)
					.allowingSelfLoops(allowingSelfLoops).vertexSupplier(vSupplier).edgeSupplier(eSupplier)
					.buildGraph();
		} else {
			graph = GraphTypeBuilder.undirected().weighted(weighted).allowingMultipleEdges(allowingMultipleEdges)
					.allowingSelfLoops(allowingSelfLoops).vertexSupplier(vSupplier).edgeSupplier(eSupplier)
					.buildGraph();
		}

		vSupplier.setGraph(graph);
		eSupplier.setGraph(graph);

		if (res.isNonNull()) {
			res.write(globalHandles.create(graph));
		}
		return Status.STATUS_SUCCESS.getCValue();
	}

	/**
	 * Create a graph and return its handle.
	 *
	 * @param thread the thread isolate
	 * @return the graph handle
	 */
	@CEntryPoint(name = Constants.LIB_PREFIX
			+ "graph_sparse_create", exceptionHandler = StatusReturnExceptionHandler.class)
	public static int createSparseGraph(IsolateThread thread, boolean directed, boolean weighted, int numVertices,
			ObjectHandle edgesListHandle, WordPointer res) {
		Graph<Integer, Integer> graph;
		if (weighted) {
			List<Triple<Integer, Integer, Double>> edges = globalHandles.get(edgesListHandle);
			if (directed) {
				graph = new SparseIntDirectedWeightedGraph(numVertices, edges);
			} else {
				graph = new SparseIntUndirectedWeightedGraph(numVertices, edges);
			}
		} else {
			List<Pair<Integer, Integer>> edges = globalHandles.get(edgesListHandle);
			if (directed) {
				graph = new SparseIntDirectedGraph(numVertices, edges);
			} else {
				graph = new SparseIntUndirectedGraph(numVertices, edges);
			}
		}
		if (res.isNonNull()) {
			res.write(globalHandles.create(graph));
		}
		return Status.STATUS_SUCCESS.getCValue();
	}

	@CEntryPoint(name = Constants.LIB_PREFIX
			+ "graph_vertices_count", exceptionHandler = StatusReturnExceptionHandler.class)
	public static int verticesCount(IsolateThread thread, ObjectHandle graphHandle, CIntPointer res) {
		Graph<Integer, Integer> g = globalHandles.get(graphHandle);
		int result = g.vertexSet().size();
		if (res.isNonNull()) {
			res.write(result);
		}
		return Status.STATUS_SUCCESS.getCValue();
	}

	@CEntryPoint(name = Constants.LIB_PREFIX
			+ "graph_edges_count", exceptionHandler = StatusReturnExceptionHandler.class)
	public static int edgesCount(IsolateThread thread, ObjectHandle graphHandle, CIntPointer res) {
		Graph<Integer, Integer> g = globalHandles.get(graphHandle);
		int result = g.edgeSet().size();
		if (res.isNonNull()) {
			res.write(result);
		}
		return Status.STATUS_SUCCESS.getCValue();
	}

	@CEntryPoint(name = Constants.LIB_PREFIX
			+ "graph_add_vertex", exceptionHandler = StatusReturnExceptionHandler.class)
	public static int addVertex(IsolateThread thread, ObjectHandle graphHandle, CIntPointer res) {
		Graph<Integer, Integer> g = globalHandles.get(graphHandle);
		int result = g.addVertex();
		if (res.isNonNull()) {
			res.write(result);
		}
		return Status.STATUS_SUCCESS.getCValue();
	}

	@CEntryPoint(name = Constants.LIB_PREFIX
			+ "graph_add_given_vertex", exceptionHandler = StatusReturnExceptionHandler.class)
	public static int addGivenVertex(IsolateThread thread, ObjectHandle graphHandle, int vertex, CIntPointer res) {
		Graph<Integer, Integer> g = globalHandles.get(graphHandle);
		boolean result = g.addVertex(vertex);
		if (res.isNonNull()) {
			res.write(result ? 1 : 0);
		}
		return Status.STATUS_SUCCESS.getCValue();
	}

	@CEntryPoint(name = Constants.LIB_PREFIX
			+ "graph_remove_vertex", exceptionHandler = StatusReturnExceptionHandler.class)
	public static int removeVertex(IsolateThread thread, ObjectHandle graphHandle, int vertex, CIntPointer res) {
		Graph<Integer, Integer> g = globalHandles.get(graphHandle);
		boolean result = g.removeVertex(vertex);
		if (res.isNonNull()) {
			res.write(result ? 1 : 0);
		}
		return Status.STATUS_SUCCESS.getCValue();
	}

	@CEntryPoint(name = Constants.LIB_PREFIX
			+ "graph_contains_vertex", exceptionHandler = StatusReturnExceptionHandler.class)
	public static int containsVertex(IsolateThread thread, ObjectHandle graphHandle, int vertex, CIntPointer res) {
		Graph<Integer, Integer> g = globalHandles.get(graphHandle);
		boolean result = g.containsVertex(vertex);
		if (res.isNonNull()) {
			res.write(result ? 1 : 0);
		}
		return Status.STATUS_SUCCESS.getCValue();
	}

	@CEntryPoint(name = Constants.LIB_PREFIX + "graph_add_edge", exceptionHandler = StatusReturnExceptionHandler.class)
	public static int addEdge(IsolateThread thread, ObjectHandle graphHandle, int source, int target, CIntPointer res) {
		Graph<Integer, Integer> g = globalHandles.get(graphHandle);
		Integer result = g.addEdge(source, target);
		if (result == null) {
			throw new IllegalArgumentException("Graph does not allow multiple edges");
		}
		if (res.isNonNull()) {
			res.write(result);
		}
		return Status.STATUS_SUCCESS.getCValue();
	}

	@CEntryPoint(name = Constants.LIB_PREFIX
			+ "graph_add_given_edge", exceptionHandler = StatusReturnExceptionHandler.class)
	public static int addGivenEdge(IsolateThread thread, ObjectHandle graphHandle, int source, int target, int edge,
			CIntPointer res) {
		Graph<Integer, Integer> g = globalHandles.get(graphHandle);
		boolean result = g.addEdge(source, target, edge);
		if (res.isNonNull()) {
			res.write(result ? 1 : 0);
		}
		return Status.STATUS_SUCCESS.getCValue();
	}

	@CEntryPoint(name = Constants.LIB_PREFIX
			+ "graph_remove_edge", exceptionHandler = StatusReturnExceptionHandler.class)
	public static int removeEdge(IsolateThread thread, ObjectHandle graphHandle, int edge, CIntPointer res) {
		Graph<Integer, Integer> g = globalHandles.get(graphHandle);
		boolean result = g.removeEdge(edge);
		if (res.isNonNull()) {
			res.write(result ? 1 : 0);
		}
		return Status.STATUS_SUCCESS.getCValue();
	}

	@CEntryPoint(name = Constants.LIB_PREFIX
			+ "graph_contains_edge", exceptionHandler = StatusReturnExceptionHandler.class)
	public static int containsEdge(IsolateThread thread, ObjectHandle graphHandle, int edge, CIntPointer res) {
		Graph<Integer, Integer> g = globalHandles.get(graphHandle);
		boolean result = g.containsEdge(edge);
		if (res.isNonNull()) {
			res.write(result ? 1 : 0);
		}
		return Status.STATUS_SUCCESS.getCValue();
	}

	@CEntryPoint(name = Constants.LIB_PREFIX
			+ "graph_contains_edge_between", exceptionHandler = StatusReturnExceptionHandler.class)
	public static int containsEdgeBetween(IsolateThread thread, ObjectHandle graphHandle, int source, int target,
			CIntPointer res) {
		Graph<Integer, Integer> g = globalHandles.get(graphHandle);
		boolean result = g.containsEdge(source, target);
		if (res.isNonNull()) {
			res.write(result ? 1 : 0);
		}
		return Status.STATUS_SUCCESS.getCValue();
	}

	@CEntryPoint(name = Constants.LIB_PREFIX + "graph_degree_of", exceptionHandler = StatusReturnExceptionHandler.class)
	public static int degreeOf(IsolateThread thread, ObjectHandle graphHandle, int vertex, CIntPointer res) {
		Graph<Integer, Integer> g = globalHandles.get(graphHandle);
		int result = g.degreeOf(vertex);
		if (res.isNonNull()) {
			res.write(result);
		}
		return Status.STATUS_SUCCESS.getCValue();
	}

	@CEntryPoint(name = Constants.LIB_PREFIX
			+ "graph_indegree_of", exceptionHandler = StatusReturnExceptionHandler.class)
	public static int inDegreeOf(IsolateThread thread, ObjectHandle graphHandle, int vertex, CIntPointer res) {
		Graph<Integer, Integer> g = globalHandles.get(graphHandle);
		int result = g.inDegreeOf(vertex);
		if (res.isNonNull()) {
			res.write(result);
		}
		return Status.STATUS_SUCCESS.getCValue();
	}

	@CEntryPoint(name = Constants.LIB_PREFIX
			+ "graph_outdegree_of", exceptionHandler = StatusReturnExceptionHandler.class)
	public static int outDegreeOf(IsolateThread thread, ObjectHandle graphHandle, int vertex, CIntPointer res) {
		Graph<Integer, Integer> g = globalHandles.get(graphHandle);
		int result = g.outDegreeOf(vertex);
		if (res.isNonNull()) {
			res.write(result);
		}
		return Status.STATUS_SUCCESS.getCValue();
	}

	@CEntryPoint(name = Constants.LIB_PREFIX
			+ "graph_edge_source", exceptionHandler = StatusReturnExceptionHandler.class)
	public static int edgeSource(IsolateThread thread, ObjectHandle graphHandle, int edge, CIntPointer res) {
		Graph<Integer, Integer> g = globalHandles.get(graphHandle);
		int result = g.getEdgeSource(edge);
		if (res.isNonNull()) {
			res.write(result);
		}
		return Status.STATUS_SUCCESS.getCValue();
	}

	@CEntryPoint(name = Constants.LIB_PREFIX
			+ "graph_edge_target", exceptionHandler = StatusReturnExceptionHandler.class)
	public static int edgeTarget(IsolateThread thread, ObjectHandle graphHandle, int edge, CIntPointer res) {
		Graph<Integer, Integer> g = globalHandles.get(graphHandle);
		int result = g.getEdgeTarget(edge);
		if (res.isNonNull()) {
			res.write(result);
		}
		return Status.STATUS_SUCCESS.getCValue();
	}

	@CEntryPoint(name = Constants.LIB_PREFIX
			+ "graph_is_weighted", exceptionHandler = StatusReturnExceptionHandler.class)
	public static int isWeighted(IsolateThread thread, ObjectHandle graphHandle, CIntPointer res) {
		Graph<Integer, Integer> g = globalHandles.get(graphHandle);
		boolean result = g.getType().isWeighted();
		if (res.isNonNull()) {
			res.write(result ? 1 : 0);
		}
		return Status.STATUS_SUCCESS.getCValue();
	}

	@CEntryPoint(name = Constants.LIB_PREFIX
			+ "graph_is_directed", exceptionHandler = StatusReturnExceptionHandler.class)
	public static int isDirected(IsolateThread thread, ObjectHandle graphHandle, CIntPointer res) {
		Graph<Integer, Integer> g = globalHandles.get(graphHandle);
		boolean result = g.getType().isDirected();
		if (res.isNonNull()) {
			res.write(result ? 1 : 0);
		}
		return Status.STATUS_SUCCESS.getCValue();
	}

	@CEntryPoint(name = Constants.LIB_PREFIX
			+ "graph_is_undirected", exceptionHandler = StatusReturnExceptionHandler.class)
	public static int isUndirected(IsolateThread thread, ObjectHandle graphHandle, CIntPointer res) {
		Graph<Integer, Integer> g = globalHandles.get(graphHandle);
		boolean result = g.getType().isUndirected();
		if (res.isNonNull()) {
			res.write(result ? 1 : 0);
		}
		return Status.STATUS_SUCCESS.getCValue();
	}

	@CEntryPoint(name = Constants.LIB_PREFIX
			+ "graph_is_allowing_selfloops", exceptionHandler = StatusReturnExceptionHandler.class)
	public static int allowSelfLoops(IsolateThread thread, ObjectHandle graphHandle, CIntPointer res) {
		Graph<Integer, Integer> g = globalHandles.get(graphHandle);
		boolean result = g.getType().isAllowingSelfLoops();
		if (res.isNonNull()) {
			res.write(result ? 1 : 0);
		}
		return Status.STATUS_SUCCESS.getCValue();
	}

	@CEntryPoint(name = Constants.LIB_PREFIX
			+ "graph_is_allowing_multipleedges", exceptionHandler = StatusReturnExceptionHandler.class)
	public static int allowMultipleEdges(IsolateThread thread, ObjectHandle graphHandle, CIntPointer res) {
		Graph<Integer, Integer> g = globalHandles.get(graphHandle);
		boolean result = g.getType().isAllowingMultipleEdges();
		if (res.isNonNull()) {
			res.write(result ? 1 : 0);
		}
		return Status.STATUS_SUCCESS.getCValue();
	}

	@CEntryPoint(name = Constants.LIB_PREFIX
			+ "graph_get_edge_weight", exceptionHandler = StatusReturnExceptionHandler.class)
	public static int getEdgeWeight(IsolateThread thread, ObjectHandle graphHandle, int edge, CDoublePointer res) {
		Graph<Integer, Integer> g = globalHandles.get(graphHandle);
		double result = g.getEdgeWeight(edge);
		if (res.isNonNull()) {
			res.write(result);
		}
		return Status.STATUS_SUCCESS.getCValue();
	}

	@CEntryPoint(name = Constants.LIB_PREFIX
			+ "graph_set_edge_weight", exceptionHandler = StatusReturnExceptionHandler.class)
	public static int setEdgeWeight(IsolateThread thread, ObjectHandle graphHandle, int edge, double weight) {
		Graph<Integer, Integer> g = globalHandles.get(graphHandle);
		g.setEdgeWeight(edge, weight);
		return Status.STATUS_SUCCESS.getCValue();
	}

	@CEntryPoint(name = Constants.LIB_PREFIX
			+ "graph_create_all_vit", exceptionHandler = StatusReturnExceptionHandler.class)
	public static int createAllVerticesIterator(IsolateThread thread, ObjectHandle graphHandle, WordPointer res) {
		Graph<Integer, Integer> g = globalHandles.get(graphHandle);
		Iterator<Integer> it = g.vertexSet().iterator();
		if (res.isNonNull()) {
			res.write(globalHandles.create(it));
		}
		return Status.STATUS_SUCCESS.getCValue();
	}

	@CEntryPoint(name = Constants.LIB_PREFIX
			+ "graph_create_all_eit", exceptionHandler = StatusReturnExceptionHandler.class)
	public static int createAllEdgesIterator(IsolateThread thread, ObjectHandle graphHandle, WordPointer res) {
		Graph<Integer, Integer> g = globalHandles.get(graphHandle);
		Iterator<Integer> it = g.edgeSet().iterator();
		if (res.isNonNull()) {
			res.write(globalHandles.create(it));
		}
		return Status.STATUS_SUCCESS.getCValue();
	}

	@CEntryPoint(name = Constants.LIB_PREFIX
			+ "graph_create_between_eit", exceptionHandler = StatusReturnExceptionHandler.class)
	public static int createEdgesBetweenIterator(IsolateThread thread, ObjectHandle graphHandle, int source, int target,
			WordPointer res) {
		Graph<Integer, Integer> g = globalHandles.get(graphHandle);
		Set<Integer> edges = g.getAllEdges(source, target);
		if (edges == null) {
			throw new IllegalArgumentException("Unknown vertex " + source + " or " + target);
		}
		Iterator<Integer> it = edges.iterator();
		if (res.isNonNull()) {
			res.write(globalHandles.create(it));
		}
		return Status.STATUS_SUCCESS.getCValue();
	}

	@CEntryPoint(name = Constants.LIB_PREFIX
			+ "graph_vertex_create_eit", exceptionHandler = StatusReturnExceptionHandler.class)
	public static int createVertexEdgesOfIterator(IsolateThread thread, ObjectHandle graphHandle, int vertex,
			WordPointer res) {
		Graph<Integer, Integer> g = globalHandles.get(graphHandle);
		Iterator<Integer> it = g.edgesOf(vertex).iterator();
		if (res.isNonNull()) {
			res.write(globalHandles.create(it));
		}
		return Status.STATUS_SUCCESS.getCValue();
	}

	@CEntryPoint(name = Constants.LIB_PREFIX
			+ "graph_vertex_create_out_eit", exceptionHandler = StatusReturnExceptionHandler.class)
	public static int createVertexOutEdgesOfIterator(IsolateThread thread, ObjectHandle graphHandle, int vertex,
			WordPointer res) {
		Graph<Integer, Integer> g = globalHandles.get(graphHandle);
		Iterator<Integer> it = g.outgoingEdgesOf(vertex).iterator();
		if (res.isNonNull()) {
			res.write(globalHandles.create(it));
		}
		return Status.STATUS_SUCCESS.getCValue();
	}

	@CEntryPoint(name = Constants.LIB_PREFIX
			+ "graph_vertex_create_in_eit", exceptionHandler = StatusReturnExceptionHandler.class)
	public static int createVertexInEdgesOfIterator(IsolateThread thread, ObjectHandle graphHandle, int vertex,
			WordPointer res) {
		Graph<Integer, Integer> g = globalHandles.get(graphHandle);
		Iterator<Integer> it = g.incomingEdgesOf(vertex).iterator();
		if (res.isNonNull()) {
			res.write(globalHandles.create(it));
		}
		return Status.STATUS_SUCCESS.getCValue();
	}

	@CEntryPoint(name = Constants.LIB_PREFIX
			+ "graph_as_undirected", exceptionHandler = StatusReturnExceptionHandler.class)
	public static int asUndirected(IsolateThread thread, ObjectHandle graphHandle, WordPointer res) {
		Graph<Integer, Integer> gIn = globalHandles.get(graphHandle);
		Graph<Integer, Integer> gOut = new AsUndirectedGraph<>(gIn);
		if (res.isNonNull()) {
			res.write(globalHandles.create(gOut));
		}
		return Status.STATUS_SUCCESS.getCValue();
	}

	@CEntryPoint(name = Constants.LIB_PREFIX
			+ "graph_as_unmodifiable", exceptionHandler = StatusReturnExceptionHandler.class)
	public static int asUnmodifiable(IsolateThread thread, ObjectHandle graphHandle, WordPointer res) {
		Graph<Integer, Integer> gIn = globalHandles.get(graphHandle);
		Graph<Integer, Integer> gOut = new AsUnmodifiableGraph<>(gIn);
		if (res.isNonNull()) {
			res.write(globalHandles.create(gOut));
		}
		return Status.STATUS_SUCCESS.getCValue();
	}

	@CEntryPoint(name = Constants.LIB_PREFIX
			+ "graph_as_unweighted", exceptionHandler = StatusReturnExceptionHandler.class)
	public static int asUnweighted(IsolateThread thread, ObjectHandle graphHandle, WordPointer res) {
		Graph<Integer, Integer> gIn = globalHandles.get(graphHandle);
		Graph<Integer, Integer> gOut = new AsUnweightedGraph<>(gIn);
		if (res.isNonNull()) {
			res.write(globalHandles.create(gOut));
		}
		return Status.STATUS_SUCCESS.getCValue();
	}

	@CEntryPoint(name = Constants.LIB_PREFIX
			+ "graph_as_edgereversed", exceptionHandler = StatusReturnExceptionHandler.class)
	public static int asEdgeReversed(IsolateThread thread, ObjectHandle graphHandle, WordPointer res) {
		Graph<Integer, Integer> gIn = globalHandles.get(graphHandle);
		Graph<Integer, Integer> gOut = new EdgeReversedGraph<>(gIn);
		if (res.isNonNull()) {
			res.write(globalHandles.create(gOut));
		}
		return Status.STATUS_SUCCESS.getCValue();
	}

}
