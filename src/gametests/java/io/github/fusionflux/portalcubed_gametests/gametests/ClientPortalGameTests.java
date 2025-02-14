package io.github.fusionflux.portalcubed_gametests.gametests;

import io.github.fusionflux.portalcubed.content.PortalCubedEntities;
import io.github.fusionflux.portalcubed.content.prop.PropType;
import io.github.fusionflux.portalcubed.content.prop.entity.Prop;
import io.github.fusionflux.portalcubed_gametests.PortalCubedGameTests;
import io.github.fusionflux.portalcubed_gametests.PortalHelper;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

// Tests for client-side visuals. Put here so they can easily be run at once with /test runall ClientPortalGameTests
// see also: portals/portal_stack
public class ClientPortalGameTests {
	private static final String GROUP = PortalCubedGameTests.ID + ":portals/client/";

	//Tests the teleported entity being pushed in the time between the client and server teleportation
	@GameTest(template = GROUP + "bump")
	public void bump(GameTestHelper helper) {
		PortalHelper portals = new PortalHelper(helper, "client_cube_bump");
		portals.primary().placeOn(1, 0, 1, Direction.UP, 180);
		portals.secondary().placeOn(5, 3, 2, Direction.DOWN, 180);

		helper.setBlock(4, 1, 3, Blocks.REDSTONE_BLOCK);

		// left half of the landing block, make sure it wasn't bumped to the right
		AABB area = new AABB(5.5, 1, 2, 6, 2, 3);

		EntityType<Prop> cube = PortalCubedEntities.PROPS.get(PropType.STORAGE_CUBE);

		helper.succeedWhen(() -> {
			if (helper.getTick() < 20 * 4)
				helper.fail("Waiting");

			helper.assertEntityPresent(cube, area);
		});
	}

	//Tests a portal being blocked in the time between the client and server teleporting the entity passing through
	@GameTest(template = GROUP + "denied")
	public void denied(GameTestHelper helper) {
		PortalHelper portals = new PortalHelper(helper, "get_denied_idiot");
		portals.primary().placeOn(8, 2, 3, Direction.WEST);
		portals.secondary().placeOn(7, 1, 1, Direction.WEST);

		helper.setBlock(1, 1, 1, Blocks.REDSTONE_BLOCK);

		EntityType<Prop> cube = PortalCubedEntities.PROPS.get(PropType.STORAGE_CUBE);

		helper.succeedWhen(() -> {
			if (helper.getTick() < 20 * 4)
				helper.fail("Waiting");

			helper.assertEntityPresent(cube, 7, 2, 3);
		});
	}
}
