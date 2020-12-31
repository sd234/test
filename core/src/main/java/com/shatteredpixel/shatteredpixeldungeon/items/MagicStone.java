


package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.Runestone;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

import java.util.ArrayList;

public class MagicStone extends Item {
    public static final String AC_USE	= "USE";

    public static final float TIME_TO_USE = 1;

    {
        image = ItemSpriteSheet.MAGIC_STONE;

        stackable = true;
        unique = true;
        bones = false;

        defaultAction = AC_USE;
    }

    @Override
    public ArrayList<String> actions(Hero hero ) {
        ArrayList<String> actions = super.actions( hero );
        actions.add( AC_USE );
        return actions;
    }

    @Override
    public void execute( Hero hero, String action ) {

        super.execute( hero, action );

        if (action.equals( AC_USE )) {
            if (this.quantity() >= 3) {
                hero.spend( TIME_TO_USE );
                hero.busy();

                hero.sprite.operate( hero.pos );

                detach( hero.belongings.backpack );
                detach( hero.belongings.backpack );
                detach( hero.belongings.backpack );

                Runestone runestone;
                runestone = (Runestone) Generator.random(Generator.Category.STONE);
                runestone.collect();
                GLog.i(Messages.get(this, "get", runestone.name()));
            } else {
                GLog.w(Messages.get(this, "cant_use"));
            }
        }
    }

    @Override
    public boolean isUpgradable() {
        return false;
    }

    @Override
    public boolean isIdentified() {
        return true;
    }

    @Override
    public int price() {
        return 10 * quantity;
    }

}
