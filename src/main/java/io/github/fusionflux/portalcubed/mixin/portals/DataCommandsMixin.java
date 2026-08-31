package io.github.fusionflux.portalcubed.mixin.portals;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import io.github.fusionflux.portalcubed.content.portal.command.PortalDataAccessor;
import net.minecraft.server.commands.ArgProvider;
import net.minecraft.server.commands.data.DataAccessor;
import net.minecraft.server.commands.data.DataCommands;

@Mixin(DataCommands.class)
public class DataCommandsMixin {
	@ModifyExpressionValue(
			method = "<clinit>",
			at = @At(
					value = "INVOKE",
					target = "Ljava/util/List;of(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Ljava/util/List;"
			)
	)
	private static List<ArgProvider.Factory<DataAccessor>> registerPortalAccessor(List<ArgProvider.Factory<DataAccessor>> original) {
		List<ArgProvider.Factory<DataAccessor>> mutable = new ArrayList<>(original);
		mutable.add(PortalDataAccessor.PROVIDER);
		return List.copyOf(mutable);
	}
}
