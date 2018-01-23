package utility;

import java.util.*;

import bc.*;

import static utility.DecisionTree.*;
import static utility.Pathfinding.*;
import static utility.RocketBot.*;

public class Globals
{
    // General purpose variables
    public static Random random;
    public static GameController gc;
    public static Direction[] directions;
    public static UnitType[] unitTypes;
    public static Planet homePlanet;
    public static Planet awayPlanet;
    public static PlanetMap homeMap;
    public static PlanetMap awayMap;
    public static Team ourTeam;
    public static Team theirTeam;
    public static long homeMapWidth;
    public static long homeMapHeight;
    public static long homeMapSize;
    public static long awayMapWidth;
    public static long awayMapHeight;
    public static long awayMapSize;
    public static long currentRound;
    public static long currentKarbonite;
    public static MapLocation[][] mapLocationAt;
    public static VecUnit initialWorkers;
    public static long passableTerrain;
    public static long initialTotalKarbonite;
    public static Set<MapLocation> karboniteLocations;
    public static long initialKarboniteLocationSize;

    public static Set<Unit> unfinishedBlueprints;
    public static HashMap<UnitType, ArrayList<Unit>> typeSortedUnitLists;
    public static long totalCombatUnits;
    public static long totalUnits;
    public static ArrayList<Unit> unitList;
    public static HashSet<Integer> builderSet;

    // Enemy locations
    public static Stack<MapLocation> initialGuesses;
    public static Set<MapLocation> enemyFactories;
    public static HashMap<Integer, VecUnit> enemyVecUnits;
    public static ArrayList<QueuePair<Double, MapLocation>> enemyHotspots;

    public static short botIntelligenceLevel;
    public static double builderFraction;
    public static boolean prepareRocketArmada;
    public static int rocketProductionCooldown;

    // Rocket landing sites
    public static PriorityQueue<QueuePair<Long, MapLocation>> potentialLandingSites;
    public static long[][] marsMapAppeals;
    public static ArrayList<QueuePair<Long, MapLocation>> updatedAppealSites;

    // Terrain appeal constants
    public final static long WEIGHT_IMPASSABLE = -1;
    public final static long WEIGHT_ROCKET_ON_MARS = -3;
    public final static long WEIGHT_STRUCTURE = -1;
    public final static long WEIGHT_NONE = 0;

    // Pathfinding data structures
    public static HashMap<MapLocation, Boolean> visited;
    public static HashMap<MapLocation, LinkedList<GraphPair<MapLocation, Long>>> waypointAdjacencyList;
    public static HashMap<MapLocation, HashMap<MapLocation, Boolean>> isReachable;
    public static HashMap<MapLocation, HashMap<MapLocation, MapLocation>> shortestPathTrees;
    public static HashMap<MapLocation, MapLocation> nearestUnobstructedWaypoints;
    public static HashMap<Pair<MapLocation, MapLocation>, MapLocation> nextBestWaypoint;
    public static HashMap<Integer, MapLocation> lastVisited;

    // Combat variables
    public static HashMap<UnitType, Long> attackRange;
    public static HashMap<UnitType, Long> abilityRange;
    public static HashMap<UnitType, Long> attackCooldown;
    public static HashMap<UnitType, Long> movementCooldown;
    public static HashMap<UnitType, Long> abilityCooldown;
    public static HashMap<UnitType, Long> visionRange;

    // Build requirement variables
    public static Deque<UnitType> buildQueue;
    public static int[] unitsInBuildQueue;
    public static Deque<UnitType> trainQueue;
    public static int[] unitsInTrainQueue;
    public static int workersRequired;
    public static int knightsRequired;
    public static int rangersRequired;
    public static int magesRequired;
    public static int healersRequired;
    public static int factoriesRequired;
    public static int rocketsRequired;

    // Research queue
    // 25+25+100+100+100+25+75+100+25+75+100+25+75+100+25+75
    public final static UnitType[] RESEARCH_QUEUE_HARD = {UnitType.Worker, UnitType.Ranger, UnitType.Ranger, UnitType.Rocket,
            UnitType.Rocket, UnitType.Healer, UnitType.Healer, UnitType.Rocket, UnitType.Worker,
            UnitType.Worker, UnitType.Worker, UnitType.Mage, UnitType.Mage, UnitType.Mage,
            UnitType.Knight, UnitType.Knight};


    // Initializer method
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
        homeMapWidth = homeMap.getWidth();
        homeMapHeight = homeMap.getHeight();
        homeMapSize = homeMapHeight * homeMapHeight;
        awayMapWidth = awayMap.getWidth();
        awayMapHeight = awayMap.getHeight();
        awayMapSize = awayMapHeight * awayMapHeight;

        botIntelligenceLevel = 1;

        // Map MapLocations to x and y for constant object references
        mapLocationAt = new MapLocation[(int) homeMapWidth][(int) homeMapHeight];
        for (int x = 0; x < homeMapWidth; x++)
        {
            for (int y = 0; y < homeMapHeight; y++)
            {
                mapLocationAt[x][y] = new MapLocation(homePlanet, x, y);
            }
        }

        karboniteLocations = new HashSet<MapLocation>();
        getInitialKarboniteLocations();

        initialWorkers = homeMap.getInitial_units();

        builderSet = new HashSet<Integer>();
        // All initial workers are builders
        for (int i = 0; i < initialWorkers.size(); i++)
        {
            Unit worker = initialWorkers.get(i);
            builderSet.add(worker.id());
        }
        setBuilderFraction();

        prepareRocketArmada = false;
        rocketProductionCooldown = 0;

        if (homePlanet == Planet.Earth)
        {
            unfinishedBlueprints = new HashSet<Unit>();
            potentialLandingSites = new PriorityQueue<QueuePair<Long, MapLocation>>();
            marsMapAppeals = new long[(int) awayMapWidth][(int) awayMapHeight];
            findMarsLocationAppeals();
            updatedAppealSites = new ArrayList<QueuePair<Long, MapLocation>>();
            findPotentialLandingSites();
        }
        else
        {
            unfinishedBlueprints = null;
            potentialLandingSites = null;
            marsMapAppeals = null;
            updatedAppealSites = null;
        }

        typeSortedUnitLists = new HashMap<UnitType, ArrayList<Unit>>();
        for (int i = 0; i < unitTypes.length; i++)
        {
            typeSortedUnitLists.put(unitTypes[i], new ArrayList<Unit>());
        }

        enemyVecUnits = new HashMap<Integer, VecUnit>();
        initialGuesses = new Stack<MapLocation>();
        enemyFactories = new HashSet<MapLocation>();
        enemyHotspots = new ArrayList<QueuePair<Double, MapLocation>>();
        for (int i = 0; i < initialWorkers.size(); i++)
        {
            Unit worker = initialWorkers.get(i);
            MapLocation workerMapLocation = worker.location().mapLocation();
            int x = (int) homeMapWidth - workerMapLocation.getX();
            int y = (int) homeMapHeight - workerMapLocation.getY();
            initialGuesses.push(new MapLocation(homePlanet, x, y));
        }

        visited = new HashMap<MapLocation, Boolean>();
        waypointAdjacencyList = new HashMap<MapLocation, LinkedList<GraphPair<MapLocation, Long>>>();
        isReachable = new HashMap<MapLocation, HashMap<MapLocation, Boolean>>();
        shortestPathTrees = new HashMap<MapLocation, HashMap<MapLocation, MapLocation>>();
        nearestUnobstructedWaypoints = new HashMap<MapLocation, MapLocation>();
        nextBestWaypoint = new HashMap<Pair<MapLocation, MapLocation>, MapLocation>();
        lastVisited = new HashMap<Integer, MapLocation>();
        computeShortestPathTrees();

        attackRange = new HashMap<UnitType, Long>();
        abilityRange = new HashMap<UnitType, Long>();
        attackCooldown = new HashMap<UnitType, Long>();
        movementCooldown = new HashMap<UnitType, Long>();
        abilityCooldown = new HashMap<UnitType, Long>();
        visionRange = new HashMap<UnitType, Long>();
        attackRange.put(UnitType.Knight, 2L);
        attackRange.put(UnitType.Ranger, 50L);
        attackRange.put(UnitType.Mage, 30L);
        attackRange.put(UnitType.Healer, 30L);
        abilityRange.put(UnitType.Knight, 10L);
        abilityRange.put(UnitType.Ranger, 5000L);
        abilityRange.put(UnitType.Mage, 8L);
        abilityRange.put(UnitType.Healer, 30L);
        attackCooldown.put(UnitType.Knight, 20L);
        attackCooldown.put(UnitType.Ranger, 20L);
        attackCooldown.put(UnitType.Mage, 20L);
        attackCooldown.put(UnitType.Healer, 10L);
        movementCooldown.put(UnitType.Knight, 15L);
        movementCooldown.put(UnitType.Ranger, 30L);
        movementCooldown.put(UnitType.Mage, 20L);
        movementCooldown.put(UnitType.Healer, 25L);
        visionRange.put(UnitType.Knight, 50L);
        visionRange.put(UnitType.Ranger, 70L);
        visionRange.put(UnitType.Mage, 30L);
        visionRange.put(UnitType.Healer, 50L);

        buildQueue = new ArrayDeque<UnitType>();
        unitsInBuildQueue = new int[unitTypes.length];
        trainQueue = new ArrayDeque<UnitType>();
        unitsInTrainQueue = new int[unitTypes.length];
        setWorkersRequired();
        setFactoriesRequired();
        setKnightsRequired();
        setRangersRequired();
        setMagesRequired();
        setHealersRequired();
        setRocketsRequired();
    }

    public static long diagonalDistanceBetween(MapLocation first, MapLocation second)
    {
        return Math.max(Math.abs(first.getX() - second.getX()), Math.abs(first.getY() - second.getY()));
    }

    public static MapLocation getConstantMapLocationRepresentation(MapLocation newRepresentation)
    {
        return mapLocationAt[newRepresentation.getX()][newRepresentation.getY()];
    }

    private static void getInitialKarboniteLocations()
    {
        for (int x = 0; x < homeMapWidth; x++)
        {
            for (int y = 0; y < homeMapHeight; y++)
            {
                MapLocation tempMapLocation = mapLocationAt[x][y];
                long karboniteAtTempMapLocation = homeMap.initialKarboniteAt(tempMapLocation);
                // karbonite amount check
                if (karboniteAtTempMapLocation > 0)
                {
                    karboniteLocations.add(tempMapLocation);
                    initialTotalKarbonite += karboniteAtTempMapLocation;
                }

                if (homeMap.isPassableTerrainAt(tempMapLocation) == 1)
                {
                    passableTerrain++;
                }
            }
        }
        initialKarboniteLocationSize = karboniteLocations.size();
    }

    // Find the appeals of all map locations on Mars
    // Will only be called from Earth
    private static void findMarsLocationAppeals()
    {
        for (int i = 0; i < awayMapWidth; i++)
        {
            for (int j = 0; j < awayMapHeight; j++)
            {
                MapLocation mapLocation = new MapLocation(awayPlanet, i, j);
                if (awayMap.isPassableTerrainAt(mapLocation) == 0)
                {
                    marsMapAppeals[i][j] = WEIGHT_IMPASSABLE;
                }
                else
                {
                    marsMapAppeals[i][j] = WEIGHT_NONE;
                }
            }
        }
    }

    // Find potential landing spots and store in a priority queue
    // Will only be called from Earth
    private static void findPotentialLandingSites()
    {
        for (int x = 0; x < awayMapWidth; x++)
        {
            for (int y = 0; y < awayMapHeight; y++)
            {
                MapLocation mapLocation = new MapLocation(awayPlanet, x, y);
                long appeal = WEIGHT_NONE;
                for (int i = 0; i < directions.length - 1; i++)
                {
                    MapLocation adjacentMapLocation = mapLocation.add(directions[i]);
                    if (awayMap.onMap(adjacentMapLocation) && awayMap.isPassableTerrainAt(adjacentMapLocation) == 1)
                    {
                        appeal += marsMapAppeals[adjacentMapLocation.getX()][adjacentMapLocation.getY()];
                    }
                }
                potentialLandingSites.add(new QueuePair<>(appeal, mapLocation));
            }
        }
    }

    public static void addUnitToBuildQueue(UnitType type)
    {
        buildQueue.addLast(type);
        unitsInBuildQueue[type.ordinal()]++;
    }

    public static void addUnitToBuildQueueUrgently(UnitType type)
    {
        buildQueue.addFirst(type);
        unitsInBuildQueue[type.ordinal()]++;
    }

    public static void removeUnitFromBuildQueue()
    {
        UnitType type = buildQueue.removeFirst();
        unitsInBuildQueue[type.ordinal()]--;
    }

    public static void addUnitToTrainQueue(UnitType type)
    {
        trainQueue.addLast(type);
        unitsInTrainQueue[type.ordinal()]++;
    }

    public static void addUnitToTrainQueueUrgently(UnitType type)
    {
        trainQueue.addFirst(type);
        unitsInTrainQueue[type.ordinal()]++;
    }

    public static void removeUnitFromTrainQueue()
    {
        UnitType type = trainQueue.removeFirst();
        unitsInTrainQueue[type.ordinal()]--;
    }
}
