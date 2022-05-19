package net.mcreator.minecraftutilities.procedures;

import net.minecraft.world.World;
import net.minecraft.world.IWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.Blocks;
import net.minecraft.block.Block;

import net.mcreator.minecraftutilities.MinecraftutilitiesModVariables;
import net.mcreator.minecraftutilities.MinecraftutilitiesMod;

import java.util.Map;

public class Pickaxe1BlockDestroyedWithToolProcedure {

	public static void executeProcedure(Map<String, Object> dependencies) {
		if (dependencies.get("world") == null) {
			if (!dependencies.containsKey("world"))
				MinecraftutilitiesMod.LOGGER.warn("Failed to load dependency world for procedure Pickaxe1BlockDestroyedWithTool!");
			return;
		}
		if (dependencies.get("x") == null) {
			if (!dependencies.containsKey("x"))
				MinecraftutilitiesMod.LOGGER.warn("Failed to load dependency x for procedure Pickaxe1BlockDestroyedWithTool!");
			return;
		}
		if (dependencies.get("y") == null) {
			if (!dependencies.containsKey("y"))
				MinecraftutilitiesMod.LOGGER.warn("Failed to load dependency y for procedure Pickaxe1BlockDestroyedWithTool!");
			return;
		}
		if (dependencies.get("z") == null) {
			if (!dependencies.containsKey("z"))
				MinecraftutilitiesMod.LOGGER.warn("Failed to load dependency z for procedure Pickaxe1BlockDestroyedWithTool!");
			return;
		}
		IWorld world = (IWorld) dependencies.get("world");
		double x = dependencies.get("x") instanceof Integer ? (int) dependencies.get("x") : (double) dependencies.get("x");
		double y = dependencies.get("y") instanceof Integer ? (int) dependencies.get("y") : (double) dependencies.get("y");
		double z = dependencies.get("z") instanceof Integer ? (int) dependencies.get("z") : (double) dependencies.get("z");
		double posX = 0;
		double posY = 0;
		double posZ = 0;
		MinecraftutilitiesModVariables.pickaxe_x = (x - 1);
		MinecraftutilitiesModVariables.pickaxe_y = (y - 1);
		MinecraftutilitiesModVariables.pickaxe_z = (z - 1);
		for (int index0 = 0; index0 < (int) (3); index0++) {
			for (int index1 = 0; index1 < (int) (3); index1++) {
				for (int index2 = 0; index2 < (int) (3); index2++) {
					if (!((world.getBlockState(new BlockPos(MinecraftutilitiesModVariables.pickaxe_x, MinecraftutilitiesModVariables.pickaxe_y,
							MinecraftutilitiesModVariables.pickaxe_z))).getBlock() == Blocks.BEDROCK
							|| (world.getBlockState(new BlockPos(MinecraftutilitiesModVariables.pickaxe_x, MinecraftutilitiesModVariables.pickaxe_y,
									MinecraftutilitiesModVariables.pickaxe_z))).getBlock() == Blocks.GRASS_BLOCK
							|| (world.getBlockState(new BlockPos(MinecraftutilitiesModVariables.pickaxe_x, MinecraftutilitiesModVariables.pickaxe_y,
									MinecraftutilitiesModVariables.pickaxe_z))).getBlock() == Blocks.GRASS_PATH
							|| (world.getBlockState(new BlockPos(MinecraftutilitiesModVariables.pickaxe_x, MinecraftutilitiesModVariables.pickaxe_y,
									MinecraftutilitiesModVariables.pickaxe_z))).getBlock() == Blocks.MYCELIUM
							|| (world.getBlockState(new BlockPos(MinecraftutilitiesModVariables.pickaxe_x, MinecraftutilitiesModVariables.pickaxe_y,
									MinecraftutilitiesModVariables.pickaxe_z))).getBlock() == Blocks.DIRT
							|| (world.getBlockState(new BlockPos(MinecraftutilitiesModVariables.pickaxe_x, MinecraftutilitiesModVariables.pickaxe_y,
									MinecraftutilitiesModVariables.pickaxe_z))).getBlock() == Blocks.OAK_SAPLING
							|| (world.getBlockState(new BlockPos(MinecraftutilitiesModVariables.pickaxe_x, MinecraftutilitiesModVariables.pickaxe_y,
									MinecraftutilitiesModVariables.pickaxe_z))).getBlock() == Blocks.GRASS
							|| (world.getBlockState(new BlockPos(MinecraftutilitiesModVariables.pickaxe_x, MinecraftutilitiesModVariables.pickaxe_y,
									MinecraftutilitiesModVariables.pickaxe_y))).getBlock() == Blocks.OAK_LOG
							|| (world.getBlockState(new BlockPos(MinecraftutilitiesModVariables.pickaxe_x, MinecraftutilitiesModVariables.pickaxe_y,
									MinecraftutilitiesModVariables.pickaxe_z))).getBlock() == Blocks.ACACIA_LOG
							|| (world.getBlockState(new BlockPos(MinecraftutilitiesModVariables.pickaxe_x, MinecraftutilitiesModVariables.pickaxe_y,
									MinecraftutilitiesModVariables.pickaxe_z))).getBlock() == Blocks.OAK_LEAVES
							|| (world.getBlockState(new BlockPos(MinecraftutilitiesModVariables.pickaxe_x, MinecraftutilitiesModVariables.pickaxe_y,
									MinecraftutilitiesModVariables.pickaxe_z))).getBlock() == Blocks.ACACIA_LEAVES
							|| (world.getBlockState(new BlockPos(MinecraftutilitiesModVariables.pickaxe_x, MinecraftutilitiesModVariables.pickaxe_y,
									MinecraftutilitiesModVariables.pickaxe_z))).getBlock() == Blocks.SAND
							|| (world.getBlockState(new BlockPos(MinecraftutilitiesModVariables.pickaxe_x, MinecraftutilitiesModVariables.pickaxe_y,
									MinecraftutilitiesModVariables.pickaxe_z))).getBlock() == Blocks.SUGAR_CANE
							|| (world.getBlockState(new BlockPos(MinecraftutilitiesModVariables.pickaxe_x, MinecraftutilitiesModVariables.pickaxe_y,
									MinecraftutilitiesModVariables.pickaxe_z))).getBlock() == Blocks.POPPY
							|| (world.getBlockState(new BlockPos(MinecraftutilitiesModVariables.pickaxe_x, MinecraftutilitiesModVariables.pickaxe_y,
									MinecraftutilitiesModVariables.pickaxe_z))).getBlock() == Blocks.DANDELION
							|| (world.getBlockState(new BlockPos(MinecraftutilitiesModVariables.pickaxe_x, MinecraftutilitiesModVariables.pickaxe_y,
									MinecraftutilitiesModVariables.pickaxe_z))).getBlock() == Blocks.SUNFLOWER
							|| (world.getBlockState(new BlockPos(MinecraftutilitiesModVariables.pickaxe_x, MinecraftutilitiesModVariables.pickaxe_y,
									MinecraftutilitiesModVariables.pickaxe_z))).getBlock() == Blocks.RED_MUSHROOM
							|| (world.getBlockState(new BlockPos(MinecraftutilitiesModVariables.pickaxe_x, MinecraftutilitiesModVariables.pickaxe_y,
									MinecraftutilitiesModVariables.pickaxe_z))).getBlock() == Blocks.OAK_PLANKS
							|| (world.getBlockState(new BlockPos(MinecraftutilitiesModVariables.pickaxe_x, MinecraftutilitiesModVariables.pickaxe_y,
									MinecraftutilitiesModVariables.pickaxe_z))).getBlock() == Blocks.LAVA
							|| (world.getBlockState(new BlockPos(MinecraftutilitiesModVariables.pickaxe_x, MinecraftutilitiesModVariables.pickaxe_y,
									MinecraftutilitiesModVariables.pickaxe_z))).getBlock() == Blocks.WATER
							|| (world.getBlockState(new BlockPos(MinecraftutilitiesModVariables.pickaxe_x, MinecraftutilitiesModVariables.pickaxe_y,
									MinecraftutilitiesModVariables.pickaxe_z))).getBlock() == Blocks.BROWN_MUSHROOM
							|| (world.getBlockState(new BlockPos(MinecraftutilitiesModVariables.pickaxe_x, MinecraftutilitiesModVariables.pickaxe_y,
									MinecraftutilitiesModVariables.pickaxe_z))).getBlock() == Blocks.RED_MUSHROOM
							|| (world.getBlockState(new BlockPos(MinecraftutilitiesModVariables.pickaxe_x, MinecraftutilitiesModVariables.pickaxe_y,
									MinecraftutilitiesModVariables.pickaxe_z))).getBlock() == Blocks.OAK_SLAB
							|| (world.getBlockState(new BlockPos(MinecraftutilitiesModVariables.pickaxe_x, MinecraftutilitiesModVariables.pickaxe_y,
									MinecraftutilitiesModVariables.pickaxe_z))).getBlock() == Blocks.PUMPKIN
							|| (world.getBlockState(new BlockPos(MinecraftutilitiesModVariables.pickaxe_x, MinecraftutilitiesModVariables.pickaxe_y,
									MinecraftutilitiesModVariables.pickaxe_z))).getBlock() == Blocks.VINE
							|| (world.getBlockState(new BlockPos(MinecraftutilitiesModVariables.pickaxe_x, MinecraftutilitiesModVariables.pickaxe_y,
									MinecraftutilitiesModVariables.pickaxe_z))).getBlock() == Blocks.BEETROOTS
							|| (world.getBlockState(new BlockPos(MinecraftutilitiesModVariables.pickaxe_x, MinecraftutilitiesModVariables.pickaxe_y,
									MinecraftutilitiesModVariables.pickaxe_z))).getBlock() == Blocks.CARROTS
							|| (world.getBlockState(new BlockPos(MinecraftutilitiesModVariables.pickaxe_x, MinecraftutilitiesModVariables.pickaxe_y,
									MinecraftutilitiesModVariables.pickaxe_z))).getBlock() == Blocks.POTATOES
							|| (world.getBlockState(new BlockPos(MinecraftutilitiesModVariables.pickaxe_x, MinecraftutilitiesModVariables.pickaxe_y,
									MinecraftutilitiesModVariables.pickaxe_z))).getBlock() == Blocks.MELON
							|| (world.getBlockState(new BlockPos(MinecraftutilitiesModVariables.pickaxe_x, MinecraftutilitiesModVariables.pickaxe_y,
									MinecraftutilitiesModVariables.pickaxe_z))).getBlock() == Blocks.NETHER_WART
							|| (world.getBlockState(new BlockPos(MinecraftutilitiesModVariables.pickaxe_x, MinecraftutilitiesModVariables.pickaxe_y,
									MinecraftutilitiesModVariables.pickaxe_z))).getBlock() == Blocks.COCOA)) {
						if (world instanceof World) {
							Block.spawnDrops(
									world.getBlockState(new BlockPos(MinecraftutilitiesModVariables.pickaxe_x,
											MinecraftutilitiesModVariables.pickaxe_y, MinecraftutilitiesModVariables.pickaxe_z)),
									(World) world, new BlockPos(MinecraftutilitiesModVariables.pickaxe_x, MinecraftutilitiesModVariables.pickaxe_y,
											MinecraftutilitiesModVariables.pickaxe_z));
							world.destroyBlock(new BlockPos(MinecraftutilitiesModVariables.pickaxe_x, MinecraftutilitiesModVariables.pickaxe_y,
									MinecraftutilitiesModVariables.pickaxe_z), false);
						}
					}
					MinecraftutilitiesModVariables.pickaxe_y = (MinecraftutilitiesModVariables.pickaxe_y + 1);
				}
				MinecraftutilitiesModVariables.pickaxe_x = (MinecraftutilitiesModVariables.pickaxe_x + 1);
				MinecraftutilitiesModVariables.pickaxe_y = (MinecraftutilitiesModVariables.pickaxe_y - 3);
			}
			MinecraftutilitiesModVariables.pickaxe_z = (MinecraftutilitiesModVariables.pickaxe_z + 1);
			MinecraftutilitiesModVariables.pickaxe_x = (MinecraftutilitiesModVariables.pickaxe_x - 3);
		}
	}
}
