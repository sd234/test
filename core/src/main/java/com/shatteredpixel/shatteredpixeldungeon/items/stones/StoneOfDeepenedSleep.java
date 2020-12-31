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

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Drowsy;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicalSleep;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Sleep;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Weakness;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;

public class StoneOfDeepenedSleep extends Runestone {
	
	{
		image = ItemSpriteSheet.STONE_SLEEP;
	}
	
	@Override
	public void activate(int cell) {
		if (curUser.heroClass != HeroClass.RUNEMAGE) {
			for (int i : PathFinder.NEIGHBOURS9) {

				CellEmitter.get(cell + i).start(Speck.factory(Speck.NOTE), 0.1f, 2);

				if (Actor.findChar(cell + i) != null) {

					Char c = Actor.findChar(cell + i);

					if ((c instanceof Mob && ((Mob) c).state == ((Mob) c).SLEEPING)) {

						Buff.affect(c, MagicalSleep.class);

					}

				}
			}
			Sample.INSTANCE.play(Assets.SND_LULLABY);

			return;
		}

		//If curUser is rune mage
		int distance = 2;
		if (curUser.subClass == HeroSubClass.RUNEMASTER) {
			++distance;
		}
		int cx = cell % Dungeon.level.width();
		int cy = cell / Dungeon.level.width();
		int ax = cx - distance;
		if (ax < 0) {
			ax = 0;
		}
		int bx = cx + distance;
		if (bx >= Dungeon.level.width()) {
			bx = Dungeon.level.width() - 1;
		}
		int ay = cy - distance;
		if (ay < 0) {
			ay = 0;
		}
		int by = cy + distance;
		if (by >= Dungeon.level.height()) {
			by = Dungeon.level.height() - 1;
		}
		for (int y = ay; y <= by; y++){
			for (int x = ax, p = ax + y * Dungeon.level.width(); x <= bx; x++, p++) {
				CellEmitter.get(p).start( Speck.factory( Speck.NOTE ), 0.1f, 2 );

				if (Actor.findChar(p) != null) {

					Char c = Actor.findChar(p);

					if (c instanceof  Mob) {
						if (((Mob) c).state == ((Mob) c).SLEEPING) {
							Buff.affect(c, MagicalSleep.class);
							if (curUser.subClass == HeroSubClass.RUNEMASTER && c.alignment != Char.Alignment.ALLY) {
								Buff.prolong(c, Weakness.class, 200f);
							}
						} else {
							Buff.affect(c, Sleep.class);
							if (curUser.subClass == HeroSubClass.RUNEMASTER) {
								Buff.affect(c, Drowsy.class);
							}
						}
					}

					if (c == curUser && curUser.subClass == HeroSubClass.RUNEMASTER) {
						Level l = Dungeon.level;
						if (l.distance(p, cell) <= 1) {
							if (p == cell) {
								Buff.affect(c, MagicalSleep.class);
							} else {
								Buff.affect(c, Drowsy.class);
							}
						}
					}

				}
			}
		}

		Sample.INSTANCE.play( Assets.SND_LULLABY );

	}
}
