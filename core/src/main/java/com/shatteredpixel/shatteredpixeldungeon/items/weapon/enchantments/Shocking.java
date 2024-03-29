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

package com.shatteredpixel.shatteredpixeldungeon.items.weapon.enchantments;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.effects.Lightning;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SparkParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.BArray;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class Shocking extends Weapon.Enchantment {

	private static ItemSprite.Glowing WHITE = new ItemSprite.Glowing( 0xFFFFFF, 0.5f );

	@Override
	public int proc( Weapon weapon, Char attacker, Char defender, int damage ) {
		// lvl 0 - 33%
		// lvl 1 - 50%
		// lvl 2 - 60%
		int level = Math.max( 0, weapon.level() );
		
		if (Random.Int( level + 3 ) >= 2) {
			boolean isEnhancer = attacker instanceof Hero && Dungeon.hero.subClass == HeroSubClass.ENHANCER;
			float chance = 0;
			int hit = 0;
			if (isEnhancer) {
				chance += (level + 3) / (float) (level + 10);
			}
			affected.clear();

			arcs.clear();
			arc(attacker, defender, 2);
			
			affected.remove(defender); //defender isn't hurt by lightning ||||only when the hero isn't enhancer
			if (Random.Float() < chance) {
				Buff.affect(defender, Paralysis.class, .5f);
				++hit;
			}

			for (Char ch : affected) {
				if(isEnhancer && ch.alignment == Char.Alignment.ALLY) {
					continue;
				}
				ch.damage(Math.round(damage*0.4f), this);

				if (isEnhancer && attacker.HP <= hit + 1) {
					continue;
				}

				if (Random.Float() < chance) {
					Buff.affect(defender, Paralysis.class, .5f);

					chance += .2f;
					if (Random.Float() < chance) {
						Buff.prolong(ch, Paralysis.class, .67f);
						ch.damage(Math.round(damage*0.6f), this);
						++hit;
					}
					++hit;
				}
			}

			if (isEnhancer && attacker.HP > hit)		attacker.damage(Math.min(hit, attacker.HT / 5),this);

			attacker.sprite.parent.addToFront( new Lightning( arcs, null ) );
			Sample.INSTANCE.play( Assets.SND_LIGHTNING );
			
		}


		return damage;
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return WHITE;
	}

	private ArrayList<Char> affected = new ArrayList<>();

	private ArrayList<Lightning.Arc> arcs = new ArrayList<>();
	
	private void arc( Char attacker, Char defender, int dist ) {
		
		affected.add(defender);
		
		defender.sprite.centerEmitter().burst(SparkParticle.FACTORY, 3);
		defender.sprite.flash();
		
		PathFinder.buildDistanceMap( defender.pos, BArray.not( Dungeon.level.solid, null ), dist );
		for (int i = 0; i < PathFinder.distance.length; i++) {
			if (PathFinder.distance[i] < Integer.MAX_VALUE) {
				Char n = Actor.findChar(i);
				if (n != null && n != attacker && !affected.contains(n)) {
					arcs.add(new Lightning.Arc(defender.sprite.center(), n.sprite.center()));
					arc(attacker, n, (Dungeon.level.water[n.pos] && !n.flying) ? 2 : 1);
				}
			}
		}
	}
}
