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
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Fire;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.BlastParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.FlameParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SmokeParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.Bomb;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite.Glowing;
import com.shatteredpixel.shatteredpixeldungeon.utils.BArray;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

import javax.swing.text.ParagraphView;

public class Blazing extends Weapon.Enchantment {

	private static ItemSprite.Glowing ORANGE = new ItemSprite.Glowing( 0xFF4400 );
	
	@Override
	public int proc( Weapon weapon, Char attacker, Char defender, int damage ) {
		// lvl 0 - 33%
		// lvl 1 - 50%
		// lvl 2 - 60%
		int level = Math.max( 0, weapon.level() );
		float chance = (level + 1) / (float) (level + 3);// old code :Random.Int( level + 3 ) >= 2
		boolean isEnhancer = attacker instanceof Hero && Dungeon.hero.subClass == HeroSubClass.ENHANCER;

		if (Random.Float() < chance) {
			if (defender.buff(Burning.class) != null){
				Buff.affect(defender, Burning.class).reignite(defender, 8f);
				int burnDamage = Random.NormalIntRange( 1, 3 + Dungeon.depth/4 );
				defender.damage( Math.round(burnDamage * 0.67f), this );
				if (isEnhancer && Random.Float() < chance - .4f) {
					Buff.affect(defender, Cripple.class, 3f);
				}
				if (isEnhancer && defender.buff(Cripple.class) != null && Random.Float() < chance - .65f) {
					Buff.affect(defender, Paralysis.class, 1f);
				}
				if (isEnhancer && defender.buff(Paralysis.class) != null) {
                    BlazingExplode(defender.pos, damage);
                }
			} else {
				Buff.affect(defender, Burning.class).reignite(defender, 8f);
			}
			
			defender.sprite.emitter().burst( FlameParticle.FACTORY, level + 1 );
			
		}

		return damage;

	}
	
	@Override
	public Glowing glowing() {
		return ORANGE;
	}


    private void BlazingExplode(int cell, int damage){

        Sample.INSTANCE.play( Assets.SND_BLAST );


        ArrayList<Char> affected = new ArrayList<>();

        if (Dungeon.level.heroFOV[cell]) {
            CellEmitter.center(cell).burst(BlastParticle.FACTORY, 30);
        }

        boolean terrainAffected = false;
        for (int n : PathFinder.NEIGHBOURS9) {
            int c = cell + n;
            if (c >= 0 && c < Dungeon.level.length()) {
                if (Dungeon.level.heroFOV[c]) {
                    CellEmitter.get(c).burst(SmokeParticle.FACTORY, 4);
                }

                if (Dungeon.level.flamable[c]) {
                    Dungeon.level.destroy(c);
                    GameScene.updateMap(c);
                    terrainAffected = true;
                }

                //destroys items / triggers bombs caught in the blast.
                Heap heap = Dungeon.level.heaps.get(c);
                if (heap != null)
                    heap.explode();

                Char ch = Actor.findChar(c);
                if (ch != null) {
                    affected.add(ch);
                }
            }
        }

        for (Char ch : affected){

            if (ch instanceof Hero) {
                continue;
            }

            int dmg = (int) (damage * 0.67f);

            if (ch.pos != cell){
                dmg = Math.round(dmg*0.67f);
            }

            dmg -= ch.drRoll();

            if (dmg > 0) {
                ch.damage(dmg, this);
            }

            if (ch == Dungeon.hero && !ch.isAlive()) {
                Dungeon.fail(Bomb.class);
            }
        }

        if (terrainAffected) {
            Dungeon.observe();
        }

        PathFinder.buildDistanceMap( cell, BArray.not( Dungeon.level.solid, null ), Random.Int(1, 2) );
        for (int i = 0; i < PathFinder.distance.length; i++) {
            if (PathFinder.distance[i] < Integer.MAX_VALUE) {
                Char ch =  Actor.findChar(i);
                if (!Dungeon.level.pit[i]){
                    if(!(ch != null && ch instanceof Hero)) {
                        CellEmitter.get(i).burst(FlameParticle.FACTORY, 5);
                        GameScene.add(Blob.seed(i, 3, Fire.class));
                    }
                }
            }
        }
        Sample.INSTANCE.play(Assets.SND_BURNING);
    }

}
