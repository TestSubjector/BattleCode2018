package utility;

import java.util.*;

import bc.*;

import static utility.Globals.*;
import static utility.FactoryBot.*;
import static utility.DecisionTree.*;

public class RocketBot
{
    // Update the appeals of tiles surrounding the updated MapLocation
    // Only called from Earth
    // We can add a positive weight if we spot a low density enemy area
    // on Mars and convey this information back to Earth
    public static void updateSurroundingAppeal(QueuePair<Long, MapLocation> destinationPair)
    {
        MapLocation destinationMapLocation = destinationPair.getSecond();
        // directions will not use CENTER because < length-1 and center is the last one
        for (int i = 0; i < directions.length - 1; i++)
        {
            MapLocation adjacentMapLocation = destinationMapLocation.add(directions[i]);
            if (awayMap.onMap(adjacentMapLocation) && awayMap.isPassableTerrainAt(adjacentMapLocation) == 1)
            {
                updatedAppealSites.add(0, new QueuePair<>(destinationPair.getFirst() - WEIGHT_ROCKET_ON_MARS, adjacentMapLocation));
            }
        }
    }


    public static QueuePair<Long, MapLocation> getBestDestinationPair()
    {
        QueuePair<Long, MapLocation> destinationPair = potentialLandingSites.poll();
        boolean isOutdated = true;
        while (isOutdated)
        {
            isOutdated = false;
            for (int j = 0; j < updatedAppealSites.size(); j++)
            {
                if (updatedAppealSites.get(j).getSecond().equals(destinationPair.getSecond())
                        && !(updatedAppealSites.get(j).getFirst().equals(destinationPair.getFirst())))
                {
                    isOutdated = true;
                    destinationPair = potentialLandingSites.poll();
                    break;
                }
            }
        }
        return destinationPair;
    }

    public static void processEarthRocket(Unit unit, Location unitLocation)
    {
        if (unit.structureIsBuilt() == 1)
        {
            rocketPositions.add(unitLocation.mapLocation());
            rocketLaunchTime.put(unit.id(), timeToIdealRocketLaunch());
            // Check all adjacent squares
            VecUnit nearbyUnits = gc.senseNearbyUnitsByTeam(unit.location().mapLocation(), 2, ourTeam);
            for (int j = 0; j < nearbyUnits.size(); j++)
            {
                Unit nearbyUnit = nearbyUnits.get(j);
                if (gc.canLoad(unit.id(), nearbyUnit.id()))
                {
                    gc.load(unit.id(), nearbyUnit.id());
                }
            }
            if (((unit.health() < 200) || (unit.structureGarrison().size() == 8) || currentRound > 748) ||
                    ((unit.structureGarrison().size() >= 2 * unit.structureMaxCapacity() / 3 ) && (currentRound >= rocketLaunchTime.get(unit.id()))))
            {
                QueuePair<Long, MapLocation> destPair = potentialLandingSites.poll();
//                System.out.println("potentialLandingSites head : " + destPair.toString());
                boolean isOutdated = true;
                while (isOutdated)
                {
                    isOutdated = false;
                    for (int j = 0; j < updatedAppealSites.size(); j++)
                    {
//                        System.out.println("Testing " + destPair.toString() + " against updated " + updatedAppealSites.get(j).toString());
                        if (updatedAppealSites.get(j).getSecond().equals(destPair.getSecond())
                                && !(updatedAppealSites.get(j).getFirst().equals(destPair.getFirst())))
                        {
                            isOutdated = true;
                            destPair = potentialLandingSites.poll();
//                            System.out.println("destPair outdated. New pair : " + destPair.toString());
                            break;
                        }
                    }
                }

                MapLocation dest = destPair.getSecond();
                // potentialLandingSites is supposed to have only those spots
                // that are passable, and not already used as a destination.
                // Hence, this check should always pass.
                if (gc.canLaunchRocket(unit.id(), dest))
                {
//                    System.out.println("Final launch choice : " + destPair.toString());
                    gc.launchRocket(unit.id(), dest);
                    rocketLaunchTime.remove(unit.id());
                    rocketPositions.remove(unitLocation.mapLocation());
                    updateSurroundingAppeal(destPair);
                }
            }
        }
    }

    public static void processMarsRocket(Unit unit, Location unitLocation)
    {
        for (int j = 0; j < directions.length; j++)
        {
            Direction unloadDirection = directions[j];
            if (unloadDirection == Direction.Center)
            {
                continue;
            }
            if (gc.canUnload(unit.id(), unloadDirection))
            {
                gc.unload(unit.id(), unloadDirection);
                break;
            }
        }
    }

    public static void processRocket(Unit unit, Location unitLocation)
    {
        if (homePlanet == Planet.Earth)
        {
            processEarthRocket(unit, unitLocation);
        }
        else
        {
            processMarsRocket(unit, unitLocation);
        }
    }
}
