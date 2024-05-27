package io.github.fusionflux.portalcubed.content.command;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import com.mojang.serialization.JsonOps;

import io.github.fusionflux.portalcubed.framework.construct.Construct;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.LevelResource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class CreateConstructCommand {
	private static final Logger logger = LoggerFactory.getLogger(CreateConstructCommand.class);
	private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

	public static LiteralArgumentBuilder<CommandSourceStack> build() {
		return literal("create_construct").then(
				argument("from", BlockPosArgument.blockPos()).then(
						argument("to", BlockPosArgument.blockPos()).then(
								argument("id", ResourceLocationArgument.id()).executes(ctx -> {
									BlockPos from = BlockPosArgument.getLoadedBlockPos(ctx, "from");
									BlockPos to = BlockPosArgument.getLoadedBlockPos(ctx, "to");
									ResourceLocation id = ResourceLocationArgument.getId(ctx, "id");

									BlockPos origin = new BlockPos(
											Math.min(from.getX(), to.getX()),
											Math.min(from.getY(), to.getY()),
											Math.min(from.getZ(), to.getZ())
									);

									ServerLevel level = ctx.getSource().getLevel();
									Construct.Builder builder = new Construct.Builder();
									Stats stats = new Stats();

									BlockPos.betweenClosed(from, to).forEach(pos -> {
										BlockState state = level.getBlockState(pos);
										if (!state.isAir()) {
											stats.blocks++;

											CompoundTag nbt = null;
											BlockEntity be = level.getBlockEntity(pos);
											if (be != null) {
												stats.blockEntities++;
												nbt = be.saveWithId();
											}

											BlockPos relative = pos.subtract(origin);

											builder.put(relative, state, nbt);
										}
									});

									Path output = level.getServer().getWorldPath(LevelResource.GENERATED_DIR)
											.resolve("constructs")
											.resolve(id.getNamespace())
											.resolve(id.getPath() + ".json");

									JsonObject json = Construct.CODEC.encodeStart(JsonOps.INSTANCE, builder.build())
											.getOrThrow(false, logger::error).getAsJsonObject();

									String string = gson.toJson(json);

									try {
										Files.createDirectories(output.getParent());
										Files.deleteIfExists(output);
										Files.writeString(output, string, StandardOpenOption.CREATE);
									} catch (IOException e) {
										throw new RuntimeException(e);
									}

									ctx.getSource().sendSuccess(stats::createMessage, false);

									return Command.SINGLE_SUCCESS;
								})
						)
				)
		);
	}

	private static final class Stats {
		private int blocks;
		private int blockEntities;

		private Component createMessage() {
			return Component.translatable(
					"commands.portalcubed.create_construct.success",
					this.blocks, this.blockEntities
			);
		}
	}
}
