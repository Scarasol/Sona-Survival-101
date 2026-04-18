const int FOG_SHAPE_SPHERICAL = 0;
const int FOG_SHAPE_CYLINDRICAL = 1;
const float SONA_EXP_FOG_START_DISTANCE = 20.0;
const float SONA_VANILLA_FOG_START_RATIO = 0.65;

float sona_exp_fog_factor(float fragDistance, float fogStart, float fogEnd) {
    float fogDistance = max(fragDistance - SONA_EXP_FOG_START_DISTANCE, 0.0);
    float fogDensity = max(-512.0 - fogStart, 0.0);
    float expFactor = 1.0 - exp(-fogDensity * fogDistance);
    float vanillaStart = fogEnd * SONA_VANILLA_FOG_START_RATIO;
    float vanillaFactor = fragDistance < fogEnd ? smoothstep(vanillaStart, fogEnd, fragDistance) : 1.0;
    return 1.0 - (1.0 - vanillaFactor) * (1.0 - expFactor);
}

vec4 _linearFog(vec4 fragColor, float fragDistance, vec4 fogColor, float fogStart, float fogEnd) {
#ifdef USE_FOG
    if (fogStart <= -512.0 && fogStart > -1024.0) {
        float factor = sona_exp_fog_factor(fragDistance, fogStart, fogEnd);
        return vec4(mix(fragColor.rgb, fogColor.rgb, factor * fogColor.a), fragColor.a);
    }

    if (fogStart == -1024.0) {
        float fogDistance = max(fragDistance - SONA_EXP_FOG_START_DISTANCE, 0.0);
        float factor = exp(-fogEnd * fogDistance * fogDistance);
        return vec4(mix(fogColor.rgb, fragColor.rgb, factor * fogColor.a), fragColor.a);
    }

    if (fragDistance <= fogStart) {
        return fragColor;
    }
    float factor = fragDistance < fogEnd ? smoothstep(fogStart, fogEnd, fragDistance) : 1.0;
    vec3 blended = mix(fragColor.rgb, fogColor.rgb, factor * fogColor.a);

    return vec4(blended, fragColor.a);
#else
    return fragColor;
#endif
}

float getFragDistance(int fogShape, vec3 position) {
    switch (fogShape) {
        case FOG_SHAPE_SPHERICAL: return length(position);
        case FOG_SHAPE_CYLINDRICAL: return max(length(position.xz), abs(position.y));
        default: return length(position);
    }
}
