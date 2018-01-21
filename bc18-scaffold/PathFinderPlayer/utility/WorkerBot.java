package utility;

import java.util.*;

import bc.*;

import static utility.Globals.*;
import static utility.DecisionTree.*;
import static utility.Movement.*;

public class WorkerBot
{
    public static void removeObsoleteMines()
    {
        // Remove obsolete mine locations
        LinkedList<MapLocation> obsoleteMines = new LinkedList<MapLocation>();
        for (MapLocation karboniteMapLocation : earthKarboniteLocations)
        {
            if (gc.canSenseLocation(karboniteMapLocation) &&
                    gc.karboniteAt(karboniteMapLocation) == 0)
            {
                obsoleteMines.add(karboniteMapLocation);
            }
        }
        for (MapLocation obsoleteMine : obsoleteMines)
        {
            earthKarboniteLocations.remove(obsoleteMine);
        }
    }

    public static void removeObsoleteBlueprints()
    {
        LinkedList<Unit> obsoleteBlueprints = new LinkedList<Unit>();
        for (Unit blueprint : unfinishedBlueprints)
        {
            MapLocation blueprintMapLocation = blueprint.location().mapLocation();
            if (gc.canSenseLocation(blueprintMapLocation))
            {
                if (!gc.hasUnitAtLocation(blueprintMapLocation))
                {
                    obsoleteBlueprints.add(blueprint);
                }
                else
                {
                    Unit unitAtLocation = gc.senseUnitAtLocation(blueprintMapLocation);
                    if (unitAtLocation.unitType() == UnitType.Factory ||
                            unitAtLocation.unitType() == UnitType.Rocket && (unitAtLocation.structureIsBuilt() == 1))
                    {
                        obsoleteBlueprints.add(blueprint);
                    }
                }
            }
        }
        for (Unit obsoleteBlueprint : obsoleteBlueprints)
        {
            if(obsoleteBlueprint.unitType() == UnitType.Rocket)
            {
                rocketProductionCooldown++;
            }
            unfinishedBlueprints.remove(obsoleteBlueprint);
        }
    }
    
    private static void processBuilder(Unit unit, Location unitLocation, MapLocation unitMapLocation, VecUnit adjacentUnits)
    {
        // Blueprint structures
        if (unit.workerHasActed() == 0)
        {
            UnitType blueprintType = null;
            if (typeSortedUnitLists.get(UnitType.Factory).size() < factoryLimit && !prepareRocketArmada)
            {
                // Blueprint a factory
                blueprintType = UnitType.Factory;
            }
            else if (typeSortedUnitLists.get(UnitType.Rocket).size() < maxRocketLimitAtTurn(totalUnits))
            {
                // Blueprint a rocket
                blueprintType = UnitType.Rocket;
            }

            if (blueprintType != null)
            {
                Direction blueprintDirection = directions[0];
                boolean isMovementNeeded = true; // to check if all points are choked
                long maxAppeal = -1000L; // tracking the candidateAppeal
                for (int j = 0; j < directions.length - 1; j++)
                {
                    Direction candidateDirection = directions[j];
                    MapLocation candidateMapLocation = unitMapLocation.add(candidateDirection);
                    if (homeMap.onMap(candidateMapLocation) && homeMap.isPassableTerrainAt(candidateMapLocation) != 0)
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

                // all points choked, move out
                if (isMovementNeeded) moveUnitInRandomDirection(unit); // TODO better direction decision
                else
                {
                    if (gc.canBlueprint(unit.id(), blueprintType, blueprintDirection))
                    {
                        MapLocation blueprintMapLocation = unitMapLocation.add(blueprintDirection);
                        gc.blueprint(unit.id(), blueprintType, blueprintDirection);
                        Unit newBlueprint = gc.senseUnitAtLocation(blueprintMapLocation);
                        unfinishedBlueprints.add(newBlueprint);
                        typeSortedUnitLists.get(blueprintType).add(newBlueprint);
                    }
                }
            }
        }

        boolean shouldMove = true;
        for (int j = 0; j < adjacentUnits.size(); j++)
        {
            Unit adjacentUnit = adjacentUnits.get(j);
            if (adjacentUnit.unitType() == UnitType.Rocket || adjacentUnit.unitType() == UnitType.Factory)
            {
                if(adjacentUnit.health() < adjacentUnit.maxHealth())
                {
                    shouldMove = false;
                    break;
                }
            }
        }
        // TODO - Remember previous target
        if (shouldMove && unit.movementHeat() < 10)
        {
            // Move towards nearest blueprint
            Unit nearestStructure = null;
            long minDiagonalDistance = 1000L;
            for (Unit blueprint : unfinishedBlueprints)
            {
                long diagonalDistanceToStructure = diagonalDistanceBetween(blueprint.location().mapLocation(), unitMapLocation);
                if (diagonalDistanceToStructure < minDiagonalDistance)
                {
                    nearestStructure = blueprint;
                    minDiagonalDistance = diagonalDistanceToStructure;
                }
            }
            if (nearestStructure != null)
            {
                moveUnitTo(unit, nearestStructure.location().mapLocation());
            }
        }
    }

    private static void processMiner(Unit unit, Location unitLocation, MapLocation unitMapLocation)
    {
        if (unit.movementHeat() < 10)
        {
            // Move towards nearest mine
            // TODO - Priority for good mines
            // TODO - Remember previous target
            if (gc.karboniteAt(unitMapLocation) == 0)
            {
                MapLocation nearestMineMapLocation = null;
                long minDiagonalDistance = 1000L;
                for (MapLocation karboniteMapLocation : earthKarboniteLocations)
                {
                    long diagonalDistanceToMine = diagonalDistanceBetween(karboniteMapLocation, unitMapLocation);
                    if (diagonalDistanceToMine < minDiagonalDistance)
                    {
                        nearestMineMapLocation = karboniteMapLocation;
                        minDiagonalDistance = diagonalDistanceToMine;
                        if (minDiagonalDistance <= 1)
                        {
                            break;
                        }
                    }
                }
                if (nearestMineMapLocation != null)
                {
                    if (!moveUnitTo(unit, nearestMineMapLocation))
                    {
                        earthKarboniteLocations.remove(nearestMineMapLocation);
                    }
                }
            }
        }
    }

    // TODO - Add repair
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
                else if(adjacentUnit.health() < adjacentUnit.maxHealth() && gc.canRepair(unit.id(), adjacentUnit.id()))
                {
                    gc.repair(unit.id(), adjacentUnit.id());
                    break;
                }
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

        if (builderSet.contains(unit.id()))
        {
            processBuilder(unit, unitLocation, unitMapLocation, adjacentUnits);
        }
        else
        {
            processMiner(unit, unitLocation, unitMapLocation);
        }

        if(prepareRocketArmada)
        {
            return;
        }

        // Replicate worker
        // TODO - To stop worker Replication when required
        if (unitList.size() < maxWorkerLimitAtTurn(currentRound))
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
                            if(newWorker.unitType() == UnitType.Worker)
                            {
                                unitList.add(newWorker);
                                // TODO - Remove builders when dead
                                if (unitList.size() * builderFraction > builderSet.size())
                                {
                                    builderSet.add(newWorker.id());
                                }
                            }
                        }
                    }
                    break;
                }
            }
        }
    }

    // TODO - Worker replication code
    // TODO - Inform Mars about the state
    public static void processMarsWorker(Unit unit, Location unitLocation)
    {
        MapLocation unitMapLocation = unitLocation.mapLocation();
        VecUnit adjacentUnits = gc.senseNearbyUnitsByTeam(unit.location().mapLocation(), 2, ourTeam);

        // Build a structure if adjacent to one
        for (int j = 0; j < adjacentUnits.size(); j++)
        {
            Unit adjacentUnit = adjacentUnits.get(j);
            if(adjacentUnit.health() < adjacentUnit.maxHealth() && gc.canRepair(unit.id(), adjacentUnit.id()))
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

        if(unit.workerHasActed() == 0)
        {
            moveUnitAwayFromMultipleUnits(adjacentUnits, unit);
        }

        // Replicate worker if enough Karbonite or Earth flooded
        if( currentRound > 749 || gc.karbonite() > 100)
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
                            if(newWorker.unitType() == UnitType.Worker)
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
        int blockages = 0;
        long appeal = WEIGHT_NONE;
        for (int i = 0; i < directions.length - 1; i++)
        {
            MapLocation adjacentMapLocation = mapLocation.add(directions[i]);
            if (!homeMap.onMap(adjacentMapLocation) || (homeMap.isPassableTerrainAt(adjacentMapLocation) == 0))
            {
                blockages++;
                appeal += WEIGHT_IMPASSABLE;
            }
            // not checking canSense because it should always be in vision range (max sq dist 8)
            UnitType type = gc.senseUnitAtLocation(adjacentMapLocation).unitType();
            if (type == UnitType.Factory || type == UnitType.Rocket)
            {
                blockages++;
                appeal += WEIGHT_STRUCTURE;
            }
        }

        // more than 2 (incl) in an 8 x 8 sq block the movement
        if (blockages >= 2) return -1002L;
        else return appeal;
    }
}
