
package net.mcreator.minecraftutilities.gui;

import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.IContainerFactory;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.DeferredWorkQueue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.item.ItemStack;
import net.minecraft.inventory.container.Slot;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Container;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.client.gui.ScreenManager;

import net.mcreator.minecraftutilities.procedures.SaveItemsProcedure;
import net.mcreator.minecraftutilities.procedures.LoadItemsProcedure;
import net.mcreator.minecraftutilities.MinecraftutilitiesModElements;
import net.mcreator.minecraftutilities.MinecraftutilitiesMod;

import java.util.stream.Stream;
import java.util.function.Supplier;
import java.util.Map;
import java.util.HashMap;
import java.util.AbstractMap;

@MinecraftutilitiesModElements.ModElement.Tag
public class BackpackGuiGui extends MinecraftutilitiesModElements.ModElement {
	public static HashMap guistate = new HashMap();
	private static ContainerType<GuiContainerMod> containerType = null;

	public BackpackGuiGui(MinecraftutilitiesModElements instance) {
		super(instance, 18);
		elements.addNetworkMessage(ButtonPressedMessage.class, ButtonPressedMessage::buffer, ButtonPressedMessage::new,
				ButtonPressedMessage::handler);
		elements.addNetworkMessage(GUISlotChangedMessage.class, GUISlotChangedMessage::buffer, GUISlotChangedMessage::new,
				GUISlotChangedMessage::handler);
		containerType = new ContainerType<>(new GuiContainerModFactory());
		FMLJavaModLoadingContext.get().getModEventBus().register(new ContainerRegisterHandler());
	}

	private static class ContainerRegisterHandler {
		@SubscribeEvent
		public void registerContainer(RegistryEvent.Register<ContainerType<?>> event) {
			event.getRegistry().register(containerType.setRegistryName("backpack_gui"));
		}
	}

	@OnlyIn(Dist.CLIENT)
	public void initElements() {
		DeferredWorkQueue.runLater(() -> ScreenManager.registerFactory(containerType, BackpackGuiGuiWindow::new));
	}

	public static class GuiContainerModFactory implements IContainerFactory {
		public GuiContainerMod create(int id, PlayerInventory inv, PacketBuffer extraData) {
			return new GuiContainerMod(id, inv, extraData);
		}
	}

	public static class GuiContainerMod extends Container implements Supplier<Map<Integer, Slot>> {
		World world;
		PlayerEntity entity;
		int x, y, z;
		private IItemHandler internal;
		private Map<Integer, Slot> customSlots = new HashMap<>();
		private boolean bound = false;

		public GuiContainerMod(int id, PlayerInventory inv, PacketBuffer extraData) {
			super(containerType, id);
			this.entity = inv.player;
			this.world = inv.player.world;
			this.internal = new ItemStackHandler(216);
			BlockPos pos = null;
			if (extraData != null) {
				pos = extraData.readBlockPos();
				this.x = pos.getX();
				this.y = pos.getY();
				this.z = pos.getZ();
			}
			if (pos != null) {
				if (extraData.readableBytes() == 1) { // bound to item
					byte hand = extraData.readByte();
					ItemStack itemstack;
					if (hand == 0)
						itemstack = this.entity.getHeldItemMainhand();
					else
						itemstack = this.entity.getHeldItemOffhand();
					itemstack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).ifPresent(capability -> {
						this.internal = capability;
						this.bound = true;
					});
				} else if (extraData.readableBytes() > 1) {
					extraData.readByte(); // drop padding
					Entity entity = world.getEntityByID(extraData.readVarInt());
					if (entity != null)
						entity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).ifPresent(capability -> {
							this.internal = capability;
							this.bound = true;
						});
				} else { // might be bound to block
					TileEntity ent = inv.player != null ? inv.player.world.getTileEntity(pos) : null;
					if (ent != null) {
						ent.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).ifPresent(capability -> {
							this.internal = capability;
							this.bound = true;
						});
					}
				}
			}
			this.customSlots.put(0, this.addSlot(new SlotItemHandler(internal, 0, 5, 5) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(0, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(0, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(0, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(1, this.addSlot(new SlotItemHandler(internal, 1, 23, 5) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(1, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(1, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(1, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(2, this.addSlot(new SlotItemHandler(internal, 2, 41, 5) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(2, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(2, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(2, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(3, this.addSlot(new SlotItemHandler(internal, 3, 59, 5) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(3, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(3, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(3, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(4, this.addSlot(new SlotItemHandler(internal, 4, 77, 5) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(4, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(4, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(4, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(5, this.addSlot(new SlotItemHandler(internal, 5, 95, 5) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(5, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(5, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(5, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(6, this.addSlot(new SlotItemHandler(internal, 6, 113, 5) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(6, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(6, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(6, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(7, this.addSlot(new SlotItemHandler(internal, 7, 131, 5) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(7, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(7, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(7, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(8, this.addSlot(new SlotItemHandler(internal, 8, 149, 5) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(8, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(8, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(8, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(9, this.addSlot(new SlotItemHandler(internal, 9, 167, 5) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(9, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(9, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(9, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(10, this.addSlot(new SlotItemHandler(internal, 10, 185, 5) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(10, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(10, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(10, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(11, this.addSlot(new SlotItemHandler(internal, 11, 203, 5) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(11, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(11, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(11, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(12, this.addSlot(new SlotItemHandler(internal, 12, 221, 5) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(12, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(12, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(12, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(13, this.addSlot(new SlotItemHandler(internal, 13, 239, 5) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(13, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(13, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(13, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(14, this.addSlot(new SlotItemHandler(internal, 14, 257, 5) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(14, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(14, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(14, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(15, this.addSlot(new SlotItemHandler(internal, 15, 275, 5) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(15, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(15, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(15, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(16, this.addSlot(new SlotItemHandler(internal, 16, 293, 5) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(16, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(16, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(16, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(17, this.addSlot(new SlotItemHandler(internal, 17, 311, 5) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(17, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(17, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(17, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(18, this.addSlot(new SlotItemHandler(internal, 18, 329, 5) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(18, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(18, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(18, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(19, this.addSlot(new SlotItemHandler(internal, 19, 347, 5) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(19, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(19, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(19, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(20, this.addSlot(new SlotItemHandler(internal, 20, 365, 5) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(20, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(20, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(20, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(21, this.addSlot(new SlotItemHandler(internal, 21, 383, 5) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(21, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(21, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(21, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(22, this.addSlot(new SlotItemHandler(internal, 22, 401, 5) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(22, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(22, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(22, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(23, this.addSlot(new SlotItemHandler(internal, 23, 419, 5) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(23, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(23, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(23, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(24, this.addSlot(new SlotItemHandler(internal, 24, 5, 23) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(24, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(24, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(24, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(25, this.addSlot(new SlotItemHandler(internal, 25, 23, 23) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(25, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(25, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(25, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(26, this.addSlot(new SlotItemHandler(internal, 26, 41, 23) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(26, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(26, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(26, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(27, this.addSlot(new SlotItemHandler(internal, 27, 59, 23) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(27, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(27, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(27, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(28, this.addSlot(new SlotItemHandler(internal, 28, 77, 23) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(28, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(28, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(28, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(29, this.addSlot(new SlotItemHandler(internal, 29, 95, 23) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(29, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(29, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(29, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(30, this.addSlot(new SlotItemHandler(internal, 30, 113, 23) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(30, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(30, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(30, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(31, this.addSlot(new SlotItemHandler(internal, 31, 131, 23) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(31, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(31, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(31, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(32, this.addSlot(new SlotItemHandler(internal, 32, 149, 23) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(32, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(32, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(32, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(33, this.addSlot(new SlotItemHandler(internal, 33, 167, 23) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(33, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(33, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(33, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(34, this.addSlot(new SlotItemHandler(internal, 34, 185, 23) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(34, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(34, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(34, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(35, this.addSlot(new SlotItemHandler(internal, 35, 203, 23) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(35, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(35, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(35, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(36, this.addSlot(new SlotItemHandler(internal, 36, 221, 23) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(36, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(36, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(36, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(37, this.addSlot(new SlotItemHandler(internal, 37, 239, 23) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(37, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(37, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(37, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(38, this.addSlot(new SlotItemHandler(internal, 38, 257, 23) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(38, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(38, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(38, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(39, this.addSlot(new SlotItemHandler(internal, 39, 275, 23) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(39, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(39, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(39, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(40, this.addSlot(new SlotItemHandler(internal, 40, 293, 23) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(40, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(40, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(40, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(41, this.addSlot(new SlotItemHandler(internal, 41, 311, 23) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(41, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(41, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(41, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(42, this.addSlot(new SlotItemHandler(internal, 42, 329, 23) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(42, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(42, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(42, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(43, this.addSlot(new SlotItemHandler(internal, 43, 347, 23) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(43, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(43, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(43, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(44, this.addSlot(new SlotItemHandler(internal, 44, 365, 23) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(44, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(44, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(44, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(45, this.addSlot(new SlotItemHandler(internal, 45, 383, 23) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(45, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(45, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(45, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(46, this.addSlot(new SlotItemHandler(internal, 46, 401, 23) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(46, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(46, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(46, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(47, this.addSlot(new SlotItemHandler(internal, 47, 419, 23) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(47, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(47, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(47, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(48, this.addSlot(new SlotItemHandler(internal, 48, 5, 41) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(48, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(48, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(48, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(49, this.addSlot(new SlotItemHandler(internal, 49, 23, 41) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(49, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(49, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(49, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(50, this.addSlot(new SlotItemHandler(internal, 50, 41, 41) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(50, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(50, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(50, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(51, this.addSlot(new SlotItemHandler(internal, 51, 59, 41) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(51, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(51, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(51, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(52, this.addSlot(new SlotItemHandler(internal, 52, 77, 41) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(52, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(52, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(52, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(53, this.addSlot(new SlotItemHandler(internal, 53, 95, 41) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(53, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(53, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(53, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(54, this.addSlot(new SlotItemHandler(internal, 54, 113, 41) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(54, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(54, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(54, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(55, this.addSlot(new SlotItemHandler(internal, 55, 131, 41) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(55, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(55, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(55, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(56, this.addSlot(new SlotItemHandler(internal, 56, 149, 41) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(56, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(56, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(56, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(57, this.addSlot(new SlotItemHandler(internal, 57, 167, 41) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(57, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(57, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(57, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(58, this.addSlot(new SlotItemHandler(internal, 58, 185, 41) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(58, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(58, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(58, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(59, this.addSlot(new SlotItemHandler(internal, 59, 203, 41) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(59, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(59, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(59, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(60, this.addSlot(new SlotItemHandler(internal, 60, 221, 41) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(60, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(60, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(60, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(61, this.addSlot(new SlotItemHandler(internal, 61, 239, 41) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(61, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(61, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(61, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(62, this.addSlot(new SlotItemHandler(internal, 62, 257, 41) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(62, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(62, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(62, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(63, this.addSlot(new SlotItemHandler(internal, 63, 275, 41) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(63, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(63, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(63, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(64, this.addSlot(new SlotItemHandler(internal, 64, 293, 41) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(64, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(64, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(64, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(65, this.addSlot(new SlotItemHandler(internal, 65, 311, 41) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(65, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(65, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(65, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(66, this.addSlot(new SlotItemHandler(internal, 66, 329, 41) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(66, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(66, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(66, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(67, this.addSlot(new SlotItemHandler(internal, 67, 347, 41) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(67, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(67, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(67, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(68, this.addSlot(new SlotItemHandler(internal, 68, 365, 41) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(68, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(68, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(68, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(69, this.addSlot(new SlotItemHandler(internal, 69, 383, 41) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(69, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(69, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(69, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(70, this.addSlot(new SlotItemHandler(internal, 70, 401, 41) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(70, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(70, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(70, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(71, this.addSlot(new SlotItemHandler(internal, 71, 419, 41) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(71, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(71, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(71, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(72, this.addSlot(new SlotItemHandler(internal, 72, 5, 59) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(72, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(72, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(72, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(73, this.addSlot(new SlotItemHandler(internal, 73, 23, 59) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(73, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(73, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(73, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(74, this.addSlot(new SlotItemHandler(internal, 74, 41, 59) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(74, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(74, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(74, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(75, this.addSlot(new SlotItemHandler(internal, 75, 59, 59) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(75, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(75, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(75, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(76, this.addSlot(new SlotItemHandler(internal, 76, 77, 59) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(76, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(76, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(76, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(77, this.addSlot(new SlotItemHandler(internal, 77, 95, 59) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(77, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(77, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(77, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(78, this.addSlot(new SlotItemHandler(internal, 78, 113, 59) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(78, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(78, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(78, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(79, this.addSlot(new SlotItemHandler(internal, 79, 131, 59) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(79, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(79, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(79, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(80, this.addSlot(new SlotItemHandler(internal, 80, 149, 59) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(80, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(80, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(80, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(81, this.addSlot(new SlotItemHandler(internal, 81, 167, 59) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(81, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(81, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(81, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(82, this.addSlot(new SlotItemHandler(internal, 82, 185, 59) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(82, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(82, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(82, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(83, this.addSlot(new SlotItemHandler(internal, 83, 203, 59) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(83, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(83, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(83, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(84, this.addSlot(new SlotItemHandler(internal, 84, 221, 59) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(84, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(84, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(84, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(85, this.addSlot(new SlotItemHandler(internal, 85, 239, 59) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(85, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(85, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(85, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(86, this.addSlot(new SlotItemHandler(internal, 86, 257, 59) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(86, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(86, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(86, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(87, this.addSlot(new SlotItemHandler(internal, 87, 275, 59) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(87, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(87, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(87, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(88, this.addSlot(new SlotItemHandler(internal, 88, 293, 59) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(88, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(88, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(88, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(89, this.addSlot(new SlotItemHandler(internal, 89, 311, 59) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(89, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(89, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(89, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(90, this.addSlot(new SlotItemHandler(internal, 90, 329, 59) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(90, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(90, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(90, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(91, this.addSlot(new SlotItemHandler(internal, 91, 347, 59) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(91, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(91, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(91, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(92, this.addSlot(new SlotItemHandler(internal, 92, 365, 59) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(92, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(92, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(92, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(93, this.addSlot(new SlotItemHandler(internal, 93, 383, 59) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(93, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(93, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(93, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(94, this.addSlot(new SlotItemHandler(internal, 94, 401, 59) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(94, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(94, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(94, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(95, this.addSlot(new SlotItemHandler(internal, 95, 419, 59) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(95, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(95, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(95, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(96, this.addSlot(new SlotItemHandler(internal, 96, 5, 77) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(96, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(96, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(96, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(97, this.addSlot(new SlotItemHandler(internal, 97, 23, 77) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(97, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(97, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(97, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(98, this.addSlot(new SlotItemHandler(internal, 98, 41, 77) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(98, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(98, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(98, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(99, this.addSlot(new SlotItemHandler(internal, 99, 59, 77) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(99, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(99, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(99, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(100, this.addSlot(new SlotItemHandler(internal, 100, 77, 77) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(100, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(100, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(100, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(101, this.addSlot(new SlotItemHandler(internal, 101, 95, 77) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(101, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(101, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(101, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(102, this.addSlot(new SlotItemHandler(internal, 102, 113, 77) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(102, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(102, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(102, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(103, this.addSlot(new SlotItemHandler(internal, 103, 131, 77) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(103, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(103, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(103, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(104, this.addSlot(new SlotItemHandler(internal, 104, 149, 77) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(104, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(104, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(104, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(105, this.addSlot(new SlotItemHandler(internal, 105, 167, 77) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(105, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(105, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(105, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(106, this.addSlot(new SlotItemHandler(internal, 106, 185, 77) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(106, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(106, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(106, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(107, this.addSlot(new SlotItemHandler(internal, 107, 203, 77) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(107, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(107, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(107, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(108, this.addSlot(new SlotItemHandler(internal, 108, 221, 77) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(108, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(108, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(108, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(109, this.addSlot(new SlotItemHandler(internal, 109, 239, 77) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(109, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(109, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(109, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(110, this.addSlot(new SlotItemHandler(internal, 110, 257, 77) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(110, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(110, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(110, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(111, this.addSlot(new SlotItemHandler(internal, 111, 275, 77) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(111, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(111, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(111, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(112, this.addSlot(new SlotItemHandler(internal, 112, 293, 77) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(112, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(112, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(112, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(113, this.addSlot(new SlotItemHandler(internal, 113, 311, 77) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(113, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(113, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(113, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(114, this.addSlot(new SlotItemHandler(internal, 114, 329, 77) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(114, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(114, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(114, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(115, this.addSlot(new SlotItemHandler(internal, 115, 347, 77) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(115, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(115, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(115, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(116, this.addSlot(new SlotItemHandler(internal, 116, 365, 77) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(116, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(116, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(116, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(117, this.addSlot(new SlotItemHandler(internal, 117, 383, 77) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(117, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(117, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(117, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(118, this.addSlot(new SlotItemHandler(internal, 118, 401, 77) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(118, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(118, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(118, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(119, this.addSlot(new SlotItemHandler(internal, 119, 419, 77) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(119, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(119, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(119, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(120, this.addSlot(new SlotItemHandler(internal, 120, 5, 95) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(120, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(120, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(120, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(121, this.addSlot(new SlotItemHandler(internal, 121, 23, 95) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(121, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(121, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(121, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(122, this.addSlot(new SlotItemHandler(internal, 122, 41, 95) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(122, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(122, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(122, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(123, this.addSlot(new SlotItemHandler(internal, 123, 59, 95) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(123, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(123, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(123, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(124, this.addSlot(new SlotItemHandler(internal, 124, 77, 95) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(124, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(124, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(124, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(125, this.addSlot(new SlotItemHandler(internal, 125, 95, 95) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(125, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(125, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(125, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(126, this.addSlot(new SlotItemHandler(internal, 126, 113, 95) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(126, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(126, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(126, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(127, this.addSlot(new SlotItemHandler(internal, 127, 131, 95) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(127, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(127, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(127, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(128, this.addSlot(new SlotItemHandler(internal, 128, 149, 95) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(128, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(128, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(128, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(129, this.addSlot(new SlotItemHandler(internal, 129, 167, 95) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(129, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(129, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(129, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(130, this.addSlot(new SlotItemHandler(internal, 130, 185, 95) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(130, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(130, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(130, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(131, this.addSlot(new SlotItemHandler(internal, 131, 203, 95) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(131, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(131, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(131, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(132, this.addSlot(new SlotItemHandler(internal, 132, 221, 95) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(132, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(132, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(132, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(133, this.addSlot(new SlotItemHandler(internal, 133, 239, 95) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(133, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(133, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(133, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(134, this.addSlot(new SlotItemHandler(internal, 134, 257, 95) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(134, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(134, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(134, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(135, this.addSlot(new SlotItemHandler(internal, 135, 275, 95) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(135, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(135, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(135, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(136, this.addSlot(new SlotItemHandler(internal, 136, 293, 95) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(136, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(136, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(136, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(137, this.addSlot(new SlotItemHandler(internal, 137, 311, 95) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(137, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(137, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(137, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(138, this.addSlot(new SlotItemHandler(internal, 138, 329, 95) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(138, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(138, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(138, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(139, this.addSlot(new SlotItemHandler(internal, 139, 347, 95) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(139, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(139, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(139, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(140, this.addSlot(new SlotItemHandler(internal, 140, 365, 95) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(140, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(140, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(140, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(141, this.addSlot(new SlotItemHandler(internal, 141, 383, 95) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(141, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(141, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(141, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(142, this.addSlot(new SlotItemHandler(internal, 142, 401, 95) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(142, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(142, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(142, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(143, this.addSlot(new SlotItemHandler(internal, 143, 419, 95) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(143, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(143, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(143, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(144, this.addSlot(new SlotItemHandler(internal, 144, 5, 113) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(144, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(144, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(144, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(145, this.addSlot(new SlotItemHandler(internal, 145, 23, 113) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(145, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(145, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(145, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(146, this.addSlot(new SlotItemHandler(internal, 146, 41, 113) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(146, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(146, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(146, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(147, this.addSlot(new SlotItemHandler(internal, 147, 59, 113) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(147, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(147, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(147, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(148, this.addSlot(new SlotItemHandler(internal, 148, 77, 113) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(148, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(148, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(148, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(149, this.addSlot(new SlotItemHandler(internal, 149, 95, 113) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(149, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(149, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(149, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(150, this.addSlot(new SlotItemHandler(internal, 150, 113, 113) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(150, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(150, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(150, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(151, this.addSlot(new SlotItemHandler(internal, 151, 131, 113) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(151, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(151, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(151, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(152, this.addSlot(new SlotItemHandler(internal, 152, 149, 113) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(152, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(152, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(152, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(153, this.addSlot(new SlotItemHandler(internal, 153, 167, 113) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(153, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(153, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(153, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(154, this.addSlot(new SlotItemHandler(internal, 154, 185, 113) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(154, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(154, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(154, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(155, this.addSlot(new SlotItemHandler(internal, 155, 203, 113) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(155, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(155, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(155, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(156, this.addSlot(new SlotItemHandler(internal, 156, 221, 113) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(156, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(156, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(156, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(157, this.addSlot(new SlotItemHandler(internal, 157, 239, 113) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(157, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(157, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(157, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(158, this.addSlot(new SlotItemHandler(internal, 158, 257, 113) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(158, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(158, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(158, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(159, this.addSlot(new SlotItemHandler(internal, 159, 275, 113) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(159, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(159, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(159, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(160, this.addSlot(new SlotItemHandler(internal, 160, 293, 113) {
				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(160, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(160, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(161, this.addSlot(new SlotItemHandler(internal, 161, 311, 113) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(161, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(161, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(161, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(162, this.addSlot(new SlotItemHandler(internal, 162, 329, 113) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(162, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(162, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(162, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(163, this.addSlot(new SlotItemHandler(internal, 163, 347, 113) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(163, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(163, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(163, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(164, this.addSlot(new SlotItemHandler(internal, 164, 365, 113) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(164, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(164, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(164, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(165, this.addSlot(new SlotItemHandler(internal, 165, 383, 113) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(165, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(165, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(165, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(166, this.addSlot(new SlotItemHandler(internal, 166, 401, 113) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(166, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(166, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(166, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(167, this.addSlot(new SlotItemHandler(internal, 167, 419, 113) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(167, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(167, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(167, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(168, this.addSlot(new SlotItemHandler(internal, 168, 5, 131) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(168, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(168, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(168, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(169, this.addSlot(new SlotItemHandler(internal, 169, 23, 131) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(169, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(169, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(169, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(170, this.addSlot(new SlotItemHandler(internal, 170, 41, 131) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(170, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(170, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(170, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(171, this.addSlot(new SlotItemHandler(internal, 171, 59, 131) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(171, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(171, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(171, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(172, this.addSlot(new SlotItemHandler(internal, 172, 77, 131) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(172, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(172, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(172, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(173, this.addSlot(new SlotItemHandler(internal, 173, 95, 131) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(173, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(173, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(173, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(174, this.addSlot(new SlotItemHandler(internal, 174, 113, 131) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(174, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(174, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(174, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(175, this.addSlot(new SlotItemHandler(internal, 175, 131, 131) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(175, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(175, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(175, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(176, this.addSlot(new SlotItemHandler(internal, 176, 149, 131) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(176, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(176, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(176, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(177, this.addSlot(new SlotItemHandler(internal, 177, 167, 131) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(177, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(177, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(177, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(178, this.addSlot(new SlotItemHandler(internal, 178, 185, 131) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(178, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(178, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(178, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(179, this.addSlot(new SlotItemHandler(internal, 179, 203, 131) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(179, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(179, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(179, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(180, this.addSlot(new SlotItemHandler(internal, 180, 221, 131) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(180, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(180, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(180, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(181, this.addSlot(new SlotItemHandler(internal, 181, 239, 131) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(181, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(181, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(181, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(182, this.addSlot(new SlotItemHandler(internal, 182, 257, 131) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(182, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(182, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(182, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(183, this.addSlot(new SlotItemHandler(internal, 183, 275, 131) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(183, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(183, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(183, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(184, this.addSlot(new SlotItemHandler(internal, 184, 293, 131) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(184, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(184, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(184, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(185, this.addSlot(new SlotItemHandler(internal, 185, 311, 131) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(185, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(185, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(185, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(186, this.addSlot(new SlotItemHandler(internal, 186, 329, 131) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(186, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(186, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(186, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(187, this.addSlot(new SlotItemHandler(internal, 187, 347, 131) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(187, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(187, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(187, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(188, this.addSlot(new SlotItemHandler(internal, 188, 365, 131) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(188, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(188, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(188, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(189, this.addSlot(new SlotItemHandler(internal, 189, 383, 131) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(189, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(189, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(189, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(190, this.addSlot(new SlotItemHandler(internal, 190, 401, 131) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(190, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(190, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(190, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(191, this.addSlot(new SlotItemHandler(internal, 191, 419, 131) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(191, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(191, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(191, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(192, this.addSlot(new SlotItemHandler(internal, 192, 5, 149) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(192, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(192, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(192, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(193, this.addSlot(new SlotItemHandler(internal, 193, 23, 149) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(193, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(193, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(193, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(194, this.addSlot(new SlotItemHandler(internal, 194, 41, 149) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(194, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(194, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(194, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(195, this.addSlot(new SlotItemHandler(internal, 195, 59, 149) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(195, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(195, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(195, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(196, this.addSlot(new SlotItemHandler(internal, 196, 77, 149) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(196, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(196, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(196, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(197, this.addSlot(new SlotItemHandler(internal, 197, 95, 149) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(197, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(197, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(197, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(198, this.addSlot(new SlotItemHandler(internal, 198, 113, 149) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(198, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(198, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(198, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(199, this.addSlot(new SlotItemHandler(internal, 199, 131, 149) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(199, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(199, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(199, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(200, this.addSlot(new SlotItemHandler(internal, 200, 149, 149) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(200, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(200, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(200, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(201, this.addSlot(new SlotItemHandler(internal, 201, 167, 149) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(201, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(201, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(201, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(202, this.addSlot(new SlotItemHandler(internal, 202, 185, 149) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(202, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(202, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(202, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(203, this.addSlot(new SlotItemHandler(internal, 203, 203, 149) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(203, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(203, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(203, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(204, this.addSlot(new SlotItemHandler(internal, 204, 221, 149) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(204, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(204, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(204, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(205, this.addSlot(new SlotItemHandler(internal, 205, 239, 149) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(205, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(205, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(205, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(206, this.addSlot(new SlotItemHandler(internal, 206, 257, 149) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(206, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(206, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(206, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(207, this.addSlot(new SlotItemHandler(internal, 207, 275, 149) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(207, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(207, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(207, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(208, this.addSlot(new SlotItemHandler(internal, 208, 293, 149) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(208, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(208, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(208, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(209, this.addSlot(new SlotItemHandler(internal, 209, 311, 149) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(209, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(209, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(209, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(210, this.addSlot(new SlotItemHandler(internal, 210, 329, 149) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(210, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(210, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(210, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(211, this.addSlot(new SlotItemHandler(internal, 211, 347, 149) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(211, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(211, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(211, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(212, this.addSlot(new SlotItemHandler(internal, 212, 365, 149) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(212, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(212, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(212, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(213, this.addSlot(new SlotItemHandler(internal, 213, 383, 149) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(213, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(213, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(213, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(214, this.addSlot(new SlotItemHandler(internal, 214, 401, 149) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(214, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(214, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(214, 2, b.getCount() - a.getCount());
				}
			}));
			this.customSlots.put(215, this.addSlot(new SlotItemHandler(internal, 215, 419, 149) {
				@Override
				public void onSlotChanged() {
					super.onSlotChanged();
					GuiContainerMod.this.slotChanged(215, 0, 0);
				}

				@Override
				public ItemStack onTake(PlayerEntity entity, ItemStack stack) {
					ItemStack retval = super.onTake(entity, stack);
					GuiContainerMod.this.slotChanged(215, 1, 0);
					return retval;
				}

				@Override
				public void onSlotChange(ItemStack a, ItemStack b) {
					super.onSlotChange(a, b);
					GuiContainerMod.this.slotChanged(215, 2, b.getCount() - a.getCount());
				}
			}));
			int si;
			int sj;
			for (si = 0; si < 3; ++si)
				for (sj = 0; sj < 9; ++sj)
					this.addSlot(new Slot(inv, sj + (si + 1) * 9, 123 + 8 + sj * 18, 84 + 84 + si * 18));
			for (si = 0; si < 9; ++si)
				this.addSlot(new Slot(inv, si, 123 + 8 + si * 18, 84 + 142));

			LoadItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}

		public Map<Integer, Slot> get() {
			return customSlots;
		}

		@Override
		public boolean canInteractWith(PlayerEntity player) {
			return true;
		}

		@Override
		public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
			ItemStack itemstack = ItemStack.EMPTY;
			Slot slot = (Slot) this.inventorySlots.get(index);
			if (slot != null && slot.getHasStack()) {
				ItemStack itemstack1 = slot.getStack();
				itemstack = itemstack1.copy();
				if (index < 216) {
					if (!this.mergeItemStack(itemstack1, 216, this.inventorySlots.size(), true)) {
						return ItemStack.EMPTY;
					}
					slot.onSlotChange(itemstack1, itemstack);
				} else if (!this.mergeItemStack(itemstack1, 0, 216, false)) {
					if (index < 216 + 27) {
						if (!this.mergeItemStack(itemstack1, 216 + 27, this.inventorySlots.size(), true)) {
							return ItemStack.EMPTY;
						}
					} else {
						if (!this.mergeItemStack(itemstack1, 216, 216 + 27, false)) {
							return ItemStack.EMPTY;
						}
					}
					return ItemStack.EMPTY;
				}
				if (itemstack1.getCount() == 0) {
					slot.putStack(ItemStack.EMPTY);
				} else {
					slot.onSlotChanged();
				}
				if (itemstack1.getCount() == itemstack.getCount()) {
					return ItemStack.EMPTY;
				}
				slot.onTake(playerIn, itemstack1);
			}
			return itemstack;
		}

		@Override /** 
					* Merges provided ItemStack with the first avaliable one in the container/player inventor between minIndex (included) and maxIndex (excluded). Args : stack, minIndex, maxIndex, negativDirection. /!\ the Container implementation do not check if the item is valid for the slot
					*/
		protected boolean mergeItemStack(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection) {
			boolean flag = false;
			int i = startIndex;
			if (reverseDirection) {
				i = endIndex - 1;
			}
			if (stack.isStackable()) {
				while (!stack.isEmpty()) {
					if (reverseDirection) {
						if (i < startIndex) {
							break;
						}
					} else if (i >= endIndex) {
						break;
					}
					Slot slot = this.inventorySlots.get(i);
					ItemStack itemstack = slot.getStack();
					if (slot.isItemValid(itemstack) && !itemstack.isEmpty() && areItemsAndTagsEqual(stack, itemstack)) {
						int j = itemstack.getCount() + stack.getCount();
						int maxSize = Math.min(slot.getSlotStackLimit(), stack.getMaxStackSize());
						if (j <= maxSize) {
							stack.setCount(0);
							itemstack.setCount(j);
							slot.putStack(itemstack);
							flag = true;
						} else if (itemstack.getCount() < maxSize) {
							stack.shrink(maxSize - itemstack.getCount());
							itemstack.setCount(maxSize);
							slot.putStack(itemstack);
							flag = true;
						}
					}
					if (reverseDirection) {
						--i;
					} else {
						++i;
					}
				}
			}
			if (!stack.isEmpty()) {
				if (reverseDirection) {
					i = endIndex - 1;
				} else {
					i = startIndex;
				}
				while (true) {
					if (reverseDirection) {
						if (i < startIndex) {
							break;
						}
					} else if (i >= endIndex) {
						break;
					}
					Slot slot1 = this.inventorySlots.get(i);
					ItemStack itemstack1 = slot1.getStack();
					if (itemstack1.isEmpty() && slot1.isItemValid(stack)) {
						if (stack.getCount() > slot1.getSlotStackLimit()) {
							slot1.putStack(stack.split(slot1.getSlotStackLimit()));
						} else {
							slot1.putStack(stack.split(stack.getCount()));
						}
						slot1.onSlotChanged();
						flag = true;
						break;
					}
					if (reverseDirection) {
						--i;
					} else {
						++i;
					}
				}
			}
			return flag;
		}

		@Override
		public void onContainerClosed(PlayerEntity playerIn) {
			super.onContainerClosed(playerIn);
			if (!bound && (playerIn instanceof ServerPlayerEntity)) {
				if (!playerIn.isAlive() || playerIn instanceof ServerPlayerEntity && ((ServerPlayerEntity) playerIn).hasDisconnected()) {
					for (int j = 0; j < internal.getSlots(); ++j) {
						if (j == 0)
							continue;
						if (j == 1)
							continue;
						if (j == 2)
							continue;
						if (j == 3)
							continue;
						if (j == 4)
							continue;
						if (j == 5)
							continue;
						if (j == 6)
							continue;
						if (j == 7)
							continue;
						if (j == 8)
							continue;
						if (j == 9)
							continue;
						if (j == 10)
							continue;
						if (j == 11)
							continue;
						if (j == 12)
							continue;
						if (j == 13)
							continue;
						if (j == 14)
							continue;
						if (j == 15)
							continue;
						if (j == 16)
							continue;
						if (j == 17)
							continue;
						if (j == 18)
							continue;
						if (j == 19)
							continue;
						if (j == 21)
							continue;
						if (j == 22)
							continue;
						if (j == 23)
							continue;
						if (j == 24)
							continue;
						if (j == 25)
							continue;
						if (j == 26)
							continue;
						if (j == 28)
							continue;
						if (j == 29)
							continue;
						if (j == 30)
							continue;
						if (j == 31)
							continue;
						if (j == 32)
							continue;
						if (j == 33)
							continue;
						if (j == 34)
							continue;
						if (j == 35)
							continue;
						if (j == 36)
							continue;
						if (j == 37)
							continue;
						if (j == 38)
							continue;
						if (j == 39)
							continue;
						if (j == 40)
							continue;
						if (j == 42)
							continue;
						if (j == 43)
							continue;
						if (j == 44)
							continue;
						if (j == 45)
							continue;
						if (j == 46)
							continue;
						if (j == 47)
							continue;
						if (j == 48)
							continue;
						if (j == 49)
							continue;
						if (j == 50)
							continue;
						if (j == 51)
							continue;
						if (j == 52)
							continue;
						if (j == 53)
							continue;
						if (j == 54)
							continue;
						if (j == 55)
							continue;
						if (j == 56)
							continue;
						if (j == 57)
							continue;
						if (j == 58)
							continue;
						if (j == 59)
							continue;
						if (j == 60)
							continue;
						if (j == 61)
							continue;
						if (j == 62)
							continue;
						if (j == 63)
							continue;
						if (j == 64)
							continue;
						if (j == 65)
							continue;
						if (j == 66)
							continue;
						if (j == 67)
							continue;
						if (j == 68)
							continue;
						if (j == 69)
							continue;
						if (j == 70)
							continue;
						if (j == 71)
							continue;
						if (j == 72)
							continue;
						if (j == 73)
							continue;
						if (j == 74)
							continue;
						if (j == 75)
							continue;
						if (j == 76)
							continue;
						if (j == 77)
							continue;
						if (j == 78)
							continue;
						if (j == 79)
							continue;
						if (j == 80)
							continue;
						if (j == 81)
							continue;
						if (j == 82)
							continue;
						if (j == 84)
							continue;
						if (j == 85)
							continue;
						if (j == 86)
							continue;
						if (j == 87)
							continue;
						if (j == 88)
							continue;
						if (j == 89)
							continue;
						if (j == 90)
							continue;
						if (j == 91)
							continue;
						if (j == 92)
							continue;
						if (j == 93)
							continue;
						if (j == 94)
							continue;
						if (j == 95)
							continue;
						if (j == 96)
							continue;
						if (j == 97)
							continue;
						if (j == 98)
							continue;
						if (j == 99)
							continue;
						if (j == 100)
							continue;
						if (j == 101)
							continue;
						if (j == 102)
							continue;
						if (j == 103)
							continue;
						if (j == 104)
							continue;
						if (j == 105)
							continue;
						if (j == 106)
							continue;
						if (j == 107)
							continue;
						if (j == 108)
							continue;
						if (j == 109)
							continue;
						if (j == 110)
							continue;
						if (j == 111)
							continue;
						if (j == 112)
							continue;
						if (j == 113)
							continue;
						if (j == 114)
							continue;
						if (j == 115)
							continue;
						if (j == 116)
							continue;
						if (j == 117)
							continue;
						if (j == 118)
							continue;
						if (j == 119)
							continue;
						if (j == 120)
							continue;
						if (j == 121)
							continue;
						if (j == 122)
							continue;
						if (j == 123)
							continue;
						if (j == 124)
							continue;
						if (j == 125)
							continue;
						if (j == 126)
							continue;
						if (j == 127)
							continue;
						if (j == 128)
							continue;
						if (j == 129)
							continue;
						if (j == 130)
							continue;
						if (j == 131)
							continue;
						if (j == 132)
							continue;
						if (j == 133)
							continue;
						if (j == 134)
							continue;
						if (j == 135)
							continue;
						if (j == 136)
							continue;
						if (j == 137)
							continue;
						if (j == 138)
							continue;
						if (j == 139)
							continue;
						if (j == 140)
							continue;
						if (j == 141)
							continue;
						if (j == 142)
							continue;
						if (j == 143)
							continue;
						if (j == 144)
							continue;
						if (j == 145)
							continue;
						if (j == 146)
							continue;
						if (j == 147)
							continue;
						if (j == 148)
							continue;
						if (j == 149)
							continue;
						if (j == 150)
							continue;
						if (j == 151)
							continue;
						if (j == 152)
							continue;
						if (j == 153)
							continue;
						if (j == 154)
							continue;
						if (j == 155)
							continue;
						if (j == 157)
							continue;
						if (j == 158)
							continue;
						if (j == 159)
							continue;
						if (j == 160)
							continue;
						if (j == 161)
							continue;
						if (j == 162)
							continue;
						if (j == 163)
							continue;
						if (j == 164)
							continue;
						if (j == 165)
							continue;
						if (j == 166)
							continue;
						if (j == 167)
							continue;
						if (j == 168)
							continue;
						if (j == 169)
							continue;
						if (j == 170)
							continue;
						if (j == 171)
							continue;
						if (j == 172)
							continue;
						if (j == 173)
							continue;
						if (j == 174)
							continue;
						if (j == 175)
							continue;
						if (j == 176)
							continue;
						if (j == 177)
							continue;
						if (j == 178)
							continue;
						if (j == 179)
							continue;
						if (j == 180)
							continue;
						if (j == 181)
							continue;
						if (j == 182)
							continue;
						if (j == 183)
							continue;
						if (j == 184)
							continue;
						if (j == 185)
							continue;
						if (j == 186)
							continue;
						if (j == 187)
							continue;
						if (j == 188)
							continue;
						if (j == 189)
							continue;
						if (j == 190)
							continue;
						if (j == 191)
							continue;
						if (j == 192)
							continue;
						if (j == 193)
							continue;
						if (j == 194)
							continue;
						if (j == 195)
							continue;
						if (j == 196)
							continue;
						if (j == 197)
							continue;
						if (j == 198)
							continue;
						if (j == 200)
							continue;
						if (j == 201)
							continue;
						if (j == 202)
							continue;
						if (j == 203)
							continue;
						if (j == 204)
							continue;
						if (j == 205)
							continue;
						if (j == 206)
							continue;
						if (j == 207)
							continue;
						if (j == 208)
							continue;
						if (j == 209)
							continue;
						if (j == 210)
							continue;
						if (j == 211)
							continue;
						if (j == 212)
							continue;
						if (j == 213)
							continue;
						if (j == 214)
							continue;
						if (j == 215)
							continue;
						playerIn.dropItem(internal.extractItem(j, internal.getStackInSlot(j).getCount(), false), false);
					}
				} else {
					for (int i = 0; i < internal.getSlots(); ++i) {
						if (i == 0)
							continue;
						if (i == 1)
							continue;
						if (i == 2)
							continue;
						if (i == 3)
							continue;
						if (i == 4)
							continue;
						if (i == 5)
							continue;
						if (i == 6)
							continue;
						if (i == 7)
							continue;
						if (i == 8)
							continue;
						if (i == 9)
							continue;
						if (i == 10)
							continue;
						if (i == 11)
							continue;
						if (i == 12)
							continue;
						if (i == 13)
							continue;
						if (i == 14)
							continue;
						if (i == 15)
							continue;
						if (i == 16)
							continue;
						if (i == 17)
							continue;
						if (i == 18)
							continue;
						if (i == 19)
							continue;
						if (i == 21)
							continue;
						if (i == 22)
							continue;
						if (i == 23)
							continue;
						if (i == 24)
							continue;
						if (i == 25)
							continue;
						if (i == 26)
							continue;
						if (i == 28)
							continue;
						if (i == 29)
							continue;
						if (i == 30)
							continue;
						if (i == 31)
							continue;
						if (i == 32)
							continue;
						if (i == 33)
							continue;
						if (i == 34)
							continue;
						if (i == 35)
							continue;
						if (i == 36)
							continue;
						if (i == 37)
							continue;
						if (i == 38)
							continue;
						if (i == 39)
							continue;
						if (i == 40)
							continue;
						if (i == 42)
							continue;
						if (i == 43)
							continue;
						if (i == 44)
							continue;
						if (i == 45)
							continue;
						if (i == 46)
							continue;
						if (i == 47)
							continue;
						if (i == 48)
							continue;
						if (i == 49)
							continue;
						if (i == 50)
							continue;
						if (i == 51)
							continue;
						if (i == 52)
							continue;
						if (i == 53)
							continue;
						if (i == 54)
							continue;
						if (i == 55)
							continue;
						if (i == 56)
							continue;
						if (i == 57)
							continue;
						if (i == 58)
							continue;
						if (i == 59)
							continue;
						if (i == 60)
							continue;
						if (i == 61)
							continue;
						if (i == 62)
							continue;
						if (i == 63)
							continue;
						if (i == 64)
							continue;
						if (i == 65)
							continue;
						if (i == 66)
							continue;
						if (i == 67)
							continue;
						if (i == 68)
							continue;
						if (i == 69)
							continue;
						if (i == 70)
							continue;
						if (i == 71)
							continue;
						if (i == 72)
							continue;
						if (i == 73)
							continue;
						if (i == 74)
							continue;
						if (i == 75)
							continue;
						if (i == 76)
							continue;
						if (i == 77)
							continue;
						if (i == 78)
							continue;
						if (i == 79)
							continue;
						if (i == 80)
							continue;
						if (i == 81)
							continue;
						if (i == 82)
							continue;
						if (i == 84)
							continue;
						if (i == 85)
							continue;
						if (i == 86)
							continue;
						if (i == 87)
							continue;
						if (i == 88)
							continue;
						if (i == 89)
							continue;
						if (i == 90)
							continue;
						if (i == 91)
							continue;
						if (i == 92)
							continue;
						if (i == 93)
							continue;
						if (i == 94)
							continue;
						if (i == 95)
							continue;
						if (i == 96)
							continue;
						if (i == 97)
							continue;
						if (i == 98)
							continue;
						if (i == 99)
							continue;
						if (i == 100)
							continue;
						if (i == 101)
							continue;
						if (i == 102)
							continue;
						if (i == 103)
							continue;
						if (i == 104)
							continue;
						if (i == 105)
							continue;
						if (i == 106)
							continue;
						if (i == 107)
							continue;
						if (i == 108)
							continue;
						if (i == 109)
							continue;
						if (i == 110)
							continue;
						if (i == 111)
							continue;
						if (i == 112)
							continue;
						if (i == 113)
							continue;
						if (i == 114)
							continue;
						if (i == 115)
							continue;
						if (i == 116)
							continue;
						if (i == 117)
							continue;
						if (i == 118)
							continue;
						if (i == 119)
							continue;
						if (i == 120)
							continue;
						if (i == 121)
							continue;
						if (i == 122)
							continue;
						if (i == 123)
							continue;
						if (i == 124)
							continue;
						if (i == 125)
							continue;
						if (i == 126)
							continue;
						if (i == 127)
							continue;
						if (i == 128)
							continue;
						if (i == 129)
							continue;
						if (i == 130)
							continue;
						if (i == 131)
							continue;
						if (i == 132)
							continue;
						if (i == 133)
							continue;
						if (i == 134)
							continue;
						if (i == 135)
							continue;
						if (i == 136)
							continue;
						if (i == 137)
							continue;
						if (i == 138)
							continue;
						if (i == 139)
							continue;
						if (i == 140)
							continue;
						if (i == 141)
							continue;
						if (i == 142)
							continue;
						if (i == 143)
							continue;
						if (i == 144)
							continue;
						if (i == 145)
							continue;
						if (i == 146)
							continue;
						if (i == 147)
							continue;
						if (i == 148)
							continue;
						if (i == 149)
							continue;
						if (i == 150)
							continue;
						if (i == 151)
							continue;
						if (i == 152)
							continue;
						if (i == 153)
							continue;
						if (i == 154)
							continue;
						if (i == 155)
							continue;
						if (i == 157)
							continue;
						if (i == 158)
							continue;
						if (i == 159)
							continue;
						if (i == 160)
							continue;
						if (i == 161)
							continue;
						if (i == 162)
							continue;
						if (i == 163)
							continue;
						if (i == 164)
							continue;
						if (i == 165)
							continue;
						if (i == 166)
							continue;
						if (i == 167)
							continue;
						if (i == 168)
							continue;
						if (i == 169)
							continue;
						if (i == 170)
							continue;
						if (i == 171)
							continue;
						if (i == 172)
							continue;
						if (i == 173)
							continue;
						if (i == 174)
							continue;
						if (i == 175)
							continue;
						if (i == 176)
							continue;
						if (i == 177)
							continue;
						if (i == 178)
							continue;
						if (i == 179)
							continue;
						if (i == 180)
							continue;
						if (i == 181)
							continue;
						if (i == 182)
							continue;
						if (i == 183)
							continue;
						if (i == 184)
							continue;
						if (i == 185)
							continue;
						if (i == 186)
							continue;
						if (i == 187)
							continue;
						if (i == 188)
							continue;
						if (i == 189)
							continue;
						if (i == 190)
							continue;
						if (i == 191)
							continue;
						if (i == 192)
							continue;
						if (i == 193)
							continue;
						if (i == 194)
							continue;
						if (i == 195)
							continue;
						if (i == 196)
							continue;
						if (i == 197)
							continue;
						if (i == 198)
							continue;
						if (i == 200)
							continue;
						if (i == 201)
							continue;
						if (i == 202)
							continue;
						if (i == 203)
							continue;
						if (i == 204)
							continue;
						if (i == 205)
							continue;
						if (i == 206)
							continue;
						if (i == 207)
							continue;
						if (i == 208)
							continue;
						if (i == 209)
							continue;
						if (i == 210)
							continue;
						if (i == 211)
							continue;
						if (i == 212)
							continue;
						if (i == 213)
							continue;
						if (i == 214)
							continue;
						if (i == 215)
							continue;
						playerIn.inventory.placeItemBackInInventory(playerIn.world,
								internal.extractItem(i, internal.getStackInSlot(i).getCount(), false));
					}
				}
			}
		}

		private void slotChanged(int slotid, int ctype, int meta) {
			if (this.world != null && this.world.isRemote()) {
				MinecraftutilitiesMod.PACKET_HANDLER.sendToServer(new GUISlotChangedMessage(slotid, x, y, z, ctype, meta));
				handleSlotAction(entity, slotid, ctype, meta, x, y, z);
			}
		}
	}

	public static class ButtonPressedMessage {
		int buttonID, x, y, z;

		public ButtonPressedMessage(PacketBuffer buffer) {
			this.buttonID = buffer.readInt();
			this.x = buffer.readInt();
			this.y = buffer.readInt();
			this.z = buffer.readInt();
		}

		public ButtonPressedMessage(int buttonID, int x, int y, int z) {
			this.buttonID = buttonID;
			this.x = x;
			this.y = y;
			this.z = z;
		}

		public static void buffer(ButtonPressedMessage message, PacketBuffer buffer) {
			buffer.writeInt(message.buttonID);
			buffer.writeInt(message.x);
			buffer.writeInt(message.y);
			buffer.writeInt(message.z);
		}

		public static void handler(ButtonPressedMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
			NetworkEvent.Context context = contextSupplier.get();
			context.enqueueWork(() -> {
				PlayerEntity entity = context.getSender();
				int buttonID = message.buttonID;
				int x = message.x;
				int y = message.y;
				int z = message.z;
				handleButtonAction(entity, buttonID, x, y, z);
			});
			context.setPacketHandled(true);
		}
	}

	public static class GUISlotChangedMessage {
		int slotID, x, y, z, changeType, meta;

		public GUISlotChangedMessage(int slotID, int x, int y, int z, int changeType, int meta) {
			this.slotID = slotID;
			this.x = x;
			this.y = y;
			this.z = z;
			this.changeType = changeType;
			this.meta = meta;
		}

		public GUISlotChangedMessage(PacketBuffer buffer) {
			this.slotID = buffer.readInt();
			this.x = buffer.readInt();
			this.y = buffer.readInt();
			this.z = buffer.readInt();
			this.changeType = buffer.readInt();
			this.meta = buffer.readInt();
		}

		public static void buffer(GUISlotChangedMessage message, PacketBuffer buffer) {
			buffer.writeInt(message.slotID);
			buffer.writeInt(message.x);
			buffer.writeInt(message.y);
			buffer.writeInt(message.z);
			buffer.writeInt(message.changeType);
			buffer.writeInt(message.meta);
		}

		public static void handler(GUISlotChangedMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
			NetworkEvent.Context context = contextSupplier.get();
			context.enqueueWork(() -> {
				PlayerEntity entity = context.getSender();
				int slotID = message.slotID;
				int changeType = message.changeType;
				int meta = message.meta;
				int x = message.x;
				int y = message.y;
				int z = message.z;
				handleSlotAction(entity, slotID, changeType, meta, x, y, z);
			});
			context.setPacketHandled(true);
		}
	}

	static void handleButtonAction(PlayerEntity entity, int buttonID, int x, int y, int z) {
		World world = entity.world;
		// security measure to prevent arbitrary chunk generation
		if (!world.isBlockLoaded(new BlockPos(x, y, z)))
			return;
	}

	private static void handleSlotAction(PlayerEntity entity, int slotID, int changeType, int meta, int x, int y, int z) {
		World world = entity.world;
		// security measure to prevent arbitrary chunk generation
		if (!world.isBlockLoaded(new BlockPos(x, y, z)))
			return;
		if (slotID == 0 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 0 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 0 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 1 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 1 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 1 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 2 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 2 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 2 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 3 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 3 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 3 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 4 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 4 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 4 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 5 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 5 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 5 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 6 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 6 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 6 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 7 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 7 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 7 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 8 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 8 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 8 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 9 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 9 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 9 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 10 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 10 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 10 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 11 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 11 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 11 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 12 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 12 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 12 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 13 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 13 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 13 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 14 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 14 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 14 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 15 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 15 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 15 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 16 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 16 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 16 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 17 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 17 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 17 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 18 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 18 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 18 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 19 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 19 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 19 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 20 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 20 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 20 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 21 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 21 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 21 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 22 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 22 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 22 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 23 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 23 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 23 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 24 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 24 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 24 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 25 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 25 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 25 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 26 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 26 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 26 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 27 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 27 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 27 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 28 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 28 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 28 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 29 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 29 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 29 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 30 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 30 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 30 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 31 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 31 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 31 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 32 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 32 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 32 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 33 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 33 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 33 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 34 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 34 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 34 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 35 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 35 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 35 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 36 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 36 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 36 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 37 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 37 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 37 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 38 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 38 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 38 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 39 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 39 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 39 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 40 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 40 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 40 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 41 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 41 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 41 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 42 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 42 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 42 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 43 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 43 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 43 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 44 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 44 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 44 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 45 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 45 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 45 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 46 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 46 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 46 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 47 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 47 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 47 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 48 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 48 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 48 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 49 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 49 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 49 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 50 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 50 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 50 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 51 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 51 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 51 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 52 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 52 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 52 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 53 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 53 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 53 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 54 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 54 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 54 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 55 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 55 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 55 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 56 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 56 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 56 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 57 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 57 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 57 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 58 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 58 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 58 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 59 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 59 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 59 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 60 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 60 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 60 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 61 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 61 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 61 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 62 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 62 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 62 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 63 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 63 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 63 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 64 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 64 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 64 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 65 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 65 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 65 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 66 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 66 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 66 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 67 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 67 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 67 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 68 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 68 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 68 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 69 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 69 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 69 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 70 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 70 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 70 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 71 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 71 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 71 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 72 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 72 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 72 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 73 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 73 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 73 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 74 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 74 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 74 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 75 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 75 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 75 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 76 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 76 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 76 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 77 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 77 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 77 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 78 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 78 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 78 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 79 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 79 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 79 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 80 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 80 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 80 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 81 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 81 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 81 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 82 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 82 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 82 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 83 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 83 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 83 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 84 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 84 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 84 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 85 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 85 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 85 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 86 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 86 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 86 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 87 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 87 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 87 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 88 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 88 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 88 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 89 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 89 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 89 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 90 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 90 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 90 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 91 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 91 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 91 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 92 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 92 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 92 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 93 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 93 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 93 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 94 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 94 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 94 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 95 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 95 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 95 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 96 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 96 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 96 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 97 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 97 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 97 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 98 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 98 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 98 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 99 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 99 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 99 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 100 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 100 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 100 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 101 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 101 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 101 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 102 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 102 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 102 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 103 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 103 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 103 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 104 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 104 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 104 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 105 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 105 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 105 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 106 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 106 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 106 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 107 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 107 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 107 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 108 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 108 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 108 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 109 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 109 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 109 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 110 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 110 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 110 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 111 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 111 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 111 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 112 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 112 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 112 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 113 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 113 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 113 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 114 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 114 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 114 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 115 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 115 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 115 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 116 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 116 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 116 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 117 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 117 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 117 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 118 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 118 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 118 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 119 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 119 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 119 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 120 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 120 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 120 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 121 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 121 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 121 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 122 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 122 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 122 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 123 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 123 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 123 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 124 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 124 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 124 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 125 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 125 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 125 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 126 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 126 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 126 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 127 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 127 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 127 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 128 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 128 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 128 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 129 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 129 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 129 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 130 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 130 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 130 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 131 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 131 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 131 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 132 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 132 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 132 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 133 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 133 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 133 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 134 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 134 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 134 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 135 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 135 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 135 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 136 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 136 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 136 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 137 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 137 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 137 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 138 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 138 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 138 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 139 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 139 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 139 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 140 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 140 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 140 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 141 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 141 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 141 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 142 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 142 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 142 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 143 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 143 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 143 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 144 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 144 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 144 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 145 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 145 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 145 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 146 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 146 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 146 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 147 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 147 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 147 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 148 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 148 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 148 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 149 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 149 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 149 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 150 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 150 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 150 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 151 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 151 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 151 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 152 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 152 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 152 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 153 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 153 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 153 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 154 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 154 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 154 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 155 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 155 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 155 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 156 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 156 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 156 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 157 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 157 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 157 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 158 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 158 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 158 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 159 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 159 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 159 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 160 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 160 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 161 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 161 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 161 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 162 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 162 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 162 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 163 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 163 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 163 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 164 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 164 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 164 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 165 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 165 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 165 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 166 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 166 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 166 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 167 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 167 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 167 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 168 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 168 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 168 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 169 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 169 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 169 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 170 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 170 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 170 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 171 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 171 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 171 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 172 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 172 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 172 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 173 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 173 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 173 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 174 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 174 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 174 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 175 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 175 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 175 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 176 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 176 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 176 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 177 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 177 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 177 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 178 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 178 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 178 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 179 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 179 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 179 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 180 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 180 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 180 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 181 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 181 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 181 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 182 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 182 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 182 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 183 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 183 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 183 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 184 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 184 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 184 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 185 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 185 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 185 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 186 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 186 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 186 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 187 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 187 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 187 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 188 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 188 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 188 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 189 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 189 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 189 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 190 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 190 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 190 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 191 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 191 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 191 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 192 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 192 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 192 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 193 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 193 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 193 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 194 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 194 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 194 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 195 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 195 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 195 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 196 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 196 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 196 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 197 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 197 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 197 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 198 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 198 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 198 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 199 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 199 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 199 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 200 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 200 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 200 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 201 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 201 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 201 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 202 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 202 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 202 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 203 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 203 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 203 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 204 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 204 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 204 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 205 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 205 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 205 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 206 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 206 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 206 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 207 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 207 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 207 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 208 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 208 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 208 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 209 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 209 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 209 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 210 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 210 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 210 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 211 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 211 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 211 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 212 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 212 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 212 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 213 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 213 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 213 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 214 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 214 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 214 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 215 && changeType == 0) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 215 && changeType == 1) {

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
		if (slotID == 215 && changeType == 2) {
			int amount = meta;

			SaveItemsProcedure
					.executeProcedure(Stream.of(new AbstractMap.SimpleEntry<>("world", world), new AbstractMap.SimpleEntry<>("entity", entity))
							.collect(HashMap::new, (_m, _e) -> _m.put(_e.getKey(), _e.getValue()), Map::putAll));
		}
	}
}
