
package net.mcreator.minecraftutilities.itemgroup;

import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemGroup;

import net.mcreator.minecraftutilities.MinecraftutilitiesModElements;

@MinecraftutilitiesModElements.ModElement.Tag
public class MinecraftUtilitiesItemGroup extends MinecraftutilitiesModElements.ModElement {
	public MinecraftUtilitiesItemGroup(MinecraftutilitiesModElements instance) {
		super(instance, 14);
	}

	@Override
	public void initElements() {
		tab = new ItemGroup("tabminecraft_utilities") {
			@OnlyIn(Dist.CLIENT)
			@Override
			public ItemStack createIcon() {
				return new ItemStack(Items.DIAMOND);
			}

			@OnlyIn(Dist.CLIENT)
			public boolean hasSearchBar() {
				return false;
			}
		};
	}

	public static ItemGroup tab;
}
