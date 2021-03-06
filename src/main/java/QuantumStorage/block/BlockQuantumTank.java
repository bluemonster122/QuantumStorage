package QuantumStorage.block;

import QuantumStorage.QuantumStorage;
import QuantumStorage.client.GuiHandler;
import QuantumStorage.init.ModBlocks;
import QuantumStorage.tile.TileQuantumTank;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class BlockQuantumTank extends BlockQuantumStorage
{
	public BlockQuantumTank(Material material) 
	{
		setUnlocalizedName("quantumtank");
	}
	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) 
	{
		if (!player.isSneaking())
		{
            if(!fillBlockWithFluid(world, pos, player, heldItem, side))
                player.openGui(QuantumStorage.INSTANCE, GuiHandler.tank, world, pos.getX(), pos.getY(), pos.getZ());
				return true;
        }
		return false;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int p_149915_2_) 
	{
		return new TileQuantumTank();
	}

    public boolean fillBlockWithFluid(World worldIn, BlockPos pos, EntityPlayer playerIn, ItemStack heldItem, EnumFacing side)
    {
        TileEntity tile = worldIn.getTileEntity(pos);
        if(tile == null || !tile.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side))
        {
            return false;
        }

        IFluidHandler fluidHandler = tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side);
		boolean inserted = FluidUtil.interactWithFluidHandler(heldItem, fluidHandler, playerIn);
		if(!worldIn.isRemote){
			TileQuantumTank tank = (TileQuantumTank) tile;
			tank.syncWithAll();
		}

        return inserted;
    }
	
	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) 
	{
		TileEntity te = world.getTileEntity(pos);
		if(te instanceof TileQuantumTank)
		{
			if (((TileQuantumTank) te).tank.getFluid() != null)
			{
				float xOffset = world.rand.nextFloat() * 0.8F + 0.1F;
				float yOffset = world.rand.nextFloat() * 0.8F + 0.1F;
				float zOffset = world.rand.nextFloat() * 0.8F + 0.1F;
						
				ItemStack stacknbt = ((TileQuantumTank) te).getDropWithNBT();
				int amountToDrop = Math.min(world.rand.nextInt(21) + 10, stacknbt.stackSize);

				EntityItem entityitem = new EntityItem(world,
						pos.getX() + xOffset, pos.getY() + yOffset, pos.getZ() + zOffset,
						stacknbt.splitStack(amountToDrop));
				world.spawnEntityInWorld(entityitem);
			}
			else 
			{
				float xOffset = world.rand.nextFloat() * 0.8F + 0.1F;
				float yOffset = world.rand.nextFloat() * 0.8F + 0.1F;
				float zOffset = world.rand.nextFloat() * 0.8F + 0.1F;
				ItemStack stack = new ItemStack(ModBlocks.QuantumTank);
				
				EntityItem entityitem = new EntityItem(world,
						pos.getX() + xOffset, pos.getY() + yOffset, pos.getZ() + zOffset, stack);
				world.spawnEntityInWorld(entityitem);
			}
		}
	}
}
