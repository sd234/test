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

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.watabou.utils.Random;

public class Stone extends Armor.Glyph {

	private static ItemSprite.Glowing GREY = new ItemSprite.Glowing( 0x222222 );

	@Override
	public int proc(Armor armor, Char attacker, Char defender, int damage) {

		boolean isEnhancer = defender instanceof Hero && Dungeon.hero.subClass == HeroSubClass.ENHANCER;
		
		testing = true;
		float evasion = defender.defenseSkill(attacker);
		float accuracy = attacker.attackSkill(defender);
		testing = false;
		
		float hitChance;
		if (evasion >= accuracy){
			hitChance = (accuracy/evasion)/2f;
		} else {
			hitChance = 1f - (evasion/accuracy)/2f;
		}
		if (isEnhancer && Random.Float() * .8f > hitChance) {
			//all damage absorb
			return 0;
		}
		
		//75% of dodge chance is applied as damage reduction
		hitChance = (1f + 3f*hitChance)/4f;
		
		damage = (int)Math.ceil(damage * hitChance);
		
		return damage;
	}
	
	private boolean testing = false;
	
	public boolean testingEvasion(){
		return testing;
	}

	@Override
	public ItemSprite.Glowing glowing() {
		return GREY;
	}

}
