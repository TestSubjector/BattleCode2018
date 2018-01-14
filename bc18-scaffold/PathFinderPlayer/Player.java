// import the API.

import bc.*;

import java.util.*;

public class Player
{
    static GameController gc;
    static PlanetMap homeMap;
    static PlanetMap awayMap;
    static long mapWidth;
    static long mapHeight;
    static Direction[] directions;
    static HashMap<MapLocation, LinkedList<Pair<MapLocation, Long>>> waypointAdjacencyList;

    public static void moveUnitInDirection(Unit unit, Direction candidateDirection)
    {
        int directionIndex = candidateDirection.swigValue();
        if (gc.isMoveReady(unit.id()))
        {
            int delta = 1;
            while (!gc.canMove(unit.id(), candidateDirection) && Math.abs(delta) <= 2)
            {
                candidateDirection = directions[(((directionIndex + delta) % 8) + 8) % 8];
                delta = -delta;
                if (delta > 0)
                {
                    delta++;
                }
            }
            if (gc.canMove(unit.id(), candidateDirection))
            {
                gc.moveRobot(unit.id(), candidateDirection);
            }
        }
    }

    public static void moveUnitTowards(Unit unit, Location targetLocation)
    {
        Direction targetDirection = unit.location().mapLocation().directionTo(targetLocation.mapLocation());
        moveUnitInDirection(unit, targetDirection);
    }

    public static void moveUnitAwayFrom(Unit unit, Location targetLocation)
    {
        Direction targetDirection = unit.location().mapLocation().directionTo(targetLocation.mapLocation());
        targetDirection = bc.bcDirectionOpposite(targetDirection);
        moveUnitInDirection(unit, targetDirection);
    }

    public static boolean isUninterruptedPathBetween(MapLocation from, MapLocation to)
    {
        while (!from.equals(to))
        {
            from = from.add(from.directionTo(to));
            if (homeMap.isPassableTerrainAt(from) != 1)
            {
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args)
    {
        // Connect to the manager, starting the game
        gc = new GameController();

        // Random number generator
        Random random = new Random();

        // Cardinal directions
        directions = Direction.values();

        // Unit types
        UnitType[] unitTypes = UnitType.values();

        // Get initial map states
        homeMap = gc.startingMap(gc.planet());
        if (gc.planet() == Planet.Mars)
        {
            awayMap = gc.startingMap(Planet.Earth);
        }
        else
        {
            awayMap = gc.startingMap(Planet.Mars);
        }

        mapWidth = homeMap.getWidth();
        mapHeight = homeMap.getHeight();

        waypointAdjacencyList = new HashMap<MapLocation, LinkedList<Pair<MapLocation, Long>>>();

        // Compute waypoints
        for (int x = 0; x < mapWidth; x++)
        {
            for (int y = 0; y < mapHeight; y++)
            {
                MapLocation possibleCornerMapLocation = new MapLocation(gc.planet(), x, y);
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
                        waypointAdjacencyList.put(possibleCornerMapLocation, new LinkedList<Pair<MapLocation, Long>>());
                    }
                }
            }
        }

        // Compute straight line paths
        long edges = 0;
        Set<MapLocation> waypoints = waypointAdjacencyList.keySet();
        for (MapLocation fromWaypoint : waypoints)
        {
            LinkedList<Pair<MapLocation, Long>> fromWaypointList = waypointAdjacencyList.get(fromWaypoint);
            for (MapLocation toWaypoint : waypoints)
            {
                if (fromWaypoint.equals(toWaypoint))
                {
                    continue;
                }
                if (isUninterruptedPathBetween(fromWaypoint, toWaypoint))
                {
                    fromWaypointList.add(new Pair<MapLocation, Long>(toWaypoint, fromWaypoint.distanceSquaredTo(toWaypoint)));
                    edges++;
                }
            }
        }
        System.out.println(edges);

        // List of blueprints
        LinkedList<Unit> unfinishedBlueprints = new LinkedList<Unit>();

        // Hashmap of units
        HashMap<UnitType, LinkedList<Unit>> typeSortedUnitLists = new HashMap<UnitType, LinkedList<Unit>>();

        for (int i = 0; i < unitTypes.length; i++)
        {
            typeSortedUnitLists.put(unitTypes[i], new LinkedList<Unit>());
        }

        while (true)
        {
            // Clear unit lists
            for (int i = 0; i < unitTypes.length; i++)
            {
                typeSortedUnitLists.get(unitTypes[i]).clear();
            }

            // Fetch current units
            VecUnit units = gc.myUnits();
            for (int i = 0; i < units.size(); i++)
            {
                Unit unit = units.get(i);
                typeSortedUnitLists.get(unit.unitType()).add(unit);
            }

            // Unit processing
            for (int i = 0; i < unitTypes.length; i++)
            {
                LinkedList<Unit> unitList = typeSortedUnitLists.get(unitTypes[i]);
                for (int u = 0; u < unitList.size(); u++)
                {
                    Unit unit = unitList.get(u);
                    if (gc.planet() == Planet.Earth)
                    {
                        if (unitTypes[i] == UnitType.Worker)
                        {
                            // Worker replication
                            if (unitList.size() < 30)
                            {
                                for (int j = 0; j < directions.length - 1; j++)
                                {
                                    Direction replicateDirection = directions[j];
                                    if (gc.canReplicate(unit.id(), replicateDirection))
                                    {
                                        gc.replicate(unit.id(), replicateDirection);
                                        unitList.add(gc.senseUnitAtLocation(unit.location().mapLocation().add(replicateDirection)));
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            // Submit the actions we've done, and wait for our next turn.
            gc.nextTurn();
        }
    }
}