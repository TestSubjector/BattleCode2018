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
                if(initialTotalKarbonite > 1000)
                {
                    return 20;
                }
                else if(initialTotalKarbonite > 750)
                {
                    return 15;
                }
                else if(initialTotalKarbonite < 100)
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
                if(initialTotalKarbonite > 1000)
                {
                    return 20;
                }
                else if(initialTotalKarbonite > 750)
                {
                    return 15;
                }
                else if(initialTotalKarbonite < 100)
                {
                    return 5;
                }
            }
            else
            {
                if(initialTotalKarbonite < 500)
                {
                    return 12;
                }
                else if(initialTotalKarbonite > 1000)
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
                if(initialTotalKarbonite > 3000)
                {
                    return 30;
                }
                else if(initialTotalKarbonite > 1000)
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
                if(initialTotalKarbonite < 500)
                {
                    return 10;
                }
                else if(initialTotalKarbonite > 1500)
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
        return Math.round(4 + 2 *((double) homeMapHeight + homeMapWidth)/10) + earthInitialTotalKarbonite/1000;
    }

    public static boolean makeRocketArmada(long totalUnits)
    {
        if(rocketProductionCooldown > 0)
        {
            return false;
        }
        else if(enemyVecUnits.size() == 0 && currentRound > 500)
        {
            return true;
        }
        else
        {
            return totalUnits > passableTerrain * ((double)homeMapHeight + homeMapWidth) / (2 * (homeMapSize));
        }
    }

    public static long maxRocketLimitAtTurn(long totalUnits)
    {
        if(enemyVecUnits.size() == 0 && currentRound > 500)
        {
            return 100;
        }
        else
        {
            // TODO  - Add area available on Mars and compare
            return Math.round((double)totalUnits / 30);
        }
    }

    public static void currentBuilderFraction()
    {
        // +0.1 to stop Arithmetic Exception
//        double remainingKarbonite = earthInitialTotalKarbonite / (earthInitialTotalKarbonite - (10 * currentRound) + 0.1);
//        if(currentRound > 180 || earthInitialTotalKarbonite < 100)
//        {
//            builderFraction = 1;
//        }
//        else if(remainingKarbonite > 0)
//        {
//            builderFraction =  0.35 + 0.65 * (1 / remainingKarbonite);
//        }
//        else
//        {
//            builderFraction = 1;
//        }
    }

    // Witch of Agnesi computation breaker
    public static boolean switchToPrimitiveMind(long currentRound, int timeLeft)
    {
        // Give 5 secs to pathfinding
        // Constant value by integrating (8*57^3)/(x^2 + 57^2) dx from x = -infinity to -375
        return timeLeft < 3920 + 25992* Math.tanh((currentRound - 375)/57);
    }

    // Cooldown till you can again make a Rocket
    public  static void findRocketProductionCooldown()
    {
        if(currentRound > 700)
        {
            rocketProductionCooldown = 0;
        }
        else
        {
            rocketProductionCooldown--;
        }
    }
}
