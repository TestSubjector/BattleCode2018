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
                    // increase appeal
                    modifyAdjacentFactoryAppeal(blueprintMapLocation, -WEIGHT_STRUCTURE);
                }
                else
                {
                    Unit unitAtLocation = gc.senseUnitAtLocation(blueprintMapLocation);
                    if (unitAtLocation.unitType() == UnitType.Factory ||
                            unitAtLocation.unitType() == UnitType.Rocket)
                    {
                        if (unitAtLocation.structureIsBuilt() == 1)
                        {
                            obsoleteBlueprints.add(blueprint);
                        }
                    }
                    else
                    {
                        // increase appeal
                        modifyAdjacentFactoryAppeal(blueprintMapLocation, -WEIGHT_STRUCTURE);
                    }
                }
            }
        }
        for (Unit obsoleteBlueprint : obsoleteBlueprints)
        {
            unfinishedBlueprints.remove(obsoleteBlueprint);
        }
    }

    // TODO - Make building locally optimized instead of globally
    private static void processBuilder(Unit unit, Location unitLocation, MapLocation unitMapLocation)
    {
        // Blueprint structures
        if (unit.workerHasActed() == 0)
        {
            UnitType blueprintType = null;
            if (typeSortedUnitLists.get(UnitType.Factory).size() < 8)
            {
                // Blueprint a factory
                blueprintType = UnitType.Factory;
            }
            else if (typeSortedUnitLists.get(UnitType.Rocket).size() < 6)
            {
                // Blueprint a rocket
                blueprintType = UnitType.Rocket;
            }
            if (blueprintType != null)
            {
                Direction blueprintDirection = directions[0];
                for (int j = 0; j < directions.length - 1; j++)
                {
                    Direction candidateDirection = directions[j];
                    MapLocation candidateMapLocation = unitMapLocation.add(candidateDirection);
                    long maxAppeal = -1000L;
                    if (homeMap.onMap(candidateMapLocation) && homeMap.isPassableTerrainAt(candidateMapLocation) != 0)
                    {
                        long appeal = potentialFactorySpots.get(candidateMapLocation.getX()).get(candidateMapLocation.getY());
                        if (appeal > maxAppeal)
                        {
                            blueprintDirection = candidateDirection;
                            maxAppeal = appeal;
                        }
                    }
                }
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

        // TODO - Remember previous target
        if (unit.movementHeat() < 10)
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
                    moveUnitTo(unit, nearestMineMapLocation);
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

        try
        {
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
        }
        catch (Exception e)
        {
            System.out.println(unit.unitType());
            System.out.println(unitMapLocation);
        }

        if (builderSet.contains(unit.id()))
        {
            processBuilder(unit, unitLocation, unitMapLocation);
        }
        else
        {
            processMiner(unit, unitLocation, unitMapLocation);
        }

        // Replicate worker
        // TODO - To stop worker Replication when required
        if(currentRound > 250 && currentKarbonite <100 && makeRocketArmada(totalUnits))
        {
            return;
        }
        if (unitList.size() < Math.sqrt(currentRound) * 5)
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
                            unitList.add(newWorker);
                            // TODO - Remove builders when dead
                            if (unitList.size() * builderFraction > builderSet.size())
                            {
                                builderSet.add(newWorker.id());
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

    // Called only from Earth
    public static void modifyAdjacentFactoryAppeal(MapLocation mapLocation, long amount)
    {
        for (int i = 0; i < directions.length - 1; i++)
        {
            MapLocation adjacentMapLocation = mapLocation.add(directions[i]);
            if (homeMap.onMap(adjacentMapLocation) && homeMap.isPassableTerrainAt(adjacentMapLocation) == 1)
            {
                int x = adjacentMapLocation.getX();
                int y = adjacentMapLocation.getY();
                long current = potentialFactorySpots.get(x).get(y);
                potentialFactorySpots.get(x).set(y, current + amount);}
        }
    }
}
