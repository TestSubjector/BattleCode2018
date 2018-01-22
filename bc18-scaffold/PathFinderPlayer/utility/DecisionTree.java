package utility;

import java.util.*;

import bc.*;

import static utility.Globals.*;
import static utility.Combat.*;

public class DecisionTree
{


    public static long maxWorkerLimitAtTurn(long currentRound)
    {
        if (homeMapSize <= 500)
        {
            if (currentRound < 75)
            {
                if (initialTotalKarbonite > 1000)
                {
                    return 20;
                }
                else if (initialTotalKarbonite > 750)
                {
                    return 15;
                }
                else if (initialTotalKarbonite < 100)
                {
                    return 5;
                }
            }
            else
            {
                return 10;
            }
        }
        else if (homeMapSize <= 900)
        {
            if (currentRound < 85)
            {
                if (initialTotalKarbonite > 1000)
                {
                    return 20;
                }
                else if (initialTotalKarbonite > 750)
                {
                    return 15;
                }
                else if (initialTotalKarbonite < 100)
                {
                    return 5;
                }
            }
            else
            {
                if (initialTotalKarbonite < 500)
                {
                    return 12;
                }
                else if (initialTotalKarbonite > 1000)
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
            if (currentRound < 75)
            {
                if (initialTotalKarbonite > 3000)
                {
                    return 30;
                }
                else if (initialTotalKarbonite > 1000)
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
                if (initialTotalKarbonite < 500)
                {
                    return 10;
                }
                else if (initialTotalKarbonite > 1500)
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

    public static long maxFactoryLimit(long earthInitialTotalKarbonite)
    {
        return Math.round(4 + ((double) homeMapHeight + homeMapWidth) / 10) + earthInitialTotalKarbonite / 1000;
    }

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

    public static long maxRocketLimitAtTurn(long totalUnits)
    {
        if (enemyVecUnits.size() == 0 && currentRound > 500)
        {
            return 100;
        }
        else
        {
            // TODO  - Add area available on Mars and compare
            return Math.round((double) totalUnits / 30);
        }
    }


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
            builderFraction = Math.min(1, (((double) currentRound + 100) / 400) * (1 - 0.8 * ((double) karboniteLocations.size() / initialKarboniteLocationSize)));
        }
    }

    // Witch of Agnesi computation breaker
    public static boolean switchToPrimitiveMind(long currentRound, int timeLeft)
    {
        // Give 5 secs to pathfinding
        // Constant value by integrating (8*57^3)/(x^2 + 57^2) dx from x = -infinity to -375
        return timeLeft < 18000 * (Math.tanh((currentRound - 375) / 57)) + 26500;
    }

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
}
