package net.mcreator.minecraftutilities.procedures;

import net.minecraft.entity.Entity;

import net.mcreator.minecraftutilities.MinecraftutilitiesModVariables;
import net.mcreator.minecraftutilities.MinecraftutilitiesMod;

import java.util.Map;

public class DropBackpackProcedure {

	public static void executeProcedure(Map<String, Object> dependencies) {
		if (dependencies.get("entity") == null) {
			if (!dependencies.containsKey("entity"))
				MinecraftutilitiesMod.LOGGER.warn("Failed to load dependency entity for procedure DropBackpack!");
			return;
		}
		Entity entity = (Entity) dependencies.get("entity");
		{
			boolean _setval = (false);
			entity.getCapability(MinecraftutilitiesModVariables.PLAYER_VARIABLES_CAPABILITY, null).ifPresent(capability -> {
				capability.hadBackpack = _setval;
				capability.syncPlayerVariables(entity);
			});
		}
	}
}
