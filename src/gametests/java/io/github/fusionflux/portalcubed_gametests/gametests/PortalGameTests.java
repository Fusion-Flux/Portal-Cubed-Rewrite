package io.github.fusionflux.portalcubed_gametests.gametests;

import io.github.fusionflux.portalcubed_gametests.PortalHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.minecraft.world.phys.Vec3;
import org.quiltmc.qsl.testing.api.game.QuiltGameTest;
import io.github.fusionflux.portalcubed_gametests.PortalCubedGameTests;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

public class PortalGameTests implements QuiltGameTest {
	private static final String GROUP = PortalCubedGameTests.ID + ":portals/";

	//Test entities being dropped into portals and maintaining momentum
	@GameTest(template = GROUP + "drop_entity_into_portal")
	public void dropEntityIntoPortal(GameTestHelper helper) {

		PortalHelper armorStandPair = new PortalHelper(helper, "armor_stand", 0x2055fe, 0xfe7020);
		PortalHelper cubePair = new PortalHelper(helper, "cube", 0xfe2020, 0xfeed20);

		armorStandPair.primary().placeOn(new BlockPos(10, 1, 1), Direction.UP, -90);
		cubePair.primary().placeOn(new BlockPos(10, 1, 3), Direction.UP, -90);
		armorStandPair.secondary().placeOn(new BlockPos(7, 3, 1), Direction.WEST);
		cubePair.secondary().placeOn(new BlockPos(7, 3, 3), Direction.WEST);

		helper.setBlock(9, 4, 1, Blocks.AIR);
		helper.setBlock(9, 4, 3, Blocks.AIR);

		helper.succeedWhen(() -> {
			helper.assertBlockProperty(new BlockPos(7, 2, 1), RedstoneLampBlock.LIT, true);
			helper.assertBlockProperty(new BlockPos(7, 2, 3), RedstoneLampBlock.LIT, true);
		});
	}

	//Test entities being dropped into portals at varying heights and landing at the correct distances
	@GameTest(template = GROUP + "fling_distance_relative_to_height")
	public void flingDistanceRelativeToHeight(GameTestHelper helper) {

		PortalHelper pairOne = new PortalHelper(helper, "one", 0x2055fe, 0xfe7020);
		PortalHelper pairTwo = new PortalHelper(helper, "two", 0xfe2020, 0xfeed20);
		PortalHelper pairThree = new PortalHelper(helper, "three", 0x20abfe, 0x9120fe);
		PortalHelper pairFour = new PortalHelper(helper, "four", 0x34fe20, 0xfe20fa);
		PortalHelper pairFive = new PortalHelper(helper, "five", 0xffffff, 0x000000);
		PortalHelper pairSix = new PortalHelper(helper, "six", 0x2f8f8e, 0xff74e4);

		pairOne.primary().placeOn(new BlockPos(22, 1, 1), Direction.UP, -90);
		pairTwo.primary().placeOn(new BlockPos(22, 1, 3), Direction.UP, -90);
		pairThree.primary().placeOn(new BlockPos(22, 1, 5), Direction.UP, -90);
		pairFour.primary().placeOn(new BlockPos(22, 1, 7), Direction.UP, -90);
		pairFive.primary().placeOn(new BlockPos(22, 1, 9), Direction.UP, -90);
		pairSix.primary().placeOn(new BlockPos(22, 1, 11), Direction.UP, -90);

		pairOne.secondary().placeOn(new BlockPos(18, 7, 1), Direction.WEST);
		pairTwo.secondary().placeOn(new BlockPos(18, 7, 3), Direction.WEST);
		pairThree.secondary().placeOn(new BlockPos(18, 7, 5), Direction.WEST);
		pairFour.secondary().placeOn(new BlockPos(18, 7, 7), Direction.WEST);
		pairFive.secondary().placeOn(new BlockPos(18, 7, 9), Direction.WEST);
		pairSix.secondary().placeOn(new BlockPos(18, 7, 11), Direction.WEST);

		helper.setBlock(21, 3, 1, Blocks.AIR);
		helper.setBlock(21, 6, 3, Blocks.AIR);
		helper.setBlock(21, 9, 5, Blocks.AIR);
		helper.setBlock(21, 12, 7, Blocks.AIR);
		helper.setBlock(21, 15, 9, Blocks.AIR);
		helper.setBlock(21, 18, 11, Blocks.AIR);

		helper.succeedWhen(() -> {
			helper.assertBlockProperty(new BlockPos(18, 2, 1), RedstoneLampBlock.LIT, true);
			helper.assertBlockProperty(new BlockPos(18, 2, 3), RedstoneLampBlock.LIT, true);
			helper.assertBlockProperty(new BlockPos(18, 2, 5), RedstoneLampBlock.LIT, true);
			helper.assertBlockProperty(new BlockPos(18, 2, 7), RedstoneLampBlock.LIT, true);
			helper.assertBlockProperty(new BlockPos(18, 2, 9), RedstoneLampBlock.LIT, true);
			helper.assertBlockProperty(new BlockPos(18, 2, 11), RedstoneLampBlock.LIT, true);
		});
	}

	//Test infinite falling to make sure the entity doesn't leave the portal if left falling forever
	@GameTest(template = GROUP + "infinite_fall")
	public void infiniteFall(GameTestHelper helper) {

		PortalHelper infiniteFall = new PortalHelper(helper, "infinite_fall", 0x2055fe, 0xfe7020);

		helper.setBlock(3, 6, 2, Blocks.AIR);
		infiniteFall.primary().placeOn(new BlockPos(3, 4, 2), Direction.UP, -90);
		infiniteFall.secondary().placeOn(new BlockPos(3, 10, 2), Direction.DOWN, 90);


		helper.runAfterDelay(99, () -> {
					helper.succeedWhen(() -> {
						helper.assertEntityPresent(EntityType.ARMOR_STAND);
						helper.assertBlockProperty(new BlockPos(0, 5, 0), RedstoneLampBlock.LIT, false);
					});
				});
	}

	//Test weird portal surface shapes to make sure the entity traveling through doesn't behave weirdly. Split into vertical and horizontal tests
	@GameTest(template = GROUP + "odd_portal_surfaces_vertical")
	public void oddPortalSurfacesVertical(GameTestHelper helper) {

		PortalHelper daylightSensor = new PortalHelper(helper, "daylight_sensor", 0x2055fe, 0xfe7020);
		PortalHelper slab = new PortalHelper(helper, "slab", 0x2055fe, 0xfe7020);
		PortalHelper stairsIn = new PortalHelper(helper, "stairs_in", 0x2055fe, 0xfe7020);
		PortalHelper stairsOut = new PortalHelper(helper, "stairs_out", 0x2055fe, 0xfe7020);
		PortalHelper carpet = new PortalHelper(helper, "carpet", 0x2055fe, 0xfe7020);
		PortalHelper stonecutter = new PortalHelper(helper, "stonecutter", 0x2055fe, 0xfe7020);
		PortalHelper trapdoor = new PortalHelper(helper, "trapdoor", 0x2055fe, 0xfe7020);
		PortalHelper path = new PortalHelper(helper, "path", 0x2055fe, 0xfe7020);

		daylightSensor.primary().shootFrom(new Vec3(23, 3, 2.5), Direction.DOWN, 0);
		slab.primary().shootFrom(new Vec3(20, 3, 2.5), Direction.DOWN, 0);
		stairsIn.primary().shootFrom(new Vec3(17, 4, 2.5), Direction.DOWN, 0);
		stairsOut.primary().shootFrom(new Vec3(14, 4, 2.5), Direction.DOWN, 0);
		carpet.primary().shootFrom(new Vec3(11, 3, 2.5), Direction.DOWN, 0);
		stonecutter.primary().shootFrom(new Vec3(8, 3, 2.5), Direction.DOWN, 0);
		trapdoor.primary().shootFrom(new Vec3(5, 3, 2.5), Direction.DOWN, 0);
		path.primary().shootFrom(new Vec3(2, 3, 2.5), Direction.DOWN, 0);

		daylightSensor.secondary().shootFrom(new Vec3(23, 7, 6.5), Direction.UP, 90);
		slab.secondary().shootFrom(new Vec3(20, 7, 6.5), Direction.UP, 90);
		stairsIn.secondary().shootFrom(new Vec3(17, 7, 6.5), Direction.UP, 0);
		stairsOut.secondary().shootFrom(new Vec3(14, 7, 6.5), Direction.UP, 0);
		carpet.secondary().shootFrom(new Vec3(11, 7, 6.5), Direction.UP, 90);
		stonecutter.secondary().shootFrom(new Vec3(8, 7, 6.5), Direction.UP, 90);
		trapdoor.secondary().shootFrom(new Vec3(5, 7, 6.5), Direction.UP, 90);
		path.secondary().shootFrom(new Vec3(2, 7, 6.5), Direction.UP, 90);

		helper.pressButton(12, 2, 9);

		helper.succeedWhen(() -> {
			helper.assertBlockProperty(new BlockPos(22, 8, 0), RedstoneLampBlock.LIT, true);
			helper.assertBlockProperty(new BlockPos(19, 8, 0), RedstoneLampBlock.LIT, true);
			helper.assertBlockProperty(new BlockPos(16, 8, 0), RedstoneLampBlock.LIT, true);
			helper.assertBlockProperty(new BlockPos(13, 8, 0), RedstoneLampBlock.LIT, true);
			helper.assertBlockProperty(new BlockPos(10, 8, 0), RedstoneLampBlock.LIT, true);
			helper.assertBlockProperty(new BlockPos(7, 8, 0), RedstoneLampBlock.LIT, true);
			helper.assertBlockProperty(new BlockPos(4, 8, 0), RedstoneLampBlock.LIT, true);
			helper.assertBlockProperty(new BlockPos(1, 8, 0), RedstoneLampBlock.LIT, true);
		});
	}
	@GameTest(template = GROUP + "odd_portal_surfaces_horizontal")
	public void oddPortalSurfacesHorizontal(GameTestHelper helper) {

		PortalHelper wallPair = new PortalHelper(helper, "wall", 0x2055fe, 0xfe7020);
		PortalHelper doorPair = new PortalHelper(helper, "door", 0x2055fe, 0xfe7020);

		wallPair.primary().shootFrom(new Vec3(8, 3, 1.5), Direction.WEST);
		doorPair.primary().shootFrom(new Vec3(8, 3, 3.5), Direction.WEST);

		wallPair.secondary().placeOn(new BlockPos(3, 2, 1), Direction.WEST);
		doorPair.secondary().placeOn(new BlockPos(3, 2, 3), Direction.WEST);

		helper.pressButton(14, 3, 2);

		helper.succeedWhen(() -> {
			helper.assertBlockProperty(new BlockPos(1, 2, 1), RedstoneLampBlock.LIT, true);
			helper.assertBlockProperty(new BlockPos(1, 2, 3), RedstoneLampBlock.LIT, true);
		});
	}

	//Test portals against solid surfaces to make sure the entity traveling through doesn't clip where it shouldn't
	//@GameTest(template = GROUP + "portal_against_solid_blocks")
	//public void portalAgainstSolidBlocks(GameTestHelper helper) {
	//
	//}

	//Test portals against thin surfaces, such as on both sides of a trapdoor
	//@GameTest(template = GROUP + "thin_portal_surfaces")
	//public void thinPortalSurfaces(GameTestHelper helper) {
	//
	//}

	//Test portal collision carving to make sure it only works in the intended direction
	@GameTest(template = GROUP + "collision_carving")
	public void collisionCarving(GameTestHelper helper) {

		PortalHelper collisionSides = new PortalHelper(helper, "collision_sides", 0x2055fe, 0xfe7020);

		collisionSides.primary().placeOn(new BlockPos(5, 7, 5), Direction.NORTH);
		collisionSides.secondary().placeOn(new BlockPos(5, 15, 5), Direction.NORTH);

		helper.setBlock(5, 10, 5, Blocks.AIR);
		helper.pressButton(new BlockPos(5, 2, 4));
		helper.pressButton(new BlockPos(4, 7, 10));
		helper.pressButton(new BlockPos(6, 7, 0));
		helper.pressButton(new BlockPos(0, 7, 4));
		helper.pressButton(new BlockPos(10, 7, 6));

		helper.succeedWhen(() -> {
			helper.assertBlockProperty(new BlockPos(4, 7, 3), RedstoneLampBlock.LIT, true);
			helper.assertBlockProperty(new BlockPos(7, 7, 4), RedstoneLampBlock.LIT, false);
			helper.assertBlockProperty(new BlockPos(3, 7, 6), RedstoneLampBlock.LIT, true);
			helper.assertBlockProperty(new BlockPos(6, 7, 7), RedstoneLampBlock.LIT, true);
			helper.assertBlockProperty(new BlockPos(3, 4, 5), RedstoneLampBlock.LIT, true);
			helper.assertBlockProperty(new BlockPos(5, 15, 3), RedstoneLampBlock.LIT, true);
			helper.assertBlockProperty(new BlockPos(5, 8, 5), RedstoneLampBlock.LIT, true);
		});
	}
	//Test collision carving to make sure that portals on corners/edges don't allow entities through unless they're directly in front of the portal
	@GameTest(template = GROUP + "edge_collision")
	public void edgeCollision(GameTestHelper helper) {

		PortalHelper collisionEdges = new PortalHelper(helper, "collision_edges", 0x2055fe, 0xfe7020);

		collisionEdges.primary().placeOn(new BlockPos(3, 2, 5), Direction.NORTH);
		collisionEdges.secondary().placeOn(new BlockPos(3, 2, 7), Direction.SOUTH);

		helper.pressButton(new BlockPos(4, 2, 0));

		helper.succeedWhen(() -> {
			helper.assertBlockProperty(new BlockPos(5, 2, 4), RedstoneLampBlock.LIT, true);
			helper.assertBlockProperty(new BlockPos(1, 2, 4), RedstoneLampBlock.LIT, true);
			helper.assertBlockProperty(new BlockPos(2, 2, 10), RedstoneLampBlock.LIT, false);
			helper.assertBlockProperty(new BlockPos(3, 2, 10), RedstoneLampBlock.LIT, true);
			helper.assertBlockProperty(new BlockPos(4, 2, 10), RedstoneLampBlock.LIT, false);
		});
	}


	//Test portals opening behind certain blocks breaking them
	@GameTest(template = GROUP + "portal_open_popoff")
	public void portalOpenPopoff(GameTestHelper helper) {

		PortalHelper floorPairOne = new PortalHelper(helper, "floor_pair_one", 0x2055fe, 0xfe7020);
		PortalHelper floorPairTwo = new PortalHelper(helper, "floor_pair_two", 0x2055fe, 0xfe7020);
		PortalHelper floorPairThree = new PortalHelper(helper, "floor_pair_three", 0x2055fe, 0xfe7020);
		PortalHelper wallPairOne = new PortalHelper(helper, "wall_pair_one", 0x2055fe, 0xfe7020);
		PortalHelper wallPairTwo = new PortalHelper(helper, "wall_pair_two", 0x2055fe, 0xfe7020);
		PortalHelper wallPairThree = new PortalHelper(helper, "wall_pair_three", 0x2055fe, 0xfe7020);
		PortalHelper ceilingPairOne = new PortalHelper(helper, "ceiling_pair_one", 0x2055fe, 0xfe7020);
		PortalHelper ceilingPairTwo = new PortalHelper(helper, "ceiling_pair_two", 0x2055fe, 0xfe7020);
		PortalHelper ceilingPairThree = new PortalHelper(helper, "ceiling_pair_three", 0x2055fe, 0xfe7020);

		floorPairOne.primary().placeOn(new BlockPos(5, 1, 1), Direction.UP);
		floorPairOne.secondary().placeOn(new BlockPos(4, 1, 1), Direction.UP);
		floorPairTwo.primary().placeOn(new BlockPos(3, 1, 1), Direction.UP);
		floorPairTwo.secondary().placeOn(new BlockPos(2, 1, 1), Direction.UP);
		floorPairThree.primary().placeOn(new BlockPos(1, 1, 1), Direction.UP);
		floorPairThree.secondary().placeOn(new BlockPos(0, 1, 1), Direction.UP);

		wallPairOne.primary().placeOn(new BlockPos(5, 3, 4), Direction.NORTH);
		wallPairOne.secondary().placeOn(new BlockPos(4, 3, 4), Direction.NORTH);
		wallPairTwo.primary().placeOn(new BlockPos(3, 3, 4), Direction.NORTH);
		wallPairTwo.secondary().placeOn(new BlockPos(2, 3, 4), Direction.NORTH);
		wallPairThree.primary().placeOn(new BlockPos(1, 3, 4), Direction.NORTH);
		wallPairThree.secondary().placeOn(new BlockPos(0, 3, 4), Direction.NORTH);

		ceilingPairOne.primary().placeOn(new BlockPos(5, 5, 1), Direction.DOWN);
		ceilingPairOne.secondary().placeOn(new BlockPos(4, 5, 1), Direction.DOWN);
		ceilingPairTwo.primary().placeOn(new BlockPos(3, 5, 1), Direction.DOWN);
		ceilingPairTwo.secondary().placeOn(new BlockPos(2, 5, 1), Direction.DOWN);
		ceilingPairThree.primary().placeOn(new BlockPos(1, 5, 1), Direction.DOWN);
		ceilingPairThree.secondary().placeOn(new BlockPos(0, 5, 1), Direction.DOWN);

		helper.succeedWhen(() -> {
			//Floor
			helper.assertBlockPresent(Blocks.AIR, 5, 2 ,1);
			helper.assertBlockPresent(Blocks.AIR, 4, 2 ,1);
			helper.assertBlockPresent(Blocks.AIR, 3, 2 ,1);
			helper.assertBlockPresent(Blocks.AIR, 2, 2 ,1);
			helper.assertBlockPresent(Blocks.AIR, 1, 2 ,1);
			//Wall
			helper.assertBlockPresent(Blocks.AIR, 5, 3 ,3);
			helper.assertBlockPresent(Blocks.AIR, 4, 3 ,3);
			helper.assertBlockPresent(Blocks.AIR, 3, 3 ,3);
			helper.assertBlockPresent(Blocks.AIR, 2, 3 ,3);
			helper.assertBlockPresent(Blocks.AIR, 1, 3 ,3);
			//Ceiling
			helper.assertBlockPresent(Blocks.AIR, 5, 4 ,1);
			helper.assertBlockPresent(Blocks.AIR, 4, 4 ,1);
			helper.assertBlockPresent(Blocks.AIR, 3, 4 ,1);
			helper.assertBlockPresent(Blocks.AIR, 2, 4 ,1);
			helper.assertBlockPresent(Blocks.AIR, 1, 4 ,1);
		});
	}

}
