package net.mcreator.minecraftutilities.procedures;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.common.MinecraftForge;

import net.minecraft.world.IWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.inventory.container.Slot;
import net.minecraft.inventory.container.Container;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.Entity;

import net.mcreator.minecraftutilities.MinecraftutilitiesModVariables;
import net.mcreator.minecraftutilities.MinecraftutilitiesMod;

import java.util.function.Supplier;
import java.util.Map;

public class Loaditem1Procedure {

	public static void executeProcedure(Map<String, Object> dependencies) {
		if (dependencies.get("world") == null) {
			if (!dependencies.containsKey("world"))
				MinecraftutilitiesMod.LOGGER.warn("Failed to load dependency world for procedure Loaditem1!");
			return;
		}
		if (dependencies.get("entity") == null) {
			if (!dependencies.containsKey("entity"))
				MinecraftutilitiesMod.LOGGER.warn("Failed to load dependency entity for procedure Loaditem1!");
			return;
		}
		IWorld world = (IWorld) dependencies.get("world");
		Entity entity = (Entity) dependencies.get("entity");
		new Object() {
			private int ticks = 0;
			private float waitTicks;
			private IWorld world;

			public void start(IWorld world, int waitTicks) {
				this.waitTicks = waitTicks;
				MinecraftForge.EVENT_BUS.register(this);
				this.world = world;
			}

			@SubscribeEvent
			public void tick(TickEvent.ServerTickEvent event) {
				if (event.phase == TickEvent.Phase.END) {
					this.ticks += 1;
					if (this.ticks >= this.waitTicks)
						run();
				}
			}

			private void run() {
				if (entity instanceof PlayerEntity) {
					Container _current = ((PlayerEntity) entity).openContainer;
					if (_current instanceof Supplier) {
						Object invobj = ((Supplier) _current).get();
						if (invobj instanceof Map) {
							ItemStack _setstack = ((entity.getCapability(MinecraftutilitiesModVariables.PLAYER_VARIABLES_CAPABILITY, null)
									.orElse(new MinecraftutilitiesModVariables.PlayerVariables())).itemSlot20);
							_setstack.setCount((int) ((((entity.getCapability(MinecraftutilitiesModVariables.PLAYER_VARIABLES_CAPABILITY, null)
									.orElse(new MinecraftutilitiesModVariables.PlayerVariables())).itemSlot20)).getCount()));
							((Slot) ((Map) invobj).get((int) (20))).putStack(_setstack);
							_current.detectAndSendChanges();
						}
					}
				}
				if (entity instanceof PlayerEntity) {
					Container _current = ((PlayerEntity) entity).openContainer;
					if (_current instanceof Supplier) {
						Object invobj = ((Supplier) _current).get();
						if (invobj instanceof Map) {
							ItemStack _setstack = ((entity.getCapability(MinecraftutilitiesModVariables.PLAYER_VARIABLES_CAPABILITY, null)
									.orElse(new MinecraftutilitiesModVariables.PlayerVariables())).itemSlot21);
							_setstack.setCount((int) ((((entity.getCapability(MinecraftutilitiesModVariables.PLAYER_VARIABLES_CAPABILITY, null)
									.orElse(new MinecraftutilitiesModVariables.PlayerVariables())).itemSlot21)).getCount()));
							((Slot) ((Map) invobj).get((int) (21))).putStack(_setstack);
							_current.detectAndSendChanges();
						}
					}
				}
				if (entity instanceof PlayerEntity) {
					Container _current = ((PlayerEntity) entity).openContainer;
					if (_current instanceof Supplier) {
						Object invobj = ((Supplier) _current).get();
						if (invobj instanceof Map) {
							ItemStack _setstack = ((entity.getCapability(MinecraftutilitiesModVariables.PLAYER_VARIABLES_CAPABILITY, null)
									.orElse(new MinecraftutilitiesModVariables.PlayerVariables())).itemSlot22);
							_setstack.setCount((int) ((((entity.getCapability(MinecraftutilitiesModVariables.PLAYER_VARIABLES_CAPABILITY, null)
									.orElse(new MinecraftutilitiesModVariables.PlayerVariables())).itemSlot22)).getCount()));
							((Slot) ((Map) invobj).get((int) (22))).putStack(_setstack);
							_current.detectAndSendChanges();
						}
					}
				}
				if (entity instanceof PlayerEntity) {
					Container _current = ((PlayerEntity) entity).openContainer;
					if (_current instanceof Supplier) {
						Object invobj = ((Supplier) _current).get();
						if (invobj instanceof Map) {
							ItemStack _setstack = ((entity.getCapability(MinecraftutilitiesModVariables.PLAYER_VARIABLES_CAPABILITY, null)
									.orElse(new MinecraftutilitiesModVariables.PlayerVariables())).itemSlot23);
							_setstack.setCount((int) ((((entity.getCapability(MinecraftutilitiesModVariables.PLAYER_VARIABLES_CAPABILITY, null)
									.orElse(new MinecraftutilitiesModVariables.PlayerVariables())).itemSlot23)).getCount()));
							((Slot) ((Map) invobj).get((int) (23))).putStack(_setstack);
							_current.detectAndSendChanges();
						}
					}
				}
				if (entity instanceof PlayerEntity) {
					Container _current = ((PlayerEntity) entity).openContainer;
					if (_current instanceof Supplier) {
						Object invobj = ((Supplier) _current).get();
						if (invobj instanceof Map) {
							ItemStack _setstack = ((entity.getCapability(MinecraftutilitiesModVariables.PLAYER_VARIABLES_CAPABILITY, null)
									.orElse(new MinecraftutilitiesModVariables.PlayerVariables())).itemSlot24);
							_setstack.setCount((int) ((((entity.getCapability(MinecraftutilitiesModVariables.PLAYER_VARIABLES_CAPABILITY, null)
									.orElse(new MinecraftutilitiesModVariables.PlayerVariables())).itemSlot24)).getCount()));
							((Slot) ((Map) invobj).get((int) (24))).putStack(_setstack);
							_current.detectAndSendChanges();
						}
					}
				}
				if (entity instanceof PlayerEntity) {
					Container _current = ((PlayerEntity) entity).openContainer;
					if (_current instanceof Supplier) {
						Object invobj = ((Supplier) _current).get();
						if (invobj instanceof Map) {
							ItemStack _setstack = ((entity.getCapability(MinecraftutilitiesModVariables.PLAYER_VARIABLES_CAPABILITY, null)
									.orElse(new MinecraftutilitiesModVariables.PlayerVariables())).itemSlot25);
							_setstack.setCount((int) ((((entity.getCapability(MinecraftutilitiesModVariables.PLAYER_VARIABLES_CAPABILITY, null)
									.orElse(new MinecraftutilitiesModVariables.PlayerVariables())).itemSlot25)).getCount()));
							((Slot) ((Map) invobj).get((int) (25))).putStack(_setstack);
							_current.detectAndSendChanges();
						}
					}
				}
				if (entity instanceof PlayerEntity) {
					Container _current = ((PlayerEntity) entity).openContainer;
					if (_current instanceof Supplier) {
						Object invobj = ((Supplier) _current).get();
						if (invobj instanceof Map) {
							ItemStack _setstack = ((entity.getCapability(MinecraftutilitiesModVariables.PLAYER_VARIABLES_CAPABILITY, null)
									.orElse(new MinecraftutilitiesModVariables.PlayerVariables())).itemSlot26);
							_setstack.setCount((int) ((((entity.getCapability(MinecraftutilitiesModVariables.PLAYER_VARIABLES_CAPABILITY, null)
									.orElse(new MinecraftutilitiesModVariables.PlayerVariables())).itemSlot26)).getCount()));
							((Slot) ((Map) invobj).get((int) (26))).putStack(_setstack);
							_current.detectAndSendChanges();
						}
					}
				}
				if (entity instanceof PlayerEntity) {
					Container _current = ((PlayerEntity) entity).openContainer;
					if (_current instanceof Supplier) {
						Object invobj = ((Supplier) _current).get();
						if (invobj instanceof Map) {
							ItemStack _setstack = ((entity.getCapability(MinecraftutilitiesModVariables.PLAYER_VARIABLES_CAPABILITY, null)
									.orElse(new MinecraftutilitiesModVariables.PlayerVariables())).itemSlot27);
							_setstack.setCount((int) ((((entity.getCapability(MinecraftutilitiesModVariables.PLAYER_VARIABLES_CAPABILITY, null)
									.orElse(new MinecraftutilitiesModVariables.PlayerVariables())).itemSlot27)).getCount()));
							((Slot) ((Map) invobj).get((int) (27))).putStack(_setstack);
							_current.detectAndSendChanges();
						}
					}
				}
				if (entity instanceof PlayerEntity) {
					Container _current = ((PlayerEntity) entity).openContainer;
					if (_current instanceof Supplier) {
						Object invobj = ((Supplier) _current).get();
						if (invobj instanceof Map) {
							ItemStack _setstack = ((entity.getCapability(MinecraftutilitiesModVariables.PLAYER_VARIABLES_CAPABILITY, null)
									.orElse(new MinecraftutilitiesModVariables.PlayerVariables())).itemSlot28);
							_setstack.setCount((int) ((((entity.getCapability(MinecraftutilitiesModVariables.PLAYER_VARIABLES_CAPABILITY, null)
									.orElse(new MinecraftutilitiesModVariables.PlayerVariables())).itemSlot28)).getCount()));
							((Slot) ((Map) invobj).get((int) (28))).putStack(_setstack);
							_current.detectAndSendChanges();
						}
					}
				}
				if (entity instanceof PlayerEntity) {
					Container _current = ((PlayerEntity) entity).openContainer;
					if (_current instanceof Supplier) {
						Object invobj = ((Supplier) _current).get();
						if (invobj instanceof Map) {
							ItemStack _setstack = ((entity.getCapability(MinecraftutilitiesModVariables.PLAYER_VARIABLES_CAPABILITY, null)
									.orElse(new MinecraftutilitiesModVariables.PlayerVariables())).itemSlot29);
							_setstack.setCount((int) ((((entity.getCapability(MinecraftutilitiesModVariables.PLAYER_VARIABLES_CAPABILITY, null)
									.orElse(new MinecraftutilitiesModVariables.PlayerVariables())).itemSlot29)).getCount()));
							((Slot) ((Map) invobj).get((int) (29))).putStack(_setstack);
							_current.detectAndSendChanges();
						}
					}
				}
				MinecraftForge.EVENT_BUS.unregister(this);
			}
		}.start(world, (int) 1);
	}
}
