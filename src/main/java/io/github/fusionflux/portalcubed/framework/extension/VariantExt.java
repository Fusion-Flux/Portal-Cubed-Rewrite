package io.github.fusionflux.portalcubed.framework.extension;

import com.mojang.math.Transformation;

public interface VariantExt {
	default Transformation pc$transformation() {
		throw new AbstractMethodError();
	}

	default void pc$transformation(Transformation transformation) {
		throw new AbstractMethodError();
	}
}
