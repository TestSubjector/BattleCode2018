// import the API.
import bc.*;
import java.util.*;

public class Player
{
    static GameController gc;
    static long initialWorkers;
    static long mapWidth;
    static long mapHeight;
    static Direction[] directions;
    static HashMap<Integer, Integer> timesMovementFailed;

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

        // Research Info
        ResearchInfo researchInfo;
        int[] researchLevelQueued = new int[5];

        // Starting PlanetMaps
        PlanetMap earthMap = gc.startingMap(Planet.Earth);
        PlanetMap marsMap = gc.startingMap(Planet.Mars);

        initialWorkers = earthMap.getInitial_units().size();
        mapWidth = earthMap.getWidth();
        mapHeight = earthMap.getHeight();

        // Initial karbonite locations
        HashMap<MapLocation, Long> earthKarboniteLocations = new HashMap<MapLocation, Long>();

        for (int x = 0; x < mapWidth; x++)
        {
            for (int y = 0; y < mapHeight; y++)
            {
                MapLocation tempMapLocation = new MapLocation(Planet.Earth, x, y);
                long karboniteAtTempMapLocation = earthMap.initialKarboniteAt(tempMapLocation);
                if (karboniteAtTempMapLocation > 0)
                {
                    earthKarboniteLocations.put(tempMapLocation, karboniteAtTempMapLocation);
                }
            }
        }

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
            // System.out.println("Current round: " + gc.round());
            // System.out.println("Karbonite: " + gc.karbonite());

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

            // Research Code - Worker(0), Rocket(7)
            if (gc.planet() == Planet.Mars)
            {
                if (researchLevelQueued[UnitType.Worker.swigValue()] < 4)
                {
                    gc.queueResearch(UnitType.Worker);
                    researchLevelQueued[UnitType.Worker.swigValue()]++;
                }
                // Removed else, replace with decision based research tree

                // Update researchInfo to new state
                researchInfo = gc.researchInfo();
                if (researchInfo.hasNextInQueue())
                {
                    UnitType currentResearchType = researchInfo.nextInQueue();
                    long currentResearchLevel = researchInfo.getLevel(currentResearchType) + 1;
//                    System.out.println(">> Researching " + currentResearchType + " Level " + currentResearchLevel);
//                    System.out.println("Research left " + researchInfo.roundsLeft());
                }
            }

            // Remove obsolete karboniteMapLocations
            Set<MapLocation> karboniteMapLocationSet = earthKarboniteLocations.keySet();
            LinkedList<MapLocation> removalList = new LinkedList<MapLocation>();
            for (MapLocation karboniteMapLocation : karboniteMapLocationSet)
            {
                if (gc.canSenseLocation(karboniteMapLocation) &&
                        gc.karboniteAt(karboniteMapLocation) == 0)
                {
                    removalList.add(karboniteMapLocation);
                }
            }
            for (MapLocation obsolete : removalList)
            {
                earthKarboniteLocations.remove(obsolete);
            }

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
                            // Build a structure if adjacent to one
                            VecUnit nearbyUnits = gc.senseNearbyUnitsByTeam(unit.location().mapLocation(), 2, gc.team());
                            for (int j = 0; j < units.size(); j++)
                            {
                                Unit nearbyUnit = units.get(j);
                                if (nearbyUnit.unitType() == UnitType.Factory || nearbyUnit.unitType() == UnitType.Rocket)
                                {
                                    if (gc.canBuild(unit.id(), nearbyUnit.id()))
                                    {
                                        gc.build(unit.id(), nearbyUnit.id());
                                    }
                                }
                            }

                            // Karbonite mining
                            for (int j = 0; j < directions.length; j++)
                            {
                                if (gc.canHarvest(unit.id(), directions[j]))
                                {
                                    gc.harvest(unit.id(), directions[j]);
                                    MapLocation minedMapLocation = unit.location().mapLocation().add(directions[j]);
                                    // remove from initial locations if depleted
                                    if (gc.karboniteAt(minedMapLocation) == 0)
                                    {
                                        earthKarboniteLocations.remove(minedMapLocation);
                                    }
                                    break;
                                }
                            }

                            // Worker replication
                            if (unitList.size() < 10 || unitList.size() < 3 * Math.sqrt(gc.round()))
                            {
                                Direction replicateDirection = directions[0];
                                int j = 1;
                                while (j < directions.length - 1 && !gc.canReplicate(unit.id(), replicateDirection))
                                {
                                    replicateDirection = directions[j++];
                                }
                                if (gc.canReplicate(unit.id(), replicateDirection))
                                {
                                    gc.replicate(unit.id(), replicateDirection);
                                    // System.out.println("Replicated at round: " + gc.round());
                                    continue;
                                }
                            }

                            // Structure building
                            while (!unfinishedBlueprints.isEmpty() &&
                                    gc.senseUnitAtLocation(unfinishedBlueprints.getFirst().location().mapLocation()).structureIsBuilt() == 1)
                            {
                                unfinishedBlueprints.removeFirst();
                            }
                            if (typeSortedUnitLists.get(UnitType.Factory).size() < Math.sqrt(gc.round()))
                            {
                                Direction blueprintDirection = directions[0];
                                int j = 1;
                                while (j < directions.length - 1 &&
                                        (!gc.canBlueprint(unit.id(), UnitType.Factory, blueprintDirection) ||
                                                gc.canSenseLocation(unit.location().mapLocation().add(blueprintDirection)) &&
                                                        gc.karboniteAt(unit.location().mapLocation().add(blueprintDirection)) != 0))
                                {
                                    blueprintDirection = directions[j++];
                                }
                                if (gc.canBlueprint(unit.id(), UnitType.Factory, blueprintDirection))
                                {
                                    gc.blueprint(unit.id(), UnitType.Factory, blueprintDirection);
                                    MapLocation blueprintLocation = unit.location().mapLocation().add(blueprintDirection);
                                    unfinishedBlueprints.add(gc.senseUnitAtLocation(blueprintLocation));
                                }
                                else
                                {
                                    blueprintDirection = directions[0];
                                    j = 1;
                                    while (j < directions.length - 1 &&
                                            !gc.canBlueprint(unit.id(), UnitType.Factory, blueprintDirection))
                                    {
                                        blueprintDirection = directions[j++];
                                    }
                                    if (gc.canBlueprint(unit.id(), UnitType.Factory, blueprintDirection))
                                    {
                                        gc.blueprint(unit.id(), UnitType.Factory, blueprintDirection);
                                        MapLocation blueprintLocation = unit.location().mapLocation().add(blueprintDirection);
                                        Unit newFactory = gc.senseUnitAtLocation(blueprintLocation);
                                        unfinishedBlueprints.add(newFactory);
                                        typeSortedUnitLists.get(UnitType.Factory).add(newFactory);
                                    }
                                }
                            }
                            if (!unfinishedBlueprints.isEmpty())
                            {
                                Unit blueprint = unfinishedBlueprints.getFirst();
                                Unit structure = gc.senseUnitAtLocation(blueprint.location().mapLocation());
                                if (unit.location().isAdjacentTo(structure.location()))
                                {
                                    if (gc.canBuild(unit.id(), structure.id()))
                                    {
                                        gc.build(unit.id(), structure.id());
                                    }
                                }
                                else
                                {
                                    moveUnitTowards(unit, structure.location());
                                }
                            }

                            // Move toward mines
                            karboniteMapLocationSet = earthKarboniteLocations.keySet();
                            MapLocation closestMineMapLocation = null;
                            MapLocation unitLoc = unit.location().mapLocation();
                            for (MapLocation karboniteMapLocation : karboniteMapLocationSet)
                            {
                                if (closestMineMapLocation == null)
                                {
                                    closestMineMapLocation = karboniteMapLocation;
                                }
                                else if (unitLoc.distanceSquaredTo(closestMineMapLocation) > unitLoc.distanceSquaredTo(karboniteMapLocation))
                                {
                                    closestMineMapLocation = karboniteMapLocation;
                                }
                            }
                            if (closestMineMapLocation != null)
                            {
                                moveUnitInDirection(unit, unitLoc.directionTo(closestMineMapLocation));
                            }
                        }

                        if (unit.unitType() == UnitType.Factory)
                        {
                            if (unit.isFactoryProducing() == 0)
                            {
                                if (gc.canProduceRobot(unit.id(), UnitType.Ranger))
                                {
                                    gc.produceRobot(unit.id(), UnitType.Ranger);
                                    Direction unloadDirection = directions[0];
                                    int j = 1;
                                    while (j < directions.length - 1 &&
                                            !gc.canUnload(unit.id(), unloadDirection))
                                    {
                                        unloadDirection = directions[j++];
                                    }
                                    if (gc.canUnload(unit.id(), unloadDirection))
                                    {
                                        gc.unload(unit.id(), unloadDirection);
                                        MapLocation unloadLocation = unit.location().mapLocation().add(unloadDirection);
                                        Unit newUnit = gc.senseUnitAtLocation(unloadLocation);
                                        typeSortedUnitLists.get(newUnit.unitType()).add(newUnit);
                                    }
                                }
                            }
                        }
                    } else
                    {
                        // Mars code here
                    }
                }
            }

            // Submit the actions we've done, and wait for our next turn.
            gc.nextTurn();
        }
    }
}
