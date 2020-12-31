package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.PinkGooWarn;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Charm;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Ooze;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.PinkElmoParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.PinkGooBlob;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.PinkGooSprite;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class PinkGoo extends Goo {
    {
        spriteClass = PinkGooSprite.class;
    }

    @Override
    void doGooSprite(String string, boolean on) {
        switch (string) {
            case SPRAY:
                ((PinkGooSprite)sprite).spray(on);
                break;
            case PUMPUP:
                ((PinkGooSprite)sprite).pumpUp();
                break;
            case PUMPATTACK:
                ((PinkGooSprite) sprite).pumpAttack();
                break;
        }
    }

    void warn(int i) {
        GameScene.add(Blob.seed(i, 2, PinkGooWarn.class));
    }

    @Override
    void burstParticle(int i) {
        CellEmitter.get(i).burst(PinkElmoParticle.FACTORY, 10);
    }

    @Override
    void gooProc(Char enemy) {
        Buff.affect( enemy, Ooze.class ).set( 20f );
        Buff.prolong( enemy, Charm.class, 20f).object = id();
    }

    @Override
    void gooDrop(int blobs) {
        for (int i = 0; i < blobs; i++){
            int ofs;
            do {
                ofs = PathFinder.NEIGHBOURS8[Random.Int(8)];
            } while (!Dungeon.level.passable[pos + ofs]);
            Dungeon.level.drop( new PinkGooBlob(), pos + ofs ).sprite.drop( pos );
        }
    }

}
