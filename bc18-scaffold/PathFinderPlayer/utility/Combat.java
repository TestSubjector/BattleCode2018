package utility;

import java.util.*;

import bc.*;

import static utility.FactoryBot.processFactory;
import static utility.Globals.*;
import static utility.HealerBot.processHealer;
import static utility.KnightBot.processKnight;
import static utility.MageBot.processMage;
import static utility.Movement.*;
import static utility.RangerBot.processRanger;
import static utility.RocketBot.processRocket;
import static utility.WorkerBot.processWorker;

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
        if(gc.isAttackReady(unit.id()) && nearbyEnemyUnits != null)
        {
            for (int j = 0; nearbyEnemyUnits != null && j < nearbyEnemyUnits.size(); j++)
            {
                if (gc.canAttack(unit.id(), nearbyEnemyUnits.get(j).id()))
                {
                    gc.attack(unit.id(), nearbyEnemyUnits.get(j).id());
                    break;
                }
            }
        }
        else
        {
            moveUnitTowards(unit, nearbyEnemyUnits.get(0).location().mapLocation());
        }
    }

    public static void simpleHeal(Unit unit, VecUnit nearbyFriendlyUnits)
    {
        if(gc.isHealReady(unit.id()) && nearbyFriendlyUnits != null)
        {
            for (int j = 0; j < nearbyFriendlyUnits.size(); j++)
            {
                if (gc.canHeal(unit.id(), nearbyFriendlyUnits.get(j).id()))
                {
                    gc.heal(unit.id(), nearbyFriendlyUnits.get(j).id());
                    break;
                }
            }
        }
        else
        {
            moveUnitTowards(unit, nearbyFriendlyUnits.get(0).location().mapLocation());
        }
    }

//    public static boolean canWin1v1(Unit unit , Unit enemyUnit)
//    {
//        UnitType enemyUnitType = enemyUnit.unitType();
//        // Non-attacking units
//        if(enemyUnitType != UnitType.Ranger && enemyUnitType != UnitType.Knight && enemyUnitType != UnitType.Mage)
//        {
//            return true;
//        }
//
//        // Our Unit Calculation
//        long numberOfAttacksAfterFirstToKillEnemyUnit;
//        if(enemyUnit.unitType() == UnitType.Knight)
//        {
//            numberOfAttacksAfterFirstToKillEnemyUnit = (long) (enemyUnit.health() - 0.001) / (unit.damage() - enemyUnit.knightDefense());
//        }
//        else
//        {
//            numberOfAttacksAfterFirstToKillEnemyUnit = (long) ((enemyUnit.health() - 0.001) / unit.damage());
//        }
//
//        long turnsTillUnitCanAttack = unit.attackHeat() / 10;
//        long effectiveAttackDelay = unit.attackCooldown() / 10;
//        long turnsToKillEnemyUnit = turnsTillUnitCanAttack + effectiveAttackDelay * numberOfAttacksAfterFirstToKillEnemyUnit;
//
//        // Enemy Unit Calculation
//        long numberOfAttacksAfterFirstForEnemyToKillUnit;
//        if(unit.unitType() == UnitType.Knight)
//        {
//            numberOfAttacksAfterFirstForEnemyToKillUnit = (long) (unit.health() - 0.001) / (enemyUnit.damage() - unit.knightDefense());
//        }
//        else
//        {
//            numberOfAttacksAfterFirstForEnemyToKillUnit = (long) ((unit.health() - 0.001) / enemyUnit.damage());
//        }
//
//        long turnsTillEnemyCanAttack = (long) enemyUnit.attackHeat()/ 10;
//        long effectiveEnemyAttackDelay = (long) enemyUnit.attackCooldown()/10;
//
//        long turnsForEnemyToKillUnit = turnsTillEnemyCanAttack + effectiveEnemyAttackDelay * numberOfAttacksAfterFirstForEnemyToKillUnit;
//
//        return turnsToKillEnemyUnit <= turnsForEnemyToKillUnit;
//    }
//
//    // TODO - Create function
//    public static boolean canWin1v1AfterMovingTo(Unit unit , Unit enemyUnit, MapLocation unitMapLocation)
//    {
//        // I require the distance from the Maplocation in front of me to the enemy location
//        return false;
//    }
//
//    // TODO - Create function and use later to harass enemy
//    public static void enemyUnitHarasser(Unit unit, Unit closestEnemyUnit, VecUnit nearbyEnemyUnits)
//    {
//
//    }
//
//    // TODO - Implement in Workers for mining locations
//    // Returns whether it's a good move to visit a certain location
//    public static boolean isItSafeToMoveTo(Unit unit, MapLocation moveUnitToLocation, VecUnit nearbyEnemyUnits)
//    {
//        Unit loneAttacker = null;
//        int numAttackers = 0;
//        for (int j = 0; j < nearbyEnemyUnits.size(); j++)
//        {
//            Unit nearbyEnemyUnit = nearbyEnemyUnits.get(j);
//            UnitType enemyUnitType = nearbyEnemyUnit.unitType();
//            if(enemyUnitType != UnitType.Ranger && enemyUnitType != UnitType.Knight && enemyUnitType != UnitType.Mage)
//            {
//                continue;
//            }
//            MapLocation nearbyEnemyUnitLocation = nearbyEnemyUnit.location().mapLocation();
//            switch (nearbyEnemyUnit.unitType())
//            {
//                case Ranger:
//                    if (moveUnitToLocation.distanceSquaredTo(nearbyEnemyUnitLocation) > 10)
//                    {
//                        return false;
//                    }
//                    break;
//                default:
//                    if (nearbyEnemyUnit.attackRange() >= moveUnitToLocation.distanceSquaredTo(nearbyEnemyUnitLocation))
//                    {
//                        numAttackers++;
//                        if (numAttackers >= 2)
//                        {
//                            return false;
//                        }
//                        loneAttacker = nearbyEnemyUnit;
//                    }
//                break;
//            }
//        }
//
//        if (numAttackers == 0)
//        {
//            return true;
//        }
//        return canWin1v1(unit, loneAttacker);
//    }

    // Find of number of units that can attack an location
    // TODO - Find total damage they can deal
//    public static int numberEnemyUnitsAimingAtLocation(MapLocation targetLocation, VecUnit nearbyEnemyUnits)
//    {
//        int locationExposure = 0;
//        for (int j = 0; nearbyEnemyUnits != null && j < nearbyEnemyUnits.size(); j++)
//        {
//            Unit nearbyEnemyUnit = nearbyEnemyUnits.get(j);
//            UnitType enemyUnitType = nearbyEnemyUnit.unitType();
//            if(enemyUnitType != UnitType.Ranger && enemyUnitType != UnitType.Knight && enemyUnitType != UnitType.Mage)
//            {
//                continue;
//            }
//            long distanceFromEnemyToLocation = targetLocation.distanceSquaredTo(nearbyEnemyUnit.location().mapLocation());
//            if (nearbyEnemyUnit.unitType() == UnitType.Ranger)
//            {
//                if(distanceFromEnemyToLocation > 10 && distanceFromEnemyToLocation < 50)
//                {
//                    locationExposure++;
//                }
//            }
//            else if (nearbyEnemyUnit.attackRange() >= distanceFromEnemyToLocation)
//            {
//                locationExposure++;
//            }
//        }
//        return locationExposure;
//    }


//    public static int numberFriendlyUnitsAimingAtLocation(MapLocation targetLocation, VecUnit nearbyFriendlyUnits)
//    {
//        int locationExposure = 0;
//        for (int j = 0; j < nearbyFriendlyUnits.size(); j++)
//        {
//            Unit nearbyFriendlyUnit = nearbyFriendlyUnits.get(j);
//            UnitType friendlyUnitType = nearbyFriendlyUnit.unitType();
//            if(friendlyUnitType != UnitType.Ranger && friendlyUnitType != UnitType.Knight && friendlyUnitType != UnitType.Mage)
//            {
//                continue;
//            }
//            long distanceFromEnemyToLocation = targetLocation.distanceSquaredTo(nearbyFriendlyUnit.location().mapLocation());
//            if (nearbyFriendlyUnit.unitType() == UnitType.Ranger)
//            {
//                if(distanceFromEnemyToLocation > 10 && distanceFromEnemyToLocation < 50)
//                {
//                    locationExposure++;
//                }
//            }
//            else if (nearbyFriendlyUnit.attackRange() >= distanceFromEnemyToLocation)
//            {
//                locationExposure++;
//            }
//        }
//        return locationExposure;
//    }
//
//    public static boolean tryMoveToEngageEnemyAtLocationInOneTurnWithMaxEnemyExposure(Unit unit, MapLocation targetLocation, int maxEnemyExposure, VecUnit nearbyEnemyUnits)
//    {
//        Direction toTargetLocation = unit.location().mapLocation().directionTo(targetLocation);
//        Direction[] tryDirection = {toTargetLocation, directions[(toTargetLocation.ordinal() + 1) % 8], directions[(toTargetLocation.ordinal() + 7) % 8]};
//        for (Direction directionToMoveTo : tryDirection)
//        {
//            if (!gc.canMove(unit.id(), directionToMoveTo))
//            {
//                continue;
//            }
//            MapLocation moveLocation = unit.location().mapLocation().add(directionToMoveTo);
//            if (unit.attackRange() < moveLocation.distanceSquaredTo(targetLocation))
//            {
//                continue; // must engage in one turn
//            }
//
//            int enemyExposure = numberEnemyUnitsAimingAtLocation(moveLocation, nearbyEnemyUnits);
//            if (enemyExposure <= maxEnemyExposure)
//            {
//                if(gc.isMoveReady(unit.id()))
//                {
//                    if(gc.canMove(unit.id(), directionToMoveTo))
//                    {
//                        gc.moveRobot(unit.id(), directionToMoveTo);
//                        return true;
//                    }
//                }
//            }
//        }
//        return false;
//    }
//
//    public static boolean tryMoveToEngageEnemyAtLocationWithMaxEnemyExposure(Unit unit, MapLocation targetLocation, int maxEnemyExposure, VecUnit nearbyEnemyUnits)
//    {
//        Direction toTargetLocation = unit.location().mapLocation().directionTo(targetLocation);
//        Direction[] tryDirection = {toTargetLocation, directions[(toTargetLocation.ordinal() + 1) % 8], directions[(toTargetLocation.ordinal() + 7) % 8]};
//        for (Direction directionToMoveTo : tryDirection) {
//            if (!gc.canMove(unit.id(), directionToMoveTo))
//            {
//                continue;
//            }
//            MapLocation moveLocation = unit.location().mapLocation().add(directionToMoveTo);
//            int enemyExposure = numberEnemyUnitsAimingAtLocation(moveLocation, nearbyEnemyUnits);
//            if (enemyExposure <= maxEnemyExposure)
//            {
//                if(unit.location().mapLocation().distanceSquaredTo(moveLocation) < 10 && unit.unitType() == UnitType.Ranger)
//                {
//                    continue;
//                }
//                else
//                {
//                    if(gc.isMoveReady(unit.id()))
//                    {
//                        if(gc.canMove(unit.id(), directionToMoveTo))
//                        {
//                            gc.moveRobot(unit.id(), directionToMoveTo);
//                            return true;
//                        }
//                    }
//                }
//            }
//        }
//        return false;
//    }
//
//    public static boolean tryMoveToEngageEnemyAtWithMaxEnemyExposure(Unit unit, MapLocation targetLocation, int maxEnemyExposure, VecUnit nearbyEnemyUnits)
//    {
//        Direction toTargetLocation = unit.location().mapLocation().directionTo(targetLocation);
//        Direction[] tryDirection = {toTargetLocation, directions[(toTargetLocation.ordinal() + 1) % 8], directions[(toTargetLocation.ordinal() + 7) % 8]};
//        for (Direction directionToMoveTo : tryDirection)
//        {
//            if (!gc.canMove(unit.id(), directionToMoveTo))
//            {
//                continue;
//            }
//            MapLocation moveLocation = unit.location().mapLocation().add(directionToMoveTo);
//            int enemyExposure = numberEnemyUnitsAimingAtLocation(moveLocation, nearbyEnemyUnits);
//            if (enemyExposure <= maxEnemyExposure)
//            {
//                gc.moveRobot(unit.id(), directionToMoveTo);
//                return true;
//            }
//        }
//        return false;
//    }

//    // TODO - Factor in Cooldown
//    private static int numberOfOtherAlliesInAttackRange(Unit unit, MapLocation targetLocation)
//    {
//        int allyAssistNumber = 0;
//        VecUnit nearbyFriendlyUnits = gc.senseNearbyUnitsByTeam(unit.location().mapLocation(), unit.visionRange(), ourTeam);
//        Unit nearbyFriendlyUnit;
//        UnitType friendlyUnitType;
//        long distanceToLocation;
//        for (int j = 0; j < nearbyFriendlyUnits.size(); j++)
//        {
//            nearbyFriendlyUnit  = nearbyFriendlyUnits.get(j);
//            friendlyUnitType = nearbyFriendlyUnit.unitType();
//            distanceToLocation = nearbyFriendlyUnit.location().mapLocation().distanceSquaredTo(targetLocation);
//            if(friendlyUnitType != UnitType.Ranger && friendlyUnitType != UnitType.Knight && friendlyUnitType != UnitType.Mage)
//            {
//                continue;
//            }
//            else if(nearbyFriendlyUnit.attackRange() >= distanceToLocation)
//            {
//                if(friendlyUnitType == UnitType.Ranger)
//                {
//                    if(distanceToLocation >= 10)
//                    {
//                        allyAssistNumber++;
//                    }
//                }
//                else
//                {
//                    allyAssistNumber++;
//                }
//            }
//        }
//        return allyAssistNumber;
//    }

    public static double getEnemyUnitPriority(UnitType enemyUnit)
    {
        double enemyUnitPriority = 1;
        if (enemyUnit == UnitType.Worker)
        {
            enemyUnitPriority = 0.5;
        }
        else if (enemyUnit == UnitType.Factory)
        {
            enemyUnitPriority = 3.0;
        }
        else if (enemyUnit == UnitType.Rocket)
        {
            enemyUnitPriority = 1.1;
        }
        else if (enemyUnit == UnitType.Knight)
        {
            enemyUnitPriority = 0.9;
        }
        else if (enemyUnit == UnitType.Healer)
        {
            enemyUnitPriority = 1.3;
        }
        else if (enemyUnit == UnitType.Mage)
        {
            enemyUnitPriority = 1.5;
        }
        else if (enemyUnit == UnitType.Ranger)
        {
            enemyUnitPriority = 1.2;
        }
        // One-shot kill
        return enemyUnitPriority;
    }
    
    // Overcharged Area
    public static void overchargedUnitsExtraTurn(Unit overchargedUnit)
    {
        Location unitLocation = overchargedUnit.location();
        // Process active unit only
        if (!unitLocation.isInGarrison() && !unitLocation.isInSpace())
        {
            if (overchargedUnit.unitType() == UnitType.Worker)
            {
                processWorker(overchargedUnit, unitLocation);
            }
            if (overchargedUnit.unitType() == UnitType.Knight)
            {
                processKnight(overchargedUnit, unitLocation);
            }
            if (overchargedUnit.unitType() == UnitType.Ranger)
            {
                processRanger(overchargedUnit, unitLocation);
            }
            if (overchargedUnit.unitType() == UnitType.Mage)
            {
                processMage(overchargedUnit, unitLocation);
            }
            if (overchargedUnit.unitType() == UnitType.Healer)
            {
                processHealer(overchargedUnit, unitLocation);
            }
            if (overchargedUnit.unitType() == UnitType.Factory)
            {
                processFactory(overchargedUnit, unitLocation);
            }
            if (overchargedUnit.unitType() == UnitType.Rocket)
            {
                processRocket(overchargedUnit, unitLocation);
            }
        }
    }

    // No enemy unit sensed
    public static void moveToEnemyBases(Unit unit, MapLocation unitMapLocation)
    {
        long nearestEnemyGridDistance = 1000000L;
        MapLocation nearestEnemyMapLocation = null;
        for(QueuePair<Double, MapLocation> enemyHotspot : enemyHotspots)
        {
            MapLocation mapLocation = enemyHotspot.getSecond();
            long enemyGridDistance = diagonalDistanceBetween(unitMapLocation, mapLocation);
            if(enemyGridDistance < nearestEnemyGridDistance)
            {
                nearestEnemyMapLocation = mapLocation;
                nearestEnemyGridDistance = enemyGridDistance;
            }
        }
        if(nearestEnemyMapLocation == null || !moveUnitTo(unit, nearestEnemyMapLocation))
        {
            long nearestEnemyFactoryDistance = 100000L;
            MapLocation nearestEnemyFactoryLocation = null;
            Iterator<MapLocation> it = enemyFactories.iterator();
            while (it.hasNext())
            {
                MapLocation factoryMapLocation = it.next();
                long enemyFactoryDistance = diagonalDistanceBetween(unitMapLocation, factoryMapLocation);
                if(enemyFactoryDistance < nearestEnemyFactoryDistance)
                {
                    nearestEnemyFactoryLocation = factoryMapLocation;
                    nearestEnemyFactoryDistance = enemyFactoryDistance;
                }
            }
            if (nearestEnemyFactoryLocation == null || !moveUnitTo(unit, nearestEnemyFactoryLocation))
            {
                if (homePlanet == Planet.Earth)
                {
                    long nearestEnemyHomeBaseDistance = 100000L;
                    MapLocation nearestEnemyHomeBase = null;
                    for(int j = 0; j < initialEnemyWorkers.size(); j++)
                    {
                        MapLocation soloEnemyInitialPosition = initialEnemyWorkers.get(j);
                        if(nearestEnemyHomeBaseDistance > unitMapLocation.distanceSquaredTo(soloEnemyInitialPosition))
                        {
                            nearestEnemyHomeBaseDistance = unitMapLocation.distanceSquaredTo(soloEnemyInitialPosition);
                            nearestEnemyHomeBase = soloEnemyInitialPosition;
                        }
                        // Since this comes under else case of no enemy units in sight
                        if(unit.visionRange() > unitMapLocation.distanceSquaredTo(soloEnemyInitialPosition))
                        {
                            initialEnemyWorkers.remove(soloEnemyInitialPosition);
                        }
                    }
                    if(nearestEnemyHomeBase != null)
                    {
                        moveUnitTo(unit, nearestEnemyHomeBase);
                    }
                }
            }
        }
    }

    // Multiple Units
    public static void doMicroRangers(Unit unit, MapLocation unitMapLocation, VecUnit nearbyEnemyUnits)
    {
        long sizeOfEnemy = (nearbyEnemyUnits != null) ? nearbyEnemyUnits.size() : 0;

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
                {
                    Unit bestTarget = null;
                    long minimumEnemyDistance = 999999L;
                    long enemyUnitHealth = 251;
                    for (int j = 0; j < nearbyEnemyUnits.size(); j++)
                    {
                        Unit enemyUnit = nearbyEnemyUnits.get(j);
                        long enemyDistance = unitMapLocation.distanceSquaredTo(enemyUnit.location().mapLocation());
                        if (enemyDistance < minimumEnemyDistance)
                        {
                            enemyUnitHealth = enemyUnit.health();
                            minimumEnemyDistance = enemyDistance;
                            bestTarget = enemyUnit;
                        }
                        else if (enemyDistance == minimumEnemyDistance && enemyUnit.health() < enemyUnitHealth)
                        {
                            enemyUnitHealth = enemyUnit.health();
                            minimumEnemyDistance = enemyDistance;
                            bestTarget = enemyUnit;
                        }
                    }
                    if (bestTarget != null)
                    {
//                    if(gc.isJavelinReady(unit.id()))
//                    {
//                        if(gc.canJavelin(unit.id(), bestTarget.id()))
//                        {
//                            gc.javelin(unit.id(), bestTarget.id());
//                        }
//                    }
                        if (gc.canAttack(unit.id(), bestTarget.id()))
                        {
                            gc.attack(unit.id(), bestTarget.id());
                        }
                        else if (unit.attackRange() < minimumEnemyDistance)
                        {
                            if (moveUnitTo(unit, bestTarget.location().mapLocation()) && gc.canAttack(unit.id(), bestTarget.id()))
                            {
                                gc.attack(unit.id(), bestTarget.id());
//                            if(gc.isJavelinReady(unit.id()))
//                            {
//                                if(gc.canJavelin(unit.id(), bestTarget.id()))
//                                {
//                                    gc.javelin(unit.id(), bestTarget.id());
//                                }
//                            }
                            }
                        }
                    }
                }
            }
            else if(unit.movementHeat() < 10)
            {
                Unit bestTarget = null;
                long minimumEnemyDistance = 99999L;
                long enemyUnitHealth = 251;
                for (int j = 0; j < nearbyEnemyUnits.size(); j++)
                {
                    Unit enemyUnit = nearbyEnemyUnits.get(j);
                    long enemyDistance = unitMapLocation.distanceSquaredTo(enemyUnit.location().mapLocation());
                    if (enemyDistance < minimumEnemyDistance)
                    {
                        enemyUnitHealth = enemyUnit.health();
                        minimumEnemyDistance = enemyDistance;
                        bestTarget = enemyUnit;
                    }
                    else if (enemyDistance == minimumEnemyDistance && enemyUnit.health() < enemyUnitHealth)
                    {
                        enemyUnitHealth = enemyUnit.health();
                        minimumEnemyDistance = enemyDistance;
                        bestTarget = enemyUnit;
                    }
                }
                if(bestTarget != null)
                {
                    moveUnitTo(unit, bestTarget.location().mapLocation());
                }
            }
        }
        else
        {
            if(!rocketPositions.isEmpty())
            {
                long distanceFromUnit = 100000L;
                MapLocation indexLocation = unitMapLocation;
                Iterator<MapLocation> rmp = rocketPositions.iterator();
                while(rmp.hasNext())
                {
                    MapLocation rocketMapLocation = rmp.next();
                    long trialDistance = unitMapLocation.distanceSquaredTo(rocketMapLocation);
                    if(unitMapLocation.isAdjacentTo(rocketMapLocation))
                    {
                        if(gc.hasUnitAtLocation(rocketMapLocation))
                        {
                            if(gc.senseUnitAtLocation(rocketMapLocation) != null)
                            {
                                if (gc.canLoad(gc.senseUnitAtLocation(rocketMapLocation).id(), unit.id()))
                                {
                                    gc.load(gc.senseUnitAtLocation(rocketMapLocation).id(), unit.id());
                                }
                            }
                        }
                        else
                        {
                            //Because dead
                            rmp.remove();
                        }
                    }
                    else if(trialDistance < distanceFromUnit)
                    {
                        distanceFromUnit = trialDistance;
                        indexLocation = rocketMapLocation;
                    }
                }
                if(currentRound < 750 && (distanceFromUnit < 40 || (currentRound > 650 && distanceFromUnit < 170) ||
                        (currentRound > 710  && distanceFromUnit < 677)))
                {
                    moveUnitTo(unit, indexLocation);
                }
            }
            else
            {
                moveToEnemyBases(unit, unitMapLocation);
            }
        }
        moveUnitInRandomDirection(unit);
    }

    // TODO - Make sure they don't cooldown units that aren't required to be overcharged
    public static void doMicroHealers(Unit unit, MapLocation unitMapLocation, VecUnit nearbyEnemyUnits, VecUnit nearbyFriendlyUnits)
    {
        boolean hasHealedThisTurn = false;
        boolean hasMovedThisTurn = false;
        int indexOfUnitWithLowestHealthInRange = -1;
        int indexOfUnitWithLowestHealthOutOfRange = -1;

        if (unitFrozenByHeat(unit))
        {
            return;
        }

        // TODO - Give combat units priority over workers
        if (gc.isHealReady(unit.id()))
        {
            if(botIntelligenceLevel == 0)
            {
                simpleHeal(unit, nearbyFriendlyUnits);
            }
            else if(nearbyFriendlyUnits != null && nearbyFriendlyUnits.size() != 0)
            {
                long heathMinimumInRange = 250;
                long heathMinimumOutOfRange = 250;

                for (int j = 0; j < nearbyFriendlyUnits.size(); j++)
                {
                    Unit nearbyFriendlyUnit = nearbyFriendlyUnits.get(j);
                    if(nearbyFriendlyUnit.unitType() != UnitType.Factory && nearbyFriendlyUnit.unitType() != UnitType.Rocket)
                    {
                        long friendlyUnitHealth = nearbyFriendlyUnit.health();
                        if (gc.canHeal(unit.id(), nearbyFriendlyUnit.id()))
                        {
                            if (friendlyUnitHealth < heathMinimumInRange && friendlyUnitHealth < nearbyFriendlyUnit.maxHealth())
                            {
                                heathMinimumInRange = friendlyUnitHealth;
                                indexOfUnitWithLowestHealthInRange = j;
                            }
                        }
                        else
                        {
                            if (friendlyUnitHealth < heathMinimumOutOfRange && friendlyUnitHealth < nearbyFriendlyUnit.maxHealth())
                            {
                                heathMinimumOutOfRange = friendlyUnitHealth;
                                indexOfUnitWithLowestHealthOutOfRange = j;
                            }
                        }
                    }
                }
                if (indexOfUnitWithLowestHealthInRange != -1)
                {
                    gc.heal(unit.id(), nearbyFriendlyUnits.get(indexOfUnitWithLowestHealthInRange).id());
                    hasHealedThisTurn = true;
                }
                else if (indexOfUnitWithLowestHealthOutOfRange != -1)
                {
                    Unit targetFriendlyUnit = nearbyFriendlyUnits.get(indexOfUnitWithLowestHealthOutOfRange);
                    int targetFriendlyUnitID = targetFriendlyUnit.id();
                    moveUnitTo(unit, targetFriendlyUnit.location().mapLocation());
                    if(gc.canHeal(unit.id(), targetFriendlyUnitID))
                    {
                        gc.heal(unit.id(), targetFriendlyUnitID);
                        hasHealedThisTurn = true;
                    }
                }
            }
        }

        // Overcharging Category
        if(gc.isOverchargeReady(unit.id()))
        {
            if(nearbyFriendlyUnits != null && nearbyFriendlyUnits.size() != 0)
            {
                long overChargedUnitIndexInRange = -1;
                long potentialOverchargedUnitPriorityInRange = 0;
                long overChargedUnitIndexOutOfRange = -1;
                long potentialOverchargedUnitPriorityOutOfRange = 0;

                for (int j = 0; j < nearbyFriendlyUnits.size(); j++)
                {
                    long unitOverChargePriority = 0;
                    Unit nearbyFriendlyUnit = nearbyFriendlyUnits.get(j);
                    if(nearbyFriendlyUnit.unitType() == UnitType.Factory || nearbyFriendlyUnit.unitType() == UnitType.Rocket ||
                            nearbyFriendlyUnit.unitType() == UnitType.Healer)
                    {
                        continue;
                    }
                    if(nearbyFriendlyUnit.movementHeat() >= 10)
                    {
                        unitOverChargePriority += 1;
                    }
                    if(nearbyFriendlyUnit.attackHeat() >= 10)
                    {
                        unitOverChargePriority += 2;
                    }
                    if(nearbyFriendlyUnit.abilityHeat() >= 10 && nearbyFriendlyUnit.unitType() != UnitType.Ranger)
                    {
                        unitOverChargePriority += 3;
                    }

                    if(unitOverChargePriority == 0)
                    {
                        continue;
                    }

                    if(nearbyFriendlyUnit.unitType() == UnitType.Worker)
                    {
                        unitOverChargePriority += 1;
                    }
                    else if(nearbyFriendlyUnit.unitType() == UnitType.Mage)
                    {
                        unitOverChargePriority += 2;
                    }
                    else if(nearbyFriendlyUnit.unitType() == UnitType.Ranger)
                    {
                        unitOverChargePriority += 3;
                    }
                    else if(nearbyFriendlyUnit.unitType() == UnitType.Knight)
                    {
                        unitOverChargePriority += 4;
                    }

                    if(unitMapLocation.distanceSquaredTo(nearbyFriendlyUnit.location().mapLocation()) <= 30)
                    {
                        if(unitOverChargePriority > potentialOverchargedUnitPriorityInRange)
                        {
                            potentialOverchargedUnitPriorityInRange = unitOverChargePriority;
                            overChargedUnitIndexInRange = j;
                        }
                    }
                    else
                    {
                        if(unitOverChargePriority > potentialOverchargedUnitPriorityOutOfRange)
                        {
                            potentialOverchargedUnitPriorityOutOfRange = unitOverChargePriority;
                            overChargedUnitIndexOutOfRange = j;
                        }
                    }
                }

                if(overChargedUnitIndexInRange != -1)
                {
                    Unit friendlyUnitToBeOverCharged = nearbyFriendlyUnits.get(overChargedUnitIndexInRange);
                    if(gc.canOvercharge(unit.id(), friendlyUnitToBeOverCharged.id()))
                    {
                        gc.overcharge(unit.id(), friendlyUnitToBeOverCharged.id());
                        overchargedUnitsExtraTurn(nearbyFriendlyUnits.get(overChargedUnitIndexInRange));
                    }
                }
                else if(overChargedUnitIndexOutOfRange != -1)
                {
                    Unit friendlyUnitToBeOverCharged = nearbyFriendlyUnits.get(overChargedUnitIndexOutOfRange);
                    if(moveUnitTo(unit, friendlyUnitToBeOverCharged.location().mapLocation()))
                    {
                        if(gc.canOvercharge(unit.id(), friendlyUnitToBeOverCharged.id()))
                        {
                            gc.overcharge(unit.id(), friendlyUnitToBeOverCharged.id());
                            overchargedUnitsExtraTurn(friendlyUnitToBeOverCharged);
                        }
                    }
                }
            }
        }

        // TODO - Implement Running/Retreating Function
        if (nearbyEnemyUnits != null && nearbyEnemyUnits.size() != 0)
        {
            moveUnitAwayFromMultipleUnits(unit, nearbyEnemyUnits);
        }
        else if(!hasHealedThisTurn)
        {
            if(!rocketPositions.isEmpty())
            {
                long distanceFromUnit = 100000L;
                MapLocation indexLocation = unitMapLocation;
                Iterator<MapLocation> rmp = rocketPositions.iterator();
                while(rmp.hasNext())
                {
                    MapLocation rocketMapLocation = rmp.next();
                    long trialDistance = unitMapLocation.distanceSquaredTo(rocketMapLocation);
                    if(unitMapLocation.isAdjacentTo(rocketMapLocation))
                    {
                        if(gc.hasUnitAtLocation(rocketMapLocation))
                        {
                            if(gc.senseUnitAtLocation(rocketMapLocation) != null)
                            {
                                if (gc.canLoad(gc.senseUnitAtLocation(rocketMapLocation).id(), unit.id()))
                                {
                                    gc.load(gc.senseUnitAtLocation(rocketMapLocation).id(), unit.id());
                                }
                            }
                        }
                        else
                        {
                            //Because dead
                            rmp.remove();
                        }
                    }
                    else if(trialDistance < distanceFromUnit)
                    {
                        distanceFromUnit = trialDistance;
                        indexLocation = rocketMapLocation;
                    }
                }
                if(currentRound < 750 && (distanceFromUnit < 40 || (currentRound > 650 && distanceFromUnit < 170) ||
                        (currentRound > 710  && distanceFromUnit < 677)))
                {
                    moveUnitTo(unit, indexLocation);
                }
            }
            else
            {
                moveToEnemyBases(unit, unitMapLocation);
            }
        }
        moveUnitInRandomDirection(unit);
    }

    public static void doMicroKnight(Unit unit, MapLocation unitMapLocation, VecUnit nearbyEnemyUnits)
    {

        long sizeOfEnemy = (nearbyEnemyUnits != null) ? nearbyEnemyUnits.size() : 0;
        if(sizeOfEnemy != 0)
        {
            if (unitFrozenByHeat(unit))
            {
                return;
            }

            if(gc.isAttackReady(unit.id()))
            {
                if(nearbyEnemyUnits.size() != 0)
                {
                    if (botIntelligenceLevel == 0)
                    {
                        simpleCombat(unit, nearbyEnemyUnits);
                    }
                    else
                    {
                        Unit bestTarget = null;
                        long minimumEnemyDistance = 999999L;
                        long enemyUnitHealth = 251;
                        for (int j = 0; j < nearbyEnemyUnits.size(); j++)
                        {
                            Unit enemyUnit = nearbyEnemyUnits.get(j);
                            long enemyDistance = unitMapLocation.distanceSquaredTo(enemyUnit.location().mapLocation());
                            if (enemyDistance < minimumEnemyDistance)
                            {
                                enemyUnitHealth = enemyUnit.health();
                                minimumEnemyDistance = enemyDistance;
                                bestTarget = enemyUnit;
                            }
                            else if (enemyDistance == minimumEnemyDistance && enemyUnit.health() < enemyUnitHealth)
                            {
                                enemyUnitHealth = enemyUnit.health();
                                minimumEnemyDistance = enemyDistance;
                                bestTarget = enemyUnit;
                            }
                        }
                        if (bestTarget != null)
                        {
                            if(gc.isJavelinReady(unit.id()))
                            {
                                if(gc.canJavelin(unit.id(), bestTarget.id()))
                                {
                                    gc.javelin(unit.id(), bestTarget.id());
                                }
                            }
                            if (gc.canAttack(unit.id(), bestTarget.id()))
                            {
                                gc.attack(unit.id(), bestTarget.id());
                                return;
                            }
                            else if (unit.attackRange() < minimumEnemyDistance)
                            {
                                if (moveUnitTo(unit, bestTarget.location().mapLocation()) && gc.canAttack(unit.id(), bestTarget.id()))
                                {
                                    gc.attack(unit.id(), bestTarget.id());
                                    if(gc.isJavelinReady(unit.id()))
                                    {
                                        if(gc.canJavelin(unit.id(), bestTarget.id()))
                                        {
                                            gc.javelin(unit.id(), bestTarget.id());
                                        }
                                    }
                                    return;
                                }
                            }
                        }
                    }
                }
            }
            else if(unit.movementHeat() < 10)
            {
                Unit bestTarget = null;
                long minimumEnemyDistance = 99999L;
                long enemyUnitHealth = 251;
                for (int j = 0; j < nearbyEnemyUnits.size(); j++)
                {
                    Unit enemyUnit = nearbyEnemyUnits.get(j);
                    long enemyDistance = unitMapLocation.distanceSquaredTo(enemyUnit.location().mapLocation());
                    if (enemyDistance < minimumEnemyDistance)
                    {
                        enemyUnitHealth = enemyUnit.health();
                        minimumEnemyDistance = enemyDistance;
                        bestTarget = enemyUnit;
                    }
                    else if (enemyDistance == minimumEnemyDistance && enemyUnit.health() < enemyUnitHealth)
                    {
                        enemyUnitHealth = enemyUnit.health();
                        minimumEnemyDistance = enemyDistance;
                        bestTarget = enemyUnit;
                    }
                }
                if(bestTarget != null)
                {
                    moveUnitTo(unit, bestTarget.location().mapLocation());
                }
            }
        }
        else
        {
            if(!rocketPositions.isEmpty())
            {
                long distanceFromUnit = 100000L;
                MapLocation indexLocation = unitMapLocation;
                Iterator<MapLocation> rmp = rocketPositions.iterator();
                while(rmp.hasNext())
                {
                    MapLocation rocketMapLocation = rmp.next();
                    long trialDistance = unitMapLocation.distanceSquaredTo(rocketMapLocation);
                    if(unitMapLocation.isAdjacentTo(rocketMapLocation))
                    {
                        if(gc.hasUnitAtLocation(rocketMapLocation))
                        {
                            if(gc.senseUnitAtLocation(rocketMapLocation) != null)
                            {
                                if (gc.canLoad(gc.senseUnitAtLocation(rocketMapLocation).id(), unit.id()))
                                {
                                    gc.load(gc.senseUnitAtLocation(rocketMapLocation).id(), unit.id());
                                }
                            }
                        }
                        else
                        {
                            //Because dead
                            rmp.remove();
                        }
                    }
                    else if(trialDistance < distanceFromUnit)
                    {
                        distanceFromUnit = trialDistance;
                        indexLocation = rocketMapLocation;
                    }
                }
                if(currentRound < 750 && (distanceFromUnit < 40 || (currentRound > 650 && distanceFromUnit < 170) ||
                        (currentRound > 710  && distanceFromUnit < 677)))
                {
                    moveUnitTo(unit, indexLocation);
                }
            }
            else
            {
                moveToEnemyBases(unit, unitMapLocation);
            }
        }
        moveUnitInRandomDirection(unit);
    }
    
    // Decides the incentive to attack an unit by Rangers
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
