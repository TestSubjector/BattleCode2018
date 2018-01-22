
// currently our micro looks like this:
// if we are getting owned, and we have core delay, try to retreat
// if we can hit an enemy, attack if our weapon delay is up. otherwise sit still
// try to stick to enemy harassers, engaging them if we can win the 1v1
// try to move toward undefended workers, engaging them if we can win the 1v1

// here's a better micro:
// if we are getting hit:
// - if we are getting owned and have core delay and can retreat, do so
// - otherwise hit an enemy if we can
// if we are not getting hit:
// - if we can assist an ally who is engaged, do so
// - if we can move to engage a worker, do so
// - if there is an enemy harasser nearby, stick to them
// - - optionally, engage if we can win the 1v1 or if there is a lot of allied support

// it's definitely good to take 1v1s if there are no nearby enemies. however we maybe
// should avoid initiating 1v1s if there are enemies nearby that can support.

private static boolean doMicro(RobotInfo[] nearbyEnemies, boolean shadowEnemyHarassers) throws GameActionException {

        } else {
        RobotInfo closestEnemy = Util.closest(moreEnemies, here);
        if (closestEnemy != null && isHarasser(closestEnemy.type) && rc.getType().attackRadiusSquared >= closestEnemy.type.attackRadiusSquared) {
        // Debug.indicate("micro", 0, "no nearby enemies, shadowing an enemy at long range");
        if (rc.isCoreReady()) {
        shadowHarasser(closestEnemy, nearbyEnemies);
        }
        return true;
        }
        }
        }


        int numEnemiesAttackingUs = 0;
        RobotInfo[] enemiesAttackingUs = new RobotInfo[99];
        for (RobotInfo enemy : nearbyEnemies) {
        if (enemy.type.attackRadiusSquared >= here.distanceSquaredTo(enemy.location)) {
        enemiesAttackingUs[numEnemiesAttackingUs++] = enemy;
        }
        }

        // TODO: below cases don't handle missiles very well
        // TODO: possible also don't handle launchers well

        if (numEnemiesAttackingUs > 0) {
        // we are in combat
        if (numEnemiesAttackingUs == 1) {
        // we are in a 1v1

        } else {
        // we are getting shot by someone who outranges us. run away!
        // Debug.indicate("micro", 0, "trying to retreat from a 1v1 where we are outranged");
        tryToRetreat(nearbyEnemies);
        return true;
        }
        } else {
        RobotInfo bestTarget = null;
        double bestTargetingMetric = 0;
        int maxAlliesAttackingAnEnemy = 0;
        for (int i = 0; i < numEnemiesAttackingUs; i++) {
        RobotInfo enemy = enemiesAttackingUs[i];
        int numAlliesAttackingEnemy = 1 + numOtherAlliesInAttackRange(enemy.location);
        if (numAlliesAttackingEnemy > maxAlliesAttackingAnEnemy) maxAlliesAttackingAnEnemy = numAlliesAttackingEnemy;
        if (rc.getType().attackRadiusSquared >= here.distanceSquaredTo(enemy.location)) {
        double targetingMetric = numAlliesAttackingEnemy / enemy.health;
        if (targetingMetric > bestTargetingMetric) {
        bestTargetingMetric = targetingMetric;
        bestTarget = enemy;
        }
        }
        }

        // multiple enemies are attacking us. stay in the fight iff enough allies are also engaged
        if (maxAlliesAttackingAnEnemy >= numEnemiesAttackingUs) {
        // enough allies are in the fight.
        // Debug.indicate("micro", 0, "attacking because numEnemiesAttackingUs = " + numEnemiesAttackingUs + ", maxAlliesAttackingEnemy = "
        // + maxAlliesAttackingAnEnemy);
        attackIfReady(bestTarget.location);
        return true;
        } else {
        // not enough allies are in the fight. we need to retreat
        if (rc.isCoreReady()) {
        // we can move this turn
        if (tryToRetreat(nearbyEnemies)) {
        // we moved away
        // Debug.indicate("micro", 0, "retreated because numEnemiesAttackingUs = " + numEnemiesAttackingUs + ", maxAlliesAttackingEnemy = "
        // + maxAlliesAttackingAnEnemy);
        return true;
        } else {
        // we couldn't find anywhere to retreat to. fire a desperate shot if possible
        // Debug.indicate("micro", 0, "no retreat square :( numEnemiesAttackingUs = " + numEnemiesAttackingUs +
        // ", maxAlliesAttackingEnemy = "
        // + maxAlliesAttackingAnEnemy);
        attackIfReady(bestTarget.location);
        return true;
        }
        } else {
        // we can't move this turn. if it won't delay retreating, shoot instead
        // Debug.indicate("micro", 0, "want to retreat but core on cooldown :( numEnemiesAttackingUs = " + numEnemiesAttackingUs
        // + ", maxAlliesAttackingEnemy = " + maxAlliesAttackingAnEnemy);
        if (rc.getType().cooldownDelay <= 1) {
        attackIfReady(bestTarget.location);
        }
        return true;
        }
        }
        }
        } else {
        // no one is shooting at us. if we can shoot at someone, do so
        RobotInfo bestTarget = null;
        double minHealth = 1e99;
        for (RobotInfo enemy : nearbyEnemies) {
        if (rc.getType().attackRadiusSquared >= here.distanceSquaredTo(enemy.location)) {
        if (enemy.health < minHealth) {
        minHealth = enemy.health;
        bestTarget = enemy;
        }
        }
        }

        // shoot someone if there is someone to shoot
        if (bestTarget != null) {
        // Debug.indicate("micro", 0, "shooting an enemy while no one can shoot us");
        attackIfReady(bestTarget.location);
        return true;
        }

        // we can't shoot anyone

        if (rc.isCoreReady()) { // all remaining possible actions are movements
        // check if we can move to help an ally who has already engaged a nearby enemy
        RobotInfo closestEnemy = Util.closest(nearbyEnemies, here);
        // we can only think about engage enemies with equal or shorter range, and we shouldn't try to engage missiles
        if (closestEnemy != null && rc.getType().attackRadiusSquared >= closestEnemy.type.attackRadiusSquared && closestEnemy.type != RobotType.MISSILE) {
        int numAlliesFightingEnemy = numOtherAlliesInAttackRange(closestEnemy.location);

        if (numAlliesFightingEnemy > 0) {
        // see if we can assist our ally(s)
        int maxEnemyExposure = numAlliesFightingEnemy;
        if (tryMoveTowardLocationWithMaxEnemyExposure(closestEnemy.location, maxEnemyExposure, nearbyEnemies)) {
        // Debug.indicate("micro", 0, "moved to assist allies against " + closestEnemy.location.toString() + "; maxEnemyExposure = "
        // + maxEnemyExposure);
        return true;
        }
        } else {
        // no one is fighting this enemy, but we can try to engage them if we can win the 1v1
        if (canWin1v1AfterMovingTo(here.add(here.directionTo(closestEnemy.location)), closestEnemy)) {
        int maxEnemyExposure = 1;
        if (tryMoveToEngageEnemyAtLocationInOneTurnWithMaxEnemyExposure(closestEnemy.location, maxEnemyExposure, nearbyEnemies)) {
        // Debug.indicate("micro", 0, "moved to engage enemy we can 1v1");
        return true;
        }
        }
        }
        }

        // try to move toward and kill an enemy worker
        if (tryMoveToEngageAnyUndefendedWorkerOrBuilding(nearbyEnemies)) {
        // Debug.indicate("micro", 0, "moved to engage an undefended worker or building");
        return true;
        }

        if (shadowEnemyHarassers) {
        if (closestEnemy != null && isHarasser(closestEnemy.type) && rc.getType().attackRadiusSquared >= closestEnemy.type.attackRadiusSquared) {
        // Debug.indicate("micro", 0, "shadowing " + closestEnemy.location.toString());
        shadowHarasser(closestEnemy, nearbyEnemies);
        return true;
        }
        }

        // no required actions
        // Debug.indicate("micro", 0, "no micro action though core is ready and there are nearby enemies");
        return false;
        }

        // return true here because core is not ready, so it's as if we took a required action
        // in the sense that we can't do anything else
        // Debug.indicate("micro", 0, "no micro action; core isn't ready");
        return true;
        }
        }

private static boolean tryFleeMissiles(RobotInfo[] nearbyEnemies) throws GameActionException {
        if (!rc.isCoreReady()) return false;

        boolean needToFlee = false;
        for (RobotInfo enemy : nearbyEnemies) {
        if (enemy.type == RobotType.MISSILE && here.distanceSquaredTo(enemy.location) <= 8) {
        needToFlee = true;
        break;
        }
        }

        if (needToFlee) {
        if (tryToRetreat(nearbyEnemies)) {
        return true;
        }
        }

        return false;
        }



private static boolean tryMoveToEngageAnyUndefendedWorkerOrBuilding(RobotInfo[] nearbyEnemies) throws GameActionException {
        for (RobotInfo enemy : nearbyEnemies) {
        if (isWorkerOrBuilding(enemy.type)) {
        if (canWin1v1(enemy)) {
        boolean canReach = true;
        MapLocation loc = here;
        while (loc.distanceSquaredTo(enemy.location) > rc.getType().attackRadiusSquared) {
        Direction dir = loc.directionTo(enemy.location);
        MapLocation newLoc = loc.add(dir);

        if (!rc.isPathable(rc.getType(), newLoc) || inEnemyTowerOrHQRange(newLoc, enemyTowers)) {
        canReach = false;
        break;
        }

        boolean noOtherEnemiesAttackNewLoc = true;
        for (RobotInfo otherEnemy : nearbyEnemies) {
        if (otherEnemy != enemy
        && (otherEnemy.type.attackRadiusSquared >= newLoc.distanceSquaredTo(otherEnemy.location) || (otherEnemy.type == RobotType.MISSILE && 15 >= newLoc
        .distanceSquaredTo(otherEnemy.location)))) {
        noOtherEnemiesAttackNewLoc = false;
        break;
        }
        }

        if (noOtherEnemiesAttackNewLoc) {
        loc = newLoc;
        } else {
        canReach = false;
        break;
        }
        }
        if (canReach) {
        Direction moveDir = here.directionTo(enemy.location);
        rc.move(moveDir);
        return true;
        }
        }
        }
        }

        return false;
        }

private static void shadowHarasser(RobotInfo enemyToShadow, RobotInfo[] nearbyEnemies) throws GameActionException {
        Direction toEnemy = here.directionTo(enemyToShadow.location);
        Direction[] dirs = new Direction[] { toEnemy, toEnemy.rotateRight(), toEnemy.rotateLeft(), toEnemy.rotateRight().rotateRight(),
        toEnemy.rotateLeft().rotateLeft() };
        for (Direction dir : dirs) {
        if (!rc.canMove(dir)) continue;

        MapLocation loc = here.add(dir);
        if (inEnemyTowerOrHQRange(loc, enemyTowers)) continue;

        boolean locIsSafe = true;

        for (RobotInfo enemy : nearbyEnemies) {
        if (enemy.type.attackRadiusSquared >= loc.distanceSquaredTo(enemy.location)
        || (enemy.type == RobotType.MISSILE && 15 >= loc.distanceSquaredTo(enemy.location))) {
        locIsSafe = false;
        break;
        }
        }

        if (locIsSafe) {
        rc.move(dir);
        break;
        }
        }
        }

