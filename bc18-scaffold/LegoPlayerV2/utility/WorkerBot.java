package utility;

import java.util.*;

import bc.*;

import static utility.Globals.*;
import static utility.DecisionTree.*;
import static utility.Movement.*;

public class WorkerBot
{
    private static void moveBuilderToDesirableLocation(Unit unit, MapLocation unitMapLocation)
    {
        if (!primeFactoryLocations.isEmpty())
        {
            MapLocation nearestLocation = null;
            long minDiagonalDistance = 1000L;
            for (MapLocation mapLocation : primeFactoryLocations)
            {
                long diagonalDistanceToLocation = diagonalDistanceBetween(mapLocation, unitMapLocation);
                if (diagonalDistanceToLocation < minDiagonalDistance)
                {
                    nearestLocation = mapLocation;
                    minDiagonalDistance = diagonalDistanceToLocation;
                    if (minDiagonalDistance == 1)
                    {
                        break;
                    }
                }
            }
            if (nearestLocation != null)
            {
                long diagonalDistance = diagonalDistanceBetween(unitMapLocation, nearestLocation);
                if (diagonalDistance < 2 && gc.senseNearbyUnitsByType(nearestLocation, 30, UnitType.Factory).size() > 0)
                {
                    primeFactoryLocations.remove(nearestLocation);
                }
                if (diagonalDistance >= 2 && diagonalDistance <= 5)
                {
                    moveUnitTo(unit, nearestLocation);
                }
            }
        }
    }

    private static void processBuilder(Unit unit, Location unitLocation, MapLocation unitMapLocation, VecUnit adjacentUnits)
    {
        if (unit.movementHeat() < 10)
        {
            // Move towards nearest blueprint if in range
            MapLocation nearestStructure = null;
            long minDiagonalDistance = 1000L;
            for (MapLocation blueprint : unfinishedBlueprints)
            {
                long diagonalDistanceToStructure = diagonalDistanceBetween(blueprint, unitMapLocation);
                if (diagonalDistanceToStructure < minDiagonalDistance)
                {
                    nearestStructure = blueprint;
                    minDiagonalDistance = diagonalDistanceToStructure;
                    if (minDiagonalDistance == 1)
                    {
                        break;
                    }
                }
            }
            if (nearestStructure != null)
            {
                if (minDiagonalDistance < 6 && minDiagonalDistance > 1)
                {
                    moveUnitTo(unit, nearestStructure);
                }
                else
                {
                    int k = random.nextInt(8);
                    for (int j = 0; j < directions.length - 1; j++)
                    {
                        MapLocation adjacentSpace = nearestStructure.add(directions[(k++) % 8]);
                        if (homeMap.onMap(adjacentSpace) &&
                                homeMap.isPassableTerrainAt(adjacentSpace) == 1 &&
                                !gc.hasUnitAtLocation(adjacentSpace))
                        {
                            moveUnitTowards(unit, adjacentSpace);
                        }
                    }
                }
            }
        }
        unitLocation = unit.location();
        unitMapLocation = unitLocation.mapLocation();
        if (!buildQueue.isEmpty())
        {
            moveBuilderToDesirableLocation(unit, unitMapLocation);
            unitLocation = unit.location();
            unitMapLocation = unitLocation.mapLocation();
        }
        if (unit.workerHasActed() == 0)
        {
            // Blueprint structures
            UnitType blueprintType = null;
            if (currentRound > 650)
            {
                if (gc.karbonite() >= 75)
                {
                    blueprintType = UnitType.Rocket;
                }
            }
            else
            {
                if (buildQueue.peekFirst() == UnitType.Factory && gc.karbonite() >= 100)
                {
                    // Blueprint a factory
                    blueprintType = UnitType.Factory;
                }
                if (buildQueue.peekFirst() == UnitType.Rocket && gc.karbonite() >= 75)
                {
                    // Blueprint a rocket
                    blueprintType = UnitType.Rocket;
                }
            }
            if (blueprintType != null)
            {
                Direction blueprintDirection = directions[0];
                boolean isMovementNeeded = true; // to check if all points are choked
                long maxAppeal = -1000L; // tracking the candidateAppeal
                for (int j = 0; j < directions.length - 1; j++)
                {
                    Direction candidateDirection = directions[j];
                    MapLocation candidateMapLocation = unitMapLocation.addMultiple(candidateDirection, 1);
                    if (homeMap.onMap(candidateMapLocation) && homeMap.isPassableTerrainAt(candidateMapLocation) == 1)
                    {
                        long locAppeal = getLocationAppeal(candidateMapLocation);
                        if (locAppeal != -1002L)
                        {
                            isMovementNeeded = false; // found a viable point
                            if (locAppeal > maxAppeal)
                            {
                                blueprintDirection = candidateDirection;
                                maxAppeal = locAppeal;
                            }
                        }
                    }
                }
                if (isMovementNeeded)
                {
                    MapLocation blueprintMapLocation = null;
                    for (int i = 2; i <= 3; i++)
                    {
                        for (int j = 0; j < directions.length - 1; j++)
                        {
                            Direction candidateDirection = directions[j];
                            MapLocation candidateMapLocation = unitMapLocation.addMultiple(candidateDirection, i);
                            if (homeMap.onMap(candidateMapLocation) && homeMap.isPassableTerrainAt(candidateMapLocation) == 1)
                            {
                                long locAppeal = getLocationAppeal(candidateMapLocation);
                                if (locAppeal != -1002L)
                                {
                                    if (locAppeal > maxAppeal)
                                    {
                                        blueprintMapLocation = candidateMapLocation;
                                        maxAppeal = locAppeal;
                                    }
                                }
                            }
                        }
                    }
                    if (blueprintMapLocation != null)
                    {
                        moveUnitTo(unit, blueprintMapLocation);
                        unitLocation = unit.location();
                        unitMapLocation = unitLocation.mapLocation();
                    }
                }
                else
                {
                    if (gc.canBlueprint(unit.id(), blueprintType, blueprintDirection))
                    {
                        MapLocation blueprintMapLocation = getConstantMapLocationRepresentation(unitMapLocation.add(blueprintDirection));
                        gc.blueprint(unit.id(), blueprintType, blueprintDirection);
                        unfinishedBlueprints.add(blueprintMapLocation);
                        removeUnitFromBuildQueue();
                    }
                }
            }
        }
    }

    private static void processMiner(Unit unit, Location unitLocation, MapLocation unitMapLocation)
    {
        if (unit.movementHeat() < 10)
        {
            // Move towards nearest mine
            if (gc.karboniteAt(unitMapLocation) == 0)
            {
                MapLocation nearestMineMapLocation = null;
                long minSquaredDistance = 1000000L;
                for (MapLocation karboniteMapLocation : karboniteLocations)
                {
                    if (!(karboniteLocationBlacklists.containsKey(unit.id()) &&
                            karboniteLocationBlacklists.get(unit.id()).contains(karboniteMapLocation)))
                    {
                        long squaredDistanceToMine = karboniteMapLocation.distanceSquaredTo(unitMapLocation);
                        if (squaredDistanceToMine < minSquaredDistance)
                        {
                            nearestMineMapLocation = karboniteMapLocation;
                            minSquaredDistance = squaredDistanceToMine;
                            if (minSquaredDistance <= 1)
                            {
                                break;
                            }
                        }
                    }
                }
                if (nearestMineMapLocation != null && !moveUnitTo(unit, nearestMineMapLocation))
                {
                    if (!karboniteLocationBlacklists.containsKey(unit.id()))
                    {
                        karboniteLocationBlacklists.put(unit.id(), new HashSet<MapLocation>());
                    }
                    karboniteLocationBlacklists.get(unit.id()).add(nearestMineMapLocation);
                }
                unitLocation = unit.location();
                unitMapLocation = unitLocation.mapLocation();
                moveUnitInRandomDirection(unit);
                unitLocation = unit.location();
                unitMapLocation = unitLocation.mapLocation();
            }
        }
    }

    public static void processEarthWorker(Unit unit, Location unitLocation)
    {
        MapLocation unitMapLocation = unitLocation.mapLocation();
        VecUnit adjacentUnits = gc.senseNearbyUnitsByTeam(unit.location().mapLocation(), 2, ourTeam);

        // Build a structure if adjacent to one
        for (int j = 0; j < adjacentUnits.size(); j++)
        {
            Unit adjacentUnit = adjacentUnits.get(j);
            if (adjacentUnit.unitType() == UnitType.Factory || adjacentUnit.unitType() == UnitType.Rocket)
            {
                if (gc.canBuild(unit.id(), adjacentUnit.id()))
                {
                    gc.build(unit.id(), adjacentUnit.id());
                    break;
                }
                else if (adjacentUnit.health() < adjacentUnit.maxHealth() && gc.canRepair(unit.id(), adjacentUnit.id()))
                {
                    gc.repair(unit.id(), adjacentUnit.id());
                    break;
                }
            }
        }

        // Move away from dangerous enemies
        VecUnit nearbyEnemies = enemyVecUnits.get(unit.id());
        if (nearbyEnemies != null)
        {
            int dangerousEnemies = 0;
            for (int j = 0; j < nearbyEnemies.size(); j++)
            {
                UnitType unitType = nearbyEnemies.get(j).unitType();
                if (unitType != UnitType.Worker && unitType != UnitType.Factory && unitType != UnitType.Rocket)
                {
                    dangerousEnemies++;
                }
            }
            if (dangerousEnemies > 1)
            {
                moveUnitAwayFromMultipleUnits(unit, nearbyEnemies);
            }
        }

        processBuilder(unit, unitLocation, unitMapLocation, adjacentUnits);

        // Mine karbonite if adjacent to or standing on a mine
        for (int j = 0; j < directions.length; j++)
        {
            if (gc.canHarvest(unit.id(), directions[j]))
            {
                gc.harvest(unit.id(), directions[j]);
                break;
            }
        }

        processMiner(unit, unitLocation, unitMapLocation);

        // Replicate worker
        if (trainQueue.peekFirst() == UnitType.Worker)
        {
            for (int j = 0; j < directions.length - 1; j++)
            {
                Direction replicateDirection = directions[j];
                MapLocation replicateMapLocation = unitMapLocation.add(replicateDirection);
                if (gc.canSenseLocation(replicateMapLocation) &&
                        !gc.hasUnitAtLocation(replicateMapLocation) &&
                        gc.canReplicate(unit.id(), replicateDirection))
                {
                    gc.replicate(unit.id(), replicateDirection);
                    if (gc.hasUnitAtLocation(replicateMapLocation))
                    {
                        Unit newWorker = gc.senseUnitAtLocation(replicateMapLocation);
                        unitList.add(newWorker);
                    }
                    removeUnitFromTrainQueue();
                    break;
                }
            }
        }
    }

    public static void processMarsWorker(Unit unit, Location unitLocation)
    {
        MapLocation unitMapLocation = unitLocation.mapLocation();
        VecUnit adjacentUnits = gc.senseNearbyUnitsByTeam(unit.location().mapLocation(), 2, ourTeam);

        // Build a structure if adjacent to one
        for (int j = 0; j < adjacentUnits.size(); j++)
        {
            Unit adjacentUnit = adjacentUnits.get(j);
            if (adjacentUnit.health() < adjacentUnit.maxHealth() && gc.canRepair(unit.id(), adjacentUnit.id()))
            {
                gc.repair(unit.id(), adjacentUnit.id());
                break;
            }
        }

        if (unit.workerHasActed() == 0)
        {
            // Mine karbonite if adjacent to or standing on a mine
            for (int j = 0; j < directions.length; j++)
            {
                if (gc.canHarvest(unit.id(), directions[j]))
                {
                    gc.harvest(unit.id(), directions[j]);
                    break;
                }
            }
        }

        processMiner(unit, unitLocation, unitMapLocation);

        // Replicate worker if enough Karbonite or Earth flooded
        if (currentRound > 749 || gc.karbonite() > 200)
        {
            for (int j = 0; j < directions.length - 1; j++)
            {
                Direction replicateDirection = directions[j];
                if (gc.canReplicate(unit.id(), replicateDirection))
                {
                    gc.replicate(unit.id(), replicateDirection);
                    MapLocation replicateMapLocation = unitMapLocation.add(replicateDirection);
                    if (gc.canSenseLocation(replicateMapLocation))
                    {
                        if (gc.hasUnitAtLocation(replicateMapLocation))
                        {
                            Unit newWorker = gc.senseUnitAtLocation(replicateMapLocation);
                            if (newWorker.unitType() == UnitType.Worker)
                            {
                                unitList.add(newWorker);
                            }
                        }
                    }
                    break;
                }
            }
        }
    }

    public static void processWorker(Unit unit, Location unitLocation)
    {
        if (homePlanet == Planet.Earth)
        {
            processEarthWorker(unit, unitLocation);
        }
        else
        {
            processMarsWorker(unit, unitLocation);
        }
    }

    // Returns a long value that is either the appeal of the tile, or -1002L if the location is a choke point
    public static long getLocationAppeal(MapLocation mapLocation)
    {
        int left_blockages = 0;
        int right_blockages = 0;
        int up_block = 0;
        int down_block = 0;

        long appeal = WEIGHT_NONE;
        for (int i = 0; i < directions.length - 1; i++)
        {
            MapLocation adjacentMapLocation = mapLocation.add(directions[i]);
            UnitType type = null;
            if (gc.hasUnitAtLocation(adjacentMapLocation))
            {
                type = gc.senseUnitAtLocation(adjacentMapLocation).unitType();
            }
            if (!homeMap.onMap(adjacentMapLocation) || (homeMap.isPassableTerrainAt(adjacentMapLocation) == 0) ||
                    (gc.hasUnitAtLocation(adjacentMapLocation) && (type == UnitType.Factory || type == UnitType.Rocket)))
            {
                switch (i)
                {
                    case 0:
                        up_block++;
                        break;
                    case 1:
                        up_block++;
                        right_blockages++;
                        break;
                    case 2:
                        right_blockages++;
                        break;
                    case 3:
                        down_block++;
                        right_blockages++;
                        break;
                    case 4:
                        down_block++;
                        break;
                    case 5:
                        down_block++;
                        left_blockages++;
                        break;
                    case 6:
                        left_blockages++;
                        break;
                    case 7:
                        up_block++;
                        left_blockages++;
                        break;
                    default:
                        break; //center; will never have a blockage
                }
                appeal += WEIGHT_IMPASSABLE;
            }
        }

        int blockageSum = left_blockages + right_blockages + up_block + down_block;

        // blockage on single side only is fine
        if (blockageSum > 2)
        {
            return -1002L;
        }
        else
        {
            return appeal;
        }
    }
}
