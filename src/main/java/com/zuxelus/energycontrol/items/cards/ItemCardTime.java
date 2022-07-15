package com.zuxelus.energycontrol.items.cards;

import java.util.ArrayList;
import java.util.List;

import com.zuxelus.energycontrol.api.CardState;
import com.zuxelus.energycontrol.api.ICardReader;
import com.zuxelus.energycontrol.api.PanelSetting;
import com.zuxelus.energycontrol.api.PanelString;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemCardTime extends ItemCardBase {

	public ItemCardTime() {
		super(ItemCardType.CARD_TIME, "card_time");
	}

	@Override
	public CardState update(World world, ICardReader reader, int range, BlockPos pos) {
		reader.setInt("time", (int) ((world.getWorldTime() + 6000) % 24000));
		return CardState.OK;
	}

	@Override
	public List<PanelString> getStringData(int settings, ICardReader reader, boolean isServer, boolean showLabels) {
		List<PanelString> result = reader.getTitleList();
		int time = reader.getInt("time");
		int hours = time / 1000;
		int minutes = (time % 1000) * 6 / 100;
		String suffix = "";
		if ((settings & 1) == 0) {
			suffix = hours < 12 ? "AM" : "PM";
			hours %= 12;
			if (hours == 0)
				hours += 12;
		}
		result.add(new PanelString("msg.ec.InfoPanelTime",String.format("%02d:%02d%s", hours, minutes, suffix), showLabels));
		return result;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public List<PanelSetting> getSettingsList(ItemStack stack) {
		List<PanelSetting> result = new ArrayList<>(1);
		result.add(new PanelSetting(I18n.format("msg.ec.cb24h"), 1));
		return result;
	}

	@Override
	public boolean isRemoteCard(ItemStack stack) {
		return false;
	}
}
