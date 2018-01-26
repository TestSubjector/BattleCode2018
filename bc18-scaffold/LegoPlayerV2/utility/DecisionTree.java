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
            if(currentRound > 300)
            {
                workersRequired = 8;
            }
            else
            {
                if (karboniteLocations.size() > 500 && initialTotalKarbonite > 1000)
                {
                    workersRequired = 25;
                }
                else if (karboniteLocations.size() > 360)
                {
                    workersRequired = 20;
                }
                else if (karboniteLocations.size() > 180)
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
        else
        {

        }
    }

    public static void setFactoriesRequired()
    {
        if(homeMapSize <= 500)
        {
            factoriesRequired = (int) ((Math.min(1, Math.sqrt((double) currentRound / 450))) *
                    (Math.ceil(((double) homeMapHeight + homeMapWidth) / 15) + initialTotalKarbonite/400));
        }
        else if(homeMapSize <= 1000)
        {
            factoriesRequired = (int) ((Math.min(1, Math.sqrt((double) currentRound / 400))) *
                    (Math.round(((double) homeMapHeight + homeMapWidth) / 30)));
        }
        else if(homeMapSize <= 1600)
        {
            factoriesRequired = (int) ((Math.min(1, Math.sqrt((double) currentRound / 400))) *
                    (Math.round(((double) homeMapHeight + homeMapWidth))) / 15 + initialTotalKarbonite/4000);
        }
        else if(homeMapSize <= 2100)
        {

        }
        else
        {

        }
        if (currentRound > 10)
        {
            factoriesRequired += 1;
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
            if(currentRound < 75)
            {
                knightsRequired = (int) (4 + ((double) currentRound / 75) * (double) homeMapSize / 900);
            }
            else
            {
                knightsRequired = 3 * (int) (((double) currentRound * (800 + homeMapSize) * passableTerrain) / (12000 * homeMapSize));
            }
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
            rangersRequired = (int) (4 + ((double) currentRound / 125) * (double) homeMapSize / 400);
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
            if (currentRound <= 75)
            {
                healersRequired = (int) (3 + ((double) currentRound / 75) * (double) homeMapSize / 300);
            }
            else
            {
                healersRequired = (int) (Math.round((rangersRequired + 2.5 * knightsRequired) / 5));
            }
        }
        else
        {

        }
    }

    public static void setMagesRequired()
    {
        magesRequired = 0;
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
//package utility;
//
//import java.util.*;
//
//import bc.*;
//
//import static utility.Globals.*;
//import static utility.Combat.*;
//
//public class DecisionTree
//{
//
//    public static boolean shouldQueueWorker()
//    {
//        return typeSortedUnitLists.get(UnitType.Worker).size() + unitsInTrainQueue[UnitType.Worker.ordinal()] < workersRequired;
//    }
//
//    public static boolean shouldQueueRanger()
//    {
//        return typeSortedUnitLists.get(UnitType.Ranger).size() + unitsInTrainQueue[UnitType.Ranger.ordinal()] < rangersRequired;
//    }
//
//    public static boolean shouldQueueMage()
//    {
//        return typeSortedUnitLists.get(UnitType.Mage).size() + unitsInTrainQueue[UnitType.Mage.ordinal()] < magesRequired;
//    }
//
//    public static boolean shouldQueueKnight()
//    {
//        return typeSortedUnitLists.get(UnitType.Knight).size() + unitsInTrainQueue[UnitType.Knight.ordinal()] < knightsRequired;
//    }
//
//    public static boolean shouldQueueHealer()
//    {
//        return typeSortedUnitLists.get(UnitType.Healer).size() + unitsInTrainQueue[UnitType.Healer.ordinal()] < healersRequired;
//    }
//
//    public static boolean shouldQueueFactory()
//    {
//        return typeSortedUnitLists.get(UnitType.Factory).size() + unitsInBuildQueue[UnitType.Factory.ordinal()] < factoriesRequired;
//    }
//
//    public static boolean shouldQueueRocket()
//    {
//        return currentRound > 100 && typeSortedUnitLists.get(UnitType.Rocket).size() + unitsInBuildQueue[UnitType.Rocket.ordinal()] < rocketsRequired;
//    }
//
//    public static void setWorkersRequired()
//    {
//        if (currentRound <= 300)
//        {
//            if (homeMapSize <= 600)
//            {
//                if (karboniteLocations.size() > 150)
//                {
//                    workersRequired = 12;
//                }
//                else if (karboniteLocations.size() > 100)
//                {
//                    workersRequired = 10;
//                }
//                else
//                {
//                    workersRequired = 8;
//                }
//            }
//            else if (homeMapSize <= 1200)
//            {
//                if (karboniteLocations.size() > 180)
//                {
//                    workersRequired = 15;
//                }
//                else if (karboniteLocations.size() > 130)
//                {
//                    workersRequired = 12;
//                }
//                else
//                {
//                    workersRequired = 10;
//                }
//            }
//            else
//            {
//                if (karboniteLocations.size() > 250)
//                {
//                    workersRequired = 24;
//                }
//                else if (karboniteLocations.size() > 200)
//                {
//                    workersRequired = 20;
//                }
//                else if (karboniteLocations.size() > 150)
//                {
//                    workersRequired = 15;
//                }
//                else
//                {
//                    workersRequired = 12;
//                }
//            }
//        }
//        else
//        {
//            if (homeMapSize <= 600)
//            {
//                workersRequired = 6;
//            }
//            else if (homeMapSize <= 1200)
//            {
//                workersRequired = 10;
//            }
//            else
//            {
//                workersRequired = 12;
//            }
//        }
//        workersRequired = 2 + (int) (Math.min(1, Math.sqrt((double) (currentRound + 100) / 400)) * workersRequired);
//    }
//
//    public static void setFactoriesRequired()
//    {
//        factoriesRequired = (int) ((Math.min(1, Math.sqrt((double) currentRound / 400))) *
//                (Math.round(((double) homeMapHeight + homeMapWidth) / 15)));
//        if (currentRound > 10)
//        {
//            factoriesRequired += 1;
//        }
//    }
//
//    public static void setRocketsRequired()
//    {
//        if (enemyVecUnits.size() == 0)
//        {
//            rocketsRequired = 10;
//        }
//        else
//        {
//            rocketsRequired = (int) ((Math.min(1, (double) ((currentRound - 100) * (currentRound - 100)) / (500 * 500))) *
//                    Math.round((double) totalUnits / 15));
//        }
//    }
//
//    public static void setKnightsRequired()
//    {
//        if (typeSortedUnitLists.get(UnitType.Factory).size() < 1 || shouldQueueWorker() || shouldQueueFactory())
//        {
//            return;
//        }
//        if(homeMapSize <= 600)
//        {
//            if(currentRound < 75)
//            {
//                knightsRequired = (int) (4 + ((double) currentRound / 75) * (double) homeMapSize / 900);
//            }
//            else
//            {
//                knightsRequired = (int) (1 + ((double) currentRound / 75) * (double) homeMapSize / 900);
//            }
//        }
//        else if (currentRound <= 75)
//        {
//            knightsRequired = (int) (4 + ((double) currentRound / 75) * (double) homeMapSize / 300);
//        }
//        else
//        {
//            knightsRequired = 2 * (int) (((double) currentRound * (600 + homeMapSize) * passableTerrain) / (12000 * homeMapSize));
//        }
//    }
//
//    public static void setRangersRequired()
//    {
//        if (typeSortedUnitLists.get(UnitType.Factory).size() < 1 || shouldQueueWorker() || shouldQueueFactory())
//        {
//            return;
//        }
//        if(homeMapSize <= 600)
//        {
//            rangersRequired = (int) (6 + ((double) currentRound / 75) * (double) homeMapSize / 100);
//        }
//        else if (currentRound <= 75)
//        {
//            rangersRequired = (int) (2 + ((double) currentRound / 75) * (double) homeMapSize / 300);
//        }
//        else
//        {
//            rangersRequired = 2 * (int) Math.round(Math.min(1, Math.sqrt((double) currentRound / 450)) *
//                    ((homeMapSize - passableTerrain) / homeMapSize) *
//                    (23 + (homeMapSize - 400) * (23 / 2100)));
//        }
//    }
//
//    public static void setMagesRequired()
//    {
//        magesRequired = 0;
////        if (typeSortedUnitLists.get(UnitType.Factory).size() < 1 || shouldQueueWorker() || shouldQueueFactory())
////        {
////            return;
////        }
////        if (currentRound <= 75)
////        {
////            magesRequired = (int) (2 + ((double) currentRound / 75) * (double) homeMapSize / 300);
////        }
////        else
////        {
////            magesRequired = (int) (((double) currentRound * (600 + homeMapSize) * passableTerrain) / (12000 * homeMapSize));
////        }
//    }
//
//    public static void setHealersRequired()
//    {
//        if (typeSortedUnitLists.get(UnitType.Factory).size() < 1 || shouldQueueWorker() || shouldQueueFactory())
//        {
//            return;
//        }
//        if (currentRound <= 75)
//        {
//            healersRequired = (int) (2 + ((double) currentRound / 75) * (double) homeMapSize / 300);
//        }
//        else
//        {
//            healersRequired = (int) (Math.round((rangersRequired + 1.8 * knightsRequired + 0.3 * magesRequired) / 5));
//        }
//    }
//
//    public static boolean switchToPrimitiveMind(long currentRound, int timeLeft)
//    {
//        // Gives a leeway of 500 ms
//        // Constant value by integrating (8*57^3)/(x^2 + 57^2) dx from x = -infinity to -375 - Not anymore
//        return timeLeft < 501;
//    }
//
//    public static long timeToIdealRocketLaunch()
//    {
//        OrbitPattern currentOrbitPattern =  gc.orbitPattern();
//        long currentTimeToReachMars = currentOrbitPattern.duration(currentRound);
//        long idealTimeToLaunch = currentTimeToReachMars;
//        long indexFromCurrentRound = 0;
//        if(currentOrbitPattern.duration(currentRound + 1) > currentTimeToReachMars)
//        {
//            return currentRound;
//        }
//        for(long j = currentRound + 2; j < currentRound + 16; j++)
//        {
//            if(currentOrbitPattern.duration(j) < currentOrbitPattern.duration(j-1) + 1)
//            {
//                idealTimeToLaunch = currentOrbitPattern.duration(j);
//                indexFromCurrentRound = j;
//            }
//            else
//            {
//                break;
//            }
//        }
//        return  currentRound + indexFromCurrentRound;
//    }
//}
