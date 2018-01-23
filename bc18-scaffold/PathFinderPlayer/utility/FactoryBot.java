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
            if (!trainQueue.isEmpty() &&
                    (trainQueue.peekFirst() != UnitType.Worker || typeSortedUnitLists.get(UnitType.Worker).size() < 4) &&
                    trainQueue.peekFirst() != UnitType.Factory &&
                    trainQueue.peekFirst() != UnitType.Rocket)
            {
                if (gc.canProduceRobot(unit.id(), trainQueue.peekFirst()))
                {
                    gc.produceRobot(unit.id(), trainQueue.peekFirst());
                    removeUnitFromTrainQueue();
                }
            }
        }
    }
}
