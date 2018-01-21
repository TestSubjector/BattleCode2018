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
import static utility.DecisionTree.*;


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
            currentKarbonite = gc.karbonite();
            int timeLeftMs = gc.getTimeLeftMs();
            if (currentRound > 1)
            {
                time += "Time taken in round " + (currentRound - 1) + " : " + (lastTime - timeLeftMs) + "\n";
            }
            lastTime = timeLeftMs + 50;
            if (currentRound % 50 == 0)
            {
                System.gc();
            }
            if (currentRound % 250 == 2)
            {
                System.out.println(time);
                time = "";
            }

            if(currentRound % 5 == 0 || currentRound < 181)
            {
                currentBuilderFraction();
            }

            if (switchToPrimitiveMind(currentRound, timeLeftMs) && currentRound < 700)
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

            if( currentRound > 200 && rocketProductionCooldown % 40 == 0)
            {
                findRocketProductionCooldown();
            }
            prepareRocketArmada = currentRound > 250 && currentKarbonite < 150 && makeRocketArmada(totalUnits);

            // Clear enemy hashmap
            enemyVecUnits.clear();

            // Fetch current units and sort by type
            VecUnit units = gc.myUnits();
            for (int i = 0; i < units.size(); i++)
            {
                Unit unit = units.get(i);
                Location unitLocation = unit.location();
                typeSortedUnitLists.get(unit.unitType()).add(unit);
                if (!unitLocation.isInGarrison() && !unitLocation.isInSpace())
                {
                    VecUnit visibleEnemyUnits = gc.senseNearbyUnitsByTeam(unitLocation.mapLocation(), unit.visionRange(), theirTeam);
                    enemyVecUnits.put(unit.id(), visibleEnemyUnits);
                    double xAverage = 0;
                    double yAverage = 0;
                    for (int j = 0; j < visibleEnemyUnits.size(); j++)
                    {
                        Unit visibleEnemyUnit = visibleEnemyUnits.get(j);
                        xAverage += visibleEnemyUnit.location().mapLocation().getX();
                        yAverage += visibleEnemyUnit.location().mapLocation().getY();
                    }
                    xAverage /= visibleEnemyUnits.size();
                    yAverage /= visibleEnemyUnits.size();
                    enemyLocationAverages.add(mapLocationAt[(int)xAverage][(int)yAverage]);
                }
            }

            // Maintain a total of the number of combat/non-combat units we have
            totalCombatUnits = typeSortedUnitLists.get(UnitType.Ranger).size() + typeSortedUnitLists.get(UnitType.Healer).size() +
                    typeSortedUnitLists.get(UnitType.Knight).size() + typeSortedUnitLists.get(UnitType.Mage).size();
            totalUnits = totalCombatUnits + typeSortedUnitLists.get(UnitType.Worker).size() +
                    typeSortedUnitLists.get(UnitType.Factory).size() + typeSortedUnitLists.get(UnitType.Rocket).size();


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
