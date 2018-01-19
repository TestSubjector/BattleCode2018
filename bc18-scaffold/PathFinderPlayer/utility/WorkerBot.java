package utility;

import java.util.*;

import bc.*;

import static utility.Globals.*;
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
                    if ((unitAtLocation.unitType() == UnitType.Factory ||
                            unitAtLocation.unitType() == UnitType.Rocket) &&
                            unitAtLocation.structureIsBuilt() == 1)
                    {
                        obsoleteBlueprints.add(blueprint);
                    }
                }
            }
        }
        for (Unit obsoleteBlueprint : obsoleteBlueprints)
        {
            unfinishedBlueprints.remove(obsoleteBlueprint);
        }
    }

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
                for (int j = 0; j < directions.length - 1; j++)
                {
                    Direction blueprintDirection = directions[j];
                    if (gc.canBlueprint(unit.id(), blueprintType, blueprintDirection))
                    {
                        gc.blueprint(unit.id(), blueprintType, blueprintDirection);
                        MapLocation blueprintMapLocation = unitMapLocation.add(blueprintDirection);
                        Unit newBlueprint = gc.senseUnitAtLocation(blueprintMapLocation);
                        unfinishedBlueprints.add(newBlueprint);
                        typeSortedUnitLists.get(blueprintType).add(newBlueprint);
                        break;
                    }
                }
            }
        }

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
            }
        }

        // Mine karbonite if adjacent to or standing on a mine
        for (int j = 0; j < directions.length; j++)
        {
            if (gc.canHarvest(unit.id(), directions[j]))
            {
                gc.harvest(unit.id(), directions[j]);
                break;
            }
        }

        // Replicate worker
        if (unitList.size() < Math.sqrt(currentRound) * 5)
        {
            for (int j = 0; j < directions.length - 1; j++)
            {
                Direction replicateDirection = directions[j];
                if (gc.canReplicate(unit.id(), replicateDirection))
                {
                    gc.replicate(unit.id(), replicateDirection);
                    MapLocation replicateMapLocation = unitMapLocation.add(replicateDirection);
                    Unit newWorker = gc.senseUnitAtLocation(replicateMapLocation);
                    unitList.add(newWorker);
                    if (unitList.size() * builderFraction > builderSet.size())
                    {
                        builderSet.add(newWorker.id());
                    }
                    break;
                }
            }
        }

        if (builderSet.contains(unit.id()))
        {
            processBuilder(unit, unitLocation, unitMapLocation);
        }
        else
        {
            processMiner(unit, unitLocation, unitMapLocation);
        }
    }

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

    public static long maxWorkerLimitAtTurn(long currentRound)
    {
        if(homeMapSize <=500)
        {
            if(currentRound < 75)
            {
                if(earthInitialTotalKarbonite > 1000)
                {
                    return 20;
                }
                else if(earthInitialTotalKarbonite > 750)
                {
                    return 15;
                }
                else if(earthInitialTotalKarbonite < 100)
                {
                    return 5;
                }
            }
            else
            {
                return 10;
            }
        }
        else if(homeMapSize <=900)
        {
            if(currentRound < 85)
            {
                if(earthInitialTotalKarbonite > 1000)
                {
                    return 20;
                }
                else if(earthInitialTotalKarbonite > 750)
                {
                    return 15;
                }
                else if(earthInitialTotalKarbonite < 100)
                {
                    return 5;
                }
            }
            else
            {
                if(earthInitialTotalKarbonite < 500)
                {
                    return 12;
                }
                else if(earthInitialTotalKarbonite > 1000)
                {
                    return 20;
                }
                else
                {
                    return 15;
                }
            }
        }
        else
        {
            if(currentRound < 75)
            {
                if(earthInitialTotalKarbonite > 3000)
                {
                    return 30;
                }
                else if(earthInitialTotalKarbonite > 1000)
                {
                    return 20;
                }
                else
                {
                    return 10;
                }
            }
            else
            {
                if(earthInitialTotalKarbonite < 500)
                {
                    return 10;
                }
                else if(earthInitialTotalKarbonite > 1500)
                {
                    return 25;
                }
                else
                {
                    return 15;
                }
            }
        }
        return 10;
    }

    public static long maxFactoryLimitAtTurn(long currentRound, long totalCombatUnits)
    {
        if (homeMapSize <= 625)
        {
            return 8;
        }
        else
        {
            return Math.round((double)homeMapSize / 100) + 1;
        }
    }

    public static boolean makeRocketArmada(long totalUnits)
    {
        return totalUnits > 4 * earthPassableTerrain * ((double)homeMapHeight + homeMapWidth) / (homeMapSize);
    }

    // Called only from Earth
    public static void modifyAdjacentFactoryAppeal(MapLocation mapLocation, long amount)
    {
        for (int i = 0; i < directions.length - 1; i++)
        {
            MapLocation adjacentMapLocation = mapLocation.add(directions[i]);
            if (homeMap.onMap(adjacentMapLocation) && homeMap.isPassableTerrainAt(adjacentMapLocation) == 1)
            {
                int x = (int)adjacentMapLocation.getX();
                int y = (int)adjacentMapLocation.getY();
                long current = potentialFactorySpots.get(x).get(y);
                potentialFactorySpots.get(x).set(y, current + amount);}
        }
    }
}
