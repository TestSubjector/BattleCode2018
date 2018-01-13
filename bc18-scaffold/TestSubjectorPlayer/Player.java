// import the API.
import bc.*;
import java.util.*;

public class Player
{
    static GameController gc;
    static long initialWorkers;
    static long mapWidth;
    static long mapHeight;

    public static void moveUnitTowards(Unit unit, Location targetLocation)
    {
        Direction movementDirection = unit.location().mapLocation().directionTo(targetLocation.mapLocation());
        if (gc.isMoveReady(unit.id()) && gc.canMove(unit.id(), movementDirection)
                )
        {
            gc.moveRobot(unit.id(), movementDirection);
        }
    }

    public static void main(String[] args)
    {
        // Connect to the manager, starting the game
        gc = new GameController();

        // Cardinal directions
        Direction[] directions = Direction.values();

        // Unit types
        UnitType[] unitTypes = UnitType.values();

        // Research Info
        ResearchInfo ri = new ResearchInfo();

        // Random Number
        Random ran = new Random();

        // Starting PlanetMaps
        PlanetMap earthMap = gc.startingMap(Planet.Earth);
        PlanetMap marsMap = gc.startingMap(Planet.Mars);

        initialWorkers = earthMap.getInitial_units().size();
        mapWidth = earthMap.getWidth();
        mapHeight = earthMap.getHeight();

        // Initial karbonite locations
        LinkedList<MapLocation> earthKarboniteLocations = new LinkedList<MapLocation>();

        for (int x = 0; x < mapWidth; x++)
        {
            for (int y = 0; y < mapHeight; y++)
            {
                MapLocation tempMapLocation = new MapLocation(Planet.Earth, x, y);
                long karboniteAtTempMapLocation = earthMap.initialKarboniteAt(tempMapLocation);
                if (karboniteAtTempMapLocation > 0)
                {
                    earthKarboniteLocations.add(tempMapLocation);
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

            VecUnit units = gc.myUnits();
            for (int i = 0; i < units.size(); i++)
            {
                Unit unit = units.get(i);
                typeSortedUnitLists.get(unit.unitType()).add(unit);
            }


            // Research Code - Worker(0), Rocket(7)
            ri = gc.researchInfo();

            if(!ri.hasNextInQueue())
            {
                if (!(ri.getLevel(unitTypes[0]) == 1))
                {
                    gc.queueResearch(unitTypes[0]); // Return 1 if done, else 0
                    //System.out.println(">> Researching worker stuff");
                }
                else
                {
                    //System.out.println(">> Researching something random");
                    gc.queueResearch(unitTypes[ran.nextInt(7)]);
                }
            }
            else
            {
                //System.out.println(">> Research left" + ri.roundsLeft());
            }

            for (int i = 0; i < unitTypes.length; i++)
            {
                LinkedList<Unit> unitList = typeSortedUnitLists.get(unitTypes[i]);
                for (Unit unit : unitList)
                {
                    if (gc.planet() == Planet.Earth)
                    {
                        if (unitTypes[i] == UnitType.Worker)
                        {
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
                            if (typeSortedUnitLists.get(UnitType.Factory).size() < 5)
                            {
                                Direction blueprintDirection = directions[0];
                                int j = 1;
                                while (j < directions.length - 1 && !gc.canBlueprint(unit.id(), UnitType.Factory, blueprintDirection))
                                {
                                    blueprintDirection = directions[j++];
                                }
                                if (gc.canBlueprint(unit.id(), UnitType.Factory, blueprintDirection))
                                {
                                    gc.blueprint(unit.id(), UnitType.Factory, blueprintDirection);
                                    MapLocation blueprintLocation = unit.location().mapLocation().add(blueprintDirection);
                                    unfinishedBlueprints.add(gc.senseUnitAtLocation(blueprintLocation));
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
                                } else
                                {
                                    moveUnitTowards(unit, structure.location());
                                }
                            }
                        }
                        if (unit.unitType() == UnitType.Factory)
                        {
                            if (unit.isFactoryProducing() == 0)
                            {
                                if (gc.canProduceRobot(unit.id(), UnitType.Ranger))
                                {   // Spawning as Garrison
                                    gc.produceRobot(unit.id(), UnitType.Ranger);
                                }
                            }
                        }
                        if (unit.unitType() == UnitType.Ranger)
                        {
                            if(unit.location().isInGarrison())
                            {
                                System.out.println(gc.round() + ">> Ranger is in Garrison! " + unit.id());
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
