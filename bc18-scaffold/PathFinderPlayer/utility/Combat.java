package utility;

import java.util.*;

import bc.*;

import static utility.Globals.*;

public class Combat
{
    // Both Movement and Attack on Cooldown
    // TODO - Add Ability Cooldown Later
    public static boolean unitFrozenByHeat(Unit unit)
    {
        if(unit.unitType() == UnitType.Healer)
        {
            return !(gc.isHealReady(unit.id()) || gc.isMoveReady(unit.id()));
        }
        else
        {
            return !(gc.isAttackReady(unit.id()) || gc.isMoveReady(unit.id()));
        }
    }

    // Decides the incentive to attack an unit by Rangers
    // TODO - Make it live rather fixed static values, if computation allows
    public static long setBountyScore(Unit unit, Unit enemyUnit)
    {
        UnitType unitType = unit.unitType();
        MapLocation unitMapLocation = unit.location().mapLocation();
        long unitHealth = unit.health();

        long incentiveToHunt = enemyUnit.health() * -1;
        UnitType enemyUnitType = enemyUnit.unitType();
        MapLocation enemyMapLocation = enemyUnit.location().mapLocation();
        long distanceBetweenUnitsSquared = (long) Math.floor(Math.sqrt(unitMapLocation.distanceSquaredTo(enemyMapLocation)));

        // Same type of enemy unit, but higher health.
        if (unitType == enemyUnitType && unitHealth < incentiveToHunt)
        {
            incentiveToHunt += unitHealth;
        }
        else if (enemyUnitType == UnitType.Worker)
        {
            incentiveToHunt = 10 - distanceBetweenUnitsSquared;
        }
        else if (enemyUnitType == UnitType.Factory || enemyUnitType == UnitType.Rocket)
        {
            incentiveToHunt = 11 - distanceBetweenUnitsSquared;
        }
        else if (unitType == UnitType.Ranger)
        {
            if (enemyUnitType == UnitType.Knight)
            {
                // TODO - Add run away instructions later
                //(6 * 4 * 40 / 2)
                incentiveToHunt += 6 * (distanceBetweenUnitsSquared - 3) * 20;
            }
            else if (enemyUnitType == UnitType.Mage)
            {
                //Match steps with Mage
                //(Is infinite, if we don't consider non-perfect movement)
                incentiveToHunt += 20 * Math.floor((double)unitHealth * 2 / 60) + (distanceBetweenUnitsSquared - 3) * 40;
            }
            else
            {
                // Chase Healers ideally
                incentiveToHunt = 10 - distanceBetweenUnitsSquared; //(Kill others first)
            }
        }
        else if (unitType == UnitType.Knight)
        {
            if (enemyUnitType == UnitType.Ranger)
            {
                incentiveToHunt += unitHealth;
            }
            else if (enemyUnitType == UnitType.Mage)
            {
                incentiveToHunt += unitHealth;
            }
            else
            {
                incentiveToHunt = 10 - distanceBetweenUnitsSquared;
            }
        }
        else if (unitType == UnitType.Mage)
        {
            if (enemyUnitType == UnitType.Knight)
            {
                incentiveToHunt += 60 * (distanceBetweenUnitsSquared - 1);
            }
            else if (enemyUnitType == UnitType.Ranger)
            {
                incentiveToHunt += 60 * (5 - distanceBetweenUnitsSquared);
            }
            else
            {
                incentiveToHunt = 10 - distanceBetweenUnitsSquared;
            }
        }
        else if (unitType == UnitType.Healer)
        {
            return 0;
        }
        return incentiveToHunt;
    }

    // This is for Rangers only currently
    // Rationale is purely on lost damage because of distance that needs to be travelled
    public static long getEnemyUnitRank(Unit enemyUnit)
    {
        long enemyUnitPriority = 10;
        if(enemyUnit.unitType() == UnitType.Worker)
        {
            enemyUnitPriority = 14;
        }
        else if(enemyUnit.unitType() == UnitType.Factory)
        {
            enemyUnitPriority = 12;
        }
        else if(enemyUnit.unitType() == UnitType.Rocket)
        {
            enemyUnitPriority = 10;
        }
        else if(enemyUnit.unitType() == UnitType.Knight)
        {
            enemyUnitPriority = 8;
        }
        else if(enemyUnit.unitType() == UnitType.Healer)
        {
            enemyUnitPriority = 6;
        }
        else if(enemyUnit.unitType() == UnitType.Mage)
        {
            enemyUnitPriority = 4;
        }
        else if(enemyUnit.unitType() == UnitType.Ranger)
        {
            enemyUnitPriority = 2;
        }
        // One-shot kill
        if(enemyUnit.health() <=40)
        {
            enemyUnitPriority /= 2;
        }
        return enemyUnitPriority;
    }
}
