// import the API.
import java.util.*;
import bc.*;

public class Player
{
    static Random random;
    static GameController gc;
    static Direction[] directions;
    static UnitType[] unitTypes;
    static Planet homePlanet;
    static Planet awayPlanet;
    static PlanetMap homeMap;
    static PlanetMap awayMap;
    static Team ourTeam;
    static Team theirTeam;
    static long mapWidth;
    static long mapHeight;
    static long mapSize;
    static VecUnit initialWorkers;
    static Set<MapLocation> earthKarboniteLocations;
    static Queue<MapLocation> potentialLandingSites;

    final static int INITIAL_RANGER_ATTACK_DISTANCE = 7;  // Rounding For Now
    final static int INITIAL_RANGER_MOVEMENT_COOLDOWN = 20;
    final static int INITIAL_RANGER_ATTACK_COOLDOWN = 20;
    final static int INITIAL_MAGE_ATTACK_DISTANCE = 5; // Rounding For Now
    final static int INITIAL_MAGE_MOVEMENT_COOLDOWN = 20;
    final static int INITIAL_MAGE_ATTACK_COOLDOWN = 20;
    final static int INITIAL_KNIGHT_ATTACK_DISTANCE = 1;
    final static int INITIAL_KNIGHT_MOVEMENT_COOLDOWN = 15;
    final static int INITIAL_KNIGHT_ATTACK_COOLDOWN = 20;

    //25+25+25+100+100+75+100+100+25+75+200+25+75
    final static UnitType[] RESEARCH_QUEUE_HARD = {UnitType.Worker, UnitType.Ranger, UnitType.Mage, UnitType.Rocket,
            UnitType.Ranger, UnitType.Mage, UnitType.Mage, UnitType.Rocket,
            UnitType.Healer, UnitType.Healer, UnitType.Mage, UnitType.Knight,
            UnitType.Knight};

    public static void initializeGlobals()
    {
        // Connect to the manager, starting the game
        gc = new GameController();

        // Random number generator
        random = new Random();

        // Cardinal directions
        directions = Direction.values();

        // Unit types
        unitTypes = UnitType.values();

        // Get planets and initial map states
        homePlanet = gc.planet();
        homeMap = gc.startingMap(gc.planet());
        if (homePlanet == Planet.Mars)
        {
            awayPlanet = Planet.Earth;
            awayMap = gc.startingMap(Planet.Earth);
        }
        else
        {
            awayPlanet = Planet.Mars;
            awayMap = gc.startingMap(Planet.Mars);
        }

        // Get team designations
        ourTeam = gc.team();
        if (ourTeam == Team.Blue)
        {
            theirTeam = Team.Red;
        }
        else
        {
            theirTeam = Team.Blue;
        }

        // Get map dimensions and calculate size
        mapWidth = homeMap.getWidth();
        mapHeight = homeMap.getHeight();
        mapSize = mapHeight * mapHeight;

        if (homePlanet == Planet.Earth)
        {
            // Get initial worker units
            initialWorkers = homeMap.getInitial_units();

            // Get initial karbonite locations
            earthKarboniteLocations = new HashSet<MapLocation>();
            for (int x = 0; x < mapWidth; x++)
            {
                for (int y = 0; y < mapHeight; y++)
                {
                    MapLocation tempMapLocation = new MapLocation(homePlanet, x, y);
                    long karboniteAtTempMapLocation = homeMap.initialKarboniteAt(tempMapLocation);
                    if (karboniteAtTempMapLocation > 0)
                    {
                        earthKarboniteLocations.add(tempMapLocation);
                    }
                }
            }
        }
        else
        {
            earthKarboniteLocations = null;
        }
    }

    public static boolean moveUnitInDirection(Unit unit, Direction candidateDirection)
    {
        int directionIndex = candidateDirection.swigValue();
        boolean didUnitMove = false;
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
                return true;
            }
        }
        return false;
    }

    public static boolean moveUnitTowards(Unit unit, Location targetLocation)
    {
        Direction targetDirection = unit.location().mapLocation().directionTo(targetLocation.mapLocation());
        return moveUnitInDirection(unit, targetDirection);
    }

    public static boolean moveUnitAwayFrom(Unit unit, MapLocation targetLocation)
    {
        Direction targetDirection = unit.location().mapLocation().directionTo(targetLocation);
        targetDirection = bc.bcDirectionOpposite(targetDirection);
        return moveUnitInDirection(unit, targetDirection);
    }

    public static boolean moveUnitAwayFrom(Unit unit, Location targetLocation)
    {
        return moveUnitAwayFrom(unit, targetLocation.mapLocation());
    }

    public static boolean moveUnitInRandomDirection(Unit unit)
    {
        Random random = new Random();
        return moveUnitInDirection(unit, directions[random.nextInt(8)]);
    }

    // Best Function Ever!
    // Resolve Center Direction at some later day
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

    // Unloads a robot, if possible
    public static boolean unloadRobot(Unit factory)
    {
        for (int i = 0; i < directions.length - 1; i++)
        {
            Direction unloadDirection = directions[i];
            if (gc.canUnload(factory.id(), unloadDirection))
            {
                gc.unload(factory.id(), unloadDirection);
                return true;
            }
        }
        return false;
    }

    // Both Movement and Attack on Cooldown
    // TODO - Add Ability Cooldown Later
    public static boolean unitFrozenByHeat(Unit unit)
    {
        return !gc.isAttackReady(unit.id()) && unit.movementCooldown() > 9;
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
                // TODO - Add run away instructions later
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
        else if (unitType == UnitType.Healer)
        {
            return 0;
        }
        return incentiveToHunt;
    }

    public static void main(String[] args)
    {
        initializeGlobals();

        gc.queueResearch(UnitType.Rocket);

        // Set of unfinished blueprints
        Set<Unit> unfinishedBlueprints = new HashSet<Unit>();

        // Hash map of units
        HashMap<UnitType, ArrayList<Unit>> typeSortedUnitLists = new HashMap<UnitType, ArrayList<Unit>>();
        // Initialize with empty lists
        for (int i = 0; i < unitTypes.length; i++)
        {
            typeSortedUnitLists.put(unitTypes[i], new ArrayList<Unit>());
        }

        // Queue researches
        if (gc.planet() == Planet.Mars)
        {
            for(int i = 0; i<10; i++)
            {
                gc.queueResearch(RESEARCH_QUEUE_HARD[i]);
            }
        }

        // Find potential landing spots and store in a priority queue
        // (Add priority logic later using Pair class and comparators)
        // potentialLandingSites = new PriorityQueue<QueuePair<Long, MapLocation>>();
        potentialLandingSites = new LinkedList<MapLocation>();
        if (gc.planet() == Planet.Earth)
        {
            MapLocation temp;
            for (int i = 0; i < mapWidth; i++)
            {
                for (int j = 0; j < mapHeight; j++)
                {
                    temp = new MapLocation(Planet.Mars, i, j);
                    if (awayMap.isPassableTerrainAt(temp) != 0)
                    {
                        potentialLandingSites.add(temp);
                    }
                }
            }
        }

        while (true)
        {
            long currentRound = gc.round();
            if(currentRound % 50 == 1)
            {
                System.out.println("Time left at start of round " + currentRound + " : " + gc.getTimeLeftMs());
            }

            // Clear unit lists
            for (int i = 0; i < unitTypes.length; i++)
            {
                typeSortedUnitLists.get(unitTypes[i]).clear();
            }

            // Fetch current units and sort by type
            VecUnit units = gc.myUnits();
            for (int i = 0; i < units.size(); i++)
            {
                Unit unit = units.get(i);
                typeSortedUnitLists.get(unit.unitType()).add(unit);
            }

            // Maintain a list of number of current units by type
            int[] unitsOfType = new int[typeSortedUnitLists.size()];
            for (int i = 0; i < unitsOfType.length; i++)
            {
                unitsOfType[i] = typeSortedUnitLists.get(unitTypes[i]).size();
            }

            if (homePlanet == Planet.Earth)
            {
                // Remove obsolete mine locations
                LinkedList<MapLocation> obsoleteMines = new LinkedList<MapLocation>();
                for (MapLocation karboniteMapLocation : earthKarboniteLocations)
                {
                    if (gc.canSenseLocation(karboniteMapLocation) &&
                            gc.karboniteAt(karboniteMapLocation) == 0)
                    {
                        obsoleteMines.add(karboniteMapLocation);
                    }
                }
                for (MapLocation obsoleteMine : obsoleteMines)
                {
                    earthKarboniteLocations.remove(obsoleteMine);
                }

                // Process unit
                for (int i = 0; i < unitTypes.length; i++)
                {
                    ArrayList<Unit> unitList = typeSortedUnitLists.get(unitTypes[i]);
                    for (int u = 0; u < unitList.size(); u++)
                    {
                        Unit unit = unitList.get(u);
                        Location unitLocation = unit.location();
                        // Process active unit only
                        if (!unitLocation.isInGarrison() && !unitLocation.isInSpace())
                        {
                            VecUnit adjacentUnits = gc.senseNearbyUnitsByTeam(unit.location().mapLocation(), 2, ourTeam);
                            MapLocation unitMapLocation = unitLocation.mapLocation();
                            if (unitTypes[i] == UnitType.Worker)
                            {
                                boolean workerBuiltThisTurn = false;
                                boolean workedMinedThisTurn = false;
                                boolean workedReplicatedThisTurn = false;
                                // Build a structure if adjacent to one
                                for (int j = 0; j < adjacentUnits.size(); j++)
                                {
                                    Unit adjacentUnit = adjacentUnits.get(j);
                                    if (adjacentUnit.unitType() == UnitType.Factory || adjacentUnit.unitType() == UnitType.Rocket)
                                    {
                                        if (gc.canBuild(unit.id(), adjacentUnit.id()))
                                        {
                                            gc.build(unit.id(), adjacentUnit.id());
                                            workerBuiltThisTurn = true;
                                        }
                                    }
                                }

                                // Mine karbonite if adjacent to or standing on a mine
                                for (int j = 0; j < directions.length; j++)
                                {
                                    if (gc.canHarvest(unit.id(), directions[j]))
                                    {
                                        gc.harvest(unit.id(), directions[j]);
                                        workedMinedThisTurn = true;
                                        break;
                                    }
                                }

                                // Make space for other units
                                // (Kushal's suggestion : make this the last movement priority,
                                //  moving to building and mining targets is more important)
                                if(!workedMinedThisTurn && !workerBuiltThisTurn)
                                {
                                    moveUnitAwayFromMultipleUnits(adjacentUnits, unit);
                                }


                                // Make space for other units
                                // (Kushal's suggestion : make this the last movement priority,
                                //  moving to building and mining targets is more important)
                                if (!workedMinedThisTurn && !workerBuiltThisTurn)
                                {
                                    moveUnitAwayFromMultipleUnits(adjacentUnits, unit);
                                }

                                // Replicate worker
                                if (unitsOfType[UnitType.Worker.swigValue()] < 20 && currentRound < 650 ||
                                        ((currentRound > 130 && currentRound < 400) &&
                                                (unitsOfType[UnitType.Worker.swigValue()] < mapSize * 2 / (mapHeight + mapWidth) - 10) ||
                                                unitsOfType[UnitType.Worker.swigValue()] < 10))
                                {
                                    for (int j = 0; j < directions.length - 1; j++)
                                    {
                                        Direction replicateDirection = directions[j];
                                        if (gc.canReplicate(unit.id(), replicateDirection))
                                        {
                                            gc.replicate(unit.id(), replicateDirection);
                                            unitsOfType[UnitType.Worker.swigValue()]++;
                                            workedReplicatedThisTurn = true;
                                            break;
                                        }
                                    }
                                }

                                // Remove obsolete blueprints
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
                                // Blueprint factories (change if condition)
                                if (unitsOfType[UnitType.Factory.swigValue()] < 8)
                                {
                                    for (int j = 0; j < directions.length - 1; j++)
                                    {
                                        Direction blueprintDirection = directions[j];
                                        if (gc.canBlueprint(unit.id(), UnitType.Factory, blueprintDirection))
                                        {
                                            gc.blueprint(unit.id(), UnitType.Factory, blueprintDirection);
                                            unitsOfType[UnitType.Factory.swigValue()]++;
                                        }
                                    }
                                }

                                // Blueprint rockets (change if condition)
                                if (unitsOfType[UnitType.Rocket.swigValue()] < 5)
                                {
                                    for (int j = 0; j < directions.length - 1; j++)
                                    {
                                        Direction blueprintDirection = directions[j];
                                        if (gc.canBlueprint(unit.id(), UnitType.Rocket, blueprintDirection))
                                        {
                                            gc.blueprint(unit.id(), UnitType.Rocket, blueprintDirection);
                                            unitsOfType[UnitType.Rocket.swigValue()]++;
                                        }
                                    }
                                }

                                // Move towards nearest structure
                                Unit nearestStructure = null;
                                long minDistanceSquared = (long) 1e5;
                                for (Unit structure : unfinishedBlueprints)
                                {
                                    long distanceSquaredToStructure = structure.location().mapLocation().distanceSquaredTo(unitMapLocation);
                                    if (distanceSquaredToStructure < minDistanceSquared)
                                    {
                                        nearestStructure = structure;
                                        minDistanceSquared = distanceSquaredToStructure;
                                    }
                                }
                                if (nearestStructure != null)
                                {
                                    moveUnitTowards(unit, nearestStructure.location());
                                }

                                // Move toward mines
                                MapLocation closestMineMapLocation = null;
                                MapLocation unitLoc = unit.location().mapLocation();
                                for (MapLocation karboniteMapLocation : earthKarboniteLocations)
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
                                unloadRobot(unit);
                                if (unit.isFactoryProducing() == 0)
                                {
                                    int workerCount = unitsOfType[UnitType.Worker.swigValue()]; // rarely produced
                                    int knightCount = unitsOfType[UnitType.Knight.swigValue()]; // not being produced
                                    int rangerCount = unitsOfType[UnitType.Ranger.swigValue()];
                                    int mageCount = unitsOfType[UnitType.Mage.swigValue()];
                                    int healerCount = unitsOfType[UnitType.Healer.swigValue()];

                                    // Think of better condition later; produce workers if existing ones are being massacred
                                    if (workerCount == 0)
                                    {
                                        if (gc.canProduceRobot(unit.id(), UnitType.Worker))
                                        {
                                            gc.produceRobot(unit.id(), UnitType.Worker);
                                            unitsOfType[UnitType.Worker.swigValue()]++;
                                        }
                                    }

                                    if (rangerCount >= (mageCount + healerCount))
                                    {
                                        UnitType typeToBeProduced = (mageCount > healerCount) ? (UnitType.Healer) : (UnitType.Mage);
                                        if (gc.canProduceRobot(unit.id(), typeToBeProduced))
                                        {
                                            gc.produceRobot(unit.id(), typeToBeProduced);
                                            unitsOfType[typeToBeProduced.swigValue()]++;
                                        }
                                    }
                                    else
                                    {
                                        if (gc.canProduceRobot(unit.id(), UnitType.Ranger))
                                        {
                                            gc.produceRobot(unit.id(), UnitType.Ranger);
                                            unitsOfType[UnitType.Ranger.swigValue()]++;
                                        }
                                    }
                                }
                            }
                            if (unit.unitType() == UnitType.Ranger)
                            {
                                if (!unit.location().isInGarrison())
                                {
                                    VecUnit nearbyEnemyUnits = gc.senseNearbyUnitsByTeam(unit.location().mapLocation(),
                                            70, theirTeam);

                                    // Must be refined later with movement code above this
                                    if (unitFrozenByHeat(unit))
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
                                    VecUnit nearbyEnemyUnits = gc.senseNearbyUnitsByTeam(unit.location().mapLocation(), 30, theirTeam);

                                    long desireToKill = -500;
                                    long rememberUnit = -1;
                                    if (unitFrozenByHeat(unit))
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

                                    if (unitFrozenByHeat(unit))
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
                                            50, theirTeam);

                                    long desireToKill = -500;
                                    long rememberUnit = -1;
                                    if (unitFrozenByHeat(unit))
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
                                    VecUnit nearbyUnits = gc.senseNearbyUnitsByTeam(unit.location().mapLocation(), 2, ourTeam);
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
                                        MapLocation dest = potentialLandingSites.remove();
                                        // potentialLandingSites is supposed to have only those spots
                                        // that are passable, and not already used as a destination.
                                        // Hence, this check should always pass.
                                        if (gc.canLaunchRocket(unit.id(), dest))
                                        {
                                            gc.launchRocket(unit.id(), dest);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (homePlanet == Planet.Mars)
            {
                // Process unit
                for (int i = 0; i < unitTypes.length; i++)
                {
                    ArrayList<Unit> unitList = typeSortedUnitLists.get(unitTypes[i]);
                    for (int u = 0; u < unitList.size(); u++)
                    {
                        Unit unit = unitList.get(u);
                        Location unitLocation = unit.location();
                        // Process active unit only
                        if (!unitLocation.isInGarrison() && !unitLocation.isInSpace())
                        {
                            VecUnit adjacentUnits = gc.senseNearbyUnitsByTeam(unit.location().mapLocation(), 2, ourTeam);
                            MapLocation unitMapLocation = unitLocation.mapLocation();
                            if (unit.unitType() == UnitType.Rocket)
                            {
                                for (Direction direction : directions)
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
            }

            // Submit the actions we've done, and wait for our next turn.
            gc.nextTurn();
        }
    }
}
