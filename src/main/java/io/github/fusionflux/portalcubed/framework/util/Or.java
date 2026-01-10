package io.github.fusionflux.portalcubed.framework.util;

import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;

import com.google.common.collect.Iterators;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;

/**
 * Similar to an {@link Either}, but allows for both the left and right value to be present at once.
 */
public sealed interface Or<L, R> {
	/**
	 * @return true if this Or has a value on the left.
	 */
	boolean hasLeft();

	/**
	 * @return true if this Or has a value on the right.
	 */
	boolean hasRight();

	/**
	 * @return an {@link Optional} holding the left value, if present.
	 */
	Optional<L> maybeLeft();

	/**
	 * @return an {@link Optional} holding the right value, if present.
	 */
	Optional<R> maybeRight();

	/**
	 * Apply a pair of functions to the possible values of this Or.
	 */
	<L2, R2> Or<L2, R2> map(Function<? super L, ? extends L2> left, Function<? super R, ? extends R2> right);

	/**
	 * Apply a function to the left value if present.
	 */
	<L2> Or<L2, R> mapLeft(Function<? super L, ? extends L2> function);

	/**
	 * Apply a function to the right value if present.
	 */
	<R2> Or<L, R2> mapRight(Function<? super R, ? extends R2> function);

	/**
	 * Invoke the given consumers with each held value.
	 */
	void forEach(Consumer<? super L> left, Consumer<? super R> right);

	/**
	 * Unwrap this Or by applying one or more functions to the contained value(s).
	 * @param left function to convert a possible left value to {@code T}
	 * @param right function to convert a possible right value to {@code T}
	 * @param merger function taking a joined {@code left} and {@code right}, merging them into a single value
	 */
	<T> T join(Function<L, T> left, Function<R, T> right, BinaryOperator<T> merger);

	/**
	 * Create a new Or with the sides swapped.
	 */
	Or<R, L> swap();

	/**
	 * @return a new Or holding the given value on the left.
	 */
	static <L, R> Left<L, R> left(L value) {
		return new Left<>(value);
	}

	/**
	 * @return a new Or holding the given value on the right.
	 */
	static <L, R> Right<L, R> right(R value) {
		return new Right<>(value);
	}

	/**
	 * @return a new Or holding both of the given values.
	 */
	static <L, R> Both<L, R> both(L left, R right) {
		return new Both<>(left, right);
	}

	/**
	 * Helper for iterating over the values of an Or when both sides have the same type.
	 * The returned Iterator will always have a size of either 1 or 2.
	 */
	static <T> Iterable<T> iterate(Or<T, T> or) {
		return switch (or) {
			case Left(T value) -> () -> Iterators.singletonIterator(value);
			case Right(T value) -> () -> Iterators.singletonIterator(value);
			case Both(T left, T right) -> () -> new DualIterator<>(left, right);
		};
	}

	static <L, R> MapCodec<Or<L, R>> codec(String leftKey, Codec<L> leftCodec, String rightKey, Codec<R> rightCodec) {
		// an intermediate state where both sides can be empty makes this easiest
		record Intermediate<L, R>(Optional<L> left, Optional<R> right) {}

		MapCodec<Intermediate<L, R>> intermediateCodec = RecordCodecBuilder.mapCodec(i -> i.group(
				leftCodec.optionalFieldOf(leftKey).forGetter(Intermediate::left),
				rightCodec.optionalFieldOf(rightKey).forGetter(Intermediate::right)
		).apply(i, Intermediate::new));

		return intermediateCodec.flatXmap(intermediate -> {
			if (intermediate.left.isPresent() && intermediate.right.isPresent()) {
				return DataResult.success(Or.both(intermediate.left.get(), intermediate.right.get()));
			} else if (intermediate.left.isPresent()) { // right must be empty
				return DataResult.success(Or.left(intermediate.left.get()));
			} else if (intermediate.right.isPresent()) { // left must be empty
				return DataResult.success(Or.right(intermediate.right.get()));
			} else { // both empty
				return DataResult.error(() -> "Both values are missing");
			}
		}, or -> DataResult.success(new Intermediate<>(or.maybeLeft(), or.maybeRight())));
	}

	static <L, R, B extends ByteBuf> StreamCodec<B, Or<L, R>> streamCodec(StreamCodec<? super B, L> leftCodec, StreamCodec<? super B, R> rightCodec) {
		enum Type {
			LEFT, RIGHT, BOTH;

			private static final StreamCodec<ByteBuf, Type> CODEC = PortalCubedStreamCodecs.ofEnum(Type.class);
		}

		return new StreamCodec<>() {
			@Override
			public void encode(B buf, Or<L, R> or) {
				Type type = switch (or) {
					case Left<L, R> ignored -> Type.LEFT;
					case Right<L, R> ignored -> Type.RIGHT;
					case Both<L, R> ignored -> Type.BOTH;
				};

				Type.CODEC.encode(buf, type);
				or.forEach(left -> leftCodec.encode(buf, left), right -> rightCodec.encode(buf, right));
			}

			@Override
			public Or<L, R> decode(B buf) {
				return switch (Type.CODEC.decode(buf)) {
					case LEFT -> Or.left(leftCodec.decode(buf));
					case RIGHT -> Or.right(rightCodec.decode(buf));
					case BOTH -> Or.both(leftCodec.decode(buf), rightCodec.decode(buf));
				};
			}
		};
	}

	record Left<L, R>(L value) implements Or<L, R> {
		@Override
		public boolean hasLeft() {
			return true;
		}

		@Override
		public boolean hasRight() {
			return false;
		}

		@Override
		public Optional<L> maybeLeft() {
			return Optional.of(this.value);
		}

		@Override
		public Optional<R> maybeRight() {
			return Optional.empty();
		}

		@Override
		public <L2, R2> Left<L2, R2> map(Function<? super L, ? extends L2> left, Function<? super R, ? extends R2> right) {
			return Or.left(left.apply(this.value));
		}

		@Override
		public <L2> Left<L2, R> mapLeft(Function<? super L, ? extends L2> function) {
			return Or.left(function.apply(this.value));
		}

		@Override
		@SuppressWarnings("unchecked")
		public <R2> Left<L, R2> mapRight(Function<? super R, ? extends R2> function) {
			return (Left<L, R2>) this;
		}

		@Override
		public void forEach(Consumer<? super L> left, Consumer<? super R> right) {
			left.accept(this.value);
		}

		@Override
		public <T> T join(Function<L, T> left, Function<R, T> right, BinaryOperator<T> merger) {
			return left.apply(this.value);
		}

		@Override
		public Right<R, L> swap() {
			return Or.right(this.value);
		}
	}

	record Right<L, R>(R value) implements Or<L, R> {
		@Override
		public boolean hasLeft() {
			return false;
		}

		@Override
		public boolean hasRight() {
			return true;
		}

		@Override
		public Optional<L> maybeLeft() {
			return Optional.empty();
		}

		@Override
		public Optional<R> maybeRight() {
			return Optional.of(this.value);
		}

		@Override
		public <L2, R2> Right<L2, R2> map(Function<? super L, ? extends L2> left, Function<? super R, ? extends R2> right) {
			return Or.right(right.apply(this.value));
		}

		@Override
		@SuppressWarnings("unchecked")
		public <L2> Right<L2, R> mapLeft(Function<? super L, ? extends L2> function) {
			return (Right<L2, R>) this;
		}

		@Override
		public <R2> Right<L, R2> mapRight(Function<? super R, ? extends R2> function) {
			return Or.right(function.apply(this.value));
		}

		@Override
		public void forEach(Consumer<? super L> left, Consumer<? super R> right) {
			right.accept(this.value);
		}

		@Override
		public <T> T join(Function<L, T> left, Function<R, T> right, BinaryOperator<T> merger) {
			return right.apply(this.value);
		}

		@Override
		public Left<R, L> swap() {
			return Or.left(this.value);
		}
	}

	record Both<L, R>(L left, R right) implements Or<L, R> {
		@Override
		public boolean hasLeft() {
			return true;
		}

		@Override
		public boolean hasRight() {
			return true;
		}

		@Override
		public Optional<L> maybeLeft() {
			return Optional.of(this.left);
		}

		@Override
		public Optional<R> maybeRight() {
			return Optional.of(this.right);
		}

		@Override
		public <L2, R2> Both<L2, R2> map(Function<? super L, ? extends L2> left, Function<? super R, ? extends R2> right) {
			return Or.both(left.apply(this.left), right.apply(this.right));
		}

		@Override
		public <L2> Both<L2, R> mapLeft(Function<? super L, ? extends L2> function) {
			return Or.both(function.apply(this.left), this.right);
		}

		@Override
		public <R2> Both<L, R2> mapRight(Function<? super R, ? extends R2> function) {
			return Or.both(this.left, function.apply(this.right));
		}

		@Override
		public void forEach(Consumer<? super L> left, Consumer<? super R> right) {
			left.accept(this.left);
			right.accept(this.right);
		}

		@Override
		public <T> T join(Function<L, T> left, Function<R, T> right, BinaryOperator<T> merger) {
			return merger.apply(left.apply(this.left), right.apply(this.right));
		}

		@Override
		public Both<R, L> swap() {
			return Or.both(this.right, this.left);
		}
	}
}
