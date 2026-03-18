package com.scarasol.sona.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.scarasol.sona.configuration.CommonConfig;
import com.scarasol.sona.manager.RotManager;
import com.scarasol.sona.manager.RustManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber({Dist.CLIENT})
@OnlyIn(Dist.CLIENT)
public class ItemMarkHandler {

    private static final ResourceLocation SAFE = new ResourceLocation("sona:textures/screens/safe.png");
    private static final ResourceLocation MILD = new ResourceLocation("sona:textures/screens/mild.png");
    private static final ResourceLocation BAD = new ResourceLocation("sona:textures/screens/bad.png");
    private static final ResourceLocation AWFUL = new ResourceLocation("sona:textures/screens/awful.png");
    private static final ResourceLocation WAXED = new ResourceLocation("sona:textures/screens/waxed.png");
    private static final ResourceLocation WARPED = new ResourceLocation("sona:textures/screens/warped.png");

    public static void renderMark(GuiGraphics guiGraphics, Slot slot) {
        if (!slot.hasItem())
            return;
        if (CommonConfig.ROT_OPEN.get() && RotManager.canBeRotten(slot.getItem()) && slot.getItem().isEdible()) {
            renderRot(guiGraphics, slot.getItem(), slot.x, slot.y);
        }else if (CommonConfig.RUST_OPEN.get() && RustManager.canBeRust(slot.getItem())) {
            renderRust(guiGraphics, slot.getItem(), slot.x, slot.y);
        }
    }

    public static void renderMark(GuiGraphics guiGraphics, ItemStack itemStack, int x, int y) {
        if (itemStack.isEmpty())
            return;
        if (CommonConfig.ROT_OPEN.get() && RotManager.canBeRotten(itemStack) && itemStack.isEdible()) {
            renderRot(guiGraphics, itemStack, x, y);
        }else if (CommonConfig.RUST_OPEN.get() && RustManager.canBeRust(itemStack)) {
            renderRust(guiGraphics, itemStack, x, y);
        }
    }

    private static void renderRot(GuiGraphics guiGraphics, ItemStack itemStack, int x, int y) {
        RenderSystem.disableDepthTest();
        double value = RotManager.getRot(itemStack);
        RenderSystem.setShaderColor(1, 1, 1, 0.5f);
//        guiGraphics.pose().pushPose();
        if (RotManager.isWarped(itemStack))
            guiGraphics.blit(WARPED, x, y, 0, 0, 16, 16, 16, 16);
        guiGraphics.blit(rotLevel(value), x, y, 0, 0, 8, 8, 8, 8);
//        guiGraphics.pose().popPose();
        RenderSystem.setShaderColor(1, 1, 1, 1);
    }

    private static void renderRust(GuiGraphics guiGraphics, ItemStack itemStack, int x, int y) {
        RenderSystem.disableDepthTest();
        double value = RustManager.getRust(itemStack);
        RenderSystem.setShaderColor(1, 1, 1, 0.5f);
//        guiGraphics.pose().pushPose();
        guiGraphics.blit(rustLevel(value), x, y, 0, 0, 8, 8, 8, 8);
        if (RustManager.isWaxed(itemStack)){
            guiGraphics.blit(WAXED, x, y + 8, 0, 0, 8, 8, 8, 8);
        }
//        guiGraphics.pose().popPose();
        RenderSystem.setShaderColor(1, 1, 1, 1);
    }

    private static ResourceLocation rustLevel(double value){
        if (value >= 70){
            return AWFUL;
        }else if (value >= 40){
            return BAD;
        }else {
            return SAFE;
        }
    }

    private static ResourceLocation rotLevel(double value){
        if (value >= 90){
            return AWFUL;
        }else if (value >= 70){
            return BAD;
        }else if (value >= 40){
            return MILD;
        }else {
            return SAFE;
        }
    }
}
