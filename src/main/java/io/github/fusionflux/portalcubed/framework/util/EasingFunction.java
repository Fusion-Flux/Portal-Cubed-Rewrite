package io.github.fusionflux.portalcubed.framework.util;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Codec;

// Functions ported from https://easings.net/
@FunctionalInterface
public interface EasingFunction {
	BiMap<String, EasingFunction> FUNCTIONS = HashBiMap.create();
	Codec<EasingFunction> CODEC = Codec.stringResolver(k -> FUNCTIONS.inverse().get(k), FUNCTIONS::get);

	EasingFunction LINEAR = create("linear", x -> x);
	EasingFunction IN_SINE = create("in_sine", x -> 1 - Math.cos((x * Math.PI) / 2));
	EasingFunction IN_CUBIC = create("in_cubic", x -> x * x * x);
	EasingFunction IN_OUT_ELASTIC = create("in_out_elastic", x -> {
		double c5 = (2 * Math.PI) / 4.5;
		double x2 = Math.sin((20 * x - 11.125) * c5);

		return x == 0
				? 0
				: x == 1
				? 1
				: x < 0.5
				? -(Math.pow(2, 20 * x - 10) * x2) / 2
				: (Math.pow(2, -20 * x + 10) * x2) / 2 + 1;
	});

	static EasingFunction create(String name, EasingFunction function) {
		FUNCTIONS.put(name, function);
		return function;
	}

	double apply(double value);
}
