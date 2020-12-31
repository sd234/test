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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Roots;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.LeafParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.plants.BlandfruitBush;
import com.shatteredpixel.shatteredpixeldungeon.plants.Blindweed;
import com.shatteredpixel.shatteredpixeldungeon.plants.Dreamfoil;
import com.shatteredpixel.shatteredpixeldungeon.plants.Earthroot;
import com.shatteredpixel.shatteredpixeldungeon.plants.Fadeleaf;
import com.shatteredpixel.shatteredpixeldungeon.plants.Firebloom;
import com.shatteredpixel.shatteredpixeldungeon.plants.Icecap;
import com.shatteredpixel.shatteredpixeldungeon.plants.Plant;
import com.shatteredpixel.shatteredpixeldungeon.plants.Rotberry;
import com.shatteredpixel.shatteredpixeldungeon.plants.Sorrowmoss;
import com.shatteredpixel.shatteredpixeldungeon.plants.Starflower;
import com.shatteredpixel.shatteredpixeldungeon.plants.Stormvine;
import com.shatteredpixel.shatteredpixeldungeon.plants.Sungrass;
import com.shatteredpixel.shatteredpixeldungeon.plants.Swiftthistle;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class Blooming extends Weapon.Enchantment {
	
	private static ItemSprite.Glowing DARK_GREEN = new ItemSprite.Glowing( 0x008800 );
	
	@Override
	public int proc(Weapon weapon, Char attacker, Char defender, int damage) {
		
		// lvl 0 - 33%
		// lvl 1 - 50%
		// lvl 2 - 60%
		int level = Math.max( 0, weapon.level() );
        boolean isEnhancer = attacker instanceof Hero && Dungeon.hero.subClass == HeroSubClass.ENHANCER;
		
		if (Random.Int( level + 3 ) >= 2) {
		    if (isEnhancer &&  !Dungeon.level.water[defender.pos] && defender.buff(Roots.class) == null && !defender.flying) {
                Buff.prolong(defender, Roots.class, 2f);
            }
			
			boolean secondPlant = level > Random.Int(10);
			if (plantGrass(defender.pos, attacker)){
				if (secondPlant) secondPlant = false;
				else return damage;
			}
			
			ArrayList<Integer> positions = new ArrayList<>();
			for (int i : PathFinder.NEIGHBOURS8){
				positions.add(i);
			}
			Random.shuffle( positions );
			for (int i : positions){
				if (plantGrass(defender.pos + i, attacker)){
					if (secondPlant) secondPlant = false;
					else return damage;
				}
			}
			
		}
		
		return damage;
	}
	
	private boolean plantGrass(int cell, Char attacker){
        boolean isEnhancer = attacker instanceof Hero && Dungeon.hero.subClass == HeroSubClass.ENHANCER;
		int c = Dungeon.level.map[cell];
		if ( c == Terrain.EMPTY || c == Terrain.EMPTY_DECO
				|| c == Terrain.EMBERS || c == Terrain.GRASS){
		    if (isEnhancer && Random.Int(3) == 0) {
		        Plant.Seed seed = new BlandfruitBush.Seed();
		        switch (Random.Int(11)) {
                    case 0:
                        seed = new Blindweed.Seed();
                        break;
                    case 1:
                        seed = new Dreamfoil.Seed();
                        break;
                    case 2:
                        seed = new Earthroot.Seed();
                        break;
                    case 3:
                        seed = new Fadeleaf.Seed();
                        break;
                    case 4:
                        seed = new Firebloom.Seed();
                        break;
                    case 5:
                        seed = new Icecap.Seed();
                        break;
                    case 6:
                        seed = new Sorrowmoss.Seed();
                        break;
                    case 7:
                        seed = new Starflower.Seed();
                        break;
                    case 8:
                        seed = new Stormvine.Seed();
                        break;
                    case 9:
                        seed = new Sungrass.Seed();
                        break;
                    case 10:
                        seed = new Swiftthistle.Seed();
                        break;
                }
                Dungeon.level.plant( seed , cell );
            } else {
                Level.set(cell, Terrain.HIGH_GRASS);
            }
			GameScene.updateMap(cell);
			CellEmitter.get( cell ).burst( LeafParticle.LEVEL_SPECIFIC, 4 );
			return true;
		}
		return false;
	}
	
	@Override
	public ItemSprite.Glowing glowing() {
		return DARK_GREEN;
	}
}
