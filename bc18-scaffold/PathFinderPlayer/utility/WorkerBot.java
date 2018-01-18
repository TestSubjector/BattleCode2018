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
        if (unitList.size() < Math.sqrt(currentRound))
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
                    break;
                }
            }
        }

        // Blueprint structures
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

        // Move towards nearest blueprint
        Unit nearestStructure = null;
        long minDistanceSquared = 100000L;
        for (Unit blueprint : unfinishedBlueprints)
        {
            long distanceSquaredToStructure = blueprint.location().mapLocation().distanceSquaredTo(unitMapLocation);
            if (distanceSquaredToStructure < minDistanceSquared)
            {
                nearestStructure = blueprint;
                minDistanceSquared = distanceSquaredToStructure;
            }
        }
        if (nearestStructure != null)
        {
            moveUnitTowards(unit, nearestStructure.location().mapLocation());
        }

        // Move towards nearest mine
        MapLocation nearestMineMapLocation = null;
        minDistanceSquared = 100000L;
        for (MapLocation karboniteMapLocation : earthKarboniteLocations)
        {
            long distanceSquaredToMine = karboniteMapLocation.distanceSquaredTo(unitMapLocation);
            if (distanceSquaredToMine < minDistanceSquared)
            {
                nearestMineMapLocation = karboniteMapLocation;
                minDistanceSquared = distanceSquaredToMine;
            }
        }
        if (nearestMineMapLocation != null)
        {
            moveUnitTowards(unit, nearestMineMapLocation);
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
}
