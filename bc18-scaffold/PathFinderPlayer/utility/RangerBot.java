package utility;

import java.util.*;

import bc.*;

import static utility.Globals.*;
import static utility.Movement.*;
import static utility.Combat.*;

public class RangerBot
{

    public static void processEarthRanger(Unit unit, Location unitLocation)
    {
        MapLocation unitMapLocation = unitLocation.mapLocation();
        VecUnit nearbyEnemyUnits = enemyVecUnits.get(unit.id());

        if(nearbyEnemyUnits.size() != 0)
        {
            // Must be refined later with movement code above this
            if (unitFrozenByHeat(unit))
            {
                return;
            }

            if(gc.isAttackReady(unit.id()))
            {
                long desireToKill = -500;
                long rememberUnit = -1;
                for (int j = 0; j < nearbyEnemyUnits.size(); j++)
                {
                    Unit nearbyEnemyUnit = nearbyEnemyUnits.get(j);
                    // Check health of enemy unit ands see if you can win
                    // Make bounty rating for all sensed units and attack highest ranked unit
                    //if(nearbyEnemyUnit.unitType() != UnitType.Worker)
                    {
                        if (gc.canAttack(unit.id(), nearbyEnemyUnit.id()))
                        {
                            long possibleDesireToKill = nearbyEnemyUnit.health() * -1;
                            if (desireToKill < possibleDesireToKill)
                            {
                                desireToKill = possibleDesireToKill;
                                rememberUnit = j;
                            }
                        }
                    }
                }
                if (rememberUnit != -1)
                {
                    gc.attack(unit.id(), nearbyEnemyUnits.get(rememberUnit).id());
                    //moveUnitAwayFrom(unit, nearbyEnemyUnits.get(rememberUnit).location());
                }
                else
                {
                    moveUnitInRandomDirection(unit);
                }
            }
        }
        else
        {
            // QueuePair<Long, Unit> enemyData;
            long nearestEnemyGridDistance = 100000L;
            MapLocation nearestEnemyMapLocation = null;
            for(MapLocation enemyLocationAverageLocation : enemyLocationAverages)
            {
                long enemyGridDistance = diagonalDistanceBetween(unitMapLocation, enemyLocationAverageLocation);
                if(enemyGridDistance < nearestEnemyGridDistance)
                {
                    nearestEnemyMapLocation = enemyLocationAverageLocation;
                    nearestEnemyGridDistance = enemyGridDistance;
                }
            }
            if(nearestEnemyMapLocation == null ||
                    !moveUnitTo(unit, nearestEnemyMapLocation));
            {
                moveUnitInRandomDirection(unit);
            }
        }
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
