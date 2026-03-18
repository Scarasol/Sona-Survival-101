package com.scarasol.sona.mixin;

import com.scarasol.sona.client.gui.ItemMarkHandler;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@OnlyIn(Dist.CLIENT)
@Mixin(Gui.class)
public abstract class GuiMixin {

    @Inject(method = "renderSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;II)V", shift = At.Shift.AFTER))
    private void onRenderSlot(GuiGraphics guiGraphics, int x, int y, float p_168680_, Player player, ItemStack itemStack, int p_168683_, CallbackInfo ci){
        ItemMarkHandler.renderMark(guiGraphics, itemStack, x, y);
    }

//    @Inject(method = "getChat", at = @At(value = "HEAD"))
//    private void onHandleChat(ChatType chatType, Component component, UUID uuid, CallbackInfo ci){
//        Level level = Minecraft.getInstance().player.getLevel();
//        if (CommonConfig.INFECTION_OPEN.get() && CommonConfig.BLUR_MESSAGE.get()){
//            Player player = level.getPlayerByUUID(uuid);
//            if (player instanceof ILivingEntityAccessor survivalEntity && component instanceof MutableComponent baseComponent)
//                InfectionManager.blurMessage(survivalEntity, baseComponent);
//        }
//    }
}
