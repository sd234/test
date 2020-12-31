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

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Gold;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite.Glowing;
import com.watabou.utils.Random;

public class Lucky extends Weapon.Enchantment {

	private static ItemSprite.Glowing GREEN = new ItemSprite.Glowing( 0x00FF00 );
	
	@Override
	public int proc( Weapon weapon, Char attacker, Char defender, int damage ) {
		int level = Math.max( 0, weapon.level() );
		boolean isEnhancer = attacker instanceof Hero && Dungeon.hero.subClass == HeroSubClass.ENHANCER;
		boolean willDead = damage >= defender.HP;
		float chance = 0;

		if (isEnhancer) {
			float dferHpMissPercent = (defender.HT - defender.HP) / (float) defender.HT;
			if (damage > defender.HT / 20 && damage > defender.HP / 10) {
				chance += dferHpMissPercent * .3f;
			}
			if (willDead) {
				chance += .1f + (level + 4) / (float) (level + 40);//old code : Random.Int( level + 40 ) >= 36
			}
		}

		// lvl 0 - 10%
		// lvl 1 ~ 12%
		// lvl 2 ~ 14%
		if (willDead || isEnhancer) {

			if (!isEnhancer) {
				chance = (level + 4) / (float) (level + 40);
			}

			if (Random.Float() < chance) {
				if (willDead) {
					Buff.affect(defender, LuckProc.class);
				} else {
					Dungeon.level.drop(Lucky.genLoot(), defender.pos).sprite.drop();
				}
			}

		}

		
		return damage;

	}
	
	public static Item genLoot(){
		float roll = Random.Float();
		if (roll < 0.6f){
			Item result = new Gold().random();
			result.quantity(Math.round(result.quantity() * 0.5f));
			return result;
		} else if (roll < 0.9f){
			return Random.Int(2) == 0
					? Generator.random(Generator.Category.SEED)
					: Generator.random(Generator.Category.STONE);
		} else {
			return Random.Int(2) == 0
					? Generator.random(Generator.Category.POTION)
					: Generator.random(Generator.Category.SCROLL);
		}
	}

	@Override
	public Glowing glowing() {
		return GREEN;
	}
	
	//used to keep track of whether a luck proc is incoming. see Mob.die()
	public static class LuckProc extends Buff {
		
		@Override
		public boolean act() {
			detach();
			return true;
		}
	}
	
}
