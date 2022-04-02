package com.zuxelus.energycontrol.crossmod;

import com.zuxelus.energycontrol.EnergyControl;
import com.zuxelus.energycontrol.OreHelper;
import com.zuxelus.energycontrol.api.CardState;
import com.zuxelus.energycontrol.api.ICardReader;
import com.zuxelus.energycontrol.api.ItemStackHelper;
import com.zuxelus.energycontrol.init.ModItems;
import com.zuxelus.energycontrol.items.ItemAFB;
import com.zuxelus.energycontrol.items.ItemAFSUUpgradeKit;
import com.zuxelus.energycontrol.items.cards.ItemCardType;
import com.zuxelus.energycontrol.utils.FluidInfo;

import ic2.api.item.ElectricItem;
import ic2.api.item.IC2Items;
import ic2.api.item.ICustomDamageItem;
import ic2.api.item.IElectricItem;
import ic2.api.reactor.IReactor;
import ic2.api.tile.IEnergyStorage;
import ic2.core.block.BlockTileEntity;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.TileEntityHeatSourceInventory;
import ic2.core.block.comp.Energy;
import ic2.core.block.comp.Fluids;
import ic2.core.block.comp.Fluids.InternalFluidTank;
import ic2.core.block.generator.tileentity.*;
import ic2.core.block.heatgenerator.tileentity.TileEntityElectricHeatGenerator;
import ic2.core.block.kineticgenerator.tileentity.*;
import ic2.core.block.machine.tileentity.TileEntityCondenser;
import ic2.core.block.machine.tileentity.TileEntityLiquidHeatExchanger;
import ic2.core.block.machine.tileentity.TileEntitySteamGenerator;
import ic2.core.block.reactor.tileentity.*;
import ic2.core.block.type.ResourceBlock;
import ic2.core.init.MainConfig;
import ic2.core.item.reactor.ItemReactorLithiumCell;
import ic2.core.item.reactor.ItemReactorUranium;
import ic2.core.item.tool.ItemToolWrench;
import ic2.core.profile.ProfileManager;
import ic2.core.profile.Version;
import ic2.core.ref.BlockName;
import ic2.core.util.Config;
import ic2.core.util.ConfigUtil;
import net.minecraft.block.Block;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class CrossIC2Exp extends CrossModBase {

	@Override
	public int getProfile() {
		return ProfileManager.selected.style == Version.OLD ? 1 : 0; 
	}

	@Override
	public Item getItem(String name) {
		switch (name) {
		case "afb":
			return new ItemAFB();
		case "afsu_upgrade_kit":
			return new ItemAFSUUpgradeKit();
		case "seed":
			return IC2Items.getItem("crop_seed_bag").getItem();
		default:
			return null;
		}
	}

	@Override
	public ItemStack getItemStack(String name) {
		switch (name) {
		case "transformer":
			return IC2Items.getItem("upgrade", "transformer");
		case "energy_storage":
			return IC2Items.getItem("upgrade", "energy_storage");
		case "machine":
			return IC2Items.getItem("resource", "machine");
		case "mfsu":
			return IC2Items.getItem("te","mfsu");
		case "circuit":
			return IC2Items.getItem("crafting","circuit");
		}
		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack getChargedStack(ItemStack stack) {
		ElectricItem.manager.charge(stack, Integer.MAX_VALUE, Integer.MAX_VALUE, true, false);
		return stack;
	}

	@Override
	public boolean isWrench(ItemStack stack) {
		return !stack.isEmpty() && stack.getItem() instanceof ItemToolWrench;
	}

	@Override
	public boolean isElectricItem(ItemStack stack) {
		return !stack.isEmpty() && stack.getItem() instanceof IElectricItem;
	}

	@Override
	public double dischargeItem(ItemStack stack, double needed) {
		IElectricItem ielectricitem = (IElectricItem) stack.getItem();
		if (ielectricitem.canProvideEnergy(stack))
			return ElectricItem.manager.discharge(stack, needed, 1, false, false, false);
		return 0;
	}
	
	/*@Override
	public void postModEvent(TileEntity te, String name) {
		if (name.equals("EnergyTileLoadEvent"))
			MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(te));
		if (name.equals("EnergyTileUnloadEvent"))
			MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
	}*/

	@Override
	public int getNuclearCellTimeLeft(ItemStack stack) {
		if (stack.isEmpty())
			return 0;
		Item item = stack.getItem();
		if (item instanceof ItemReactorUranium || item instanceof ItemReactorLithiumCell)
			return ((ICustomDamageItem)item).getMaxCustomDamage(stack) - ((ICustomDamageItem)item).getCustomDamage(stack);
		// Coaxium Mod
		if (item.getClass().getName().equals("com.sm.FirstMod.items.ItemCoaxiumRod") || item.getClass().getName().equals("com.sm.FirstMod.items.ItemCesiumRod"))
			return stack.getMaxDamage() - getCoaxiumDamage(stack);
		return 0;
	}

	private int getCoaxiumDamage(ItemStack stack) {
		if (!stack.hasTagCompound())
			return 0;
		return stack.getTagCompound().getInteger("fuelRodDamage");
	}

	@Override
	public NBTTagCompound getEnergyData(TileEntity te) {
		if (te instanceof IEnergyStorage) {
			NBTTagCompound tag = new NBTTagCompound();
			IEnergyStorage storage = (IEnergyStorage) te;
			tag.setString("euType", "EU");
			tag.setDouble("storage", storage.getStored());
			tag.setDouble("maxStorage", storage.getCapacity());
			return tag;
		}
		return null;
	}

	@Override
	public ItemStack getGeneratorCard(TileEntity te) {
		if (te instanceof TileEntityBaseGenerator || te instanceof TileEntityConversionGenerator
				|| te instanceof TileEntitySteamGenerator|| te instanceof TileEntityCondenser) {
			ItemStack card = new ItemStack(ModItems.itemCard, 1, ItemCardType.CARD_GENERATOR);
			ItemStackHelper.setCoordinates(card, te.getPos());
			return card;
		}
		if (te instanceof TileEntityElectricKineticGenerator || te instanceof TileEntityManualKineticGenerator
				|| te instanceof TileEntitySteamKineticGenerator || te instanceof TileEntityStirlingKineticGenerator
				|| te instanceof TileEntityWaterKineticGenerator || te instanceof TileEntityWindKineticGenerator) {
			ItemStack card = new ItemStack(ModItems.itemCard, 1, ItemCardType.CARD_GENERATOR_KINETIC);
			ItemStackHelper.setCoordinates(card, te.getPos());
			return card;
		}
		if (te instanceof TileEntityHeatSourceInventory) {
			ItemStack card = new ItemStack(ModItems.itemCard, 1, ItemCardType.CARD_GENERATOR_HEAT);
			ItemStackHelper.setCoordinates(card, te.getPos());
			return card;
		}
		return ItemStack.EMPTY;
	}

	@Override
	public NBTTagCompound getGeneratorData(TileEntity te) {
		try {
			NBTTagCompound tag = new NBTTagCompound();
			boolean active = isActive(te);
			tag.setBoolean("active", active);
			tag.setString("euType", "EU");
			if (te instanceof TileEntityBaseGenerator) {
				tag.setInteger("type", 1);
				Energy energy = ((TileEntityBaseGenerator) te).getComponent(Energy.class);
				tag.setDouble("storage", energy.getEnergy());
				tag.setDouble("maxStorage", energy.getCapacity());
				if (te instanceof TileEntitySolarGenerator) {
					float light = ((TileEntitySolarGenerator)te).skyLight;
					active = light > 0 && energy.getEnergy() < energy.getCapacity();
					tag.setBoolean("active", active);
					if (active)
						tag.setDouble("production", light);
					else
						tag.setDouble("production", 0);
					return tag;
				}
				if (te instanceof TileEntityRTGenerator) {
					tag.setInteger("type", 4);
					int counter = 0;
					for (int i = 0; i < ((TileEntityRTGenerator) te).fuelSlot.size(); i++)
						if (!((TileEntityRTGenerator) te).fuelSlot.isEmpty(i))
							counter++;
					tag.setInteger("items", counter);
					if (counter == 0 || energy.getEnergy() >= energy.getCapacity()) {
						tag.setBoolean("active", false);
						tag.setDouble("production", 0);
						return tag;
					}
					tag.setBoolean("active", true);
					Field field = TileEntityRTGenerator.class.getDeclaredField("efficiency");
					field.setAccessible(true);
					tag.setDouble("multiplier", (float) field.get(te));
					tag.setDouble("production", Math.pow(2.0D, (counter - 1)) * (float) field.get(te));
					return tag;
				}
				if (te instanceof TileEntityWaterGenerator) {
					active = ((TileEntityWaterGenerator)te).water > 0 || ((TileEntityWaterGenerator)te).fuel > 0;
					tag.setBoolean("active", active);
					if (((TileEntityWaterGenerator) te).fuel <= 0) {
						Field field = TileEntityWaterGenerator.class.getDeclaredField("energyMultiplier");
						field.setAccessible(true);
						tag.setDouble("production", (Double) field.get(te) * ((TileEntityWaterGenerator) te).water / 100);
						return tag;
					}
				}
				if (active) {
					Field field = TileEntityBaseGenerator.class.getDeclaredField("production");
					field.setAccessible(true);
					tag.setDouble("production", (Double) field.get(te));
				} else
					tag.setDouble("production", 0);
				return tag;
			}
			if (te instanceof TileEntityConversionGenerator) {
				if (active) {
					Field field = TileEntityConversionGenerator.class.getDeclaredField("lastProduction");
					field.setAccessible(true);
					tag.setDouble("production", (Double) field.get(te));
				} else
					tag.setDouble("production", 0);
				if (te instanceof TileEntityStirlingGenerator) {
					tag.setInteger("type", 2);
					Field field = TileEntityStirlingGenerator.class.getDeclaredField("productionpeerheat");
					field.setAccessible(true);
					tag.setDouble("multiplier", (Double) field.get(te));
				}
				if (te instanceof TileEntityKineticGenerator) {
					tag.setInteger("type", 2);
					Field field = TileEntityKineticGenerator.class.getDeclaredField("euPerKu");
					field.setAccessible(true);
					tag.setDouble("multiplier", (Double) field.get(te));
				}
				return tag;
			}
			if (te instanceof TileEntitySteamGenerator) {
				tag.setInteger("type", 7);
				tag.setBoolean("active", ((TileEntitySteamGenerator) te).getHeatInput() > 0);
				tag.setDouble("heat", ((TileEntitySteamGenerator) te).getSystemHeat());
				tag.setInteger("production", ((TileEntitySteamGenerator) te).getOutputMB());
				tag.setInteger("consumption", ((TileEntitySteamGenerator) te).getInputMB());
				tag.setInteger("heatChange", ((TileEntitySteamGenerator) te).getHeatInput());
				tag.setInteger("water", ((TileEntitySteamGenerator) te).waterTank.getFluidAmount());
				tag.setDouble("calcification", ((TileEntitySteamGenerator) te).getCalcification());
				tag.setInteger("pressure", ((TileEntitySteamGenerator) te).getPressure());
				return tag;
			}
			if (te instanceof TileEntityCondenser) {
				tag.setInteger("type", 8);
				tag.setBoolean("active", true);
				Energy energy = ((TileEntityCondenser) te).getComponent(Energy.class);
				tag.setDouble("storage", energy.getEnergy());
				tag.setDouble("maxStorage", energy.getCapacity());
				tag.setDouble("progress", ((TileEntityCondenser) te).progress * 100.0D / ((TileEntityCondenser) te).maxProgress);
				tag.setInteger("steam", ((TileEntityCondenser) te).getInputTank().getFluidAmount());
				tag.setInteger("water", ((TileEntityCondenser) te).getOutputTank().getFluidAmount());
				return tag;
			}
		} catch (Throwable t) { }
		return null;
	}

	@Override
	public NBTTagCompound getGeneratorKineticData(TileEntity te) {
		try {
			NBTTagCompound tag = new NBTTagCompound();
			if (te instanceof TileEntityManualKineticGenerator) {
				tag.setInteger("type", 1);
				tag.setDouble("storage", ((TileEntityManualKineticGenerator)te).currentKU);
				tag.setDouble("maxStorage", ((TileEntityManualKineticGenerator)te).maxKU);
				return tag;
			}
			Boolean active = ((TileEntityBlock) te).getActive();
			if (te instanceof TileEntityWindKineticGenerator) {
				TileEntityWindKineticGenerator entity = ((TileEntityWindKineticGenerator) te);
				tag.setInteger("type", 5);
				tag.setDouble("output", entity.getKuOutput());
				Field field = TileEntityWindKineticGenerator.class.getDeclaredField("windStrength");
				field.setAccessible(true);
				tag.setDouble("wind", (Double) field.get(te));
				tag.setDouble("multiplier", entity.getEfficiency() * TileEntityWindKineticGenerator.outputModifier);
				tag.setInteger("height", entity.getPos().getY());
				 if (entity.rotorSlot.isEmpty())
					 tag.setInteger("health", -1);
				 else
					 tag.setDouble("health", 100.0F - entity.rotorSlot.get().getItemDamage() * 100.0F / entity.rotorSlot.get().getMaxDamage());
				return tag;
			}
			if (te instanceof TileEntityWaterKineticGenerator) {
				TileEntityWaterKineticGenerator entity = ((TileEntityWaterKineticGenerator) te);
				tag.setInteger("type", 6);
				tag.setDouble("output", entity.getKuOutput());
				Field field = TileEntityWaterKineticGenerator.class.getDeclaredField("waterFlow");
				field.setAccessible(true);
				tag.setDouble("wind", (Integer) field.get(te));
				field = TileEntityWaterKineticGenerator.class.getDeclaredField("outputModifier");
				field.setAccessible(true);
				tag.setDouble("multiplier", (double) (Float) field.get(te));
				tag.setInteger("height", entity.getPos().getY());
				 if (entity.rotorSlot.isEmpty())
					 tag.setInteger("health", -1);
				 else
					 tag.setDouble("health", 100.0F - entity.rotorSlot.get().getItemDamage() * 100.0F / entity.rotorSlot.get().getMaxDamage());
				return tag;
			}
			if (te instanceof TileEntityStirlingKineticGenerator) {
				//TODO
			}
			if (te instanceof TileEntitySteamKineticGenerator) {
				TileEntitySteamKineticGenerator entity = ((TileEntitySteamKineticGenerator) te);
				tag.setInteger("type", 7);
				if (!entity.hasTurbine())
					tag.setString("status", "ic2.SteamKineticGenerator.gui.error.noturbine");
				else if (entity.isTurbineBlockedByWater())
					tag.setString("status", "ic2.SteamKineticGenerator.gui.error.filledupwithwater");
				else if (entity.getActive())
					tag.setString("status", "ic2.SteamKineticGenerator.gui.aktive");
				else
					tag.setString("status", "ic2.SteamKineticGenerator.gui.waiting");
				tag.setDouble("output", entity.getKUoutput());
				Field field = TileEntitySteamKineticGenerator.class.getDeclaredField("outputModifier");
				field.setAccessible(true);
				tag.setDouble("multiplier", (double) (Float) field.get(te) * (1.0F - ((float) entity.getDistilledWaterTank().getFluidAmount()) / entity.getDistilledWaterTank().getCapacity()));
				field = TileEntitySteamKineticGenerator.class.getDeclaredField("condensationProgress");
				field.setAccessible(true);
				tag.setInteger("condProgress", (int) field.get(te)); 
				return tag;
			}
		} catch (Throwable ignored) {
		}
		return null;
	}

	@Override
	public NBTTagCompound getGeneratorHeatData(TileEntity te) {
		try {
			NBTTagCompound tag = new NBTTagCompound();
			boolean active = ((TileEntityBlock)te).getActive();
			tag.setBoolean("active", active);
			if (te instanceof TileEntityHeatSourceInventory) {
				tag.setInteger("type", 1);
				if (active)
					tag.setInteger("output", ((TileEntityHeatSourceInventory)te).gettransmitHeat());
				else
					tag.setInteger("output", 0);
				if (te instanceof TileEntityElectricHeatGenerator) {
					Energy energy = ((TileEntityHeatSourceInventory) te).getComponent(Energy.class);
					tag.setDouble("storage", energy.getEnergy());
					tag.setDouble("maxStorage", energy.getCapacity());
					int count = 0;
					for (ItemStack stack : ((TileEntityElectricHeatGenerator) te).coilSlot)
						if (!stack.isEmpty())
							count++;
					tag.setInteger("coils", count);
				}
				if (te instanceof TileEntityLiquidHeatExchanger) {
					Fluids fluid = ((TileEntityLiquidHeatExchanger) te).getComponent(Fluids.class);
					Iterable<InternalFluidTank> tanks = fluid.getAllTanks();
					InternalFluidTank tank = tanks.iterator().next();
					tag.setDouble("storage", tank.getFluidAmount());
					tag.setDouble("maxStorage", tank.getCapacity());
					int count = 0;
					for (ItemStack stack : ((TileEntityLiquidHeatExchanger) te).heatexchangerslots)
						if (!stack.isEmpty())
							count++;
					tag.setInteger("coils", count);
				}
				return tag;
			}
		} catch (Throwable t) { }
		return null;
	}

	private boolean isActive(TileEntity te) {
		if (te instanceof TileEntityGeoGenerator || te instanceof TileEntityConversionGenerator || te instanceof TileEntitySolarGenerator)
			return ((TileEntityBlock)te).getActive();
		if (te instanceof TileEntityBaseGenerator)
			return ((TileEntityBaseGenerator)te).isConverting();
		return false;
	}

	@Override
	public ItemStack getReactorCard(World world, BlockPos pos) {
		Block block = world.getBlockState(pos).getBlock();
		if (!(block instanceof BlockTileEntity))
			return ItemStack.EMPTY;

		TileEntity te = world.getTileEntity(pos);
		if (te instanceof TileEntityNuclearReactorElectric || te instanceof TileEntityReactorChamberElectric) {
			BlockPos position = IC2ReactorHelper.getTargetCoordinates(world, pos);
			if (position != null) {
				ItemStack card = new ItemStack(ModItems.itemCard, 1, ItemCardType.CARD_REACTOR);
				ItemStackHelper.setCoordinates(card, position);
				return card;
			}
		} else if (te instanceof TileEntityReactorFluidPort || te instanceof TileEntityReactorRedstonePort
				|| te instanceof TileEntityReactorAccessHatch) {
			BlockPos position = IC2ReactorHelper.get5x5TargetCoordinates(world, pos);
			if (position != null) {
				ItemStack card = new ItemStack(ModItems.itemCard, 1, ItemCardType.CARD_REACTOR5X5);
				ItemStackHelper.setCoordinates(card, position);
				return card;
			}
		}
		return ItemStack.EMPTY;
	}

	public NBTTagCompound getReactorData(TileEntity te) {
		if (!(te instanceof TileEntityNuclearReactorElectric))
			return null;
		IReactor reactor = (IReactor) te;
		NBTTagCompound tag = new NBTTagCompound();
		tag.setInteger("heat", reactor.getHeat());
		tag.setInteger("maxHeat", reactor.getMaxHeat());
		tag.setBoolean("reactorPoweredB", reactor.produceEnergy());
		tag.setInteger("output", (int) Math.round(reactor.getReactorEUEnergyOutput()));
		tag.setBoolean("isSteam", false);

		IInventory inventory = (IInventory) reactor;
		int slotCount = inventory.getSizeInventory();
		int dmgLeft = 0;
		for (int i = 0; i < slotCount; i++) {
			ItemStack stack = inventory.getStackInSlot(i);
			if (!stack.isEmpty())
				dmgLeft = Math.max(dmgLeft, IC2ReactorHelper.getNuclearCellTimeLeft(stack));
		}
		tag.setInteger("timeLeft", dmgLeft * reactor.getTickRate() / 20);
		return tag;
	}

	public NBTTagCompound getReactor5x5Data(TileEntity te) {
		if (!(te instanceof TileEntityNuclearReactorElectric))
			return null;
		IReactor reactor = (IReactor) te;
		NBTTagCompound tag = new NBTTagCompound();
		tag.setInteger("heat", reactor.getHeat());
		tag.setInteger("maxHeat", reactor.getMaxHeat());
		tag.setBoolean("reactorPoweredB", reactor.produceEnergy());
		tag.setInteger("output", ((TileEntityNuclearReactorElectric)reactor).EmitHeat);

		IInventory inventory = (IInventory) reactor;
		int slotCount = inventory.getSizeInventory();
		int dmgLeft = 0;
		for (int i = 0; i < slotCount; i++) {
			ItemStack stack = inventory.getStackInSlot(i);
			if (!stack.isEmpty())
				dmgLeft = Math.max(dmgLeft, IC2ReactorHelper.getNuclearCellTimeLeft(stack));
		}

		int timeLeft = dmgLeft * reactor.getTickRate() / 20;
		tag.setInteger("timeLeft", timeLeft);
		return tag;
	}

	@Override
	public int getReactorHeat(World world, BlockPos pos) {
		IReactor reactor = IC2ReactorHelper.getReactorAround(world, pos);
		if (reactor != null)
			return reactor.getHeat();
		reactor = IC2ReactorHelper.getReactor3x3(world, pos);
		if (reactor != null)
			return reactor.getHeat();
		return -1;
	}

	@Override
	public List<FluidInfo> getAllTanks(TileEntity te) {
		if (!(te instanceof TileEntityBlock))
			return null;

		if (!((TileEntityBlock)te).hasComponent(Fluids.class))
			return null;

		Fluids fluid = ((TileEntityBlock)te).getComponent(Fluids.class);

		List<FluidInfo> result = new ArrayList<>();
		for (FluidTank tank: fluid.getAllTanks())
			result.add(new FluidInfo(tank));

		return result;
	}

	@Override
	public void loadOreInfo() {
		Config config = MainConfig.get().getSub("worldgen");
		loadOre(BlockName.resource.getBlockState(ResourceBlock.copper_ore).getBlock(), 1,config.getSub("copper"));
		loadOre(BlockName.resource.getBlockState(ResourceBlock.lead_ore).getBlock(), 2,config.getSub("lead"));
		loadOre(BlockName.resource.getBlockState(ResourceBlock.tin_ore).getBlock(), 3,config.getSub("tin"));
		loadOre(BlockName.resource.getBlockState(ResourceBlock.uranium_ore).getBlock(), 4,config.getSub("uranium"));
	}

	private void loadOre(Block block, int meta, Config config) {
		EnergyControl.oreHelper.put(OreHelper.getId(block, meta),
			new OreHelper(ConfigUtil.getInt(config, "minHeight"), ConfigUtil.getInt(config, "maxHeight"), ConfigUtil.getInt(config, "size"), ConfigUtil.getInt(config, "count")));
	}
	
	/*	@Override
	public ItemStack getLiquidAdvancedCard(World world, BlockPos pos) {
		Block block = world.getBlockState(pos).getBlock();
		if (!(block instanceof BlockTileEntity))
			return ItemStack.EMPTY;
		
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof TileEntityReactorFluidPort || te instanceof TileEntityReactorRedstonePort
				|| te instanceof TileEntityReactorAccessHatch) {
			BlockPos position = ReactorHelper.get5x5TargetCoordinates(world, pos);
			if (position != null) {
				ItemStack sensorLocationCard = new ItemStack(ModItems.itemCard, 1, ItemCardType.CARD_LIQUID_ADVANCED);
				ItemStackHelper.setCoordinates(sensorLocationCard, position);
				return sensorLocationCard;
			}
		}
		return ItemStack.EMPTY;
	}

	@Override
	public void showBarrelInfo(EntityPlayer player, TileEntity te) {
		if (te instanceof TileEntityBarrel) {
			int age = -1;
			int boozeAmount = 0;
			try {
				Field field = TileEntityBarrel.class.getDeclaredField("age");
				field.setAccessible(true);
				age = (int) field.get(te);
				field = TileEntityBarrel.class.getDeclaredField("boozeAmount");
				field.setAccessible(true);
				boozeAmount = (int) field.get(te);
			} catch (Throwable t) { }
			if (age >= 0)
				player.sendMessage(new TextComponentString(age + " / " + ((TileEntityBarrel) te).timeNedForRum(boozeAmount)));
		}
	}*/
}
