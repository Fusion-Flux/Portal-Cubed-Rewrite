package io.github.fusionflux.portalcubed.framework.construct;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import io.github.fusionflux.portalcubed.content.cannon.ConstructionCannonItem;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEvent.Context;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A construct that has been configured with a rotation and offset, ready for placement.
 */
public class ConfiguredConstruct {
	public final Map<BlockPos, Construct.BlockInfo> blocks;
	public final Rotation rotation;
	public final BoundingBox bounds;
	public final Vec3i offset;

	public ConfiguredConstruct(Construct construct) {
		this(construct, Rotation.NONE, Vec3i.ZERO);
	}

	public ConfiguredConstruct(Construct construct, Rotation rotation, Vec3i offset) {
		this.blocks = construct.getBlocks(rotation);
		this.rotation = rotation;
		this.bounds = construct.getBounds(rotation);
		this.offset = offset;
	}

	public BoundingBox getAbsoluteBounds(BlockPos pos) {
		return this.bounds.moved(
				pos.getX() + this.offset.getX(),
				pos.getY() + this.offset.getY(),
				pos.getZ() + this.offset.getZ()
		);
	}

	public void place(ServerLevel level, BlockPos pos, @Nullable Player player, @Nullable ItemStack cannonStack) {
		boolean dropReplacedBlocks = player == null || !player.getAbilities().instabuild;
		this.getAbsoluteBlocks(pos).forEach((blockPos, info) -> {
			BlockState state = info.state();
			level.destroyBlock(blockPos, dropReplacedBlocks, player);
			level.setBlock(blockPos, state, Block.UPDATE_ALL_IMMEDIATE);
			info.maybeNbt().ifPresent(nbt -> {
				BlockEntity be = level.getBlockEntity(blockPos);
				if (be != null) {
					be.load(nbt);
				}
			});

			// post-processing
			if (cannonStack != null) {
				state.getBlock().setPlacedBy(level, blockPos, state, player, cannonStack);
				if (player instanceof ServerPlayer serverPlayer) {
					CriteriaTriggers.PLACED_BLOCK.trigger(serverPlayer, blockPos, cannonStack);
				}
			}
			// sounds
			SoundType soundType = state.getSoundType();
			level.playSound(
					null, blockPos, soundType.getPlaceSound(), SoundSource.BLOCKS,
					(soundType.getVolume() + 1) / 2f, soundType.getPitch() * 0.8f
			);
			level.gameEvent(GameEvent.BLOCK_PLACE, blockPos, Context.of(player, state));
		});
	}

	public boolean isObstructed(Level level, BlockPos pos, boolean replaceMode) {
		for (Map.Entry<BlockPos, Construct.BlockInfo> entry : this.getAbsoluteBlocks(pos).entrySet()) {
			BlockPos blockPos = entry.getKey();
			BlockState existingState = level.getBlockState(blockPos);
			if (ConstructionCannonItem.cantBeReplaced(existingState, replaceMode))
				return true;

			// check entity collision
			BlockState state = entry.getValue().state();
			VoxelShape shape = state.getCollisionShape(level, blockPos);
			shape = shape.move(blockPos.getX(), blockPos.getY(), blockPos.getZ());
			if (!level.isUnobstructed(null, shape)) {
				return true;
			}
		}
		return false;
	}

	private Map<BlockPos, Construct.BlockInfo> getAbsoluteBlocks(BlockPos pos) {
		Map<BlockPos, Construct.BlockInfo> map = new HashMap<>();

		BlockPos origin = pos.offset(this.offset);
		this.blocks.forEach((relativePos, info) -> {
			BlockPos absolute = origin.offset(relativePos);
			map.put(absolute, info);
		});

		return map;
	}
}
