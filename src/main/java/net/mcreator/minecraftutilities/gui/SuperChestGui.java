
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

import net.mcreator.minecraftutilities.MinecraftutilitiesModElements;
import net.mcreator.minecraftutilities.MinecraftutilitiesMod;

import java.util.function.Supplier;
import java.util.Map;
import java.util.HashMap;

@MinecraftutilitiesModElements.ModElement.Tag
public class SuperChestGui extends MinecraftutilitiesModElements.ModElement {
	public static HashMap guistate = new HashMap();
	private static ContainerType<GuiContainerMod> containerType = null;

	public SuperChestGui(MinecraftutilitiesModElements instance) {
		super(instance, 39);
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
			event.getRegistry().register(containerType.setRegistryName("super_chest"));
		}
	}

	@OnlyIn(Dist.CLIENT)
	public void initElements() {
		DeferredWorkQueue.runLater(() -> ScreenManager.registerFactory(containerType, SuperChestGuiWindow::new));
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
			this.internal = new ItemStackHandler(246);
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
			}));
			this.customSlots.put(1, this.addSlot(new SlotItemHandler(internal, 1, 23, 5) {
			}));
			this.customSlots.put(2, this.addSlot(new SlotItemHandler(internal, 2, 41, 5) {
			}));
			this.customSlots.put(3, this.addSlot(new SlotItemHandler(internal, 3, 59, 5) {
			}));
			this.customSlots.put(4, this.addSlot(new SlotItemHandler(internal, 4, 77, 5) {
			}));
			this.customSlots.put(5, this.addSlot(new SlotItemHandler(internal, 5, 95, 5) {
			}));
			this.customSlots.put(6, this.addSlot(new SlotItemHandler(internal, 6, 113, 5) {
			}));
			this.customSlots.put(7, this.addSlot(new SlotItemHandler(internal, 7, 131, 5) {
			}));
			this.customSlots.put(8, this.addSlot(new SlotItemHandler(internal, 8, 149, 5) {
			}));
			this.customSlots.put(9, this.addSlot(new SlotItemHandler(internal, 9, 167, 5) {
			}));
			this.customSlots.put(10, this.addSlot(new SlotItemHandler(internal, 10, 185, 5) {
			}));
			this.customSlots.put(11, this.addSlot(new SlotItemHandler(internal, 11, 203, 5) {
			}));
			this.customSlots.put(12, this.addSlot(new SlotItemHandler(internal, 12, 221, 5) {
			}));
			this.customSlots.put(13, this.addSlot(new SlotItemHandler(internal, 13, 239, 5) {
			}));
			this.customSlots.put(14, this.addSlot(new SlotItemHandler(internal, 14, 257, 5) {
			}));
			this.customSlots.put(15, this.addSlot(new SlotItemHandler(internal, 15, 275, 5) {
			}));
			this.customSlots.put(16, this.addSlot(new SlotItemHandler(internal, 16, 293, 5) {
			}));
			this.customSlots.put(17, this.addSlot(new SlotItemHandler(internal, 17, 311, 5) {
			}));
			this.customSlots.put(18, this.addSlot(new SlotItemHandler(internal, 18, 329, 5) {
			}));
			this.customSlots.put(19, this.addSlot(new SlotItemHandler(internal, 19, 347, 5) {
			}));
			this.customSlots.put(20, this.addSlot(new SlotItemHandler(internal, 20, 365, 5) {
			}));
			this.customSlots.put(21, this.addSlot(new SlotItemHandler(internal, 21, 383, 5) {
			}));
			this.customSlots.put(22, this.addSlot(new SlotItemHandler(internal, 22, 401, 5) {
			}));
			this.customSlots.put(23, this.addSlot(new SlotItemHandler(internal, 23, 419, 5) {
			}));
			this.customSlots.put(24, this.addSlot(new SlotItemHandler(internal, 24, 5, 23) {
			}));
			this.customSlots.put(25, this.addSlot(new SlotItemHandler(internal, 25, 23, 23) {
			}));
			this.customSlots.put(26, this.addSlot(new SlotItemHandler(internal, 26, 41, 23) {
			}));
			this.customSlots.put(27, this.addSlot(new SlotItemHandler(internal, 27, 59, 23) {
			}));
			this.customSlots.put(28, this.addSlot(new SlotItemHandler(internal, 28, 77, 23) {
			}));
			this.customSlots.put(29, this.addSlot(new SlotItemHandler(internal, 29, 95, 23) {
			}));
			this.customSlots.put(30, this.addSlot(new SlotItemHandler(internal, 30, 113, 23) {
			}));
			this.customSlots.put(31, this.addSlot(new SlotItemHandler(internal, 31, 131, 23) {
			}));
			this.customSlots.put(32, this.addSlot(new SlotItemHandler(internal, 32, 149, 23) {
			}));
			this.customSlots.put(33, this.addSlot(new SlotItemHandler(internal, 33, 167, 23) {
			}));
			this.customSlots.put(34, this.addSlot(new SlotItemHandler(internal, 34, 185, 23) {
			}));
			this.customSlots.put(35, this.addSlot(new SlotItemHandler(internal, 35, 203, 23) {
			}));
			this.customSlots.put(36, this.addSlot(new SlotItemHandler(internal, 36, 221, 23) {
			}));
			this.customSlots.put(37, this.addSlot(new SlotItemHandler(internal, 37, 239, 23) {
			}));
			this.customSlots.put(38, this.addSlot(new SlotItemHandler(internal, 38, 257, 23) {
			}));
			this.customSlots.put(39, this.addSlot(new SlotItemHandler(internal, 39, 275, 23) {
			}));
			this.customSlots.put(40, this.addSlot(new SlotItemHandler(internal, 40, 293, 23) {
			}));
			this.customSlots.put(41, this.addSlot(new SlotItemHandler(internal, 41, 311, 23) {
			}));
			this.customSlots.put(42, this.addSlot(new SlotItemHandler(internal, 42, 329, 23) {
			}));
			this.customSlots.put(43, this.addSlot(new SlotItemHandler(internal, 43, 347, 23) {
			}));
			this.customSlots.put(44, this.addSlot(new SlotItemHandler(internal, 44, 365, 23) {
			}));
			this.customSlots.put(45, this.addSlot(new SlotItemHandler(internal, 45, 383, 23) {
			}));
			this.customSlots.put(46, this.addSlot(new SlotItemHandler(internal, 46, 401, 23) {
			}));
			this.customSlots.put(47, this.addSlot(new SlotItemHandler(internal, 47, 419, 23) {
			}));
			this.customSlots.put(48, this.addSlot(new SlotItemHandler(internal, 48, 5, 41) {
			}));
			this.customSlots.put(49, this.addSlot(new SlotItemHandler(internal, 49, 23, 41) {
			}));
			this.customSlots.put(50, this.addSlot(new SlotItemHandler(internal, 50, 41, 41) {
			}));
			this.customSlots.put(51, this.addSlot(new SlotItemHandler(internal, 51, 59, 41) {
			}));
			this.customSlots.put(52, this.addSlot(new SlotItemHandler(internal, 52, 77, 41) {
			}));
			this.customSlots.put(53, this.addSlot(new SlotItemHandler(internal, 53, 95, 41) {
			}));
			this.customSlots.put(54, this.addSlot(new SlotItemHandler(internal, 54, 113, 41) {
			}));
			this.customSlots.put(55, this.addSlot(new SlotItemHandler(internal, 55, 131, 41) {
			}));
			this.customSlots.put(56, this.addSlot(new SlotItemHandler(internal, 56, 149, 41) {
			}));
			this.customSlots.put(57, this.addSlot(new SlotItemHandler(internal, 57, 167, 41) {
			}));
			this.customSlots.put(58, this.addSlot(new SlotItemHandler(internal, 58, 185, 41) {
			}));
			this.customSlots.put(59, this.addSlot(new SlotItemHandler(internal, 59, 203, 41) {
			}));
			this.customSlots.put(60, this.addSlot(new SlotItemHandler(internal, 60, 221, 41) {
			}));
			this.customSlots.put(61, this.addSlot(new SlotItemHandler(internal, 61, 239, 41) {
			}));
			this.customSlots.put(62, this.addSlot(new SlotItemHandler(internal, 62, 257, 41) {
			}));
			this.customSlots.put(63, this.addSlot(new SlotItemHandler(internal, 63, 275, 41) {
			}));
			this.customSlots.put(64, this.addSlot(new SlotItemHandler(internal, 64, 293, 41) {
			}));
			this.customSlots.put(65, this.addSlot(new SlotItemHandler(internal, 65, 311, 41) {
			}));
			this.customSlots.put(66, this.addSlot(new SlotItemHandler(internal, 66, 329, 41) {
			}));
			this.customSlots.put(67, this.addSlot(new SlotItemHandler(internal, 67, 347, 41) {
			}));
			this.customSlots.put(68, this.addSlot(new SlotItemHandler(internal, 68, 365, 41) {
			}));
			this.customSlots.put(69, this.addSlot(new SlotItemHandler(internal, 69, 383, 41) {
			}));
			this.customSlots.put(70, this.addSlot(new SlotItemHandler(internal, 70, 401, 41) {
			}));
			this.customSlots.put(71, this.addSlot(new SlotItemHandler(internal, 71, 419, 41) {
			}));
			this.customSlots.put(72, this.addSlot(new SlotItemHandler(internal, 72, 5, 59) {
			}));
			this.customSlots.put(73, this.addSlot(new SlotItemHandler(internal, 73, 23, 59) {
			}));
			this.customSlots.put(74, this.addSlot(new SlotItemHandler(internal, 74, 41, 59) {
			}));
			this.customSlots.put(75, this.addSlot(new SlotItemHandler(internal, 75, 59, 59) {
			}));
			this.customSlots.put(76, this.addSlot(new SlotItemHandler(internal, 76, 77, 59) {
			}));
			this.customSlots.put(77, this.addSlot(new SlotItemHandler(internal, 77, 95, 59) {
			}));
			this.customSlots.put(78, this.addSlot(new SlotItemHandler(internal, 78, 113, 59) {
			}));
			this.customSlots.put(79, this.addSlot(new SlotItemHandler(internal, 79, 131, 59) {
			}));
			this.customSlots.put(80, this.addSlot(new SlotItemHandler(internal, 80, 149, 59) {
			}));
			this.customSlots.put(81, this.addSlot(new SlotItemHandler(internal, 81, 167, 59) {
			}));
			this.customSlots.put(82, this.addSlot(new SlotItemHandler(internal, 82, 185, 59) {
			}));
			this.customSlots.put(83, this.addSlot(new SlotItemHandler(internal, 83, 203, 59) {
			}));
			this.customSlots.put(84, this.addSlot(new SlotItemHandler(internal, 84, 221, 59) {
			}));
			this.customSlots.put(85, this.addSlot(new SlotItemHandler(internal, 85, 239, 59) {
			}));
			this.customSlots.put(86, this.addSlot(new SlotItemHandler(internal, 86, 257, 59) {
			}));
			this.customSlots.put(87, this.addSlot(new SlotItemHandler(internal, 87, 275, 59) {
			}));
			this.customSlots.put(88, this.addSlot(new SlotItemHandler(internal, 88, 293, 59) {
			}));
			this.customSlots.put(89, this.addSlot(new SlotItemHandler(internal, 89, 311, 59) {
			}));
			this.customSlots.put(90, this.addSlot(new SlotItemHandler(internal, 90, 329, 59) {
			}));
			this.customSlots.put(91, this.addSlot(new SlotItemHandler(internal, 91, 347, 59) {
			}));
			this.customSlots.put(92, this.addSlot(new SlotItemHandler(internal, 92, 365, 59) {
			}));
			this.customSlots.put(93, this.addSlot(new SlotItemHandler(internal, 93, 383, 59) {
			}));
			this.customSlots.put(94, this.addSlot(new SlotItemHandler(internal, 94, 401, 59) {
			}));
			this.customSlots.put(95, this.addSlot(new SlotItemHandler(internal, 95, 419, 59) {
			}));
			this.customSlots.put(96, this.addSlot(new SlotItemHandler(internal, 96, 5, 77) {
			}));
			this.customSlots.put(97, this.addSlot(new SlotItemHandler(internal, 97, 23, 77) {
			}));
			this.customSlots.put(98, this.addSlot(new SlotItemHandler(internal, 98, 41, 77) {
			}));
			this.customSlots.put(99, this.addSlot(new SlotItemHandler(internal, 99, 59, 77) {
			}));
			this.customSlots.put(100, this.addSlot(new SlotItemHandler(internal, 100, 77, 77) {
			}));
			this.customSlots.put(101, this.addSlot(new SlotItemHandler(internal, 101, 95, 77) {
			}));
			this.customSlots.put(102, this.addSlot(new SlotItemHandler(internal, 102, 113, 77) {
			}));
			this.customSlots.put(103, this.addSlot(new SlotItemHandler(internal, 103, 131, 77) {
			}));
			this.customSlots.put(104, this.addSlot(new SlotItemHandler(internal, 104, 149, 77) {
			}));
			this.customSlots.put(105, this.addSlot(new SlotItemHandler(internal, 105, 167, 77) {
			}));
			this.customSlots.put(106, this.addSlot(new SlotItemHandler(internal, 106, 185, 77) {
			}));
			this.customSlots.put(107, this.addSlot(new SlotItemHandler(internal, 107, 203, 77) {
			}));
			this.customSlots.put(108, this.addSlot(new SlotItemHandler(internal, 108, 221, 77) {
			}));
			this.customSlots.put(109, this.addSlot(new SlotItemHandler(internal, 109, 239, 77) {
			}));
			this.customSlots.put(110, this.addSlot(new SlotItemHandler(internal, 110, 257, 77) {
			}));
			this.customSlots.put(111, this.addSlot(new SlotItemHandler(internal, 111, 275, 77) {
			}));
			this.customSlots.put(112, this.addSlot(new SlotItemHandler(internal, 112, 293, 77) {
			}));
			this.customSlots.put(113, this.addSlot(new SlotItemHandler(internal, 113, 311, 77) {
			}));
			this.customSlots.put(114, this.addSlot(new SlotItemHandler(internal, 114, 329, 77) {
			}));
			this.customSlots.put(115, this.addSlot(new SlotItemHandler(internal, 115, 347, 77) {
			}));
			this.customSlots.put(116, this.addSlot(new SlotItemHandler(internal, 116, 365, 77) {
			}));
			this.customSlots.put(117, this.addSlot(new SlotItemHandler(internal, 117, 383, 77) {
			}));
			this.customSlots.put(118, this.addSlot(new SlotItemHandler(internal, 118, 401, 77) {
			}));
			this.customSlots.put(119, this.addSlot(new SlotItemHandler(internal, 119, 419, 77) {
			}));
			this.customSlots.put(120, this.addSlot(new SlotItemHandler(internal, 120, 5, 95) {
			}));
			this.customSlots.put(121, this.addSlot(new SlotItemHandler(internal, 121, 23, 95) {
			}));
			this.customSlots.put(122, this.addSlot(new SlotItemHandler(internal, 122, 41, 95) {
			}));
			this.customSlots.put(123, this.addSlot(new SlotItemHandler(internal, 123, 59, 95) {
			}));
			this.customSlots.put(124, this.addSlot(new SlotItemHandler(internal, 124, 77, 95) {
			}));
			this.customSlots.put(125, this.addSlot(new SlotItemHandler(internal, 125, 95, 95) {
			}));
			this.customSlots.put(126, this.addSlot(new SlotItemHandler(internal, 126, 113, 95) {
			}));
			this.customSlots.put(127, this.addSlot(new SlotItemHandler(internal, 127, 131, 95) {
			}));
			this.customSlots.put(128, this.addSlot(new SlotItemHandler(internal, 128, 149, 95) {
			}));
			this.customSlots.put(129, this.addSlot(new SlotItemHandler(internal, 129, 167, 95) {
			}));
			this.customSlots.put(130, this.addSlot(new SlotItemHandler(internal, 130, 185, 95) {
			}));
			this.customSlots.put(131, this.addSlot(new SlotItemHandler(internal, 131, 203, 95) {
			}));
			this.customSlots.put(132, this.addSlot(new SlotItemHandler(internal, 132, 221, 95) {
			}));
			this.customSlots.put(133, this.addSlot(new SlotItemHandler(internal, 133, 239, 95) {
			}));
			this.customSlots.put(134, this.addSlot(new SlotItemHandler(internal, 134, 257, 95) {
			}));
			this.customSlots.put(135, this.addSlot(new SlotItemHandler(internal, 135, 275, 95) {
			}));
			this.customSlots.put(136, this.addSlot(new SlotItemHandler(internal, 136, 293, 95) {
			}));
			this.customSlots.put(137, this.addSlot(new SlotItemHandler(internal, 137, 311, 95) {
			}));
			this.customSlots.put(138, this.addSlot(new SlotItemHandler(internal, 138, 329, 95) {
			}));
			this.customSlots.put(139, this.addSlot(new SlotItemHandler(internal, 139, 347, 95) {
			}));
			this.customSlots.put(140, this.addSlot(new SlotItemHandler(internal, 140, 365, 95) {
			}));
			this.customSlots.put(141, this.addSlot(new SlotItemHandler(internal, 141, 383, 95) {
			}));
			this.customSlots.put(142, this.addSlot(new SlotItemHandler(internal, 142, 401, 95) {
			}));
			this.customSlots.put(143, this.addSlot(new SlotItemHandler(internal, 143, 419, 95) {
			}));
			this.customSlots.put(144, this.addSlot(new SlotItemHandler(internal, 144, 5, 113) {
			}));
			this.customSlots.put(145, this.addSlot(new SlotItemHandler(internal, 145, 23, 113) {
			}));
			this.customSlots.put(146, this.addSlot(new SlotItemHandler(internal, 146, 41, 113) {
			}));
			this.customSlots.put(147, this.addSlot(new SlotItemHandler(internal, 147, 59, 113) {
			}));
			this.customSlots.put(148, this.addSlot(new SlotItemHandler(internal, 148, 77, 113) {
			}));
			this.customSlots.put(149, this.addSlot(new SlotItemHandler(internal, 149, 95, 113) {
			}));
			this.customSlots.put(150, this.addSlot(new SlotItemHandler(internal, 150, 113, 113) {
			}));
			this.customSlots.put(151, this.addSlot(new SlotItemHandler(internal, 151, 131, 113) {
			}));
			this.customSlots.put(152, this.addSlot(new SlotItemHandler(internal, 152, 149, 113) {
			}));
			this.customSlots.put(153, this.addSlot(new SlotItemHandler(internal, 153, 167, 113) {
			}));
			this.customSlots.put(154, this.addSlot(new SlotItemHandler(internal, 154, 185, 113) {
			}));
			this.customSlots.put(155, this.addSlot(new SlotItemHandler(internal, 155, 203, 113) {
			}));
			this.customSlots.put(156, this.addSlot(new SlotItemHandler(internal, 156, 221, 113) {
			}));
			this.customSlots.put(157, this.addSlot(new SlotItemHandler(internal, 157, 239, 113) {
			}));
			this.customSlots.put(158, this.addSlot(new SlotItemHandler(internal, 158, 257, 113) {
			}));
			this.customSlots.put(159, this.addSlot(new SlotItemHandler(internal, 159, 275, 113) {
			}));
			this.customSlots.put(160, this.addSlot(new SlotItemHandler(internal, 160, 293, 113) {
			}));
			this.customSlots.put(161, this.addSlot(new SlotItemHandler(internal, 161, 311, 113) {
			}));
			this.customSlots.put(162, this.addSlot(new SlotItemHandler(internal, 162, 329, 113) {
			}));
			this.customSlots.put(163, this.addSlot(new SlotItemHandler(internal, 163, 347, 113) {
			}));
			this.customSlots.put(164, this.addSlot(new SlotItemHandler(internal, 164, 365, 113) {
			}));
			this.customSlots.put(165, this.addSlot(new SlotItemHandler(internal, 165, 383, 113) {
			}));
			this.customSlots.put(166, this.addSlot(new SlotItemHandler(internal, 166, 401, 113) {
			}));
			this.customSlots.put(167, this.addSlot(new SlotItemHandler(internal, 167, 419, 113) {
			}));
			this.customSlots.put(168, this.addSlot(new SlotItemHandler(internal, 168, 5, 131) {
			}));
			this.customSlots.put(169, this.addSlot(new SlotItemHandler(internal, 169, 23, 131) {
			}));
			this.customSlots.put(170, this.addSlot(new SlotItemHandler(internal, 170, 41, 131) {
			}));
			this.customSlots.put(171, this.addSlot(new SlotItemHandler(internal, 171, 59, 131) {
			}));
			this.customSlots.put(172, this.addSlot(new SlotItemHandler(internal, 172, 77, 131) {
			}));
			this.customSlots.put(173, this.addSlot(new SlotItemHandler(internal, 173, 95, 131) {
			}));
			this.customSlots.put(174, this.addSlot(new SlotItemHandler(internal, 174, 113, 131) {
			}));
			this.customSlots.put(175, this.addSlot(new SlotItemHandler(internal, 175, 131, 131) {
			}));
			this.customSlots.put(176, this.addSlot(new SlotItemHandler(internal, 176, 149, 131) {
			}));
			this.customSlots.put(177, this.addSlot(new SlotItemHandler(internal, 177, 167, 131) {
			}));
			this.customSlots.put(178, this.addSlot(new SlotItemHandler(internal, 178, 185, 131) {
			}));
			this.customSlots.put(179, this.addSlot(new SlotItemHandler(internal, 179, 203, 131) {
			}));
			this.customSlots.put(180, this.addSlot(new SlotItemHandler(internal, 180, 221, 131) {
			}));
			this.customSlots.put(181, this.addSlot(new SlotItemHandler(internal, 181, 239, 131) {
			}));
			this.customSlots.put(182, this.addSlot(new SlotItemHandler(internal, 182, 257, 131) {
			}));
			this.customSlots.put(183, this.addSlot(new SlotItemHandler(internal, 183, 275, 131) {
			}));
			this.customSlots.put(184, this.addSlot(new SlotItemHandler(internal, 184, 293, 131) {
			}));
			this.customSlots.put(185, this.addSlot(new SlotItemHandler(internal, 185, 311, 131) {
			}));
			this.customSlots.put(186, this.addSlot(new SlotItemHandler(internal, 186, 329, 131) {
			}));
			this.customSlots.put(187, this.addSlot(new SlotItemHandler(internal, 187, 347, 131) {
			}));
			this.customSlots.put(188, this.addSlot(new SlotItemHandler(internal, 188, 365, 131) {
			}));
			this.customSlots.put(189, this.addSlot(new SlotItemHandler(internal, 189, 383, 131) {
			}));
			this.customSlots.put(190, this.addSlot(new SlotItemHandler(internal, 190, 401, 131) {
			}));
			this.customSlots.put(191, this.addSlot(new SlotItemHandler(internal, 191, 419, 131) {
			}));
			this.customSlots.put(192, this.addSlot(new SlotItemHandler(internal, 192, 5, 149) {
			}));
			this.customSlots.put(193, this.addSlot(new SlotItemHandler(internal, 193, 23, 149) {
			}));
			this.customSlots.put(194, this.addSlot(new SlotItemHandler(internal, 194, 41, 149) {
			}));
			this.customSlots.put(195, this.addSlot(new SlotItemHandler(internal, 195, 59, 149) {
			}));
			this.customSlots.put(196, this.addSlot(new SlotItemHandler(internal, 196, 77, 149) {
			}));
			this.customSlots.put(197, this.addSlot(new SlotItemHandler(internal, 197, 95, 149) {
			}));
			this.customSlots.put(198, this.addSlot(new SlotItemHandler(internal, 198, 113, 149) {
			}));
			this.customSlots.put(199, this.addSlot(new SlotItemHandler(internal, 199, 131, 149) {
			}));
			this.customSlots.put(200, this.addSlot(new SlotItemHandler(internal, 200, 149, 149) {
			}));
			this.customSlots.put(201, this.addSlot(new SlotItemHandler(internal, 201, 167, 149) {
			}));
			this.customSlots.put(202, this.addSlot(new SlotItemHandler(internal, 202, 185, 149) {
			}));
			this.customSlots.put(203, this.addSlot(new SlotItemHandler(internal, 203, 203, 149) {
			}));
			this.customSlots.put(204, this.addSlot(new SlotItemHandler(internal, 204, 221, 149) {
			}));
			this.customSlots.put(205, this.addSlot(new SlotItemHandler(internal, 205, 239, 149) {
			}));
			this.customSlots.put(206, this.addSlot(new SlotItemHandler(internal, 206, 257, 149) {
			}));
			this.customSlots.put(207, this.addSlot(new SlotItemHandler(internal, 207, 275, 149) {
			}));
			this.customSlots.put(208, this.addSlot(new SlotItemHandler(internal, 208, 293, 149) {
			}));
			this.customSlots.put(209, this.addSlot(new SlotItemHandler(internal, 209, 311, 149) {
			}));
			this.customSlots.put(210, this.addSlot(new SlotItemHandler(internal, 210, 329, 149) {
			}));
			this.customSlots.put(211, this.addSlot(new SlotItemHandler(internal, 211, 347, 149) {
			}));
			this.customSlots.put(212, this.addSlot(new SlotItemHandler(internal, 212, 365, 149) {
			}));
			this.customSlots.put(213, this.addSlot(new SlotItemHandler(internal, 213, 383, 149) {
			}));
			this.customSlots.put(214, this.addSlot(new SlotItemHandler(internal, 214, 401, 149) {
			}));
			this.customSlots.put(215, this.addSlot(new SlotItemHandler(internal, 215, 419, 149) {
			}));
			this.customSlots.put(216, this.addSlot(new SlotItemHandler(internal, 216, 5, 167) {
			}));
			this.customSlots.put(217, this.addSlot(new SlotItemHandler(internal, 217, 23, 167) {
			}));
			this.customSlots.put(218, this.addSlot(new SlotItemHandler(internal, 218, 41, 167) {
			}));
			this.customSlots.put(219, this.addSlot(new SlotItemHandler(internal, 219, 59, 167) {
			}));
			this.customSlots.put(220, this.addSlot(new SlotItemHandler(internal, 220, 77, 167) {
			}));
			this.customSlots.put(221, this.addSlot(new SlotItemHandler(internal, 221, 95, 167) {
			}));
			this.customSlots.put(222, this.addSlot(new SlotItemHandler(internal, 222, 113, 167) {
			}));
			this.customSlots.put(223, this.addSlot(new SlotItemHandler(internal, 223, 293, 167) {
			}));
			this.customSlots.put(224, this.addSlot(new SlotItemHandler(internal, 224, 311, 167) {
			}));
			this.customSlots.put(225, this.addSlot(new SlotItemHandler(internal, 225, 329, 167) {
			}));
			this.customSlots.put(226, this.addSlot(new SlotItemHandler(internal, 226, 347, 167) {
			}));
			this.customSlots.put(227, this.addSlot(new SlotItemHandler(internal, 227, 365, 167) {
			}));
			this.customSlots.put(228, this.addSlot(new SlotItemHandler(internal, 228, 383, 167) {
			}));
			this.customSlots.put(229, this.addSlot(new SlotItemHandler(internal, 229, 401, 167) {
			}));
			this.customSlots.put(230, this.addSlot(new SlotItemHandler(internal, 230, 419, 167) {
			}));
			this.customSlots.put(231, this.addSlot(new SlotItemHandler(internal, 231, 5, 185) {
			}));
			this.customSlots.put(232, this.addSlot(new SlotItemHandler(internal, 232, 23, 185) {
			}));
			this.customSlots.put(233, this.addSlot(new SlotItemHandler(internal, 233, 41, 185) {
			}));
			this.customSlots.put(234, this.addSlot(new SlotItemHandler(internal, 234, 59, 185) {
			}));
			this.customSlots.put(235, this.addSlot(new SlotItemHandler(internal, 235, 77, 185) {
			}));
			this.customSlots.put(236, this.addSlot(new SlotItemHandler(internal, 236, 95, 185) {
			}));
			this.customSlots.put(237, this.addSlot(new SlotItemHandler(internal, 237, 113, 185) {
			}));
			this.customSlots.put(238, this.addSlot(new SlotItemHandler(internal, 238, 293, 185) {
			}));
			this.customSlots.put(239, this.addSlot(new SlotItemHandler(internal, 239, 311, 185) {
			}));
			this.customSlots.put(240, this.addSlot(new SlotItemHandler(internal, 240, 329, 185) {
			}));
			this.customSlots.put(241, this.addSlot(new SlotItemHandler(internal, 241, 347, 185) {
			}));
			this.customSlots.put(242, this.addSlot(new SlotItemHandler(internal, 242, 365, 185) {
			}));
			this.customSlots.put(243, this.addSlot(new SlotItemHandler(internal, 243, 383, 185) {
			}));
			this.customSlots.put(244, this.addSlot(new SlotItemHandler(internal, 244, 401, 185) {
			}));
			this.customSlots.put(245, this.addSlot(new SlotItemHandler(internal, 245, 419, 185) {
			}));
			int si;
			int sj;
			for (si = 0; si < 3; ++si)
				for (sj = 0; sj < 9; ++sj)
					this.addSlot(new Slot(inv, sj + (si + 1) * 9, 123 + 8 + sj * 18, 84 + 84 + si * 18));
			for (si = 0; si < 9; ++si)
				this.addSlot(new Slot(inv, si, 123 + 8 + si * 18, 84 + 142));
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
				if (index < 246) {
					if (!this.mergeItemStack(itemstack1, 246, this.inventorySlots.size(), true)) {
						return ItemStack.EMPTY;
					}
					slot.onSlotChange(itemstack1, itemstack);
				} else if (!this.mergeItemStack(itemstack1, 0, 246, false)) {
					if (index < 246 + 27) {
						if (!this.mergeItemStack(itemstack1, 246 + 27, this.inventorySlots.size(), true)) {
							return ItemStack.EMPTY;
						}
					} else {
						if (!this.mergeItemStack(itemstack1, 246, 246 + 27, false)) {
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
						playerIn.dropItem(internal.extractItem(j, internal.getStackInSlot(j).getCount(), false), false);
					}
				} else {
					for (int i = 0; i < internal.getSlots(); ++i) {
						if (i == 0)
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
	}
}
