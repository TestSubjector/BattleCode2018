import bc.*;
import java.util.*;

public class Player
{
    static GameController gc;

    public static void moveUnitTowards(Unit unit, Location targetLocation)
    {
        Direction movementDirection = unit.location().mapLocation().directionTo(targetLocation.mapLocation());
        if (gc.isMoveReady(unit.id()) && gc.canMove(unit.id(), movementDirection)
                )
        {
            gc.moveRobot(unit.id(), movementDirection);
        }
    }

    public static void main(String[] args)
    {
        // MapLocation is a data structure you'll use a lot.

        // Connect to the manager, starting the game
        GameController gc = new GameController();

        // Direction is a normal java enum.
        Direction[] directions = Direction.values();

        while (true)
        {
            System.out.println("Current round: " + gc.round());
            // VecUnit is a class that you can think of as similar to ArrayList<Unit>, but immutable.
            VecUnit units = gc.myUnits();
            for (int i = 0; i < units.size(); i++)
            {
                Unit unit = units.get(i);
                // For Worker
                if (unit.unitType() == UnitType.Worker)
                {
                    // Check Replication
                    if (gc.canReplicate(unit.id(), Direction.Southwest))
                    {
                        gc.replicate(unit.id(), Direction.Southwest);
                    }

                    // Most methods on gc take unit IDs, instead of the unit objects themselves.
                    if (gc.isMoveReady(unit.id()) && gc.canMove(unit.id(), Direction.East)) {
                        gc.moveRobot(unit.id(), Direction.East);
                    }
                }
            }
            // Submit the actions we've done, and wait for our next turn.
            gc.nextTurn();
        }
    }
}