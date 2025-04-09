package io.github.fusionflux.portalcubed.framework.util;

import com.mojang.serialization.Codec;

import net.minecraft.util.ExtraCodecs;

// Functions ported from https://easings.net/
@FunctionalInterface
public interface EasingFunction {
	ExtraCodecs.LateBoundIdMapper<String, EasingFunction> ID_MAPPER = new ExtraCodecs.LateBoundIdMapper<>();
	Codec<EasingFunction> CODEC = ID_MAPPER.codec(Codec.STRING);

	EasingFunction LINEAR = create("linear", x -> x);

	EasingFunction IN_SINE = create("in_sine", x -> 1 - Math.cos((x * Math.PI) / 2));
	EasingFunction OUT_SINE = create("out_sine", x -> Math.sin((x * Math.PI) / 2));
	EasingFunction IN_OUT_SINE = create("in_out_sine", x -> -(Math.cos(Math.PI * x) - 1) / 2);

	EasingFunction IN_CUBIC = create("in_cubic", x -> x * x * x);
	EasingFunction OUT_CUBIC = create("out_cubic", x -> 1 - Math.pow(1 - x, 3));
	EasingFunction IN_OUT_CUBIC = create("in_out_cubic", x -> x < 0.5 ? 4 * x * x * x : 1 - Math.pow(-2 * x + 2, 3) / 2);

	EasingFunction IN_QUAD = create("in_quad", x -> x * x);
	EasingFunction OUT_QUAD = create("out_quad", x -> 1 - (1 - x) * (1 - x));
	EasingFunction IN_OUT_QUAD = create("in_out_quad", x -> x < 0.5 ? 2 * x * x : 1 - Math.pow(-2 * x + 2, 2) / 2);

	EasingFunction IN_QUART = create("in_quart", x -> x * x * x * x);
	EasingFunction OUT_QUART = create("out_quart", x -> 1 - Math.pow(1 - x, 4));
	EasingFunction IN_OUT_QUART = create("in_out_quart", x -> x < 0.5 ? 8 * x * x * x * x : 1 - Math.pow(-2 * x + 2, 4) / 2);

	EasingFunction IN_QUINT = create("in_quint", x -> x * x * x * x * x);
	EasingFunction OUT_QUINT = create("out_quint", x -> 1 - Math.pow(1 - x, 5));
	EasingFunction IN_OUT_QUINT = create("in_out_quint", x -> x < 0.5 ? 16 * x * x * x * x * x : 1 - Math.pow(-2 * x + 2, 5) / 2);

	EasingFunction IN_EXPO = create("in_expo", x -> x == 0 ? 0 : Math.pow(2, 10 * x - 10));
	EasingFunction OUT_EXPO = create("out_expo", x -> x == 1 ? 1 : 1 - Math.pow(2, -10 * x));
	EasingFunction IN_OUT_EXPO = create("in_out_expo", x -> x == 0
			? 0
			: x == 1
			? 1
			: x < 0.5 ? Math.pow(2, 20 * x - 10) / 2
			: (2 - Math.pow(2, -20 * x + 10)) / 2
	);

	EasingFunction IN_CIRC = create("in_circ", x -> 1 - Math.sqrt(1 - Math.pow(x, 2)));
	EasingFunction OUT_CIRC = create("out_circ", x -> Math.sqrt(1 - Math.pow(x - 1, 2)));
	EasingFunction IN_OUT_CIRC = create("in_out_circ", x -> x < 0.5
			? (1 - Math.sqrt(1 - Math.pow(2 * x, 2))) / 2
			: (Math.sqrt(1 - Math.pow(-2 * x + 2, 2)) + 1) / 2);

	EasingFunction IN_BACK = create("in_back", x -> {
		double c1 = 1.70158;
		double c3 = c1 + 1;

		return c3 * x * x * x - c1 * x * x;
	});
	EasingFunction OUT_BACK = create("out_back", x -> {
		double c1 = 1.70158;
		double c3 = c1 + 1;

		return 1 + c3 * Math.pow(x - 1, 3) + c1 * Math.pow(x - 1, 2);
	});
	EasingFunction IN_OUT_BACK = create("in_out_back", x -> {
		double c1 = 1.70158;
		double c2 = c1 * 1.525;

		return x < 0.5
				? (Math.pow(2 * x, 2) * ((c2 + 1) * 2 * x - c2)) / 2
				: (Math.pow(2 * x - 2, 2) * ((c2 + 1) * (x * 2 - 2) + c2) + 2) / 2;
	});

	EasingFunction IN_ELASTIC = create("in_elastic", x -> {
		double c4 = (2 * Math.PI) / 3;

		return x == 0
				? 0
				: x == 1
				? 1
				: -Math.pow(2, 10 * x - 10) * Math.sin((x * 10 - 10.75) * c4);
	});
	EasingFunction OUT_ELASTIC = create("out_elastic", x -> {
		double c4 = (2 * Math.PI) / 3;

		return x == 0
				? 0
				: x == 1
				? 1
				: Math.pow(2, -10 * x) * Math.sin((x * 10 - 0.75) * c4) + 1;
	});
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

	EasingFunction OUT_BOUNCE = create("out_bounce", x -> {
		double n1 = 7.5625;
		double d1 = 2.75;

		if (x < 1 / d1) {
			return n1 * x * x;
		} else if (x < 2 / d1) {
			return n1 * (x -= 1.5 / d1) * x + 0.75;
		} else if (x < 2.5 / d1) {
			return n1 * (x -= 2.25 / d1) * x + 0.9375;
		} else {
			return n1 * (x -= 2.625 / d1) * x + 0.984375;
		}
	});
	EasingFunction IN_BOUNCE = create("in_bounce", x -> 1 - OUT_BOUNCE.apply(1 - x));

	EasingFunction IN_OUT_BOUNCE = create("in_out_bounce", x -> x < 0.5
			? (1 - OUT_BOUNCE.apply(1 - 2 * x)) / 2
			: (1 + OUT_BOUNCE.apply(2 * x - 1)) / 2);

	static EasingFunction create(String name, EasingFunction function) {
		ID_MAPPER.put(name, function);
		return function;
	}

	double apply(double value);
}
