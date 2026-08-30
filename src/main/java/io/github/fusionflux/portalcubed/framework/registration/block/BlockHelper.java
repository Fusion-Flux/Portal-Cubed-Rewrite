package io.github.fusionflux.portalcubed.framework.registration.block;

import io.github.fusionflux.portalcubed.framework.registration.Registrar;
import net.minecraft.world.level.block.Block;

public final class BlockHelper {
	private final Registrar registrar;

	public BlockHelper(Registrar registrar) {
		this.registrar = registrar;
	}

	public BlockBuilder<Block> create(String name) {
		return this.create(name, Block::new);
	}

	public <T extends Block> BlockBuilder<T> create(String name, BlockFactory<T> factory) {
		return new BlockBuilderImpl<>(this.registrar, name, factory);
	}

	public BlockBuilder<Block> createFrom(String name, Block copyFrom) {
		return this.create(name).copyFrom(copyFrom);
	}

	public <T extends Block> BlockBuilder<T> createFrom(String name, BlockFactory<T> factory, Block copyFrom) {
		return this.create(name, factory).copyFrom(copyFrom);
	}

	public Block simple(String name, Block copyFrom) {
		return this.create(name).copyFrom(copyFrom).build();
	}

	public <T extends Block> T simple(String name, BlockFactory<T> factory, Block copyFrom) {
		return this.create(name, factory).copyFrom(copyFrom).build();
	}
}
