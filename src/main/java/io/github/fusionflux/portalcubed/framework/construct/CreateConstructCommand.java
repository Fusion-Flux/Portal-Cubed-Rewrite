package io.github.fusionflux.portalcubed.framework.construct;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.slf4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.TagValueOutput;

public class CreateConstructCommand {
	private static final Logger logger = LogUtils.getLogger();
	private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

	public static LiteralArgumentBuilder<CommandSourceStack> build() {
		return literal("create_construct")
				.requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
				.then(
						argument("from", BlockPosArgument.blockPos()).then(
								argument("to", BlockPosArgument.blockPos()).then(
										argument("id", IdentifierArgument.id()).executes(ctx -> {
											BlockPos from = BlockPosArgument.getLoadedBlockPos(ctx, "from");
											BlockPos to = BlockPosArgument.getLoadedBlockPos(ctx, "to");
											Identifier id = IdentifierArgument.getId(ctx, "id");

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
														try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(be.problemPath(), logger)) {
															TagValueOutput output = TagValueOutput.createWithContext(reporter, level.registryAccess());
															be.saveWithId(output);
															nbt = output.buildResult();
														}
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
													.getOrThrow().getAsJsonObject();

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
