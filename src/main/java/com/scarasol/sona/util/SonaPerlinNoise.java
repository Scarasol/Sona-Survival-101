package com.scarasol.sona.util;

/**
 * @author Scarasol
 */
public record SonaPerlinNoise(long seed) {


    /**
     * 主函数：计算二维 Perlin 噪声值
     *
     * @param x 浮点横坐标
     * @param y 浮点纵坐标
     * @return 噪声值，范围约为 [0, 1]
     */
    public double perlin(double x, double y) {
        // 找到当前坐标所在的网格左下角整数坐标
        int xi = (int) Math.floor(x);
        int yi = (int) Math.floor(y);

        // 获取坐标在单元格内的小数偏移 [0, 1)
        double xf = x - xi;
        double yf = y - yi;

        // 计算 fade 插值权重
        double u = fade(xf);
        double v = fade(yf);

        // 获取四个角落的哈希值（用作梯度方向）
        int aa = hash2D(xi, yi);
        int ab = hash2D(xi, yi + 1);
        int ba = hash2D(xi + 1, yi);
        int bb = hash2D(xi + 1, yi + 1);

        // 计算四个角的影响（梯度向量与位置向量点积）
        double gradAA = grad(aa, xf, yf);
        double gradBA = grad(ba, xf - 1, yf);
        double gradAB = grad(ab, xf, yf - 1);
        double gradBB = grad(bb, xf - 1, yf - 1);

        // x 方向插值
        double lerpX1 = lerp(gradAA, gradBA, u);
        double lerpX2 = lerp(gradAB, gradBB, u);

        // y 方向插值并归一化
        double result = lerp(lerpX1, lerpX2, v);
        return (result + 1) / 2.0; // 映射到 [0,1]
    }

    /**
     * 平滑插值函数：6t⁵ - 15t⁴ + 10t³
     */
    private double fade(double t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    /**
     * 线性插值函数
     */
    private double lerp(double a, double b, double t) {
        return a + t * (b - a);
    }

    /**
     * 简化版梯度函数：根据 hash 值选择方向，然后与输入向量点积
     * 实际只用 hash 的16种方向
     */
    private double grad(int hash, double x, double y) {
        double angle = (hash & 15) * (Math.PI * 2 / 16); // 16个方向
        return x * Math.cos(angle) + y * Math.sin(angle);
    }

    /**
     * 自定义二维哈希函数：用于从坐标中生成伪随机数
     * 保证每个 (x, y) 坐标有唯一且一致的 hash 值
     */
    private int hash2D(int x, int y) {
        long h = seed;
        h ^= (x * 0x632BE59BD9B4E019L) ^ (y * 0x9E3779B97F4A7C15L);
        h ^= (h >> 32);
        h *= 0x27d4eb2dL;
        return (int) (h ^ (h >> 16));
    }

    public double emphasizeExtremes(double v, double sharpness) {
        // v ∈ [0, 1]，sharpness 越大，低/高点越集中
        double mid = 0.5;
        if (v < mid) {
            return mid * Math.pow(v / mid, sharpness);
        } else {
            return 1.0 - mid * Math.pow((1.0 - v) / mid, sharpness);
        }
    }

    /**
     * 根据离中心距离：
     * - 越近 → 趋向 0（指数压低）
     * - 中间 → 保留原值
     * - 越远 → 极慢地趋向 1（不会完全等于 1）
     */
    public double applyCenterSuppression(double val, double x, double y, double strength) {
        double dist = Math.sqrt(x * x + y * y);

        // 中心压制（指数趋近于 0）
        double centerBias = 1.0 - Math.exp(-dist * strength);  // 越近越接近 0
        double suppressed = lerp(0.0, val, centerBias);        // 越近越小，越远越还原原值

        // 远距离提升（缓慢向 1 趋近）
        double farBoost = 1.0 - Math.exp(-dist * 0.0005); // 越远越趋近 1.0（但永远不等于）
        return lerp(suppressed, 1.0, farBoost * 0.5);   // 乘一个小权重，控制影响速度（0.15 可调）
    }


    /**
     * 根据距离控制振幅和整体偏移：
     * - 距离 < 0.5：值总 ≥ 0.75
     * - 距离 > 50：值总 ≤ 0.5
     * - 中间平滑过渡但保留原始结构
     */
    public double applyDistanceRemap(double val, double fx, double fy) {
        double dist = Math.sqrt(fx * fx + fy * fy);

        // Case 1: 距离 ≤ 2.0，输出抬高到 [0.75, 1.0]
        if (dist <= 2) {
            return 0.75 + 0.25 * val;
        }

        // Case 2: 2 < dist < 8，线性过渡从 [0.75~1.0] 到原始 val
        if (dist < 8) {
            double t = (dist - 2) / 6.0; // t ∈ [0, 1]
            double boosted = 0.75 + 0.25 * val;
            return lerp(boosted, val, t);
        }

        // Case 3: 8 ≤ dist ≤ 36，保留原始噪声结构
        if (dist <= 36.0) {
            return val;
        }

        // Case 4: 36 < dist < 40，线性过渡从 val 到 [0.0, 0.5]
        if (dist < 40.0) {
            double t = (dist - 36.0) / 4.0; // t ∈ [0, 1]
            double lowered = 0.5 * val;
            return lerp(val, lowered, t);
        }

        // Case 5: dist ≥ 40，强制压低到 [0.0, 0.5]
        return 0.5 * val;
    }
}
