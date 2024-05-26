package com.scarasol.sona.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.scarasol.sona.client.gui.ItemMarkHandler;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMixin extends GuiComponent {

    @Inject(method = "renderSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;renderGuiItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;II)V", shift = At.Shift.AFTER))
    private void onRenderSlot(int x, int y, float p_168680_, Player player, ItemStack itemStack, int p_168683_, CallbackInfo ci){
        ItemMarkHandler.renderMark(new PoseStack(), itemStack, x, y);
    }
}
