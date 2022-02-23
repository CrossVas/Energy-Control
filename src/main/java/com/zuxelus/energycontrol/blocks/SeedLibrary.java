package com.zuxelus.energycontrol.blocks;

import com.zuxelus.energycontrol.tileentities.TileEntitySeedLibrary;
import com.zuxelus.zlib.tileentities.TileEntityFacing;

public class SeedLibrary extends FacingHorizontalActiveEC {

	@Override
	public TileEntityFacing createTileEntity(int meta) {
		return new TileEntitySeedLibrary();
	}

	@Override
	protected int getBlockGuiId() {
		return BlockDamages.DAMAGE_SEED_LIBRARY;
	}
}
