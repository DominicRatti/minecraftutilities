package net.mcreator.minecraftutilities;

import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.Capability;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Direction;
import net.minecraft.network.PacketBuffer;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.client.Minecraft;

import java.util.function.Supplier;

public class MinecraftutilitiesModVariables {
	public MinecraftutilitiesModVariables(MinecraftutilitiesModElements elements) {
		elements.addNetworkMessage(PlayerVariablesSyncMessage.class, PlayerVariablesSyncMessage::buffer, PlayerVariablesSyncMessage::new,
				PlayerVariablesSyncMessage::handler);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::init);
	}

	private void init(FMLCommonSetupEvent event) {
		CapabilityManager.INSTANCE.register(PlayerVariables.class, new PlayerVariablesStorage(), PlayerVariables::new);
	}

	@CapabilityInject(PlayerVariables.class)
	public static Capability<PlayerVariables> PLAYER_VARIABLES_CAPABILITY = null;

	@SubscribeEvent
	public void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
		if (event.getObject() instanceof PlayerEntity && !(event.getObject() instanceof FakePlayer))
			event.addCapability(new ResourceLocation("minecraftutilities", "player_variables"), new PlayerVariablesProvider());
	}

	private static class PlayerVariablesProvider implements ICapabilitySerializable<INBT> {
		private final LazyOptional<PlayerVariables> instance = LazyOptional.of(PLAYER_VARIABLES_CAPABILITY::getDefaultInstance);

		@Override
		public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
			return cap == PLAYER_VARIABLES_CAPABILITY ? instance.cast() : LazyOptional.empty();
		}

		@Override
		public INBT serializeNBT() {
			return PLAYER_VARIABLES_CAPABILITY.getStorage().writeNBT(PLAYER_VARIABLES_CAPABILITY, this.instance.orElseThrow(RuntimeException::new),
					null);
		}

		@Override
		public void deserializeNBT(INBT nbt) {
			PLAYER_VARIABLES_CAPABILITY.getStorage().readNBT(PLAYER_VARIABLES_CAPABILITY, this.instance.orElseThrow(RuntimeException::new), null,
					nbt);
		}
	}

	private static class PlayerVariablesStorage implements Capability.IStorage<PlayerVariables> {
		@Override
		public INBT writeNBT(Capability<PlayerVariables> capability, PlayerVariables instance, Direction side) {
			CompoundNBT nbt = new CompoundNBT();
			nbt.put("itemSlot0", instance.itemSlot0.write(new CompoundNBT()));
			nbt.putDouble("itemSlot0quantity", instance.itemSlot0quantity);
			nbt.put("itemSlot1", instance.itemSlot1.write(new CompoundNBT()));
			nbt.putDouble("itemSlot1quantity", instance.itemSlot1quantity);
			nbt.put("itemSlot2", instance.itemSlot2.write(new CompoundNBT()));
			nbt.put("itemSlot3", instance.itemSlot3.write(new CompoundNBT()));
			nbt.put("itemSlot4", instance.itemSlot4.write(new CompoundNBT()));
			nbt.put("itemSlot5", instance.itemSlot5.write(new CompoundNBT()));
			nbt.put("itemSlot6", instance.itemSlot6.write(new CompoundNBT()));
			nbt.put("itemSlot7", instance.itemSlot7.write(new CompoundNBT()));
			nbt.put("itemSlot8", instance.itemSlot8.write(new CompoundNBT()));
			nbt.put("itemSlot9", instance.itemSlot9.write(new CompoundNBT()));
			nbt.put("itemSlot10", instance.itemSlot10.write(new CompoundNBT()));
			nbt.put("itemSlot11", instance.itemSlot11.write(new CompoundNBT()));
			nbt.put("itemSlot12", instance.itemSlot12.write(new CompoundNBT()));
			nbt.put("itemSlot13", instance.itemSlot13.write(new CompoundNBT()));
			nbt.put("itemSlot14", instance.itemSlot14.write(new CompoundNBT()));
			nbt.put("itemSlot15", instance.itemSlot15.write(new CompoundNBT()));
			nbt.put("itemSlot16", instance.itemSlot16.write(new CompoundNBT()));
			nbt.put("itemSlot17", instance.itemSlot17.write(new CompoundNBT()));
			nbt.put("itemSlot18", instance.itemSlot18.write(new CompoundNBT()));
			nbt.put("itemSlot19", instance.itemSlot19.write(new CompoundNBT()));
			nbt.put("itemSlot20", instance.itemSlot20.write(new CompoundNBT()));
			return nbt;
		}

		@Override
		public void readNBT(Capability<PlayerVariables> capability, PlayerVariables instance, Direction side, INBT inbt) {
			CompoundNBT nbt = (CompoundNBT) inbt;
			instance.itemSlot0 = ItemStack.read(nbt.getCompound("itemSlot0"));
			instance.itemSlot0quantity = nbt.getDouble("itemSlot0quantity");
			instance.itemSlot1 = ItemStack.read(nbt.getCompound("itemSlot1"));
			instance.itemSlot1quantity = nbt.getDouble("itemSlot1quantity");
			instance.itemSlot2 = ItemStack.read(nbt.getCompound("itemSlot2"));
			instance.itemSlot3 = ItemStack.read(nbt.getCompound("itemSlot3"));
			instance.itemSlot4 = ItemStack.read(nbt.getCompound("itemSlot4"));
			instance.itemSlot5 = ItemStack.read(nbt.getCompound("itemSlot5"));
			instance.itemSlot6 = ItemStack.read(nbt.getCompound("itemSlot6"));
			instance.itemSlot7 = ItemStack.read(nbt.getCompound("itemSlot7"));
			instance.itemSlot8 = ItemStack.read(nbt.getCompound("itemSlot8"));
			instance.itemSlot9 = ItemStack.read(nbt.getCompound("itemSlot9"));
			instance.itemSlot10 = ItemStack.read(nbt.getCompound("itemSlot10"));
			instance.itemSlot11 = ItemStack.read(nbt.getCompound("itemSlot11"));
			instance.itemSlot12 = ItemStack.read(nbt.getCompound("itemSlot12"));
			instance.itemSlot13 = ItemStack.read(nbt.getCompound("itemSlot13"));
			instance.itemSlot14 = ItemStack.read(nbt.getCompound("itemSlot14"));
			instance.itemSlot15 = ItemStack.read(nbt.getCompound("itemSlot15"));
			instance.itemSlot16 = ItemStack.read(nbt.getCompound("itemSlot16"));
			instance.itemSlot17 = ItemStack.read(nbt.getCompound("itemSlot17"));
			instance.itemSlot18 = ItemStack.read(nbt.getCompound("itemSlot18"));
			instance.itemSlot19 = ItemStack.read(nbt.getCompound("itemSlot19"));
			instance.itemSlot20 = ItemStack.read(nbt.getCompound("itemSlot20"));
		}
	}

	public static class PlayerVariables {
		public ItemStack itemSlot0 = ItemStack.EMPTY;
		public double itemSlot0quantity = 0;
		public ItemStack itemSlot1 = ItemStack.EMPTY;
		public double itemSlot1quantity = 0;
		public ItemStack itemSlot2 = ItemStack.EMPTY;
		public ItemStack itemSlot3 = ItemStack.EMPTY;
		public ItemStack itemSlot4 = ItemStack.EMPTY;
		public ItemStack itemSlot5 = ItemStack.EMPTY;
		public ItemStack itemSlot6 = ItemStack.EMPTY;
		public ItemStack itemSlot7 = ItemStack.EMPTY;
		public ItemStack itemSlot8 = ItemStack.EMPTY;
		public ItemStack itemSlot9 = ItemStack.EMPTY;
		public ItemStack itemSlot10 = ItemStack.EMPTY;
		public ItemStack itemSlot11 = ItemStack.EMPTY;
		public ItemStack itemSlot12 = ItemStack.EMPTY;
		public ItemStack itemSlot13 = ItemStack.EMPTY;
		public ItemStack itemSlot14 = ItemStack.EMPTY;
		public ItemStack itemSlot15 = ItemStack.EMPTY;
		public ItemStack itemSlot16 = ItemStack.EMPTY;
		public ItemStack itemSlot17 = ItemStack.EMPTY;
		public ItemStack itemSlot18 = ItemStack.EMPTY;
		public ItemStack itemSlot19 = ItemStack.EMPTY;
		public ItemStack itemSlot20 = ItemStack.EMPTY;

		public void syncPlayerVariables(Entity entity) {
			if (entity instanceof ServerPlayerEntity)
				MinecraftutilitiesMod.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) entity),
						new PlayerVariablesSyncMessage(this));
		}
	}

	@SubscribeEvent
	public void onPlayerLoggedInSyncPlayerVariables(PlayerEvent.PlayerLoggedInEvent event) {
		if (!event.getPlayer().world.isRemote())
			((PlayerVariables) event.getPlayer().getCapability(PLAYER_VARIABLES_CAPABILITY, null).orElse(new PlayerVariables()))
					.syncPlayerVariables(event.getPlayer());
	}

	@SubscribeEvent
	public void onPlayerRespawnedSyncPlayerVariables(PlayerEvent.PlayerRespawnEvent event) {
		if (!event.getPlayer().world.isRemote())
			((PlayerVariables) event.getPlayer().getCapability(PLAYER_VARIABLES_CAPABILITY, null).orElse(new PlayerVariables()))
					.syncPlayerVariables(event.getPlayer());
	}

	@SubscribeEvent
	public void onPlayerChangedDimensionSyncPlayerVariables(PlayerEvent.PlayerChangedDimensionEvent event) {
		if (!event.getPlayer().world.isRemote())
			((PlayerVariables) event.getPlayer().getCapability(PLAYER_VARIABLES_CAPABILITY, null).orElse(new PlayerVariables()))
					.syncPlayerVariables(event.getPlayer());
	}

	@SubscribeEvent
	public void clonePlayer(PlayerEvent.Clone event) {
		PlayerVariables original = ((PlayerVariables) event.getOriginal().getCapability(PLAYER_VARIABLES_CAPABILITY, null)
				.orElse(new PlayerVariables()));
		PlayerVariables clone = ((PlayerVariables) event.getEntity().getCapability(PLAYER_VARIABLES_CAPABILITY, null).orElse(new PlayerVariables()));
		clone.itemSlot0 = original.itemSlot0;
		clone.itemSlot0quantity = original.itemSlot0quantity;
		clone.itemSlot1 = original.itemSlot1;
		clone.itemSlot1quantity = original.itemSlot1quantity;
		clone.itemSlot2 = original.itemSlot2;
		clone.itemSlot3 = original.itemSlot3;
		clone.itemSlot4 = original.itemSlot4;
		clone.itemSlot5 = original.itemSlot5;
		clone.itemSlot6 = original.itemSlot6;
		clone.itemSlot7 = original.itemSlot7;
		clone.itemSlot8 = original.itemSlot8;
		clone.itemSlot9 = original.itemSlot9;
		clone.itemSlot10 = original.itemSlot10;
		clone.itemSlot11 = original.itemSlot11;
		clone.itemSlot12 = original.itemSlot12;
		clone.itemSlot13 = original.itemSlot13;
		clone.itemSlot14 = original.itemSlot14;
		clone.itemSlot15 = original.itemSlot15;
		clone.itemSlot16 = original.itemSlot16;
		clone.itemSlot17 = original.itemSlot17;
		clone.itemSlot18 = original.itemSlot18;
		clone.itemSlot19 = original.itemSlot19;
		clone.itemSlot20 = original.itemSlot20;
		if (!event.isWasDeath()) {
		}
	}

	public static class PlayerVariablesSyncMessage {
		public PlayerVariables data;

		public PlayerVariablesSyncMessage(PacketBuffer buffer) {
			this.data = new PlayerVariables();
			new PlayerVariablesStorage().readNBT(null, this.data, null, buffer.readCompoundTag());
		}

		public PlayerVariablesSyncMessage(PlayerVariables data) {
			this.data = data;
		}

		public static void buffer(PlayerVariablesSyncMessage message, PacketBuffer buffer) {
			buffer.writeCompoundTag((CompoundNBT) new PlayerVariablesStorage().writeNBT(null, message.data, null));
		}

		public static void handler(PlayerVariablesSyncMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
			NetworkEvent.Context context = contextSupplier.get();
			context.enqueueWork(() -> {
				if (!context.getDirection().getReceptionSide().isServer()) {
					PlayerVariables variables = ((PlayerVariables) Minecraft.getInstance().player.getCapability(PLAYER_VARIABLES_CAPABILITY, null)
							.orElse(new PlayerVariables()));
					variables.itemSlot0 = message.data.itemSlot0;
					variables.itemSlot0quantity = message.data.itemSlot0quantity;
					variables.itemSlot1 = message.data.itemSlot1;
					variables.itemSlot1quantity = message.data.itemSlot1quantity;
					variables.itemSlot2 = message.data.itemSlot2;
					variables.itemSlot3 = message.data.itemSlot3;
					variables.itemSlot4 = message.data.itemSlot4;
					variables.itemSlot5 = message.data.itemSlot5;
					variables.itemSlot6 = message.data.itemSlot6;
					variables.itemSlot7 = message.data.itemSlot7;
					variables.itemSlot8 = message.data.itemSlot8;
					variables.itemSlot9 = message.data.itemSlot9;
					variables.itemSlot10 = message.data.itemSlot10;
					variables.itemSlot11 = message.data.itemSlot11;
					variables.itemSlot12 = message.data.itemSlot12;
					variables.itemSlot13 = message.data.itemSlot13;
					variables.itemSlot14 = message.data.itemSlot14;
					variables.itemSlot15 = message.data.itemSlot15;
					variables.itemSlot16 = message.data.itemSlot16;
					variables.itemSlot17 = message.data.itemSlot17;
					variables.itemSlot18 = message.data.itemSlot18;
					variables.itemSlot19 = message.data.itemSlot19;
					variables.itemSlot20 = message.data.itemSlot20;
				}
			});
			context.setPacketHandled(true);
		}
	}
}
