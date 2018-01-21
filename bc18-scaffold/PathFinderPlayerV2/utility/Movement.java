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
            while (!gc.canMove(unit.id(), candidateDirection) && Math.abs(delta) <= 2)
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
        try
        {
            Direction targetDirection = unit.location().mapLocation().directionTo(targetMapLocation);
            return moveUnitInDirection(unit, targetDirection);
        }
        catch (Exception e)
        {
            System.out.println(unit.location().mapLocation());
            System.out.println(targetMapLocation);
        }
        return false;
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
        if (homeMap.isPassableTerrainAt(targetMapLocation) == 0 || waypointAdjacencyList.isEmpty())
        {
            return moveUnitTowards(unit ,targetMapLocation);
        }
        MapLocation unitMapLocation = getConstantMapLocationRepresentation(unit.location().mapLocation());
        targetMapLocation = getConstantMapLocationRepresentation(targetMapLocation);
        MapLocation startWaypoint = findNearestUnobstructedWaypoint(unitMapLocation);
        MapLocation endWaypoint = findNearestUnobstructedWaypoint(targetMapLocation);
        if (startWaypoint.equals(endWaypoint))
        {
            return moveUnitTowards(unit, targetMapLocation);
        }
        constructPathBetween(startWaypoint, endWaypoint);
        MapLocation nextWaypoint = nextBestWaypoint.get(new Pair<MapLocation, MapLocation>(startWaypoint, endWaypoint));
//        System.out.println(nextWaypoint);
//        System.out.println("================================");
        return moveUnitTowards(unit, nextWaypoint);
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
