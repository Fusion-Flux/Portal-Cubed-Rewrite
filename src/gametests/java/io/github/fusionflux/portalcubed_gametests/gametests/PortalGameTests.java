package io.github.fusionflux.portalcubed_gametests.gametests;

import static io.github.fusionflux.portalcubed_gametests.gametests.PropGameTests.spawnProp;

import java.util.stream.IntStream;

import io.github.fusionflux.portalcubed.content.PortalCubedEntities;
import io.github.fusionflux.portalcubed.content.prop.PropType;
import io.github.fusionflux.portalcubed.content.prop.entity.Prop;
import io.github.fusionflux.portalcubed_gametests.Batches;
import io.github.fusionflux.portalcubed_gametests.PortalCubedGameTests;
import io.github.fusionflux.portalcubed_gametests.PortalHelper;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CopperBulbBlock;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.minecraft.world.phys.Vec3;

public class PortalGameTests implements FabricGameTest {
	private static final String GROUP = PortalCubedGameTests.ID + ":portals/";

	//Tests entities being dropped into portals and maintaining momentum
	@GameTest(template = GROUP + "drop_entity_into_portal")
	public void dropEntityIntoPortal(GameTestHelper helper) {

		PortalHelper armorStandPair = new PortalHelper(helper, "armor_stand");
		PortalHelper cubePair = new PortalHelper(helper, "cube", 0xfe2020, 0xfeed20);

		armorStandPair.primary().placeOn(new BlockPos(10, 0, 1), Direction.UP, -90);
		cubePair.primary().placeOn(new BlockPos(10, 0, 3), Direction.UP, -90);
		armorStandPair.secondary().placeOn(new BlockPos(7, 2, 1), Direction.WEST);
		cubePair.secondary().placeOn(new BlockPos(7, 2, 3), Direction.WEST);

		helper.setBlock(9, 3, 1, Blocks.AIR);
		helper.setBlock(9, 3, 3, Blocks.AIR);

		helper.succeedWhen(() -> {
			helper.assertEntityPresent(EntityType.ARMOR_STAND, new BlockPos(2, 1, 1));
			helper.assertEntityPresent(PropType.PORTAL_1_STORAGE_CUBE.entityType(), new BlockPos(2, 1, 3));
		});
	}


	//Tests entities being dropped into portals at varying heights and landing at the correct distances
	@GameTest(template = GROUP + "fling_distance_relative_to_height")
	public void flingDistanceRelativeToHeight(GameTestHelper helper) {

		PortalHelper pairOne = new PortalHelper(helper, "one");
		PortalHelper pairTwo = new PortalHelper(helper, "two", 0xfe2020, 0xfeed20);
		PortalHelper pairThree = new PortalHelper(helper, "three", 0x20abfe, 0x9120fe);
		PortalHelper pairFour = new PortalHelper(helper, "four", 0x34fe20, 0xfe20fa);
		PortalHelper pairFive = new PortalHelper(helper, "five", 0xffffff, 0x000000);
		PortalHelper pairSix = new PortalHelper(helper, "six", 0x2f8f8e, 0xff74e4);

		pairOne.primary().placeOn(new BlockPos(22, 0, 1), Direction.UP, -90);
		pairTwo.primary().placeOn(new BlockPos(22, 0, 3), Direction.UP, -90);
		pairThree.primary().placeOn(new BlockPos(22, 0, 5), Direction.UP, -90);
		pairFour.primary().placeOn(new BlockPos(22, 0, 7), Direction.UP, -90);
		pairFive.primary().placeOn(new BlockPos(22, 0, 9), Direction.UP, -90);
		pairSix.primary().placeOn(new BlockPos(22, 0, 11), Direction.UP, -90);

		pairOne.secondary().placeOn(new BlockPos(18, 6, 1), Direction.WEST);
		pairTwo.secondary().placeOn(new BlockPos(18, 6, 3), Direction.WEST);
		pairThree.secondary().placeOn(new BlockPos(18, 6, 5), Direction.WEST);
		pairFour.secondary().placeOn(new BlockPos(18, 6, 7), Direction.WEST);
		pairFive.secondary().placeOn(new BlockPos(18, 6, 9), Direction.WEST);
		pairSix.secondary().placeOn(new BlockPos(18, 6, 11), Direction.WEST);

		helper.setBlock(21, 2, 1, Blocks.AIR);
		helper.setBlock(21, 5, 3, Blocks.AIR);
		helper.setBlock(21, 8, 5, Blocks.AIR);
		helper.setBlock(21, 11, 7, Blocks.AIR);
		helper.setBlock(21, 14, 9, Blocks.AIR);
		helper.setBlock(21, 17, 11, Blocks.AIR);

		helper.succeedWhen(() -> {
			helper.assertEntityPresent(EntityType.ARMOR_STAND, new BlockPos(12, 1, 1));
			helper.assertEntityPresent(EntityType.ARMOR_STAND, new BlockPos(10, 1, 3));
			helper.assertEntityPresent(EntityType.ARMOR_STAND, new BlockPos(9, 1, 5));
			helper.assertEntityPresent(EntityType.ARMOR_STAND, new BlockPos(8, 1, 7));
			helper.assertEntityPresent(EntityType.ARMOR_STAND, new BlockPos(6, 1, 9));
			helper.assertEntityPresent(EntityType.ARMOR_STAND, new BlockPos(6, 1, 11));
		});
	}


	//Tests infinite falling to make sure the entity doesn't leave the portal if left falling forever
	@GameTest(template = GROUP + "infinite_fall")
	public void infiniteFall(GameTestHelper helper) {

		PortalHelper infiniteFall = new PortalHelper(helper, "infinite_fall");

		helper.setBlock(3, 5, 2, Blocks.AIR);
		infiniteFall.primary().placeOn(new BlockPos(2, 3, 2), Direction.UP, 90);
		infiniteFall.secondary().placeOn(new BlockPos(2, 9, 2), Direction.DOWN, 90);

		helper.runAfterDelay(99, () -> helper.succeedWhen(() -> helper.assertEntityPresent(EntityType.ARMOR_STAND)));
	}

	//Tests infinite falling to make sure the entity doesn't collide with blocks behind the portal they fall into at high speeds
	@GameTest(template = GROUP + "infinite_fall_collision")
	public void infiniteFallCollision(GameTestHelper helper) {

		PortalHelper infiniteFallCollision = new PortalHelper(helper, "infinite_fall_collision");

		infiniteFallCollision.primary().placeOn(new BlockPos(1, 6, 4), Direction.UP, 90);
		infiniteFallCollision.secondary().placeOn(new BlockPos(1, 10, 4), Direction.DOWN, 90);
		spawnProp(helper, PropType.PORTAL_1_COMPANION_CUBE, new BlockPos(2, 8, 4));

		helper.runAfterDelay(99, () ->
				helper.assertBlockProperty(new BlockPos(2, 1, 0), RedstoneLampBlock.LIT, false));
	}


	//Tests weird portal surface shapes to make sure the entity traveling through doesn't behave weirdly. Split into vertical and horizontal tests
	@GameTest(template = GROUP + "odd_portal_surfaces_vertical")
	public void oddPortalSurfacesVertical(GameTestHelper helper) {

		PortalHelper daylightSensor = new PortalHelper(helper, "daylight_sensor");
		PortalHelper slab = new PortalHelper(helper, "slab");
		PortalHelper stairsIn = new PortalHelper(helper, "stairs_in");
		PortalHelper stairsOut = new PortalHelper(helper, "stairs_out");
		PortalHelper carpet = new PortalHelper(helper, "carpet");
		PortalHelper stonecutter = new PortalHelper(helper, "stonecutter");
		PortalHelper trapdoor = new PortalHelper(helper, "trapdoor");
		PortalHelper path = new PortalHelper(helper, "path");

		daylightSensor.primary().shootFrom(new Vec3(23, 3, 2.5), Direction.DOWN, 0);
		slab.primary().shootFrom(new Vec3(20, 3, 2.5), Direction.DOWN, 0);
		stairsIn.primary().shootFrom(new Vec3(17, 3, 2.5), Direction.DOWN, 0);
		stairsOut.primary().shootFrom(new Vec3(14, 3, 2.5), Direction.DOWN, 0);
		carpet.primary().shootFrom(new Vec3(11, 3, 2.5), Direction.DOWN, 0);
		stonecutter.primary().shootFrom(new Vec3(8, 3, 2.5), Direction.DOWN, 0);
		trapdoor.primary().shootFrom(new Vec3(5, 3, 2.5), Direction.DOWN, 0);
		path.primary().shootFrom(new Vec3(2, 3, 2.5), Direction.DOWN, 0);

		daylightSensor.secondary().shootFrom(new Vec3(23, 6, 6.5), Direction.UP, 90);
		slab.secondary().shootFrom(new Vec3(20, 6, 6.5), Direction.UP, 90);
		stairsIn.secondary().shootFrom(new Vec3(17, 6, 6.5), Direction.UP, 0);
		stairsOut.secondary().shootFrom(new Vec3(14, 6, 6.5), Direction.UP, 0);
		carpet.secondary().shootFrom(new Vec3(11, 6, 6.5), Direction.UP, 90);
		stonecutter.secondary().shootFrom(new Vec3(8, 6, 6.5), Direction.UP, 90);
		trapdoor.secondary().shootFrom(new Vec3(5, 6, 6.5), Direction.UP, 90);
		path.secondary().shootFrom(new Vec3(2, 6, 6.5), Direction.UP, 90);

		helper.pressButton(12, 1, 9);

		helper.succeedWhen(() -> {
			helper.assertBlockProperty(new BlockPos(22, 7, 0), RedstoneLampBlock.LIT, true);
			helper.assertBlockProperty(new BlockPos(19, 7, 0), RedstoneLampBlock.LIT, true);
			helper.assertBlockProperty(new BlockPos(16, 7, 0), RedstoneLampBlock.LIT, true);
			helper.assertBlockProperty(new BlockPos(13, 7, 0), RedstoneLampBlock.LIT, true);
			helper.assertBlockProperty(new BlockPos(10, 7, 0), RedstoneLampBlock.LIT, true);
			helper.assertBlockProperty(new BlockPos(7, 7, 0), RedstoneLampBlock.LIT, true);
			helper.assertBlockProperty(new BlockPos(4, 7, 0), RedstoneLampBlock.LIT, true);
			helper.assertBlockProperty(new BlockPos(1, 7, 0), RedstoneLampBlock.LIT, true);
		});
	}
	@GameTest(template = GROUP + "odd_portal_surfaces_horizontal")
	public void oddPortalSurfacesHorizontal(GameTestHelper helper) {

		PortalHelper wallPair = new PortalHelper(helper, "wall");
		PortalHelper doorPair = new PortalHelper(helper, "door");

		wallPair.primary().shootFrom(new Vec3(8, 2, 1.5), Direction.WEST);
		doorPair.primary().shootFrom(new Vec3(8, 2, 3.5), Direction.WEST);

		wallPair.secondary().placeOn(new BlockPos(3, 1, 1), Direction.WEST);
		doorPair.secondary().placeOn(new BlockPos(3, 1, 3), Direction.WEST);

		helper.pressButton(14, 2, 2);

		helper.succeedWhen(() -> {
			helper.assertEntityPresent(EntityType.ARMOR_STAND, new BlockPos(2, 1, 1));
			helper.assertEntityPresent(EntityType.ARMOR_STAND, new BlockPos(2, 1, 3));
		});
	}


	//Tests portals against solid surfaces to make sure the entity traveling through doesn't clip when it shouldn't.  Currently only tests full blocks, could be expanded in the future
	@GameTest(template = GROUP + "portal_against_solid_blocks")
	public void portalAgainstSolidBlocks(GameTestHelper helper) {

		PortalHelper verticalGrate = new PortalHelper(helper, "vertical_grate");
		PortalHelper horizontalGrate = new PortalHelper(helper, "horizontal_grate");

		verticalGrate.primary().placeOn(new BlockPos(7, 1, 2), Direction.UP);
		horizontalGrate.primary().placeOn(new BlockPos(2, 1, 2), Direction.UP);

		verticalGrate.secondary().placeOn(new BlockPos(7, 2, 8), Direction.NORTH);
		horizontalGrate.secondary().placeOn(new BlockPos(2, 4, 6), Direction.DOWN);

		helper.setBlock(7, 3, 3, Blocks.AIR);
		helper.setBlock(2, 3, 3, Blocks.AIR);

		helper.runAfterDelay(40, () -> helper.succeedWhen(() -> {
			helper.assertBlockProperty(new BlockPos(7, 1, 8), RedstoneLampBlock.LIT, false);
			helper.assertBlockProperty(new BlockPos(0, 1, 7), RedstoneLampBlock.LIT, false);
		}));

	}

	//Tests portals being shot through solid and nonsolid blocks such as grates
	@GameTest(template = GROUP + "portal_shots_through_nonsolid_blocks")
	public void portalShotsThroughNonsolidBlocks(GameTestHelper helper) {
		PortalHelper nonsolidSurfaceShot = new PortalHelper(helper, "nonsolidSurfaceShot");

		nonsolidSurfaceShot.primary().shootFrom(new Vec3(5.5, 3, 0), Direction.SOUTH);
		nonsolidSurfaceShot.secondary().shootFrom(new Vec3(2.5, 3, 0), Direction.SOUTH);

		helper.runAfterDelay(20, () -> helper.succeedWhen(() -> {
			nonsolidSurfaceShot.primary().assertPresent(5.5, 3, 3, Direction.NORTH);
			nonsolidSurfaceShot.secondary().assertPresent(2.5, 3, 3, Direction.NORTH);
		}));
	}


	//Tests the gamerule for more restrictive portal surfaces.  Ran separately as its own batch; toggles the gamerule on before running, then off when finishing
	@GameTest(template = GROUP + "restrictive_portal_surfaces", batch = Batches.RESTRICTED_PORTAL_SURFACES)
	public void restrictivePortalSurfaces(GameTestHelper helper) {
		PortalHelper restrictiveSurface = new PortalHelper(helper, "restrictive_surface");

		restrictiveSurface.primary().shootFrom(new Vec3(5.5, 3, 0), Direction.SOUTH);
		restrictiveSurface.secondary().shootFrom(new Vec3(2.5, 3, 0), Direction.SOUTH);

		helper.runAfterDelay(10, () ->
				helper.succeedWhen(() -> restrictiveSurface.primary().assertNotPresent())
		);
	}

	//Tests the "create" portion of the portal command
	@GameTest(template = GROUP + "portal_command_create")
	public void portalCommandCreate(GameTestHelper helper) {

		PortalHelper placeOn1 = new PortalHelper(helper, "placeOn1");
		PortalHelper placeAt1 = new PortalHelper(helper, "placeAt1");
		PortalHelper shotFrom1 = new PortalHelper(helper, "shotFrom1");

		helper.pressButton(0, 2, 1);

		helper.succeedWhen(() -> {
			placeOn1.primary().assertPresent(1.5, 2, 5.5, Direction.NORTH);
			placeOn1.secondary().assertNotPresent();
			placeAt1.primary().assertPresent(2.5, 2, 5.5, Direction.NORTH);
			placeAt1.secondary().assertNotPresent(); //this needs to be assertPresent eventually I think
			shotFrom1.primary().assertPresent(3.5, 2, 5.5, Direction.NORTH);
			shotFrom1.secondary().assertNotPresent();
			//TODO: revisit this after place_at is fixed to make sure the rest are correct
		});
	}

	//Tests the "remove" portion of the portal command
	@GameTest(template = GROUP + "portal_command_remove")
	public void portalCommandRemove(GameTestHelper helper) {
		PortalHelper removeSingle = new PortalHelper(helper, "removesingle");
		PortalHelper removePair = new PortalHelper(helper, "removepair");

		removeSingle.primary().placeOn(new BlockPos(0, 2, 4), Direction.NORTH);
		removePair.primary().placeOn(new BlockPos(2, 2, 4), Direction.NORTH);
		removePair.secondary().placeOn(new BlockPos(4, 2, 4), Direction.NORTH);

		helper.runAfterDelay(20, () -> helper.pullLever(2, 1, 0));
		helper.succeedWhen(() -> {
			removeSingle.primary().assertNotPresent();
			removePair.primary().assertNotPresent();
			removePair.secondary().assertNotPresent();
		});
	}

	//Tests the "create" portion of the portal command
	@GameTest(template = GROUP + "projectiles_through_portals")
	public void projectilesThroughPortals(GameTestHelper helper) {

		PortalHelper windCharge = new PortalHelper(helper, "wind_charge");
		PortalHelper snowball = new PortalHelper(helper, "snowball");
		PortalHelper fireworkRocket = new PortalHelper(helper, "firework_rocket");
		PortalHelper arrow = new PortalHelper(helper, "arrow");

		windCharge.primary().shootFrom(new Vec3(1.5, 2, 4.5), Direction.UP);
		windCharge.secondary().shootFrom(new Vec3(1.5, 2.5, 1.5), Direction.DOWN);
		snowball.primary().shootFrom(new Vec3(3.5, 2, 4.5), Direction.UP);
		snowball.secondary().shootFrom(new Vec3(3.5, 2.5, 1.5), Direction.DOWN);
		fireworkRocket.primary().shootFrom(new Vec3(5.5, 2, 4.5), Direction.UP);
		fireworkRocket.secondary().shootFrom(new Vec3(5.5, 2.5, 1.5), Direction.DOWN);
		arrow.primary().shootFrom(new Vec3(7.5, 2, 4.5), Direction.UP);
		arrow.secondary().shootFrom(new Vec3(7.5, 2.5, 1.5), Direction.DOWN);

		helper.pullLever(6, 1, 4);
		helper.pullLever(2, 1, 4);

		helper.succeedWhen(() -> {
			helper.assertBlockProperty(new BlockPos(1, 4, 1), CopperBulbBlock.LIT, true);
			helper.assertBlockProperty(new BlockPos(3, 4, 1), CopperBulbBlock.LIT, true);
			helper.assertBlockProperty(new BlockPos(5, 4, 1), RedstoneLampBlock.LIT, true);
			helper.assertBlockProperty(new BlockPos(7, 4, 1), CopperBulbBlock.LIT, true);
		});
	}

	//Tests portals becoming obstructed by solid blocks
	@GameTest(template = GROUP + "portal_become_obstructed")
	public void portalBecomeObstructed(GameTestHelper helper) {

		PortalHelper pistonSolid = new PortalHelper(helper, "piston_solid");
		PortalHelper pistonNonSolid = new PortalHelper(helper, "piston_nonsolid");
		PortalHelper waterFlow = new PortalHelper(helper, "water_flow");
		PortalHelper doorTrapdoor = new PortalHelper(helper, "door_trapdoor");

		pistonSolid.primary().placeOn(new BlockPos(7, 1, 8), Direction.NORTH);
		pistonNonSolid.primary().placeOn(new BlockPos(5, 1, 8), Direction.NORTH);
		waterFlow.primary().placeOn(new BlockPos(3, 1, 8), Direction.NORTH);
		doorTrapdoor.primary().placeOn(new BlockPos(1, 1, 8), Direction.NORTH);

		pistonSolid.secondary().placeOn(new BlockPos(7, 0, 2), Direction.UP, 180);
		pistonNonSolid.secondary().placeOn(new BlockPos(5, 0, 2), Direction.UP, 180);
		waterFlow.secondary().placeOn(new BlockPos(3, 0, 2), Direction.UP, 180);
		doorTrapdoor.secondary().placeOn(new BlockPos(1, 0, 2), Direction.UP, 180);

		helper.pullLever(6, 2, 5);
		helper.pullLever(2, 2, 5);

		helper.succeedWhen(() -> {
			pistonSolid.primary().assertNotPresent();
			pistonSolid.secondary().assertNotPresent();
			pistonNonSolid.primary().assertPresent(5.5, 2, 8, Direction.NORTH);
			pistonNonSolid.secondary().assertPresent(5.5, 1, 2, Direction.UP);
			waterFlow.primary().assertNotPresent();
			waterFlow.secondary().assertNotPresent();
			doorTrapdoor.primary().assertNotPresent();
			doorTrapdoor.secondary().assertNotPresent();
		});
	}


	//Tests portals being placed on/in blocks that should or shouldn't support them
	@GameTest(template = GROUP + "valid_portal_surfaces")
	public void validPortalSurfaces(GameTestHelper helper) {
		PortalHelper sanePortalSurfaces = new PortalHelper(helper, "sane_surfaces");
		PortalHelper facadeSurfaces = new PortalHelper(helper, "facade_surfaces");

		sanePortalSurfaces.primary().shootFrom(new Vec3(11.5, 3, 1), Direction.SOUTH);
		sanePortalSurfaces.secondary().shootFrom(new Vec3(8.5, 3, 1), Direction.SOUTH);
		facadeSurfaces.primary().shootFrom(new Vec3(5.5, 3, 1), Direction.SOUTH);
		facadeSurfaces.secondary().shootFrom(new Vec3(2.5, 3, 1), Direction.SOUTH);

		helper.succeedWhen(() -> {
			sanePortalSurfaces.primary().assertPresent(11.5, 3, 3, Direction.NORTH);
			sanePortalSurfaces.secondary().assertNotPresent();
			facadeSurfaces.primary().assertNotPresent();
			facadeSurfaces.secondary().assertPresent(2.5, 3, 3, Direction.NORTH);
		});
	}


	//Tests portal bumping if you shoot too close to a wall
	@GameTest(template = GROUP + "portal_bump_wall")
	public void portalBumpWall(GameTestHelper helper) {
		PortalHelper portalBumpWall = new PortalHelper(helper, "portal_bump_wall");

		// 0.1 offset on X
		portalBumpWall.primary().shootFrom(new Vec3(1.1, 3, 2.5), Direction.DOWN);

		helper.runAfterDelay(10, () ->
			helper.succeedWhen(() -> portalBumpWall.primary().assertPresent(1.5, 1, 2.5, Direction.UP))
		);
	}


	//Tests portal bumping if you shoot too close to another portal
	@GameTest(template = GROUP + "portal_bump_portal")
	public void portalBumpPortal(GameTestHelper helper) {

		PortalHelper portalBumpPortal = new PortalHelper(helper, "portal_bump_portal");
		portalBumpPortal.primary().shootFrom(new Vec3(1.5, 3, 2.5), Direction.DOWN);
		helper.runAfterDelay(10, () -> portalBumpPortal.secondary().shootFrom(new Vec3(2, 3, 2.5), Direction.DOWN));

		helper.runAfterDelay(20, () ->
			helper.succeedWhen(() -> portalBumpPortal.secondary().assertPresent(2.5, 1, 2.5, Direction.UP))
		);
	}


	//Tests portal bumping if you shoot an invalid portal surface.  The portal should not be bumped.
	@GameTest(template = GROUP + "portal_bump_invalid_surface")
	public void portalBumpInvalidSurface(GameTestHelper helper) {
		PortalHelper portalBumpInvalidSurface = new PortalHelper(helper, "portal_bump_invalid_surface");

		portalBumpInvalidSurface.primary().shootFrom(new Vec3(0.75, 3, 2.5), Direction.DOWN);

		helper.runAfterDelay(10, () ->
			helper.succeedWhen(() -> portalBumpInvalidSurface.primary().assertNotPresent())
		);
	}


	//Tests portal bumping across a gap.  It probably shouldn't bump, but the current implementation does, and it's hard to make it not do that.
	@GameTest(template = GROUP + "portal_bump_gap", batch = Batches.PORTALS_BUMP_THROUGH_WALLS)
	public void portalBumpGap(GameTestHelper helper) {
		PortalHelper portalBumpGap = new PortalHelper(helper, "portal_bump_gap");

		portalBumpGap.primary().shootFrom(new Vec3(1.35, 3, 2.5), Direction.DOWN, 5);

		helper.runAfterDelay(10, () ->
			helper.succeedWhen(() -> portalBumpGap.primary().assertPresent(2.6, 1, 2.5, Direction.UP))
		);
	}


	//Tests portal bumping through a thin wall with the gamerule enabled.  The portal should bump through the wall.
	@GameTest(template = GROUP + "portal_bump_thin_wall", batch = Batches.PORTALS_BUMP_THROUGH_WALLS)
	public void portalBumpThinWall(GameTestHelper helper) {
		PortalHelper portalBumpThinWall = new PortalHelper(helper, "portal_bump_thin_wall");

		portalBumpThinWall.primary().shootFrom(new Vec3(1.25, 3, 2.5), Direction.DOWN);

		helper.runAfterDelay(10, () ->
				helper.succeedWhen(() -> portalBumpThinWall.primary().assertPresent(2, 1, 2.5, Direction.UP))
		);
	}

	//Tests portal bumping through a thin wall with the gamerule disabled.  The portal should not bump through the wall.
	@GameTest(template = GROUP + "portal_bump_thin_wall", batch = Batches.PORTALS_DO_NOT_BUMP_THROUGH_WALLS)
	public void portalBumpThinWallDisabled(GameTestHelper helper) {
		PortalHelper portalBumpThinWall = new PortalHelper(helper, "portal_bump_thin_wall_disabled");

		portalBumpThinWall.primary().shootFrom(new Vec3(1.25, 3, 2.5), Direction.DOWN);

		helper.runAfterDelay(10, () ->
			helper.succeedWhen(() -> portalBumpThinWall.primary().assertNotPresent())
		);
	}


	//Tests portal bumping when a portal is shot on the edge of a block
	@GameTest(template = GROUP + "portal_bump_edge")
	public void portalBumpEdge(GameTestHelper helper) {
		PortalHelper portalBumpEdge = new PortalHelper(helper, "portal_bump_edge");

		portalBumpEdge.primary().shootFrom(new Vec3(1.25, 3, 2.5), Direction.DOWN);

		helper.runAfterDelay(10, () ->
				helper.succeedWhen(() -> portalBumpEdge.primary().assertPresent(1.5, 2, 2.5, Direction.UP))
		);
	}


	//Tests portal bumping when a portal is shot into a thin trench.  The portal should not be placed.
	@GameTest(template = GROUP + "portal_bump_trench")
	public void portalBumpTrench(GameTestHelper helper) {
		PortalHelper portalBumpTrench = new PortalHelper(helper, "portal_bump_trench");

		portalBumpTrench.primary().shootFrom(new Vec3(1.25, 3, 2.5), Direction.DOWN);

		helper.runAfterDelay(10, () ->
				helper.succeedWhen(() -> portalBumpTrench.primary().assertNotPresent())
		);
	}

	//Tests blocks that add or remove portalability to block surfaces, such as facades and portal barriers.  Makes sure that only the intended surface supports portals.
	@GameTest(template = GROUP + "surface_modification")
	public void surfaceModification(GameTestHelper helper) {
		PortalHelper addsPortalability = new PortalHelper(helper, "adds_portalability");
		PortalHelper removesPortalability = new PortalHelper(helper, "removes_portalability");
		PortalHelper portalBarrier = new PortalHelper(helper, "portal_barrier");

		addsPortalability.primary().shootFrom(new Vec3(1.5, 1.5, 2.5), Direction.SOUTH);
		addsPortalability.secondary().shootFrom(new Vec3(1.5, 1.5, 2.5), Direction.EAST);

		removesPortalability.primary().shootFrom(new Vec3(4.5, 1.5, 2.5), Direction.SOUTH);
		removesPortalability.secondary().shootFrom(new Vec3(4.5, 1.5, 2.5), Direction.EAST);

		portalBarrier.primary().shootFrom(new Vec3(7.5, 1.5, 2.5), Direction.SOUTH);
		portalBarrier.secondary().shootFrom(new Vec3(7.5, 1.5, 2.5), Direction.EAST);

		helper.succeedWhen(() -> {
			addsPortalability.primary().assertNotPresent();
			addsPortalability.secondary().assertPresent(2, 2, 2.5, Direction.WEST);
			removesPortalability.primary().assertPresent(4.5, 2, 3, Direction.NORTH);
			removesPortalability.secondary().assertNotPresent();
			portalBarrier.primary().assertPresent(7.5, 2, 3, Direction.NORTH);
			portalBarrier.secondary().assertNotPresent();
		});
	}


	//Tests portal collision carving to make sure it only works in the intended direction
	@GameTest(template = GROUP + "collision_carving")
	public void collisionCarving(GameTestHelper helper) {

		PortalHelper collisionSides = new PortalHelper(helper, "collision_sides");

		collisionSides.primary().placeOn(new BlockPos(5, 6, 5), Direction.NORTH);
		collisionSides.secondary().placeOn(new BlockPos(5, 14, 5), Direction.NORTH);

		helper.setBlock(5, 9, 5, Blocks.AIR);
		helper.pressButton(new BlockPos(5, 1, 4));
		helper.pressButton(new BlockPos(4, 6, 10));
		helper.pressButton(new BlockPos(6, 6, 0));
		helper.pressButton(new BlockPos(0, 6, 4));
		helper.pressButton(new BlockPos(10, 6, 6));

		helper.succeedWhen(() -> {
			helper.assertEntityNotPresent(EntityType.ARMOR_STAND, new BlockPos(5, 6, 4));
			helper.assertEntityPresent(EntityType.ARMOR_STAND, new BlockPos(6, 6, 5));
			helper.assertEntityPresent(EntityType.ARMOR_STAND, new BlockPos(4, 6, 5));
			helper.assertEntityPresent(EntityType.ARMOR_STAND, new BlockPos(5, 6, 6));
			helper.assertEntityPresent(EntityType.ARMOR_STAND, new BlockPos(5, 8, 5));
			helper.assertEntityPresent(EntityType.ARMOR_STAND, new BlockPos(5, 14, 4));
			helper.assertBlockProperty(new BlockPos(3, 3, 5), RedstoneLampBlock.LIT, true);
		});
	}


	//Tests collision carving to make sure that portals on corners/edges don't allow entities through in cases where they shouldn't
	@GameTest(template = GROUP + "edge_collision")
	public void edgeCollision(GameTestHelper helper) {

		PortalHelper collisionEdges = new PortalHelper(helper, "collision_edges");

		collisionEdges.primary().placeOn(new BlockPos(3, 1, 5), Direction.NORTH);
		collisionEdges.secondary().placeOn(new BlockPos(3, 1, 7), Direction.SOUTH);

		helper.pressButton(new BlockPos(4, 1, 0));
		helper.runAfterDelay(20, () ->
			helper.succeedWhen(() -> {
				helper.assertBlockProperty(new BlockPos(5, 1, 4), RedstoneLampBlock.LIT, true);
				helper.assertBlockProperty(new BlockPos(1, 1, 4), RedstoneLampBlock.LIT, true);
				helper.assertBlockProperty(new BlockPos(2, 1, 10), RedstoneLampBlock.LIT, false);
				helper.assertBlockProperty(new BlockPos(3, 1, 10), RedstoneLampBlock.LIT, true);
				helper.assertBlockProperty(new BlockPos(4, 1, 10), RedstoneLampBlock.LIT, false);
			})
		);
	}


	//Tests portals breaking certain blocks by opening over their attachment points
	@GameTest(template = GROUP + "portal_open_popoff", required = false) //TODO: require this once the feature exists, until then mark not required
	public void portalOpenPopoff(GameTestHelper helper) {

		PortalHelper floorPairOne = new PortalHelper(helper, "floor_pair_one");
		PortalHelper floorPairTwo = new PortalHelper(helper, "floor_pair_two");
		PortalHelper floorPairThree = new PortalHelper(helper, "floor_pair_three");
		PortalHelper wallPairOne = new PortalHelper(helper, "wall_pair_one");
		PortalHelper wallPairTwo = new PortalHelper(helper, "wall_pair_two");
		PortalHelper wallPairThree = new PortalHelper(helper, "wall_pair_three");
		PortalHelper ceilingPairOne = new PortalHelper(helper, "ceiling_pair_one");
		PortalHelper ceilingPairTwo = new PortalHelper(helper, "ceiling_pair_two");
		PortalHelper ceilingPairThree = new PortalHelper(helper, "ceiling_pair_three");

		floorPairOne.primary().placeOn(new BlockPos(5, 0, 1), Direction.UP);
		floorPairOne.secondary().placeOn(new BlockPos(4, 0, 1), Direction.UP);
		floorPairTwo.primary().placeOn(new BlockPos(3, 0, 1), Direction.UP);
		floorPairTwo.secondary().placeOn(new BlockPos(2, 0, 1), Direction.UP);
		floorPairThree.primary().placeOn(new BlockPos(1, 0, 1), Direction.UP);
		floorPairThree.secondary().placeOn(new BlockPos(0, 0, 1), Direction.UP);

		wallPairOne.primary().placeOn(new BlockPos(5, 2, 4), Direction.NORTH);
		wallPairOne.secondary().placeOn(new BlockPos(4, 2, 4), Direction.NORTH);
		wallPairTwo.primary().placeOn(new BlockPos(3, 2, 4), Direction.NORTH);
		wallPairTwo.secondary().placeOn(new BlockPos(2, 2, 4), Direction.NORTH);
		wallPairThree.primary().placeOn(new BlockPos(1, 2, 4), Direction.NORTH);
		wallPairThree.secondary().placeOn(new BlockPos(0, 2, 4), Direction.NORTH);

		ceilingPairOne.primary().placeOn(new BlockPos(5, 4, 2), Direction.DOWN);
		ceilingPairOne.secondary().placeOn(new BlockPos(4, 4, 2), Direction.DOWN);
		ceilingPairTwo.primary().placeOn(new BlockPos(3, 4, 2), Direction.DOWN);
		ceilingPairTwo.secondary().placeOn(new BlockPos(2, 4, 2), Direction.DOWN);
		ceilingPairThree.primary().placeOn(new BlockPos(1, 4, 2), Direction.DOWN);
		ceilingPairThree.secondary().placeOn(new BlockPos(0, 4, 2), Direction.DOWN);

		helper.succeedWhen(() -> {
			//Floor
			helper.assertBlockPresent(Blocks.AIR, 5, 1 ,1);
			helper.assertBlockPresent(Blocks.AIR, 4, 1 ,1);
			helper.assertBlockPresent(Blocks.AIR, 3, 1 ,1);
			helper.assertBlockPresent(Blocks.AIR, 2, 1 ,1);
			helper.assertBlockPresent(Blocks.AIR, 1, 1 ,1);
			//Wall
			helper.assertBlockPresent(Blocks.AIR, 5, 2 ,3);
			helper.assertBlockPresent(Blocks.AIR, 4, 2 ,3);
			helper.assertBlockPresent(Blocks.AIR, 3, 2 ,3);
			helper.assertBlockPresent(Blocks.AIR, 2, 2 ,3);
			helper.assertBlockPresent(Blocks.AIR, 1, 2 ,3);
			//Ceiling
			helper.assertBlockPresent(Blocks.AIR, 5, 3 ,1);
			helper.assertBlockPresent(Blocks.AIR, 4, 3 ,1);
			helper.assertBlockPresent(Blocks.AIR, 3, 3 ,1);
			helper.assertBlockPresent(Blocks.AIR, 2, 3 ,1);
			helper.assertBlockPresent(Blocks.AIR, 1, 3 ,1);
		});
	}

	//Tests a series of portals close together on thin surfaces
	@GameTest(template = GROUP + "portal_stack")
	public void stack(GameTestHelper helper) {
		RandomSource random = helper.getLevel().random.fork();
		PortalHelper[] pairs = IntStream.range(0, 7).mapToObj(i -> {
			random.setSeed(i);
			int colorBase = random.nextInt();
			int primary = (colorBase + 10_000) | 0xFF000000;
			int secondary = (colorBase - 10_000) | 0xFF000000;
			return new PortalHelper(helper, "portal_stack_pair_" + i, primary, secondary);
		}).toArray(PortalHelper[]::new);

		pairs[0].primary().placeOn(5, 2, 1, Direction.WEST);
		pairs[0].secondary().placeOn(5, 2, 1, Direction.EAST);
		pairs[1].primary().placeOn(6, 2, 1, Direction.WEST);
		pairs[1].secondary().placeOn(6, 2, 1, Direction.EAST);
		pairs[2].primary().placeOn(7, 2, 1, Direction.WEST);
		pairs[2].secondary().placeOn(9, 2, 1, Direction.EAST);
		pairs[3].primary().placeOn(10, 2, 1, Direction.WEST);
		pairs[3].secondary().placeOn(10, 2, 1, Direction.EAST);
		pairs[4].primary().placeOn(11, 2, 1, Direction.WEST);
		pairs[4].secondary().placeOn(13, 2, 1, Direction.EAST);
		pairs[5].primary().placeOn(14, 2, 1, Direction.WEST);
		pairs[5].secondary().placeOn(14, 2, 1, Direction.EAST);
		pairs[6].primary().placeOn(15, 2, 1, Direction.WEST);
		pairs[6].secondary().placeOn(15, 2, 1, Direction.EAST);

		helper.setBlock(1, 1, 1, Blocks.REDSTONE_BLOCK);
		EntityType<Prop> cube = PortalCubedEntities.PROPS.get(PropType.STORAGE_CUBE);
		helper.succeedWhen(() -> {
			if (helper.getTick() < 20 * 4)
				helper.fail("Waiting");

			helper.assertEntityPresent(cube, 16, 2, 1);
		});
	}
}
