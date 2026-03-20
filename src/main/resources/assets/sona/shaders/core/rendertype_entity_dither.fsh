#version 150

uniform sampler2D Sampler0;
uniform vec4 ColorModulator;

in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

const float dither[16] = float[16](
    0.0625, 0.5625, 0.1875, 0.6875,
    0.8125, 0.3125, 0.9375, 0.4375,
    0.2500, 0.7500, 0.1250, 0.6250,
    1.0000, 0.5000, 0.8750, 0.3750
);

void main() {
    vec4 color = texture(Sampler0, texCoord0) * vertexColor * ColorModulator;

    // 如果原贴图本身就是完全透明的，直接丢弃
    if (color.a < 0.1) {
        discard;
    }

    // 计算屏幕空间的坐标
    int x = int(mod(gl_FragCoord.x, 4.0));
    int y = int(mod(gl_FragCoord.y, 4.0));

    // 抖动测试：如果当前 Alpha 值小于矩阵对应的阈值，则丢弃该像素
    if (color.a < dither[y * 4 + x]) {
        discard;
    }

    // 存活下来的像素以完全不透明的状态渲染，避免深度排序问题
    color.a = 1.0;
    fragColor = color;
}