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
            for (int j = 0; j < nearbyEnemyUnits.size(); j++)
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

        long turnsTillUnitCanAttack = unit.attackHeat() / 10;
        long effectiveAttackDelay = unit.attackCooldown() / 10;
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
        Direction[] tryDirection = {toTargetLocation, directions[(toTargetLocation.ordinal() + 1) % 8], directions[(toTargetLocation.ordinal() + 7) % 8]};
        for (Direction directionToMoveTo : tryDirection)
        {
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
                if(gc.isMoveReady(unit.id()))
                {
                    if(gc.canMove(unit.id(), directionToMoveTo))
                    {
                        gc.moveRobot(unit.id(), directionToMoveTo);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean tryMoveToEngageEnemyAtLocationWithMaxEnemyExposure(Unit unit, MapLocation targetLocation, int maxEnemyExposure, VecUnit nearbyEnemyUnits)
    {
        Direction toTargetLocation = unit.location().mapLocation().directionTo(targetLocation);
        Direction[] tryDirection = {toTargetLocation, directions[(toTargetLocation.ordinal() + 1) % 8], directions[(toTargetLocation.ordinal() + 7) % 8]};
        for (Direction directionToMoveTo : tryDirection) {
            if (!gc.canMove(unit.id(), directionToMoveTo))
            {
                continue;
            }
            MapLocation moveLocation = unit.location().mapLocation().add(directionToMoveTo);
            int enemyExposure = numberEnemyUnitsAimingAtLocation(moveLocation, nearbyEnemyUnits);
            if (enemyExposure <= maxEnemyExposure)
            {
                if(unit.location().mapLocation().distanceSquaredTo(moveLocation) < 10 && unit.unitType() == UnitType.Ranger)
                {
                    continue;
                }
                else
                {
                    if(gc.isMoveReady(unit.id()))
                    {
                        if(gc.canMove(unit.id(), directionToMoveTo))
                        {
                            gc.moveRobot(unit.id(), directionToMoveTo);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public static boolean tryMoveToEngageEnemyAtWithMaxEnemyExposure(Unit unit, MapLocation targetLocation, int maxEnemyExposure, VecUnit nearbyEnemyUnits)
    {
        Direction toTargetLocation = unit.location().mapLocation().directionTo(targetLocation);
        Direction[] tryDirection = {toTargetLocation, directions[(toTargetLocation.ordinal() + 1) % 8], directions[(toTargetLocation.ordinal() + 7) % 8]};
        for (Direction directionToMoveTo : tryDirection)
        {
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
        VecUnit nearbyFriendlyUnits = gc.senseNearbyUnitsByTeam(unit.location().mapLocation(), unit.visionRange(), ourTeam);
        Unit nearbyFriendlyUnit;
        UnitType friendlyUnitType;
        long distanceToLocation;
        for (int j = 0; j < nearbyFriendlyUnits.size(); j++)
        {
            nearbyFriendlyUnit  = nearbyFriendlyUnits.get(j);
            friendlyUnitType = nearbyFriendlyUnit.unitType();
            distanceToLocation = nearbyFriendlyUnit.location().mapLocation().distanceSquaredTo(targetLocation);
            if(friendlyUnitType != UnitType.Ranger && friendlyUnitType != UnitType.Knight && friendlyUnitType != UnitType.Mage)
            {
                continue;
            }
            else if(nearbyFriendlyUnit.attackRange() >= distanceToLocation)
            {
                if(friendlyUnitType == UnitType.Ranger)
                {
                    if(distanceToLocation >= 10)
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

    // Multiple Units
    public static void doMicroRangers(Unit unit, MapLocation unitMapLocation, VecUnit nearbyEnemyUnits)
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
                {
                    if(sizeOfEnemy == 1)
                    {
                        Unit loneEnemyUnit = nearbyEnemyUnits.get(0);
                        MapLocation loneEnemyUnitMapLocation = loneEnemyUnit.location().mapLocation();
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
                                if(numberOfOtherAlliesInAttackRange(unit,loneEnemyUnitMapLocation ) > 0)
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
                                            if(moveUnitAwayFrom(unit, loneEnemyUnitMapLocation))
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
                            if(unit.location().mapLocation().distanceSquaredTo(loneEnemyUnitMapLocation) > 10)
                            {
                                moveUnitTo(unit, loneEnemyUnit.location().mapLocation());
                            }
                            else
                            {
                                moveUnitAwayFrom(unit, loneEnemyUnitMapLocation);
                            }
                        }
                    }
                    // Multiple Units
                    else
                    {
                        Unit bestTarget = null;
                        double bestTargetingMetric = 0;
                        double maxAllyUnitsAttackingAnEnemy = 0;
                        for (int j = 0; j < nearbyEnemyUnits.size(); j++)
                        {
                            Unit nearbyEnemyUnit = nearbyEnemyUnits.get(j);
                            UnitType enemyUnitType = nearbyEnemyUnit.unitType();
                            int attackNumber = numberOfOtherAlliesInAttackRange(unit, nearbyEnemyUnit.location().mapLocation());
                            if(attackNumber > maxAllyUnitsAttackingAnEnemy)
                            {
                                maxAllyUnitsAttackingAnEnemy = attackNumber;
                                double targetingMetric = getEnemyUnitPriority(enemyUnitType) * attackNumber/ nearbyEnemyUnit.health();
                                if(targetingMetric > bestTargetingMetric)
                                {
                                    bestTargetingMetric = targetingMetric;
                                    bestTarget = nearbyEnemyUnit;
                                }
                            }
                        }
                        if(bestTarget != null)
                        {
                            if(gc.canAttack(unit.id(), bestTarget.id()))
                            {
                                gc.attack(unit.id(), bestTarget.id());
                                return;
                            }
                            else if(unit.attackRange() < unitMapLocation.distanceSquaredTo(bestTarget.location().mapLocation()))
                            {
                                // TODO - Retreat function here
                                if(moveUnitTo(unit, bestTarget.location().mapLocation()) && gc.canAttack(unit.id(), bestTarget.id()))
                                {
                                    // Desperate attack
                                    gc.attack(unit.id(), bestTarget.id());
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
                double bestTargetingMetric = 0;
                double maxAllyUnitsAttackingAnEnemy = 0;
                for (int j = 0; j < nearbyEnemyUnits.size(); j++)
                {
                    Unit nearbyEnemyUnit = nearbyEnemyUnits.get(j);
                    UnitType enemyUnitType = nearbyEnemyUnit.unitType();
                    int attackNumber = numberOfOtherAlliesInAttackRange(unit, nearbyEnemyUnit.location().mapLocation());
                    if(attackNumber > maxAllyUnitsAttackingAnEnemy)
                    {
                        maxAllyUnitsAttackingAnEnemy = attackNumber;
                        double targetingMetric = getEnemyUnitPriority(enemyUnitType) * attackNumber/ nearbyEnemyUnit.health();
                        if(targetingMetric > bestTargetingMetric)
                        {
                            bestTargetingMetric = targetingMetric;
                            bestTarget = nearbyEnemyUnit;
                        }
                    }
                }
                if(bestTarget != null && unit.attackRange() < unitMapLocation.distanceSquaredTo(bestTarget.location().mapLocation()))
                {
                    moveUnitTo(unit, bestTarget.location().mapLocation());
                }
            }
        }
        else
        {
            long nearestEnemyGridDistance = 100000L;
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
            if(nearestEnemyMapLocation == null || !moveUnitTo(unit, nearestEnemyMapLocation));
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
                    moveUnitTo(unit, initialEnemyWorkers.peek());
                }
            }
        }
    }

    public static  void doMicroHealers(Unit unit, MapLocation unitMapLocation, VecUnit nearbyEnemyUnits, VecUnit nearbyFriendlyUnits)
    {
        boolean hasHealedThisTurn = false;
        boolean hasMovedThisTurn = false;
        int indexOfUnitWithLowestHealthInRange = -1;
        int indexOfUnitWithLowestHealthOutOfRange = -1;

        if (unitFrozenByHeat(unit)) {
            return;
        }

        if (gc.isHealReady(unit.id())) {
            long heathMinimumInRange = 250;
            long heathMinimumOutOfRange = 250;
            for (int j = 0; j < nearbyFriendlyUnits.size(); j++) {
                Unit nearbyFriendlyUnit = nearbyFriendlyUnits.get(j);
                {
                    long friendlyUnitHealth = nearbyFriendlyUnit.health();
                    if (gc.canHeal(unit.id(), nearbyFriendlyUnit.id())) {
                        if (friendlyUnitHealth < heathMinimumInRange && friendlyUnitHealth < nearbyFriendlyUnit.maxHealth()) {
                            heathMinimumInRange = friendlyUnitHealth;
                            indexOfUnitWithLowestHealthInRange = j;
                        }
                    }
                    else {
                        if (friendlyUnitHealth < nearbyFriendlyUnit.maxHealth())
                        {
                            indexOfUnitWithLowestHealthOutOfRange = j;
                        }
                    }
                }
            }
            if (indexOfUnitWithLowestHealthInRange != -1) {
                gc.heal(unit.id(), nearbyFriendlyUnits.get(indexOfUnitWithLowestHealthInRange).id());
                hasHealedThisTurn = true;
            }
            else if (indexOfUnitWithLowestHealthOutOfRange != -1) {
                moveUnitTo(unit, nearbyFriendlyUnits.get(indexOfUnitWithLowestHealthOutOfRange).location().mapLocation());
                hasMovedThisTurn = true;
            }
        }

        // TODO - Implement Running/Retreating Function
        if (nearbyEnemyUnits.size() != 0)
        {
            if(!hasMovedThisTurn)
            {
                moveUnitAwayFromMultipleUnits(unit, nearbyEnemyUnits);
            }
        }
        else if(!hasHealedThisTurn)
        {
            long nearestEnemyGridDistance = 100000L;
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
            if(nearestEnemyMapLocation == null || !moveUnitTo(unit, nearestEnemyMapLocation));
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
                    moveUnitTo(unit, initialGuesses.peek());
                }
            }
        }
    }

    public static void doMicroKnight(Unit unit, MapLocation unitMapLocation, VecUnit nearbyEnemyUnits)
    {
        long sizeOfEnemy = nearbyEnemyUnits.size();
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
                        if (bestTarget != null)
                        {
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
            long nearestEnemyGridDistance = 100000L;
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
            if(nearestEnemyMapLocation == null || !moveUnitTo(unit, nearestEnemyMapLocation));
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
                    moveUnitTo(unit, initialEnemyWorkers.peek());
                }
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
