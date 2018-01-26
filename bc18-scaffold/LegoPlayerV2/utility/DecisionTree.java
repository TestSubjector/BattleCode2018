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

    public static void maintainArmyRatio()
    {
        if(rangerMeta)
        {
            long numberOfHealers = typeSortedUnitLists.get(UnitType.Healer).size();
            long numberOfRangers = typeSortedUnitLists.get(UnitType.Ranger).size();
            if( numberOfRangers < 2* numberOfHealers)
            {
                addUnitToBuildQueueUrgently(UnitType.Ranger);
            }
            else
            {
                addUnitToBuildQueueUrgently(UnitType.Healer);
            }
        }
        else
        {
            if(homeMapSize <= 500)
            {
                long numberOfKnights = typeSortedUnitLists.get(UnitType.Knight).size();
                long numberOfHealers = typeSortedUnitLists.get(UnitType.Healer).size();
                if( numberOfKnights < 2.5 * numberOfHealers)
                {
                    addUnitToBuildQueueUrgently(UnitType.Knight);
                }
                else
                {
                    addUnitToBuildQueueUrgently(UnitType.Healer);
                }
            }
            else if(homeMapSize <= 1000)
            {
                long numberOfKnights = typeSortedUnitLists.get(UnitType.Knight).size();
                long numberOfHealers = typeSortedUnitLists.get(UnitType.Healer).size();
                long numberOfRangers = typeSortedUnitLists.get(UnitType.Ranger).size();
                
            }
            else if(homeMapSize <= 1600)
            {
                long numberOfKnights = typeSortedUnitLists.get(UnitType.Knight).size();
                long numberOfHealers = typeSortedUnitLists.get(UnitType.Healer).size();
                long numberOfRangers = typeSortedUnitLists.get(UnitType.Ranger).size();
            }
            else
            {
                long numberOfKnights = typeSortedUnitLists.get(UnitType.Knight).size();
                long numberOfHealers = typeSortedUnitLists.get(UnitType.Healer).size();
                long numberOfRangers = typeSortedUnitLists.get(UnitType.Ranger).size();
            }
        }
    }

    public static void setWorkersRequired()
    {
        if(homeMapSize <= 500)
        {
            if(currentRound > 300)
            {
                workersRequired = 6;
            }
            else
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
            workersRequired -= 2;
        }
        else if(homeMapSize <= 1000)
        {
            if(currentRound > 300)
            {
                workersRequired = 9;
            }
            else
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
        }
        else if(homeMapSize <= 1600)
        {
            if(currentRound > 300)
            {
                workersRequired = 11;
            }
            else
            {
                if (karboniteLocations.size() > 250)
                {
                    workersRequired = 24;
                }
                else if (karboniteLocations.size() > 200)
                {
                    workersRequired = 20;
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
            if(currentRound > 300)
            {
                workersRequired = 10;
            }
            else
            {
                if (karboniteLocations.size() > 250)
                {
                    workersRequired = 24;
                }
                else if (karboniteLocations.size() > 200)
                {
                    workersRequired = 20;
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
        workersRequired = 5 + (int) (Math.min(1, Math.sqrt((double) (currentRound + 125) / 400)) * workersRequired);
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
        else
        {
            factoriesRequired = (int) ((Math.min(1, Math.sqrt((double) currentRound / 400))) *
                    (Math.round(((double) homeMapHeight + homeMapWidth) / 15)));
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
            if(currentRound < 75)
            {
                knightsRequired = (int) (4 + ((double) currentRound / 75) * (double) homeMapSize / 900);
            }
            else
            {
                knightsRequired = (int) (1 + ((double) currentRound / 75) * (double) homeMapSize / 900);
            }
        }
        else if(homeMapSize <= 1000)
        {
            if(currentRound < 75)
            {
                knightsRequired = (int) (4 + ((double) currentRound / 75) * (double) homeMapSize / 300);
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
                knightsRequired = (int) (4 + ((double) currentRound / 75) * (double) homeMapSize / 300);
            }
            else
            {
                knightsRequired = 2 * (int) (((double) currentRound * (600 + homeMapSize) * passableTerrain) / (12000 * homeMapSize));
            }
        }
        else
        {
            if(currentRound < 75)
            {
                knightsRequired = (int) (4 + ((double) currentRound / 75) * (double) homeMapSize / 300);
            }
            else
            {
                knightsRequired = 2 * (int) (((double) currentRound * (600 + homeMapSize) * passableTerrain) / (12000 * homeMapSize));
            }
        }
    }

    public static void setRangersRequired()
    {
        if(homeMapSize <= 500)
        {
            if (currentRound <= 75)
            {
                rangersRequired = (int) (2 + ((double) currentRound / 75) * (double) homeMapSize / 300);
            }
            else
            {
                rangersRequired = (int) (6 + ((double) currentRound / 75) * (double) homeMapSize / 100);
            }
            rangersRequired = 0;
        }
        else if(homeMapSize <= 1000)
        {
            if (currentRound <= 75)
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
        else if(homeMapSize <= 1600)
        {
            if (currentRound <= 75)
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
        else
        {
            if (currentRound <= 75)
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
                healersRequired = (int) (Math.round((rangersRequired + 1.8 * knightsRequired + 0.3 * magesRequired) / 5));
            }
        }
        else if(homeMapSize <= 1600)
        {
            if (currentRound <= 75)
            {
                healersRequired = (int) (2 + ((double) currentRound / 75) * (double) homeMapSize / 300);
            }
            else
            {
                healersRequired = (int) (Math.round((rangersRequired + 1.8 * knightsRequired + 0.3 * magesRequired) / 5));
            }
        }
        else
        {
            if (currentRound <= 75)
            {
                healersRequired = (int) (2 + ((double) currentRound / 75) * (double) homeMapSize / 300);
            }
            else
            {
                healersRequired = (int) (Math.round((rangersRequired + 1.8 * knightsRequired + 0.3 * magesRequired) / 5));
            }
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
