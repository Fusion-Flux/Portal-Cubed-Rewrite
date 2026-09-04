package io.github.fusionflux.portalcubed.mixin.portals.client;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.client.KeyboardHandler;

@Mixin(KeyboardHandler.class)
public abstract class KeyboardHandlerMixin {
//	@Shadow
//	protected abstract void debugFeedbackTranslated(String message, Object... args);
//
//	@WrapOperation(
//			method = "handleDebugKeys",
//			slice = @Slice(
//					from = @At(value = "CONSTANT", args = "stringValue=debug.gamemodes.help")
//			),
//			at = @At(
//					value = "INVOKE",
//					target = "Lnet/minecraft/client/gui/components/ChatComponent;addMessage(Lnet/minecraft/network/chat/Component;)V"
//			)
//	)
//	private void addPortalDebugKeybind(ChatComponent chat, Component message, Operation<Void> original) {
//		original.call(chat, message);
//		original.call(chat, makePortalCubedMessage("debug.portalcubed.help.portals"));
//	}
//
//	@Inject(method = "handleDebugKeys", at = @At("HEAD"), cancellable = true)
//	private void handlePortalDebugKeybind(int key, CallbackInfoReturnable<Boolean> cir) {
//		if (key == GLFW.GLFW_KEY_O) {
//			PortalCubedClient.portalDebugEnabled = !PortalCubedClient.portalDebugEnabled;
//			String suffix = PortalCubedClient.portalDebugEnabled ? "on" : "off";
//			this.debugFeedbackTranslated("debug.portalcubed.help.portals." + suffix);
//			cir.setReturnValue(true);
//		}
//	}
//
//	@Unique
//	private static Component makePortalCubedMessage(String key) {
//		// this is so unnecessary but let me have my fun
//
//		Component message = Component.translatable(key);
//
//		IntIntPair colors = chooseColors();
//		Component portal = Component.translatable("debug.portalcubed.help.prefix.portal").withColor(colors.firstInt());
//		Component cubed = Component.translatable("debug.portalcubed.help.prefix.cubed").withColor(colors.secondInt());
//		Component inner = Component.translatable("debug.portalcubed.help.prefix.inner", portal, cubed);
//		return Component.translatable("debug.portalcubed.help", inner, message);
//	}
//
//	@Unique
//	private static IntIntPair chooseColors() {
//		Minecraft mc = Minecraft.getInstance();
//		if (mc.player != null) {
//			// main is first so it takes priority
//			for (InteractionHand hand : InteractionHand.values()) {
//				ItemStack stack = mc.player.getItemInHand(hand);
//				PortalGunSettings settings = stack.get(PortalCubedDataComponents.PORTAL_GUN_SETTINGS);
//				if (settings != null) {
//					float ticks = ClientTicks.get();
//					int primary = settings.primaryOrSecondary().color().getOpaque(ticks);
//					int secondary = settings.secondaryOrPrimary().color().getOpaque(ticks);
//					return IntIntPair.of(primary, secondary);
//				}
//			}
//		}
//
//		List<IntIntPair> fallback = List.of(
//				IntIntPair.of(Polarity.PRIMARY.defaultColor, Polarity.SECONDARY.defaultColor),
//				IntIntPair.of(0x76b5eb, 0x4f45b8), // atlas
//				IntIntPair.of(0xcac850, 0x9f2525) // p-body
//		);
//
//		return Util.getRandom(fallback, RandomSource.create());
//	}
}
