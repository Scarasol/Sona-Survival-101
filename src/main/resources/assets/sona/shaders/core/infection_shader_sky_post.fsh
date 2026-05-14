#version 150

uniform sampler2D SceneColor;
uniform sampler2D SceneDepth;
uniform vec4 ColorModulator;
uniform vec2 ScreenSize;
uniform vec3 InfectionColor;
uniform float InfectionWeight;
uniform float InfectionLevel;
uniform float Time;

in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

float hash(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453123);
}

float noise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    vec2 u = f * f * (3.0 - 2.0 * f);

    return mix(
        mix(hash(i), hash(i + vec2(1.0, 0.0)), u.x),
        mix(hash(i + vec2(0.0, 1.0)), hash(i + vec2(1.0, 1.0)), u.x),
        u.y
    );
}

float fbm(vec2 p) {
    float value = 0.0;
    float amplitude = 0.5;
    for (int i = 0; i < 4; i++) {
        value += amplitude * noise(p);
        p = p * 2.03 + vec2(17.1, 9.2);
        amplitude *= 0.5;
    }
    return value;
}

float luminance(vec3 color) {
    return dot(color, vec3(0.299, 0.587, 0.114));
}

float saturationOf(vec3 color) {
    float maxChannel = max(max(color.r, color.g), color.b);
    float minChannel = min(min(color.r, color.g), color.b);
    return (maxChannel - minChannel) / max(maxChannel, 0.0001);
}

void main() {
    vec4 scene = texture(SceneColor, texCoord0) * vertexColor * ColorModulator;
    float depth = texture(SceneDepth, texCoord0).r;

    float skyMask = smoothstep(0.995, 0.99995, depth);
    float farMask = smoothstep(0.93, 0.9975, depth);
    float horizonBand = smoothstep(0.18, 0.42, texCoord0.y) * (1.0 - smoothstep(0.62, 0.88, texCoord0.y));
    float upperSkyMask = smoothstep(0.30, 0.92, texCoord0.y);
    float screenHazeMask = max(horizonBand * 0.34, upperSkyMask * 0.10);

    vec2 noiseUv = texCoord0 * (ScreenSize / vec2(320.0, 220.0)) + vec2(Time * 0.0045, -Time * 0.0030);
    float hazeNoise = mix(0.90, 1.08, fbm(noiseUv));

    float skyVeil = skyMask * (0.04 + 0.26 * horizonBand + 0.08 * upperSkyMask);
    float farVeil = farMask * 0.18;
    float screenVeil = screenHazeMask * 0.28;
    float highInfectionBoost = smoothstep(62.0, 100.0, InfectionLevel);
    float infectionMask = clamp(InfectionWeight * max(skyVeil + farVeil, screenVeil) * mix(0.70, 1.24, highInfectionBoost), 0.0, 1.0) * hazeNoise;
    infectionMask = clamp(infectionMask, 0.0, 1.0);

    float luma = luminance(scene.rgb);
    float brightMask = smoothstep(0.36, 0.86, luma);
    float skyPresence = max(skyMask, screenHazeMask);
    float saturation = saturationOf(scene.rgb);
    float neutralHighlight = (1.0 - smoothstep(0.08, 0.34, saturation)) * smoothstep(0.34, 0.78, luma);
    float daylightMask = smoothstep(0.24, 0.58, luma);
    float exposureDrop = smoothstep(66.0, 100.0, InfectionLevel) * daylightMask * (0.08 + 0.16 * skyPresence + 0.08 * InfectionWeight);
    vec3 compressedScene = scene.rgb * (1.0 - exposureDrop);

    float cloudProtect = neutralHighlight * skyPresence;
    float darkenStrength = infectionMask * brightMask * (0.18 + 0.16 * skyPresence + 0.08 * highInfectionBoost);
    darkenStrength *= mix(1.0, 0.62, cloudProtect);

    vec3 darkened = compressedScene * (1.0 - darkenStrength);
    vec3 gray = vec3(luminance(darkened));
    float desaturation = infectionMask * mix(0.08, 0.22, highInfectionBoost);
    desaturation *= mix(1.0, 0.35, cloudProtect);
    vec3 toned = mix(darkened, gray, desaturation);

    float tintStrength = infectionMask * (0.08 + 0.16 * skyPresence + 0.12 * horizonBand + 0.08 * highInfectionBoost);
    tintStrength *= mix(1.0, 0.30, cloudProtect);
    vec3 tinted = mix(toned, mix(toned, InfectionColor, 0.38), tintStrength);

    fragColor = vec4(tinted, scene.a);
}
