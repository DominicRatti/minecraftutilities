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

import java.util.stream.Stream;
import java.util.function.Supplier;
import java.util.Map;
import java.util.HashMap;
import java.util.AbstractMap;

public class LoadItemsProcedure {

	public static void executeProcedure(Map<String, Object> dependencies) {
		if (dependencies.get("world") == null) {
			if (!dependencies.containsKey("world"))
				MinecraftutilitiesMod.LOGGER.warn("Failed to load dependency world for procedure LoadItems!");
			return;
		}
		if (dependencies.get("entity") == null) {
			if (!dependencies.containsKey("entity"))
				MinecraftutilitiesMod.LOGGER.warn("Failed to load dependency entity for procedure LoadItems!");
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
									.orElse(new MinecraftutilitiesModVariables.PlayerVariables())).itemSlot0);
							_setstack.setCount((int) ((((entity.getCapability(MinecraftutilitiesModVariables.PLAYER_VARIABLES_CAPABILITY, null)
									.orElse(new MinecraftutilitiesModVariables.PlayerVariables())).itemSlot0)).getCount()));
							((Slot) ((Map) invobj).get((int) (0))).putStack(_setstack);
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
									.orElse(new MinecraftutilitiesModVariables.PlayerVariables())).itemSlot1);
							_setstack.setCount((int) ((((entity.getCapability(MinecraftutilitiesModVariables.PLAYER_VARIABLES_CAPABILITY, null)
									.orElse(new MinecraftutilitiesModVariables.PlayerVariables())).itemSlot1)).getCount()));
							((Slot) ((Map) invobj).get((int) (1))).putStack(_setstack);
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
									.orElse(new MinecraftutilitiesModVariables.PlayerVariables())).itemSlot2);
							_setstack.setCount((int) ((((entity.getCapability(MinecraftutilitiesModVariables.PLAYER_VARIABLES_CAPABILITY, null)
									.orElse(new MinecraftutilitiesModVariables.PlayerVariables())).itemSlot2)).getCount()));
							((Slot) ((Map) invobj).get((int) (2))).putStack(_setstack);
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
									.orElse(new MinecraftutilitiesModVariables.PlayerVariables())).itemSlot3);
							_setstack.setCount((int) ((((entity.getCapability(MinecraftutilitiesModVariables.PLAYER_VARIABLES_CAPABILITY, null)
									.orElse(new MinecraftutilitiesModVariables.PlayerVariables())).itemSlot3)).getCount()));
							((Slot) ((Map) invobj).get((int) (3))).putStack(_setstack);
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
									.orElse(new MinecraftutilitiesModVariables.PlayerVariables())).itemSlot4);
							_setstack.setCount((int) ((((entity.getCapability(MinecraftutilitiesModVariables.PLAYER_VARIABLES_CAPABILITY, null)
									.orElse(new MinecraftutilitiesModVariables.PlayerVariables())).itemSlot4)).getCount()));
							((Slot) ((Map) invobj).get((int) (4))).putStack(_setstack);
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
									.orElse(new MinecraftutilitiesModVariables.PlayerVariables())).itemSlot5);
							_setstack.setCount((int) ((((entity.getCapability(MinecraftutilitiesModVariables.PLAYER_VARIABLES_CAPABILITY, null)
									.orElse(new MinecraftutilitiesModVariables.PlayerVariables())).itemSlot5)).getCount()));
							((Slot) ((Map) invobj).get((int) (5))).putStack(_setstack);
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
									.orElse(new MinecraftutilitiesModVariables.PlayerVariables())).itemSlot6);
							_setstack.setCount((int) ((((entity.getCapability(MinecraftutilitiesModVariables.PLAYER_VARIABLES_CAPABILITY, null)
									.orElse(new MinecraftutilitiesModVariables.PlayerVariables())).itemSlot6)).getCount()));
							((Slot) ((Map) invobj).get((int) (6))).putStack(_setstack);
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
									.orElse(new MinecraftutilitiesModVariables.PlayerVariables())).itemSlot7);
							_setstack.setCount((int) ((((entity.getCapability(MinecraftutilitiesModVariables.PLAYER_VARIABLES_CAPABILITY, null)
									.orElse(new MinecraftutilitiesModVariables.PlayerVariables())).itemSlot7)).getCount()));
							((Slot) ((Map) invobj).get((int) (7))).putStack(_setstack);
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
									.orElse(new MinecraftutilitiesModVariables.PlayerVariables())).itemSlot8);
							_setstack.setCount((int) ((((entity.getCapability(MinecraftutilitiesModVariables.PLAYER_VARIABLES_CAPABILITY, null)
									.orElse(new MinecraftutilitiesModVariables.PlayerVariables())).itemSlot8)).getCount()));
							((Slot) ((Map) invobj).get((int) (8))).putStack(_setstack);
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
									.orElse(new MinecraftutilitiesModVariables.PlayerVariables())).itemSlot9);
							_setstack.setCount((int) ((((entity.getCapability(MinecraftutilitiesModVariables.PLAYER_VARIABLES_CAPABILITY, null)
									.orElse(new MinecraftutilitiesModVariables.PlayerVariables())).itemSlot9)).getCount()));
							((Slot) ((Map) invobj).get((int) (9))).putStack(_setstack);
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
									.orElse(new MinecraftutilitiesModVariables.PlayerVariables())).itemSlot10);
							_setstack.setCount((int) ((((entity.getCapability(MinecraftutilitiesModVariables.PLAYER_VARIABLES_CAPABILITY, null)
									.orElse(new MinecraftutilitiesModVariables.PlayerVariables())).itemSlot10)).getCount()));
							((Slot) ((Map) invobj).get((int) (10))).putStack(_setstack);
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
									.orElse(new MinecraftutilitiesModVariables.PlayerVariables())).itemSlot11);
							_setstack.setCount((int) ((((entity.getCapability(MinecraftutilitiesModVariables.PLAYER_VARIABLES_CAPABILITY, null)
									.orElse(new MinecraftutilitiesModVariables.PlayerVariables())).itemSlot11)).getCount()));
							((Slot) ((Map) invobj).get((int) (11))).putStack(_setstack);
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
									.orElse(new MinecraftutilitiesModVariables.PlayerVariables())).itemSlot12);
							_setstack.setCount((int) ((((entity.getCapability(MinecraftutilitiesModVariables.PLAYER_VARIABLES_CAPABILITY, null)
									.orElse(new MinecraftutilitiesModVariables.PlayerVariables())).itemSlot12)).getCount()));
							((Slot) ((Map) invobj).get((int) (12))).putStack(_setstack);
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
									.orElse(new MinecraftutilitiesModVariables.PlayerVariables())).itemSlot13);
							_setstack.setCount((int) ((((entity.getCapability(MinecraftutilitiesModVariables.PLAYER_VARIABLES_CAPABILITY, null)
									.orElse(new MinecraftutilitiesModVariables.PlayerVariables())).itemSlot13)).getCount()));
							((Slot) ((Map) invobj).get((int) (13))).putStack(_setstack);
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
									.orElse(new MinecraftutilitiesModVariables.PlayerVariables())).itemSlot14);
							_setstack.setCount((int) ((((entity.getCapability(MinecraftutilitiesModVariables.PLAYER_VARIABLES_CAPABILITY, null)
									.orElse(new MinecraftutilitiesModVariables.PlayerVariables())).itemSlot14)).getCount()));
							((Slot) ((Map) invobj).get((int) (14))).putStack(_setstack);
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
									.orElse(new MinecraftutilitiesModVariables.PlayerVariables())).itemSlot15);
							_setstack.setCount((int) ((((entity.getCapability(MinecraftutilitiesModVariables.PLAYER_VARIABLES_CAPABILITY, null)
									.orElse(new MinecraftutilitiesModVariables.PlayerVariables())).itemSlot15)).getCount()));
							((Slot) ((Map) invobj).get((int) (15))).putStack(_setstack);
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
									.orElse(new MinecraftutilitiesModVariables.PlayerVariables())).itemSlot16);
							_setstack.setCount((int) ((((entity.getCapability(MinecraftutilitiesModVariables.PLAYER_VARIABLES_CAPABILITY, null)
									.orElse(new MinecraftutilitiesModVariables.PlayerVariables())).itemSlot16)).getCount()));
							((Slot) ((Map) invobj).get((int) (16))).putStack(_setstack);
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
									.orElse(new MinecraftutilitiesModVariables.PlayerVariables())).itemSlot17);
							_setstack.setCount((int) ((((entity.getCapability(MinecraftutilitiesModVariables.PLAYER_VARIABLES_CAPABILITY, null)
									.orElse(new MinecraftutilitiesModVariables.PlayerVariables())).itemSlot17)).getCount()));
							((Slot) ((Map) invobj).get((int) (17))).putStack(_setstack);
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
									.orElse(new MinecraftutilitiesModVariables.PlayerVariables())).itemSlot18);
							_setstack.setCount((int) ((((entity.getCapability(MinecraftutilitiesModVariables.PLAYER_VARIABLES_CAPABILITY, null)
									.orElse(new MinecraftutilitiesModVariables.PlayerVariables())).itemSlot18)).getCount()));
							((Slot) ((Map) invobj).get((int) (18))).putStack(_setstack);
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
									.orElse(new MinecraftutilitiesModVariables.PlayerVariables())).itemSlot19);
							_setstack.setCount((int) ((((entity.getCapability(MinecraftutilitiesModVariables.PLAYER_VARIABLES_CAPABILITY, null)
									.orElse(new MinecraftutilitiesModVariables.PlayerVariables())).itemSlot19)).getCount()));
							((Slot) ((Map) invobj).get((int) (19))).putStack(_setstack);
							_current.detectAndSendChanges();
						}
					}
				}
				MinecraftForge.EVENT_BUS.unregister(this);
			}
		}.start(world, (int) 1);

		Loaditem1Procedure.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
				.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
	}
}
