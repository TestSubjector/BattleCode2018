package utility;

import java.util.*;

import bc.*;

import static utility.Globals.*;
import static utility.FactoryBot.*;

public class RocketBot
{
    // Update the appeals of tiles surrounding the updated MapLocation
    // Only called from Earth
    // We can add a positive weight if we spot a low density enemy area
    // on Mars and convey this information back to Earth
    public static void updateSurroundingAppeal(QueuePair<Long, MapLocation> destinationPair)
    {
        MapLocation destinationMapLocation = destinationPair.getSecond();
        for (int i = 0; i < directions.length - 1; i++)
        {
            MapLocation adjacentMapLocation = destinationMapLocation.add(directions[i]);
            if (awayMap.onMap(adjacentMapLocation) && awayMap.isPassableTerrainAt(adjacentMapLocation) == 1)
            {
                updatedAppealSites.add(0, new QueuePair<>(destinationPair.getFirst() - WEIGHT_ROCKET_ON_MARS, destinationMapLocation));
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
            if (unit.structureGarrison().size() >= unit.structureMaxCapacity() / 2)
            {
                QueuePair<Long, MapLocation> destPair = potentialLandingSites.poll();
                boolean isOutdated = true;
                while (isOutdated)
                {
                    isOutdated = false;
                    for (int j = 0; j < updatedAppealSites.size(); j++)
                    {
                        if (updatedAppealSites.get(j).getSecond().equals(destPair.getSecond())
                                && !(updatedAppealSites.get(j).getFirst().equals(destPair.getFirst())))
                        {
                            isOutdated = true;
                            destPair = potentialLandingSites.poll();
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
                    gc.launchRocket(unit.id(), dest);
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
