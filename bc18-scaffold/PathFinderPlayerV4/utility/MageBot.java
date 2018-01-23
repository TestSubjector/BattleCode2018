package utility;

import java.util.*;

import bc.*;

import static utility.Globals.*;
import static utility.Movement.*;

public class MageBot
{
    public static void processEarthMage(Unit unit, Location unitLocation)
    {
        MapLocation unitMapLocation = unitLocation.mapLocation();
        VecUnit adjacentUnits = gc.senseNearbyUnitsByTeam(unit.location().mapLocation(), 2, ourTeam);

    }

    public static void processMarsMage(Unit unit, Location unitLocation)
    {

    }

    public static void processMage(Unit unit, Location unitLocation)
    {
        if (homePlanet == Planet.Earth)
        {
            processEarthMage(unit, unitLocation);
        }
        else
        {
            processMarsMage(unit, unitLocation);
        }
    }
}
