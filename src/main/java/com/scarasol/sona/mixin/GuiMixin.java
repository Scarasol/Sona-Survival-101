package com.scarasol.sona.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.scarasol.sona.accessor.ILivingEntityAccessor;
import com.scarasol.sona.client.gui.ItemMarkHandler;
import com.scarasol.sona.configuration.CommonConfig;
import com.scarasol.sona.manager.InfectionManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.BaseComponent;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@OnlyIn(Dist.CLIENT)
@Mixin(Gui.class)
public abstract class GuiMixin extends GuiComponent {

    @Inject(method = "renderSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;renderGuiItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;II)V", shift = At.Shift.AFTER))
    private void onRenderSlot(int x, int y, float p_168680_, Player player, ItemStack itemStack, int p_168683_, CallbackInfo ci){
        ItemMarkHandler.renderMark(new PoseStack(), itemStack, x, y);
    }

    @Inject(method = "handleChat", at = @At(value = "HEAD"))
    private void onHandleChat(ChatType chatType, Component component, UUID uuid, CallbackInfo ci){
        Level level = Minecraft.getInstance().player.getLevel();
        if (CommonConfig.INFECTION_OPEN.get() && CommonConfig.BLUR_MESSAGE.get()){
            Player player = level.getPlayerByUUID(uuid);
            if (player instanceof ILivingEntityAccessor survivalEntity && component instanceof BaseComponent baseComponent)
                InfectionManager.blurMessage(survivalEntity, baseComponent);
        }
    }
}
