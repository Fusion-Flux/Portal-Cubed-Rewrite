package io.github.fusionflux.portalcubed.data.models;

import io.github.fusionflux.portalcubed.content.PortalCubedBlocks;
import io.github.fusionflux.portalcubed.content.button.FloorButtonBlock;
import io.github.fusionflux.portalcubed.framework.block.multiblock.AbstractMultiBlock;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.core.Direction;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.ItemModelGenerators;
import net.minecraft.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.data.models.blockstates.PropertyDispatch;
import net.minecraft.data.models.blockstates.Variant;
import net.minecraft.data.models.blockstates.VariantProperties;
import net.minecraft.data.models.model.ModelLocationUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/**
 * generated files:
 * assets\portalcubed\blockstates\old_ap_floor_button.json
 * assets\portalcubed\blockstates\floor_button.json
 */
public class FloorButtonBlockStates extends FabricModelProvider {
	public static final String[][] QUADRANT_LOOKUP = new String[][]{
			new String[]{"bottom_left", "bottom_right"},
			new String[]{"top_left", "top_right"}
	};

	public FloorButtonBlockStates(FabricDataOutput output) {
		super(output);
	}

	public static Variant variantForMultiBlockDirection(Direction direction) {
		return switch (direction) {
			case DOWN ->
					Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R180).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270);
			case UP -> Variant.variant();
			case NORTH -> Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R90);
			case SOUTH -> Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R270);
			case WEST ->
					Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R90).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270);
			case EAST ->
					Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R90).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90);
		};
	}

	@Override
	public void generateBlockStateModels(BlockModelGenerators blockStateModelGenerator) {
		createFloorButton(blockStateModelGenerator, PortalCubedBlocks.FLOOR_BUTTON_BLOCK);
		createFloorButton(blockStateModelGenerator, PortalCubedBlocks.OLD_AP_FLOOR_BUTTON_BLOCK);
		createFloorButton(blockStateModelGenerator, PortalCubedBlocks.PORTAL_1_FLOOR_BUTTON_BLOCK);
	}

	@Override
	public void generateItemModels(ItemModelGenerators itemModelGenerator) {

	}

	private void createFloorButton(BlockModelGenerators blockStateModelGenerator, FloorButtonBlock floorButtonBlock) {
		ResourceLocation regularModelId = ModelLocationUtils.getModelLocation(floorButtonBlock);
		ResourceLocation activeModelId = ModelLocationUtils.getModelLocation(floorButtonBlock, "_active");

		AbstractMultiBlock.SizeProperties sizeProperties = floorButtonBlock.sizeProperties();
		IntegerProperty xProp = sizeProperties.x().orElse(null);
		IntegerProperty yProp = sizeProperties.y().orElse(null);

		blockStateModelGenerator.blockStateOutput.accept(
				MultiVariantGenerator.multiVariant(floorButtonBlock)
						.with(
								PropertyDispatch.properties(FloorButtonBlock.ACTIVE, BlockStateProperties.FACING, xProp, yProp).generate(
										(active, facing, x, y) -> {
											String quadrantName = switch (facing) {
												case SOUTH -> QUADRANT_LOOKUP[y == 1 ? 0 : 1][x];
												case WEST -> QUADRANT_LOOKUP[y][x == 1 ? 0 : 1];
												case DOWN -> QUADRANT_LOOKUP[x == 1 ? 0 : 1][y == 1 ? 0 : 1];
												default -> QUADRANT_LOOKUP[y][x];
											};
											ResourceLocation modelId = (active ? activeModelId : regularModelId).withSuffix("_" + quadrantName);
											return variantForMultiBlockDirection(facing).with(VariantProperties.MODEL, modelId);
										}
								)
						)
		);
		blockStateModelGenerator.skipAutoItemBlock(floorButtonBlock);
	}
}
