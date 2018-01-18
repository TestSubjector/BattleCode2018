// import the API.
import java.util.*;
import bc.*;

public class Player
{
    static Random random;
    static GameController gc;
    static Direction[] directions;
    static ArrayList<Direction> randomDirections;
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
    static long earthInitialTotalKarbonite = 0;
    static Set<MapLocation> earthKarboniteLocations;
    static PriorityQueue<QueuePair<Long, MapLocation>> potentialLandingSites;
    static ArrayList<QueuePair<Long, MapLocation>> updatedAppealSites;
    static ArrayList<ArrayList<Long>> potentialFactorySpots;

    final static int INITIAL_RANGER_ATTACK_DISTANCE = 7;  // Rounding For Now
    final static int INITIAL_RANGER_MOVEMENT_COOLDOWN = 20;
    final static int INITIAL_RANGER_ATTACK_COOLDOWN = 20;
    final static int INITIAL_MAGE_ATTACK_DISTANCE = 5; // Rounding For Now
    final static int INITIAL_MAGE_MOVEMENT_COOLDOWN = 20;
    final static int INITIAL_MAGE_ATTACK_COOLDOWN = 20;
    final static int INITIAL_KNIGHT_ATTACK_DISTANCE = 1;
    final static int INITIAL_KNIGHT_MOVEMENT_COOLDOWN = 15;
    final static int INITIAL_KNIGHT_ATTACK_COOLDOWN = 20;
    static int rangerTalentVision = 0;

    final static long WEIGHT_IMPASSABLE = -2;
    final static long WEIGHT_ROCKET = -1;
    final static long WEIGHT_NONE = 0;
    // 25+25+100+100+100+25+75+25+100+75+100+25+75+100+25+75
    final static UnitType[] RESEARCH_QUEUE_HARD = {UnitType.Worker, UnitType.Ranger, UnitType.Ranger, UnitType.Rocket,
            UnitType.Rocket, UnitType.Healer, UnitType.Healer, UnitType.Worker, UnitType.Rocket,
            UnitType.Worker, UnitType.Worker, UnitType.Mage, UnitType.Mage, UnitType.Mage,
            UnitType.Knight, UnitType.Knight};

    public static void initializeGlobals()
    {
        // Connect to the manager, starting the game
        gc = new GameController();

        // Random number generator
        random = new Random();

        // Cardinal directions
        directions = Direction.values();

        // Direction list (to be randomized before each use)
        randomDirections = new ArrayList<Direction>(Arrays.asList(directions));

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

        // Get initial worker units
        initialWorkers = homeMap.getInitial_units();

        if (homePlanet == Planet.Earth)
        {
            // Get initial karbonite locations and factory appeal values
            earthKarboniteLocations = new HashSet<MapLocation>();
            potentialFactorySpots = new ArrayList<>();
            for (int x = 0; x < mapWidth; x++)
            {
                potentialFactorySpots.add(new ArrayList<Long>((int)mapHeight));
                for (int y = 0; y < mapHeight; y++)
                {
                    MapLocation tempMapLocation = new MapLocation(homePlanet, x, y);
                    long karboniteAtTempMapLocation = homeMap.initialKarboniteAt(tempMapLocation);

                    if (karboniteAtTempMapLocation > 0)
                    {
                        earthKarboniteLocations.add(tempMapLocation);
                        earthInitialTotalKarbonite += karboniteAtTempMapLocation;
                    }

                    if (homeMap.isPassableTerrainAt(tempMapLocation) != 0)
                    {
                        long appeal = WEIGHT_NONE;

                        // top row
                        appeal += getFactoryLocationAppeal(x - 1,y + 1);
                        appeal += getFactoryLocationAppeal(x,y + 1);
                        appeal += getFactoryLocationAppeal(x + 1,y + 1);

                        // middle row
                        appeal += getFactoryLocationAppeal(x - 1,y);
                        appeal += getFactoryLocationAppeal(x + 1,y);

                        // bottom row
                        appeal += getFactoryLocationAppeal(x - 1,y - 1);
                        appeal += getFactoryLocationAppeal(x,y - 1);
                        appeal += getFactoryLocationAppeal(x + 1,y - 1);

                        potentialFactorySpots.get(x).add(appeal);
                    }
                    else
                    {
                        potentialFactorySpots.get(x).add(-1000L);
                    }
                }
            }
        }
        else
        {
            earthKarboniteLocations = null;
        }
        potentialLandingSites = new PriorityQueue<QueuePair<Long, MapLocation>>();
        updatedAppealSites = new ArrayList<QueuePair<Long, MapLocation>>();
    }

    public static boolean moveUnitInDirection(Unit unit, Direction candidateDirection)
    {
        int directionIndex = candidateDirection.ordinal();
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
    public static boolean tryToUnloadRobot(Unit factory)
    {
        Collections.shuffle(randomDirections);
        for (int i = 0; i < randomDirections.size(); i++)
        {
            Direction unloadDirection = randomDirections.get(i);
            if (unloadDirection == Direction.Center)
            {
                continue;
            }
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

    public static long getLandingLocationAppeal(int x, int y)
    {
        if (x < 0 || x >= awayMap.getWidth()) return WEIGHT_IMPASSABLE;

        if (y < 0 || y >= awayMap.getHeight()) return WEIGHT_IMPASSABLE;

        MapLocation tempLoc = new MapLocation(awayPlanet, x, y);

        // only called from Earth, so awayMap will be Mars
        if (awayMap.isPassableTerrainAt(tempLoc) == 0) return WEIGHT_IMPASSABLE;
        else return WEIGHT_NONE;
    }

    public static long getFactoryLocationAppeal(int x, int y)
    {
        if (x < 0 || x >= homeMap.getWidth()) return WEIGHT_IMPASSABLE;

        if (y < 0 || y >= homeMap.getHeight()) return WEIGHT_IMPASSABLE;

        MapLocation tempLoc = new MapLocation(Planet.Earth, x, y);

        // only called from Earth
        if (homeMap.isPassableTerrainAt(tempLoc) == 0) return WEIGHT_IMPASSABLE;
        else return WEIGHT_NONE;
    }

    public static void modifyAdjacentAppeal(MapLocation loc, long amount)
    {
        int temp_x = loc.getX();
        int temp_y = loc.getY();

        for (int x = -1; x <= 1; x++)
        {
            for (int y = -1; y <= 1; y++)
            {
                if(!(x == 0 && y == 0))
                {
                    MapLocation tempLoc = new MapLocation(homePlanet, temp_x + x, temp_y + y);
                    if (homeMap.onMap(tempLoc))
                    {
                        long tmp = potentialFactorySpots.get(temp_x + x).get(temp_y + y);
                        potentialFactorySpots.get(temp_x + x).set(temp_y + y, tmp + amount);
                    }
                }
            }
        }
    }

    public static void updateSurroundingAppeal(QueuePair<Long, MapLocation> destPair)
    {
        int temp_x = destPair.getSecond().getX();
        int temp_y = destPair.getSecond().getY();

        MapLocation tempLoc;
        for (int x = -1; x <= 1; x++)
        {
            for (int y = -1; y <= 1; y++)
            {
                if(!(x == 0 && y == 0))
                {
                    tempLoc = new MapLocation(Planet.Mars, temp_x + x, temp_y + y);
                    if (awayMap.onMap(tempLoc) && awayMap.isPassableTerrainAt(tempLoc) != 0)
                    {
                        updatedAppealSites.add(0, new QueuePair<>(destPair.getFirst() - WEIGHT_ROCKET, destPair.getSecond()));
                        // newer updates come earlier, can break once encountered.
                    }
                }
            }
        }
    }

    public static long maxWorkerLimitAtTurn(long currentRound)
    {
        if(mapSize <=500)
        {
            if(currentRound < 75)
            {
                if(earthInitialTotalKarbonite > 1000)
                {
                    return 20;
                }
                else if(earthInitialTotalKarbonite > 750)
                {
                    return 15;
                }
                else if(earthInitialTotalKarbonite < 100)
                {
                    return 5;
                }
            }
            else
            {
                return 10;
            }
        }
        else if(mapSize <=900)
        {
            if(currentRound < 85)
            {
                if(earthInitialTotalKarbonite > 1000)
                {
                    return 20;
                }
                else if(earthInitialTotalKarbonite > 750)
                {
                    return 15;
                }
                else if(earthInitialTotalKarbonite < 100)
                {
                    return 5;
                }
            }
            else
            {
                if(earthInitialTotalKarbonite < 500)
                {
                    return 12;
                }
                else if(earthInitialTotalKarbonite > 1000)
                {
                    return 20;
                }
                else
                {
                    return 15;
                }
            }
        }
        else
        {
            if(currentRound < 75)
            {
                if(earthInitialTotalKarbonite > 3000)
                {
                    return 30;
                }
                else if(earthInitialTotalKarbonite > 1000)
                {
                    return 20;
                }
                else
                {
                    return 10;
                }
            }
            else
            {
                if(earthInitialTotalKarbonite < 500)
                {
                    return 10;
                }
                else if(earthInitialTotalKarbonite > 1500)
                {
                    return 25;
                }
                else
                {
                    return 15;
                }
            }
        }
    return 10;
    }

    /*
    public static long maxFactoryLimitAtTurn(long currentRound)
    {
        if(mapSize <=500)
        {
            if(currentRound < 200)
            {
                return 3;
            }
            else if(currentRound < 600)
            {
                return 5;
            }
            else
            {
                return 3;
            }
        }
        else if(mapSize <=900)
        {
            if(currentRound < 200)
            {
                return 5;
            }
            else if(currentRound < 650)
            {
                return 10;
            }
            else
            {
                return 8;
            }
        }
        else
        {
            if(currentRound < 200)
            {
                return 10;
            }
            else if(currentRound < 650)
            {
                return 20;
            }
            else
            {
                return 10;
            }
        }
    }

    public static long maxRocketLimitAtTurn(long currentRound)
    {
        if(mapSize <=500)
        {
            if(currentRound < 200)
            {
                return 3;
            }
            else if(currentRound < 600)
            {
                return 6;
            }
            else
            {
                return 10;
            }
        }
        else if(mapSize <=900)
        {
            if(currentRound < 200)
            {
                if(earthInitialTotalKarbonite > 1000)
                {
                    return 5;
                }
                else
                {
                    return 3;
                }
            }
            else if(currentRound < 650)
            {
                return 10;
            }
            else
            {
                return 20;
            }
        }
        else
        {
            if(currentRound < 200)
            {
                if(earthInitialTotalKarbonite > 3000)
                {
                    return 6;
                }
                else if(earthInitialTotalKarbonite > 1000)
                {
                    return 4;
                }
                else
                {
                    return 3;
                }
            }
            else if(currentRound < 650)
            {
                return 15;
            }
            else
            {
                return 25;
            }
        }
    }
    */

    // This for Rangers only currently
    // Rational is purely on lost damage because of distance that needs to be travelled

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

    public static void main(String[] args)
    {
        initializeGlobals();

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
         potentialLandingSites = new PriorityQueue<>();
        if (gc.planet() == Planet.Earth)
        {
            for (int i = 0; i < awayMap.getWidth(); i++)
            {
                for (int j = 0; j < awayMap.getHeight(); j++)
                {
                    MapLocation tempLoc = new MapLocation(Planet.Mars, i, j);
                    if (awayMap.isPassableTerrainAt(tempLoc) != 0)
                    {
                        long appeal = WEIGHT_NONE;

                        // top row
                        appeal += getLandingLocationAppeal(i - 1,j + 1);
                        appeal += getLandingLocationAppeal(i,j + 1);
                        appeal += getLandingLocationAppeal(i + 1,j + 1);

                        // middle row
                        appeal += getLandingLocationAppeal(i - 1,j);
                        appeal += getLandingLocationAppeal(i + 1,j);

                        // bottom row
                        appeal += getLandingLocationAppeal(i - 1,j - 1);
                        appeal += getLandingLocationAppeal(i,j - 1);
                        appeal += getLandingLocationAppeal(i + 1,j - 1);

                        potentialLandingSites.add(new QueuePair<>(appeal, tempLoc));
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

            if(currentRound == 150)
            {
                rangerTalentVision = 30;
            }
            // Clear unit lists
            for (int i = 0; i < unitTypes.length; i++)
            {
                typeSortedUnitLists.get(unitTypes[i]).clear();
            }

            HashMap<Integer, QueuePair<Long, MapLocation>> visibleEnemyPriorities = new HashMap<Integer, QueuePair<Long, MapLocation>>();

            // Fetch current units and sort by type
            // Also includes code for targeting enemy units globally
            VecUnit units = gc.myUnits();
            for (int i = 0; i < units.size(); i++)
            {
                Unit unit = units.get(i);
                Location unitLocation = unit.location();
                if (!unitLocation.isInGarrison() && !unitLocation.isInSpace())
                {
                    VecUnit visibleEnemyUnits  = gc.senseNearbyUnitsByTeam(unit.location().mapLocation(), unit.visionRange(), theirTeam);
                    for(int j = 0; j < visibleEnemyUnits.size(); j++)
                    {
                        // Global micro-fighting priority decider
                        Unit visibleEnemyUnit = visibleEnemyUnits.get(j);
                        long enemyUnitRank = getEnemyUnitRank(visibleEnemyUnit);

                        visibleEnemyPriorities.put(visibleEnemyUnit.id(), new QueuePair<>(enemyUnitRank, visibleEnemyUnit.location().mapLocation()));
                    }
                }
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
                for (int ii = 0; ii < unitTypes.length; ii++)
                {
                    int i = (UnitType.Factory.ordinal() + ii) % unitTypes.length;
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
                                boolean workerMinedThisTurn = false;
                                boolean workerReplicatedThisTurn = false;
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
                                            break;
                                        }
                                    }
                                }

                                // Mine karbonite if adjacent to or standing on a mine
                                Collections.shuffle(randomDirections);
                                for (int j = 0; j < randomDirections.size(); j++)
                                {
                                    if (gc.canHarvest(unit.id(), randomDirections.get(j)))
                                    {
                                        gc.harvest(unit.id(), randomDirections.get(j));
                                        workerMinedThisTurn = true;
                                        break;
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
                                            // increase appeal
                                            modifyAdjacentAppeal(blueprintMapLocation, +1);
                                        }
                                        else
                                        {
                                            Unit unitAtLocation = gc.senseUnitAtLocation(blueprintMapLocation);
                                            if ((unitAtLocation.unitType() == UnitType.Factory ||
                                                    unitAtLocation.unitType() == UnitType.Rocket))
                                            {
                                                if (unitAtLocation.structureIsBuilt() == 1)
                                                {
                                                    obsoleteBlueprints.add(blueprint);
                                                }
                                            }
                                            else
                                            {
                                                // increase appeal
                                                modifyAdjacentAppeal(blueprintMapLocation, +1);
                                            }
                                        }
                                    }
                                }
                                for (Unit obsoleteBlueprint : obsoleteBlueprints)
                                {
                                    unfinishedBlueprints.remove(obsoleteBlueprint);
                                }
                                // Blueprint factories (change if condition)
                                if (unitsOfType[UnitType.Factory.ordinal()] < 8)
                                {
                                    MapLocation tempLoc = null;
                                    PriorityQueue<QueuePair<Long, MapLocation>> tempQueue = new PriorityQueue<>();
                                    for (int x = -1; x <= 1; x++)
                                    {
                                        for (int y = -1; y <= 1; y++)
                                        {
                                            if(!(x == 0 && y == 0))
                                            {
                                                tempLoc = new MapLocation(homePlanet, unitMapLocation.getX() + x, unitMapLocation.getY() + y);
                                                if (homeMap.onMap(tempLoc) && homeMap.isPassableTerrainAt(tempLoc) != 0)
                                                {
                                                    long newAppeal = potentialFactorySpots.get(tempLoc.getX()).get(tempLoc.getY());
                                                    newAppeal -= gc.karboniteAt(tempLoc); // reduce appeal by amount of karbonite at loc
                                                    tempQueue.add(new QueuePair<>(newAppeal, tempLoc));
                                                }
                                            }
                                        }
                                    }

                                    // tempQueue now contains appeal ordered neighbours
                                    Direction blueprintDirection = null;
                                    while (!tempQueue.isEmpty())
                                    {
                                        tempLoc = tempQueue.poll().getSecond();
                                        blueprintDirection = unitMapLocation.directionTo(tempLoc);
                                        if (gc.canBlueprint(unit.id(), UnitType.Factory, blueprintDirection))
                                        {
                                            gc.blueprint(unit.id(), UnitType.Factory, blueprintDirection);
                                            unitsOfType[UnitType.Factory.ordinal()]++;
                                            modifyAdjacentAppeal(tempLoc, -1);
                                            break;
                                        }
                                    }
                                }

                                // Blueprint rockets (change if condition)
                                Collections.shuffle(randomDirections);
                                if (unitsOfType[UnitType.Rocket.ordinal()] < 6)
                                {
                                    for (int j = 0; j < randomDirections.size(); j++)
                                    {
                                        Direction blueprintDirection = randomDirections.get(j);
                                        if (blueprintDirection == Direction.Center)
                                        {
                                            continue;
                                        }
                                        if (gc.canBlueprint(unit.id(), UnitType.Rocket, blueprintDirection))
                                        {
                                            gc.blueprint(unit.id(), UnitType.Rocket, blueprintDirection);
                                            unitsOfType[UnitType.Rocket.ordinal()]++;
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

                                // Replicate worker
                                if (unitsOfType[UnitType.Worker.ordinal()] < maxWorkerLimitAtTurn(currentRound))
                                {
                                    Collections.shuffle(randomDirections);
                                    for (int j = 0; j < randomDirections.size(); j++)
                                    {
                                        Direction replicateDirection = randomDirections.get(j);
                                        if (replicateDirection == Direction.Center)
                                        {
                                            continue;
                                        }
                                        if (gc.canReplicate(unit.id(), replicateDirection))
                                        {
                                            gc.replicate(unit.id(), replicateDirection);
                                            unitsOfType[UnitType.Worker.ordinal()]++;
                                            workerReplicatedThisTurn = true;
                                            break;
                                        }
                                    }
                                }

                                // Make space for other units
                                // Moved this to end to check results
                                // Seems better at the bottom
//                                if(!workerMinedThisTurn && !workerBuiltThisTurn)
//                                {
//                                    moveUnitAwayFromMultipleUnits(adjacentUnits, unit);
//                                }
                            }
                            if (unit.unitType() == UnitType.Rocket)
                            {
                                // If it's a new blueprint, add to the set
                                if (unit.structureIsBuilt() == 0 && !unfinishedBlueprints.contains(unit))
                                {
                                    unfinishedBlueprints.add(unit);
                                }
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
                                        QueuePair<Long, MapLocation> destPair = potentialLandingSites.poll();
                                        boolean isOutdated = true;
                                        while (isOutdated)
                                        {
                                            isOutdated = false;
                                            for (int j = 0; j < updatedAppealSites.size(); j++)
                                            {
                                                if (updatedAppealSites.get(j).getSecond().equals(destPair.getSecond())
                                                        && !(updatedAppealSites.get(j).getFirst().equals(destPair.getFirst())))
                                                {
                                                    isOutdated = true;
                                                    destPair = potentialLandingSites.poll();
                                                    break;
                                                }
                                            }
                                        }

                                        MapLocation dest = destPair.getSecond();
                                        // potentialLandingSites is supposed to have only those spots
                                        // that are passable, and not already used as a destination.
                                        // Hence, this check should always pass.
                                        if (gc.canLaunchRocket(unit.id(), dest))
                                        {
                                            gc.launchRocket(unit.id(), dest);
                                            updateSurroundingAppeal(destPair);
                                        }
                                    }
                                }
                            }
                            if (unit.unitType() == UnitType.Ranger)
                            {
                                if (!unit.location().isInGarrison())
                                {
                                    VecUnit nearbyEnemyUnits = gc.senseNearbyUnitsByTeam(unit.location().mapLocation(),
                                            70 + rangerTalentVision, theirTeam);

                                    if(nearbyEnemyUnits.size() != 0)
                                    {
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
                                    else
                                    {
                                        // QueuePair<Long, Unit> enemyData;
                                        Integer bestEnemyRank = 15;
                                        // To fix uninitialisation warning
                                        MapLocation targetEnemyMapLocation = unitMapLocation;
                                        for(HashMap.Entry<Integer, QueuePair<Long, MapLocation>> entry : visibleEnemyPriorities.entrySet())
                                        {
                                            Integer enemyUnitID = entry.getKey();
                                            Long primitiveRank = entry.getValue().getFirst();
                                            MapLocation enemyUnitMapLocation = entry.getValue().getSecond();
                                            primitiveRank *= (1 + (2 * unitMapLocation.distanceSquaredTo(enemyUnitMapLocation) -
                                                                    (70 + rangerTalentVision)/ (mapWidth + mapHeight)));
                                            if(primitiveRank < bestEnemyRank)
                                            {
                                                targetEnemyMapLocation = enemyUnitMapLocation;
                                            }
                                        }
                                        if(targetEnemyMapLocation == unitMapLocation ||
                                                !moveUnitInDirection(unit, unitMapLocation.directionTo(targetEnemyMapLocation)));
                                        {
                                            moveUnitInRandomDirection(unit);
                                        }
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
                                    moveUnitInRandomDirection(unit);
                                    VecUnit nearbyFriendlyUnits = gc.senseNearbyUnitsByTeam(unit.location().mapLocation(),
                                            50, ourTeam);
                                    boolean hasHealedThisTurn = false;
                                    boolean hasMovedThisTurn = false;

                                    if (unitFrozenByHeat(unit))
                                    {
                                        continue;
                                    }

                                    for (int j = 0; j < nearbyFriendlyUnits.size(); j++)
                                    {

                                        Unit nearbyFriendlyUnit = nearbyFriendlyUnits.get(j);
                                        {
                                            if (gc.canHeal(unit.id(), nearbyFriendlyUnit.id()) &&
                                                nearbyFriendlyUnit.health() < nearbyFriendlyUnit.maxHealth())
                                            {
                                                gc.heal(unit.id(), nearbyFriendlyUnit.id());
                                                hasHealedThisTurn = true;
                                            }
                                            if(nearbyFriendlyUnit.health() < nearbyFriendlyUnit.maxHealth())
                                            {
                                                if(moveUnitTowards(unit, nearbyFriendlyUnit.location()))
                                                {
                                                    hasMovedThisTurn = true;
                                                }
                                            }
                                        }
                                        if(hasMovedThisTurn && hasHealedThisTurn)
                                        {
                                            break;
                                        }
                                    }
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
                            if (unit.unitType() == UnitType.Factory)
                            {
                                // If it's a new blueprint, add to the set
                                if (unit.structureIsBuilt() == 0 && !unfinishedBlueprints.contains(unit))
                                {
                                    unfinishedBlueprints.add(unit);
                                }
                                tryToUnloadRobot(unit);
                                if (unit.isFactoryProducing() == 0)
                                {
                                    int workerCount = unitsOfType[UnitType.Worker.ordinal()]; // rarely produced
                                    int knightCount = unitsOfType[UnitType.Knight.ordinal()]; // not being produced
                                    int rangerCount = unitsOfType[UnitType.Ranger.ordinal()];
                                    int mageCount = unitsOfType[UnitType.Mage.ordinal()];
                                    int healerCount = unitsOfType[UnitType.Healer.ordinal()];

                                    // Think of better condition later; produce workers if existing ones are being massacred
                                    if (workerCount == 0)
                                    {
                                        if (gc.canProduceRobot(unit.id(), UnitType.Worker))
                                        {
                                            gc.produceRobot(unit.id(), UnitType.Worker);
                                            unitsOfType[UnitType.Worker.ordinal()]++;
                                        }
                                    }

                                    if (rangerCount >= 8 * (healerCount))
                                    {
                                        UnitType typeToBeProduced = UnitType.Healer;
                                        if (gc.canProduceRobot(unit.id(), typeToBeProduced))
                                        {
                                            gc.produceRobot(unit.id(), typeToBeProduced);
                                            unitsOfType[typeToBeProduced.ordinal()]++;
                                        }
                                    }
                                    else
                                    {
                                        if (gc.canProduceRobot(unit.id(), UnitType.Ranger))
                                        {
                                            gc.produceRobot(unit.id(), UnitType.Ranger);
                                            unitsOfType[UnitType.Ranger.ordinal()]++;
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
                                Collections.shuffle(randomDirections);
                                for (int j = 0; j < randomDirections.size(); j++)
                                {
                                    Direction unloadDirection = randomDirections.get(j);
                                    if (unloadDirection == Direction.Center)
                                    {
                                        continue;
                                    }
                                    if (gc.canUnload(unit.id(), unloadDirection))
                                    {
                                        gc.unload(unit.id(), unloadDirection);
                                        break;
                                    }
                                }
                            }
                            if (unitTypes[i] == UnitType.Worker)
                            {
                                boolean workerRepairedThisTurn = false;
                                boolean workerMinedThisTurn = false;
                                boolean workerReplicatedThisTurn = false;
                                // Build a structure if adjacent to one
                                for (int j = 0; j < adjacentUnits.size(); j++)
                                {
                                    Unit adjacentUnit = adjacentUnits.get(j);
                                    if (adjacentUnit.unitType() == UnitType.Factory || adjacentUnit.unitType() == UnitType.Rocket)
                                    {
                                        if (gc.canRepair(unit.id(), adjacentUnit.id()))
                                        {
                                            gc.repair(unit.id(), adjacentUnit.id());
                                            workerRepairedThisTurn = true;
                                            break;
                                        }
                                    }
                                }

                                // Mine karbonite if adjacent to or standing on a mine
                                Collections.shuffle(randomDirections);
                                for (int j = 0; j < randomDirections.size(); j++)
                                {
                                    if (gc.canHarvest(unit.id(), randomDirections.get(j)))
                                    {
                                        gc.harvest(unit.id(), randomDirections.get(j));
                                        workerMinedThisTurn = true;
                                        break;
                                    }
                                }

                                // Move Worker
                                if(!workerMinedThisTurn && !workerRepairedThisTurn)
                                {
                                    moveUnitAwayFromMultipleUnits(adjacentUnits, unit);
                                }

                                // Replicate worker if enough Karbonite or Earth flooded
                                if(currentRound > 749 || gc.karbonite() > 100)
                                {
                                    Collections.shuffle(randomDirections);
                                    for (int j = 0; j < randomDirections.size(); j++)
                                    {
                                        Direction replicateDirection = randomDirections.get(j);
                                        if (replicateDirection == Direction.Center)
                                        {
                                            continue;
                                        }
                                        if (gc.canReplicate(unit.id(), replicateDirection))
                                        {
                                            gc.replicate(unit.id(), replicateDirection);
                                            unitsOfType[UnitType.Worker.ordinal()]++;
                                            workerReplicatedThisTurn = true;
                                            break;
                                        }
                                    }
                                }
                            }
                            if (unit.unitType() == UnitType.Healer)
                            {
                                if (!unit.location().isInGarrison())
                                {
                                    moveUnitInRandomDirection(unit);
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
                                            if (gc.canHeal(unit.id(), nearbyFriendlyUnit.id()) &&
                                                    nearbyFriendlyUnit.health() < nearbyFriendlyUnit.maxHealth())
                                            {
                                                gc.heal(unit.id(), nearbyFriendlyUnit.id());
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                            if (unit.unitType() == UnitType.Ranger)
                            {
                                if (!unit.location().isInGarrison())
                                {
                                    VecUnit nearbyEnemyUnits = gc.senseNearbyUnitsByTeam(unit.location().mapLocation(),
                                            70 + rangerTalentVision, theirTeam);

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
                        }
                    }
                }
            }

            // Submit the actions we've done, and wait for our next turn.
            gc.nextTurn();
        }
    }
}
