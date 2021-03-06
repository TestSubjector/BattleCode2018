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
        if(homeMap.isPassableTerrainAt(targetMapLocation) == 0)
        {
            return moveUnitInRandomDirection(unit);
        }
        if (waypointAdjacencyList.isEmpty())
        {
            return moveUnitTowards(unit ,targetMapLocation);
        }
        MapLocation unitMapLocation = getConstantMapLocationRepresentation(unit.location().mapLocation());
        targetMapLocation = getConstantMapLocationRepresentation(targetMapLocation);
        MapLocation startWaypoint = findNearestUnobstructedWaypoint(unitMapLocation);
        MapLocation endWaypoint = findNearestUnobstructedWaypoint(targetMapLocation);
        if (startWaypoint == null || endWaypoint == null)
        {
            System.out.println(unitMapLocation);
            System.out.println(targetMapLocation);
            System.out.println(startWaypoint);
            System.out.println(endWaypoint);
            System.out.println("================================");
        }
        try
        {
            if (startWaypoint.equals(endWaypoint))
            {
                return moveUnitTowards(unit, targetMapLocation);
            }
        }
        catch (Exception e)
        {
            System.out.println(unitMapLocation);
            System.out.println(targetMapLocation);
            System.out.println(startWaypoint);
            System.out.println(endWaypoint);
            System.out.println("================================");
        }
        constructPathBetween(startWaypoint, endWaypoint);
        MapLocation nextWaypoint = nextBestWaypoint.get(new Pair<MapLocation, MapLocation>(startWaypoint, endWaypoint));
//        System.out.println(nextWaypoint);
//        System.out.println("================================");
        return moveUnitTowards(unit, nextWaypoint);
    }
}
