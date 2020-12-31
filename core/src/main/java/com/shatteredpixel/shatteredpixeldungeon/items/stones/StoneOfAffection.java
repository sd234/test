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
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Amok;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Charm;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;

public class StoneOfAffection extends Runestone {
	
	{
		image = ItemSpriteSheet.STONE_AFFECTION;
	}
	
	@Override
	public void activate(int cell) {
		
		for (int i : PathFinder.NEIGHBOURS9){
			
			CellEmitter.center(cell + i).start( Speck.factory( Speck.HEART ), 0.2f, 5 );
			
			
			Char ch = Actor.findChar( cell + i );
			
			if (ch != null && ch.alignment == Char.Alignment.ENEMY){
				float timeOfCharm = 10f;
				if (curUser.heroClass == HeroClass.RUNEMAGE) {
					timeOfCharm += 20f;
					if (ch.buff(Charm.class) != null) {
						timeOfCharm *= 2f;
						if (curUser.subClass == HeroSubClass.RUNEMASTER) {
							Buff.prolong(ch, Paralysis.class, timeOfCharm / 5f);
						}
					}
				}

				Buff.prolong(ch, Charm.class, timeOfCharm).object = curUser.id();
				if (curUser.subClass == HeroSubClass.RUNEMASTER) {
					Buff.prolong(ch, Amok.class, timeOfCharm / 3f);
				}
			}
		}
		
		Sample.INSTANCE.play( Assets.SND_CHARMS );
		
	}
	
}
