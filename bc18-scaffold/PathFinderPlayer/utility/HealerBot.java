package utility;

import java.util.*;

import bc.*;

import static utility.Globals.*;
import static utility.Movement.*;
import static utility.Combat.*;

public class HealerBot
{
    public static void processEarthHealer(Unit unit, Location unitLocation) {
        MapLocation unitMapLocation = unitLocation.mapLocation();
        VecUnit nearbyFriendlyUnits = gc.senseNearbyUnitsByTeam(unitLocation.mapLocation(), unit.visionRange(), ourTeam);
        VecUnit nearbyEnemyUnits = enemyVecUnits.get(unit.id());

        doMicroHealers(unit, unitLocation.mapLocation(), nearbyEnemyUnits, nearbyFriendlyUnits);
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
