package com.scarasol.sona.client.renderer;

import com.mojang.blaze3d.vertex.VertexConsumer;


/**
 * @author Scarasol
 */
public class AlphaVertexConsumer implements VertexConsumer {
    private final VertexConsumer delegate;
    private final float alphaMultiplier;

    public AlphaVertexConsumer(VertexConsumer delegate, float alphaMultiplier) {
        this.delegate = delegate;
        this.alphaMultiplier = alphaMultiplier;
    }

    @Override
    public VertexConsumer vertex(double x, double y, double z) {
        delegate.vertex(x, y, z);
        return this;
    }

    @Override
    public VertexConsumer color(int r, int g, int b, int a) {
        delegate.color(r, g, b, (int) (a * alphaMultiplier));
        return this;
    }

    @Override
    public VertexConsumer uv(float u, float v) {
        delegate.uv(u, v);
        return this;
    }

    @Override
    public VertexConsumer overlayCoords(int u, int v) {
        delegate.overlayCoords(u, v);
        return this;
    }

    @Override
    public VertexConsumer uv2(int u, int v) {
        delegate.uv2(u, v);
        return this;
    }

    @Override
    public VertexConsumer normal(float x, float y, float z) {
        delegate.normal(x, y, z);
        return this;
    }

    @Override
    public void endVertex() {
        delegate.endVertex();
    }

    @Override
    public void defaultColor(int r, int g, int b, int a) {
        delegate.defaultColor(r, g, b, (int) (a * alphaMultiplier));
    }

    @Override
    public void unsetDefaultColor() {
        delegate.unsetDefaultColor();
    }
}