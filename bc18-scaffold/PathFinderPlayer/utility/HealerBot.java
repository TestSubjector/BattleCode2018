package utility;

import java.util.*;

import bc.*;

import static utility.Globals.*;
import static utility.Movement.*;

public class HealerBot
{
    public static void processEarthHealer(Unit unit, Location unitLocation)
    {
        MapLocation unitMapLocation = unitLocation.mapLocation();
        VecUnit adjacentUnits = gc.senseNearbyUnitsByTeam(unit.location().mapLocation(), 2, ourTeam);

    }

    public static void processMarsHealer(Unit unit, Location unitLocation)
    {

    }

    public static void processHealer(Unit unit, Location unitLocation)
    {
        if (homePlanet == Planet.Earth)
        {
            processEarthHealer(unit, unitLocation);
        }
        else
        {
            processMarsHealer(unit, unitLocation);
        }
    }
}
