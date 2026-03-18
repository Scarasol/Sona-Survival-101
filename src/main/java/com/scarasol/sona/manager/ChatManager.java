package com.scarasol.sona.manager;

import com.scarasol.sona.accessor.mixin.ILivingEntityAccessor;
import com.scarasol.sona.configuration.CommonConfig;
import com.scarasol.sona.network.NetworkHandler;
import com.scarasol.sona.network.SyncChatPacket;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.*;

public class ChatManager {

    private static boolean CHAT_LIMIT;
    private static int CHAT_RANGE;
    private static final List<String> RANGE_ITEM = new ArrayList<>();


    public static boolean isChatLimit() {
        return CHAT_LIMIT;
    }

    public static void setChatLimit(boolean chatLimit) {
        CHAT_LIMIT = chatLimit;
    }

    public static int getChatRange() {
        return CHAT_RANGE;
    }

    public static void setChatRange(int chatRange) {
        CHAT_RANGE = chatRange;
    }

    public static List<String> getRangeItem() {
        return RANGE_ITEM;
    }

    public static void addRangeItem(String rangeItem) {
        RANGE_ITEM.add(rangeItem);
    }

    public static void syncChatManager(ServerPlayer serverPlayer) {
        for (String rangeItem : getRangeItem()) {
            NetworkHandler.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new SyncChatPacket(rangeItem, getChatRange(), isChatLimit()));
        }
    }

    public static void broadcastMessage(Player player, PlayerChatMessage playerChatMessage1) {
        if (player.level() instanceof ServerLevel serverLevel && player instanceof ILivingEntityAccessor livingEntityAccessor) {
            int range = getMaxRange(player);
            BlockPos pos = player.blockPosition();
            String message = playerChatMessage1.decoratedContent().getString();
            message += " @TheMessageCutter@ " +  livingEntityAccessor.getInfectionLevel() + " ; " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + " ; " + range;
            ChatType.Bound bound = ChatType.bind(ChatType.CHAT, player);
            serverLevel.getServer().logChatMessage(playerChatMessage1.decoratedContent(), bound, null);
            PlayerChatMessage playerChatMessage = playerChatMessage1.withUnsignedContent(Component.literal(message).setStyle(playerChatMessage1.decoratedContent().getStyle()));
            OutgoingChatMessage outgoingChatMessage = OutgoingChatMessage.create(playerChatMessage);
            if (range == -2 || !isChatLimit()) {
                broadcastMessageInServer(serverLevel, outgoingChatMessage, bound);
            } else if (range == -1) {
                broadcastMessageInLevel(serverLevel, outgoingChatMessage, bound);
            } else {
                broadcastMessageInRange(serverLevel, outgoingChatMessage, bound, (int)(range * 1.5), new Vec3(pos.getX(), pos.getY(), pos.getZ()));
            }
        }
    }

    public static void broadcastMessageInRange(ServerLevel serverLevel, OutgoingChatMessage outgoingChatMessage, ChatType.Bound bound, int range, Vec3 center) {
        serverLevel.getEntitiesOfClass(ServerPlayer.class, new AABB(center, center).inflate(range), e -> true)
                .forEach(p -> p.sendChatMessage(outgoingChatMessage, false, bound));
    }

    public static void broadcastMessageInLevel(ServerLevel serverLevel, OutgoingChatMessage outgoingChatMessage, ChatType.Bound bound) {
        serverLevel.getPlayers(serverPlayer -> true)
                .forEach(p -> p.sendChatMessage(outgoingChatMessage, false, bound));
    }

    public static void broadcastMessageInServer(ServerLevel serverLevel, OutgoingChatMessage outgoingChatMessage, ChatType.Bound bound) {
        serverLevel.getServer().getPlayerList().getPlayers()
                .forEach(p -> p.sendChatMessage(outgoingChatMessage, false, bound));
    }

    public static int getMaxRange(Player player) {
        Inventory inventory = player.getInventory();
        int maxRange = getChatRange();
        for (int i = 0; i <= 40; ++i) {
            ItemStack item = inventory.getItem(i);
            int range = getItemRange(item);
            if (range > 0 && maxRange >= 0) {
                maxRange = Math.max(maxRange, range);
            } else if (range == -1 && maxRange != -2) {
                maxRange = -1;
            } else if (range == -2) {
                maxRange = -2;
            }
        }
        return maxRange;
    }

    public static boolean canReceiveBeyondMessage(Player player) {
        Inventory inventory = player.getInventory();
        for (int i = 0; i <= 40; ++i) {
            ItemStack item = inventory.getItem(i);
            if (getItemRange(item) != -3)
                return true;
        }
        return false;
    }

    public static int getItemRange(ItemStack itemStack) {
        if (!itemStack.hasTag())
            return -3;
        CompoundTag tag = itemStack.getOrCreateTag();
        if (tag.contains("MessageRange"))
            return tag.getInt("MessageRange");
        return -3;
    }

    public static void setItemRange(Object object) {
        if (object instanceof ItemStack itemStack)
            setItemRange(itemStack);
    }

    public static void setItemRange(ItemStack itemStack) {
        String name = ForgeRegistries.ITEMS.getKey(itemStack.getItem()).toString();
        int index = CommonConfig.findIndex(name, getRangeItem());
        if (index == -1) {
            if (itemStack.hasTag() && itemStack.getOrCreateTag().contains("MessageRange")) {
                itemStack.getOrCreateTag().remove("MessageRange");
                if (itemStack.getOrCreateTag().size() == 0)
                    itemStack.setTag(null);
            }
            return;
        }
        String[] info = getRangeItem().get(index).split(",");
        if (info.length < 2)
            return;
        itemStack.getOrCreateTag().putInt("MessageRange", Integer.parseInt(info[1].trim()));
    }

    public static void sendMessage(CommandSourceStack commandSourceStack, Collection<ServerPlayer> collection, PlayerChatMessage playerChatMessage, int range) {
        ChatType.Bound chattype$bound = ChatType.bind(ChatType.MSG_COMMAND_INCOMING, commandSourceStack);
        ServerPlayer serverPlayer = commandSourceStack.getPlayer();
        if (serverPlayer instanceof ILivingEntityAccessor livingEntityAccessor) {
            BlockPos pos = serverPlayer.blockPosition();
            Component component = playerChatMessage.decoratedContent();
            String message = component.getString();
            message += " @TheMessageCutter@ " +  livingEntityAccessor.getInfectionLevel() + " ; " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + " ; " + range;
            playerChatMessage = playerChatMessage.withUnsignedContent(Component.literal(message));
        }
        OutgoingChatMessage outgoingchatmessage = OutgoingChatMessage.create(playerChatMessage);
        boolean flag = false;

        for (ServerPlayer serverplayer : collection) {
            ChatType.Bound chattype$bound1 = ChatType.bind(ChatType.MSG_COMMAND_OUTGOING, commandSourceStack).withTargetName(serverplayer.getDisplayName());
            commandSourceStack.sendChatMessage(outgoingchatmessage, false, chattype$bound1);
            boolean flag1 = commandSourceStack.shouldFilterMessageTo(serverplayer);
            serverplayer.sendChatMessage(outgoingchatmessage, flag1, chattype$bound);
            flag |= flag1 && playerChatMessage.isFullyFiltered();
        }

        if (flag) {
            commandSourceStack.sendSystemMessage(PlayerList.CHAT_FILTERED_FULL);
        }

    }

    @Nullable
    public static String[] parseMessage(PlayerChatMessage playerChatMessage) {
        return parseMessage(playerChatMessage.decoratedContent().getString());
    }

    @Nullable
    public static String[] parseMessage(String info) {
        String[] infoList1 = info.split(" @TheMessageCutter@ ");
        if (infoList1.length != 2)
            return null;
        String[] infoList = infoList1[1].split(" ; ");
        if (infoList.length != 3)
            return null;
        return infoList;
    }

    public static MutableComponent lostMessage(MutableComponent component, double distance, int range) {
        if (range >= distance || range < 0)
            return component;
        StringBuilder content = new StringBuilder(component.getString());
        Random random = new Random();
        double probability = Math.max((distance - range) * 2 / range, 0.1);
        for (int i = 0; i < content.length(); i++) {
            if (random.nextDouble() < probability)
                content.replace(i, i + 1, "…");
        }
        return Component.literal(content.toString()).setStyle(component.getStyle());
    }
}
