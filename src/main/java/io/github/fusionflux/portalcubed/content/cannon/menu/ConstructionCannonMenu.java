package io.github.fusionflux.portalcubed.content.cannon.menu;

import io.github.fusionflux.portalcubed.content.PortalCubedMenus;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ConstructionCannonMenu extends AbstractContainerMenu {
	public static final Component TITLE = Component.translatable("container.portalcubed.panel_placer");

	private final Container materialContainer;

	public ConstructionCannonMenu(int syncId, Inventory playerInv) {
		super(PortalCubedMenus.PANEL_PLACER, syncId);
		this.materialContainer = new SimpleContainer(1);

		this.addSlot(new MaterialSlot(this.materialContainer, 0, 0, 0));
	}

	public ConstructionCannonMenu(int syncId, Inventory playerInv, Player player) {
		this(syncId, playerInv);
	}

	@Override
	protected void clearContainer(Player player, Container inventory) {
		super.clearContainer(player, inventory);
	}

	@Override
	public ItemStack quickMoveStack(Player player, int fromIndex) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean stillValid(Player player) {
		return true;
	}

	private static class MaterialSlot extends Slot {
		public MaterialSlot(Container inventory, int index, int x, int y) {
			super(inventory, index, x, y);
		}

		@Override
		public boolean mayPlace(ItemStack stack) {
			return true;
		}
	}
}
