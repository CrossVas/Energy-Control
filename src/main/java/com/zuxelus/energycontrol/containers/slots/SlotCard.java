package com.zuxelus.energycontrol.containers.slots;

import com.zuxelus.zlib.containers.slots.SlotFilter;

import net.minecraft.inventory.IInventory;
import net.minecraft.util.IIcon;

public class SlotCard extends SlotFilter {
	public static IIcon slotIcon;

	public SlotCard(IInventory inventory, int slotIndex, int x, int y) {
		super(inventory, slotIndex, x, y);
	}

	@Override
	public IIcon getBackgroundIconIndex() {
		return slotIcon;
	}
}