package com.zuxelus.energycontrol.items.cards;

import java.util.ArrayList;
import java.util.List;

import com.zuxelus.energycontrol.api.CardState;
import com.zuxelus.energycontrol.api.ICardReader;
import com.zuxelus.energycontrol.api.PanelSetting;
import com.zuxelus.energycontrol.api.PanelString;
import com.zuxelus.energycontrol.crossmod.CrossModLoader;
import com.zuxelus.energycontrol.crossmod.ModIDs;
import com.zuxelus.energycontrol.utils.DataHelper;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;

public class ItemCardDraconicEvolution extends ItemCardBase {
	public ItemCardDraconicEvolution() {
		super(ItemCardType.CARD_DRACONIC_EVOLUTION, "card_draconic_evolution");
	}

	@Override
	public CardState update(World world, ICardReader reader, int range, int x, int y, int z) {
		ChunkCoordinates target = reader.getTarget();
		if (target == null)
			return CardState.NO_TARGET;

		TileEntity te = world.getTileEntity(target.posX, target.posY, target.posZ);
		NBTTagCompound tag = CrossModLoader.getCrossMod(ModIDs.DRACONIC_EVOLUTION).getCardData(te);
		if (tag == null)
			return CardState.NO_TARGET;
		reader.reset();
		reader.copyFrom(tag);
		return CardState.OK;
	}

	@Override
	public List<PanelString> getStringData(int settings, ICardReader reader, boolean isServer, boolean showLabels) {
		List<PanelString> result = reader.getTitleList();
		if (reader.hasField(DataHelper.ENERGY) && (settings & 2) > 0)
			result.add(new PanelString("msg.ec.InfoPanelEnergy", reader.getLong(DataHelper.ENERGY), "RF", showLabels));
		if (reader.hasField(DataHelper.CAPACITY) && (settings & 2) > 0)
			result.add(new PanelString("msg.ec.InfoPanelCapacity", reader.getLong(DataHelper.CAPACITY), "RF", showLabels));
		if (reader.hasField("diff") && (settings & 64) > 0)
			result.add(new PanelString("msg.ec.InfoPanelDifference", reader.getLong("diff"), "RF/t", showLabels));
		if (reader.hasField("tier") && (settings & 64) > 0)
			result.add(new PanelString("msg.ec.InfoPanelTier", reader.getInt("tier"), showLabels));
		if (reader.hasField("connections") && (settings & 64) > 0)
			result.add(new PanelString("msg.ec.InfoPanelConnections", reader.getInt("connections"), showLabels));
		if (reader.hasField("flowLow") && (settings & 64) > 0)
			result.add(new PanelString("msg.ec.InfoPanelSignalLow", reader.getInt("flowLow"), "RF/t", showLabels));
		if (reader.hasField("flowHigh") && (settings & 64) > 0)
			result.add(new PanelString("msg.ec.InfoPanelSignalHigh", reader.getInt("flowHigh"), "RF/t", showLabels));
		if (reader.hasField("status") && (settings & 1) > 0)
			result.add(new PanelString("msg.ec.InfoPanelStatus", reader.getString("status"), showLabels));
		if (reader.hasField("temp") && (settings & 8) > 0)
			result.add(new PanelString("msg.ec.InfoPanelCoreHeat", reader.getInt("temp"), "�C", showLabels));
		if (reader.hasField(DataHelper.CONSUMPTION) && (settings & 4) > 0)
			result.add(new PanelString("msg.ec.InfoPanelConsumption", reader.getDouble(DataHelper.CONSUMPTION), "RF/t", showLabels));
		if (reader.hasField(DataHelper.OUTPUT) && (settings & 4) > 0)
			result.add(new PanelString("msg.ec.InfoPanelOutput", reader.getDouble(DataHelper.OUTPUT), "RF/t", showLabels));
		if (reader.hasField("shield") && (settings & 64) > 0)
			result.add(new PanelString("msg.ec.InfoPanelFieldStrength", reader.getDouble("shield"), "%", showLabels));
		if (reader.hasField("saturation") && (settings & 64) > 0)
			result.add(new PanelString("msg.ec.InfoPanelSaturation", reader.getDouble("saturation"), "%", showLabels));
		if (reader.hasField("fuelMax") && (settings & 16) > 0)
			result.add(new PanelString("msg.ec.InfoPanelFuel", reader.getInt("fuelMax"), showLabels));
		if (reader.hasField("fuel") && (settings & 16) > 0)
			result.add(new PanelString("msg.ec.InfoPanelConvertedFuel", reader.getInt("fuel"), showLabels));
		if (reader.hasField("fuelRate") && (settings & 16) > 0)
			result.add(new PanelString("msg.ec.InfoPanelConversionRate", reader.getDouble("fuelRate") * 1000000, "nb/t", showLabels));
		if (reader.hasField("diam") && (settings & 64) > 0)
			result.add(new PanelString("msg.ec.InfoPanelDiameter", reader.getDouble("diam"), showLabels));
		return result;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public List<PanelSetting> getSettingsList() {
		List<PanelSetting> result = new ArrayList<PanelSetting>(6);
		result.add(new PanelSetting(I18n.format("msg.ec.cbInfoPanelStatus"), 1));
		result.add(new PanelSetting(I18n.format("msg.ec.cbInfoPanelEnergy"), 2));
		result.add(new PanelSetting(I18n.format("msg.ec.cbInfoPanelOutput"), 4));
		result.add(new PanelSetting(I18n.format("msg.ec.cbInfoPanelHeat"), 8));
		result.add(new PanelSetting(I18n.format("msg.ec.cbInfoPanelFuel"), 16));
		//result.add(new PanelSetting(I18n.format("msg.ec.cbInfoPanelOnOff"), 32));
		result.add(new PanelSetting(I18n.format("msg.ec.cbInfoPanelOther"), 64));
		return result;
	}
}