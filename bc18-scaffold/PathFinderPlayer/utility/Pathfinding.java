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
            if (homeMap.isPassableTerrainAt(temp) == 0)
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
                int totalPassableDiagonalSquares = 0;
                for (int i = 1; i < directions.length - 1; i += 2)
                {
                    MapLocation diagonalSquare = possibleWaypoint.add(directions[i]);
                    boolean diagonalSquareIsPassable = homeMap.onMap(diagonalSquare) &&
                            homeMap.isPassableTerrainAt(diagonalSquare) == 1;
                    if (diagonalSquareIsPassable)
                    {
                        totalPassableDiagonalSquares++;
                    }
                    MapLocation[] sideSquares = new MapLocation[4];
                    sideSquares[0] = possibleWaypoint.add(directions[(i + 1) % 8]);
                    sideSquares[1] = possibleWaypoint.add(directions[(i + 3) % 8]);
                    sideSquares[2] = possibleWaypoint.add(directions[(i + 5) % 8]);
                    sideSquares[3] = possibleWaypoint.add(directions[(i + 7) % 8]);
                    boolean isExteriorCorner = homeMap.onMap(diagonalSquare) &&
                            homeMap.isPassableTerrainAt(diagonalSquare) == 0 &&
                            homeMap.isPassableTerrainAt(sideSquares[0]) == 1 &&
                            homeMap.isPassableTerrainAt(sideSquares[3]) == 1;
                    int blockedSides = 0;
                    for (int j = 0; j < 4; j++)
                    {
                        if (!homeMap.onMap(sideSquares[j]) || homeMap.isPassableTerrainAt(sideSquares[j]) == 0)
                        {
                            blockedSides++;
                        }
                    }
                    if (isExteriorCorner ||
                            (totalPassableDiagonalSquares >= 1 && blockedSides >= 3) ||
                            (totalPassableDiagonalSquares >= 2 && blockedSides >= 2))
                    {
                        waypointAdjacencyList.put(possibleWaypoint, new LinkedList<GraphPair<MapLocation, Long>>());
                        nearestUnobstructedWaypoints.put(possibleWaypoint, possibleWaypoint);
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
        numberOfConnectedComponents = 0;
        HashMap<MapLocation, Long> connectedComponentSize = new HashMap<MapLocation, Long>();
        for (MapLocation sourceWaypoint : waypoints)
        {
            if (!connectedComponentSize.containsKey(sourceWaypoint))
            {
                numberOfConnectedComponents++;
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
        HashMap<MapLocation, HashMap<MapLocation, Long>> distances = new HashMap<MapLocation, HashMap<MapLocation, Long>>();
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
            while (!edgeQueue.isEmpty())
            {
                GraphPair<MapLocation, Long> topEdge = edgeQueue.remove();
                if (done.contains(topEdge.getFirst()))
                {
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
            distances.put(sourceWaypoint, distanceTo);
        }
        computeStartingPaths(distances);
    }

    private static void computeStartingPaths(HashMap<MapLocation, HashMap<MapLocation, Long>> distances)
    {
        primeFactoryLocations = new HashSet<MapLocation>();
        for (int i = 0; i < initialWorkers.size(); i++)
        {
            MapLocation us = initialWorkers.get(i).location().mapLocation();
            MapLocation ourWaypoint = findNearestUnobstructedWaypoint(us);
            for (int j = 0; j < initialEnemyWorkers.size(); j++)
            {
                MapLocation them = initialEnemyWorkers.get(j);
                MapLocation theirWaypoint = findNearestUnobstructedWaypoint(them);
                if (!constructPathBetween(ourWaypoint, theirWaypoint))
                {
                    workersInDifferentComponents = true;
                }
                else
                {
                    long fullDistance = distances.get(ourWaypoint).get(theirWaypoint);
                    MapLocation currentWaypoint = ourWaypoint;
                    while (distances.get(currentWaypoint).get(theirWaypoint) < 0.4 * fullDistance)
                    {
                        primeFactoryLocations.add(currentWaypoint);
                        System.out.println(currentWaypoint);
                        currentWaypoint = nextBestWaypoint.get(new Pair<MapLocation, MapLocation>(currentWaypoint, theirWaypoint));
                    }
                }
            }
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
        long minSquaredDistance = 100000L;
        for (MapLocation waypoint : waypoints)
        {
            long newSquaredDistance = mapLocation.distanceSquaredTo(waypoint);
            if (newSquaredDistance < minSquaredDistance && isUninterruptedPathBetween(mapLocation, waypoint))
            {
                nearest = waypoint;
                minSquaredDistance = newSquaredDistance;
                if (minSquaredDistance == 1)
                {
                    break;
                }
            }
        }
        nearestUnobstructedWaypoints.put(mapLocation, nearest);
        return nearest;
    }

    public static boolean constructPathBetween(MapLocation startWaypoint, MapLocation endWaypoint)
    {
        Pair<MapLocation, MapLocation> key = new Pair<>(startWaypoint, endWaypoint);
        if (!nextBestWaypoint.containsKey(key))
        {
            HashMap<MapLocation, MapLocation> shortestPathTree = shortestPathTrees.get(startWaypoint);
            MapLocation son = endWaypoint;
            Stack<MapLocation> tree = new Stack<MapLocation>();
            while (son != null && !son.equals(startWaypoint))
            {
                MapLocation parent = shortestPathTree.get(son);
                nextBestWaypoint.put(new Pair<MapLocation, MapLocation>(parent, endWaypoint), son);
                son = parent;
            }
            if (son == null)
            {
                return false;
            }
        }
        return true;
    }
}
