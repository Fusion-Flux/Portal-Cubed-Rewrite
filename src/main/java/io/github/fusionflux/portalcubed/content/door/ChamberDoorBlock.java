package io.github.fusionflux.portalcubed.content.door;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import io.github.fusionflux.portalcubed.PortalCubed;
import io.github.fusionflux.portalcubed.content.PortalCubedSounds;
import io.github.fusionflux.portalcubed.framework.extension.BigShapeBlock;
import io.github.fusionflux.portalcubed.framework.util.VoxelShaper;
import net.fabricmc.fabric.api.object.builder.v1.block.type.BlockSetTypeBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ChamberDoorBlock extends DoorBlock implements BigShapeBlock {
	private static BlockSetType createBlockSetType(String name, SoundEvent openSound, SoundEvent closeSound) {
		return BlockSetTypeBuilder.copyOf(BlockSetType.IRON)
				.doorOpenSound(openSound)
				.doorCloseSound(closeSound)
				.build(PortalCubed.id(name));
	}

	private static final BlockSetType BLOCK_SET_TYPE = createBlockSetType("chamber_door", PortalCubedSounds.CHAMBER_DOOR_OPEN, PortalCubedSounds.CHAMBER_DOOR_CLOSE);
	private static final BlockSetType OLD_AP_BLOCK_SET_TYPE = createBlockSetType("old_ap_chamber_door", PortalCubedSounds.OLD_AP_CHAMBER_DOOR_OPEN, PortalCubedSounds.OLD_AP_CHAMBER_DOOR_CLOSE);
	private static final BlockSetType PORTAL_1_BLOCK_SET_TYPE = createBlockSetType("portal_1_chamber_door", PortalCubedSounds.PORTAL_1_CHAMBER_DOOR_OPEN, PortalCubedSounds.PORTAL_1_CHAMBER_DOOR_CLOSE);

	private static final VoxelShaper SHAPE = VoxelShaper.forHorizontal(box(0, 0, 11, 16, 16, 15), Direction.NORTH);
	private static final Map<DoorHingeSide, VoxelShaper> OPEN_SHAPE = ImmutableMap.of(
			DoorHingeSide.LEFT, VoxelShaper.forHorizontal(box(-12, 0, 11, 4, 16, 15), Direction.NORTH),
			DoorHingeSide.RIGHT, VoxelShaper.forHorizontal(box(12, 0, 11, 28, 16, 15), Direction.NORTH)
	);
	private static final Map<DoorHingeSide, VoxelShaper> OLD_AP_OPEN_SHAPE = ImmutableMap.of(
			DoorHingeSide.LEFT, VoxelShaper.forHorizontal(box(-8, 0, 11, 8, 16, 15), Direction.NORTH),
			DoorHingeSide.RIGHT, VoxelShaper.forHorizontal(box(8, 0, 11, 24, 16, 15), Direction.NORTH)
	);

	protected final VoxelShaper shape;
	protected final Map<DoorHingeSide, VoxelShaper> openShape;

	public ChamberDoorBlock(BlockSetType blockSetType, Properties settings, VoxelShaper shape, Map<DoorHingeSide, VoxelShaper> openShape) {
		super(blockSetType, settings);
		this.shape = shape;
		this.openShape = openShape;
	}

	public ChamberDoorBlock(Properties settings) {
		this(BLOCK_SET_TYPE, settings, SHAPE, OPEN_SHAPE);
	}

	public static ChamberDoorBlock oldAp(Properties settings) {
		return new ChamberDoorBlock(OLD_AP_BLOCK_SET_TYPE, settings, SHAPE, OLD_AP_OPEN_SHAPE);
	}

	public static ChamberDoorBlock p1(Properties settings) {
		return new ChamberDoorBlock(PORTAL_1_BLOCK_SET_TYPE, settings, SHAPE, OPEN_SHAPE);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return (this.isOpen(state) ? this.openShape.get(state.getValue(HINGE)) : this.shape).get(state.getValue(FACING));
	}
}
