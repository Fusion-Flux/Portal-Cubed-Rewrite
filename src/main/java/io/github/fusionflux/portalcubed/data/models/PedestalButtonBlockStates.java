package io.github.fusionflux.portalcubed.data.models;

import com.google.gson.JsonPrimitive;

import io.github.fusionflux.portalcubed.content.PortalCubedBlocks;
import io.github.fusionflux.portalcubed.content.button.pedestal.PedestalButtonBlock;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.core.Direction;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.ItemModelGenerators;
import net.minecraft.data.models.blockstates.Condition;
import net.minecraft.data.models.blockstates.MultiPartGenerator;
import net.minecraft.data.models.blockstates.Variant;
import net.minecraft.data.models.blockstates.VariantProperties;
import net.minecraft.data.models.blockstates.VariantProperty;
import net.minecraft.data.models.model.ModelLocationUtils;
import net.minecraft.world.phys.Vec3;

public class PedestalButtonBlockStates extends FabricModelProvider {
	public static final VariantProperty<Float> SHIFT_X = new VariantProperty<>("portalcubed:shift_x", JsonPrimitive::new);
	public static final VariantProperty<Float> SHIFT_Y = new VariantProperty<>("portalcubed:shift_y", JsonPrimitive::new);
	public static final VariantProperty<Float> SHIFT_Z = new VariantProperty<>("portalcubed:shift_z", JsonPrimitive::new);
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
		var centerBaseModelId = ModelLocationUtils.getModelLocation(pedestalButtonBlock, "_base_center");
		var edgeBaseModelId = ModelLocationUtils.getModelLocation(pedestalButtonBlock, "_base_edge");

		var generator = MultiPartGenerator.multiPart(pedestalButtonBlock);
		for (var state : pedestalButtonBlock.getStateDefinition().getPossibleStates()) {
			var face = state.getValue(PedestalButtonBlock.FACE);
			var facing = state.getValue(PedestalButtonBlock.FACING);
			var offset = state.getValue(PedestalButtonBlock.OFFSET);
			boolean base = state.getValue(PedestalButtonBlock.BASE);

			var variant = Variant.variant();
			{
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
			}

			var condition = Condition.condition()
				.term(PedestalButtonBlock.FACE, face)
				.term(PedestalButtonBlock.FACING, facing)
				.term(PedestalButtonBlock.OFFSET, offset)
				.term(PedestalButtonBlock.BASE, base);

			var baseShift = offset.get(face, facing, base);
			if (base)
				generator.with(condition, shiftedVariantCopy(variant, baseShift).with(VariantProperties.MODEL, offset.centered ? centerBaseModelId : edgeBaseModelId));

			var buttonShift = baseShift.add(Vec3.atLowerCornerOf(face.getNormal()));
			for (boolean active : PedestalButtonBlock.ACTIVE.getPossibleValues()) {
				generator.with(
					Condition.and(condition, Condition.condition().term(PedestalButtonBlock.ACTIVE, active)),
					shiftedVariantCopy(variant, base ? buttonShift : baseShift).with(VariantProperties.MODEL, active ? activeModelId : regularModelId)
				);
			}
		}

		blockStateModelGenerator.blockStateOutput.accept(generator);
		blockStateModelGenerator.skipAutoItemBlock(pedestalButtonBlock);
	}

	public static Variant shiftedVariantCopy(Variant variant, Vec3 shift) {
		variant = Variant.merge(variant, Variant.variant());
		if (shift.x() != 0)
			variant.with(SHIFT_X, (float) shift.x());
		if (shift.y() != 0)
			variant.with(SHIFT_Y, (float) shift.y());
		if (shift.z() != 0)
			variant.with(SHIFT_Z, (float) shift.z());
		return variant;
	}

	@Override
	public void generateItemModels(ItemModelGenerators itemModelGenerator) {

	}
}
