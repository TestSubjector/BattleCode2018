import java.util.*;

import bc.*;

import utility.QueuePair;

import static utility.Globals.*;
import static utility.WorkerBot.*;
import static utility.KnightBot.*;
import static utility.RangerBot.*;
import static utility.MageBot.*;
import static utility.HealerBot.*;
import static utility.FactoryBot.*;
import static utility.RocketBot.*;

public class Player
{
    public static void main(String[] args)
    {
        initializeGlobals();

        // Queue researches
        if (gc.planet() == Planet.Mars)
        {
            for (int i = 0; i < 10; i++)
            {
                gc.queueResearch(RESEARCH_QUEUE_HARD[i]);
            }
        }
        String time = "";
        int lastTime = gc.getTimeLeftMs();

        while (true)
        {
            currentRound = gc.round();
            int timeLeftMs = gc.getTimeLeftMs();
            if (currentRound > 1)
            {
                time += "Time taken in round " + (currentRound - 1) + " : " + (lastTime - timeLeftMs) + "\n";
            }
            lastTime = timeLeftMs + 50;
            if (currentRound % 250 == 2)
            {
                System.out.println(time);
                time = "";
            }
            if(switchToPrimitiveMind(currentRound, timeLeftMs) && currentRound < 700)
            {
                botIntelligenceLevel = 0;
            }
            else
            {
                botIntelligenceLevel = 1;
            }
            // Clear unit lists
            for (int i = 0; i < unitTypes.length; i++)
            {
                typeSortedUnitLists.get(unitTypes[i]).clear();
            }

            // Fetch current units and sort by type
            VecUnit units = gc.myUnits();
            for (int i = 0; i < units.size(); i++)
            {
                Unit unit = units.get(i);
                typeSortedUnitLists.get(unit.unitType()).add(unit);
            }

            if (homePlanet == Planet.Earth)
            {
                removeObsoleteMines();
                removeObsoleteBlueprints();
            }

            // Process units
            for (int i = 0; i < unitTypes.length; i++)
            {
                unitList = typeSortedUnitLists.get(unitTypes[i]);
                for (int u = 0; u < unitList.size(); u++)
                {
                    Unit unit = unitList.get(u);
                    Location unitLocation = unit.location();
                    // Process active unit only
                    if (!unitLocation.isInGarrison() && !unitLocation.isInSpace())
                    {
                        if (unitTypes[i] == UnitType.Worker)
                        {
                            processWorker(unit, unitLocation);
                        }
                        if (unitTypes[i] == UnitType.Knight)
                        {
                            processKnight(unit, unitLocation);
                        }
                        if (unitTypes[i] == UnitType.Ranger)
                        {
                            processRanger(unit, unitLocation);
                        }
                        if (unitTypes[i] == UnitType.Mage)
                        {
                            processMage(unit, unitLocation);
                        }
                        if (unitTypes[i] == UnitType.Healer)
                        {
                            processHealer(unit, unitLocation);
                        }
                        if (unitTypes[i] == UnitType.Factory)
                        {
                            processFactory(unit, unitLocation);
                        }
                        if (unitTypes[i] == UnitType.Rocket)
                        {
                            processRocket(unit, unitLocation);
                        }
                    }
                }
            }

            // Submit the actions we've done, and wait for our next turn.
            gc.nextTurn();
        }
    }
}
