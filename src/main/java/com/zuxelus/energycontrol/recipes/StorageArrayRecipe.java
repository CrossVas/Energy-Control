package com.zuxelus.energycontrol.recipes;

import com.zuxelus.energycontrol.init.ModItems;
import com.zuxelus.energycontrol.items.cards.ItemCardMain;
import com.zuxelus.energycontrol.items.cards.ItemCardReader;
import com.zuxelus.energycontrol.items.cards.ItemCardType;
import ic2.api.item.IC2Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Vector;

public class StorageArrayRecipe extends net.minecraftforge.registries.IForgeRegistryEntry.Impl<IRecipe> implements IRecipe {

	@Override
	public boolean matches(InventoryCrafting inventory, World world) {
		return !getCraftingResult(inventory).isEmpty();
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting inventory) {
		int inventoryLength = inventory.getSizeInventory();
		int cardCount = 0;
		int arrayCount = 0;
		int cardCountLiquid = 0;
		int arrayCountLiquid = 0;
		int cardCountGenerator = 0;
		int arrayCountGenerator = 0;
		ItemStack array = null;
		Vector<ItemStack> cards = new Vector<>();
		for (int i = 0; i < inventoryLength; i++) {
			ItemStack itemStack = inventory.getStackInSlot(i);
			if (itemStack.isEmpty())
				continue;
			if (!(itemStack.getItem() instanceof ItemCardMain))
				return ItemStack.EMPTY;

			switch (itemStack.getItemDamage())
			{
			case ItemCardType.CARD_ENERGY:
				cards.add(itemStack);
				cardCount++;
				break;
			case ItemCardType.CARD_LIQUID:
				cards.add(itemStack);
				cardCountLiquid++;
				break;
			case ItemCardType.CARD_GENERATOR:
				cards.add(itemStack);
				cardCountGenerator++;
				break;
			case ItemCardType.CARD_ENERGY_ARRAY:
				array = itemStack;				
				arrayCount++;
				break;
			case ItemCardType.CARD_LIQUID_ARRAY:
				array = itemStack;
				arrayCountLiquid++;
				break;
			case ItemCardType.CARD_GENERATOR_ARRAY:
				array = itemStack;
				arrayCountGenerator++;
				break;
			}
		}
		if (((cardCount + arrayCount) != 0 && (cardCountLiquid + arrayCountLiquid) != 0)
				|| ((cardCount + arrayCount) != 0 && (cardCountGenerator + arrayCountGenerator) != 0)
				|| ((cardCountLiquid + arrayCountLiquid) != 0 && (cardCountGenerator + arrayCountGenerator) != 0))
			return ItemStack.EMPTY;

		ItemStack stack = getCraftingResult(cardCount, arrayCount, ItemCardType.CARD_ENERGY_ARRAY, cards, array);
		if (!stack.isEmpty())
			return stack;
		stack = getCraftingResult(cardCountLiquid, arrayCountLiquid, ItemCardType.CARD_LIQUID_ARRAY, cards, array);
		if (!stack.isEmpty())
			return stack;
		return getCraftingResult(cardCountGenerator, arrayCountGenerator, ItemCardType.CARD_GENERATOR_ARRAY, cards, array);
	}

	private ItemStack getCraftingResult(int cardCount, int arrayCount, int type, Vector<ItemStack> cards, ItemStack array) {
		if (cardCount >= 2 && cardCount <= 16 && arrayCount == 0) {
			ItemStack itemStack = new ItemStack(ModItems.itemCard, 1, type);
			initArray(itemStack, cards);
			return itemStack;
		}
		if (cardCount == 0 && arrayCount == 1) {
			int cnt = new ItemCardReader(array).getInt("cardCount");
			if (cnt > 0)
				return new ItemStack(IC2Items.getItem("crafting","circuit").getItem(), cnt, IC2Items.getItem("crafting","circuit").getItemDamage());
		} else if (arrayCount == 1 && cardCount > 0) {
			int cnt = new ItemCardReader(array).getInt("cardCount");
			if (cnt + cardCount <= 16) {
				ItemStack itemStack = new ItemStack(ModItems.itemCard, 1, type);
				itemStack.setTagCompound(array.getTagCompound().copy());
				initArray(itemStack, cards);
				return itemStack;
			}
		}
		return ItemStack.EMPTY;
	}

	private static void initArray(ItemStack stack, Vector<ItemStack> cards) {
		if (stack.getItemDamage() != ItemCardType.CARD_ENERGY_ARRAY
				&& stack.getItemDamage() != ItemCardType.CARD_LIQUID_ARRAY
				&& stack.getItemDamage() != ItemCardType.CARD_GENERATOR_ARRAY)
			return;
		ItemCardReader reader = new ItemCardReader(stack);
		int cardCount = reader.getCardCount();
		for (ItemStack subCard : cards) {
			ItemCardReader wrapper = new ItemCardReader(subCard);
			BlockPos target = wrapper.getTarget();
			if (target == null)
				continue;
			reader.setInt(String.format("_%dx", cardCount), target.getX());
			reader.setInt(String.format("_%dy", cardCount), target.getY());
			reader.setInt(String.format("_%dz", cardCount), target.getZ());
			//reader.setInt(String.format("_%dtargetType", cardCount), wrapper.getInt("targetType"));
			cardCount++;
		}
		reader.setInt("cardCount", cardCount);
	}

	@Override
	public ItemStack getRecipeOutput() {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean isDynamic() {
		return true;
	}

	@Override
	public boolean canFit(int width, int height) {
		return width * height >= 2;
	}
}
