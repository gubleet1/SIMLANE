package ch.simlane.tme.algorithms;

import ch.simlane.tme.components.Connector;
import ch.simlane.tme.components.Lane;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class ShortestPath {

    public static LinkedList<Connector> shortestPath(Connector startConnector, Connector endConnector) {
        Node start = new Node(startConnector);
        Node end = new Node(endConnector);
        Graph graph = createGraph(start);
        if (!graph.contains(end)) {
            return null;
        }
        HashMap<Node, Node> parents = new HashMap<>();
        HashMap<Node, Double> shortestPath = new HashMap<>();
        parents.put(start, null);
        for (Node node : graph.nodes) {
            shortestPath.put(node, node.equals(start) ? 0.0 : Double.POSITIVE_INFINITY);
        }
        for (Edge edge : start.edges) {
            shortestPath.put(edge.end, edge.weight);
            parents.put(edge.end, start);
        }
        start.visit();
        while (true) {
            Node current = nextUnvisited(graph, shortestPath);
            if (current == null) {
                return null;
            }
            if (current.equals(end)) {
                LinkedList<Connector> path = new LinkedList<>();
                Node node = end;
                path.push(node.connector);
                while (true) {
                    node = parents.get(node);
                    if (node == null) {
                        break;
                    }
                    path.push(node.connector);
                }
                return path;
            }
            current.visit();
            for (Edge edge : current.edges) {
                if (edge.end.isVisited()) {
                    continue;
                }
                double weight = shortestPath.get(current) + edge.weight;
                if (weight < shortestPath.get(edge.end)) {
                    shortestPath.put(edge.end, weight);
                    parents.put(edge.end, current);
                }
            }
        }
    }

    private static Node nextUnvisited(Graph graph, HashMap<Node, Double> shortestPath) {
        double shortest = Double.POSITIVE_INFINITY;
        Node next = null;
        for (Node node : graph.nodes) {
            if (node.isVisited()) {
                continue;
            }
            double distance = shortestPath.get(node);
            if (distance == Double.POSITIVE_INFINITY) {
                continue;
            }
            if (distance < shortest) {
                shortest = distance;
                next = node;
            }
        }
        return next;
    }

    private static Graph createGraph(Node start) {
        Graph graph = new Graph();
        graph.add(start);
        LinkedList<Edge> unprocessed = new LinkedList<>(createEdges(start));
        while (!unprocessed.isEmpty()) {
            Edge edge = unprocessed.pop();
            if (!graph.contains(edge.end)) {
                graph.add(edge.end);
                unprocessed.addAll(createEdges(edge.end));
            }
            graph.add(edge);
        }
        return graph;
    }

    private static List<Edge> createEdges(Node start) {
        List<Edge> edges = new LinkedList<>();
        Connector connector = start.connector;
        if (connector.getOut() == null) {
            return edges;
        }
        for (Lane lane : connector.getOut().getLanes()) {
            Node end = new Node(lane.getEnd());
            Edge edge = new Edge(start, end, lane.getLength());
            edges.add(edge);
        }
        return edges;
    }

    private static class Graph {

        List<Node> nodes;

        Graph() {
            nodes = new LinkedList<>();
        }

        void add(Node node) {
            nodes.add(node);
        }

        void add(Edge edge) {
            edge.start.addEdge(edge);
        }

        boolean contains(Node node) {
            return nodes.contains(node);
        }
    }

    private static class Node {

        List<Edge> edges;
        Connector connector;

        boolean visited;

        Node(Connector connector) {
            edges = new LinkedList<>();
            this.connector = connector;
        }

        void addEdge(Edge edge) {
            edges.add(edge);
        }

        boolean isVisited() {
            return visited;
        }

        void visit() {
            visited = true;
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof Node)) {
                return false;
            }
            Node other = (Node) o;
            return connector.equals(other.connector);
        }

        public int hashCode() {
            return Objects.hash(connector);
        }
    }

    private static class Edge {

        Node start;
        Node end;
        double weight;

        Edge(Node start, Node end, double weight) {
            this.start = start;
            this.end = end;
            this.weight = weight;
        }
    }
}
