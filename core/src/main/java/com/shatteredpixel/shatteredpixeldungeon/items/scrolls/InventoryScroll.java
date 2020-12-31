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

package com.shatteredpixel.shatteredpixeldungeon.items.scrolls;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.MagicStone;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.Runestone;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfAugmentation;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfDisarming;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfEnchantment;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfIntuition;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.noosa.audio.Sample;

public abstract class InventoryScroll extends Scroll {

	protected String inventoryTitle = Messages.get(this, "inv_title");
	protected WndBag.Mode mode = WndBag.Mode.ALL;

//	public boolean used = false;
	
	@Override
	public void doRead() {
		
		if (!isKnown()) {
			setKnown();
			identifiedByUse = true;
		} else {
			identifiedByUse = false;
		}
		
		GameScene.selectItem( itemSelector, mode, inventoryTitle );
	}
	
	private void confirmCancelation() {
		GameScene.show( new WndOptions( Messages.titleCase(name()), Messages.get(this, "warning"),
				Messages.get(this, "yes"), Messages.get(this, "no") ) {
			@Override
			protected void onSelect( int index ) {
				switch (index) {
				case 0:
					curUser.spendAndNext( TIME_TO_READ );
					identifiedByUse = false;
					break;
				case 1:
					GameScene.selectItem( itemSelector, mode, inventoryTitle );
					break;
				}
			}
			public void onBackPressed() {}
		} );
	}
	
	protected abstract void onItemSelected( Item item );
	
	protected static boolean identifiedByUse = false;
	protected static WndBag.Listener itemSelector = new WndBag.Listener() {
		@Override
		public void onSelect( Item item ) {
			
			//FIXME this safety check shouldn't be necessary
			//it would be better to eliminate the curItem static variable.
			if (!(curItem instanceof InventoryScroll)){
				return;
			}
			
			if (item != null) {
				
				((InventoryScroll)curItem).onItemSelected( item );
				((InventoryScroll)curItem).readAnimation();

				if (curUser.heroClass == HeroClass.RUNEMAGE) {
					MagicStone magicStone = new MagicStone();
					if( !magicStone.quantity(2).collect() ) {
						Dungeon.level.drop( magicStone, curUser.pos );
					}

					if (curUser.subClass == HeroSubClass.RUNEMASTER && getStone) {
//						boolean getStone = false;
						Runestone stone = new StoneOfIntuition();
						if (curItem instanceof ScrollOfIdentify) { stone = new StoneOfIntuition(); }
						if (curItem instanceof ScrollOfRemoveCurse) { stone = new StoneOfDisarming(); }
						if (curItem instanceof ScrollOfTransmutation) { stone = new StoneOfAugmentation(); }
						if (curItem instanceof ScrollOfUpgrade) { stone = new StoneOfEnchantment(); }
						if(!stone.collect()) {
							Dungeon.level.drop( stone, curUser.pos );
						}
					}
				}

				Sample.INSTANCE.play( Assets.SND_READ );
				Invisibility.dispel();
				
			} else if (identifiedByUse && !((Scroll)curItem).anonymous) {
				
				((InventoryScroll)curItem).confirmCancelation();

				if (curUser.heroClass == HeroClass.RUNEMAGE) {
					MagicStone magicStone = new MagicStone();
					if( !magicStone.collect() ) {
						Dungeon.level.drop( magicStone, curUser.pos );
					}

					if (curUser.subClass == HeroSubClass.RUNEMASTER && getStone) {
//						boolean getStone = false;
						Runestone stone = new StoneOfIntuition();
						if (curItem instanceof ScrollOfIdentify) { stone = new StoneOfIntuition(); /*getStone = true;*/}
						if (curItem instanceof ScrollOfRemoveCurse) { stone = new StoneOfDisarming(); /*getStone = true;*/}
						if (curItem instanceof ScrollOfTransmutation) { stone = new StoneOfAugmentation(); /*getStone = true;*/}
						if (curItem instanceof ScrollOfUpgrade) { stone = new StoneOfEnchantment(); /*getStone = true;*/}
						if(!stone.collect()) {
							Dungeon.level.drop( stone, curUser.pos );
						}
					}
				}
				
			} else if (!((Scroll)curItem).anonymous) {
				
				curItem.collect( curUser.belongings.backpack );
				
			}
		}
	};
}
