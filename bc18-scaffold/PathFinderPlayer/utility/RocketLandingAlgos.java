package utility;

import java.util.*;

import bc.*;

import static utility.Globals.*;

public class RocketLandingAlgos
{
    // Find the appeal of the MapLocation at the given coordinates
    public static long getLocationAppeal(int x, int y)
    {
        if (x < 0 || x >= awayMap.getWidth())
        {
            return WEIGHT_IMPASSABLE;
        }

        if (y < 0 || y >= awayMap.getHeight())
        {
            return WEIGHT_IMPASSABLE;
        }

        MapLocation tempLoc = new MapLocation(Planet.Mars, x, y);

        // only called from Earth, so awayMap will be Mars
        if (awayMap.isPassableTerrainAt(tempLoc) == 0)
        {
            return WEIGHT_IMPASSABLE;
        }
        else
        {
            return WEIGHT_NONE;
        }
    }

    // Update the appeals of tiles surrounding the updated MapLocation
    public static void updateSurroundingAppeal(QueuePair<Long, MapLocation> destPair)
    {
        int temp_x = destPair.getSecond().getX();
        int temp_y = destPair.getSecond().getY();

        MapLocation tempLoc;
        for (int x = -1; x <= 1; x++)
        {
            for (int y = -1; y <= 1; y++)
            {
                if (!(x == 0 && y == 0))
                {
                    tempLoc = new MapLocation(Planet.Mars, temp_x + x, temp_y + y);
                    if (awayMap.isPassableTerrainAt(tempLoc) != 0)
                    {
                        updatedAppealSites.add(0, new QueuePair<>(destPair.getFirst() - WEIGHT_ROCKET, destPair.getSecond()));
                        // newer updates come earlier, can break once encountered.
                    }
                }
            }
        }
    }

    // Find potential landing spots and store in a priority queue
    // (Add priority logic later using Pair class and comparators)
    public static void findPotentialLandingSites()
    {
        if (gc.planet() == Planet.Earth)
        {
            for (int i = 0; i < awayMap.getWidth(); i++)
            {
                for (int j = 0; j < awayMap.getHeight(); j++)
                {
                    MapLocation tempLoc = new MapLocation(Planet.Mars, i, j);
                    if (awayMap.isPassableTerrainAt(tempLoc) != 0)
                    {
                        long appeal = WEIGHT_NONE;

                        // top row
                        appeal += getLocationAppeal(i - 1, j + 1);
                        appeal += getLocationAppeal(i, j + 1);
                        appeal += getLocationAppeal(i + 1, j + 1);

                        // middle row
                        appeal += getLocationAppeal(i - 1, j);
                        appeal += getLocationAppeal(i + 1, j);

                        // bottom row
                        appeal += getLocationAppeal(i - 1, j - 1);
                        appeal += getLocationAppeal(i, j - 1);
                        appeal += getLocationAppeal(i + 1, j - 1);

                        potentialLandingSites.add(new QueuePair<>(appeal, tempLoc));
                    }
                }
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
}
