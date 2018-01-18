package utility;

import java.util.*;

import bc.*;

import static utility.Globals.*;

public class Pathfinding
{
    public static long diagonalDistanceBetween(MapLocation first, MapLocation second)
    {
        return Math.max(Math.abs(first.getX() - second.getX()), Math.abs(first.getY() - second.getY()));
    }

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
                MapLocation possibleCornerMapLocation = mapLocationAt[x][y];
                if (homeMap.isPassableTerrainAt(possibleCornerMapLocation) == 0)
                {
                    continue;
                }
                for (int i = 1; i < directions.length - 1; i += 2)
                {
                    MapLocation possibleObstacleMapLocation = possibleCornerMapLocation.add(directions[i]);
                    MapLocation possibleFreeMapLocation1 = possibleCornerMapLocation.add(directions[i - 1]);
                    MapLocation possibleFreeMapLocation2 = possibleCornerMapLocation.add(directions[(i + 1) % 8]);
                    if (homeMap.onMap(possibleObstacleMapLocation) &&
                            homeMap.isPassableTerrainAt(possibleObstacleMapLocation) == 0 &&
                            homeMap.isPassableTerrainAt(possibleFreeMapLocation1) == 1 &&
                            homeMap.isPassableTerrainAt(possibleFreeMapLocation2) == 1)
                    {
                        waypointAdjacencyList.put(possibleCornerMapLocation, new LinkedList<GraphPair<MapLocation, Long>>());
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
                    edgeQueue.add(new GraphPair<MapLocation, Long>(waypoint, 100000L));
                }
            }
            long connectedWaypoints = connectedComponentSize.get(sourceWaypoint);
            for (int i = 0; i < connectedWaypoints; i++)
            {
                GraphPair<MapLocation, Long> topEdge = edgeQueue.remove();
                for (GraphPair<MapLocation, Long> edge : waypointAdjacencyList.get(topEdge.getFirst()))
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

    public static void constructPathBetween(MapLocation startWaypoint, MapLocation endWaypoint)
    {
        Pair<MapLocation, MapLocation> key = new Pair<>(startWaypoint, endWaypoint);
        if (!nextBestWaypoint.containsKey(key))
        {
            HashMap<MapLocation, MapLocation> shortestPathTree = shortestPathTrees.get(startWaypoint);
            MapLocation son = endWaypoint;
            while (!son.equals(startWaypoint))
            {
                MapLocation parent = shortestPathTree.get(son);
                nextBestWaypoint.put(new Pair<MapLocation, MapLocation>(parent, endWaypoint), son);
                son = parent;
            }
        }
    }
}
