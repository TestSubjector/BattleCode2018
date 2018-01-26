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
        else
        {
            if (karboniteLocations.size() == 0)
            {
                builderFraction = 1;
            }
            else if (initialKarboniteLocationSize > 120)
            {
                if (currentRound > 450)
                {
                    builderFraction = 1;
                }
                else
                {
                    builderFraction = Math.min(1, (((double) currentRound + 200) / 400) * (1 - 0.7 * ((double) karboniteLocations.size() / initialKarboniteLocationSize)));
                }
            }
            else
            {
                if (currentRound > 300)
                {
                    builderFraction = 1;
                }
                else
                {
                    builderFraction = Math.min(1, (((double) currentRound + 200) / 300) * (1 - 0.5 * ((double) karboniteLocations.size() / initialKarboniteLocationSize)));
                }
            }
        }
    }

    public static void setWorkersRequired()
    {
        if(homeMapSize <= 500)
        {
            if(currentRound > 300)
            {
                workersRequired = 5;
            }
            else
            {
                if (karboniteLocations.size() > 150)
                {
                    workersRequired = 9;
                }
                else if (karboniteLocations.size() > 100)
                {
                    workersRequired = 7;
                }
                else
                {
                    workersRequired = 5;
                }
            }
            workersRequired = 3 + (int) (Math.min(1, Math.sqrt((double) (currentRound + 125) / 400)) * workersRequired);
        }
        else if(homeMapSize <= 1000)
        {
            if(currentRound > 300)
            {
                workersRequired = 7;
            }
            else
            {
                if (karboniteLocations.size() > 180)
                {
                    workersRequired = 15;
                }
                else if (karboniteLocations.size() > 130)
                {
                    workersRequired = 11;
                }
                else
                {
                    workersRequired = 9;
                }
            }
            workersRequired = 3 + (int) (Math.min(1, Math.sqrt((double) (currentRound + 125) / 400)) * workersRequired);
        }
        else if(homeMapSize <= 1600)
        {

        }
        else if(homeMapSize <= 2100)
        {

        }
        else
        {

        }
    }

    public static void setFactoriesRequired()
    {
        if(homeMapSize <= 500)
        {
            factoriesRequired = 1 + (int) ((Math.min(1, Math.sqrt((double) currentRound / 450))) *
                    (Math.ceil(((double) homeMapHeight + homeMapWidth) / 15) + initialTotalKarbonite/400));
        }
        else if(homeMapSize <= 1000)
        {
            factoriesRequired = 1 + (int) ((Math.min(1, Math.sqrt((double) currentRound / 400))) *
                    (Math.round(((double) homeMapHeight + homeMapWidth) / 15)));
        }
        else if(homeMapSize <= 1600)
        {

        }
        else if(homeMapSize <= 2100)
        {

        }
        else
        {

        }
    }

    public static void setRocketsRequired()
    {
        if (enemyVecUnits.size() == 0 && currentRound > 400)
        {
            rocketsRequired = 10;
        }
        else
        {
            rocketsRequired = (int) ((Math.min(1, (double) ((currentRound - 100) * (currentRound - 100)) / (250 * 250))) *
                    Math.round((double) totalUnits / 10));
        }
    }

    public static void setKnightsRequired()
    {
        if(homeMapSize <= 500)
        {
            knightsRequired = (int) (4 + ((double) currentRound / 50) * (double) homeMapSize / 500);
        }
        else if(homeMapSize <= 1000)
        {
            if(currentRound < 75)
            {
                knightsRequired = (int) (4 + ((double) currentRound / 75) * (double) homeMapSize / 900);
            }
            else
            {
                knightsRequired = 2 * (int) (((double) currentRound * (600 + homeMapSize) * passableTerrain) / (12000 * homeMapSize));
            }
        }
        else if(homeMapSize <= 1600)
        {

        }
        else if(homeMapSize <= 2100)
        {

        }
        else
        {

        }
    }

    public static void setRangersRequired()
    {
        if(homeMapSize <= 500)
        {
            rangersRequired = (int) (1 + ((double) currentRound / 125) * (double) homeMapSize / 400);
        }
        else if(homeMapSize <= 1000)
        {
            rangersRequired = (int) (6 + ((double) currentRound / 75) * (double) homeMapSize / 100);
        }
        else if(homeMapSize <= 1600)
       {

        }
        else if(homeMapSize <= 2100)
        {

        }
        else
        {

        }
    }

    public static void setHealersRequired()
    {
        if(homeMapSize <= 500)
        {
            if (currentRound <= 75)
            {
                healersRequired = (int) (2 + ((double) currentRound / 75) * (double) homeMapSize / 300);
            }
            else
            {
                healersRequired = (int) (Math.round((rangersRequired + 3 * knightsRequired) / 7));
            }
        }
        else if(homeMapSize <= 1000)
        {
            if (currentRound <= 75)
            {
                healersRequired = (int) (2 + ((double) currentRound / 75) * (double) homeMapSize / 300);
            }
            else
            {
                healersRequired = (int) (Math.round((rangersRequired + 2 * knightsRequired) / 5));
            }
        }
        else if(homeMapSize <= 1600)
        {

        }
        else if(homeMapSize <= 2100)
        {

        }
        else
        {

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

    public static boolean switchToPrimitiveMind(long currentRound, int timeLeft)
    {
        // Gives a leeway of 500 ms
        // Constant value by integrating (8*57^3)/(x^2 + 57^2) dx from x = -infinity to -375 - Not anymore
        return timeLeft < 501;
    }

    public static long timeToIdealRocketLaunch()
    {
        OrbitPattern currentOrbitPattern =  gc.orbitPattern();
        long currentTimeToReachMars = currentOrbitPattern.duration(currentRound);
        long idealTimeToLaunch = currentTimeToReachMars;
        long indexFromCurrentRound = 0;
        if(currentOrbitPattern.duration(currentRound + 1) > currentTimeToReachMars)
        {
            return currentRound;
        }
        for(long j = currentRound + 2; j < currentRound + 16; j++)
        {
            if(currentOrbitPattern.duration(j) < currentOrbitPattern.duration(j-1) + 1)
            {
                idealTimeToLaunch = currentOrbitPattern.duration(j);
                indexFromCurrentRound = j;
            }
            else
            {
                break;
            }
        }
        return  currentRound + indexFromCurrentRound;
    }
}