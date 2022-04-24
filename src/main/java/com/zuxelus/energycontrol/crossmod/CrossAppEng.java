package com.zuxelus.energycontrol.crossmod;

import appeng.api.networking.energy.IAEPowerStorage;
import com.zuxelus.energycontrol.items.cards.ItemCardType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class CrossAppEng extends CrossModBase {

	@Override
	public NBTTagCompound getEnergyData(TileEntity te) {
		if (te instanceof IAEPowerStorage) {
			NBTTagCompound tag = new NBTTagCompound();
			IAEPowerStorage storage = (IAEPowerStorage) te;
			tag.setString("euType", "AE");
			tag.setDouble("storage", storage.getAECurrentPower());
			tag.setDouble("maxStorage", storage.getAEMaxPower());
			return tag;
		}
		return null;
	}
}
