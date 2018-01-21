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
        if (diagonalDistanceBetween(unitMapLocation, startWaypoint) <= 1)
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

    public static void moveUnitAwayFromMultipleUnits(VecUnit nearbyUnits, Unit unit)
    {
        long[] directionArray = {1,1,1,1,1,1,1,1,1};
        long numberOfNearbyUnits = nearbyUnits.size();
        long count = 8;
        MapLocation unitLocation = unit.location().mapLocation();
        for(int i = 0; i< numberOfNearbyUnits; i++)
        {
            // Gives Direction Between Units
            Direction directionToOtherUnit = unitLocation.directionTo(nearbyUnits.get(i).location().mapLocation());
            directionArray[directionToOtherUnit.ordinal()] = 0;
        }
        for(int j = 0; j < 8; j++)
        {
            if(directionArray[j] != 0)
            {
                if(moveUnitInDirection(unit, Direction.values()[j]))
                {
                    break;
                }
            }
            else
            {
                count--;
            }
        }
        if(count == 0)
        {
            moveUnitInRandomDirection(unit);
        }
    }
}
