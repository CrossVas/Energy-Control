package com.zuxelus.energycontrol.crossmod.opencomputers;

import java.util.ArrayList;
import java.util.List;

import com.zuxelus.energycontrol.api.PanelString;
import com.zuxelus.energycontrol.items.ItemUpgrade;
import com.zuxelus.energycontrol.items.cards.ItemCardMain;
import com.zuxelus.energycontrol.items.cards.ItemCardReader;
import com.zuxelus.energycontrol.tileentities.TileEntityAdvancedInfoPanel;

import li.cil.oc.api.driver.NamedBlock;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.prefab.DriverSidedTileEntity;
import li.cil.oc.integration.ManagedTileEntityEnvironment;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class DriverAdvancedInfoPanel extends DriverSidedTileEntity {

	@Override
	public Class<?> getTileEntityClass() {
		return TileEntityAdvancedInfoPanel.class;
	}

	@Override
	public ManagedEnvironment createEnvironment(World world, int x, int y, int z, ForgeDirection dir) {
		return new Environment((TileEntityAdvancedInfoPanel) world.getTileEntity(x, y, z));
	}

	public static final class Environment extends ManagedTileEntityEnvironment<TileEntityAdvancedInfoPanel> implements NamedBlock {
		public Environment(final TileEntityAdvancedInfoPanel tileentity) {
			super(tileentity, TileEntityAdvancedInfoPanel.NAME);
		}

		@Override
		public String preferredName() {
			return TileEntityAdvancedInfoPanel.NAME;
		}

		@Override
		public int priority() {
			return 0;
		}

		@Callback(doc = "function():boolean -- Check if panel is active.")
		public Object[] isActive(final Context context, final Arguments args) {
			return new Object[] { tileEntity.powered };
		}

		@Callback(doc = "function():number -- Get panel range.")
		public Object[] getRange(final Context context, final Arguments args) {
			ItemStack itemStack = tileEntity.getStackInSlot(tileEntity.getSlotUpgradeRange());
			int upgradeCountRange = 0;
			if (itemStack != null && itemStack.getItem() instanceof ItemUpgrade && itemStack.getItemDamage() == ItemUpgrade.DAMAGE_RANGE)
				upgradeCountRange = itemStack.stackSize;
			return new Object[] { ItemCardMain.LOCATION_RANGE * (int) Math.pow(2, Math.min(upgradeCountRange, 7)) };
		}

		@Callback(doc = "function():list<string> -- Get card data.")
		public Object[] getCardData(final Context context, final Arguments args) {
			return new Object[] { tileEntity.getPanelStringList(false) };
		}

		@Callback(doc = "function():list<string> -- Get raw card data.")
		public Object[] getCardDataRaw(final Context context, final Arguments args) {
			return new Object[] { tileEntity.getPanelStringList(true) };
		}

		@Callback(doc = "function():number -- Get background color.")
		public Object[] getColorBack(final Context context, final Arguments args) {
			return new Object[] { tileEntity.getColorBackground() };
		}

		@Callback(doc = "function():number -- Get font color.")
		public Object[] getColorText(final Context context, final Arguments args) {
			return new Object[] { tileEntity.getColorText() };
		}

		@Callback(doc = "function(number) -- Set background color.")
		public Object[] setColorBack(final Context context, final Arguments args) {
			int newColor = args.checkInteger(0);
			if (newColor >= 0 && newColor < 16)
				tileEntity.setColorBackground(newColor);
			return null;
		}

		@Callback(doc = "function(number) -- Set font color.")
		public Object[] setColorText(final Context context, final Arguments args) {
			int value = args.checkInteger(0);
			if (value >= 0 && value < 16)
				tileEntity.setColorText(value);
			return null;
		}

		@Callback(doc = "function(number):string -- Get card title.")
		public Object[] getCardTitle(final Context context, final Arguments args) {
			int value = args.checkInteger(0);
			if (value < 0 || value > 2)
				return new Object[] { "" };
			ItemStack stack = tileEntity.getStackInSlot(value);
			if (!ItemCardMain.isCard(stack))
				return new Object[] { "" }; 
			return new Object[] { new ItemCardReader(stack).getTitle() };
		}

		@Callback(doc = "function(number,string) -- Set card title.")
		public Object[] setCardTitle(final Context context, final Arguments args) {
			int value = args.checkInteger(0);
			String title = args.checkString(1);
			if (value < 0 || value > 2)
				return null;
			ItemStack stack = tileEntity.getStackInSlot(value);
			if (ItemCardMain.isCard(stack))
				new ItemCardReader(stack).setTitle(title);
			return null;
		}

		@Callback(doc = "function():number -- Get panel thickness.")
		public Object[] getThickness(final Context context, final Arguments args) {
			return new Object[] { ((int) tileEntity.thickness) };
		}

		@Callback(doc = "function(number) -- Set panel thickness.")
		public Object[] setThickness(final Context context, final Arguments args) {
			int value = args.checkInteger(0);
			if (value > 0 && value <= 16) {
				tileEntity.thickness = (byte) value;
				tileEntity.updateTileEntity();
			}
			return null;
		}

		@Callback(doc = "function():number -- Get panel horizonal rotation.")
		public Object[] getRotHor(final Context context, final Arguments args) {
			return new Object[] { ((int) tileEntity.rotateHor / 7) };
		}

		@Callback(doc = "function(number) -- Set panel horizonal rotation.")
		public Object[] setRotHor(final Context context, final Arguments args) {
			int value = args.checkInteger(0);
			if (value > -9 && value < 9) {
				tileEntity.rotateHor = (byte) (value * 7);
				tileEntity.updateTileEntity();
			}
			return null;
		}

		@Callback(doc = "function():number -- Get panel vertical rotation.")
		public Object[] getRotVert(final Context context, final Arguments args) {
			return new Object[] { ((int) tileEntity.rotateVert / 7) };
		}

		@Callback(doc = "function(number) -- Set panel vertical rotation.")
		public Object[] setRotVert(final Context context, final Arguments args) {
			int value = args.checkInteger(0);
			if (value > -9 && value < 9) {
				tileEntity.rotateVert = (byte) (value * 7);
				tileEntity.updateTileEntity();
			}
			return null;
		}
	}
}
