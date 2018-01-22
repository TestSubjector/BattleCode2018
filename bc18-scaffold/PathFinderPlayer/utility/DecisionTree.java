package utility;

import java.util.*;

import bc.*;

import static utility.Globals.*;
import static utility.Combat.*;

public class DecisionTree
{
    public static void setBuilderFraction()
    {
        if (homePlanet == Planet.Mars)
        {
            builderFraction = 0;
        }
        if (initialKarboniteLocationSize == 0)
        {
            builderFraction = 1;
        }
        else
        {
            builderFraction = Math.min(1, (((double) currentRound + 200) / 400) * (1 - 0.8 * ((double) karboniteLocations.size() / initialKarboniteLocationSize)));
        }
    }

    public static boolean shouldQueueWorker()
    {
        return typeSortedUnitLists.get(UnitType.Worker).size() + unitsInQueue[UnitType.Worker.ordinal()] < workersRequired;
    }

    public static boolean shouldQueueFactory()
    {
        return typeSortedUnitLists.get(UnitType.Factory).size() + unitsInQueue[UnitType.Factory.ordinal()] < factoriesRequired;
    }

    public static boolean shouldQueueRocket()
    {
        return gc.researchInfo().getLevel(UnitType.Rocket) != 0 && typeSortedUnitLists.get(UnitType.Rocket).size() + unitsInQueue[UnitType.Rocket.ordinal()] < rocketsRequired;
    }
    // Implement the following and the setXRequired() functions
    //
    //

    //
    //    public static boolean shouldQueueRanger()
    //    {
    //        return typeSortedUnitLists.get(UnitType.Worker).size() + unitsInQueue[UnitType.Worker.ordinal()] < workersRequired;
    //    }
    //
    //    public static boolean shouldQueueMage()
    //    {
    //        return typeSortedUnitLists.get(UnitType.Worker).size() + unitsInQueue[UnitType.Worker.ordinal()] < workersRequired;
    //    }
    //
    //    public static boolean shouldQueueKnight()
    //    {
    //        return typeSortedUnitLists.get(UnitType.Worker).size() + unitsInQueue[UnitType.Worker.ordinal()] < workersRequired;
    //    }
    //
    //    public static boolean shouldQueueHealer()
    //    {
    //        return typeSortedUnitLists.get(UnitType.Worker).size() + unitsInQueue[UnitType.Worker.ordinal()] < workersRequired;
    //    }

    // This is not very good, needs an overhaul
    public static void setWorkersRequired()
    {
        if (homeMapSize <= 500)
        {
            if (currentRound < 75)
            {
                if (initialTotalKarbonite > 1000)
                {
                    workersRequired = 20;
                }
                else if (initialTotalKarbonite > 750)
                {
                    workersRequired = 15;
                }
                else if (initialTotalKarbonite < 100)
                {
                    workersRequired = 5;
                }
            }
            else
            {
                workersRequired = 10;
            }
        }
        else if (homeMapSize <= 900)
        {
            if (currentRound < 85)
            {
                if (initialTotalKarbonite > 1000)
                {
                    workersRequired = 20;
                }
                else if (initialTotalKarbonite > 750)
                {
                    workersRequired = 15;
                }
                else if (initialTotalKarbonite < 100)
                {
                    workersRequired = 5;
                }
            }
            else
            {
                if (initialTotalKarbonite < 500)
                {
                    workersRequired = 12;
                }
                else if (initialTotalKarbonite > 1000)
                {
                    workersRequired = 20;
                }
                else
                {
                    workersRequired = 15;
                }
            }
        }
        else
        {
            if (currentRound < 75)
            {
                if (initialTotalKarbonite > 3000)
                {
                    workersRequired = 30;
                }
                else if (initialTotalKarbonite > 1000)
                {
                    workersRequired = 20;
                }
                else
                {
                    workersRequired = 10;
                }
            }
            else
            {
                if (initialTotalKarbonite < 500)
                {
                    workersRequired = 10;
                }
                else if (initialTotalKarbonite > 1500)
                {
                    workersRequired = 25;
                }
                else
                {
                    workersRequired = 15;
                }
            }
        }
        workersRequired = 10;
    }

    public static void setFactoriesRequired()
    {
        factoriesRequired = (int) ((Math.min(1, (double) currentRound / 400)) *
                Math.round(4 + ((double) homeMapHeight + homeMapWidth) / 10) +
                (double) initialTotalKarbonite / 1000);
    }

    // These metrics are horrid as of now
    public static void setRocketsRequired()
    {
        if (enemyVecUnits.size() == 0 && currentRound > 500)
        {
            rocketsRequired = 10;
        }
        else
        {
            rocketsRequired = (int) ((Math.min(1, (double) currentRound - 120 / 400)) * Math.round((double) totalUnits / 30));
        }
    }

    // Replace with something else or include in the above
    public static boolean makeRocketArmada(long totalUnits)
    {
        if (rocketProductionCooldown > 0)
        {
            return false;
        }
        else if (enemyVecUnits.size() == 0 && currentRound > 500)
        {
            return true;
        }
        else
        {
            return totalUnits > passableTerrain * ((double) homeMapHeight + homeMapWidth) / (2 * (homeMapSize));
        }
    }

    // Replace with something else or include in the above
    // Cooldown till you can again make a Rocket
    public static void findRocketProductionCooldown()
    {
        if (currentRound > 700)
        {
            rocketProductionCooldown = 0;
        }
        else
        {
            rocketProductionCooldown--;
        }
    }

    // Witch of Agnesi computation breaker
    public static boolean switchToPrimitiveMind(long currentRound, int timeLeft)
    {
        // Give 5 secs to pathfinding
        // Constant value by integrating (8*57^3)/(x^2 + 57^2) dx from x = -infinity to -375
        return timeLeft < 18000 * (Math.tanh((currentRound - 375) / 57)) + 26500;
    }
}
