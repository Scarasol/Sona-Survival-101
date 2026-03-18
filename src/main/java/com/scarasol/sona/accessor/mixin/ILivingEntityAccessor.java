package com.scarasol.sona.accessor.mixin;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface ILivingEntityAccessor {
    float getInfectionLevel();

    float getInjuryLevel();

    float getBandageLevel();

    int getCamouflageAmplifier();

    boolean isSona$carapace();

    boolean getInfectionLayer();

    void setInfectionLevel(float infectionLevel);

    void setInjuryLevel(float injuryLevel);

    void setBandageLevel(float bandageLevel);

    void setCamouflageAmplifier(int camouflageAmplifier);

    void setSona$carapace(boolean sona$carapace);

    void setSona$laceration(float sona$laceration);

    float getSona$laceration();

    void setInfectionLayer(boolean infectionLayer);

    @OnlyIn(Dist.CLIENT)
    float getCamouflageAlpha();

    boolean needInit();

    void setNeedInit(boolean needInit);
}
