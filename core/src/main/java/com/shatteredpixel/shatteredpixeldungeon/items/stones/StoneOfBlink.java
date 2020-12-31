/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2019 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.items.stones;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.BArray;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.PathFinder;

import java.util.ArrayList;

public class StoneOfBlink extends Runestone {

    private static final String AC_CAST	= "CAST";
    private static final float TIME_TO_CAST = 1.0f;


	
	{
		image = ItemSpriteSheet.STONE_BLINK;
        defaultAction = AC_CAST;
	}

	@Override
    public ArrayList<String> actions(Hero hero ) {
        ArrayList<String> actions = super.actions( hero );
        actions.add( AC_CAST );
        return actions;
    }

    @Override
    public void execute( Hero hero, String action ) {

        super.execute( hero, action );

        if (action.equals( AC_CAST )) {
            if (curUser.heroClass != HeroClass.RUNEMAGE) {
                //FIXME there need add a message
                GLog.w( Messages.get(StoneOfBlink.class, "cant_cast") );
                return;
            }
            curUser = hero;
            curItem = this;
            GameScene.selectCell( caster );
        }
    }

    protected static CellSelector.Listener caster = new  CellSelector.Listener() {
        @Override
        public void onSelect( Integer target ) {
            if (target != null) {
                PathFinder.buildDistanceMap(target, BArray.or(Dungeon.level.passable, Dungeon.level.avoid, null));
                if (PathFinder.distance[curUser.pos] == Integer.MAX_VALUE
                        || !curUser.fieldOfView[target]
                        || (!Dungeon.level.passable[target] && !Dungeon.level.avoid[target])
                        || Actor.findChar(target) != null) {
                    //TODO there need a message
                    GLog.w( Messages.get(ScrollOfTeleportation.class, "cant_reach") );
                    //curItem.collect(curUser.belongings.backpack);
                } else if (curUser.heroClass != HeroClass.RUNEMAGE) {
                    //TODO there need a message
                    GLog.w( Messages.get(StoneOfBlink.class, "cant_cast") );
                    //curItem.collect(curUser.belongings.backpack);
                } else {
                    int prepos = curUser.pos;
                    ScrollOfTeleportation.teleportToLocation(curUser, target);
                    if (curUser.subClass == HeroSubClass.RUNEMASTER) {
                        for (int i = 0; i < PathFinder.NEIGHBOURS8.length; i++) {
                            Char mob = Actor.findChar(prepos + PathFinder.NEIGHBOURS8[i]);
                            if (mob != null && mob != curUser && mob.alignment != Char.Alignment.ALLY) {
                                Buff.prolong(mob, Blindness.class, 5f);
                            }
                            Char mob2 = Actor.findChar(curUser.pos + PathFinder.NEIGHBOURS8[i]);
                            if (mob2 != null && mob2 != curUser && mob2.alignment != Char.Alignment.ALLY) {
                                Buff.prolong(mob2, Paralysis.class, 3f);
                            }
                        }
                    }
                    curItem.detach(curUser.belongings.backpack);
                    curUser.spendAndNext(TIME_TO_CAST);
                }
            }
        }
        @Override
        public String prompt() {
            return Messages.get(StoneOfBlink.class, "prompt");
        }
    };




    private static Ballistica throwPath;

	@Override
	public int throwPos(Hero user, int dst) {
		throwPath = new Ballistica( user.pos, dst, Ballistica.PROJECTILE );
		return throwPath.collisionPos;
	}
	
	@Override
	protected void onThrow(int cell) {
		if (Actor.findChar(cell) != null && throwPath.dist >= 1){
			cell = throwPath.path.get(throwPath.dist-1);
		}
		throwPath = null;
		super.onThrow(cell);
	}
	
	@Override
	public void activate(int cell) {
		ScrollOfTeleportation.teleportToLocation(curUser, cell);
	}
}
