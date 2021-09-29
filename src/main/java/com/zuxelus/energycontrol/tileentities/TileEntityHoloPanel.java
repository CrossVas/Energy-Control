package com.zuxelus.energycontrol.tileentities;

import com.zuxelus.energycontrol.blocks.HoloPanel;
import com.zuxelus.energycontrol.containers.ContainerHoloPanel;
import com.zuxelus.energycontrol.init.ModItems;
import com.zuxelus.energycontrol.init.ModTileEntityTypes;
import com.zuxelus.energycontrol.items.cards.ItemCardMain;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TileEntityHoloPanel extends TileEntityInfoPanel {
	private static final byte SLOT_CARD = 0;
	private static final byte SLOT_UPGRADE_RANGE = 1;
	private static final byte SLOT_UPGRADE_POWER = 2;

	public TileEntityHoloPanel() {
		super(ModTileEntityTypes.holo_panel.get());
	}

	public int getPower() {
		ItemStack stack = getItem(SLOT_UPGRADE_POWER);
		if (stack.isEmpty())
			return 1;
		return stack.getCount() + 1;
	}

	@Override
	public void notifyBlockUpdate() {
		BlockState blockstate = level.getBlockState(worldPosition);
		if (blockstate.getValue(HoloPanel.ACTIVE) == powered) {
			level.sendBlockUpdated(worldPosition, blockstate, blockstate, 2);
			return;
		}
		BlockState newState = blockstate.getBlock().defaultBlockState()
				.setValue(HoloPanel.FACING, blockstate.getValue(HoloPanel.FACING))
				.setValue(HoloPanel.ACTIVE, powered);
		level.setBlock(worldPosition, newState, 3);
	}

	@Override
	protected void readProperties(CompoundNBT tag) {
		boolean old = powered;
		super.readProperties(tag);
		if (powered != old && level.isClientSide) {
			BlockState iblockstate = level.getBlockState(worldPosition);
			BlockState newState = iblockstate.getBlock().defaultBlockState()
					.setValue(HoloPanel.FACING, iblockstate.getValue(HoloPanel.FACING))
					.setValue(HoloPanel.ACTIVE, powered);
			level.setBlock(worldPosition, newState, 3);
		}
	}

	@Override
	public boolean isItemValid(int index, ItemStack stack) { // ISlotItemFilter
		switch (index) {
		case SLOT_CARD:
			return ItemCardMain.isCard(stack);
		case SLOT_UPGRADE_RANGE:
			return stack.getItem().equals(ModItems.upgrade_range.get());
		case SLOT_UPGRADE_POWER:
			return stack.getItem().equals(ModItems.advanced_circuit.get());
		default:
			return false;
		}
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public AxisAlignedBB getRenderBoundingBox() {
		if (screen == null)
			return new AxisAlignedBB(worldPosition.offset(0, 0, 0), worldPosition.offset(1, 1, 1));
		return new AxisAlignedBB(new BlockPos(screen.minX, screen.minY, screen.minZ), new BlockPos(screen.maxX + 1, screen.maxY + getPower(), screen.maxZ + 1));
	}

	// INamedContainerProvider
	@Override
	public Container createMenu(int windowId, PlayerInventory inventory, PlayerEntity player) {
		return new ContainerHoloPanel(windowId, inventory, this);
	}

	@Override
	public ITextComponent getDisplayName() {
		return new TranslationTextComponent(ModItems.holo_panel.get().getDescriptionId());
	}
}
