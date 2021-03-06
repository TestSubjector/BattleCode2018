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
        if (karboniteLocations.size() == 0)
        {
            builderFraction = 1;
        }
        else if(currentRound > 650)
        {
            builderFraction = 1;
        }
        else
        {
            builderFraction = Math.min(1, (((double) currentRound + 200) / 400) * (1 - 0.5 * ((double) karboniteLocations.size() / initialKarboniteLocationSize)));
        }
    }

    public static boolean shouldQueueWorker()
    {
        return typeSortedUnitLists.get(UnitType.Worker).size() + unitsInTrainQueue[UnitType.Worker.ordinal()] < workersRequired;
    }

    public static boolean shouldQueueRanger()
    {
        return typeSortedUnitLists.get(UnitType.Ranger).size() + unitsInTrainQueue[UnitType.Ranger.ordinal()] < rangersRequired;
    }

    public static boolean shouldQueueMage()
    {
        return typeSortedUnitLists.get(UnitType.Mage).size() + unitsInTrainQueue[UnitType.Mage.ordinal()] < magesRequired;
    }

    public static boolean shouldQueueKnight()
    {
        return typeSortedUnitLists.get(UnitType.Knight).size() + unitsInTrainQueue[UnitType.Knight.ordinal()] < knightsRequired;
    }

    public static boolean shouldQueueHealer()
    {
        return typeSortedUnitLists.get(UnitType.Healer).size() + unitsInTrainQueue[UnitType.Healer.ordinal()] < healersRequired;
    }

    public static boolean shouldQueueFactory()
    {
        return typeSortedUnitLists.get(UnitType.Factory).size() + unitsInBuildQueue[UnitType.Factory.ordinal()] < factoriesRequired;
    }

    public static boolean shouldQueueRocket()
    {
        return currentRound > 100 && typeSortedUnitLists.get(UnitType.Rocket).size() + unitsInBuildQueue[UnitType.Rocket.ordinal()] < rocketsRequired;
    }

    // This is not very good, needs an overhaul
    public static void setWorkersRequired()
    {
        if (homeMapSize <= 500)
        {
            if (currentRound < 75)
            {
                if (initialTotalKarbonite > 1000)
                {
                    workersRequired = 14;
                }
                else if (initialTotalKarbonite > 750)
                {
                    workersRequired = 11;
                }
                else if (initialTotalKarbonite < 100)
                {
                    workersRequired = 7;
                }
            }
            else
            {
                workersRequired = 7;
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
                    workersRequired = 7;
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
        workersRequired = (int) (Math.min(1, (double) (currentRound + 200) / 400) * workersRequired);
    }

    public static void setFactoriesRequired()
    {
        if (currentRound < (homeMapSize / 400))
        {
            factoriesRequired = 1;
        }
        else
        {
            factoriesRequired = (int) ((Math.min(1, Math.sqrt((double) currentRound / 400))) *
                    (Math.round(3 + ((double) homeMapHeight + homeMapWidth) / 10) +
                            (double) initialTotalKarbonite / 1500));
        }
    }

    public static void setRocketsRequired()
    {
        if (enemyVecUnits.size() == 0 && currentRound > 500)
        {
            rocketsRequired = 25;
        }
        else
        {
            rocketsRequired = (int) ((Math.min(2, (double) (currentRound * currentRound) / (300 * 300))) * Math.round((double) totalUnits / 5));
        }
    }

    public static void setKnightsRequired()
    {
        if (typeSortedUnitLists.get(UnitType.Factory).size() < 1 || shouldQueueWorker() || shouldQueueFactory())
        {
            return;
        }
        if(homeMapSize <= 600)
        {
            knightsRequired = (int) (1 + ((double) currentRound / 75) * (double) homeMapSize / 900);
        }
        else if (currentRound <= 75)
        {
            knightsRequired = (int) (4 + ((double) currentRound / 75) * (double) homeMapSize / 300);
        }
        else
        {
            knightsRequired = 2 * (int) (((double) currentRound * (600 + homeMapSize) * passableTerrain) / (12000 * homeMapSize));
        }
    }

    public static void setRangersRequired()
    {
        if (typeSortedUnitLists.get(UnitType.Factory).size() < 1 || shouldQueueWorker() || shouldQueueFactory())
        {
            return;
        }
        if(homeMapSize <= 600)
        {
            rangersRequired = (int) (6 + ((double) currentRound / 75) * (double) homeMapSize / 100);
        }
        else if (currentRound <= 75)
        {
            rangersRequired = (int) (2 + ((double) currentRound / 75) * (double) homeMapSize / 300);
        }
        else
        {
            rangersRequired = 2 * (int) Math.round(Math.min(1, Math.sqrt((double) currentRound / 450)) *
                    ((homeMapSize - passableTerrain) / homeMapSize) *
                    (23 + (homeMapSize - 400) * (23 / 2100)));
        }
    }

    public static void setMagesRequired()
    {
        magesRequired = 0;
//        if (typeSortedUnitLists.get(UnitType.Factory).size() < 1 || shouldQueueWorker() || shouldQueueFactory())
//        {
//            return;
//        }
//        if (currentRound <= 75)
//        {
//            magesRequired = (int) (2 + ((double) currentRound / 75) * (double) homeMapSize / 300);
//        }
//        else
//        {
//            magesRequired = (int) (((double) currentRound * (600 + homeMapSize) * passableTerrain) / (12000 * homeMapSize));
//        }
    }

    public static void setHealersRequired()
    {
        if (typeSortedUnitLists.get(UnitType.Factory).size() < 1 || shouldQueueWorker() || shouldQueueFactory())
        {
            return;
        }
        if (currentRound <= 75)
        {
            healersRequired = (int) (2 + ((double) currentRound / 75) * (double) homeMapSize / 300);
        }
        else
        {
            healersRequired = (int) (Math.round((rangersRequired + 1.8 * knightsRequired + 0.3 * magesRequired) / 5));
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
