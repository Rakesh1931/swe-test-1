package com.ecocode.analyzer;

import java.util.*;

public class CallGraph {
    private final Map<String, Set<String>> edges = new HashMap<>();
    private final Map<String, Integer> depthCache = new HashMap<>();

    public void addEdge(String caller, String callee) {
        if (caller == null || callee == null) return;
        edges.computeIfAbsent(caller, k -> new HashSet<>()).add(callee);
    }

    public Map<String, Set<String>> getEdges() {
        return edges;
    }

    public Set<String> reachableFrom(String startMethod) {
        Set<String> visited = new LinkedHashSet<>();
        if (startMethod == null || !edges.containsKey(startMethod)) {
            return visited;
        }

        Queue<String> queue = new LinkedList<>();
        queue.add(startMethod);
        visited.add(startMethod);

        while (!queue.isEmpty()) {
            String curr = queue.poll();
            Set<String> neighbors = edges.getOrDefault(curr, Collections.emptySet());
            for (String neighbor : neighbors) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }

        return visited;
    }

    public boolean canReach(String fromMethod, String toMethod) {
        if (fromMethod == null || toMethod == null) return false;
        if (fromMethod.equals(toMethod)) return true;

        Set<String> visited = new HashSet<>();
        return dfs(fromMethod, toMethod, visited);
    }

    private boolean dfs(String curr, String target, Set<String> visited) {
        if (curr.equals(target)) return true;
        visited.add(curr);

        Set<String> neighbors = edges.getOrDefault(curr, Collections.emptySet());
        for (String neighbor : neighbors) {
            if (!visited.contains(neighbor)) {
                if (dfs(neighbor, target, visited)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Fast bounded depth calculation. Threshold for EC106 is 8, so we bound search to max 12.
     * Uses memoization to avoid exponential re-evaluations.
     */
    public int getReachabilityDepth(String startMethod) {
        if (startMethod == null || !edges.containsKey(startMethod)) return 0;
        if (depthCache.containsKey(startMethod)) {
            return depthCache.get(startMethod);
        }

        Set<String> visited = new HashSet<>();
        int depth = calculateDepthBounded(startMethod, visited, 0, 12);
        depthCache.put(startMethod, depth);
        return depth;
    }

    private int calculateDepthBounded(String curr, Set<String> visited, int currentDepth, int maxLimit) {
        if (currentDepth >= maxLimit) {
            return maxLimit;
        }
        visited.add(curr);
        int maxChildDepth = 0;

        for (String neighbor : edges.getOrDefault(curr, Collections.emptySet())) {
            if (!visited.contains(neighbor)) {
                int child = calculateDepthBounded(neighbor, visited, currentDepth + 1, maxLimit);
                maxChildDepth = Math.max(maxChildDepth, child);
                if (maxChildDepth >= maxLimit) break;
            }
        }

        visited.remove(curr); // backtrack efficiently without cloning sets
        return 1 + maxChildDepth;
    }

    public static CallGraph buildFrom(ProjectModel model) {
        CallGraph graph = new CallGraph();
        for (MethodCallInfo call : model.getAllMethodCalls()) {
            String caller = call.getCallerFullMethodId();
            String callee = call.getResolvedTarget();
            graph.addEdge(caller, callee);
        }
        return graph;
    }
}
