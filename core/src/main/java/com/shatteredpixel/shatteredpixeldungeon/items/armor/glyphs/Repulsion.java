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

package com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.watabou.noosa.Camera;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;

public class Repulsion extends Armor.Glyph {

	private static ItemSprite.Glowing WHITE = new ItemSprite.Glowing( 0xFFFFFF );
	
	@Override
	public int proc( Armor armor, Char attacker, Char defender, int damage) {

		int level = Math.max( 0, armor.level() );
		boolean isEnhancer = defender instanceof Hero && Dungeon.hero.subClass == HeroSubClass.ENHANCER;
		int power = 2;
		if (isEnhancer) {
			power += level;
		}

		if (Random.Int( level + 5 ) >= 4){
			int oppositeHero = attacker.pos + (attacker.pos - defender.pos);
			Ballistica trajectory = new Ballistica(attacker.pos, oppositeHero, Ballistica.MAGIC_BOLT);
			WandOfBlastWave.throwChar(attacker, trajectory, power);

			if (isEnhancer) {
				//FIXME there is a bug that enemy may be struck in wall.
				trajectory =  new Ballistica(attacker.pos, oppositeHero, Ballistica.MAGIC_BOLT);
                int dist = Math.min(trajectory.dist, power);

                if (attacker.properties().contains(Char.Property.BOSS))
                    dist /= 2;

                if (dist == 0 || attacker.properties().contains(Char.Property.IMMOVABLE)) return damage;

                if (Actor.findChar(trajectory.path.get(dist)) != null){
                    dist--;
                }

                final int newPos = trajectory.path.get(dist);

                if (newPos == attacker.pos) return damage;
				if (newPos == trajectory.collisionPos) {
					if (Dungeon.level.heroFOV[newPos]) {
						CellEmitter.get( newPos - Dungeon.level.width() ).start(Speck.factory(Speck.ROCK), 0.07f, 10);
						Camera.main.shake(3, 0.7f);
						Sample.INSTANCE.play(Assets.SND_ROCKS);
					}
					if (attacker.isAlive()) {
						Buff.affect(attacker, Paralysis.class, Random.Float(1f, 5f));
						int dmg = (int)Math.ceil(attacker.buff(Paralysis.class).cooldown());
						dmg *= Dungeon.depth / 5 + 1;
						attacker.damage(dmg, this);
					}
				}
//				if (/*attacker.buff(Paralysis.class) != null && */Dungeon.level.heroFOV[ attacker.pos ]){
//					Buff.affect(attacker, Paralysis.class, 5f);
//					int dmg = (int)Math.ceil(attacker.buff(Paralysis.class).cooldown());
//					dmg *= Dungeon.depth / 5 + 1;
//					attacker.damage(dmg, this);
//					CellEmitter.get( attacker.pos - Dungeon.level.width() ).start(Speck.factory(Speck.ROCK), 0.07f, 10);
//					Camera.main.shake(3, 0.7f);
//					Sample.INSTANCE.play(Assets.SND_ROCKS);
//				}
			}
		}
		
		return damage;
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return WHITE;
	}
}
