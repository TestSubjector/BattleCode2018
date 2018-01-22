package utility;

import java.util.*;

import bc.*;

import static utility.Globals.*;
import static utility.DecisionTree.*;

public class FactoryBot
{
    // Unloads a robot, if possible
    public static boolean tryToUnloadRobot(Unit structure)
    {
        for (int i = 0; i < directions.length - 1; i++)
        {
            Direction unloadDirection = directions[i];
            if (gc.canUnload(structure.id(), unloadDirection))
            {
                gc.unload(structure.id(), unloadDirection);
                MapLocation unloadLocation = structure.location().mapLocation().add(unloadDirection);
                if (gc.canSenseLocation(unloadLocation))
                {
                    if (gc.hasUnitAtLocation(unloadLocation))
                    {
                        Unit newUnit = gc.senseUnitAtLocation(unloadLocation);
                        typeSortedUnitLists.get(newUnit.unitType()).add(newUnit);
                    }
                }
                return true;
            }
        }
        return false;
    }

    public static void processFactory(Unit unit, Location unitLocation)
    {
        tryToUnloadRobot(unit);
        if (unit.isFactoryProducing() == 0)
        {
            if (!buildQueue.isEmpty() &&
                    buildQueue.peekFirst() != UnitType.Worker &&
                    buildQueue.peekFirst() != UnitType.Factory &&
                    buildQueue.peekFirst() != UnitType.Rocket)
            {
                if (gc.canProduceRobot(unit.id(), buildQueue.peekFirst()))
                {
                    gc.produceRobot(unit.id(), buildQueue.peekFirst());
                    removeUnitFromQueue();
                }
            }
        }
    }
}
