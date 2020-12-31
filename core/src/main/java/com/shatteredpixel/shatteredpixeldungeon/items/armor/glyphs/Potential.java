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
import com.shatteredpixel.shatteredpixeldungeon.effects.Lightning;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.EnergyParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SparkParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor.Glyph;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite.Glowing;
import com.shatteredpixel.shatteredpixeldungeon.utils.BArray;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class Potential extends Glyph {
	
	private static ItemSprite.Glowing WHITE = new ItemSprite.Glowing( 0xFFFFFF, 0.6f );
	
	@Override
	public int proc( Armor armor, Char attacker, Char defender, int damage) {

		int level = Math.max( 0, armor.level() );
		float charge = 1f;

		boolean isEnhancer = defender instanceof Hero && Dungeon.hero.subClass == HeroSubClass.ENHANCER;
		int hit = 0;

		if (isEnhancer && Random.Int( level + 6 ) >= 5) {
			float chance = (level + 3) / (float) (level + 10);

			affected.clear();

			arcs.clear();
			arc(defender, attacker, 2);

			for (Char ch : affected) {
				if (ch.alignment == Char.Alignment.ALLY) {
					continue;
				}

				ch.damage(Math.round(damage*0.4f), this);
				if (Random.Float() < chance) {
					Buff.affect(attacker, Paralysis.class, .5f);
					chance += .2f;
					if (Random.Float() < chance) {
						Buff.prolong(ch, Paralysis.class, 1f);
						ch.damage(Math.round(damage*0.2f), this);
						++hit;
					}
					++hit;
				}
			}

			int dmg = hit * (Dungeon.depth / 5);
			if (dmg > defender.HT / 3 || defender.HP - dmg < defender.HT * .1f) {
				defender.damage(Math.min( defender.HT / 3, ((int) Math.floor(defender.HP - defender.HT * .1f)) ), this);
				for (int i :PathFinder.NEIGHBOURS9) {
					CellEmitter.center(defender.pos + i).start(SparkParticle.FACTORY, .1f,20);
					CellEmitter.center(defender.pos + i).start(SparkParticle.FACTORY, 10f,5);
					Char ch = Actor.findChar( defender.pos + i );
					if (ch != null) {
						if (ch instanceof Hero) {
							Buff.affect(ch, Paralysis.class, 3f);
						} else {
							Buff.affect(ch, Paralysis.class, 10f);
						}
					}
				}
			} else {
				defender.damage(dmg,this);
			}

			charge += hit;
			attacker.sprite.parent.addToFront( new Lightning( arcs, null ) );
			Sample.INSTANCE.play( Assets.SND_LIGHTNING );
		}

		// lvl 0 - 16.7%
		// lvl 1 - 28.6%
		// lvl 2 - 37.5%
		if (defender instanceof Hero && Random.Int( level + 6 ) >= 5 ) {
			int wands = ((Hero) defender).belongings.charge( charge );
			if (wands > 0) {
				defender.sprite.centerEmitter().burst(EnergyParticle.FACTORY, 10);
			}
		}
		
		return damage;
	}

	@Override
	public Glowing glowing() {
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
