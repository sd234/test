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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vertigo;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.Bomb;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class StoneOfBlast extends Runestone {
	
	{
		image = ItemSpriteSheet.STONE_BLAST;
	}
	
	@Override
	public void activate(int cell) {
		new Bomb().explode(cell);
		if (curUser.heroClass != HeroClass.RUNEMAGE) {
			return;
		}
		Level l = Dungeon.level;
		for (Char ch : Actor.chars()) {
			int power = 20 - 4 * l.distance(ch.pos, cell);
			if (power >= 12) {
				Buff.prolong(ch, Cripple.class, power - 2);
			}
			if (ch.fieldOfView != null && ch.fieldOfView[cell] && curUser.subClass == HeroSubClass.RUNEMASTER && power > 0) {
					Buff.prolong(ch, Vertigo.class, power);
					Buff.prolong(ch, Blindness.class, power);
					new Bomb().explode(ch.pos);
				if (ch == Dungeon.hero) {
					GameScene.flash(0xFFFFFF);
				}
			}
		}
	}
}
