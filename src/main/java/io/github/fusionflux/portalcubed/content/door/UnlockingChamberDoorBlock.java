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
import net.minecraft.world.InteractionHand;
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
import net.minecraft.world.phys.BlockHitResult;

public class UnlockingChamberDoorBlock extends ChamberDoorBlock {
	public static final EnumProperty<State> STATE = EnumProperty.create("state", State.class);

	public UnlockingChamberDoorBlock(Properties settings) {
		super(settings);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(HALF, FACING, STATE, HINGE, POWERED);
	}

	@Override
	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
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
	@SuppressWarnings("deprecation")
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
	public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
		boolean powered = world.hasNeighborSignal(pos) || world.hasNeighborSignal(state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos.above() : pos.below());
		if (!this.defaultBlockState().is(block) && powered != state.getValue(POWERED)) {
			state.getValue(STATE).toBoolean().ifPresent(open -> {
				this.playSound(world, pos, open ? PortalCubedSounds.CHAMBER_DOOR_CLOSE : PortalCubedSounds.CHAMBER_DOOR_UNLOCK);
				world.gameEvent(null, open ? GameEvent.BLOCK_CLOSE : GameEvent.BLOCK_OPEN, pos);
			});

			world.setBlock(pos, state.setValue(POWERED, powered).setValue(STATE, State.UNLOCKED), Block.UPDATE_CLIENTS);
			world.scheduleTick(pos, this, 5);
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
