package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class RunicShortsword extends MeleeWeapon {
    {
        image = ItemSpriteSheet.RUNIC_SHORTWORD;

        tier = 1;

        bones = false;
    }

    @Override
    public int max(int lvl) {
        return (9 + lvl * (tier + 2)) * (hasGoodEnchant() ? 4 : 3) / 3;
    }

}
