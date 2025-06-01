package io.github.fusionflux.portalcubed.mixin.portals;

import java.util.function.Function;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.google.common.collect.ImmutableList;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import io.github.fusionflux.portalcubed.content.portal.PortalDataAccessor;
import net.minecraft.server.commands.data.DataCommands;

@Mixin(DataCommands.class)
public class DataCommandsMixin {
	@ModifyExpressionValue(
			method = "<clinit>",
			at = @At(
					value = "INVOKE",
					target = "Lcom/google/common/collect/ImmutableList;of(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;"
			)
	)
	private static ImmutableList<Function<String, DataCommands.DataProvider>> registerPortalAccessor(ImmutableList<Function<String, DataCommands.DataProvider>> original) {
		ImmutableList.Builder<Function<String, DataCommands.DataProvider>> builder = ImmutableList.builder();
		builder.addAll(original);
		builder.add(PortalDataAccessor.Provider::new);
		return builder.build();
	}
}
