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
        if(prepareRocketArmada)
        {
            return;
        }
        else if (unit.isFactoryProducing() == 0)
        {
            int workerCount = typeSortedUnitLists.get(UnitType.Worker).size(); // rarely produced
            int knightCount = typeSortedUnitLists.get(UnitType.Knight).size(); // not being produced
            int rangerCount = typeSortedUnitLists.get(UnitType.Ranger).size();
            int mageCount = typeSortedUnitLists.get(UnitType.Mage).size();
            int healerCount = typeSortedUnitLists.get(UnitType.Healer).size();

            // Think of better condition later; produce workers if existing ones are being massacred
            if (workerCount == 0)
            {
                if (gc.canProduceRobot(unit.id(), UnitType.Worker))
                {
                    gc.produceRobot(unit.id(), UnitType.Worker);
                }
            }
            else if (rangerCount >= 7 * (healerCount))
            {
                UnitType typeToBeProduced = UnitType.Healer;
                if (gc.canProduceRobot(unit.id(), typeToBeProduced))
                {
                    gc.produceRobot(unit.id(), typeToBeProduced);
                }
            }
            else
            {
                if (gc.canProduceRobot(unit.id(), UnitType.Ranger))
                {
                    gc.produceRobot(unit.id(), UnitType.Ranger);
                }
            }
        }
    }
}
