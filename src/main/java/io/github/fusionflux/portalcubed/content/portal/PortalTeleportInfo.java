package io.github.fusionflux.portalcubed.content.portal;

import java.util.UUID;

import com.mojang.datafixers.util.Pair;

import io.github.fusionflux.portalcubed.content.portal.manager.PortalManager;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.FriendlyByteBuf;

public record PortalTeleportInfo(UUID pairId, PortalType entered, @Nullable PortalTeleportInfo next) {
	public boolean matches(UUID pairId, PortalType entered) {
		return this.pairId.equals(pairId) && this.entered == entered;
	}

	/**
	 * Returns a new info where the previously null tail has bet set to the given info.
	 */
	public PortalTeleportInfo append(PortalTeleportInfo last) {
		PortalTeleportInfo newNext = this.next == null ? last : this.next.append(last);
		return new PortalTeleportInfo(this.pairId, this.entered, newNext);
	}

	public PortalTeleportInfo last() {
		return this.next == null ? this : this.next.last();
	}

	/**
	 * Get the first portal entered and the last portal exited.
	 * If the relevant portal pairs do not exist or are not linked, null is returned.
	 */
	@Nullable
	public Pair<PortalInstance, PortalInstance> getFirstAndLast(PortalManager manager) {
		PortalPair firstPair = manager.getPair(this.pairId);
		if (firstPair == null || !firstPair.isLinked())
			return null;

		PortalInstance first = firstPair.getOrThrow(this.entered);
		if (this.next == null) {
			// shortcut: only 1 pair passed through
			PortalInstance last = firstPair.getOrThrow(this.entered.opposite());
			return Pair.of(first, last);
		}

		PortalTeleportInfo lastInfo = this.last();
		PortalPair lastPair = manager.getPair(lastInfo.pairId);
		if (lastPair == null || !lastPair.isLinked())
			return null;

		PortalInstance last = lastPair.getOrThrow(lastInfo.entered.opposite());
		return Pair.of(first, last);
	}

	public void toNetwork(FriendlyByteBuf buf) {
		toNetwork(buf, this);
	}

	private static void toNetwork(FriendlyByteBuf buf, PortalTeleportInfo info) {
		buf.writeUUID(info.pairId);
		buf.writeEnum(info.entered);
		buf.writeNullable(info.next, PortalTeleportInfo::toNetwork);
	}

	public static PortalTeleportInfo fromNetwork(FriendlyByteBuf buf) {
		UUID pairId = buf.readUUID();
		PortalType entered = buf.readEnum(PortalType.class);
		PortalTeleportInfo next = buf.readNullable(PortalTeleportInfo::fromNetwork);
		return new PortalTeleportInfo(pairId, entered, next);
	}
}
