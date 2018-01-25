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
    public static MapLocation[][] mapLocationAt;
    public static ArrayList<Unit> initialWorkers;
    public static long passableTerrain;
    public static long initialTotalKarbonite;
    public static Set<MapLocation> karboniteLocations;
    public static long initialKarboniteLocationSize;
    public static AsteroidPattern asteroidPattern;

    public static short botIntelligenceLevel;

    public static HashMap<UnitType, ArrayList<Unit>> typeSortedUnitLists;
    public static ArrayList<Unit> unitList;
    public static long totalCombatUnits;
    public static long totalUnits;
    public static double builderFraction;
    public static HashSet<Integer> builderSet;
    public static Set<Unit> unfinishedBlueprints;

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
    public static HashMap<MapLocation, HashMap<MapLocation, MapLocation>> shortestPathTrees;
    public static HashMap<MapLocation, MapLocation> nearestUnobstructedWaypoints;
    public static HashMap<Pair<MapLocation, MapLocation>, MapLocation> nextBestWaypoint;
    public static HashMap<Integer, MapLocation> lastVisited;
    public static int numberOfConnectedComponents;

    // Enemy locations
    public static ArrayList<MapLocation> initialEnemyWorkers;
    public static Set<MapLocation> enemyFactories;
    public static Set<MapLocation> rocketPositions;
    public static HashMap<Integer, VecUnit> enemyVecUnits;
    public static ArrayList<QueuePair<Double, MapLocation>> enemyHotspots;

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
    public final static UnitType[] RESEARCH_QUEUE_HARD_LARGE_MAPS = {UnitType.Knight, UnitType.Knight, UnitType.Rocket,
            UnitType.Healer, UnitType.Healer, UnitType.Knight, UnitType.Healer, UnitType.Rocket, UnitType.Rocket,
            UnitType.Worker, UnitType.Worker, UnitType.Worker,  UnitType.Mage, UnitType.Mage, UnitType.Mage,
            UnitType.Knight, UnitType.Ranger};

    public final static UnitType[] RESEARCH_QUEUE_HARD_SMALL_MAPS = {UnitType.Ranger, UnitType.Healer, UnitType.Rocket,
            UnitType.Ranger, UnitType.Healer, UnitType.Healer, UnitType.Ranger, UnitType.Rocket, UnitType.Rocket,
            UnitType.Worker, UnitType.Worker, UnitType.Worker,  UnitType.Mage, UnitType.Mage, UnitType.Mage,
            UnitType.Knight};

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
        asteroidPattern = gc.asteroidPattern();

        VecUnit allInitialWorkers = homeMap.getInitial_units();
        initialWorkers = new ArrayList<Unit>();

        enemyVecUnits = new HashMap<Integer, VecUnit>();
        initialEnemyWorkers = new ArrayList<MapLocation>();
        enemyFactories = new HashSet<MapLocation>();
        rocketPositions = new HashSet<MapLocation>();
        enemyHotspots = new ArrayList<QueuePair<Double, MapLocation>>();

        builderSet = new HashSet<Integer>();
        // All initial workers are builders
        for (int i = 0; i < allInitialWorkers.size(); i++)
        {
            Unit worker = allInitialWorkers.get(i);
            if (worker.team() == ourTeam)
            {
                initialWorkers.add(worker);
                builderSet.add(worker.id());
            }
            else
            {
                initialEnemyWorkers.add(worker.location().mapLocation());
                // System.out.println("Enemy Worker Here" + worker.location().mapLocation());
            }
        }
        setBuilderFraction();

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

        visited = new HashMap<MapLocation, Boolean>();
        waypointAdjacencyList = new HashMap<MapLocation, LinkedList<GraphPair<MapLocation, Long>>>();
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
                // add only passable squares to the queue
                if (marsMapAppeals[x][y] != WEIGHT_IMPASSABLE) {
                    long appeal = WEIGHT_NONE;
                    // directions will not use CENTER because < length-1 and center is the last one
                    for (int i = 0; i < directions.length - 1; i++)
                    {
                        MapLocation adjacentMapLocation = mapLocation.add(directions[i]);

                        if (awayMap.onMap(adjacentMapLocation))
                        {
                            appeal += marsMapAppeals[adjacentMapLocation.getX()][adjacentMapLocation.getY()];
                        }
                        else
                        {
                            // map edges
                            appeal += WEIGHT_IMPASSABLE;
                        }

                    }
                    potentialLandingSites.add(new QueuePair<>(appeal, mapLocation));
                }
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

    public static void removeObsoleteMines()
    {
        // Remove obsolete mine locations
        LinkedList<MapLocation> obsoleteMines = new LinkedList<MapLocation>();
        for (MapLocation karboniteMapLocation : karboniteLocations)
        {
            if (gc.canSenseLocation(karboniteMapLocation) &&
                    gc.karboniteAt(karboniteMapLocation) == 0)
            {
                obsoleteMines.add(karboniteMapLocation);
            }
        }
        for (MapLocation obsoleteMine : obsoleteMines)
        {
            karboniteLocations.remove(obsoleteMine);
        }
    }

    public static void removeObsoleteBlueprints()
    {
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
                    if ((unitAtLocation.unitType() == UnitType.Factory || unitAtLocation.unitType() == UnitType.Rocket) &&
                            (unitAtLocation.structureIsBuilt() == 1))
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
    }

    public static void removeObsoleteBuilders()
    {
        LinkedList<Integer> obsoleteBuilders = new LinkedList<Integer>();
        for (int builderID : builderSet)
        {
            if (!gc.canSenseUnit(builderID))
            {
                obsoleteBuilders.add(builderID);
            }
        }
        for (int obsoleteBuilder : obsoleteBuilders)
        {
            builderSet.remove(obsoleteBuilder);
        }
    }

    public static void removeObsoleteEnemyFactories()
    {
        // Remove obsolete enemy factory locations
        LinkedList<MapLocation> obsoleteEnemyFactories = new LinkedList<MapLocation>();
        for (MapLocation enemyFactory : enemyFactories)
        {
            if (gc.canSenseLocation(enemyFactory))
            {
                if (gc.hasUnitAtLocation(enemyFactory))
                {
                    Unit unit = gc.senseUnitAtLocation(enemyFactory);
                    if (unit.unitType() != UnitType.Factory || unit.team() != theirTeam)
                    {
                        obsoleteEnemyFactories.add(enemyFactory);
                    }
                }
                else
                {
                    obsoleteEnemyFactories.add(enemyFactory);
                }
            }
        }
        for (MapLocation obsoleteEnemyFactory : obsoleteEnemyFactories)
        {
            enemyFactories.remove(obsoleteEnemyFactory);
        }
    }

    public static void removeObsoleteEnemyHotspots()
    {
        // Remove obsolete enemy hotspots
        LinkedList<QueuePair<Double, MapLocation>> obsoleteEnemyHotspots = new LinkedList<QueuePair<Double, MapLocation>>();
        for (QueuePair<Double, MapLocation> enemyHotspot : enemyHotspots)
        {
            MapLocation enemyHotspotMapLocation = enemyHotspot.getSecond();
            boolean noEnemiesInSight = true;
            if (gc.canSenseLocation(enemyHotspotMapLocation))
            {
                for (int i = 0; i < directions.length - 1; i++)
                {
                    MapLocation oneBlockAway = enemyHotspotMapLocation.add(directions[i]);
                    MapLocation twoBlocksAway = enemyHotspotMapLocation.addMultiple(directions[i], 2);
                    if (gc.canSenseLocation(oneBlockAway) && gc.hasUnitAtLocation(oneBlockAway))
                    {
                        Unit unit = gc.senseUnitAtLocation(oneBlockAway);
                        if (unit.team() == theirTeam)
                        {
                            noEnemiesInSight = false;
                            break;
                        }
                    }
                    if (gc.canSenseLocation(twoBlocksAway) && gc.hasUnitAtLocation(twoBlocksAway))
                    {
                        Unit unit = gc.senseUnitAtLocation(twoBlocksAway);
                        if (unit.team() == theirTeam)
                        {
                            noEnemiesInSight = false;
                            break;
                        }
                    }
                }
                if (gc.canSenseLocation(enemyHotspotMapLocation) && gc.hasUnitAtLocation(enemyHotspotMapLocation))
                {
                    Unit unit = gc.senseUnitAtLocation(enemyHotspotMapLocation);
                    if (unit.team() == theirTeam)
                    {
                        noEnemiesInSight = false;
                        break;
                    }
                }
            }
            if (noEnemiesInSight)
            {
                obsoleteEnemyHotspots.add(enemyHotspot);
            }
        }
        for (QueuePair<Double, MapLocation> obsoleteEnemyHotspot : obsoleteEnemyHotspots)
        {
            enemyHotspots.remove(obsoleteEnemyHotspot);
        }
    }
}
