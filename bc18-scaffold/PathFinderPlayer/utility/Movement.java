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
            moveUnitTowards(unit ,targetMapLocation);
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

    // Simple Retreat Function [Now Improved]

    public static boolean moveUnitAwayFromMultipleUnits(VecUnit nearbyUnits, Unit unit)
    {
        long[] directionArray = {5,5,5,5,5,5,5,5,5};
        long numberOfNearbyUnits = nearbyUnits.size();
        long count = -20;
        int index = -1;
        MapLocation unitLocation = unit.location().mapLocation();
        for(int i = 0; i < numberOfNearbyUnits; i++)
        {
            // Gives Direction Between Units
            Direction directionToOtherUnit = unitLocation.directionTo(nearbyUnits.get(i).location().mapLocation());
            directionArray[directionToOtherUnit.ordinal()] -= 2;
            directionArray[directionToOtherUnit.ordinal() + 1] -= 1;
            directionArray[(directionToOtherUnit.ordinal() + 7) % 8] -= 1;
        }
        for(int j = 0; j < 8; j++)
        {
            if(count < directionArray[j])
            {
                count = directionArray[j];
                index = j;
            }
        }
        if(index != -1)
        {
            return moveUnitInDirection(unit, Direction.values()[index]);
        }
        return false;
    }
}
