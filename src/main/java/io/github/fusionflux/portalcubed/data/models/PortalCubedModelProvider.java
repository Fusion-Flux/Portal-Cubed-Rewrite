package io.github.fusionflux.portalcubed.data.models;

import io.github.fusionflux.portalcubed.content.PortalCubedBlocks;
import io.github.fusionflux.portalcubed.content.button.FloorButtonBlock;
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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class PortalCubedModelProvider extends FabricModelProvider {
	public static final String[][] QUADRANT_LOOKUP = new String[][]{
		new String[]{"bottom_left", "bottom_right"},
		new String[]{"top_left", "top_right"}
	};

	public PortalCubedModelProvider(FabricDataOutput output) {
		super(output);
	}

	@Override
	public void generateBlockStateModels(BlockModelGenerators blockStateModelGenerator) {
		createFloorButton(blockStateModelGenerator, PortalCubedBlocks.FLOOR_BUTTON_BLOCK);
	}

	@Override
	public void generateItemModels(ItemModelGenerators itemModelGenerator) {

	}

	private void createFloorButton(BlockModelGenerators blockStateModelGenerator, FloorButtonBlock floorButtonBlock) {
		var regularModelId = ModelLocationUtils.getModelLocation(floorButtonBlock);
		var activeModelId = ModelLocationUtils.getModelLocation(floorButtonBlock, "_active");

		var sizeProperties = floorButtonBlock.sizeProperties();
		var xProp = sizeProperties.x().get();
		var yProp = sizeProperties.y().get();

		blockStateModelGenerator.blockStateOutput.accept(
			MultiVariantGenerator.multiVariant(floorButtonBlock)
				.with(
					PropertyDispatch.properties(FloorButtonBlock.ACTIVE, xProp, yProp).generate(
						(active, x, y) -> {
							var modelId = (active ? activeModelId : regularModelId)
								.withSuffix("_" + QUADRANT_LOOKUP[y][x]);
							return new Variant().with(VariantProperties.MODEL, modelId);
						}
					)
				).with(multiBlockFacingDispatch())
		);
	}

	public static PropertyDispatch multiBlockFacingDispatch() {
		return PropertyDispatch.property(BlockStateProperties.FACING)
			.select(Direction.DOWN, Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R90))
			.select(Direction.UP, Variant.variant().with(VariantProperties.X_ROT, VariantProperties.Rotation.R270))
			.select(Direction.NORTH, Variant.variant())
			.select(Direction.SOUTH, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
			.select(Direction.WEST, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270))
			.select(Direction.EAST, Variant.variant().with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90));
	}
}
