package QuantumStorage.tile;

import QuantumStorage.config.ConfigQuantumStorage;
import QuantumStorage.init.ModBlocks;
import QuantumStorage.packet.PacketHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.IItemHandler;
import reborncore.api.IListInfoProvider;
import reborncore.common.util.Inventory;
import reborncore.common.util.Tank;

import java.util.List;

public class TileQuantumTank extends TileQuantumStorage implements IInventory, IListInfoProvider, ITickable, IItemHandler
{
	public int storage = ConfigQuantumStorage.tankMaxStorage;
	public Tank tank = new Tank("TileQuantumTank", storage, this);
	public Inventory inventory = new Inventory(3, "TileQuantumTank", 1, this);

	@Override
	public void readFromNBT(NBTTagCompound tagCompound) 
	{
		readFromNBTWithoutCoords(tagCompound);
        super.readFromNBT(tagCompound);
    }

	public void readFromNBTWithoutCoords(NBTTagCompound tagCompound) 
	{
		tank.readFromNBT(tagCompound);
		inventory.readFromNBT(tagCompound);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound)
	{
		writeToNBTWithoutCoords(compound);
		return super.writeToNBT(compound);
	}

	public void writeToNBTWithoutCoords(NBTTagCompound tagCompound) 
	{
		tank.writeToNBT(tagCompound);
		inventory.writeToNBT(tagCompound);
	}

	public Packet getDescriptionPacket() 
	{
		NBTTagCompound nbtTag = new NBTTagCompound();
		writeToNBT(nbtTag);
		return new SPacketUpdateTileEntity(this.pos, 1, nbtTag);
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) 
	{
		readFromNBT(packet.getNbtCompound());
	}

	@Override
	public void update() 
	{
		super.update();
		if (!worldObj.isRemote) 
		{
            emptyContainer();
            fillContainer();
            tank.compareAndUpdate();
        }
	}

    public void emptyContainer()
    {
        if(FluidUtil.tryEmptyContainerAndStow(getStackInSlot(0), this.tank, this, Fluid.BUCKET_VOLUME, null))
        {
            moveStack(0, 1);

            syncWithAll();
        }
    }

    public void fillContainer()
    {
        if(FluidUtil.tryFillContainerAndStow(getStackInSlot(0), this.tank, this, Fluid.BUCKET_VOLUME, null))
        {
            moveStack(0, 1);

            syncWithAll();
        }
    }

    public boolean moveStack(int from, int to)
    {
        ItemStack stackToMove = getStackInSlot(from).copy();
        if(stackToMove != null && getStackInSlot(to) == null)
        {
            setInventorySlotContents(from, null);
            setInventorySlotContents(to, stackToMove);
            return true;
        }
        return false;
    }

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing)
	{
		if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
		{
			return true;
		}
		return super.hasCapability(capability, facing);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing)
	{
		if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
		{
			return (T) tank;
		}
		return super.getCapability(capability, facing);
	}

	// IInventory
	@Override
	public int getSizeInventory() 
	{
		return inventory.getSizeInventory();
	}

    @Override
    public int getSlots()
    {
        return inventory.getSizeInventory();
    }

    @Override
	public ItemStack getStackInSlot(int slot) 
	{
		return inventory.getStackInSlot(slot);
	}

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
    {
        return null;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate)
    {
        return null;
    }

    @Override
	public ItemStack decrStackSize(int slotId, int count) 
	{
		ItemStack stack = inventory.decrStackSize(slotId, count);
		syncWithAll();
		return stack;
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack stack) 
	{
		inventory.setInventorySlotContents(slot, stack);
		syncWithAll();
	}

	@Override
	public int getInventoryStackLimit() 
	{
		return inventory.getInventoryStackLimit();
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player) 
	{
		return inventory.isUseableByPlayer(player);
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) 
	{
		return inventory.isItemValidForSlot(slot, stack);
	}

	public ItemStack getDropWithNBT() 
	{
		NBTTagCompound tileEntity = new NBTTagCompound();
		ItemStack dropStack = new ItemStack(ModBlocks.QuantumTank, 1);
		writeToNBTWithoutCoords(tileEntity);
		dropStack.setTagCompound(new NBTTagCompound());
		dropStack.getTagCompound().setTag("tileEntity", tileEntity);
		return dropStack;
	}

	@Override
	public void addInfo(List<String> info, boolean isRealTile)
	{
		if (isRealTile) {
			if (tank.getFluid() != null)
			{
				info.add(tank.getFluidAmount() + " of " + tank.getFluidType().getName());
			}
			else 
			{
				info.add("Empty");
			}
		}
		info.add("Capacity " + tank.getCapacity() + " mb");
	}

	@Override
	public void addWailaInfo(List<String> info)
	{
		if (tank.getFluid() != null)
		{
			info.add(tank.getFluidAmount() + " of " + tank.getFluidType().getName());
		}
		else
		{
			info.add("Empty");
		}
		info.add("Capacity " + tank.getCapacity() + " mb");
	}

	public void syncWithAll()
	{
		if (!worldObj.isRemote) 
		{
			PacketHandler.sendPacketToAllPlayers(getDescriptionPacket(), worldObj);
		}
	}

	@Override
	public String getName() 
	{
		return inventory.getName();
	}

	@Override
	public boolean hasCustomName() 
	{
		return inventory.hasCustomName();
	}

	@Override
	public ITextComponent getDisplayName() 
	{
		return inventory.getDisplayName();
	}

	@Override
	public ItemStack removeStackFromSlot(int index) 
	{
		return inventory.removeStackFromSlot(index);
	}

	@Override
	public void openInventory(EntityPlayer player) 
	{
		inventory.openInventory(player);
	}

	@Override
	public void closeInventory(EntityPlayer player) 
	{
		inventory.closeInventory(player);
	}

	@Override
	public int getField(int id) 
	{
		return 0;
	}

	@Override
	public void setField(int id, int value)
	{
		inventory.getField(id);
	}

	@Override
	public int getFieldCount() 
	{
		return inventory.getFieldCount();
	}

	@Override
	public void clear() {}
}
