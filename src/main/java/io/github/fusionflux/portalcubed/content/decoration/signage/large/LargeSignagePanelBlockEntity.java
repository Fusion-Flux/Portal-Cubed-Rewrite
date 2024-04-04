package io.github.fusionflux.portalcubed.content.decoration.signage.large;

import io.github.fusionflux.portalcubed.content.PortalCubedBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import org.quiltmc.qsl.block.entity.api.QuiltBlockEntity;

public class LargeSignagePanelBlockEntity extends BlockEntity implements QuiltBlockEntity {
	public LargeSignagePanelBlockEntity(BlockPos pos, BlockState state) {
		super(PortalCubedBlockEntityTypes.LARGE_SIGNAGE_PANEL, pos, state);
	}
}
