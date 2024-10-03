package com.scarasol.sona.manager;

import com.scarasol.sona.configuration.CommonConfig;
import com.scarasol.sona.entity.SoundDecoy;
import com.scarasol.sona.init.SonaEntities;
import com.scarasol.sona.network.NetworkHandler;
import com.scarasol.sona.network.SoundDecoyPacket;
import com.scarasol.sona.network.SyncSoundPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

public class SoundManager {

    private static final List<String> soundWhiteList = new ArrayList<>();
    private static boolean soundOpen;

    public static boolean isSoundOpen() {
        return soundOpen;
    }

    public static void setSoundOpen(boolean soundOpen) {
        SoundManager.soundOpen = soundOpen;
    }

    public static void addSoundWhiteList(String sound){
        soundWhiteList.add(sound);
    }

    public static void syncSoundWhiteList(ServerPlayer player){
        for (String sound : soundWhiteList){
            NetworkHandler.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> player), new SyncSoundPacket(sound, soundOpen));
        }
    }

    public static boolean isSoundAttractedMob(Mob mob){
        if (CommonConfig.findIndex(ForgeRegistries.ENTITY_TYPES.getKey(mob.getType()).toString(), CommonConfig.SOUND_ATTRACTED_MOB_WHITELIST.get()) != -1)
            return true;
        if (CommonConfig.findIndex(ForgeRegistries.ENTITY_TYPES.getKey(mob.getType()).toString(), CommonConfig.SOUND_ATTRACTED_MOB_BLACKLIST.get()) != -1)
            return false;
        return mob.getMobType() == MobType.UNDEAD;
    }

    public static void insertAi(Mob mob){
        if (isSoundAttractedMob(mob))
            mob.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(mob, Mob.class, 5, false, false, livingEntity -> livingEntity instanceof SoundDecoy));
    }

    public static int getIndex(String soundName){
        int index = CommonConfig.findIndex(soundName, soundWhiteList);
        if (index == -1)
            index = CommonConfig.containSearch(soundName, soundWhiteList);
        return index;
    }

    public static int getAmplifier(int index){
        String[] str = soundWhiteList.get(index).split(",");
        if (str.length == 2)
            return Math.max(Integer.parseInt(str[1].trim()), 0);
        return 0;
    }

    public static void spawnSoundDecoy(Level level, double x, double y, double z, int amplifier){
        if (level instanceof ServerLevel serverLevel){
            SoundDecoy soundDecoy = new SoundDecoy(SonaEntities.SOUND_DECOY.get(), level, amplifier);
            soundDecoy.setPos(x, y, z);
            soundDecoy.finalizeSpawn(serverLevel, level.getCurrentDifficultyAt(new BlockPos(x, y, z)), MobSpawnType.MOB_SUMMONED, null, null);
            serverLevel.addFreshEntity(soundDecoy);
        }else if (level.isClientSide()){
            NetworkHandler.PACKET_HANDLER.sendToServer(new SoundDecoyPacket(x, y, z, amplifier));
        }
    }


}
