package com.shatteredpixel.shatteredpixeldungeon.items.artifacts;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Charm;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Light;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LockedFloor;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.FlameParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.PinkGooBlob;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;

import java.util.ArrayList;

public class PinkCandle extends Artifact {

    {
        image = ItemSpriteSheet.ARTIFACT_PINKCANDLE1;

        exp = 0;
        levelCap = 10;

        charge = 0;
        partialCharge = 0;
        chargeCap = 100;
        defaultAction = AC_LIGHT;
    }

    private static final String AC_LIGHT = "LIGHT";
    private static final String AC_CAST = "CAST";
    private static final String AC_BLOW_OUT = "BLOW_OUT";

    private static boolean isLight = false;

    @Override
    public ArrayList<String> actions(Hero hero ) {
        ArrayList<String> actions = super.actions( hero );
        if (isEquipped( hero ) && !cursed) {
            if (charge > 10 + (hero.buff(Light.class) == null ? 5 : 0) && !isLight) {
                actions.add(AC_LIGHT);
            } else if(isLight) {
                actions.add(AC_BLOW_OUT);
            }
            if (charge >= 60)
                actions.add(AC_CAST);
        }

        return actions;
    }

    @Override
    public void execute( Hero hero, String action ) {
        super.execute(hero, action);

        if ( !isEquipped(hero) &&( action.equals(AC_LIGHT) || action.equals(AC_BLOW_OUT) || action.equals(AC_CAST) ) ) {
            GLog.i( Messages.get(Artifact.class, "need_to_equip") );
            return;
        }

        if (cursed) {
            GLog.i( Messages.get(this, "cursed") );
            return;
        }

        if (action.equals(AC_LIGHT)) {
            if (charge <= 10 + (hero.buff(Light.class) == null ? 5 : 0)) {
                GLog.i( Messages.get(this, "no_charge") );
                return;
            }
            doLight();
            if (hero.buff(Light.class) == null) {
                charge -= 5;
            }
            if (hero.buff(Light.class) == null || hero.buff(Light.class).cooldown() < 1f) {
                Buff.prolong(hero, Light.class, 1f);
                Emitter emitter = hero.sprite.centerEmitter();
                emitter.start( FlameParticle.FACTORY, 0.2f, 3 );
            }

            hero.spend(1f);
            hero.busy();
            hero.sprite.operate( hero.pos );

        } else if (action.equals(AC_CAST)) {

            doUnlight(hero);

            float timeOfCharm = 10f + (level() + 5) * (5f + (charge - 80) / 4f) / 6f;
            int cell = hero.pos;
            for (int i : PathFinder.NEIGHBOURS9){

                CellEmitter.center(cell + i).start( Speck.factory( Speck.HEART ), 1.75f, 2 );
                CellEmitter.center(cell + i).start( Speck.factory( Speck.HEART ), 0.2f, 5 );

                Char ch = Actor.findChar( cell + i );

                //when level = 10: charge = 60, time = 10; charge = 80, time = 22.5; charge = 100, time = 35, per charge ~ 0.625s
                //when level = 1: charge = 60, time = 10; charge = 80, time = 15; charge = 100, time = 20, per charge ~ 0.25s

//                Buff.affect(hero, Recharging.class, timeOfCharm);
//                float timeOfCharm = 10f;
                if (ch != null && ch.alignment == Char.Alignment.ENEMY){
                    Buff.prolong(ch, Charm.class, timeOfCharm).object = hero.id();
                }

            }
            charge = 0;
            Sample.INSTANCE.play( Assets.SND_CHARMS );

            hero.spendAndNext(1f);
        } else if (action.equals(AC_BLOW_OUT)) {

            doUnlight(hero);

            hero.spend(1f);
            hero.busy();
            hero.sprite.operate( hero.pos );
        }
        updateQuickslot();
    }

    @Override
    public String desc() {
        String desc = Messages.get(this, "desc");

        if ( isEquipped ( Dungeon.hero ) ){
            desc += "\n\n";

            if (!cursed)
                desc += Messages.get(this, "desc_hint");
            else
                desc += Messages.get(this, "desc_cursed");
            desc += "\n\n";

            if (isLight)
                desc += Messages.get(this, "desc_light");
            else
                desc += Messages.get(this, "desc_unlight");
        }

        return desc;
    }

    private static final String ISLIGHT = "islight";
    private static final String DEFAULTACTION = "defaultaction";

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(ISLIGHT, isLight);
        bundle.put(DEFAULTACTION, defaultAction);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        isLight = bundle.getBoolean(ISLIGHT);
        defaultAction = bundle.getString(DEFAULTACTION);
    }

    @Override
    protected ArtifactBuff passiveBuff() {
        return new candleRecharge();
    }

    @Override
    public void charge(Hero target) {
        if (charge < chargeCap){
            partialCharge += 30f;
            while (partialCharge >= 1 && charge < chargeCap) {
                partialCharge --;
                charge ++;
            }
            if (charge == chargeCap) {
                partialCharge = 0;
                GLog.p( Messages.get(candleRecharge.class, "full_charge") );
            }
            updateImage();
        }
    }

    //the impact to respawn mobs is in Level.respawnTime and RegularLevel.nMobs
    public class candleRecharge extends ArtifactBuff {
        @Override
        public boolean act() {
            if (isLight) {
                charge--;
                if (charge < 10 || cursed) {
                    doUnlight((Hero)target);
                } else if (target.buff(Light.class) == null || target.buff(Light.class).cooldown() < 1f) {
                    Buff.prolong(target, Light.class, 1f);
                    exp++;
                    if (exp >= level() * 30 + 10 && level() < levelCap) {
                        upgrade();
                        GLog.p( Messages.get(this, "levelup") );
                        exp = 0;
                    }
                }
            } else {
                LockedFloor lock = target.buff(LockedFloor.class);
                if (charge < chargeCap && !cursed && (lock == null || lock.regenOn())) {
                    partialCharge += level() / 10f + .1f;

                    while (partialCharge >= 1 && charge < chargeCap) {
                        partialCharge --;
                        charge ++;
                    }

                    if (charge == chargeCap) {
                        partialCharge = 0;
                        GLog.p( Messages.get(this, "full_charge") );
                    }
                }
            }

            updateImage();

            spend( TICK );

            return true;
        }

        public boolean isLight() {
            return isLight;
        }
    }

    @Override
    public boolean doUnequip(Hero hero,boolean collect, boolean single) {
        doUnlight(hero);
        return super.doUnequip( hero, collect, single );
    }

    private void doLight() {
        isLight = true;
        defaultAction = AC_BLOW_OUT;
        updateImage();
    }

    private void doUnlight(Hero hero) {
        isLight = false;
        defaultAction = AC_LIGHT;
        if (hero.buff(Light.class) != null && hero.buff(Light.class).cooldown() <= 1f) {
            Buff.detach(hero, Light.class);
        }
        GLog.w(Messages.get(this, "unlight"));
        updateImage();
    }

    private void updateImage() {
        if (isLight) {
            if (charge < 30) {
                image = ItemSpriteSheet.ARTIFACT_PINKCANDLE2;
            } else if (charge < 60) {
                image = ItemSpriteSheet.ARTIFACT_PINKCANDLE3;
            } else {
                image = ItemSpriteSheet.ARTIFACT_PINKCANDLE4;
            }
        } else {
            image = ItemSpriteSheet.ARTIFACT_PINKCANDLE1;
        }
        updateQuickslot();
    }


    public static class Recipe extends com.shatteredpixel.shatteredpixeldungeon.items.Recipe.SimpleRecipe {

        {
            inputs =  new Class[]{PinkGooBlob.class};
            inQuantity = new int[]{2};

            cost = 4;

            output = PinkCandle.class;
            outQuantity = 1;
        }

    }

}
