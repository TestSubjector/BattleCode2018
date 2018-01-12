// import the API.
import bc.*;
import java.util.*;

public class Player 
{
    static GameController gc;

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

        // Starting PlanetMaps
        PlanetMap earthMap = gc.startingMap(Planet.Earth);
        PlanetMap marsMap = gc.startingMap(Planet.Mars);

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

            for (int i = 0; i < unitTypes.length; i++)
            {
                LinkedList<Unit> unitList = typeSortedUnitLists.get(unitTypes[i]);
                for (Unit unit : unitList)
                {
                    if (gc.planet() == Planet.Earth)
                    {
                        if (unit.unitType() == UnitType.Worker)
                        {
                            if (units.size() < 6 || units.size() < Math.sqrt(gc.round()))
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
                                    continue;
                                }
                            }
                            while (!unfinishedBlueprints.isEmpty() &&
                                    gc.senseUnitAtLocation(unfinishedBlueprints.getFirst().location().mapLocation()).structureIsBuilt() == 1)
                            {
                                unfinishedBlueprints.removeFirst();
                            }
                            if (unfinishedBlueprints.isEmpty())
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
                            else
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
                        }
                        if (unit.unitType() == UnitType.Factory)
                        {

                        }
                    }
                    else
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