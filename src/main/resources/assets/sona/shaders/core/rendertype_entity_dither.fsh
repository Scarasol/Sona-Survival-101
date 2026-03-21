#version 150

#moj_import <fog.glsl>

uniform sampler2D Sampler0;
uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

in float vertexDistance;
in vec4 vertexColor;
in vec4 lightMapColor;
in vec4 overlayColor;
in vec2 texCoord0;

out vec4 fragColor;

const float dither[16] = float[16](
    0.0625, 0.5625, 0.1875, 0.6875,
    0.8125, 0.3125, 0.9375, 0.4375,
    0.2500, 0.7500, 0.1250, 0.6250,
    1.0000, 0.5000, 0.8750, 0.3750
);

void main() {
    vec4 color = texture(Sampler0, texCoord0);
    if (color.a < 0.1) {
        discard;
    }

    int x = int(mod(gl_FragCoord.x, 4.0));
    int y = int(mod(gl_FragCoord.y, 4.0));

    color *= vertexColor * ColorModulator;
    color.rgb = mix(overlayColor.rgb, color.rgb, overlayColor.a);
    color *= lightMapColor;

    if (color.a < dither[y * 4 + x]) {
        discard;
    }

    color.a = 1.0;
    fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
}
