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
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.Trap;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.ShadowCaster;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class StoneOfDisarming extends Runestone {
	
	private static final int DIST = 8;
	
	{
		image = ItemSpriteSheet.STONE_DISARM;
	}
	
	@Override
	public void activate(final int cell) {
		boolean[] FOV = new boolean[Dungeon.level.length()];
		Point c = Dungeon.level.cellToPoint(cell);
		ShadowCaster.castShadow(c.x, c.y, FOV, Dungeon.level.losBlocking, DIST);
		
		int sX = Math.max(0, c.x - DIST);
		int eX = Math.min(Dungeon.level.width()-1, c.x + DIST);
		
		int sY = Math.max(0, c.y - DIST);
		int eY = Math.min(Dungeon.level.height()-1, c.y + DIST);
		
		ArrayList<Trap> disarmCandidates = new ArrayList<>();
		
		for (int y = sY; y <= eY; y++){
			int curr = y*Dungeon.level.width() + sX;
			for ( int x = sX; x <= eX; x++){

				if (curUser.heroClass == HeroClass.RUNEMAGE) {
					Level l = Dungeon.level;
					if (Actor.findChar(curr) != null) {
						Char ch = Actor.findChar(curr);
						//int power = 20 - 4 * l.distance(ch.pos, cell);
						if (l.distance(ch.pos, cell) <= 3 || curUser.subClass == HeroSubClass.RUNEMASTER) {
							if (ch.properties().contains(Char.Property.UNDEAD) || ch.properties().contains(Char.Property.DEMONIC)) {
								ch.sprite.emitter().start( ShadowParticle.UP, 0.05f, 10 );

								int damage = Math.round(Random.NormalIntRange( Dungeon.depth+5, 10 + Dungeon.depth * 2 ) * 0.67f);
								ch.damage(damage, this);
							}
						}
					}
				}


				if (FOV[curr]){
					
					Trap t = Dungeon.level.traps.get(curr);
					if (t != null && t.active){
						disarmCandidates.add(t);
					}
					
				}
				curr++;
			}
		}
		
		Collections.sort(disarmCandidates, new Comparator<Trap>() {
			@Override
			public int compare(Trap o1, Trap o2) {
				float diff = Dungeon.level.trueDistance(cell, o1.pos) - Dungeon.level.trueDistance(cell, o2.pos);
				if (diff < 0){
					return -1;
				} else if (diff == 0){
					return Random.Int(2) == 0 ? -1 : 1;
				} else {
					return 1;
				}
			}
		});
		
		//disarms at most nine traps
		int disArmSum = 0;
		if (curUser.heroClass == HeroClass.RUNEMAGE) {
			disArmSum = 13;
			if (curUser.subClass == HeroSubClass.RUNEMASTER) {
				disArmSum = 25;
			}
		} else {
			disArmSum = 9;
		}
		while (disarmCandidates.size() > disArmSum){
			disarmCandidates.remove(disArmSum);
		}
		
		for ( Trap t : disarmCandidates){
			t.reveal();
			t.disarm();
			CellEmitter.get(t.pos).burst(Speck.factory(Speck.STEAM), 6);
		}
		
		Sample.INSTANCE.play( Assets.SND_TELEPORT );
	}
}
