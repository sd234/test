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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Chill;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Frost;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.effects.Splash;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite.Glowing;
import com.watabou.utils.Random;

public class Chilling extends Weapon.Enchantment {

	private static ItemSprite.Glowing TEAL = new ItemSprite.Glowing( 0x00FFFF );
	
	@Override
	public int proc( Weapon weapon, Char attacker, Char defender, int damage ) {

		boolean isEnhancer = attacker instanceof Hero && Dungeon.hero.subClass == HeroSubClass.ENHANCER;
		if (defender.buff(Frost.class) != null && isEnhancer) {
			return damage;
		}

		// lvl 0 - 33%
		// lvl 1 - 50%
		// lvl 2 - 60%
		int level = Math.max( 0, weapon.level() );
		boolean inWater =  Dungeon.level.water[defender.pos];

		float maxTime = 6f;
		float eachTime = 3f;
		if (isEnhancer) {
			maxTime += 3f;
			eachTime += 1.5f;
			if (inWater) {
				maxTime += 1.5f;
				eachTime += .75f;
			}
		}

		if (Random.Int( level + 3 ) >= 2) {
			
			//adds 3 turns of chill per proc, with a cap of 6 turns
			float durationToAdd = eachTime;
			Chill existing = defender.buff(Chill.class);
			if (existing != null){
				durationToAdd = Math.min(durationToAdd, maxTime-existing.cooldown());
			}
			
			Buff.affect( defender, Chill.class, durationToAdd );
			existing = defender.buff(Chill.class);
			if ( isEnhancer && existing != null && Random.Float(7, 20) < defender.buff(Chill.class).cooldown() + (inWater ? 3 : 0) ) {
                //need to delay this through an actor so that the freezing isn't broken by taking damage from the staff hit.
                //words up there is written by Evan, I can't understand it now.
                new FlavourBuff(){
                    {actPriority = VFX_PRIO;}
                    public boolean act() {
                        Buff.affect(target, Frost.class, Frost.duration(target) * Random.Float(1f, 2f));
                        return super.act();
                    }
                }.attachTo(defender);
			}
			Splash.at( defender.sprite.center(), 0xFFB2D6FF, 5);

		}

		return damage;
	}
	
	@Override
	public Glowing glowing() {
		return TEAL;
	}

}
