package com.zuxelus.energycontrol.items.cards;

import java.util.List;

import com.zuxelus.energycontrol.api.CardState;
import com.zuxelus.energycontrol.api.ICardReader;
import com.zuxelus.energycontrol.api.PanelSetting;
import com.zuxelus.energycontrol.api.PanelString;
import com.zuxelus.energycontrol.tileentities.TileEntityAverageCounter;
import com.zuxelus.energycontrol.tileentities.TileEntityEnergyCounter;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;

public class ItemCardCounter extends ItemCardBase {

	public ItemCardCounter() {
		super(ItemCardType.CARD_COUNTER, "card_counter");
	}

	@Override
	public CardState update(World world, ICardReader reader, int range, int x, int y, int z) {
		ChunkCoordinates target = reader.getTarget();
		if (target == null) 
			return CardState.NO_TARGET;

		TileEntity tileEntity = world.getTileEntity(target.posX, target.posY, target.posZ);
		if (tileEntity == null)
			return CardState.NO_TARGET;

		if (tileEntity instanceof TileEntityEnergyCounter) {
			TileEntityEnergyCounter counter = (TileEntityEnergyCounter) tileEntity;
			reader.setDouble("energy", counter.counter);
			return CardState.OK;
		}
		if (tileEntity instanceof TileEntityAverageCounter) {
			TileEntityAverageCounter avgCounter = (TileEntityAverageCounter) tileEntity;
			reader.setDouble("average", avgCounter.getClientAverage());
			reader.setInt("period", (int) avgCounter.period);
			return CardState.OK;
		}
		return CardState.NO_TARGET;
	}

	@Override
	public List<PanelString> getStringData(int settings, ICardReader reader, boolean isServer, boolean showLabels) {
		List<PanelString> result = reader.getTitleList();
		// average counter
		if (reader.hasField("average")) {
			if ((settings & 1) > 0)
				result.add(new PanelString("msg.ec.InfoPanelOutput", reader.getDouble("average"), "EU/t", showLabels));
			if ((settings & 2) > 0)
				result.add(new PanelString("msg.ec.InfoPanelPeriod", reader.getInt("period"), showLabels));
			return result;
		}
		// energy counter
		if ((settings & 1) > 0) {
			double energy = reader.getDouble("energy");
			result.add(new PanelString("msg.ec.InfoPanelEnergy", energy, "EU", showLabels));
		}
		return result;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public List<PanelSetting> getSettingsList() {
		return null;
	}
}