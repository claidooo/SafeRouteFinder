package com.cjsm.saferoutefinder;

import android.content.Context;
import java.util.*;

public class SafestPathFinder {
    static class Graph {
        private final Map<String, List<Edge>> adjacencyList = new HashMap<>();

        static class Edge {
            String destination;
            double weight;

            Edge(String destination, double weight) {
                this.destination = destination;
                this.weight = weight;
            }
        }

        void addEdge(String from, String to, double safetyRating) {
            double weight = 1.0 / safetyRating; // Higher rating = lower weight (safest)

            // Instead of computeIfAbsent, manually check and add entries
            if (!adjacencyList.containsKey(from)) {
                adjacencyList.put(from, new ArrayList<>());
            }
            if (!adjacencyList.containsKey(to)) {
                adjacencyList.put(to, new ArrayList<>());
            }

            adjacencyList.get(from).add(new Edge(to, weight));
            adjacencyList.get(to).add(new Edge(from, weight));
        }

        Map<String, List<Edge>> getGraph() {
            return adjacencyList;
        }
    }

    public static List<String> findSafestPath(Context context, String start, String destination) {
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        Map<String, Double> ratings = dbHelper.getAllRatings();

        Graph graph = new Graph();
        for (Map.Entry<String, Double> entry : ratings.entrySet()) {
            String street = entry.getKey();
            double rating = entry.getValue();
            graph.addEdge(street, street, rating);
        }

        return dijkstraSafest(graph, start, destination);
    }

    private static List<String> dijkstraSafest(Graph graph, String start, String destination) {
        PriorityQueue<Node> pq = new PriorityQueue<>();
        Map<String, Double> safestScores = new HashMap<>();
        Map<String, String> previous = new HashMap<>();
        Set<String> visited = new HashSet<>();

        for (String street : graph.getGraph().keySet()) {
            safestScores.put(street, Double.MAX_VALUE);
        }
        safestScores.put(start, 0.0);
        pq.add(new Node(start, 0.0));

        while (!pq.isEmpty()) {
            Node current = pq.poll();
            String currentStreet = current.street;

            if (visited.contains(currentStreet)) continue;
            visited.add(currentStreet);

            if (currentStreet.equals(destination)) break;

            List<Graph.Edge> edges = graph.getGraph().containsKey(currentStreet) ? graph.getGraph().get(currentStreet) : new ArrayList<>();
            for (Graph.Edge edge : edges) {
                if (visited.contains(edge.destination)) continue;

                double newScore = safestScores.get(currentStreet) + edge.weight;
                if (newScore < safestScores.get(edge.destination)) {
                    safestScores.put(edge.destination, newScore);
                    previous.put(edge.destination, currentStreet);
                    pq.add(new Node(edge.destination, newScore));
                }
            }

        }

        List<String> safestPath = new ArrayList<>();
        for (String at = destination; at != null; at = previous.get(at)) {
            safestPath.add(at);
        }
        Collections.reverse(safestPath);
        return safestPath;
    }

    static class Node implements Comparable<Node> {
        String street;
        double safestScore;

        Node(String street, double safestScore) {
            this.street = street;
            this.safestScore = safestScore;
        }

        @Override
        public int compareTo(Node other) {
            return Double.compare(this.safestScore, other.safestScore);
        }
    }
}