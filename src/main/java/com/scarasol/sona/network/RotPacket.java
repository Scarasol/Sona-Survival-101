package com.scarasol.sona.network;

import com.scarasol.sona.manager.RotManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RotPacket{

    private final double rotValue;
    private final int slotInt;
    private final boolean isInventory;

    public RotPacket(double rotValue, int slotInt, boolean isInventory) {
        this.rotValue = rotValue;
        this.slotInt = slotInt;
        this.isInventory = isInventory;
    }

    public static RotPacket decode(FriendlyByteBuf buf) {
        return new RotPacket(buf.readDouble(), buf.readInt(), buf.readBoolean());
    }

    public static void encode(RotPacket msg, FriendlyByteBuf buf) {
        buf.writeDouble(msg.rotValue);
        buf.writeInt(msg.slotInt);
        buf.writeBoolean(msg.isInventory);
    }

    public double getRotValue() {
        return this.rotValue;
    }

    public int getSlotInt() {
        return slotInt;
    }

    public boolean isInventory() {
        return isInventory;
    }

    public static void handler(RotPacket msg, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            if (msg != null) {
                context.get().enqueueWork(() -> {
                    if (!context.get().getDirection().getReceptionSide().isServer()){
                        ItemStack itemStack;
                        if (msg.isInventory()){
                            itemStack = Minecraft.getInstance().player.getInventory().getItem(msg.getSlotInt());
                        }else {
                            itemStack = Minecraft.getInstance().player.containerMenu.slots.get(msg.getSlotInt()).getItem();
                        }
                        RotManager.putRot(itemStack, msg.getRotValue());
                        if (!msg.isInventory()){
                            Minecraft.getInstance().player.containerMenu.slots.get(msg.getSlotInt()).setChanged();
                        }
                    }
                });
            }
        });
        context.get().setPacketHandled(true);
    }
}
