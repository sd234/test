package com.shatteredpixel.shatteredpixeldungeon.items.armor;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.Runestone;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfAggression;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfBlink;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfIntuition;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.BArray;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.PathFinder;

public class RunemageArmor extends ClassArmor {

    private static int CAST_TIME	= 1;

    {
        image = ItemSpriteSheet.ARMOR_RUNEMAGE;
    }
    @Override
    public void doSpecial() {
        GameScene.selectCell( leaper );
    }

    protected static CellSelector.Listener leaper = new  CellSelector.Listener() {

        @Override
        public void onSelect( Integer target ) {
            if (target != null) {

                curUser.HP -= (curUser.HP / 3);

                Ballistica route = new Ballistica(curUser.pos, target, Ballistica.PROJECTILE);
                int cell = route.collisionPos;

                Runestone runestone;// = (Runestone) Generator.random(Generator.Category.STONE);
                Runestone runestone1 = (Runestone) Generator.random(Generator.Category.STONE)
                , runestone2 = runestone1, runestone3 = runestone1;
                boolean hadBlink = false;
                for (int i = 1; i <= 3; ++i) {
                    do {
                        runestone = (Runestone) Generator.random(Generator.Category.STONE);
                    }
                    while (runestone instanceof StoneOfIntuition
                            || (runestone instanceof StoneOfAggression && Actor.findChar(cell) == null)
                            || (runestone instanceof StoneOfBlink && hadBlink));
//                    GLog.i(Messages.get(RunemageArmor.class, "stone", runestone));

//                    DeviceCompat.log( "GAME", Messages.get(RunemageArmor.class, "stone", runestone) + (i < 3 ? "\n" : ""));
//                    if (i < 3)  GLog.i("\n");
                    if (runestone instanceof StoneOfBlink) {
                        PathFinder.buildDistanceMap(target, BArray.or(Dungeon.level.passable, Dungeon.level.avoid, null));
                        if (PathFinder.distance[curUser.pos] == Integer.MAX_VALUE
                                || !curUser.fieldOfView[target]
                                || (!Dungeon.level.passable[target] && !Dungeon.level.avoid[target])
                                || Actor.findChar(target) != null) {
                            GLog.w( Messages.get(ScrollOfTeleportation.class, "cant_reach") );
                        } else {
                            int prepos = curUser.pos;
                            ScrollOfTeleportation.teleportToLocation(curUser, target);
                            if (curUser.subClass == HeroSubClass.RUNEMASTER) {
                                for (int j = 0; j < PathFinder.NEIGHBOURS8.length; j++) {
                                    Char mob = Actor.findChar(prepos + PathFinder.NEIGHBOURS8[j]);
                                    if (mob != null && mob != curUser && mob.alignment != Char.Alignment.ALLY) {
                                        Buff.prolong(mob, Blindness.class, 5f);
                                    }
                                    Char mob2 = Actor.findChar(curUser.pos + PathFinder.NEIGHBOURS8[j]);
                                    if (mob2 != null && mob2 != curUser && mob2.alignment != Char.Alignment.ALLY) {
                                        Buff.prolong(mob2, Paralysis.class, 3f);
                                    }
                                }
                            }
                        }
                        hadBlink = true;
                        switch (i) {
                            case 1:
                                runestone1 = runestone;
                                break;
                            case 2:
                                runestone2 = runestone;
                                break;
                            case 3:
                                runestone3 = runestone;
                                break;
                        }

                        continue;
                    }
                    runestone.activate(cell);
                    switch (i) {
                        case 1:
                            runestone1 = runestone;
                            break;
                        case 2:
                            runestone2 = runestone;
                            break;
                        case 3:
                            runestone3 = runestone;
                            break;
                    }
                }

                GLog.i(Messages.get(RunemageArmor.class, "stone", runestone1, runestone2, runestone3));
                //CellEmitter.center(cell).burst(Speck.factory(Speck.DUST), 10);
                //Camera.main.shake(2, 0.5f);

                curUser.spendAndNext(CAST_TIME);
            }
        }

        @Override
        public String prompt() {
            return Messages.get(RunemageArmor.class, "prompt");
        }
    };
}
