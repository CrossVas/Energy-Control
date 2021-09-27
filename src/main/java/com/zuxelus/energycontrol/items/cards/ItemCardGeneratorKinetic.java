package com.zuxelus.energycontrol.items.cards;

import com.zuxelus.energycontrol.api.CardState;
import com.zuxelus.energycontrol.api.ICardReader;
import com.zuxelus.energycontrol.api.PanelSetting;
import com.zuxelus.energycontrol.api.PanelString;
import com.zuxelus.energycontrol.crossmod.CrossModLoader;
import com.zuxelus.energycontrol.crossmod.ModIDs;
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

public class ItemCardGeneratorKinetic extends ItemCardBase {

	public ItemCardGeneratorKinetic() {
		super(ItemCardType.CARD_GENERATOR_KINETIC, "card_generator_kinetic");
	}

	@Override
	public CardState update(World world, ICardReader reader, int range, BlockPos pos) {
		BlockPos target = reader.getTarget();
		if (target == null) 
			return CardState.NO_TARGET;
		
		TileEntity entity = world.getTileEntity(target);
		NBTTagCompound tag = CrossModLoader.getCrossMod(ModIDs.IC2).getGeneratorKineticData(entity);
		if (tag == null || !tag.hasKey("type"))
			return CardState.NO_TARGET;

		reader.setInt("type", tag.getInteger("type"));
		switch (tag.getInteger("type")) {
		case 1: // TileEntityElectricKineticGenerator
			reader.setDouble("output", tag.getDouble("output"));
			reader.setBoolean("active", tag.getBoolean("active"));
			reader.setDouble("storage", tag.getDouble("storage"));
			reader.setDouble("maxStorage", tag.getDouble("maxStorage"));
			reader.setDouble("energy", tag.getDouble("energy"));
			reader.setDouble("maxEnergy", tag.getDouble("maxEnergy"));
			reader.setInt("items", tag.getInteger("items"));
			reader.setDouble("multiplier", tag.getDouble("multiplier"));
			break;
		case 2: // TileEntityManualKineticGenerator
			reader.setDouble("storage", tag.getDouble("storage"));
			reader.setDouble("maxStorage", tag.getDouble("maxStorage"));
			break;
		case 3: // TileEntitySteamKineticGenerator
			reader.setDouble("output", tag.getDouble("output"));
			reader.setBoolean("active", tag.getBoolean("active"));
			reader.setDouble("storage", tag.getDouble("storage"));
			reader.setDouble("maxStorage", tag.getDouble("maxStorage"));
			break;
		case 4: // TileEntityStirlingKineticGenerator
			reader.setBoolean("active", tag.getBoolean("active"));
			reader.setDouble("storage", tag.getDouble("storage"));
			reader.setDouble("maxStorage", tag.getDouble("maxStorage"));
			reader.setDouble("energy", tag.getDouble("energy"));
			reader.setDouble("maxEnergy", tag.getDouble("maxEnergy"));
			reader.setDouble("multiplier", tag.getDouble("multiplier"));
			break;
		case 5: // TileEntityWindKineticGenerator
		case 6: // TileEntityWaterKineticGenerator
			reader.setDouble("output", tag.getDouble("output"));
			reader.setDouble("wind", tag.getDouble("wind"));
			reader.setDouble("multiplier", tag.getDouble("multiplier"));
			reader.setInt("height", tag.getInteger("height"));
			reader.setDouble("health", tag.getDouble("health"));
			break;
		case 7: // TileEntitySteamKineticGenerator
			reader.setString("status", tag.getString("status"));
			reader.setDouble("output", tag.getDouble("output"));
			reader.setDouble("multiplier", tag.getDouble("multiplier"));
			reader.setInt("condProgress", tag.getInteger("condProgress"));
			break;
		}
		return CardState.OK;
	}

	@Override
	public List<PanelString> getStringData(int settings, ICardReader reader, boolean isServer, boolean showLabels) {
		List<PanelString> result = reader.getTitleList();
		switch (reader.getInt("type")) {
		case 1: // TileEntityElectricKineticGenerator
			if ((settings & 1) > 0)
				result.add(new PanelString("msg.ec.InfoPanelOutputKU", reader.getDouble("output"), showLabels));
			if ((settings & 2) > 0)
				result.add(new PanelString("msg.ec.InfoPanelEnergyKU", reader.getDouble("storage"), showLabels));
			if ((settings & 4) > 0)
				result.add(new PanelString("msg.ec.InfoPanelCapacityKU", reader.getDouble("maxStorage"), showLabels));
			if ((settings & 8) > 0)
				result.add(new PanelString("msg.ec.InfoPanelStorageEU", reader.getInt("energy"), showLabels));
			if ((settings & 4) > 0)
				result.add(new PanelString("msg.ec.InfoPanelCapacityEU", reader.getDouble("maxEnergy"), showLabels));
			if ((settings & 16) > 0)
				result.add(new PanelString("msg.ec.InfoPanelMotors", reader.getInt("items"), showLabels));
			if ((settings & 32) > 0)
				result.add(new PanelString("msg.ec.InfoPanelMultiplier", reader.getDouble("multiplier"), showLabels));
				addOnOff(result, isServer, reader.getBoolean("active"));
			break;
		case 2: // TileEntityManualKineticGenerator
			if ((settings & 2) > 0)
				result.add(new PanelString("msg.ec.InfoPanelEnergyKU", reader.getDouble("storage"), showLabels));
			if ((settings & 4) > 0)
				result.add(new PanelString("msg.ec.InfoPanelCapacityKU", reader.getDouble("maxStorage"), showLabels));
			break;
		case 3: // TileEntitySteamKineticGenerator
			if ((settings & 1) > 0)
				result.add(new PanelString("msg.ec.InfoPanelOutputKU", reader.getDouble("output"), showLabels));
			if ((settings & 8) > 0)
				result.add(new PanelString("msg.ec.InfoPanelStorageL", reader.getDouble("storage"), showLabels));
			if ((settings & 4) > 0)
				result.add(new PanelString("msg.ec.InfoPanelCapacityL", reader.getDouble("maxStorage"), showLabels));
			addOnOff(result, isServer, reader.getBoolean("active"));
			break;
		case 4: // TileEntityStirlingKineticGenerator
			if ((settings & 2) > 0)
				result.add(new PanelString("msg.ec.InfoPanelEnergyKU", reader.getDouble("storage"), showLabels));
			if ((settings & 4) > 0)
				result.add(new PanelString("msg.ec.InfoPanelCapacityKU", reader.getDouble("maxStorage"), showLabels));
			if ((settings & 8) > 0)
				result.add(new PanelString("msg.ec.InfoPanelStorageHU", reader.getInt("energy"), showLabels));
			if ((settings & 4) > 0)
				result.add(new PanelString("msg.ec.InfoPanelCapacityHU", reader.getDouble("maxEnergy"), showLabels));
			if ((settings & 32) > 0)
				result.add(new PanelString("msg.ec.InfoPanelMultiplier", reader.getDouble("multiplier"), showLabels));
				addOnOff(result, isServer, reader.getBoolean("active"));
			break;
		case 5: // TileEntityWindKineticGenerator
			if ((settings & 1) > 0)
				result.add(new PanelString("msg.ec.InfoPanelOutputKU", reader.getDouble("output"), showLabels));
			if ((settings & 2) > 0)
				result.add(new PanelString("msg.ec.InfoPanelWindStrength", reader.getDouble("wind"), showLabels));
			if ((settings & 32) > 0)
				result.add(new PanelString("msg.ec.InfoPanelMultiplier", reader.getDouble("multiplier"), showLabels));
			if ((settings & 64) > 0)
				result.add(new PanelString("msg.ec.InfoPanelHeight", reader.getInt("height"), showLabels));
			if ((settings & 16) > 0)
				result.add(new PanelString("msg.ec.InfoPanelRotorHealth", reader.getDouble("health"), showLabels));
			break;
		case 6: // TileEntityWaterKineticGenerator
			if ((settings & 1) > 0)
				result.add(new PanelString("msg.ec.InfoPanelOutputKU", reader.getDouble("output"), showLabels));
			if ((settings & 2) > 0)
				result.add(new PanelString("msg.ec.InfoPanelWaterFlow", reader.getDouble("wind"), showLabels));
			if ((settings & 32) > 0)
				result.add(new PanelString("msg.ec.InfoPanelMultiplier", reader.getDouble("multiplier"), showLabels));
			if ((settings & 64) > 0)
				result.add(new PanelString("msg.ec.InfoPanelHeight", reader.getInt("height"), showLabels));
			if ((settings & 16) > 0)
				result.add(new PanelString("msg.ec.InfoPanelRotorHealth", reader.getDouble("health"), showLabels));
			break;
		case 7: // TileEntitySteamKineticGenerator
			if ((settings & 2) > 0)
				result.add(new PanelString(reader.getString("status"), "", true));
			if ((settings & 1) > 0)
				result.add(new PanelString("msg.ec.InfoPanelOutputKU", reader.getDouble("output"), showLabels));
			if ((settings & 32) > 0)
				result.add(new PanelString("msg.ec.InfoPanelMultiplier", reader.getDouble("multiplier"), showLabels));
			if ((settings & 8) > 0)
				result.add(new PanelString("msg.ec.InfoPanelWater", String.format("%s mB", reader.getInt("condProgress") / 100.0F), showLabels));
			break;
		}
		return result;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public List<PanelSetting> getSettingsList() {
		List<PanelSetting> result = new ArrayList<>(8);
		result.add(new PanelSetting(I18n.format("msg.ec.cbInfoPanelOutput"), 1));
		result.add(new PanelSetting(I18n.format("msg.ec.cbInfoPanelEnergy"), 2));
		result.add(new PanelSetting(I18n.format("msg.ec.cbInfoPanelCapacity"), 4));
		result.add(new PanelSetting(I18n.format("msg.ec.cbInfoPanelStorage"), 8));
		result.add(new PanelSetting(I18n.format("msg.ec.cbInfoPanelItems"), 16));
		result.add(new PanelSetting(I18n.format("msg.ec.cbInfoPanelMultiplier"), 32));
		result.add(new PanelSetting(I18n.format("msg.ec.cbInfoPanelHeight"), 64));
		return result;
	}
}
