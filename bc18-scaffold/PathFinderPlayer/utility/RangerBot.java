package utility;

import java.util.*;

import bc.*;

import static utility.Globals.*;
import static utility.Movement.*;

public class RangerBot
{

    public static void processEarthRanger(Unit unit, Location unitLocation)
    {
        MapLocation unitMapLocation = unitLocation.mapLocation();
        VecUnit adjacentUnits = gc.senseNearbyUnitsByTeam(unit.location().mapLocation(), 2, ourTeam);

    }

    public static void processMarsRanger(Unit unit, Location unitLocation)
    {

    }

    public static void processRanger(Unit unit, Location unitLocation)
    {
        if (homePlanet == Planet.Earth)
        {
            processEarthRanger(unit, unitLocation);
        }
        else
        {
            processMarsRanger(unit, unitLocation);
        }
    }
}
