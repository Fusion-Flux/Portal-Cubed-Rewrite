package io.github.fusionflux.portalcubed.framework.registration.block;

import java.util.HashMap;
import java.util.Map;

import io.github.fusionflux.portalcubed.framework.registration.Registrar;
import io.github.fusionflux.portalcubed.framework.registration.RenderTypes;
import net.minecraft.world.level.block.Block;

public class BlockHelper {
	private final Registrar registrar;

	final Map<Block, RenderTypes> renderTypes = new HashMap<>();

	public BlockHelper(Registrar registrar) {
		this.registrar = registrar;
	}

	public BlockBuilder<Block> create(String name) {
		return create(name, Block::new);
	}

	public <T extends Block> BlockBuilder<T> create(String name, BlockFactory<T> factory) {
		return new BlockBuilderImpl<>(registrar, name, factory);
	}

	public BlockBuilder<Block> createFrom(String name, Block copyFrom) {
		return create(name).copyFrom(copyFrom);
	}

	public <T extends Block> BlockBuilder<T> createFrom(String name, BlockFactory<T> factory, Block copyFrom) {
		return create(name, factory).copyFrom(copyFrom);
	}

	public Block simple(String name, Block copyFrom) {
		return create(name).copyFrom(copyFrom).build();
	}

	public <T extends Block> T simple(String name, BlockFactory<T> factory, Block copyFrom) {
		return create(name, factory).copyFrom(copyFrom).build();
	}
}
