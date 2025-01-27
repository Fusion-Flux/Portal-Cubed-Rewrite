package io.github.fusionflux.portalcubed.content.door;

import java.util.Locale;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import io.github.fusionflux.portalcubed.content.PortalCubedSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;

public class LockingChamberDoorBlock extends ChamberDoorBlock {
	public static final EnumProperty<State> STATE = EnumProperty.create("state", State.class);
	public static final int UNLOCK_DELAY = 5;

	public LockingChamberDoorBlock(Properties settings) {
		super(settings);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		// Can't call super because we have to replace the open property with the state property
		builder.add(HALF, FACING, STATE, HINGE, POWERED);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
		return InteractionResult.PASS;
	}

	@Override
	public boolean isOpen(BlockState state) {
		return state.getValue(STATE).toBoolean().orElse(false);
	}

	@Override
	public void setOpen(@Nullable Entity entity, Level world, BlockState state, BlockPos pos, boolean open) {
		if (state.is(this) && !this.isOpen(state)) {
			world.setBlock(pos, state.setValue(STATE, open ? State.OPEN : State.CLOSED), Block.UPDATE_CLIENTS);
			world.gameEvent(entity, open ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pos);
		}
	}

	private void playSound(Level world, BlockPos pos, SoundEvent sound) {
		world.playSound(null, pos, sound, SoundSource.BLOCKS, 1f, 1f);
	}

	/*
	closed (no sound) -> unlocked (unlock sound) -> open (open sound)
	open (no sound) -> unlocked (close sound) -> closed (lock sound)
	 */
	@Override
	public void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
		if (state.getValue(STATE) == State.UNLOCKED) {
			if (state.getValue(POWERED)) {
				this.setOpen(null, world, state, pos, true);
				this.playSound(world, pos, this.type().doorOpen());
			} else {
				this.setOpen(null, world, state, pos, false);
				this.playSound(world, pos, PortalCubedSounds.CHAMBER_DOOR_LOCK);
			}
		}
	}

	@Override
	protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, @Nullable Orientation orientation, boolean movedByPiston) {
		BlockPos powerPos = state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos.above() : pos.below();
		boolean powered = level.hasNeighborSignal(pos) || level.hasNeighborSignal(powerPos);
		if (neighborBlock != this && state.getValue(POWERED) != powered) {
			state.getValue(STATE).toBoolean().ifPresent(open -> {
				this.playSound(level, pos, open ? PortalCubedSounds.CHAMBER_DOOR_CLOSE : PortalCubedSounds.CHAMBER_DOOR_UNLOCK);
				level.gameEvent(null, open ? GameEvent.BLOCK_CLOSE : GameEvent.BLOCK_OPEN, pos);
			});

			level.setBlock(pos, state.setValue(POWERED, powered).setValue(STATE, State.UNLOCKED), Block.UPDATE_CLIENTS);
			level.scheduleTick(pos, this, UNLOCK_DELAY);
		}
	}


	public enum State implements StringRepresentable {
		CLOSED, UNLOCKED, OPEN;

		public final String name = this.name().toLowerCase(Locale.ROOT);

		@Override
		public String getSerializedName() {
			return this.name;
		}

		public Optional<Boolean> toBoolean() {
			return this == UNLOCKED ? Optional.empty() : Optional.of(this == OPEN);
		}
	}
}
