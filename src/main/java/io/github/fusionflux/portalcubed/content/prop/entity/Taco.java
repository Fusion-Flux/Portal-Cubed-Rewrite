package io.github.fusionflux.portalcubed.content.prop.entity;

import io.github.fusionflux.portalcubed.content.prop.PropType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class Taco extends Prop {
	private static final EntityDataAccessor<Boolean> DATA_IS_IGNITED = SynchedEntityData.defineId(Taco.class, EntityDataSerializers.BOOLEAN);

	private int explodeTicks = 0;

	public Taco(PropType type, EntityType<?> entityType, Level level) {
		super(type, entityType, level);
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();

		this.entityData.define(DATA_IS_IGNITED, false);
	}

	@Override
	public void tick() {
		Level level = level();
		if (!level.isClientSide && isIgnited()) {
			if (--explodeTicks == 0) {
				level.explode(this, this.getX(), this.getY(), this.getZ(), this.random.nextInt(10, 20), true, Level.ExplosionInteraction.MOB);
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

		if (tag.getBoolean("ignited")) this.ignite();
		this.explodeTicks = tag.getInt("explode_ticks");
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);

		tag.putBoolean("ignited", this.isIgnited());
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
					itemInHand.hurtAndBreak(1, player, $ -> player.broadcastBreakEvent(hand));
				}
			}
			return InteractionResult.sidedSuccess(level.isClientSide);
		}
		return super.interact(player, hand);
	}

	public boolean isIgnited() {
		return this.entityData.get(DATA_IS_IGNITED);
	}

	public void ignite() {
		this.entityData.set(DATA_IS_IGNITED, true);
		this.explodeTicks = 5 * 20;
	}
}
