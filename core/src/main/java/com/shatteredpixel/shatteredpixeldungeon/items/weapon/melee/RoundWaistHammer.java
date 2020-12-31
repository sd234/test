package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Crab;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Random;

public class RoundWaistHammer extends MeleeWeapon {

    {
        image = ItemSpriteSheet.ROUND_WAIST_HAMMER;

        tier = 2;
    }

    @Override
    public int proc(Char attacker, Char defender, int damage) {
        if (defender instanceof  Crab && Random.Int(2) == 0) {
            damage += Random.Int(2, 5);
            Buff.affect(defender, Bleeding.class).set(damage);
        }

        return damage;
    }

    @Override
    public int max(int lvl) {
        return  3*(tier+1) +    //9 base, down from 15
                lvl*(tier-1);   //+2 per level, down from +3
    }

    @Override
    public int STRReq(int lvl) {
        return super.STRReq(lvl) - 1;
    }
}
