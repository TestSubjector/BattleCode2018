// import the API.

import bc.*;
import java.util.*;

public class Player
{
    static GameController gc;
    static PlanetMap homeMap;
    static PlanetMap awayMap;
    static long initialWorkers;
    static long mapWidth;
    static long mapHeight;
    static long mapSize;
    static Direction[] directions;
    static HashMap<Integer, Integer> timesMovementFailed;

    static final int initialRangerAttackDistance = 7;  // Rounding For Now
    static final int initialRangerMovementCooldown = 20;
    static final int initialRangerAttackCooldown = 20;
    static final int initialMageAttackDistance = 5; // Rounding For Now
    static final int initialMageMovementCooldown = 20;
    static final int initialMageAttackCooldown = 20;
    static final int initialKnightAttackDistance = 1;
    static final int initialKnightMovementCooldown = 15;
    static final int initialKnightAttackCooldown = 20;

    //25+25+25+100+100+75+100+100+25+75+200+25+75
    static final UnitType[] RESEARCH_QUEUE_HARD = {UnitType.Worker, UnitType.Ranger, UnitType.Mage, UnitType.Rocket,
                                                    UnitType.Ranger, UnitType.Mage, UnitType.Mage, UnitType.Rocket,
                                                    UnitType.Healer, UnitType.Healer, UnitType.Mage, UnitType.Knight,
                                                    UnitType.Knight};

    public static void moveUnitInDirection(Unit unit, Direction candidateDirection)
    {
        int directionIndex = candidateDirection.swigValue();
        if (gc.isMoveReady(unit.id()))
        {
            int delta = 1;
            while (!gc.canMove(unit.id(), candidateDirection) && Math.abs(delta) <= 2)
            {
                candidateDirection = directions[(((directionIndex + delta) % 8) + 8) % 8];
                delta = -delta;
                if (delta > 0)
                {
                    delta++;
                }
            }
            if (gc.canMove(unit.id(), candidateDirection))
            {
                gc.moveRobot(unit.id(), candidateDirection);
            }
        }
    }

    public static void moveUnitTowards(Unit unit, Location targetLocation)
    {
        Direction targetDirection = unit.location().mapLocation().directionTo(targetLocation.mapLocation());
        moveUnitInDirection(unit, targetDirection);
    }

    public static void moveUnitAwayFrom(Unit unit, MapLocation targetLocation)
    {
        Direction targetDirection = unit.location().mapLocation().directionTo(targetLocation);
        targetDirection = bc.bcDirectionOpposite(targetDirection);
        moveUnitInDirection(unit, targetDirection);
    }

    public static void moveUnitAwayFrom(Unit unit, Location targetLocation)
    {
        moveUnitAwayFrom(unit, targetLocation.mapLocation());
    }

    // Move Unit In Random Direction
    public static void moveUnitInRandomDirection(Unit unit)
    {
        Random random = new Random();
        moveUnitInDirection(unit, Direction.values()[1 + random.nextInt(8)]);
    }

    // Both Movement and Attack on Cooldown
    // ++++ TODO - Add Ability Cooldown Later
    public static boolean unitFrozenByHeat(GameController gc, Unit unit)
    {
        return !gc.isAttackReady(unit.id()) && unit.movementCooldown() > 9;
    }

    /**
     * Produces a robot and updates unit lists. CHECK BEFORE CALL
     *
     * @param factory             where the robot should be spawned
     * @param type                of the robot to be spawned
     * @param typeSortedUnitLists HashMap where the spawned robot will be added to keep track
     */
    public static void produceAndAddRobot(Unit factory, UnitType type, HashMap<UnitType, LinkedList<Unit>> typeSortedUnitLists)
    {
        gc.produceRobot(factory.id(), type);
        Direction unloadDirection = directions[0];
        int j = 1;
        while (j < directions.length - 1 &&
                !gc.canUnload(factory.id(), unloadDirection))
        {
            unloadDirection = directions[j++];
        }
        if (gc.canUnload(factory.id(), unloadDirection))
        {
            gc.unload(factory.id(), unloadDirection);
            MapLocation unloadLocation = factory.location().mapLocation().add(unloadDirection);
            Unit newUnit = gc.senseUnitAtLocation(unloadLocation);
            typeSortedUnitLists.get(type).add(newUnit);
        }
    }

    // Decides the incentive to attack an unit by Rangers
    // **** TODO - Make it live rather fixed static values, if computation allows
    public static long setBountyScore(Unit unit, Unit enemyUnit)
    {
        UnitType unitType = unit.unitType();
        MapLocation unitMapLocation = unit.location().mapLocation();
        long unitHealth = unit.health();

        long incentiveToHunt = enemyUnit.health() * -1;
        UnitType enemyUnitType = enemyUnit.unitType();
        MapLocation enemyMapLocation = enemyUnit.location().mapLocation();
        long distanceBetweenUnitsSquared = (int) Math.floor(Math.sqrt(unitMapLocation.distanceSquaredTo(enemyMapLocation)));

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
                // **** TODO - Add run away instructions later
                //(6 * 4 * 40 / 2)
                incentiveToHunt += 6 * (distanceBetweenUnitsSquared - 3) * 20;
            }
            else if (enemyUnitType == UnitType.Mage)
            {
                //Match steps with Mage
                //(Is infinite, if we don't consider non-perfect movement)
                incentiveToHunt += 20 * Math.floor(unitHealth * 2 / 60) + (distanceBetweenUnitsSquared - 3) * 40;
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
        else
        {
            // Unrequired else case
            return 0;
        }
        return incentiveToHunt;
    }

    public static void main(String[] args)
    {
        // Connect to the manager, starting the game
        gc = new GameController();

        // Random number generator
        Random random = new Random();

        // Cardinal directions
        directions = Direction.values();

        // Unit types
        UnitType[] unitTypes = UnitType.values();

        // Research Info
        ResearchInfo researchInfo = new ResearchInfo();

        // Get initial map states
        homeMap = gc.startingMap(gc.planet());
        if (gc.planet() == Planet.Mars)
        {
            awayMap = gc.startingMap(Planet.Earth);
        }
        else
        {
            awayMap = gc.startingMap(Planet.Mars);
        }

        Team ourTeam = gc.team();
        Team enemyTeam;
        if (ourTeam == Team.Blue)
        {
            enemyTeam = Team.Red;
        }
        else
        {
            enemyTeam = Team.Blue;
        }

        initialWorkers = homeMap.getInitial_units().size();
        mapWidth = homeMap.getWidth();
        mapHeight = homeMap.getHeight();
        mapSize = mapHeight * mapHeight;

        // Initial karbonite locations
        HashMap<MapLocation, Long> earthKarboniteLocations = new HashMap<MapLocation, Long>();

        for (int x = 0; x < mapWidth; x++)
        {
            for (int y = 0; y < mapHeight; y++)
            {
                MapLocation tempMapLocation = new MapLocation(gc.planet(), x, y);
                long karboniteAtTempMapLocation = homeMap.initialKarboniteAt(tempMapLocation);
                if (karboniteAtTempMapLocation > 0)
                {
                    earthKarboniteLocations.put(tempMapLocation, karboniteAtTempMapLocation);
                }
            }
        }

        // List of blueprints
        LinkedList<Unit> unfinishedBlueprints = new LinkedList<Unit>();

        // Hashmap of units
        HashMap<UnitType, LinkedList<Unit>> typeSortedUnitLists = new HashMap<UnitType, LinkedList<Unit>>();

        // Research code
        if (gc.planet() == Planet.Mars)
        {
            for(int i = 0; i<10; i++)
            {
                gc.queueResearch(RESEARCH_QUEUE_HARD[i]);
            }
        }

        for (int i = 0; i < unitTypes.length; i++)
        {
            typeSortedUnitLists.put(unitTypes[i], new LinkedList<Unit>());
        }

        while (true)
        {
            System.out.println("Time left at start of round " + gc.round() + " : " + gc.getTimeLeftMs());
            // System.out.println("Current round: " + gc.round());
            // System.out.println("Karbonite: " + gc.karbonite());

            // Clear unit lists
            for (int i = 0; i < unitTypes.length; i++)
            {
                typeSortedUnitLists.get(unitTypes[i]).clear();
            }

            // Fetch current units
            VecUnit units = gc.myUnits();
            for (int i = 0; i < units.size(); i++)
            {
                Unit unit = units.get(i);
                typeSortedUnitLists.get(unit.unitType()).add(unit);
            }

            // Remove obsolete karboniteMapLocations
            Set<MapLocation> karboniteMapLocationSet = earthKarboniteLocations.keySet();
            LinkedList<MapLocation> removalList = new LinkedList<MapLocation>();
            for (MapLocation karboniteMapLocation : karboniteMapLocationSet)
            {
                if (gc.canSenseLocation(karboniteMapLocation) &&
                        gc.karboniteAt(karboniteMapLocation) == 0)
                {
                    removalList.add(karboniteMapLocation);
                }
            }
            for (MapLocation obsolete : removalList)
            {
                earthKarboniteLocations.remove(obsolete);
            }

            // Unit processing
            for (int i = 0; i < unitTypes.length; i++)
            {
                LinkedList<Unit> unitList = typeSortedUnitLists.get(unitTypes[i]);
                for (int u = 0; u < unitList.size(); u++)
                {
                    Unit unit = unitList.get(u);
                    if (gc.planet() == Planet.Earth)
                    {
                        if (!unit.location().isInGarrison() && !unit.location().isInSpace())
                        {
                            if (unitTypes[i] == UnitType.Worker)
                            {
                                // Build a structure if adjacent to one
                                VecUnit nearbyUnits = gc.senseNearbyUnitsByTeam(unit.location().mapLocation(), 2, gc.team());
                                for (int j = 0; j < nearbyUnits.size(); j++)
                                {
                                    Unit nearbyUnit = nearbyUnits.get(j);
                                    if (nearbyUnit.unitType() == UnitType.Factory || nearbyUnit.unitType() == UnitType.Rocket)
                                    {
                                        if (gc.canBuild(unit.id(), nearbyUnit.id()))
                                        {
                                            gc.build(unit.id(), nearbyUnit.id());
                                        }
                                        else
                                        {
                                            moveUnitAwayFrom(unit, nearbyUnit.location());
                                        }
                                    }
                                }

                                // Karbonite mining
                                for (int j = 0; j < directions.length; j++)
                                {
                                    if (gc.canHarvest(unit.id(), directions[j]))
                                    {
                                        gc.harvest(unit.id(), directions[j]);
                                        MapLocation minedMapLocation = unit.location().mapLocation().add(directions[j]);
                                        // remove from initial locations if depleted
                                        if (gc.karboniteAt(minedMapLocation) == 0)
                                        {
                                            earthKarboniteLocations.remove(minedMapLocation);
                                        }
                                        break;
                                    }
                                }

                                // Worker replication
                                //Requires Several If-Else Conditions
                                if (unitList.size() < 20) //|| (gc.round() > 150 && unitList.size() < mapSize/20 - 10))
                                {
                                    for (int j = 0; j < directions.length - 1; j++)
                                    {
                                        Direction replicateDirection = directions[j];
                                        if (gc.canReplicate(unit.id(), replicateDirection))
                                        {
                                            gc.replicate(unit.id(), replicateDirection);
                                            unitList.add(gc.senseUnitAtLocation(unit.location().mapLocation().add(replicateDirection)));
                                            break;
                                        }
                                    }
                                }

                                // Structure building
                                LinkedList<Unit> obsoleteBlueprints = new LinkedList<Unit>();
                                for (Unit blueprint : unfinishedBlueprints)
                                {
                                    MapLocation blueprintMapLocation = blueprint.location().mapLocation();
                                    if (gc.canSenseLocation(blueprintMapLocation))
                                    {
                                        if (!gc.hasUnitAtLocation(blueprintMapLocation))
                                        {
                                            obsoleteBlueprints.add(blueprint);
                                        }
                                        else
                                        {
                                            Unit unitAtLocation = gc.senseUnitAtLocation(blueprintMapLocation);
                                            if ((unitAtLocation.unitType() == UnitType.Factory ||
                                                    unitAtLocation.unitType() == UnitType.Rocket) &&
                                                    unitAtLocation.structureIsBuilt() == 1)
                                            {
                                                obsoleteBlueprints.add(blueprint);
                                            }
                                        }
                                    }
                                }
                                for (Unit obsoleteBlueprint : obsoleteBlueprints)
                                {
                                    unfinishedBlueprints.remove(obsoleteBlueprint);
                                }
                                if (typeSortedUnitLists.get(UnitType.Factory).size() < Math.sqrt(gc.round()))
                                {
                                    Direction blueprintDirection = directions[0];
                                    int j = 1;
                                    while (j < directions.length - 1 &&
                                            (!gc.canBlueprint(unit.id(), UnitType.Factory, blueprintDirection) ||
                                                    gc.canSenseLocation(unit.location().mapLocation().add(blueprintDirection)) &&
                                                            gc.karboniteAt(unit.location().mapLocation().add(blueprintDirection)) != 0))
                                    {
                                        blueprintDirection = directions[j++];
                                    }

                                    // Copying Factory Code, needs a decision tree
                                    if (gc.canBlueprint(unit.id(), UnitType.Rocket, blueprintDirection))
                                    {
                                        gc.blueprint(unit.id(), UnitType.Rocket, blueprintDirection);
                                        MapLocation blueprintLocation = unit.location().mapLocation().add(blueprintDirection);
                                        unfinishedBlueprints.add(gc.senseUnitAtLocation(blueprintLocation));
                                    }
                                    else
                                    {
                                        blueprintDirection = directions[0];
                                        j = 1;
                                        while (j < directions.length - 1 &&
                                                !gc.canBlueprint(unit.id(), UnitType.Rocket, blueprintDirection))
                                        {
                                            blueprintDirection = directions[j++];
                                        }
                                        if (gc.canBlueprint(unit.id(), UnitType.Rocket, blueprintDirection))
                                        {
                                            gc.blueprint(unit.id(), UnitType.Rocket, blueprintDirection);
                                            MapLocation blueprintLocation = unit.location().mapLocation().add(blueprintDirection);
                                            Unit newFactory = gc.senseUnitAtLocation(blueprintLocation);
                                            unfinishedBlueprints.add(newFactory);
                                            typeSortedUnitLists.get(UnitType.Rocket).add(newFactory);
                                        }
                                    }

                                    if (gc.canBlueprint(unit.id(), UnitType.Factory, blueprintDirection))
                                    {
                                        gc.blueprint(unit.id(), UnitType.Factory, blueprintDirection);
                                        MapLocation blueprintLocation = unit.location().mapLocation().add(blueprintDirection);
                                        unfinishedBlueprints.add(gc.senseUnitAtLocation(blueprintLocation));
                                    }
                                    else
                                    {
                                        blueprintDirection = directions[0];
                                        j = 1;
                                        while (j < directions.length - 1 &&
                                                !gc.canBlueprint(unit.id(), UnitType.Factory, blueprintDirection))
                                        {
                                            blueprintDirection = directions[j++];
                                        }
                                        if (gc.canBlueprint(unit.id(), UnitType.Factory, blueprintDirection))
                                        {
                                            gc.blueprint(unit.id(), UnitType.Factory, blueprintDirection);
                                            MapLocation blueprintLocation = unit.location().mapLocation().add(blueprintDirection);
                                            Unit newFactory = gc.senseUnitAtLocation(blueprintLocation);
                                            unfinishedBlueprints.add(newFactory);
                                            typeSortedUnitLists.get(UnitType.Factory).add(newFactory);
                                        }
                                    }
                                }
                                if (!unfinishedBlueprints.isEmpty())
                                {
                                    Unit blueprint = unfinishedBlueprints.getFirst();
                                    Unit structure = gc.senseUnitAtLocation(blueprint.location().mapLocation());
                                    if (unit.location().isAdjacentTo(structure.location()))
                                    {
                                        if (gc.canBuild(unit.id(), structure.id()))
                                        {
                                            gc.build(unit.id(), structure.id());
                                        }
                                    }
                                    else
                                    {
                                        moveUnitTowards(unit, structure.location());
                                    }
                                }

                                // Move toward mines
                                karboniteMapLocationSet = earthKarboniteLocations.keySet();
                                MapLocation closestMineMapLocation = null;
                                MapLocation unitLoc = unit.location().mapLocation();
                                for (MapLocation karboniteMapLocation : karboniteMapLocationSet)
                                {
                                    if (closestMineMapLocation == null)
                                    {
                                        closestMineMapLocation = karboniteMapLocation;
                                    }
                                    else if (unitLoc.distanceSquaredTo(closestMineMapLocation) > unitLoc.distanceSquaredTo(karboniteMapLocation))
                                    {
                                        closestMineMapLocation = karboniteMapLocation;
                                    }
                                }
                                if (closestMineMapLocation != null)
                                {
                                    moveUnitInDirection(unit, unitLoc.directionTo(closestMineMapLocation));
                                }
                            }
                            if (unit.unitType() == UnitType.Factory)
                            {
                                if (unit.isFactoryProducing() == 0)
                                {
                                    int workerCount = typeSortedUnitLists.get(UnitType.Worker).size(); // rarely produced
                                    int knightCount = typeSortedUnitLists.get(UnitType.Knight).size(); // not being produced
                                    int rangerCount = typeSortedUnitLists.get(UnitType.Ranger).size();
                                    int mageCount = typeSortedUnitLists.get(UnitType.Mage).size();
                                    int healerCount = typeSortedUnitLists.get(UnitType.Healer).size();

                                    // think of better condition later; produce workers if existing ones are being massacred
                                    if (workerCount == 0)
                                    {
                                        if (gc.canProduceRobot(unit.id(), UnitType.Worker))
                                        {
                                            produceAndAddRobot(unit, UnitType.Worker, typeSortedUnitLists);
                                        }
                                    }

                                    if (rangerCount >= (mageCount + healerCount))
                                    {
                                        UnitType typeToBeProduced = (mageCount > healerCount) ? (UnitType.Healer) : (UnitType.Mage);
                                        if (gc.canProduceRobot(unit.id(), typeToBeProduced))
                                        {
                                            produceAndAddRobot(unit, typeToBeProduced, typeSortedUnitLists);
                                        }
                                    }
                                    else
                                    {
                                        if (gc.canProduceRobot(unit.id(), UnitType.Ranger))
                                        {
                                            produceAndAddRobot(unit, UnitType.Ranger, typeSortedUnitLists);
                                        }
                                    }
                                }
                            }
                            if (unit.unitType() == UnitType.Ranger)
                            {
                                if (!unit.location().isInGarrison())
                                {
                                    VecUnit nearbyEnemyUnits = gc.senseNearbyUnitsByTeam(unit.location().mapLocation(),
                                            70, enemyTeam);

                                    // Must be refined later with movement code above this
                                    if (unitFrozenByHeat(gc, unit))
                                    {
                                        continue;
                                    }

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
                                                long possibleDesireToKill = setBountyScore(unit, nearbyEnemyUnit);
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
                            if (unit.unitType() == UnitType.Mage)
                            {
                                if (!unit.location().isInGarrison())
                                {
                                    VecUnit nearbyEnemyUnits = gc.senseNearbyUnitsByTeam(unit.location().mapLocation(),
                                            30, enemyTeam);

                                    long desireToKill = -500;
                                    long rememberUnit = -1;
                                    if (unitFrozenByHeat(gc, unit))
                                    {
                                        continue;
                                    }
                                    // Convenient because Attack Range = Vision Range for Mage
                                    for (int j = 0; j < nearbyEnemyUnits.size(); j++)
                                    {
                                        Unit nearbyEnemyUnit = nearbyEnemyUnits.get(j);
                                        {
                                            if (gc.canAttack(unit.id(), nearbyEnemyUnit.id()))
                                            {
                                                long possibleDesireToKill = setBountyScore(unit, nearbyEnemyUnit);
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
                                    }
                                    else
                                    {
                                        moveUnitInRandomDirection(unit);
                                    }

                                }
                            }
                            if (unit.unitType() == UnitType.Healer)
                            {
                                if (!unit.location().isInGarrison())
                                {
                                    VecUnit nearbyFriendlyUnits = gc.senseNearbyUnitsByTeam(unit.location().mapLocation(),
                                            50, ourTeam);

                                    if (unitFrozenByHeat(gc, unit))
                                    {
                                        continue;
                                    }

                                    for (int j = 0; j < nearbyFriendlyUnits.size(); j++)
                                    {
                                        Unit nearbyFriendlyUnit = nearbyFriendlyUnits.get(j);
                                        {
                                            if (gc.canHeal(unit.id(), nearbyFriendlyUnit.id()))
                                            {
                                                gc.heal(unit.id(), nearbyFriendlyUnit.id());
                                                break;
                                            }
                                        }
                                    }
                                    moveUnitInRandomDirection(unit);
                                }
                            }
                            if (unit.unitType() == UnitType.Knight)
                            {
                                if (!unit.location().isInGarrison())
                                {
                                    VecUnit nearbyEnemyUnits = gc.senseNearbyUnitsByTeam(unit.location().mapLocation(),
                                            50, enemyTeam);

                                    long desireToKill = -500;
                                    long rememberUnit = -1;
                                    if (unitFrozenByHeat(gc, unit))
                                    {
                                        continue;
                                    }
                                    // Convenient because Attack Range = Vision Range for Mage
                                    for (int j = 0; j < nearbyEnemyUnits.size(); j++)
                                    {
                                        Unit nearbyEnemyUnit = nearbyEnemyUnits.get(j);
                                        {
                                            if (gc.canAttack(unit.id(), nearbyEnemyUnit.id()))
                                            {
                                                long possibleDesireToKill = setBountyScore(unit, nearbyEnemyUnit);
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
                                    }
                                    else
                                    {
                                        moveUnitInRandomDirection(unit);
                                    }
                                }
                            }
                            if (unit.unitType() == UnitType.Rocket)
                            {
                                if (unit.structureIsBuilt() == 1)
                                {
                                    // Check all adjacent squares
                                    VecUnit nearbyUnits = gc.senseNearbyUnitsByTeam(unit.location().mapLocation(), 2, gc.team());
                                    for (int j = 0; j < nearbyUnits.size(); j++)
                                    {
                                        Unit nearbyUnit = nearbyUnits.get(j);
                                        if (gc.canLoad(unit.id(), nearbyUnit.id()))
                                        {
                                            gc.load(unit.id(), nearbyUnit.id());
                                        }
                                    }
                                    if (unit.structureGarrison().size() >= unit.structureMaxCapacity() / 2)
                                    {
                                        int x = random.nextInt((int) mapWidth);
                                        int y = random.nextInt((int) mapHeight);
                                        MapLocation randomLocationOnMars = new MapLocation(Planet.Mars, x, y);
                                        if (gc.canLaunchRocket(unit.id(), randomLocationOnMars))
                                        {
                                            gc.launchRocket(unit.id(), new MapLocation(Planet.Mars, x, y));
                                        }
                                        /*
                                        MapLocation mapLocation;
                                        for(int x = 0; x < mapWidth; x++)
                                        {
                                            for(int y = 0; x < mapHeight; y++)
                                            {

                                            }
                                        }
                                        */
                                    }
                                }

                            }
                        }
                    }
                    else
                    {
                        // Mars code here
                        if (unit.unitType() == UnitType.Rocket)
                        {
                            for (Direction direction : Direction.values())
                            {
                                if (gc.canUnload(unit.id(), direction))
                                {
                                    gc.unload(unit.id(), direction);
                                }
                            }
                        }
                    }
                }
            }
            // Submit the actions we've done, and wait for our next turn.
            gc.nextTurn();
        }
    }
}
