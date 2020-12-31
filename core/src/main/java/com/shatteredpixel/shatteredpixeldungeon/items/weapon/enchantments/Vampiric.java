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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hunger;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite.Glowing;
import com.watabou.utils.Random;

import static com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hunger.STARVING;

public class Vampiric extends Weapon.Enchantment {

	private static ItemSprite.Glowing RED = new ItemSprite.Glowing( 0x660022 );
	
	@Override
	public int proc( Weapon weapon, Char attacker, Char defender, int damage ) {

		boolean isEnhancer = attacker instanceof Hero && Dungeon.hero.subClass == HeroSubClass.ENHANCER;
		
		//chance to heal scales from 5%-30% based on missing HP
		float missingPercent = (attacker.HT - attacker.HP) / (float)attacker.HT;
		float healChance = 0.05f + .25f*missingPercent;
		boolean willDead = damage >= defender.HP;

		if (isEnhancer) {
            int level = Math.max( 0, weapon.level() );
			float chanceAdd = (defender.HT - defender.HP) / (float)defender.HT;
			chanceAdd *= level / 30f;
			healChance += chanceAdd;
		}

		if (Random.Float() < healChance){
			
			//heals for 50% of damage dealt
			int healAmt = Math.round(damage * 0.5f);
			if (isEnhancer && willDead) {
				healAmt += Math.min(defender.HP, defender.HT / 3);
			}
			healAmt = Math.min( healAmt, attacker.HT - attacker.HP );

			
			if (healAmt > 0 && attacker.isAlive()) {
				
				attacker.HP += healAmt;
				if (isEnhancer) {
					//STARVING=450f,is a final float from buffs.Hunger
					//get satisfy as the percentage of heal
					Buff.affect(Dungeon.hero, Hunger.class).satisfy(healAmt * STARVING / Dungeon.hero.HT);
				}

				attacker.sprite.emitter().start( Speck.factory( Speck.HEALING ), 0.4f, 1 );
				attacker.sprite.showStatus( CharSprite.POSITIVE, Integer.toString( healAmt ) );
				
			}
		}

		return damage;
	}
	
	@Override
	public Glowing glowing() {
		return RED;
	}
}
