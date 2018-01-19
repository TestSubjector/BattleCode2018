package utility;

import java.util.*;

import bc.*;

import static utility.Globals.*;

public class FactoryBot
{
    // Unloads a robot, if possible
    public static boolean tryToUnloadRobot(Unit factory)
    {
        for (int i = 0; i < directions.length - 1; i++)
        {
            Direction unloadDirection = directions[i];
            if (gc.canUnload(factory.id(), unloadDirection))
            {
                gc.unload(factory.id(), unloadDirection);
                return true;
            }
        }
        return false;
    }

    public static void processFactory(Unit unit, Location unitLocation)
    {

    }
}
