package com.zuxelus.energycontrol.tileentities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.zuxelus.energycontrol.EnergyControl;
import com.zuxelus.energycontrol.api.CardState;
import com.zuxelus.energycontrol.api.ICardReader;
import com.zuxelus.energycontrol.api.IHasBars;
import com.zuxelus.energycontrol.api.ITouchAction;
import com.zuxelus.energycontrol.api.PanelString;
import com.zuxelus.energycontrol.blocks.BlockDamages;
import com.zuxelus.energycontrol.blocks.HoloPanelExtender;
import com.zuxelus.energycontrol.blocks.InfoPanelExtender;
import com.zuxelus.energycontrol.init.ModItems;
import com.zuxelus.energycontrol.items.ItemUpgrade;
import com.zuxelus.energycontrol.items.cards.ItemCardMain;
import com.zuxelus.energycontrol.items.cards.ItemCardReader;
import com.zuxelus.energycontrol.utils.StringUtils;
import com.zuxelus.zlib.containers.slots.ISlotItemFilter;
import com.zuxelus.zlib.tileentities.ITilePacketHandler;
import com.zuxelus.zlib.tileentities.TileEntityInventory;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityInfoPanel extends TileEntityInventory implements ITilePacketHandler, IScreenPart, ISlotItemFilter {
	public static final String NAME = "info_panel";
	public static final int DISPLAY_DEFAULT = Integer.MAX_VALUE - 1024;
	private static final byte SLOT_CARD = 0;
	private static final byte SLOT_UPGRADE_RANGE = 1;
	private static final byte SLOT_UPGRADE_COLOR = 2;
	private static final byte SLOT_UPGRADE_TOUCH = 3;

	private final Map<Integer, List<PanelString>> cardData;
	protected final Map<Integer, Map<String, Integer>> displaySettings;
	protected Screen screen;
	public NBTTagCompound screenData;
	public boolean init;
	protected int updateTicker;
	protected int dataTicker;
	protected int tickRate;

	public boolean showLabels;
	public int colorBackground;
	public int colorText;

	protected boolean colored;
	public boolean powered;

	public TileEntityInfoPanel() {
		super("tile." + NAME + ".name");
		cardData = new HashMap<>();
		displaySettings = new HashMap<>(1);
		displaySettings.put(0, new HashMap<>());
		tickRate = EnergyControl.config.infoPanelRefreshPeriod;
		updateTicker = tickRate - 1;
		dataTicker = 4;
		showLabels = true;
		colorBackground = 2;
		colored = false;
	}

	private void initData() {
		init = true;
		if (worldObj.isRemote)
			return;

		updateBlockState();
		if (screenData == null) {
			EnergyControl.instance.screenManager.registerInfoPanel(this);
		} else {
			screen = EnergyControl.instance.screenManager.loadScreen(this);
			if (screen != null)
				screen.init(true, worldObj);
		}
		notifyBlockUpdate();
	}

	@Override
	public void setFacing(int meta) {
		ForgeDirection newFacing = ForgeDirection.getOrientation(meta);
		if (facing == newFacing)
			return;
		facing = newFacing;
		if (init) {
			EnergyControl.instance.screenManager.unregisterScreenPart(this);
			EnergyControl.instance.screenManager.registerInfoPanel(this);
		}
	}

	public boolean getShowLabels() {
		return showLabels;
	}

	public void setShowLabels(boolean newShowLabels) {
		if (!worldObj.isRemote && showLabels != newShowLabels)
			notifyBlockUpdate();
		showLabels = newShowLabels;
	}

	public int getTickRate() {
		return tickRate;
	}

	public void setTickRate(int newValue) {
		if (!worldObj.isRemote && tickRate != newValue)
			notifyBlockUpdate();
		tickRate = newValue;
	}

	public boolean getColored() {
		return colored;
	}

	public void setColored(boolean newColored) {
		if (!worldObj.isRemote && colored != newColored)
			notifyBlockUpdate();
		colored = newColored;
	}

	public int getColorBackground() {
		return colorBackground;
	}

	public void setColorBackground(int c) {
		if (!worldObj.isRemote && colorBackground != c)
			notifyBlockUpdate();
		colorBackground = c;
	}

	public int getColorText() {
		return colorText;
	}

	public void setColorText(int c) {
		if (!worldObj.isRemote && colorText != c)
			notifyBlockUpdate();
		colorText = c;
	}

	public boolean getPowered() {
		return powered;
	}

	public void setPowered(boolean value) {
		powered = value;
	}

	public void setScreenData(NBTTagCompound nbtTagCompound) {
		screenData = nbtTagCompound;
		if (screen != null && worldObj.isRemote)
			screen.destroy(true, worldObj);
		if (screenData != null) {
			screen = EnergyControl.instance.screenManager.loadScreen(this);
			if (screen != null)
				screen.init(true, worldObj);
		}
	}

	@Override
	public void onClientMessageReceived(NBTTagCompound tag) { }

	@Override
	public void onServerMessageReceived(NBTTagCompound tag) {
		if (!tag.hasKey("type"))
			return;
		switch (tag.getInteger("type")) {
		case 1:
			if (tag.hasKey("slot") && tag.hasKey("value"))
				setDisplaySettings(tag.getInteger("slot"), tag.getInteger("value"));
			break;
		case 3:
			if (tag.hasKey("value"))
				setShowLabels(tag.getInteger("value") == 1);
			break;
		case 4:
			if (tag.hasKey("slot") && tag.hasKey("title")) {
				ItemStack stack = getStackInSlot(tag.getInteger("slot"));
				if (ItemCardMain.isCard(stack)) {
					new ItemCardReader(stack).setTitle(tag.getString("title"));
					resetCardData();
				}
			}
		case 5:
			if (tag.hasKey("value"))
				setTickRate(tag.getInteger("value"));
			break;
		case 6:
			if (tag.hasKey("value"))
				setColorBackground(tag.getInteger("value"));
			break;
		case 7:
			if (tag.hasKey("value"))
				setColorText(tag.getInteger("value"));
			break;
		}
	}

	@Override
	public Packet getDescriptionPacket() {
		NBTTagCompound tag = new NBTTagCompound();
		tag = writeProperties(tag);
		tag.setBoolean("powered", powered);
		colored = isColoredEval();
		tag.setBoolean("colored", colored);
		return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, tag);
	}

	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
		if (!worldObj.isRemote)
			return;
		readProperties(pkt.func_148857_g());
	}

	protected void deserializeDisplaySettings(NBTTagCompound tag) {
		deserializeSlotSettings(tag, "dSettings", SLOT_CARD);
	}

	protected void deserializeSlotSettings(NBTTagCompound tag, String tagName, int slot) {
		if (!(tag.hasKey(tagName)))
			return;
		NBTTagList settingsList = tag.getTagList(tagName, Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < settingsList.tagCount(); i++) {
			NBTTagCompound compound = settingsList.getCompoundTagAt(i);
			try {
				getDisplaySettingsForSlot(slot).put(compound.getString("key"), compound.getInteger("value"));
			} catch (IllegalArgumentException e) {
				EnergyControl.logger.warn("Invalid display settings for Information Panel");
			}
		}
	}

	@Override
	protected void readProperties(NBTTagCompound tag) {
		super.readProperties(tag);
		if (tag.hasKey("tickRate"))
			tickRate = tag.getInteger("tickRate");
		if (tag.hasKey("showLabels"))
			showLabels = tag.getBoolean("showLabels");
		if (tag.hasKey("colorText"))
			colorText = tag.getInteger("colorText");
		if (tag.hasKey("colorBackground"))
			colorBackground = tag.getInteger("colorBackground");
		if (tag.hasKey("colored"))
			setColored(tag.getBoolean("colored"));

		if (tag.hasKey("screenData")) {
			if (worldObj != null)
				setScreenData((NBTTagCompound) tag.getTag("screenData"));
			else
				screenData = (NBTTagCompound) tag.getTag("screenData");
		} else
			screenData = null;
		deserializeDisplaySettings(tag);
		if (tag.hasKey("powered") && worldObj.isRemote) {
			boolean newPowered = tag.getBoolean("powered");
			if (powered != newPowered) {
				powered = newPowered; 
				worldObj.func_147451_t(xCoord, yCoord, zCoord);
			}
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		readProperties(tag);
	}

	protected void serializeDisplaySettings(NBTTagCompound tag) {
		tag.setTag("dSettings", serializeSlotSettings(SLOT_CARD));
	}

	protected NBTTagList serializeSlotSettings(int slot) {
		NBTTagList settingsList = new NBTTagList();
		for (Map.Entry<String, Integer> item : getDisplaySettingsForSlot(slot).entrySet()) {
			NBTTagCompound tag = new NBTTagCompound();
			tag.setString("key", item.getKey());
			tag.setInteger("value", item.getValue());
			settingsList.appendTag(tag);
		}
		return settingsList;
	}

	@Override
	protected NBTTagCompound writeProperties(NBTTagCompound tag) {
		tag = super.writeProperties(tag);
		tag.setInteger("tickRate",tickRate);
		tag.setBoolean("showLabels", getShowLabels());
		tag.setInteger("colorBackground", colorBackground);
		tag.setInteger("colorText", colorText);
		serializeDisplaySettings(tag);

		if (screen != null) {
			screenData = screen.toTag();
			tag.setTag("screenData", screenData);
		}
		return tag;
	}

	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		writeProperties(tag);
	}

	@Override
	public void invalidate() {
		if (!worldObj.isRemote)
			EnergyControl.instance.screenManager.unregisterScreenPart(this);
		super.invalidate();
	}

	@Override
	public void updateEntity() {
		if (!init)
			initData();
		if (!powered)
			return;
		dataTicker--;
		if (dataTicker <= 0) {
			resetCardData();
			dataTicker = 4;
		}
		if (!worldObj.isRemote) {
			if (updateTicker-- > 0)
				return;
			updateTicker = tickRate - 1;
			if (hasCards())
				markDirty();
		}
	}

	private boolean hasCards() {
		for (ItemStack card : getCards())
			if (card != null)
				return true;
		return false;
	}

	@Override
	public void updateTileEntity() {
		notifyBlockUpdate();
	}

	public void resetCardData() {
		cardData.clear();
	}

	public List<PanelString> getCardData(int settings, ItemStack cardStack, ItemCardReader reader, boolean isServer, boolean showLabels) {
		int slot = getCardSlot(cardStack);
		List<PanelString> data = cardData.get(slot);
		if (data == null) {
			data = ItemCardMain.getStringData(settings, reader, isServer, showLabels);
			cardData.put(slot, data);
		}
		return data;
	}

	@Override
	protected boolean hasRotation() {
		return true;
	}

	// ------- Settings --------
	public List<ItemStack> getCards() {
		List<ItemStack> data = new ArrayList<>();
		data.add(getStackInSlot(SLOT_CARD));
		return data;
	}

	public List<PanelString> getPanelStringList(boolean isServer, boolean showLabels) {
		List<ItemStack> cards = getCards();
		boolean anyCardFound = false;
		List<PanelString> joinedData = new LinkedList<>();
		for (ItemStack card : cards) {
			if (card == null)
				continue;
			int settings = getDisplaySettingsByCard(card);
			if (settings == 0)
				continue;
			ItemCardReader reader = new ItemCardReader(card);
			CardState state = reader.getState();
			List<PanelString> data;
			if (state != CardState.OK && state != CardState.CUSTOM_ERROR)
				data = ItemCardReader.getStateMessage(state);
			else
				data = getCardData(settings, card, reader, isServer, showLabels);
			if (data == null)
				continue;
			joinedData.addAll(data);
			anyCardFound = true;
		}
		if (anyCardFound)
			return joinedData;
		return null;
	}

	public List<String> getPanelStringList(boolean isRaw) {
		List<PanelString> joinedData = getPanelStringList(true, false);
		List<String> list = new ArrayList<>();
		if (joinedData == null || joinedData.size() == 0)
			return list;

		for (PanelString panelString : joinedData) {
			if (panelString.textLeft != null)
				list.add(formatString(panelString.textLeft, isRaw));
			if (panelString.textCenter != null)
				list.add(formatString(panelString.textCenter, isRaw));
			if (panelString.textRight != null)
				list.add(formatString(panelString.textRight, isRaw));
		}
		return list;
	}

	private String formatString(String text, boolean isRaw) {
		return isRaw ? text : text.replaceAll("\\u00a7[1-9,a-f]", "");
	}

	public int getCardSlot(ItemStack card) {
		if (card == null)
			return 0;

		int slot = 0;
		for (int i = 0; i < getSizeInventory(); i++) {
			ItemStack stack = getStackInSlot(i);
			if (stack != null && stack.equals(card)) {
				slot = i;
				break;
			}
		}
		return slot;
	}

	private void processCard(ItemStack card, int slot, ItemStack stack) {
		if (ItemCardMain.isCard(card)) {
			ItemCardReader reader = new ItemCardReader(card);
			ItemCardMain.updateCardNBT(card, worldObj, xCoord, yCoord, zCoord, reader, stack);
			//ItemCardMain.sendCardToWS(getPanelStringList(true, getShowLabels()), reader);
			reader.updateClient(card, this, slot);
		}
	}

	public boolean isColoredEval() {
		ItemStack stack = getStackInSlot(SLOT_UPGRADE_COLOR);
		return stack != null && stack.getItem() instanceof ItemUpgrade && stack.getItemDamage() == ItemUpgrade.DAMAGE_COLOR;
	}

	@Override
	public void markDirty() {
		super.markDirty();
		if (!worldObj.isRemote) {
			setColored(isColoredEval());
			if (powered) {
				ItemStack itemStack = getStackInSlot(getSlotUpgradeRange());
				for (ItemStack card : getCards())
					processCard(card, getCardSlot(card), itemStack);
			}
		}
	}

	public byte getSlotUpgradeRange() {
		return SLOT_UPGRADE_RANGE;
	}

	public boolean isCardSlot(int slot) {
		return slot == SLOT_CARD;
	}

	public Map<String, Integer> getDisplaySettingsForSlot(int slot) {
		if (!displaySettings.containsKey(slot))
			displaySettings.put(slot, new HashMap<>());
		return displaySettings.get(slot);
	}

	public int getDisplaySettingsForCardInSlot(int slot) {
		ItemStack card = getStackInSlot(slot);
		if (card == null)
			return 0;
		return getDisplaySettingsByCard(card);
	}

	public int getDisplaySettingsByCard(ItemStack card) {
		int slot = getCardSlot(card);
		if (card == null)
			return 0;

		if (displaySettings.containsKey(slot)) {
			for (Map.Entry<String, Integer> entry : displaySettings.get(slot).entrySet()) {
				if (StringUtils.getItemId(card).equals(entry.getKey()))
					return entry.getValue();
			}
		}

		return DISPLAY_DEFAULT;
	}

	public void setDisplaySettings(int slot, int settings) {
		if (!isCardSlot(slot))
			return;
		ItemStack stack = getStackInSlot(slot);
		if (!ItemCardMain.isCard(stack))
			return;

		if (!displaySettings.containsKey(slot))
			displaySettings.put(slot, new HashMap<>());
		displaySettings.get(slot).put(StringUtils.getItemId(stack), settings);
		if (!worldObj.isRemote)
			notifyBlockUpdate();
	}

	// ------- Inventory ------- 
	@Override
	public int getSizeInventory() {
		return 4;
	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		return isItemValid(index, stack);
	}

	@Override
	public boolean isItemValid(int index, ItemStack stack) { // ISlotItemFilter
		switch (index) {
		case SLOT_CARD:
			return ItemCardMain.isCard(stack);
		case SLOT_UPGRADE_RANGE:
			return stack.getItem() instanceof ItemUpgrade && stack.getItemDamage() == ItemUpgrade.DAMAGE_RANGE;
		case SLOT_UPGRADE_COLOR:
			return stack.getItem() instanceof ItemUpgrade && stack.getItemDamage() == ItemUpgrade.DAMAGE_COLOR;
		case SLOT_UPGRADE_TOUCH:
			return stack.getItem() instanceof ItemUpgrade && stack.getItemDamage() == ItemUpgrade.DAMAGE_TOUCH;
		default:
			return false;
		}
	}

	@Override
	public void setScreen(Screen screen) {
		this.screen = screen;
	}

	@Override
	public Screen getScreen() {
		return screen;
	}

	@Override
	public void updateData() {
		if (worldObj.isRemote)
			return;

		if (screen == null) {
			screenData = null;
		} else
			screenData = screen.toTag();
		notifyBlockUpdate();
	}

	public void updateBlockState() {
		int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
		boolean flag = worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord);
		powered = flag;
		if (flag == (meta > 5))
			return;

		if (flag)
			worldObj.scheduleBlockUpdate(xCoord, yCoord, zCoord, worldObj.getBlock(xCoord, yCoord, zCoord), 4);
		else {
			worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, meta > 5 ? meta - 6 : meta + 6, 2);
			updateExtenders(true);
		}
	}

	public void updateExtenders(Boolean active) {
		if (screen == null)
			return;

		for (int x = screen.minX; x <= screen.maxX; x++)
			for (int y = screen.minY; y <= screen.maxY; y++)
				for (int z = screen.minZ; z <= screen.maxZ; z++) {
					Block block = worldObj.getBlock(x, y, z);
					if (block instanceof InfoPanelExtender || block instanceof HoloPanelExtender) {
						int meta = worldObj.getBlockMetadata(x, y, z);
						if (active != meta > 5)
							worldObj.setBlockMetadataWithNotify(x, y, z, active ? meta + 6 : meta - 6, 2);
						//worldObj.func_147451_t(x , y, z);
					}
				}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox() {
		if (screen == null)
			return AxisAlignedBB.getBoundingBox(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
		return AxisAlignedBB.getBoundingBox(screen.minX, screen.minY, screen.minZ, screen.maxX + 1, screen.maxY + 1, screen.maxZ + 1);
	}

	@Override
	public boolean shouldRefresh(Block oldBlock, Block newBlock, int oldMeta, int newMeta, World world, int x, int y, int z) {
		return oldBlock != newBlock;
	}

	@SideOnly(Side.CLIENT)
	public int findTexture() {
		Screen scr = getScreen();
		if (scr != null) {
			switch (getFacingForge()) {
			case SOUTH:
				return boolToInt(xCoord == scr.minX) + 2 * boolToInt(xCoord == scr.maxX) + 4 * boolToInt(yCoord == scr.minY) + 8 * boolToInt(yCoord == scr.maxY);
			case WEST:
				return 8 * boolToInt(zCoord == scr.minZ) + 4 * boolToInt(zCoord == scr.maxZ) + boolToInt(yCoord == scr.minY) + 2 * boolToInt(yCoord == scr.maxY);
			case EAST:
				return 8 * boolToInt(zCoord == scr.minZ) + 4 * boolToInt(zCoord == scr.maxZ) + 2 * boolToInt(yCoord == scr.minY) + boolToInt(yCoord == scr.maxY);
			case NORTH:
				return boolToInt(xCoord == scr.minX) + 2 * boolToInt(xCoord == scr.maxX) + 8 * boolToInt(yCoord == scr.minY) + 4 * boolToInt(yCoord == scr.maxY);
			case UP:
				return boolToInt(xCoord == scr.minX) + 2 * boolToInt(xCoord == scr.maxX) + 8 * boolToInt(zCoord == scr.minZ) + 4 * boolToInt(zCoord == scr.maxZ);
			case DOWN:
				return boolToInt(xCoord == scr.minX) + 2 * boolToInt(xCoord == scr.maxX) + 4 * boolToInt(zCoord == scr.minZ) + 8 * boolToInt(zCoord == scr.maxZ);
			}
		}
		return 15;
	}

	private int boolToInt(boolean b) {
		return b ? 1 : 0;
	}

	public boolean runTouchAction(ItemStack stack, int x, int y, int z, float hitX, float hitY, float hitZ) {
		if (worldObj.isRemote)
			return false;
		ItemStack card = getStackInSlot(SLOT_CARD);
		runTouchAction(this, card, stack, SLOT_CARD, true);
		return true;
	}

	public boolean isTouchCard() {
		return isTouchCard(getStackInSlot(SLOT_CARD));
	}

	public boolean isTouchCard(ItemStack stack) {
		return stack != null && stack.getItem() instanceof ITouchAction && ((ITouchAction) stack.getItem()).enableTouch(stack);
	}

	public boolean hasBars() {
		return hasBars(getStackInSlot(SLOT_CARD));
	}

	public boolean hasBars(ItemStack stack) {
		return stack != null && stack.getItem() instanceof IHasBars && ((IHasBars) stack.getItem()).enableBars(stack) && (getDisplaySettingsForCardInSlot(SLOT_CARD) & 1024) > 0;
	}

	public void renderImage(TextureManager manager, double displayWidth, double displayHeight) {
		ItemStack stack = getStackInSlot(SLOT_CARD);
		Item card = stack.getItem();
		if (isTouchCard())
			((ITouchAction) card).renderImage(manager, new ItemCardReader(stack));
		if (hasBars())
			((IHasBars) card).renderBars(manager, displayWidth, displayHeight, new ItemCardReader(stack));
	}

	protected void runTouchAction(TileEntityInfoPanel panel, ItemStack cardStack, ItemStack stack, int slot, boolean needsTouchUpgrade) {
		if (isTouchCard(cardStack) && (!needsTouchUpgrade || getStackInSlot(SLOT_UPGRADE_TOUCH) != null)) {
			ICardReader reader = new ItemCardReader(cardStack);
			if (((ITouchAction) cardStack.getItem()).runTouchAction(panel.getWorldObj(), reader, stack))
				reader.updateClient(cardStack, panel, slot);
		}
	}

	// IWrenchable
	@Override
	public void setFacing(short facing) {
		super.setFacing(facing);
		if ((facing == 0 || facing == 1) && rotation == ForgeDirection.DOWN)
			rotation = ForgeDirection.NORTH;
		if (facing != 0 && facing != 1 && rotation != ForgeDirection.DOWN)
			rotation = ForgeDirection.DOWN;
	}

	@Override
	public ItemStack getWrenchDrop(EntityPlayer player) {
		return new ItemStack(ModItems.blockInfoPanel);
	}
}
