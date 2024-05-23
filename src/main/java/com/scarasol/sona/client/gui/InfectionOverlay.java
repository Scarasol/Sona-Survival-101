package com.scarasol.sona.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.scarasol.sona.configuration.CommonConfig;
import com.scarasol.sona.accessor.ILivingEntityAccessor;
import com.scarasol.sona.manager.InfectionManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber({Dist.CLIENT})
public class InfectionOverlay extends GuiComponent{

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void eventHandler(RenderGameOverlayEvent.Pre event) {
        if (event.getType() == RenderGameOverlayEvent.ElementType.ALL) {
            int width = event.getWindow().getGuiScaledWidth();
            int height = event.getWindow().getGuiScaledHeight();
            int posX = getXOffset(width);
            int posY = getYOffset(height);
            Player entity = Minecraft.getInstance().player;
            if (entity != null) {
                RenderSystem.disableDepthTest();
                RenderSystem.depthMask(false);
                RenderSystem.enableBlend();
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                RenderSystem.setShaderColor(1, 1, 1, 1);
                if (CommonConfig.INFECTION_OPEN.get() && entity instanceof ILivingEntityAccessor player && !(entity.isCreative() || entity.isSpectator())) {
                    if (InfectionManager.getInfection(player) <= 40) {
                        RenderSystem.setShaderTexture(0, new ResourceLocation("sona:textures/screens/n1.png"));
                        GuiComponent.blit(event.getMatrixStack(), posX, posY, 0, 0, 32, 32, 32, 32);
                    } else if (InfectionManager.getInfection(player) <= 70) {
                        RenderSystem.setShaderTexture(0, new ResourceLocation("sona:textures/screens/g2.png"));
                        GuiComponent.blit(event.getMatrixStack(), posX, posY, 0, 0, 32, 32, 32, 32);
                    } else if (InfectionManager.getInfection(player) <= 90) {
                        RenderSystem.setShaderTexture(0, new ResourceLocation("sona:textures/screens/g3.png"));
                        GuiComponent.blit(event.getMatrixStack(), posX, posY, 0, 0, 32, 32, 32, 32);
                    } else if (InfectionManager.getInfection(player) < 100) {
                        RenderSystem.setShaderTexture(0, new ResourceLocation("sona:textures/screens/g4.png"));
                        GuiComponent.blit(event.getMatrixStack(), posX, posY, 0, 0, 32, 32, 32, 32);
                    } else {
                        RenderSystem.setShaderTexture(0, new ResourceLocation("sona:textures/screens/g5.png"));
                        GuiComponent.blit(event.getMatrixStack(), posX, posY, 0, 0, 32, 32, 32, 32);
                    }
                }
                RenderSystem.depthMask(true);
                RenderSystem.defaultBlendFunc();
                RenderSystem.enableDepthTest();
                RenderSystem.disableBlend();
                RenderSystem.setShaderColor(1, 1, 1, 1);
            }
        }
    }

    public static int getXOffset(int scaledWidth){
        return switch (CommonConfig.INFECTION_OVERLAY_PRESET.get()) {
            case 1 -> 0;
            case 2 -> scaledWidth - 32;
            case 3 -> scaledWidth / 2 - 16;
            default -> CommonConfig.INFECTION_X_OFFSET.get();
        };
    }

    public static int getYOffset(int scaledHeight){
        return switch (CommonConfig.INFECTION_OVERLAY_PRESET.get()) {
            case 1, 2 -> scaledHeight - 32;
            case 3 -> scaledHeight - 65;
            default -> scaledHeight - CommonConfig.INFECTION_Y_OFFSET.get();
        };
    }
}
