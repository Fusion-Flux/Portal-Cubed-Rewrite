package io.github.fusionflux.portalcubed.framework.command;

import java.util.Collection;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;

public class CollectionSmuggler<S> extends ArgumentBuilder<S, CollectionSmuggler<S>> {
	public final Collection<? extends ArgumentBuilder<S, ?>> collection;

	public CollectionSmuggler(Collection<? extends ArgumentBuilder<S, ?>> collection) {
		this.collection = collection;
	}

	@Override
	protected CollectionSmuggler<S> getThis() {
		throw new IllegalStateException("Should not call getThis on a CollectionArgumentBuilder");
	}

	@Override
	public CommandNode<S> build() {
		throw new IllegalStateException("Should not call build on a CollectionArgumentBuilder");
	}
}
