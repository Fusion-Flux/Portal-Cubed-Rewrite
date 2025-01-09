package io.github.fusionflux.portalcubed.mixin;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;

import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;

import io.github.fusionflux.portalcubed.framework.command.CollectionSmuggler;
import io.github.fusionflux.portalcubed.framework.extension.RequiredArgumentBuilderExt;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ArgumentBuilder.class, remap = false)
public abstract class ArgumentBuilderMixin<S, T extends ArgumentBuilder<S, T>> {
	@Shadow
	@Final
	private RootCommandNode<S> arguments;

	@Shadow
	public abstract T then(ArgumentBuilder<S, ?> argument);

	@Shadow
	protected abstract T getThis();

	@Shadow
	public abstract T executes(Command<S> command);

	@Inject(
			method = "then(Lcom/mojang/brigadier/builder/ArgumentBuilder;)Lcom/mojang/brigadier/builder/ArgumentBuilder;",
			at = @At("HEAD"),
			cancellable = true
	)
	private void handleCollection(ArgumentBuilder<S, ?> argument, CallbackInfoReturnable<T> cir) {
		if (argument instanceof CollectionSmuggler<?>) {
			//noinspection unchecked
			CollectionSmuggler<S> smuggler = (CollectionSmuggler<S>) argument;
			for (ArgumentBuilder<S, ?> arg : smuggler.collection) {
				this.then(arg);
			}
			cir.setReturnValue(this.getThis());
		}
	}

	@Inject(
			method = "then(Lcom/mojang/brigadier/builder/ArgumentBuilder;)Lcom/mojang/brigadier/builder/ArgumentBuilder;",
			at = @At("TAIL")
	)
	private void handleOptional(ArgumentBuilder<S, ?> argument, CallbackInfoReturnable<T> cir) {
		if (argument instanceof RequiredArgumentBuilderExt ext && ext.pc$isOptional()) {
			for (CommandNode<S> child : argument.getArguments()) {
				this.arguments.addChild(child);
			}
			Command<S> command = argument.getCommand();
			if (command != null) {
				this.executes(command);
			}
		}
	}
}
