package utility;

import java.util.*;

import bc.*;

import static utility.Globals.*;

public class Pathfinding
{

    public static boolean isUninterruptedPathBetween(MapLocation from, MapLocation to)
    {
        MapLocation temp = from;
        while (!temp.equals(to))
        {
            if (homeMap.isPassableTerrainAt(temp) != 1)
            {
                return false;
            }
            temp = temp.add(temp.directionTo(to));
        }
        return true;
    }

    public static long findComponentSize(MapLocation from)
    {
        visited.put(from, true);
        long subgraphSize = 1;
        for (GraphPair<MapLocation, Long> waypoint : waypointAdjacencyList.get(from))
        {
            if (!visited.get(waypoint.getFirst()))
            {
                subgraphSize += findComponentSize(waypoint.getFirst());
            }
        }
        return subgraphSize;
    }

    public static void computeShortestPathTrees()
    {
        // Compute waypoints
        for (int x = 0; x < homeMapWidth; x++)
        {
            for (int y = 0; y < homeMapHeight; y++)
            {
                MapLocation possibleWaypoint = mapLocationAt[x][y];
                if (homeMap.isPassableTerrainAt(possibleWaypoint) == 0)
                {
                    continue;
                }

                for (int i = 1; i < directions.length - 1; i += 2)
                {
                    // TODO - Add more waypoints for edge case maps
                    MapLocation diagonalSquare = possibleWaypoint.add(directions[i]);
                    MapLocation sideSquareOne = possibleWaypoint.add(directions[i - 1]);
                    MapLocation sideSquareTwo = possibleWaypoint.add(directions[(i + 1) % 8]);
                    boolean isExteriorCorner = homeMap.onMap(diagonalSquare) &&
                            homeMap.isPassableTerrainAt(diagonalSquare) == 0 &&
                            homeMap.isPassableTerrainAt(sideSquareOne) == 1 &&
                            homeMap.isPassableTerrainAt(sideSquareTwo) == 1;
                    boolean isInteriorCorner = homeMap.onMap(diagonalSquare) &&
                            homeMap.isPassableTerrainAt(diagonalSquare) == 0 &&
                            homeMap.isPassableTerrainAt(sideSquareOne) == 0 &&
                            homeMap.isPassableTerrainAt(sideSquareTwo) == 0;
                    if (isExteriorCorner || isInteriorCorner)
                    {
                        waypointAdjacencyList.put(possibleWaypoint, new LinkedList<GraphPair<MapLocation, Long>>());
                    }
                }
            }
        }

        // Compute straight line paths
        long edges = 0;
        Set<MapLocation> waypoints = waypointAdjacencyList.keySet();
        for (MapLocation fromWaypoint : waypoints)
        {
            LinkedList<GraphPair<MapLocation, Long>> fromWaypointList = waypointAdjacencyList.get(fromWaypoint);
            for (MapLocation toWaypoint : waypoints)
            {
                if (!fromWaypoint.equals(toWaypoint) && isUninterruptedPathBetween(fromWaypoint, toWaypoint))
                {
                    fromWaypointList.add(new GraphPair<MapLocation, Long>(toWaypoint, diagonalDistanceBetween(fromWaypoint, toWaypoint)));
                    edges++;
                }
            }
        }
        System.out.println("Waypoints: " + waypoints.size());
        System.out.println("Edges: " + edges);

        // Find connected component sizes
        HashMap<MapLocation, Long> connectedComponentSize = new HashMap<MapLocation, Long>();
        for (MapLocation sourceWaypoint : waypoints)
        {
            if (!connectedComponentSize.containsKey(sourceWaypoint))
            {
                for (MapLocation waypoint : waypoints)
                {
                    visited.put(waypoint, false);
                }
                long componentSize = findComponentSize(sourceWaypoint);
                for (MapLocation waypoint : waypoints)
                {
                    if (visited.get(waypoint))
                    {
                        connectedComponentSize.put(waypoint, componentSize);
                    }
                }
            }
        }

        // TODO - Make Dijkstra on demand to save computation
        // TODO - Probably even switch out in favour of distributed A*
        // Run Dijkstra's Algorithm for each waypoint to generate shortest path trees
        for (MapLocation sourceWaypoint : waypoints)
        {
            shortestPathTrees.put(sourceWaypoint, new HashMap<MapLocation, MapLocation>());
            HashMap<MapLocation, MapLocation> shortestPathTree = shortestPathTrees.get(sourceWaypoint);
            Queue<GraphPair<MapLocation, Long>> edgeQueue = new PriorityQueue<GraphPair<MapLocation, Long>>();
            HashMap<MapLocation, Long> distanceTo = new HashMap<MapLocation, Long>();
            for (MapLocation waypoint : waypoints)
            {
                if (waypoint.equals(sourceWaypoint))
                {
                    distanceTo.put(waypoint, 0L);
                    edgeQueue.add(new GraphPair<MapLocation, Long>(waypoint, 0L));
                }
                else
                {
                    distanceTo.put(waypoint, 100000L);
                }
            }
            HashSet<MapLocation> done = new HashSet<MapLocation>();
            int repeats = 0;
            long connectedWaypoints = connectedComponentSize.get(sourceWaypoint);
            while (!edgeQueue.isEmpty())
            {
                GraphPair<MapLocation, Long> topEdge = edgeQueue.remove();
                if (done.contains(topEdge.getFirst()))
                {
                    repeats++;
                    continue;
                }
                done.add(topEdge.getFirst());
                for (GraphPair<MapLocation, Long> edge : waypointAdjacencyList.get(topEdge.getFirst()))
                {
                    if (!done.contains(edge.getFirst()))
                    {
                        long newDistance = distanceTo.get(topEdge.getFirst()) + edge.getSecond();
                        if (newDistance < distanceTo.get(edge.getFirst()))
                        {
                            distanceTo.put(edge.getFirst(), newDistance);
                            shortestPathTree.put(edge.getFirst(), topEdge.getFirst());
                            edgeQueue.add(new GraphPair<MapLocation, Long>(edge.getFirst(), newDistance));
                        }
                    }
                }
            }
            // System.out.println(repeats);
        }
    }

    public static MapLocation findNearestUnobstructedWaypoint(MapLocation mapLocation)
    {
        if (nearestUnobstructedWaypoints.containsKey(mapLocation))
        {
            return nearestUnobstructedWaypoints.get(mapLocation);
        }
        Set<MapLocation> waypoints = waypointAdjacencyList.keySet();
        MapLocation nearest = null;
        long distance = 100000L;
        for (MapLocation waypoint : waypoints)
        {
            long newDistance = diagonalDistanceBetween(mapLocation, waypoint);
            if (newDistance < distance && isUninterruptedPathBetween(mapLocation, waypoint))
            {
                nearest = waypoint;
                distance = newDistance;
                if (distance <= 1)
                {
                    break;
                }
            }
        }
        nearestUnobstructedWaypoints.put(mapLocation, nearest);
        return nearest;
    }

     // TODO - Fix unreachable locations
    public static void constructPathBetween(MapLocation startWaypoint, MapLocation endWaypoint)
    {
        Pair<MapLocation, MapLocation> key = new Pair<>(startWaypoint, endWaypoint);
        if (!nextBestWaypoint.containsKey(key))
        {
            HashMap<MapLocation, MapLocation> shortestPathTree = shortestPathTrees.get(startWaypoint);
            MapLocation son = endWaypoint;
            Stack<MapLocation> tree = new Stack<MapLocation>();
            try
            {
                while (!son.equals(startWaypoint))
                {
                    MapLocation parent = shortestPathTree.get(son);
                    tree.push(parent);
                    nextBestWaypoint.put(new Pair<MapLocation, MapLocation>(parent, endWaypoint), son);
                    son = parent;
                }
            }
            catch (Exception e)
            {
                System.out.println(startWaypoint);
                while (!tree.isEmpty())
                {
                    System.out.println(tree.pop());
                }
                System.out.println(endWaypoint);
                System.out.println(son);
                System.out.println("========================================");
            }
        }
    }
}
