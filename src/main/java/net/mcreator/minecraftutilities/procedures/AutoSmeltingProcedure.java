package net.mcreator.minecraftutilities.procedures;

import net.minecraft.world.World;
import net.minecraft.world.IWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.block.Blocks;

import net.mcreator.minecraftutilities.MinecraftutilitiesMod;

import java.util.Map;

public class AutoSmeltingProcedure {

	public static void executeProcedure(Map<String, Object> dependencies) {
		if (dependencies.get("world") == null) {
			if (!dependencies.containsKey("world"))
				MinecraftutilitiesMod.LOGGER.warn("Failed to load dependency world for procedure AutoSmelting!");
			return;
		}
		if (dependencies.get("x") == null) {
			if (!dependencies.containsKey("x"))
				MinecraftutilitiesMod.LOGGER.warn("Failed to load dependency x for procedure AutoSmelting!");
			return;
		}
		if (dependencies.get("y") == null) {
			if (!dependencies.containsKey("y"))
				MinecraftutilitiesMod.LOGGER.warn("Failed to load dependency y for procedure AutoSmelting!");
			return;
		}
		if (dependencies.get("z") == null) {
			if (!dependencies.containsKey("z"))
				MinecraftutilitiesMod.LOGGER.warn("Failed to load dependency z for procedure AutoSmelting!");
			return;
		}
		IWorld world = (IWorld) dependencies.get("world");
		double x = dependencies.get("x") instanceof Integer ? (int) dependencies.get("x") : (double) dependencies.get("x");
		double y = dependencies.get("y") instanceof Integer ? (int) dependencies.get("y") : (double) dependencies.get("y");
		double z = dependencies.get("z") instanceof Integer ? (int) dependencies.get("z") : (double) dependencies.get("z");
		boolean removeBlock = false;
		if ((world.getBlockState(new BlockPos(x, y, z))).getBlock() == Blocks.STONE) {
			if (world instanceof World && !world.isRemote()) {
				ItemEntity entityToSpawn = new ItemEntity((World) world, (x + 0.5), (y + 0.5), (z + 0.5), new ItemStack(Blocks.STONE));
				entityToSpawn.setPickupDelay((int) 0);
				world.addEntity(entityToSpawn);
			}
			world.setBlockState(new BlockPos(x, y, z), Blocks.AIR.getDefaultState(), 3);
		} else if ((world.getBlockState(new BlockPos(x, y, z))).getBlock() == Blocks.IRON_ORE) {
			if (world instanceof World && !world.isRemote()) {
				ItemEntity entityToSpawn = new ItemEntity((World) world, (x + 0.5), (y + 0.5), (z + 0.5), new ItemStack(Items.IRON_INGOT));
				entityToSpawn.setPickupDelay((int) 1);
				world.addEntity(entityToSpawn);
			}
			world.setBlockState(new BlockPos(x, y, z), Blocks.AIR.getDefaultState(), 3);
		} else if ((world.getBlockState(new BlockPos(x, y, z))).getBlock() == Blocks.GOLD_ORE) {
			if (world instanceof World && !world.isRemote()) {
				ItemEntity entityToSpawn = new ItemEntity((World) world, (x + 0.5), (y + 0.5), (z + 0.5), new ItemStack(Items.GOLD_INGOT));
				entityToSpawn.setPickupDelay((int) 0);
				world.addEntity(entityToSpawn);
			}
			world.setBlockState(new BlockPos(x, y, z), Blocks.AIR.getDefaultState(), 3);
		}
	}
}
