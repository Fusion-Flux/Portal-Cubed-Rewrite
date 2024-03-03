package io.github.fusionflux.portalcubed.data.models;

import com.google.gson.JsonPrimitive;

import io.github.fusionflux.portalcubed.content.PortalCubedBlocks;
import io.github.fusionflux.portalcubed.content.button.PedestalButtonBlock;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.core.Direction;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.ItemModelGenerators;
import net.minecraft.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.data.models.blockstates.PropertyDispatch;
import net.minecraft.data.models.blockstates.Variant;
import net.minecraft.data.models.blockstates.VariantProperties;
import net.minecraft.data.models.blockstates.VariantProperty;
import net.minecraft.data.models.model.ModelLocationUtils;

public class PedestalButtonBlockStates extends FabricModelProvider {
	public static final VariantProperty<Integer> SHIFT_X = new VariantProperty<>("portalcubed:shift_x", JsonPrimitive::new);
	public static final VariantProperty<Integer> SHIFT_Y = new VariantProperty<>("portalcubed:shift_y", JsonPrimitive::new);
	public static final VariantProperty<Integer> SHIFT_Z = new VariantProperty<>("portalcubed:shift_z", JsonPrimitive::new);
	public static final VariantProperty<Integer> Z_ROT = new VariantProperty<>("portalcubed:rot_z", JsonPrimitive::new);
	public static final VariantProperty<Boolean> LOCAL_Z_ROT = new VariantProperty<>("portalcubed:local_rot_z", JsonPrimitive::new);

	public PedestalButtonBlockStates(FabricDataOutput output) {
		super(output);
	}

	@Override
	public void generateBlockStateModels(BlockModelGenerators blockStateModelGenerator) {
		createPedestalButton(blockStateModelGenerator, PortalCubedBlocks.PEDESTAL_BUTTON);
		createPedestalButton(blockStateModelGenerator, PortalCubedBlocks.OLD_AP_PEDESTAL_BUTTON);
	}

	public void createPedestalButton(BlockModelGenerators blockStateModelGenerator, PedestalButtonBlock pedestalButtonBlock) {
		var regularModelId = ModelLocationUtils.getModelLocation(pedestalButtonBlock);
		var activeModelId = ModelLocationUtils.getModelLocation(pedestalButtonBlock, "_active");

		blockStateModelGenerator.blockStateOutput.accept(
			MultiVariantGenerator.multiVariant(pedestalButtonBlock)
				.with(
					PropertyDispatch.properties(PedestalButtonBlock.FACE, PedestalButtonBlock.FACING, PedestalButtonBlock.OFFSET, PedestalButtonBlock.ACTIVE).generate(
						(face, facing, offset, active) -> {
							var variant = Variant.variant();

							var faceAxis = face.getAxis();
							boolean flip = face.getAxisDirection() == Direction.AxisDirection.NEGATIVE;
							variant = switch (faceAxis) {
								case X -> switch (facing) {
									case SOUTH -> flip ? variant.with(Z_ROT, 90).with(VariantProperties.X_ROT, VariantProperties.Rotation.R270).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180) :
														 variant.with(Z_ROT, 90).with(VariantProperties.X_ROT, VariantProperties.Rotation.R270);
									case WEST -> flip ?  variant.with(Z_ROT, 90).with(VariantProperties.X_ROT, VariantProperties.Rotation.R180).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180) :
														 variant.with(Z_ROT, 90);
									case EAST -> flip ?  variant.with(Z_ROT, 90).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180) :
														 variant.with(Z_ROT, 90).with(VariantProperties.X_ROT, VariantProperties.Rotation.R180);
									default -> flip ?	 variant.with(Z_ROT, 90).with(VariantProperties.X_ROT, VariantProperties.Rotation.R90).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180) :
														 variant.with(Z_ROT, 90).with(VariantProperties.X_ROT, VariantProperties.Rotation.R90);
								};
								case Y -> switch (facing) {
									case SOUTH -> flip ? variant.with(VariantProperties.X_ROT, VariantProperties.Rotation.R180) :
														 variant.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180);
									case WEST -> flip ?  variant.with(VariantProperties.X_ROT, VariantProperties.Rotation.R180).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90) :
														 variant.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270);
									case EAST -> flip ?  variant.with(VariantProperties.X_ROT, VariantProperties.Rotation.R180).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270) :
														 variant.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90);
									default -> flip ?	 variant.with(VariantProperties.X_ROT, VariantProperties.Rotation.R180).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180) :
														 variant;
								};
								case Z -> switch (facing) {
									case SOUTH -> flip ? variant.with(VariantProperties.X_ROT, VariantProperties.Rotation.R90).with(Z_ROT, 180) :
														 variant.with(VariantProperties.X_ROT, VariantProperties.Rotation.R90).with(Z_ROT, 180).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180) ;
									case WEST -> flip ?  variant.with(VariantProperties.X_ROT, VariantProperties.Rotation.R90).with(Z_ROT, 90) :
														 variant.with(VariantProperties.X_ROT, VariantProperties.Rotation.R90).with(Z_ROT, 90).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180);
									case EAST -> flip ?  variant.with(VariantProperties.X_ROT, VariantProperties.Rotation.R90).with(Z_ROT, 270) :
														 variant.with(VariantProperties.X_ROT, VariantProperties.Rotation.R90).with(Z_ROT, 270).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180);
									default -> flip ?	 variant.with(VariantProperties.X_ROT, VariantProperties.Rotation.R90) :
														 variant.with(VariantProperties.X_ROT, VariantProperties.Rotation.R90).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180);
								};
							};
							if (faceAxis == Direction.Axis.Z) variant.with(LOCAL_Z_ROT, true);

							var shift = offset.relative(face, facing);
							if (shift.getX() != 0)
								variant.with(SHIFT_X, shift.getX());
							if (shift.getY() != 0)
								variant.with(SHIFT_Y, shift.getY());
							if (shift.getZ() != 0)
								variant.with(SHIFT_Z, shift.getZ());

							return variant.with(VariantProperties.MODEL, active ? activeModelId : regularModelId);
						}
					)
				)
		);
		blockStateModelGenerator.skipAutoItemBlock(pedestalButtonBlock);
	}

	@Override
	public void generateItemModels(ItemModelGenerators itemModelGenerator) {

	}
}
