package com.shatteredpixel.shatteredpixeldungeon.actors.blobs;

import com.shatteredpixel.shatteredpixeldungeon.sprites.PinkGooSprite;

public class PinkGooWarn extends GooWarn {

    @Override
    void emitterPour() {
        emitter.pour(PinkGooSprite.PinkGooParticle.FACTORY, 0.03f );
    }
}
