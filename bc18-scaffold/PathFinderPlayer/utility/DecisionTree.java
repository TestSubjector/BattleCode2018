package utility;

import java.util.*;

import bc.*;

import static utility.Globals.*;
import static utility.Combat.*;

public class DecisionTree
{
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
        else if(currentRound > 300)
        {
            builderFraction = 1;
        }
        else
        {
            builderFraction = Math.min(1, (((double) currentRound + 200) / 300) * (1 - 0.4 * ((double) karboniteLocations.size() / initialKarboniteLocationSize)));
        }
    }

    public static void setWorkersRequired()
    {
        if (currentRound <= 300)
        {
            if (homeMapSize <= 600)
            {
                if (karboniteLocations.size() > 150)
                {
                    workersRequired = 12;
                }
                else if (karboniteLocations.size() > 100)
                {
                    workersRequired = 10;
                }
                else
                {
                    workersRequired = 8;
                }
            }
            else if (homeMapSize <= 1200)
            {
                if (karboniteLocations.size() > 180)
                {
                    workersRequired = 15;
                }
                else if (karboniteLocations.size() > 130)
                {
                    workersRequired = 12;
                }
                else
                {
                    workersRequired = 10;
                }
            }
            else
            {
                if (karboniteLocations.size() > 200)
                {
                    workersRequired = 18;
                }
                else if (karboniteLocations.size() > 150)
                {
                    workersRequired = 15;
                }
                else
                {
                    workersRequired = 12;
                }
            }
        }
        else
        {
            if (homeMapSize <= 600)
            {
                workersRequired = 6;
            }
            else if (homeMapSize <= 1200)
            {
                workersRequired = 10;
            }
            else
            {
                workersRequired = 12;
            }
        }
        workersRequired = 2 + (int) (Math.min(1, Math.sqrt((double) (currentRound + 100) / 400)) * workersRequired);
    }

    public static void setFactoriesRequired()
    {
        factoriesRequired = 1 + (int) ((Math.min(1, Math.sqrt((double) currentRound / 400))) *
                (Math.round(2 + ((double) homeMapHeight + homeMapWidth) / 10) +
                        (double) initialTotalKarbonite / 2000));
    }

    public static void setRocketsRequired()
    {
        if (enemyVecUnits.size() == 0)
        {
            rocketsRequired = 10;
        }
        else
        {
            rocketsRequired = (int) ((Math.min(1, (double) ((currentRound - 100) * (currentRound - 100)) / (500 * 500))) *
                    Math.round((double) totalUnits / 20));
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
            if(currentRound < 75)
            {
                knightsRequired = (int) (4 + ((double) currentRound / 75) * (double) homeMapSize / 900);
            }
            else
            {
                knightsRequired = (int) (1 + ((double) currentRound / 75) * (double) homeMapSize / 900);
            }
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

    public static boolean switchToPrimitiveMind(long currentRound, int timeLeft)
    {
        // Gives a leeway of 500 ms
        // Constant value by integrating (8*57^3)/(x^2 + 57^2) dx from x = -infinity to -375 - Not anymore
        return timeLeft < 501;
    }

    public static long timeToIdealRocketLaunch()
    {
        long center;
        return 0;
    }

}
