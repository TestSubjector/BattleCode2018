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
                // time += "Time taken in round " + (currentRound - 1) + " : " + (lastTime - timeLeftMs) + "\n";
            }
            lastTime = timeLeftMs + 50;
            if (currentRound % 50 == 0)
            {
                System.runFinalization();
                System.gc();
            }
            if (currentRound % 150 == 2)
            {
                //System.out.println(time);
                // time = "";
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
            enemyHotspots.clear();

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
            Collections.sort(enemyHotspots);

            // Maintain a total of the number of combat/non-combat units we have
            totalCombatUnits = typeSortedUnitLists.get(UnitType.Ranger).size() + typeSortedUnitLists.get(UnitType.Healer).size() +
                    typeSortedUnitLists.get(UnitType.Knight).size() + typeSortedUnitLists.get(UnitType.Mage).size();
            totalUnits = totalCombatUnits + typeSortedUnitLists.get(UnitType.Worker).size() +
                    typeSortedUnitLists.get(UnitType.Factory).size() + typeSortedUnitLists.get(UnitType.Rocket).size();


            removeObsoleteMines();
            if (homePlanet == Planet.Earth)
            {
                removeObsoleteBlueprints();
                removeObsoleteBuilders();
                removeObsoleteEnemyFactories();
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
                    System.out.println("Switching to primitive mind");
                    botIntelligenceLevel = 0;
                }
                else
                {
                    botIntelligenceLevel = 1;
                }
            }

            // TODO - add stagnation
            // Process build and train queues
            if (homePlanet == Planet.Earth)
            {
                if (currentRound == 650)
                {
                    trainQueue.clear();
                    buildQueue.clear();
                }
                if (currentRound >= 650)
                {
                    setRocketsRequired();
                    while (shouldQueueRocket())
                    {
                        addUnitToBuildQueue(UnitType.Rocket);
                        // System.out.println("Roc");
                    }
                }
                else
                {
                    setKnightsRequired();
                    while (shouldQueueKnight())
                    {
                        addUnitToTrainQueue(UnitType.Knight);
                        // System.out.println("Kni");
                    }
                    setRangersRequired();
                    while (shouldQueueRanger())
                    {
                        addUnitToTrainQueue(UnitType.Ranger);
                        // System.out.println("Ran");
                    }
                    setMagesRequired();
                    while (shouldQueueMage())
                    {
                        addUnitToTrainQueue(UnitType.Mage);
                        // System.out.println("Mag");
                    }
                    setHealersRequired();
                    while (shouldQueueHealer())
                    {
                        addUnitToTrainQueue(UnitType.Healer);
                        // System.out.println("Heal");
                    }
                    setFactoriesRequired();
                    while (shouldQueueFactory())
                    {
                        addUnitToBuildQueue(UnitType.Factory);
                        // System.out.println("Fac");
                    }
                    setRocketsRequired();
                    while (shouldQueueRocket())
                    {
                        addUnitToBuildQueue(UnitType.Rocket);
                        // System.out.println("Roc");
                    }
                    if (trainQueue.isEmpty())
                    {
                        addUnitToTrainQueue(UnitType.Ranger);
                    }
                }
            }
            setWorkersRequired();
            int workerCost = 30;
            int structureCosts = 0;
            int buildQueueSize = buildQueue.size();
            for (int j = 0; j < buildQueueSize; j++)
            {
                structureCosts += (buildQueue.peekFirst() == UnitType.Factory) ? 100 : 75;
                buildQueue.addLast(buildQueue.removeFirst());
            }
            while (typeSortedUnitLists.get(UnitType.Factory).size() * 20 + workerCost + structureCosts <= gc.karbonite())
            {
                addUnitToTrainQueueUrgently(UnitType.Worker);
                workerCost += 30;
            }
            while (shouldQueueWorker())
            {
                addUnitToTrainQueue(UnitType.Worker);
                // System.out.println("Worker");
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
