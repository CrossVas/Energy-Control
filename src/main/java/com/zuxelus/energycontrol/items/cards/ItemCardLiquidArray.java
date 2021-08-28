package com.zuxelus.energycontrol.items.cards;

import com.zuxelus.energycontrol.api.CardState;
import com.zuxelus.energycontrol.api.ICardReader;
import com.zuxelus.energycontrol.api.PanelSetting;
import com.zuxelus.energycontrol.api.PanelString;
import com.zuxelus.energycontrol.crossmod.CrossModLoader;
import com.zuxelus.energycontrol.utils.StringUtils;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

public class ItemCardLiquidArray extends ItemCardBase {
	private static final int STATUS_NOT_FOUND = Integer.MIN_VALUE;
	private static final int STATUS_OUT_OF_RANGE = Integer.MIN_VALUE + 1;
	
	public ItemCardLiquidArray() {
		super(ItemCardType.CARD_LIQUID_ARRAY, "card_liquid_array");
	}

	@Override
	public CardState update(World world, ICardReader reader, int range, BlockPos pos) {
		int cardCount = reader.getCardCount();
		if (cardCount == 0)
			return CardState.INVALID_CARD;

		double totalAmount = 0.0;

		boolean foundAny = false;
		boolean outOfRange = false;
		for (int i = 0; i < cardCount; i++) {
			BlockPos target = getCoordinates(reader, i);
			int dx = target.getX() - pos.getX();
			int dy = target.getY() - pos.getY();
			int dz = target.getZ() - pos.getZ();
			if (Math.abs(dx) <= range && Math.abs(dy) <= range && Math.abs(dz) <= range) {
				IFluidTank storage = CrossModLoader.getTankAt(world, target);
				if (storage != null) {
					FluidStack stack = storage.getFluid(); 
					if (stack != null) {
						totalAmount += stack.amount;
						reader.setInt(String.format("_%damount", i),stack.amount);
						String name = "";
						if (stack.amount > 0)
							name = FluidRegistry.getFluidName(stack);
						reader.setString(String.format("_%dname", i), name);
					}
					reader.setInt(String.format("_%dcapacity", i), storage.getCapacity());
					foundAny = true;
				} else
					reader.setInt(String.format("_%damount", i), STATUS_NOT_FOUND);
			} else {
				reader.setInt(String.format("_%damount", i), STATUS_OUT_OF_RANGE);
				outOfRange = true;
			}
		}
		reader.setDouble("energy", totalAmount);
		if (!foundAny) {
			if (outOfRange)
				return CardState.OUT_OF_RANGE;
			return CardState.NO_TARGET;
		}
		return CardState.OK;
	}

	@Override
	public List<PanelString> getStringData(int displaySettings, ICardReader reader, boolean isServer, boolean showLabels) {
		List<PanelString> result = reader.getTitleList();
		double totalAmount = 0;
		double totalCapacity = 0;
		boolean showName = (displaySettings & 1) > 0;
		boolean showAmount = true;
		boolean showFree = (displaySettings & 2) > 0;
		boolean showCapacity = (displaySettings & 4) > 0;
		boolean showPercentage = (displaySettings & 8) > 0;
		boolean showEach = (displaySettings & 16) > 0;
		boolean showSummary = (displaySettings & 32) > 0;
		for (int i = 0; i < reader.getInt("cardCount"); i++) {
			int amount = reader.getInt(String.format("_%damount", i));
			int capacity = reader.getInt(String.format("_%dcapacity", i));
			boolean isOutOfRange = amount == STATUS_OUT_OF_RANGE;
			boolean isNotFound = amount == STATUS_NOT_FOUND;
			if (showSummary && !isOutOfRange && !isNotFound) {
				totalAmount += amount;
				totalCapacity += capacity;
			}

			if (showEach) {
				if (isOutOfRange) {
					result.add(new PanelString(StringUtils.getFormattedKey("msg.ec.InfoPanelOutOfRangeN", i + 1)));
				} else if (isNotFound) {
					result.add(new PanelString(StringUtils.getFormattedKey("msg.ec.InfoPanelNotFoundN", i + 1)));
				} else {
					if (showName) {
						if (showLabels)
							result.add(new PanelString(StringUtils.getFormattedKey("msg.ec.InfoPanelLiquidNameN", i + 1,
									reader.getString(String.format("_%dname", i)))));
						else
							result.add(new PanelString(StringUtils.getFormatted("", amount, false)));
					}
					if (showAmount) {
						if (showLabels)
							result.add(new PanelString(StringUtils.getFormattedKey("msg.ec.InfoPanelLiquidN", i + 1,
									StringUtils.getFormatted("", amount, false))));
						else
							result.add(new PanelString(StringUtils.getFormatted("", amount, false)));
					}
					if (showFree) {
						if (showLabels)
							result.add(new PanelString(StringUtils.getFormattedKey("msg.ec.InfoPanelLiquidFreeN", i + 1,
									StringUtils.getFormatted("", capacity - amount, false))));
						else
							result.add(new PanelString(StringUtils.getFormatted("", capacity - amount, false)));
					}
					if (showCapacity) {
						if (showLabels)
							result.add(new PanelString(StringUtils.getFormattedKey("msg.ec.InfoPanelLiquidCapacityN",
									i + 1, StringUtils.getFormatted("", capacity, false))));
						else
							result.add(new PanelString(StringUtils.getFormatted("", capacity, false)));
					}
					if (showPercentage) {
						if (showLabels)
							result.add(new PanelString(StringUtils.getFormattedKey("msg.ec.InfoPanelLiquidPercentageN",
									i + 1, StringUtils.getFormatted("",
											capacity == 0 ? 100 : (((double) amount / capacity) * 100), false))));
						else
							result.add(new PanelString(StringUtils.getFormatted("",
									capacity == 0 ? 100 : (((double) amount / capacity) * 100), false)));
					}
				}
			}
		}
		if (showSummary) {
			if (showAmount)
				result.add(new PanelString("msg.ec.InfoPanelLiquidAmount", totalAmount, showLabels));
			if (showFree)
				result.add(new PanelString("msg.ec.InfoPanelLiquidFree", totalCapacity - totalAmount, showLabels));
			if (showName)
				result.add(new PanelString("msg.ec.InfoPanelLiquidCapacity", totalCapacity, showLabels));
			if (showPercentage)
				result.add(new PanelString("msg.ec.InfoPanelLiquidPercentage",
						totalCapacity == 0 ? 100 : ((totalAmount / totalCapacity) * 100), showLabels));
		}
		return result;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public List<PanelSetting> getSettingsList() {
		List<PanelSetting> result = new ArrayList<>(6);
		result.add(new PanelSetting(I18n.format("msg.ec.cbInfoPanelLiquidName"), 1));
		result.add(new PanelSetting(I18n.format("msg.ec.cbInfoPanelLiquidFree"), 2));
		result.add(new PanelSetting(I18n.format("msg.ec.cbInfoPanelLiquidCapacity"), 4));
		result.add(new PanelSetting(I18n.format("msg.ec.cbInfoPanelLiquidPercentage"), 8));
		result.add(new PanelSetting(I18n.format("msg.ec.cbInfoPanelEachCard"), 16));
		result.add(new PanelSetting(I18n.format("msg.ec.cbInfoPanelTotal"), 32));
		return result;
	}

	@Override
	public boolean isRemoteCard() {
		return false;
	}

	@Override
	public int getKitId() {
		return ItemCardType.KIT_LIQUID;
	}
}
