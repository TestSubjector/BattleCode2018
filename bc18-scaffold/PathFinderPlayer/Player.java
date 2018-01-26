import java.util.*;

import bc.*;

import utility.QueuePair;

import static utility.Globals.*;
import static utility.Combat.*;
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
        if (gc.planet() == Planet.Earth)
        {
//            if(rangerMeta)
//            {
//                for (int i = 0; i < RESEARCH_QUEUE_HARD_RANGER_META.length; i++)
//                {
//                    gc.queueResearch(RESEARCH_QUEUE_HARD_RANGER_META[i]);
//                }
//            }
//            else
//            {
//                for (int i = 0; i < RESEARCH_QUEUE_HARD_KNIGHT_META.length; i++)
//                {
//                    gc.queueResearch(RESEARCH_QUEUE_HARD_KNIGHT_META[i]);
//                }
//            }
            for (int i = 0; i < RESEARCH_QUEUE_HARD_KNIGHT_META.length; i++)
            {
                gc.queueResearch(RESEARCH_QUEUE_HARD_KNIGHT_META[i]);
            }
        }

        while (true)
        {
            currentRound = gc.round();
            int timeLeftMs = gc.getTimeLeftMs();
            if (currentRound > 1)
            {
                // System.out.println("Time to reach is " + rocketConstant);
                // time += "Time taken in round " + (currentRound - 1) + " : " + (lastTime - timeLeftMs) + "\n";
            }
            if (currentRound % 50 == 0)
            {
                System.runFinalization();
                System.gc();
            }

            if (homePlanet == Planet.Mars)
            {
                if (asteroidPattern.hasAsteroid(currentRound + 1))
                {
                    AsteroidStrike asteroidStrike = asteroidPattern.asteroid(currentRound + 1);
                    MapLocation asteroidLocation = asteroidStrike.getLocation();
                    boolean isReachable = false;
                    for (int j = 0; j < directions.length - 1; j++)
                    {
                        if (homeMap.onMap(asteroidLocation.add(directions[j])) &&
                                homeMap.isPassableTerrainAt(asteroidLocation.add(directions[j])) == 1)
                        {
                            isReachable = true;
                            break;
                        }
                    }
                    if (isReachable)
                    {
                        karboniteLocations.add(asteroidLocation);
                    }
                }
            }

            // Clear unit lists
            for (int i = 0; i < unitTypes.length; i++)
            {
                typeSortedUnitLists.get(unitTypes[i]).clear();
            }

            // Clear enemy hashmap
            enemyVecUnits.clear();
            sniperRoost.clear();
            removeObsoleteMines();
            if (homePlanet == Planet.Earth)
            {
                removeObsoleteBlueprints();
                removeObsoleteEnemyFactories();
                removeObsoleteEnemyHotspots();
            }

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
                    if (visibleEnemyUnits.size() != 0)
                    {
                        double xAverage = 0;
                        double yAverage = 0;
                        double priority = 0;
                        for (int j = 0; j < visibleEnemyUnits.size(); j++)
                        {
                            Unit visibleEnemyUnit = visibleEnemyUnits.get(j);
                            MapLocation mapLocation = getConstantMapLocationRepresentation(visibleEnemyUnit.location().mapLocation());
                            if (visibleEnemyUnit.unitType() == UnitType.Factory &&
                                    !enemyFactories.contains(mapLocation))
                            {
                                enemyFactories.add(mapLocation);
                            }
                            xAverage += visibleEnemyUnit.location().mapLocation().getX();
                            yAverage += visibleEnemyUnit.location().mapLocation().getY();
                            priority += getEnemyUnitPriority(visibleEnemyUnit.unitType());
                        }
                        if (enemyHotspots.size() < 5)
                        {
                            xAverage /= visibleEnemyUnits.size();
                            yAverage /= visibleEnemyUnits.size();
                            MapLocation enemyLocationAverage = mapLocationAt[(int) xAverage][(int) yAverage];
                            QueuePair<Double, MapLocation> qp = new QueuePair<Double, MapLocation>(priority, enemyLocationAverage);
                            enemyHotspots.add(qp);
                        }
                        else
                        {
                            QueuePair<Double, MapLocation> minQP = null;
                            double minPriority = 10000;
                            for (QueuePair<Double, MapLocation> enemyHotspot : enemyHotspots)
                            {
                                if (minPriority > enemyHotspot.getFirst())
                                {
                                    minPriority = enemyHotspot.getFirst();
                                    minQP = enemyHotspot;
                                }
                            }
                            if (minPriority < priority)
                            {
                                xAverage /= visibleEnemyUnits.size();
                                yAverage /= visibleEnemyUnits.size();
                                MapLocation enemyLocationAverage = mapLocationAt[(int) xAverage][(int) yAverage];
                                QueuePair<Double, MapLocation> qp = new QueuePair<Double, MapLocation>(priority, enemyLocationAverage);
                                enemyHotspots.remove(minQP);
                                enemyHotspots.add(qp);
                            }
                        }
                    }
                }
            }
            Collections.sort(enemyHotspots);

            // Maintain a total of the number of combat/non-combat units we have
            totalCombatUnits = typeSortedUnitLists.get(UnitType.Ranger).size() + typeSortedUnitLists.get(UnitType.Healer).size() +
                    typeSortedUnitLists.get(UnitType.Knight).size() + typeSortedUnitLists.get(UnitType.Mage).size();
            totalUnits = totalCombatUnits + typeSortedUnitLists.get(UnitType.Worker).size();

            if (currentRound % 10 == 0)
            {
                if (switchToPrimitiveMind(currentRound, timeLeftMs) && currentRound < 700)
                {
                    botIntelligenceLevel = 0;
                }
                else
                {
                    botIntelligenceLevel = 1;
                }
            }

            // Process build and train queues
            int workerCost = 30;
            int structureCosts = 0;
            if (homePlanet == Planet.Earth)
            {
                setFactoriesRequired();
                while (shouldQueueFactory())
                {
                    addUnitToBuildQueue(UnitType.Factory);
                }
                setRocketsRequired();
                while (shouldQueueRocket())
                {
                    addUnitToBuildQueue(UnitType.Rocket);
                }
                int buildQueueSize = buildQueue.size();
                for (int j = 0; j < buildQueueSize; j++)
                {
                    structureCosts += (buildQueue.peekFirst() == UnitType.Factory) ? 200 : 150;
                    buildQueue.addLast(buildQueue.removeFirst());
                }
                if (structureCosts <= gc.karbonite())
                {
                    setWorkersRequired();
                    while (shouldQueueWorker())
                    {
                        addUnitToTrainQueue(UnitType.Worker);
                    }
                    setKnightsRequired();
                    while (shouldQueueKnight())
                    {
                        addUnitToTrainQueue(UnitType.Knight);
                    }
                    setRangersRequired();
                    while (shouldQueueRanger())
                    {
                        addUnitToTrainQueue(UnitType.Ranger);
                    }
                    setMagesRequired();
                    while (shouldQueueMage())
                    {
                        addUnitToTrainQueue(UnitType.Mage);
                    }
                    setHealersRequired();
                    while (shouldQueueHealer())
                    {
                        addUnitToTrainQueue(UnitType.Healer);
                    }
                    while (typeSortedUnitLists.get(UnitType.Factory).size() * 40 + workerCost + structureCosts <= gc.karbonite() &&
                            shouldQueueWorker())
                    {
                        addUnitToTrainQueueUrgently(UnitType.Worker);
                        workerCost += 60;
                    }
                }
            }
            else
            {
                setWorkersRequired();
                while (shouldQueueWorker())
                {
                    addUnitToTrainQueue(UnitType.Worker);
                }
            }
//            System.out.println(currentRound);
//            System.out.println("Build Queue : " + buildQueue.peekFirst());
//            System.out.println("Factories required : " + factoriesRequired);
//            System.out.println("Train Queue : " + trainQueue.peekFirst());

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
                            if (!factoryIDsByBuildOrder.contains(unit.id()))
                            {
                                factoryIDsByBuildOrder.add(unit.id());
                            }
                        }
                        if (unitTypes[i] == UnitType.Rocket)
                        {
                            processRocket(unit, unitLocation);
                        }
                    }
                }
            }
            for (int i = factoryIDsByBuildOrder.size() - 1; i >= 0 ; i--)
            {
                int ID = factoryIDsByBuildOrder.get(i);
                unitList = typeSortedUnitLists.get(UnitType.Factory);
                for (int j = 0; j < unitList.size(); j++)
                {
                    Unit unit = unitList.get(j);
                    Location unitLocation = unit.location();
                    if (unit.id() == ID)
                    {
                        processFactory(unit, unitLocation);
                    }
                }
            }
            processSnipe();
            // Submit the actions we've done, and wait for our next turn.
            gc.nextTurn();
        }
    }
}
