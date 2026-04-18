#version 150

const float SONA_EXP_FOG_START_DISTANCE = 20.0;
const float SONA_VANILLA_FOG_START_RATIO = 0.65;

float sona_exp_fog_factor(float vertexDistance, float fogStart, float fogEnd) {
    float fogDistance = max(vertexDistance - SONA_EXP_FOG_START_DISTANCE, 0.0);
    float fogDensity = max(-512.0 - fogStart, 0.0);
    float expFactor = 1.0 - exp(-fogDensity * fogDistance);
    float vanillaStart = fogEnd * SONA_VANILLA_FOG_START_RATIO;
    float vanillaFactor = vertexDistance < fogEnd ? smoothstep(vanillaStart, fogEnd, vertexDistance) : 1.0;
    return 1.0 - (1.0 - vanillaFactor) * (1.0 - expFactor);
}

vec4 linear_fog(vec4 inColor, float vertexDistance, float fogStart, float fogEnd, vec4 fogColor) {
    if (fogStart <= -512.0 && fogStart > -1024.0) {
        float fogFactor = sona_exp_fog_factor(vertexDistance, fogStart, fogEnd);
        return vec4(mix(inColor.rgb, fogColor.rgb, fogFactor * fogColor.a), inColor.a);
    }

    if (fogStart == -1024.0) {
        float fogDistance = max(vertexDistance - SONA_EXP_FOG_START_DISTANCE, 0.0);
        float fogValue = exp(-fogEnd * fogDistance * fogDistance);
        return vec4(mix(fogColor.rgb, inColor.rgb, fogValue * fogColor.a), inColor.a);
    }

    if (vertexDistance <= fogStart) {
        return inColor;
    }

    float fogValue = vertexDistance < fogEnd ? smoothstep(fogStart, fogEnd, vertexDistance) : 1.0;
    return vec4(mix(inColor.rgb, fogColor.rgb, fogValue * fogColor.a), inColor.a);
}

float linear_fog_fade(float vertexDistance, float fogStart, float fogEnd) {
    if (fogStart <= -512.0 && fogStart > -1024.0) {
        return 1.0 - sona_exp_fog_factor(vertexDistance, fogStart, fogEnd);
    }

    if (fogStart == -1024.0) {
        float fogDistance = max(vertexDistance - SONA_EXP_FOG_START_DISTANCE, 0.0);
        return exp(-fogEnd * fogDistance * fogDistance);
    }

    if (vertexDistance <= fogStart) {
        return 1.0;
    } else if (vertexDistance >= fogEnd) {
        return 0.0;
    }

    return smoothstep(fogEnd, fogStart, vertexDistance);
}

float fog_distance(mat4 modelViewMat, vec3 pos, int shape) {
    if (shape == 0) {
        return length((modelViewMat * vec4(pos, 1.0)).xyz);
    } else {
        float distXZ = length((modelViewMat * vec4(pos.x, 0.0, pos.z, 1.0)).xyz);
        float distY = length((modelViewMat * vec4(0.0, pos.y, 0.0, 1.0)).xyz);
        return max(distXZ, distY);
    }
}
