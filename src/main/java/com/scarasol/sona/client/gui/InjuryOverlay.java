package com.scarasol.sona.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.scarasol.sona.configuration.CommonConfig;
import com.scarasol.sona.accessor.ILivingEntityAccessor;
import com.scarasol.sona.manager.InjuryManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber({Dist.CLIENT})
public class InjuryOverlay extends GuiComponent {

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void renderInjury(RenderGuiEvent.Pre event) {
        int w = event.getWindow().getGuiScaledWidth();
        int h = event.getWindow().getGuiScaledHeight();
        int posX = getXOffset(w);
        int posY = getYOffset(h);
        Player entity = Minecraft.getInstance().player;
        if (entity instanceof ILivingEntityAccessor player) {
            double blood = InjuryManager.getInjury(player);
            double gold_blood = blood + InjuryManager.getBandage(player);
            if (CommonConfig.INJURY_OVERLAY_PRESET.get() == 1 || CommonConfig.INJURY_OVERLAY_PRESET.get() == 2 || (CommonConfig.INJURY_OVERLAY_PRESET.get() == 0 && CommonConfig.RISE_UNDERWATER.get())) {
                if (entity.getAirSupply() < entity.getMaxAirSupply() || entity.isEyeInFluid(FluidTags.WATER)) {
                    posY -= 9;
                }
            }
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            RenderSystem.setShaderColor(1, 1, 1, 1);
            if (CommonConfig.INJURY_OPEN.get() && !(entity.isCreative() || entity.isSpectator())) {
                for (int i = 0; i < 10; ++i) {
                    int j = (9 - i) * 10;
                    int k = 8;
                    RenderSystem.setShaderTexture(0, new ResourceLocation("sona:textures/screens/blood0.png"));
                    Minecraft.getInstance().gui.blit(event.getPoseStack(), posX + 10 + k * i, posY, 0, 0, 9, 9, 9, 9);
                    if (blood > j + 5) {
                        RenderSystem.setShaderTexture(0, new ResourceLocation("sona:textures/screens/blood1.png"));
                        Minecraft.getInstance().gui.blit(event.getPoseStack(), posX + 10 + k * i, posY, 0, 0, 9, 9, 9, 9);
                    } else if (blood > j) {
                        if (gold_blood > j + 5) {
                            RenderSystem.setShaderTexture(0, new ResourceLocation("sona:textures/screens/blood5.png"));
                            Minecraft.getInstance().gui.blit(event.getPoseStack(), posX + 10 + k * i, posY, 0, 0, 9, 9, 9, 9);
                        } else {
                            RenderSystem.setShaderTexture(0, new ResourceLocation("sona:textures/screens/blood2.png"));
                            Minecraft.getInstance().gui.blit(event.getPoseStack(), posX + 10 + k * i, posY, 0, 0, 9, 9, 9, 9);
                        }
                    } else if (gold_blood > j + 5) {
                        RenderSystem.setShaderTexture(0, new ResourceLocation("sona:textures/screens/blood3.png"));
                        Minecraft.getInstance().gui.blit(event.getPoseStack(), posX + 10 + k * i, posY, 0, 0, 9, 9, 9, 9);
                    } else if (gold_blood > j) {
                        RenderSystem.setShaderTexture(0, new ResourceLocation("sona:textures/screens/blood4.png"));
                        Minecraft.getInstance().gui.blit(event.getPoseStack(), posX + 10 + k * i, posY, 0, 0, 9, 9, 9, 9);
                    }
                }


            }
            RenderSystem.depthMask(true);
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();
            RenderSystem.disableBlend();
            RenderSystem.setShaderColor(1, 1, 1, 1);
        }

    }

    public static int getXOffset(int scaledWidth) {
        return switch (CommonConfig.INJURY_OVERLAY_PRESET.get()) {
            case 1, 2 -> scaledWidth / 2;
            case 3 -> 20;
            default -> CommonConfig.INJURY_X_OFFSET.get();
        };
    }

    public static int getYOffset(int scaledHeight) {
        return switch (CommonConfig.INJURY_OVERLAY_PRESET.get()) {
            case 1 -> scaledHeight - 50;
            case 2 -> scaledHeight - 59;
            case 3 -> scaledHeight - 20;
            default -> scaledHeight - CommonConfig.INJURY_Y_OFFSET.get();
        };
    }
}
