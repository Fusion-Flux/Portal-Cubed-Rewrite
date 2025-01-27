package io.github.fusionflux.portalcubed.content.prop.entity;

import io.github.fusionflux.portalcubed.content.PortalCubedSounds;
import io.github.fusionflux.portalcubed.content.prop.PropType;
import io.github.fusionflux.portalcubed.framework.util.ColorUtil;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class Taco extends Prop {
	private static final int MIN_CONFETTI_AMOUNT = 10;
	private static final int MAX_CONFETTI_AMOUNT = 30;
	private static final double CONFETTI_RADIUS = 2.3;
	private static final float MIN_EXPLOSION_POWER = 1;
	private static final float MAX_EXPLOSION_POWER = 2;
	private static final double MIN_PUSH_POWER = 3;
	private static final double PUSH_RADIUS = 7;
	private static final AABB PUSH_AABB = AABB.ofSize(Vec3.ZERO, PUSH_RADIUS + 1, PUSH_RADIUS + 1, PUSH_RADIUS + 1);
	public static final float BB_LENGTH = 0.19375f;

	private int explodeTicks;

	public Taco(PropType type, EntityType<?> entityType, Level level) {
		super(type, entityType, level);
	}

	@Override
	protected AABB makeBoundingBox(Vec3 position) {
		float width =  this.getBbWidth() / 2f;
		float height = this.getBbHeight();
		float length = BB_LENGTH / 2f;
		return new AABB(position.x - width, position.y, position.z - length, position.x + width, position.y + height, position.z + length);
	}

	@Override
	public void tick() {
		if (level() instanceof ServerLevel level && isIgnited()) {
			if (--explodeTicks <= 0) {
				Vec3 position = position();
				level.explode(
						this,
						position.x,
						position.y,
						position.z,
						Math.max(this.random.nextFloat() * MAX_EXPLOSION_POWER, MIN_EXPLOSION_POWER),
						false,
						Level.ExplosionInteraction.MOB
				);
				for (Entity entityToPush : level.getEntities(this, PUSH_AABB.move(position))) {
					if (!entityToPush.isInvulnerable() && !entityToPush.isNoGravity()) {
						Vec3 vectorToThis = position.vectorTo(entityToPush.position());
						double dist = Math.max(1, vectorToThis.length());
						if (dist <= PUSH_RADIUS) {
							Vec3 pushDirection = vectorToThis.add(0, dist / PUSH_RADIUS, 0).normalize();
							double pushForce = Math.max((this.random.nextDouble() * PUSH_RADIUS) / (dist / 2), MIN_PUSH_POWER);
							entityToPush.setDeltaMovement(entityToPush.getDeltaMovement().add(pushDirection.scale(pushForce)));
						}
					}
				}

				for (int i = 0; i < this.random.nextInt(MIN_CONFETTI_AMOUNT, MAX_CONFETTI_AMOUNT); i++) {
					Vec3 randomAreaPos = position.add(new Vec3(
							(this.random.nextDouble() * 2 - 1) * CONFETTI_RADIUS,
							(this.random.nextDouble() * 2 - 1) * CONFETTI_RADIUS,
							(this.random.nextDouble() * 2 - 1) * CONFETTI_RADIUS
					));
					ColorUtil.randomConfettiBlock(this.random).ifPresent(confettiBlock -> {
						BlockParticleOption particleOption = new BlockParticleOption(ParticleTypes.BLOCK, confettiBlock.defaultBlockState());
						level.sendParticles(particleOption, randomAreaPos.x, randomAreaPos.y, randomAreaPos.z, 20, 0, 0, 0, 1);
					});
				}
				playSound(PortalCubedSounds.SURPRISE);

				discard();
				return;
			} else {
				setVariant(explodeTicks / 5 % 2 == 0 ? 1 : 0);
			}
		}
		super.tick();
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		this.explodeTicks = tag.getInt("explode_ticks");
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putInt("explode_ticks", this.explodeTicks);
	}

	@Override
	public InteractionResult interact(Player player, InteractionHand hand) {
		ItemStack itemInHand = player.getItemInHand(hand);
		Level level = level();
		if (itemInHand.is(ItemTags.CREEPER_IGNITERS) && !isIgnited()) {
			SoundEvent soundEvent = itemInHand.is(Items.FIRE_CHARGE) ? SoundEvents.FIRECHARGE_USE : SoundEvents.FLINTANDSTEEL_USE;
			level.playSound(player, this.getX(), this.getY(), this.getZ(), soundEvent, this.getSoundSource(), 1.0F, this.random.nextFloat() * 0.4F + 0.8F);
			if (!level.isClientSide) {
				this.ignite();
				if (!itemInHand.isDamageableItem()) {
					itemInHand.shrink(1);
				} else {
					itemInHand.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
				}
			}
			return InteractionResult.SUCCESS;
		}
		return super.interact(player, hand);
	}

	public boolean isIgnited() {
		return this.explodeTicks != 0;
	}

	public void ignite() {
		this.explodeTicks = 5 * 20;
	}
}
