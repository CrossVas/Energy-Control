package com.zuxelus.energycontrol.blocks;

import com.zuxelus.energycontrol.tileentities.TileEntityIndustrialAlarm;
import com.zuxelus.zlib.tileentities.TileEntityFacing;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class IndustrialAlarm extends HowlerAlarm {

	@Override
	public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
		TileEntity te = world.getTileEntity(pos);
		if (!(te instanceof TileEntityIndustrialAlarm))
			return 0;
		return ((TileEntityIndustrialAlarm)te).lightLevel;
	}

	@Override
	protected TileEntityFacing createTileEntity() {
		return new TileEntityIndustrialAlarm();
	}

	@Override
	protected int getBlockGuiId() {
		return BlockDamages.DAMAGE_INDUSTRIAL_ALARM;
	}
}
