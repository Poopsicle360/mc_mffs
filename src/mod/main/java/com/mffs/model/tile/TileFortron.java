package com.mffs.model.tile;

import com.mffs.MFFS;
import com.mffs.MFFSConfig;
import com.mffs.api.card.ICard;
import com.mffs.api.fortron.FrequencyGrid;
import com.mffs.api.fortron.IFortronFrequency;
import com.mffs.model.fluids.Fortron;
import com.mffs.model.net.packet.FortronSync;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import mekanism.api.Pos3D;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.*;

import java.util.Set;

/**
 * Created by pwaln on 6/2/2016.
 */
public abstract class TileFortron extends TileFrequency implements IFluidHandler, IFortronFrequency {

    /* Deteremines if we can export fortron */
    public boolean sendFortron = true;

    /* This will hold our fluids */
    protected FluidTank tank = new FluidTank(1_000);

    @Override
    public void updateEntity() {
        super.updateEntity();

        if (this.ticks % MFFSConfig.FORTRON_SYNC_TICKS == 0) {
            //TODO: Send fortron only to people in the interface!
            MFFS.channel.sendToAllAround(new FortronSync(this), new NetworkRegistry.TargetPoint(this.worldObj.provider.dimensionId, xCoord, yCoord, zCoord, 25));
        }
    }

    @Override
    public void invalidate() {
        if (sendFortron) {
            int totalFortron = 0, totalCapacity = 0;
            Set<IFortronFrequency> connections = FrequencyGrid.instance().getFortronTiles(this.worldObj, new Pos3D(this.xCoord, this.yCoord, this.zCoord), 100, getFrequency());
            for (IFortronFrequency machine : connections) {
                if (machine != null) {
                    totalCapacity += machine.getFortronCapacity();
                    totalFortron += machine.getFortronEnergy();
                }
            }
            if (totalCapacity <= 0 || totalFortron <= 0) return;
        }
        super.invalidate();
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        if (tank.getFluid() != null) {
            nbt.setTag("mffs_fortron", this.tank.getFluid().writeToNBT(nbt.getCompoundTag("mffs_fortron")));
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.tank.setFluid(FluidStack.loadFluidStackFromNBT(nbt.getCompoundTag("mffs_fortron")));
    }

    @Override
    public int getFortronEnergy() {
        return tank.getFluidAmount();
    }

    @Override
    public void setFortronEnergy(int paramInt) {
        tank.setFluid(FluidRegistry.getFluidStack("fortron", paramInt));
    }

    @Override
    public int getFortronCapacity() {
        return tank.getCapacity();
    }

    @Override
    public int requestFortron(int paramInt, boolean paramBoolean) {
        tank.drain(paramInt, paramBoolean);
        return tank.getFluidAmount();
    }

    @Override
    public int provideFortron(int paramInt, boolean paramBoolean) {
        return tank.fill(FluidRegistry.getFluidStack("fortron", paramInt), paramBoolean);
    }

    /**
     * Fills fluid into internal tanks, distribution is left entirely to the IFluidHandler.
     *
     * @param from     Orientation the Fluid is pumped in from.
     * @param resource FluidStack representing the Fluid and maximum amount of fluid to be filled.
     * @param doFill   If false, fill will only be simulated.
     * @return Amount of resource that was (or would have been, if simulated) filled.
     */
    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
        if (resource.getFluidID() == Fortron.FLUID_ID) {
            return this.tank.fill(resource, doFill);
        }
        return 0;
    }

    /**
     * Drains fluid out of internal tanks, distribution is left entirely to the IFluidHandler.
     *
     * @param from     Orientation the Fluid is drained to.
     * @param resource FluidStack representing the Fluid and maximum amount of fluid to be drained.
     * @param doDrain  If false, drain will only be simulated.
     * @return FluidStack representing the Fluid and amount that was (or would have been, if
     * simulated) drained.
     */
    @Override
    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
        if ((resource == null) || (!resource.isFluidEqual(this.tank.getFluid()))) {
            return null;
        }
        return this.tank.drain(resource.amount, doDrain);
    }

    /**
     * Drains fluid out of internal tanks, distribution is left entirely to the IFluidHandler.
     * <p>
     * This method is not Fluid-sensitive.
     *
     * @param from     Orientation the fluid is drained to.
     * @param maxDrain Maximum amount of fluid to drain.
     * @param doDrain  If false, drain will only be simulated.
     * @return FluidStack representing the Fluid and amount that was (or would have been, if
     * simulated) drained.
     */
    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
        return this.tank.drain(maxDrain, doDrain);
    }

    /**
     * Returns true if the given fluid can be inserted into the given direction.
     * <p>
     * More formally, this should return true if fluid is able to enter from the given direction.
     *
     * @param from
     * @param fluid
     */
    @Override
    public boolean canFill(ForgeDirection from, Fluid fluid) {
        return true;
    }

    /**
     * Returns true if the given fluid can be extracted from the given direction.
     * <p>
     * More formally, this should return true if fluid is able to leave from the given direction.
     *
     * @param from
     * @param fluid
     */
    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid) {
        return true;
    }

    /**
     * Returns an array of objects which represent the internal tanks. These objects cannot be used
     * to manipulate the internal tanks. See {@link FluidTankInfo}.
     *
     * @param from Orientation determining which tanks should be queried.
     * @return Info for the relevant internal tanks.
     */
    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from) {
        return new FluidTankInfo[0];
    }

    public ItemStack getCard() {
        ItemStack itemStack = getStackInSlot(0);

        if (itemStack != null) {
            if ((itemStack.getItem() instanceof ICard)) {
                return itemStack;
            }
        }

        return null;
    }

    /**
     * Gets the tank associated.
     *
     * @return
     */
    public FluidTank getTank() {
        return tank;
    }

    /**
     * Handles the message given by the handler.
     *
     * @param imessage The message.
     */
    @Override
    public IMessage handleMessage(IMessage imessage) {
        if (imessage instanceof FortronSync) {
            FortronSync sync = (FortronSync) imessage;
            if (tank.getFluid() != null) {
                tank.getFluid().amount = sync.amount;
                //tank.setCapacity(sync.capacity);
            }
        }
        return super.handleMessage(imessage);
    }
}
