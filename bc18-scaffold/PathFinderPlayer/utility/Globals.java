package utility;

import java.util.*;

import bc.*;

public class Globals
{
    // Variables
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
    public static VecUnit initialWorkers;
    public static long earthInititalTotalKarbonite = 0;
    public static Set<MapLocation> earthKarboniteLocations;
    public static PriorityQueue<QueuePair<Long, MapLocation>> potentialLandingSites;
    public static ArrayList<QueuePair<Long, MapLocation>> updatedAppealSites;
    public static HashMap<MapLocation, Boolean> visited;
    public static HashMap<MapLocation, LinkedList<GraphPair<MapLocation, Long>>> waypointAdjacencyList;
    public static HashMap<MapLocation, HashMap<MapLocation, Boolean>> isReachable;
    public static HashMap<MapLocation, HashMap<MapLocation, MapLocation>> shortestPathTrees;

    // Combat constants
    public final static int INITIAL_RANGER_ATTACK_DISTANCE = 7;  // Rounding For Now
    public final static int INITIAL_RANGER_MOVEMENT_COOLDOWN = 20;
    public final static int INITIAL_RANGER_ATTACK_COOLDOWN = 20;
    public final static int INITIAL_MAGE_ATTACK_DISTANCE = 5; // Rounding For Now
    public final static int INITIAL_MAGE_MOVEMENT_COOLDOWN = 20;
    public final static int INITIAL_MAGE_ATTACK_COOLDOWN = 20;
    public final static int INITIAL_KNIGHT_ATTACK_DISTANCE = 1;
    public final static int INITIAL_KNIGHT_MOVEMENT_COOLDOWN = 15;
    public final static int INITIAL_KNIGHT_ATTACK_COOLDOWN = 20;

    // Unit deltas (more to be added)
    public static int rangerTalentVision = 0;

    // Terrain appeal constants
    public final static long WEIGHT_IMPASSABLE = -2;
    //    public final static long WEIGHT_KARBONITE_CENTER = -1; // central tile is Karb; undesirable
    public final static long WEIGHT_ROCKET = -1;
    //    public final static long WEIGHT_KARBONITE_SIDE = +1; // Karb to the side; desirable
    public final static long WEIGHT_NONE = 0;

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

        // Get initial worker units
        initialWorkers = homeMap.getInitial_units();

        if (homePlanet == Planet.Earth)
        {
            // Get initial karbonite locations
            earthKarboniteLocations = new HashSet<MapLocation>();
            for (int x = 0; x < homeMapWidth; x++)
            {
                for (int y = 0; y < homeMapHeight; y++)
                {
                    MapLocation tempMapLocation = new MapLocation(homePlanet, x, y);
                    long karboniteAtTempMapLocation = homeMap.initialKarboniteAt(tempMapLocation);
                    if (karboniteAtTempMapLocation > 0)
                    {
                        earthKarboniteLocations.add(tempMapLocation);
                        earthInititalTotalKarbonite += karboniteAtTempMapLocation;
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

        visited = new HashMap<MapLocation, Boolean>();
        waypointAdjacencyList = new HashMap<MapLocation, LinkedList<GraphPair<MapLocation, Long>>>();
        isReachable = new HashMap<MapLocation, HashMap<MapLocation, Boolean>>();
        shortestPathTrees = new HashMap<MapLocation, HashMap<MapLocation, MapLocation>>();
    }
}
