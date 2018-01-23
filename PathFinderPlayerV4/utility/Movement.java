package utility;

import java.util.*;

import bc.*;

import static utility.Globals.*;
import static utility.Pathfinding.*;

public class Movement
{
    public static boolean moveUnitInDirection(Unit unit, Direction candidateDirection)
    {
        int directionIndex = candidateDirection.ordinal();
        if (gc.isMoveReady(unit.id()))
        {
            int delta = 1;
            while (!gc.canMove(unit.id(), candidateDirection) && Math.abs(delta) <= 1)
            {
                candidateDirection = directions[(((directionIndex + delta) % 8) + 8) % 8];
                delta = -delta;
                if (delta > 0)
                {
                    delta++;
                }
            }
            if (gc.canMove(unit.id(), candidateDirection))
            {
                gc.moveRobot(unit.id(), candidateDirection);
                return true;
            }
        }
        return false;
    }

    public static boolean moveUnitTowards(Unit unit, MapLocation targetMapLocation)
    {
        Direction targetDirection = unit.location().mapLocation().directionTo(targetMapLocation);
        return moveUnitInDirection(unit, targetDirection);
    }

    public static boolean moveUnitAwayFrom(Unit unit, MapLocation targetLocation)
    {
        Direction targetDirection = unit.location().mapLocation().directionTo(targetLocation);
        targetDirection = bc.bcDirectionOpposite(targetDirection);
        return moveUnitInDirection(unit, targetDirection);
    }

    public static boolean moveUnitInRandomDirection(Unit unit)
    {
        Random random = new Random();
        return moveUnitInDirection(unit, directions[random.nextInt(8)]);
    }

    public static boolean moveUnitTo(Unit unit, MapLocation targetMapLocation)
    {
        if (waypointAdjacencyList.isEmpty())
        {
            moveUnitTowards(unit, targetMapLocation);
            return true;
        }
        MapLocation unitMapLocation = getConstantMapLocationRepresentation(unit.location().mapLocation());
        targetMapLocation = getConstantMapLocationRepresentation(targetMapLocation);
        MapLocation startWaypoint = findNearestUnobstructedWaypoint(unitMapLocation);
        MapLocation endWaypoint = findNearestUnobstructedWaypoint(targetMapLocation);
        if (startWaypoint == null)
        {
            // We are stuck
            return false;
        }
        if (endWaypoint == null)
        {
            // Target unreachable
            return false;
        }
        if (startWaypoint.equals(endWaypoint))
        {
            moveUnitTowards(unit, targetMapLocation);
            return true;
        }
        constructPathBetween(startWaypoint, endWaypoint);
        MapLocation nextWaypoint = nextBestWaypoint.get(new Pair<MapLocation, MapLocation>(startWaypoint, endWaypoint));
        if (nextWaypoint == null)
        {
            // Target unreachable
            return false;
        }
        if (unitMapLocation.distanceSquaredTo(startWaypoint) <= 2)
        {
            lastVisited.put(unit.id(), startWaypoint);
        }
        if (lastVisited.containsKey(unit.id()) && lastVisited.get(unit.id()).equals(startWaypoint))
        {
            moveUnitTowards(unit, nextWaypoint);
        }
        else
        {
            moveUnitTowards(unit, startWaypoint);
        }
        return true;
    }

    // Retreat Function
    public static boolean moveUnitAwayFromMultipleUnits(Unit unit, VecUnit nearbyUnits)
    {
        long n = nearbyUnits.size();
        MapLocation unitMapLocation = unit.location().mapLocation();
        long x = unitMapLocation.getX();
        long y = unitMapLocation.getY();
        double X = 0;
        double Y = 0;
        for (int i = 0; i < n; i++)
        {
            Unit u = nearbyUnits.get(i);
            double d1 = x - u.location().mapLocation().getX();
            double d2 = y - u.location().mapLocation().getY();
            double magnitudeSquared = x * x + y * y;
            if (u.team() == theirTeam)
            {
                if (d1 != 0)
                {
                    X += (3000 * d1) / magnitudeSquared;
                }
                if (d2 != 0)
                {
                    Y += (3000 * d2) / magnitudeSquared;
                }
            }
            else
            {
                if (d1 != 0)
                {
                    X += (1000 * d1) / magnitudeSquared;
                }
                if (d2 != 0)
                {
                    Y += (1000 * d1) / magnitudeSquared;
                }
            }
        }
        return moveUnitInDirection(unit, unitMapLocation.directionTo(new MapLocation(homePlanet, (int) Math.round(x + X), (int) Math.round(y + Y))));
    }
}
