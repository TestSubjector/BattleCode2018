package utility;

import java.util.*;

import bc.*;

import static utility.Globals.*;

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

    public static void processRocket(Unit unit, Location unitLocation)
    {

    }
}
