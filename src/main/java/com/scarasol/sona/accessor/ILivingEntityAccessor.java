package com.scarasol.sona.accessor;

public interface ILivingEntityAccessor {
//    InfectionManager getInfectionManager();
//    InjuryManager getInjuryManager();
    float getInfectionLevel();
    float getInjuryLevel();
    float getBandageLevel();
    void setInfectionLevel(float infectionLevel);
    void setInjuryLevel(float injuryLevel);
    void setBandageLevel(float bandageLevel);
}
