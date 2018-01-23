package utility;

import java.util.*;

import bc.*;

import static utility.Globals.*;
import static utility.Movement.*;
import static utility.Combat.*;

public class HealerBot
{
    public static void processEarthHealer(Unit unit, Location unitLocation)
    {
        MapLocation unitMapLocation = unitLocation.mapLocation();
        VecUnit nearbyFriendlyUnits = gc.senseNearbyUnitsByTeam(unitLocation.mapLocation(), unit.visionRange(), ourTeam);
        VecUnit nearbyEnemyUnits = enemyVecUnits.get(unit.id());
        boolean hasHealedThisTurn = false;
        boolean hasMovedThisTurn = false;

        if (unitFrozenByHeat(unit))
        {
            return;
        }

        for (int j = 0; j < nearbyFriendlyUnits.size(); j++)
        {
            Unit nearbyFriendlyUnit = nearbyFriendlyUnits.get(j);
            {
                if (gc.isHealReady(unit.id()))
                {
                    if (gc.canHeal(unit.id(), nearbyFriendlyUnit.id()) &&
                            nearbyFriendlyUnit.health() < nearbyFriendlyUnit.maxHealth())
                    {
                        gc.heal(unit.id(), nearbyFriendlyUnit.id());
                        hasHealedThisTurn = true;
                    }
                }
                // TODO - Implement function so as to not get too close
                if (nearbyFriendlyUnit.health() < nearbyFriendlyUnit.maxHealth())
                {
                    if (moveUnitTo(unit, nearbyFriendlyUnit.location().mapLocation()))
                    {
                        hasMovedThisTurn = true;
                    }
                }
            }
            if (hasMovedThisTurn && hasHealedThisTurn)
            {
                break;
            }
        }
        // TODO - Implement Running/Retreating Function
        if (nearbyEnemyUnits.size() != 0)
        {
            // moveUnitAwayFromMultipleUnits(nearbyEnemyUnits, unit);
        }
        else
        {
            moveUnitInRandomDirection(unit);
        }
        moveUnitInRandomDirection(unit);
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
