package utility;

import java.util.*;

import bc.*;

import static utility.Globals.*;
import static utility.Movement.*;

public class KnightBot
{
    public static void processEarthKnight(Unit unit, Location unitLocation)
    {
        MapLocation unitMapLocation = unitLocation.mapLocation();
        VecUnit adjacentUnits = gc.senseNearbyUnitsByTeam(unit.location().mapLocation(), 2, ourTeam);

    }

    public static void processMarsKnight(Unit unit, Location unitLocation)
    {

    }

    public static void processKnight(Unit unit, Location unitLocation)
    {
        if (homePlanet == Planet.Earth)
        {
            processEarthKnight(unit, unitLocation);
        }
        else
        {
            processMarsKnight(unit, unitLocation);
        }
    }
}
