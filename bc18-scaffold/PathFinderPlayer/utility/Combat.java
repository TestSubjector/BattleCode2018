package utility;

import java.util.*;

import bc.*;

import static utility.Globals.*;

public class Combat
{
    // Both Movement and Attack on Cooldown
    // TODO - Add Ability Cooldown Later
    public static boolean unitFrozenByHeat(Unit unit)
    {
        if(unit.unitType() == UnitType.Healer)
        {
            return !(gc.isHealReady(unit.id()) || gc.isMoveReady(unit.id()));
        }
        else
        {
            return !(gc.isAttackReady(unit.id()) || gc.isMoveReady(unit.id()));
        }
    }
}
