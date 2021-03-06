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

import static utility.Movement.*;


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
//            if (currentRound > 1)
//            {
//                time += "Time taken in round " + (currentRound - 1) + " : " + (lastTime - timeLeftMs) + "\n";
//            }
            lastTime = timeLeftMs + 50;
            if (currentRound % 50 == 0)
            {
                System.runFinalization();
                System.gc();
            }
//            if (currentRound % 250 == 2)
//            {
//                System.out.println(time);
//                time = "";
//            }

            // Clear unit lists
            for (int i = 0; i < unitTypes.length; i++)
            {
                typeSortedUnitLists.get(unitTypes[i]).clear();
            }

            // Clear enemy hashmap
            enemyVecUnits.clear();

            // Clear friendly units hashing
            //friendlyVecUnits.clear();

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
                    // VecUnit visibleFriendlyUnits = gc.senseNearbyUnitsByTeam(unitLocation.mapLocation(), unit.visionRange(), ourTeam);
                    // friendlyVecUnits.put(unit.id(), visibleFriendlyUnits);
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
                    MapLocation enemyLocationAverage = mapLocationAt[(int) xAverage][(int) yAverage];
                    if (visibleEnemyUnits.size() != 0 && homeMap.isPassableTerrainAt(enemyLocationAverage) == 1)
                    {
                        enemyLocationAverages.add(mapLocationAt[(int) xAverage][(int) yAverage]);
                    }
                    else if (visibleEnemyUnits.size() != 0)
                    {
                        enemyLocationAverages.add(mapLocationAt[visibleEnemyUnits.get(0).location().mapLocation().getX()][visibleEnemyUnits.get(0).location().mapLocation().getY()]);
                    }
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
                removeObsoleteBuilders();
            }

            if (currentRound % 10 == 0)
            {
                setBuilderFraction();
//                System.out.println(currentRound);
//                System.out.println(builderFraction);
//                System.out.println(builderSet.size());
//                System.out.println(typeSortedUnitLists.get(UnitType.Worker).size());
//                System.out.println();
                if (switchToPrimitiveMind(currentRound, timeLeftMs) && currentRound < 700)
                {
                    botIntelligenceLevel = 0;
                }
                else
                {
                    botIntelligenceLevel = 1;
                }
            }

            // Process build queue
            setWorkersRequired();
            while (shouldQueueWorker())
            {
                addUnitToQueue(UnitType.Worker);
                // System.out.println("Worker");
            }
            setFactoriesRequired();
            while (shouldQueueFactory())
            {
                addUnitToQueue(UnitType.Factory);
                // System.out.println("Fac");
            }
            setRocketsRequired();
            while (shouldQueueRocket())
            {
                addUnitToQueue(UnitType.Rocket);
                // System.out.println("Roc");
            }
            setKnightsRequired();
            while (shouldQueueKnight())
            {
                addUnitToQueue(UnitType.Knight);
                // System.out.println("Kni");
            }
            setRangersRequired();
            while (shouldQueueRanger())
            {
                addUnitToQueue(UnitType.Ranger);
                // System.out.println("Ran");
            }
            setMagesRequired();
            while (shouldQueueMage())
            {
                addUnitToQueue(UnitType.Mage);
                // System.out.println("Mag");
            }
            setHealersRequired();
            while (shouldQueueHealer())
            {
                addUnitToQueue(UnitType.Healer);
                // System.out.println("Heal");
            }
//            System.out.println(currentRound);
//            System.out.println(buildQueue.peekFirst());
//            System.out.println();
//            System.out.println(workersRequired);
//            System.out.println(unitsInQueue[UnitType.Worker.ordinal()]);
//            System.out.println(factoriesRequired);
//            System.out.println(unitsInQueue[UnitType.Factory.ordinal()]);
//            System.out.println(rocketsRequired);
//            System.out.println(typeSortedUnitLists.get(UnitType.Rocket).size());
//            System.out.println(unitsInQueue[UnitType.Rocket.ordinal()]);
//            System.out.println();

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
