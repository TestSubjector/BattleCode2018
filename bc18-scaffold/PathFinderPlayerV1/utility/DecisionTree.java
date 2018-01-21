package utility;

import java.util.*;

import bc.*;

import static utility.Globals.*;
import static utility.Combat.*;

public class DecisionTree
{


    public static long maxWorkerLimitAtTurn(long currentRound)
    {
        if(homeMapSize <=500)
        {
            if(currentRound < 75)
            {
                if(earthInitialTotalKarbonite > 1000)
                {
                    return 20;
                }
                else if(earthInitialTotalKarbonite > 750)
                {
                    return 15;
                }
                else if(earthInitialTotalKarbonite < 100)
                {
                    return 5;
                }
            }
            else
            {
                return 10;
            }
        }
        else if(homeMapSize <=900)
        {
            if(currentRound < 85)
            {
                if(earthInitialTotalKarbonite > 1000)
                {
                    return 20;
                }
                else if(earthInitialTotalKarbonite > 750)
                {
                    return 15;
                }
                else if(earthInitialTotalKarbonite < 100)
                {
                    return 5;
                }
            }
            else
            {
                if(earthInitialTotalKarbonite < 500)
                {
                    return 12;
                }
                else if(earthInitialTotalKarbonite > 1000)
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
            if(currentRound < 75)
            {
                if(earthInitialTotalKarbonite > 3000)
                {
                    return 30;
                }
                else if(earthInitialTotalKarbonite > 1000)
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
                if(earthInitialTotalKarbonite < 500)
                {
                    return 10;
                }
                else if(earthInitialTotalKarbonite > 1500)
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
        return Math.round(3 + ((double) homeMapHeight + homeMapWidth)/10) + earthInitialTotalKarbonite/1000;
    }

    public static boolean makeRocketArmada(long totalUnits)
    {
        return totalUnits > earthPassableTerrain * ((double)homeMapHeight + homeMapWidth) / 2 * (homeMapSize);
    }

    public static long maxRocketLimitAtTurn(long totalUnits)
    {
        return Math.round((double)totalUnits/ 16);
    }

    // Witch of Agnesi computation breaker
    public static boolean switchToPrimitiveMind(long currentRound, int timeLeft)
    {
        // Give 5 secs to pathfinding
        // Constant value by integrating (8*57^3)/(x^2 + 57^2) dx from x = -infinity to -375
        return timeLeft < 3920 + 25992* Math.tanh((currentRound - 375)/57);
    }

}
