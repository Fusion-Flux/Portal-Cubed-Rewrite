package io.github.fusionflux.portalcubed.content.decoration.signage.large;

import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.MapCodec;

import io.github.fusionflux.portalcubed.content.decoration.signage.SignageBlock;
import io.github.fusionflux.portalcubed.packet.PortalCubedPackets;
import io.github.fusionflux.portalcubed.packet.clientbound.OpenSignageConfigPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.phys.BlockHitResult;

public class LargeSignageBlock extends SignageBlock {
	public static final MapCodec<LargeSignageBlock> CODEC = simpleCodec(LargeSignageBlock::new);

	public LargeSignageBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any()
				.setValue(FACE, AttachFace.WALL)
				.setValue(FACING, Direction.NORTH)
				.setValue(WATERLOGGED, false)
		);
	}

	@Override
	@NotNull
	protected MapCodec<LargeSignageBlock> codec() {
		return CODEC;
	}

	@Override
	@NotNull
	public InteractionResult onHammered(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hitResult) {
		if (player instanceof ServerPlayer serverPlayer)
			PortalCubedPackets.sendToClient(serverPlayer, new OpenSignageConfigPacket.Large(pos));
		return InteractionResult.SUCCESS;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new LargeSignageBlockEntity(pos, state);
	}
}
