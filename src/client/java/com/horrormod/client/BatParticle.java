package com.horrormod.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

/** A little harmless bat from the Bat Potion: flutters along the velocity it was given and fades away. */
public class BatParticle extends SingleQuadParticle {
	BatParticle(final ClientLevel level, final double x, final double y, final double z, final double xd, final double yd, final double zd,
			final TextureAtlasSprite sprite) {
		super(level, x, y, z, 0.0, 0.0, 0.0, sprite);
		this.xd = xd;
		this.yd = yd;
		this.zd = zd;
		this.friction = 0.9F;
		this.gravity = 0.0F;
		this.hasPhysics = false;
		this.lifetime = 14 + this.random.nextInt(6);
		this.quadSize = 0.16F;
	}

	@Override
	public void tick() {
		super.tick();
		this.yd += Math.sin(this.age * 1.3) * 0.01; // flutter
	}

	@Override
	public SingleQuadParticle.Layer getLayer() {
		return SingleQuadParticle.Layer.TRANSLUCENT;
	}

	public static class Provider implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet sprite;

		public Provider(final SpriteSet sprite) {
			this.sprite = sprite;
		}

		@Override
		public Particle createParticle(final SimpleParticleType options, final ClientLevel level, final double x, final double y, final double z,
				final double xd, final double yd, final double zd, final RandomSource random) {
			return new BatParticle(level, x, y, z, xd, yd, zd, this.sprite.get(random));
		}
	}
}
