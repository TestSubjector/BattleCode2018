package utility;

import java.util.*;

import bc.*;

import static utility.Globals.*;

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
}
