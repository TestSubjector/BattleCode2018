
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

