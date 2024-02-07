package io.github.fusionflux.portalcubed.content.prop;

import io.github.fusionflux.portalcubed.content.button.FloorButtonBlock;
import io.github.fusionflux.portalcubed.data.tags.PortalCubedTags;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;

public class P2CubeProp extends Prop {
	public P2CubeProp(PropType type, EntityType<?> entityType, Level level) {
		super(type, entityType, level);
	}

	public void setActivated(boolean activated) {
		setVariant(((activated ? 1 : 0) << 0) | ((getVariant() >> 1) << 1));
	}

	private boolean setDirty(boolean dirty) {
		boolean isDirty = (getVariant() >> 1) != 0;
		setVariant(getVariant() | ((dirty ? 1 : 0) << 1));
		return !isDirty;
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void checkInsideBlocks() {
		var box = this.getBoundingBox();
		var start = BlockPos.containing(box.minX + 1.0E-7, box.minY + 1.0E-7, box.minZ + 1.0E-7);
		var end = BlockPos.containing(box.maxX - 1.0E-7, box.maxY - 1.0E-7, box.maxZ - 1.0E-7);
		if (level().hasChunksAt(start, end) && isAlive()) {
			boolean buttonChecked = false;
			boolean onButton = false;

			for (var pos : BlockPos.betweenClosed(start, end)) {
				var state = level().getBlockState(pos);

				try {
					if (!buttonChecked && state.getBlock() instanceof FloorButtonBlock button) {
						var originPos = button.getOriginPos(pos, state);
						onButton = button.getButtonBounds(state.getValue(FloorButtonBlock.FACING)).move(originPos).intersects(getBoundingBox());
						buttonChecked = true;
					}
					state.entityInside(this.level(), pos, this);
				} catch (Throwable t) {
					var crashReport = CrashReport.forThrowable(t, "Colliding entity with block");
					var crashReportCategory = crashReport.addCategory("Block being collided with");
					CrashReportCategory.populateBlockDetails(crashReportCategory, level(), pos, state);
					throw new ReportedException(crashReport);
				}
			}

			setActivated(onButton);
		}
	}

	@Override
	public InteractionResult interact(Player player, InteractionHand hand) {
		var itemInHand = player.getItemInHand(hand);
		if (itemInHand.is(PortalCubedTags.Item.AGED_CRAFTING_MATERIALS) && setDirty(true)) {
			var level = level();
			if (!level.isClientSide) {
				level.playSound(null, this, SoundType.VINE.getPlaceSound(), SoundSource.PLAYERS, 1f, .5f);
				var particleOption = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.VINE.defaultBlockState());
				for (var dir : Direction.values()) {
					double x = getX() + (dir.getStepX() * getBbWidth() / 2);
					double y = getY() + (dir.getStepY() * getBbWidth() / 2);
					double z = getZ() + (dir.getStepZ() * getBbWidth() / 2);
					((ServerLevel) level).sendParticles(particleOption, x, y, z, level.random.nextInt(5, 8), 0, 0, 0, 1);
				}
				itemInHand.shrink(1);
				return InteractionResult.SUCCESS;
			} else {
				return InteractionResult.CONSUME;
			}
		}
		return InteractionResult.PASS;
	}
}
