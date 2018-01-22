package utility;

import java.util.*;

import bc.*;

import static utility.Globals.*;
import static utility.Movement.*;

public class Combat
{
    // Both Movement and Attack on Cooldown
    // TODO - Add Ability Cooldown Later
    public static boolean unitFrozenByHeat(Unit unit)
    {
        if (unit.unitType() == UnitType.Healer)
        {
            return !(gc.isHealReady(unit.id()) || gc.isMoveReady(unit.id()));
        }
        else
        {
            return !(gc.isAttackReady(unit.id()) || gc.isMoveReady(unit.id()));
        }
    }

    public static void simpleCombat(Unit unit, VecUnit nearbyEnemyUnits)
    {
        if(gc.isAttackReady(unit.id()))
        {
            for (int j = 0; j < nearbyEnemyUnits.size(); j++) {
                if (gc.canAttack(unit.id(), nearbyEnemyUnits.get(j).id())) {
                    gc.attack(unit.id(), nearbyEnemyUnits.get(j).id());
                    break;
                }
            }
        }
        else
        {
            moveUnitTowards(unit, nearbyEnemyUnits.get(1).location().mapLocation());
        }
    }

    public static boolean canWin1v1(Unit unit , Unit enemyUnit)
    {
        UnitType enemyUnitType = enemyUnit.unitType();
        // Non-attacking units
        if(enemyUnitType != UnitType.Ranger && enemyUnitType != UnitType.Knight && enemyUnitType != UnitType.Mage)
        {
            return true;
        }

        // Our Unit Calculation
        long numberOfAttacksAfterFirstToKillEnemyUnit;
        if(enemyUnit.unitType() == UnitType.Knight)
        {
            numberOfAttacksAfterFirstToKillEnemyUnit = (long) (enemyUnit.health() - 0.001) / (unit.damage() - enemyUnit.knightDefense());
        }
        else
        {
            numberOfAttacksAfterFirstToKillEnemyUnit = (long) ((enemyUnit.health() - 0.001) / unit.damage());
        }

        long turnsTillUnitCanAttack = (long) unit.attackHeat()/ 10;
        long effectiveAttackDelay = (long) unit.attackCooldown()/10;
        long turnsToKillEnemyUnit = turnsTillUnitCanAttack + effectiveAttackDelay * numberOfAttacksAfterFirstToKillEnemyUnit;

        // Enemy Unit Calculation
        long numberOfAttacksAfterFirstForEnemyToKillUnit;
        if(unit.unitType() == UnitType.Knight)
        {
            numberOfAttacksAfterFirstForEnemyToKillUnit = (long) (unit.health() - 0.001) / (enemyUnit.damage() - unit.knightDefense());
        }
        else
        {
            numberOfAttacksAfterFirstForEnemyToKillUnit = (long) ((unit.health() - 0.001) / enemyUnit.damage());
        }

        long turnsTillEnemyCanAttack = (long) enemyUnit.attackHeat()/ 10;
        long effectiveEnemyAttackDelay = (long) enemyUnit.attackCooldown()/10;

        long turnsForEnemyToKillUnit = turnsTillEnemyCanAttack + effectiveEnemyAttackDelay * numberOfAttacksAfterFirstForEnemyToKillUnit;

        return turnsToKillEnemyUnit <= turnsForEnemyToKillUnit;
    }

    // TODO - Create function
    public static boolean canWin1v1AfterMovingTo(Unit unit , Unit enemyUnit, MapLocation unitMapLocation)
    {
        // I require the distance from the Maplocation in front of me to the enemy location
        return false;
    }

    // TODO - Create function and use later to harass enemy
    public static void enemyUnitHarasser(Unit unit, Unit closestEnemyUnit, VecUnit nearbyEnemyUnits)
    {

    }

    // TODO - Implement in Workers for mining locations
    // Returns whether it's a good move to visit a certain location
    public static boolean isItSafeToMoveTo(Unit unit, MapLocation moveUnitToLocation, VecUnit nearbyEnemyUnits)
    {
        Unit loneAttacker = null;
        int numAttackers = 0;
        for (int j = 0; j < nearbyEnemyUnits.size(); j++)
        {
            Unit nearbyEnemyUnit = nearbyEnemyUnits.get(j);
            UnitType enemyUnitType = nearbyEnemyUnit.unitType();
            if(enemyUnitType != UnitType.Ranger && enemyUnitType != UnitType.Knight && enemyUnitType != UnitType.Mage)
            {
                continue;
            }
            MapLocation nearbyEnemyUnitLocation = nearbyEnemyUnit.location().mapLocation();
            switch (nearbyEnemyUnit.unitType())
            {
                case Ranger:
                    if (moveUnitToLocation.distanceSquaredTo(nearbyEnemyUnitLocation) > 10)
                    {
                        return false;
                    }
                    break;
                default:
                    if (nearbyEnemyUnit.attackRange() >= moveUnitToLocation.distanceSquaredTo(nearbyEnemyUnitLocation))
                    {
                        numAttackers++;
                        if (numAttackers >= 2)
                        {
                            return false;
                        }
                        loneAttacker = nearbyEnemyUnit;
                    }
                break;
            }
        }

        if (numAttackers == 0)
        {
            return true;
        }
        return canWin1v1(unit, loneAttacker);
    }

    // Find of number of units that can attack an location
    // TODO - Find total damage they can deal
    public static int numberEnemyUnitsAimingAtLocation(MapLocation targetLocation, VecUnit nearbyEnemyUnits)
    {
        int locationExposure = 0;
        for (int j = 0; j < nearbyEnemyUnits.size(); j++)
        {
            Unit nearbyEnemyUnit = nearbyEnemyUnits.get(j);
            UnitType enemyUnitType = nearbyEnemyUnit.unitType();
            if(enemyUnitType != UnitType.Ranger && enemyUnitType != UnitType.Knight && enemyUnitType != UnitType.Mage)
            {
                continue;
            }
            long distanceFromEnemyToLocation = targetLocation.distanceSquaredTo(nearbyEnemyUnit.location().mapLocation());
            if (nearbyEnemyUnit.unitType() == UnitType.Ranger)
            {
                if(distanceFromEnemyToLocation > 10 && distanceFromEnemyToLocation < 50)
                {
                    locationExposure++;
                }
            }
            else if (nearbyEnemyUnit.attackRange() >= distanceFromEnemyToLocation)
            {
                locationExposure++;
            }
        }
        return locationExposure;
    }

    public static int numberFriendlyUnitsAimingAtLocation(MapLocation targetLocation, VecUnit nearbyFriendlyUnits)
    {
        int locationExposure = 0;
        for (int j = 0; j < nearbyFriendlyUnits.size(); j++)
        {
            Unit nearbyFriendlyUnit = nearbyFriendlyUnits.get(j);
            UnitType friendlyUnitType = nearbyFriendlyUnit.unitType();
            if(friendlyUnitType != UnitType.Ranger && friendlyUnitType != UnitType.Knight && friendlyUnitType != UnitType.Mage)
            {
                continue;
            }
            long distanceFromEnemyToLocation = targetLocation.distanceSquaredTo(nearbyFriendlyUnit.location().mapLocation());
            if (nearbyFriendlyUnit.unitType() == UnitType.Ranger)
            {
                if(distanceFromEnemyToLocation > 10 && distanceFromEnemyToLocation < 50)
                {
                    locationExposure++;
                }
            }
            else if (nearbyFriendlyUnit.attackRange() >= distanceFromEnemyToLocation)
            {
                locationExposure++;
            }
        }
        return locationExposure;
    }

    public static boolean tryMoveToEngageEnemyAtLocationInOneTurnWithMaxEnemyExposure(Unit unit, MapLocation targetLocation, int maxEnemyExposure, VecUnit nearbyEnemyUnits)
    {
        Direction toTargetLocation = unit.location().mapLocation().directionTo(targetLocation);
        Direction[] tryDirection = new Direction[]{toTargetLocation, Direction.swigToEnum(toTargetLocation.swigValue()-1), Direction.swigToEnum(toTargetLocation.swigValue()+1)};
        for (Direction directionToMoveTo : tryDirection) {
            if (!gc.canMove(unit.id(), directionToMoveTo))
            {
                continue;
            }
            MapLocation moveLocation = unit.location().mapLocation().add(directionToMoveTo);
            if (unit.attackRange() < moveLocation.distanceSquaredTo(targetLocation))
            {
                continue; // must engage in one turn
            }

            int enemyExposure = numberEnemyUnitsAimingAtLocation(moveLocation, nearbyEnemyUnits);
            if (enemyExposure <= maxEnemyExposure)
            {
                gc.moveRobot(unit.id(), directionToMoveTo);
                return true;
            }
        }
        return false;
    }

    public static boolean tryMoveToEngageEnemyAtWithMaxEnemyExposure(Unit unit, MapLocation targetLocation, int maxEnemyExposure, VecUnit nearbyEnemyUnits)
    {
        Direction toTargetLocation = unit.location().mapLocation().directionTo(targetLocation);
        Direction[] tryDirection = new Direction[]{toTargetLocation, Direction.swigToEnum(toTargetLocation.swigValue()-1), Direction.swigToEnum(toTargetLocation.swigValue()+1)};
        for (Direction directionToMoveTo : tryDirection) {
            if (!gc.canMove(unit.id(), directionToMoveTo))
            {
                continue;
            }
            MapLocation moveLocation = unit.location().mapLocation().add(directionToMoveTo);
            int enemyExposure = numberEnemyUnitsAimingAtLocation(moveLocation, nearbyEnemyUnits);
            if (enemyExposure <= maxEnemyExposure)
            {
                gc.moveRobot(unit.id(), directionToMoveTo);
                return true;
            }
        }
        return false;
    }

    // TODO - Factor in Cooldown
    private static int numberOfOtherAlliesInAttackRange(Unit unit, MapLocation targetLocation)
    {
        int allyAssistNumber = 0;
        VecUnit nearbyFriendlyUnits = friendlyVecUnits.get(unit.id());
        for (int j = 0; j < nearbyFriendlyUnits.size(); j++)
        {
            Unit nearbyFriendlyUnit  = nearbyFriendlyUnits.get(j);
            UnitType friendlyUnitType = nearbyFriendlyUnit.unitType();
            if(friendlyUnitType != UnitType.Ranger && friendlyUnitType != UnitType.Knight && friendlyUnitType != UnitType.Mage)
            {
                continue;
            }
            else if(nearbyFriendlyUnit.attackRange() >= nearbyFriendlyUnit.location().mapLocation().distanceSquaredTo(targetLocation))
            {
                if(friendlyUnitType == UnitType.Ranger)
                {
                    if(10 <= nearbyFriendlyUnit.location().mapLocation().distanceSquaredTo(targetLocation))
                    {
                        allyAssistNumber++;
                    }
                }
                else
                {
                    allyAssistNumber++;
                }
            }
        }
        return allyAssistNumber;
    }

    // Multiple Units
    public static void doMicro(Unit unit, MapLocation unitMapLocation, VecUnit nearbyEnemyUnits)
    {
        long sizeOfEnemy = nearbyEnemyUnits.size();
        if(sizeOfEnemy != 0)
        {
            // Must be refined later with movement code above this
            if (unitFrozenByHeat(unit))
            {
                return;
            }

            if(gc.isAttackReady(unit.id()))
            {
                if(botIntelligenceLevel == 0)
                {
                    simpleCombat(unit, nearbyEnemyUnits);
                }
                else
                {   // TODO - Make condition so that if Knight then attack rather than retreat
                    if(sizeOfEnemy == 1)
                    {
                        Unit loneEnemyUnit = nearbyEnemyUnits.get(0);
                        if(gc.canAttack(unit.id(), loneEnemyUnit.id()))
                        {
                            if(canWin1v1(unit, loneEnemyUnit))
                            {

                                gc.attack(unit.id(), loneEnemyUnit.id());
                                return;
                            }
                            else
                            {
                                boolean haveSupport = false;
                                if(numberOfOtherAlliesInAttackRange(unit, loneEnemyUnit.location().mapLocation()) > 0)
                                {
                                    haveSupport = true;
                                }

                                // Have ally help
                                if(haveSupport)
                                {
                                    gc.attack(unit.id(), loneEnemyUnit.id());
                                    return;
                                }
                                else
                                {
                                    // TODO - Retreat Function as we don't have backup
                                    // Enemy can't fire. Shoot and retreat
                                    if(loneEnemyUnit.attackCooldown() >= 20)
                                    {

                                        gc.attack(unit.id(), loneEnemyUnit.id());
                                        return;
                                    }
                                    else
                                    {
                                        // If we can move, then try retreat
                                        if(unit.movementHeat() <10)
                                        {
                                            // TODO - Retreat function here
                                            if(moveUnitAwayFrom(unit, loneEnemyUnit.location().mapLocation()))
                                            {
                                                return;
                                            }
                                            else
                                            {
                                                // Desperate attack
                                                gc.attack(unit.id(), loneEnemyUnit.id());
                                                return;
                                            }
                                        }
                                        else
                                        {
                                            gc.attack(unit.id(), loneEnemyUnit.id());
                                            return;
                                        }
                                    }
                                }
                            }
                        }
                        else
                        {
                            // Can't attack enemy unit
                            moveUnitAwayFrom(unit, loneEnemyUnit.location().mapLocation());
                        }
                    }
                    else
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



    // Decides the incentive to attack an unit by Rangers
    // TODO - Make it live rather fixed static values, if computation allows
    /*
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
                incentiveToHunt += 20 * Math.floor((double) unitHealth * 2 / 60) + (distanceBetweenUnitsSquared - 3) * 40;
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
    */

    /*

    // This is for Rangers only currently
    // Rationale is purely on lost damage because of distance that needs to be travelled
    public static long getEnemyUnitRank(Unit enemyUnit)
    {
        long enemyUnitPriority = 10;
        if (enemyUnit.unitType() == UnitType.Worker)
        {
            enemyUnitPriority = 14;
        }
        else if (enemyUnit.unitType() == UnitType.Factory)
        {
            enemyUnitPriority = 12;
        }
        else if (enemyUnit.unitType() == UnitType.Rocket)
        {
            enemyUnitPriority = 10;
        }
        else if (enemyUnit.unitType() == UnitType.Knight)
        {
            enemyUnitPriority = 8;
        }
        else if (enemyUnit.unitType() == UnitType.Healer)
        {
            enemyUnitPriority = 6;
        }
        else if (enemyUnit.unitType() == UnitType.Mage)
        {
            enemyUnitPriority = 4;
        }
        else if (enemyUnit.unitType() == UnitType.Ranger)
        {
            enemyUnitPriority = 2;
        }
        // One-shot kill
        if (enemyUnit.health() <= 40)
        {
            enemyUnitPriority /= 2;
        }
        return enemyUnitPriority;
    }

    */
    /* We are not going to use this function it is not very good
    public static void moveUnitAwayFromMultipleUnits(VecUnit nearbyUnits, Unit unit)
    {
        long[] directionArray = {1,1,1,1,1,1,1,1,1};
        long numberOfNearbyUnits = nearbyUnits.size();
        long count = 8;
        MapLocation unitLocation = unit.location().mapLocation();
        for(int i = 0; i< numberOfNearbyUnits; i++)
        {
            // Gives Direction Between Units
            Direction directionToOtherUnit = unitLocation.directionTo(nearbyUnits.get(i).location().mapLocation());
            directionArray[directionToOtherUnit.ordinal()] = 0;
        }
        for(int j = 0; j < 8; j++)
        {
            if(directionArray[j] != 0)
            {
                if(moveUnitInDirection(unit, Direction.values()[j]))
                {
                    break;
                }
            }
            else
            {
                count--;
            }
        }
        if(count == 0)
        {
            moveUnitInRandomDirection(unit);
        }
    }
*/
}
