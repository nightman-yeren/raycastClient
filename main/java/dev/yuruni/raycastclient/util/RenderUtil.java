package dev.yuruni.raycastclient.util;

import dev.yuruni.raycastclient.RaycastClient;
import net.minecraft.client.render.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.awt.*;

public class RenderUtil {

    public static final Matrix4f lastProjMat = new Matrix4f();

    public static final Matrix4f lastModMat = new Matrix4f();

    public static final Matrix4f lastWorldSpaceMatrix = new Matrix4f();

    private static final MinecraftClient client = MinecraftClient.getInstance();

    final float ROUND_QUALITY = 10;

    public Vec3d worldSpaceToScreenSpace( Vec3d pos) {
        Camera camera = client.getEntityRenderDispatcher().camera;
        int displayHeight = client.getWindow().getHeight();
        int[] viewport = new int[4];
        GL11.glGetIntegerv(GL11.GL_VIEWPORT, viewport);
        Vector3f target = new Vector3f();

        double deltaX = pos.x - camera.getPos().x;
        double deltaY = pos.y - camera.getPos().y;
        double deltaZ = pos.z - camera.getPos().z;

        Vector4f transformedCoordinates = new Vector4f((float) deltaX, (float) deltaY, (float) deltaZ, 1.f).mul(
                lastWorldSpaceMatrix);

        Matrix4f matrixProj = new Matrix4f(lastProjMat);
        Matrix4f matrixModel = new Matrix4f(lastModMat);

        matrixProj.mul(matrixModel)
                .project(transformedCoordinates.x(), transformedCoordinates.y(), transformedCoordinates.z(), viewport,
                        target);

        return new Vec3d(target.x / client.getWindow().getScaleFactor(),
                (displayHeight - target.y) / client.getWindow().getScaleFactor(), target.z);
    }

    public boolean screenSpaceCoordinateIsVisible(Vec3d pos) {
        return pos != null && pos.z > -1 && pos.z < 1;
    }

    public void drawTexturedQuad(Matrix4f matrix4f, Identifier texture, float x1, float y1, float width, float height, Color color) {
        float red = color.getRedFloat();
        float green = color.getGreenFloat();
        float blue = color.getBlueFloat();
        float alpha = color.getAlphaFloat();

        float x2 = x1 + width;
        float y2 = y1 + height;

        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShader(GameRenderer::getPositionColorTexProgram);
        RenderSystem.enableBlend();
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE);
        bufferBuilder.vertex(matrix4f, x1, y1, 0).color(red, green, blue, alpha).texture(0, 0).next();
        bufferBuilder.vertex(matrix4f, x1, y2, 0).color(red, green, blue, alpha).texture(0, 1).next();
        bufferBuilder.vertex(matrix4f, x2, y2, 0).color(red, green, blue, alpha).texture(1, 1).next();
        bufferBuilder.vertex(matrix4f, x2, y1, 0).color(red, green, blue, alpha).texture(1, 0).next();
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        RenderSystem.disableBlend();
    }

    public void drawBox(Matrix4f matrix4f, float x, float y, float width, float height, Color color) {

        RenderSystem.setShaderColor(color.getRedFloat(), color.getGreenFloat(), color.getBlueFloat(), color.getAlphaFloat());

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        Tessellator tessellator = RenderSystem.renderThreadTesselator();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        RenderSystem.setShader(GameRenderer::getPositionProgram);

        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
        bufferBuilder.vertex(matrix4f, x, y, 0).next();
        bufferBuilder.vertex(matrix4f, x + width, y, 0).next();
        bufferBuilder.vertex(matrix4f, x + width, y + height, 0).next();
        bufferBuilder.vertex(matrix4f, x, y + height, 0).next();

        tessellator.draw();

        RenderSystem.setShaderColor(1, 1, 1, 1);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_BLEND);
    }

    public void drawCircle(Matrix4f matrix4f, float x, float y, float radius, Color color) {
        RenderSystem.setShaderColor(color.getRedFloat(), color.getGreenFloat(), color.getBlueFloat(), color.getAlphaFloat());

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        Tessellator tessellator = RenderSystem.renderThreadTesselator();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        RenderSystem.setShader(GameRenderer::getPositionProgram);

        bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION);

        double roundedInterval = (360.0f / 30.0f);

        for(int i = 0; i < 30; i++) {
            double angle = Math.toRadians(0 + (i * roundedInterval));
            double angle2 = Math.toRadians(0 + ((i + 1) * roundedInterval));
            float radiusX1 = (float)(Math.cos(angle) * radius);
            float radiusY1 = (float)Math.sin(angle) * radius;
            float radiusX2 = (float)Math.cos(angle2) * radius;
            float radiusY2 = (float)Math.sin(angle2) * radius;

            bufferBuilder.vertex(matrix4f, x, y, 0).next();
            bufferBuilder.vertex(matrix4f, x + radiusX1, y + radiusY1, 0).next();
            bufferBuilder.vertex(matrix4f, x + radiusX2, y + radiusY2, 0).next();
        }

        tessellator.draw();

        RenderSystem.setShaderColor(1, 1, 1, 1);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_BLEND);
    }

    public void drawRoundedBox(Matrix4f matrix4f, float x, float y, float width, float height, float radius, Color color) {
        RenderSystem.setShaderColor(color.getRedFloat(), color.getGreenFloat(), color.getBlueFloat(), color.getAlphaFloat());

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        Tessellator tessellator = RenderSystem.renderThreadTesselator();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        RenderSystem.setShader(GameRenderer::getPositionProgram);

        bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION);

        buildFilledArc(bufferBuilder, matrix4f, x + radius, y + radius, radius, 180.0f, 90.0f);
        buildFilledArc(bufferBuilder, matrix4f, x + width - radius, y + radius, radius, 270.0f, 90.0f);
        buildFilledArc(bufferBuilder, matrix4f, x + width - radius, y + height - radius, radius, 0.0f, 90.0f);
        buildFilledArc(bufferBuilder, matrix4f, x + radius, y + height - radius, radius, 90.0f, 90.0f);

        // |---
        bufferBuilder.vertex(matrix4f, x + radius, y, 0).next();
        bufferBuilder.vertex(matrix4f, x + width - radius, y, 0).next();
        bufferBuilder.vertex(matrix4f, x + radius, y + radius, 0).next();

        // ---|
        bufferBuilder.vertex(matrix4f, x + radius, y + radius, 0).next();
        bufferBuilder.vertex(matrix4f, x + width - radius, y, 0).next();
        bufferBuilder.vertex(matrix4f, x + width - radius, y + radius, 0).next();

        // _||
        bufferBuilder.vertex(matrix4f, x + width - radius, y + radius, 0).next();
        bufferBuilder.vertex(matrix4f, x + width, y + radius, 0).next();
        bufferBuilder.vertex(matrix4f, x + width - radius, y + height - radius, 0).next();

        // |||
        bufferBuilder.vertex(matrix4f, x + width, y + radius, 0).next();
        bufferBuilder.vertex(matrix4f, x + width, y + height - radius, 0).next();
        bufferBuilder.vertex(matrix4f, x + width - radius, y + height - radius, 0).next();

        /// __|
        bufferBuilder.vertex(matrix4f, x + width - radius, y + height - radius, 0).next();
        bufferBuilder.vertex(matrix4f, x + width - radius, y + height, 0).next();
        bufferBuilder.vertex(matrix4f, x + radius, y + height - radius, 0).next();

        // |__
        bufferBuilder.vertex(matrix4f, x + radius, y + height - radius, 0).next();
        bufferBuilder.vertex(matrix4f, x + radius, y + height, 0).next();
        bufferBuilder.vertex(matrix4f, x + width - radius, y + height, 0).next();

        // |||
        bufferBuilder.vertex(matrix4f, x + radius, y + height - radius, 0).next();
        bufferBuilder.vertex(matrix4f, x, y + height - radius, 0).next();
        bufferBuilder.vertex(matrix4f, x , y + radius, 0).next();

        /// ||-
        bufferBuilder.vertex(matrix4f, x , y + radius, 0).next();
        bufferBuilder.vertex(matrix4f, x + radius , y + radius, 0).next();
        bufferBuilder.vertex(matrix4f, x + radius, y + height - radius, 0).next();

        /// |-/
        bufferBuilder.vertex(matrix4f, x + radius , y + radius, 0).next();
        bufferBuilder.vertex(matrix4f, x + width - radius , y + radius, 0).next();
        bufferBuilder.vertex(matrix4f, x + radius , y + height - radius, 0).next();

        /// /_|
        bufferBuilder.vertex(matrix4f, x + radius , y + height - radius, 0).next();
        bufferBuilder.vertex(matrix4f, x + width - radius , y + height - radius, 0).next();
        bufferBuilder.vertex(matrix4f, x + width - radius , y + radius, 0).next();

        tessellator.draw();

        RenderSystem.setShaderColor(1, 1, 1, 1);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_BLEND);
    }

    public void drawRoundedOutline(Matrix4f matrix4f, float x, float y, float width, float height, float radius, Color color) {
        RenderSystem.setShaderColor(color.getRedFloat(), color.getGreenFloat(), color.getBlueFloat(), color.getAlphaFloat());

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        Tessellator tessellator = RenderSystem.renderThreadTesselator();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        RenderSystem.setShader(GameRenderer::getPositionProgram);

        bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION);

        // Top Left Arc and Top
        buildArc(bufferBuilder, matrix4f, x + radius, y + radius, radius, 180.0f, 90.0f);
        bufferBuilder.vertex(matrix4f, x + radius, y, 0).next();
        bufferBuilder.vertex(matrix4f, x + width - radius, y, 0).next();

        // Top Right Arc and Right
        buildArc(bufferBuilder, matrix4f, x + width - radius, y + radius, radius, 270.0f, 90.0f);
        bufferBuilder.vertex(matrix4f, x + width, y + radius, 0).next();
        bufferBuilder.vertex(matrix4f, x + width, y + height - radius, 0).next();

        // Bottom Right
        buildArc(bufferBuilder, matrix4f, x + width - radius, y + height - radius, radius, 0.0f, 90.0f);
        bufferBuilder.vertex(matrix4f, x + width - radius, y + height, 0).next();
        bufferBuilder.vertex(matrix4f, x + radius, y + height, 0).next();

        // Bottom Left
        buildArc(bufferBuilder, matrix4f, x + radius, y + height - radius, radius, 90.0f, 90.0f);
        bufferBuilder.vertex(matrix4f, x, y + height - radius, 0).next();
        bufferBuilder.vertex(matrix4f, x, y + radius, 0).next();

        tessellator.draw();

        RenderSystem.setShaderColor(1, 1, 1, 1);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
    }

    public void drawOutlinedBox(Matrix4f matrix4f, float x, float y, float width, float height, Color outlineColor, Color backgroundColor) {
        RenderSystem.setShaderColor(backgroundColor.getRedFloat(), backgroundColor.getGreenFloat(), backgroundColor.getBlueFloat(), backgroundColor.getAlphaFloat());

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        Tessellator tessellator = RenderSystem.renderThreadTesselator();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        RenderSystem.setShader(GameRenderer::getPositionProgram);

        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);

        bufferBuilder.vertex(matrix4f, x, y, 0).next();
        bufferBuilder.vertex(matrix4f, x + width, y, 0).next();
        bufferBuilder.vertex(matrix4f, x + width, y + height, 0).next();
        bufferBuilder.vertex(matrix4f, x, y + height, 0).next();

        tessellator.draw();

        RenderSystem.setShaderColor(outlineColor.getRedFloat(), outlineColor.getGreenFloat(), outlineColor.getBlueFloat(), outlineColor.getAlphaFloat());
        RenderSystem.setShader(GameRenderer::getPositionProgram);
        bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION);

        bufferBuilder.vertex(matrix4f, x, y, 0).next();
        bufferBuilder.vertex(matrix4f, x + width, y, 0).next();
        bufferBuilder.vertex(matrix4f, x + width, y + height, 0).next();
        bufferBuilder.vertex(matrix4f, x, y + height, 0).next();
        bufferBuilder.vertex(matrix4f, x, y, 0).next();

        tessellator.draw();
        RenderSystem.setShaderColor(1, 1, 1, 1);

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_BLEND);
    }

    public void drawOutlinedBox(Matrix4f matrix4f, float x, float y, float width, float height, Color color) {
        drawOutlinedBox(matrix4f, x, y, width, height, new Color(0, 0, 0), color);
    }

    public void drawLine(Matrix4f matrix4f, float x1, float y1, float x2, float y2, Color color) {
        RenderSystem.setShaderColor(color.getRedFloat(), color.getGreenFloat(), color.getBlueFloat(), color.getAlphaFloat());

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        Tessellator tessellator = RenderSystem.renderThreadTesselator();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        RenderSystem.setShader(GameRenderer::getPositionProgram);

        bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION);

        bufferBuilder.vertex(matrix4f, x1, y1, 0).next();
        bufferBuilder.vertex(matrix4f, x2, y2, 0).next();

        tessellator.draw();
        RenderSystem.setShaderColor(1, 1, 1, 1);

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_BLEND);
    }

    public void drawHorizontalGradient(Matrix4f matrix4f, float x, float y, float width, float height, Color startColor, Color endColor) {
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        Tessellator tessellator = RenderSystem.renderThreadTesselator();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        bufferBuilder.vertex(matrix4f, x, y, 0.0F).color(startColor.getColorAsInt()).next();
        bufferBuilder.vertex(matrix4f, x + width, y, 0.0F).color(endColor.getColorAsInt()).next();
        bufferBuilder.vertex(matrix4f, x + width, y + height, 0.0F).color(endColor.getColorAsInt()).next();
        bufferBuilder.vertex(matrix4f, x, y + height, 0.0F).color(startColor.getColorAsInt()).next();

        tessellator.draw();
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_BLEND);
    }

    public void drawVerticalGradient(Matrix4f matrix4f, float x, float y, float width, float height, Color startColor, Color endColor) {
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        Tessellator tessellator = RenderSystem.renderThreadTesselator();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        bufferBuilder.vertex(matrix4f, x, y, 0.0F).color(startColor.getColorAsInt()).next();
        bufferBuilder.vertex(matrix4f, x + width, y, 0.0F).color(startColor.getColorAsInt()).next();
        bufferBuilder.vertex(matrix4f, x + width, y + height, 0.0F).color(endColor.getColorAsInt()).next();
        bufferBuilder.vertex(matrix4f, x, y + height, 0.0F).color(endColor.getColorAsInt()).next();

        tessellator.draw();
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_BLEND);
    }

    public void drawOutline(Matrix4f matrix4f, float x, float y, float width, float height) {
        RenderSystem.setShaderColor(0, 0, 0, 1);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        Tessellator tessellator = RenderSystem.renderThreadTesselator();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        RenderSystem.setShader(GameRenderer::getPositionProgram);

        bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION);

        bufferBuilder.vertex(matrix4f, x, y, 0).next();
        bufferBuilder.vertex(matrix4f, x + width, y, 0).next();
        bufferBuilder.vertex(matrix4f, x + width, y + height, 0).next();
        bufferBuilder.vertex(matrix4f, x, y + height, 0).next();
        bufferBuilder.vertex(matrix4f, x, y, 0).next();

        tessellator.draw();
        RenderSystem.setShaderColor(1, 1, 1, 1);

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_BLEND);
    }

    public void drawOutline(Matrix4f matrix4f, float x, float y, float width, float height, Color color) {
        RenderSystem.setShaderColor(color.getRedFloat(), color.getGreenFloat(), color.getBlueFloat(), color.getAlphaFloat());

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        Tessellator tessellator = RenderSystem.renderThreadTesselator();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        RenderSystem.setShader(GameRenderer::getPositionProgram);

        bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION);

        bufferBuilder.vertex(matrix4f, x, y, 0).next();
        bufferBuilder.vertex(matrix4f, x + width, y, 0).next();
        bufferBuilder.vertex(matrix4f, x + width, y + height, 0).next();
        bufferBuilder.vertex(matrix4f, x, y + height, 0).next();
        bufferBuilder.vertex(matrix4f, x, y, 0).next();

        tessellator.draw();
        RenderSystem.setShaderColor(1, 1, 1, 1);

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_BLEND);
    }

    public void draw3DBox(Matrix4f matrix4f, Box box, Color color) {
        RenderSystem.setShaderColor(color.getRedFloat(), color.getGreenFloat(), color.getBlueFloat(), 1.0f);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        Tessellator tessellator = RenderSystem.renderThreadTesselator();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        RenderSystem.setShader(GameRenderer::getPositionProgram);

        bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION);

        bufferBuilder.vertex(matrix4f, (float) box.minX, (float) box.minY, (float) box.minZ).next();
        bufferBuilder.vertex(matrix4f, (float) box.maxX, (float) box.minY, (float) box.minZ).next();

        bufferBuilder.vertex(matrix4f, (float) box.maxX, (float) box.minY, (float) box.minZ).next();
        bufferBuilder.vertex(matrix4f, (float) box.maxX, (float) box.minY, (float) box.maxZ).next();

        bufferBuilder.vertex(matrix4f, (float) box.maxX, (float) box.minY, (float) box.maxZ).next();
        bufferBuilder.vertex(matrix4f, (float) box.minX, (float) box.minY, (float) box.maxZ).next();

        bufferBuilder.vertex(matrix4f, (float) box.minX, (float) box.minY, (float) box.maxZ).next();
        bufferBuilder.vertex(matrix4f, (float) box.minX, (float) box.minY, (float) box.minZ).next();

        bufferBuilder.vertex(matrix4f, (float) box.minX, (float) box.minY, (float) box.minZ).next();
        bufferBuilder.vertex(matrix4f, (float) box.minX, (float) box.maxY, (float) box.minZ).next();

        bufferBuilder.vertex(matrix4f, (float) box.maxX, (float) box.minY, (float) box.minZ).next();
        bufferBuilder.vertex(matrix4f, (float) box.maxX, (float) box.maxY, (float) box.minZ).next();

        bufferBuilder.vertex(matrix4f, (float) box.maxX, (float) box.minY, (float) box.maxZ).next();
        bufferBuilder.vertex(matrix4f, (float) box.maxX, (float) box.maxY, (float) box.maxZ).next();

        bufferBuilder.vertex(matrix4f, (float) box.minX, (float) box.minY, (float) box.maxZ).next();
        bufferBuilder.vertex(matrix4f, (float) box.minX, (float) box.maxY, (float) box.maxZ).next();

        bufferBuilder.vertex(matrix4f, (float) box.minX, (float) box.maxY, (float) box.minZ).next();
        bufferBuilder.vertex(matrix4f, (float) box.maxX, (float) box.maxY, (float) box.minZ).next();

        bufferBuilder.vertex(matrix4f, (float) box.maxX, (float) box.maxY, (float) box.minZ).next();
        bufferBuilder.vertex(matrix4f, (float) box.maxX, (float) box.maxY, (float) box.maxZ).next();

        bufferBuilder.vertex(matrix4f, (float) box.maxX, (float) box.maxY, (float) box.maxZ).next();
        bufferBuilder.vertex(matrix4f, (float) box.minX, (float) box.maxY, (float) box.maxZ).next();

        bufferBuilder.vertex(matrix4f, (float) box.minX, (float) box.maxY, (float) box.maxZ).next();
        bufferBuilder.vertex(matrix4f, (float) box.minX, (float) box.maxY, (float) box.minZ).next();

        tessellator.draw();

        RenderSystem.setShaderColor(color.getRedFloat(), color.getGreenFloat(), color.getBlueFloat(), color.getAlphaFloat());
        RenderSystem.setShader(GameRenderer::getPositionProgram);

        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);

        bufferBuilder.vertex(matrix4f, (float) box.minX, (float) box.minY, (float) box.minZ).next();
        bufferBuilder.vertex(matrix4f, (float) box.maxX, (float) box.minY, (float) box.minZ).next();
        bufferBuilder.vertex(matrix4f, (float) box.maxX, (float) box.minY, (float) box.maxZ).next();
        bufferBuilder.vertex(matrix4f, (float) box.minX, (float) box.minY, (float) box.maxZ).next();

        bufferBuilder.vertex(matrix4f, (float) box.minX, (float) box.maxY, (float) box.minZ).next();
        bufferBuilder.vertex(matrix4f, (float) box.minX, (float) box.maxY, (float) box.maxZ).next();
        bufferBuilder.vertex(matrix4f, (float) box.maxX, (float) box.maxY, (float) box.maxZ).next();
        bufferBuilder.vertex(matrix4f, (float) box.maxX, (float) box.maxY, (float) box.minZ).next();

        bufferBuilder.vertex(matrix4f, (float) box.minX, (float) box.minY, (float) box.minZ).next();
        bufferBuilder.vertex(matrix4f, (float) box.minX, (float) box.maxY, (float) box.minZ).next();
        bufferBuilder.vertex(matrix4f, (float) box.maxX, (float) box.maxY, (float) box.minZ).next();
        bufferBuilder.vertex(matrix4f, (float) box.maxX, (float) box.minY, (float) box.minZ).next();

        bufferBuilder.vertex(matrix4f, (float) box.maxX, (float) box.minY, (float) box.minZ).next();
        bufferBuilder.vertex(matrix4f, (float) box.maxX, (float) box.maxY, (float) box.minZ).next();
        bufferBuilder.vertex(matrix4f, (float) box.maxX, (float) box.maxY, (float) box.maxZ).next();
        bufferBuilder.vertex(matrix4f, (float) box.maxX, (float) box.minY, (float) box.maxZ).next();

        bufferBuilder.vertex(matrix4f, (float) box.minX, (float) box.minY, (float) box.maxZ).next();
        bufferBuilder.vertex(matrix4f, (float) box.maxX, (float) box.minY, (float) box.maxZ).next();
        bufferBuilder.vertex(matrix4f, (float) box.maxX, (float) box.maxY, (float) box.maxZ).next();
        bufferBuilder.vertex(matrix4f, (float) box.minX, (float) box.maxY, (float) box.maxZ).next();

        bufferBuilder.vertex(matrix4f, (float) box.minX, (float) box.minY, (float) box.minZ).next();
        bufferBuilder.vertex(matrix4f, (float) box.minX, (float) box.minY, (float) box.maxZ).next();
        bufferBuilder.vertex(matrix4f, (float) box.minX, (float) box.maxY, (float) box.maxZ).next();
        bufferBuilder.vertex(matrix4f, (float) box.minX, (float) box.maxY, (float) box.minZ).next();
        tessellator.draw();
        RenderSystem.setShaderColor(1, 1, 1, 1);

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_BLEND);
    }

    public void drawTransparent3DBox(MatrixStack matrixStack, Box box, Color color, float alpha) {
        if (!RaycastClient.renderObjects) return;
        RenderSystem.setShaderColor(color.getRedFloat(), color.getGreenFloat(), color.getBlueFloat(), 1.0f);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        Matrix4f matrix = matrixStack.peek().getPositionMatrix();
        Tessellator tessellator = RenderSystem.renderThreadTesselator();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        RenderSystem.setShader(GameRenderer::getPositionProgram);

        bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION);

        bufferBuilder.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.minZ).next();
        bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.minZ).next();

        bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.minZ).next();
        bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.maxZ).next();

        bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.maxZ).next();
        bufferBuilder.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.maxZ).next();

        bufferBuilder.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.maxZ).next();
        bufferBuilder.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.minZ).next();

        bufferBuilder.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.minZ).next();
        bufferBuilder.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.minZ).next();

        bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.minZ).next();
        bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.minZ).next();

        bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.maxZ).next();
        bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.maxZ).next();

        bufferBuilder.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.maxZ).next();
        bufferBuilder.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.maxZ).next();

        bufferBuilder.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.minZ).next();
        bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.minZ).next();

        bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.minZ).next();
        bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.maxZ).next();

        bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.maxZ).next();
        bufferBuilder.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.maxZ).next();

        bufferBuilder.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.maxZ).next();
        bufferBuilder.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.minZ).next();

        tessellator.draw();

        RenderSystem.setShaderColor(color.getRedFloat(), color.getGreenFloat(), color.getBlueFloat(), alpha);
        RenderSystem.setShader(GameRenderer::getPositionProgram);

        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);

        bufferBuilder.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.minZ).next();
        bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.minZ).next();
        bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.maxZ).next();
        bufferBuilder.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.maxZ).next();

        bufferBuilder.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.minZ).next();
        bufferBuilder.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.maxZ).next();
        bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.maxZ).next();
        bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.minZ).next();

        bufferBuilder.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.minZ).next();
        bufferBuilder.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.minZ).next();
        bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.minZ).next();
        bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.minZ).next();

        bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.minZ).next();
        bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.minZ).next();
        bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.maxZ).next();
        bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.maxZ).next();

        bufferBuilder.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.maxZ).next();
        bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.maxZ).next();
        bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.maxZ).next();
        bufferBuilder.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.maxZ).next();

        bufferBuilder.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.minZ).next();
        bufferBuilder.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.maxZ).next();
        bufferBuilder.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.maxZ).next();
        bufferBuilder.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.minZ).next();
        tessellator.draw();
        RenderSystem.setShaderColor(1, 1, 1, 1);

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_BLEND);
    }

    public void drawLine3D(Matrix4f matrix4f, Vec3d pos, Vec3d pos2, Color color) {
        RenderSystem.setShaderColor(color.getRedFloat(), color.getGreenFloat(), color.getBlueFloat(), color.getAlphaFloat());

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        Tessellator tessellator = RenderSystem.renderThreadTesselator();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        RenderSystem.setShader(GameRenderer::getPositionProgram);

        bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION);

        bufferBuilder.vertex(matrix4f, (float) pos.x, (float) pos.y, (float) pos.z).next();
        bufferBuilder.vertex(matrix4f, (float) pos2.x, (float) pos2.y, (float) pos2.z).next();

        tessellator.draw();
        RenderSystem.setShaderColor(1, 1, 1, 1);

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_BLEND);
    }

    /*
    public void drawString(DrawContext drawContext, String text, float x, float y, Color color) {
        AobaClient aoba = Aoba.getInstance();
        MatrixStack matrixStack = drawContext.getMatrices();
        matrixStack.push();
        matrixStack.scale(2.0f, 2.0f, 1.0f);
        matrixStack.translate(-x / 2, -y / 2, 0.0f);
        drawContext.drawText(aoba.fontManager.GetRenderer(), text, (int)x, (int)y, color.getColorAsInt(), false);
        matrixStack.pop();
    }

    public void drawString(DrawContext drawContext, String text, float x, float y, int color) {
        AobaClient aoba = Aoba.getInstance();
        MatrixStack matrixStack = drawContext.getMatrices();
        matrixStack.push();
        matrixStack.scale(2.0f, 2.0f, 1.0f);
        matrixStack.translate(-x / 2, -y / 2, 0.0f);
        drawContext.drawText(aoba.fontManager.GetRenderer(), text, (int)x, (int)y, color, false);
        matrixStack.pop();
    }

    public void drawStringWithScale(DrawContext drawContext, String text, float x, float y, Color color, float scale) {
        AobaClient aoba = Aoba.getInstance();
        MatrixStack matrixStack = drawContext.getMatrices();
        matrixStack.push();
        matrixStack.scale(scale, scale, 1.0f);
        if (scale > 1.0f) {
            matrixStack.translate(-x / scale, -y / scale, 0.0f);
        } else {
            matrixStack.translate((x / scale) - x, (y * scale) - y, 0.0f);
        }
        drawContext.drawText(aoba.fontManager.GetRenderer(), text, (int)x, (int)y, color.getColorAsInt(), false);
        matrixStack.pop();
    }

    public void drawStringWithScale(DrawContext drawContext, String text, float x, float y, int color, float scale) {
        AobaClient aoba = Aoba.getInstance();
        MatrixStack matrixStack = drawContext.getMatrices();
        matrixStack.push();
        matrixStack.scale(scale, scale, 1.0f);
        if (scale > 1.0f) {
            matrixStack.translate(-x / scale, -y / scale, 0.0f);
        } else {
            matrixStack.translate(x / scale, y * scale, 0.0f);
        }
        drawContext.drawText(aoba.fontManager.GetRenderer(), text, (int)x, (int)y, color, false);
        matrixStack.pop();
    }
     */
    //TODO: make font manager and fix this shit

    private void buildFilledArc(BufferBuilder bufferBuilder, Matrix4f matrix, float x, float y, float radius, float startAngle, float sweepAngle) {
        double roundedInterval = (sweepAngle / ROUND_QUALITY);

        for(int i = 0; i < ROUND_QUALITY; i++) {
            double angle = Math.toRadians(startAngle + (i * roundedInterval));
            double angle2 = Math.toRadians(startAngle + ((i + 1) * roundedInterval));
            float radiusX1 = (float)(Math.cos(angle) * radius);
            float radiusY1 = (float)Math.sin(angle) * radius;
            float radiusX2 = (float)Math.cos(angle2) * radius;
            float radiusY2 = (float)Math.sin(angle2) * radius;

            bufferBuilder.vertex(matrix, x, y, 0).next();
            bufferBuilder.vertex(matrix, x + radiusX1, y + radiusY1, 0).next();
            bufferBuilder.vertex(matrix, x + radiusX2, y + radiusY2, 0).next();
        }
    }

    private void buildArc(BufferBuilder bufferBuilder, Matrix4f matrix, float x, float y, float radius, float startAngle, float sweepAngle) {
        double roundedInterval = (sweepAngle / ROUND_QUALITY);

        for(int i = 0; i < ROUND_QUALITY; i++) {
            double angle = Math.toRadians(startAngle + (i * roundedInterval));
            float radiusX1 = (float) (Math.cos(angle) * radius);
            float radiusY1 = (float)Math.sin(angle) * radius;

            bufferBuilder.vertex(matrix, x + radiusX1, y + radiusY1, 0).next();
        }
    }

    /**
     * Gets the interpolated position of the entity given a tick delta.
     * @param entity Entity to get position of
     * @param delta Tick delta.
     * @return Vec3d representing the interpolated position of the entity.
     */
    public Vec3d getEntityPositionInterpolated(Entity entity, float delta) {
        return new Vec3d(MathHelper.lerp(delta, entity.prevX, entity.getX()),
                MathHelper.lerp(delta, entity.prevY, entity.getY()),
                MathHelper.lerp(delta, entity.prevZ, entity.getZ()));
    }

    /**
     * Gets the difference between the interpolated position and 
     * @param entity Entity to get position of
     * @param delta Tick delta.
     * @return Vec3d representing the interpolated position of the entity.
     */
    public Vec3d getEntityPositionOffsetInterpolated(Entity entity, float delta) {
        Vec3d interpolated = getEntityPositionInterpolated(entity, delta);
        return entity.getPos().subtract(interpolated);
    }

    public static void applyRenderOffset(MatrixStack matrixStack) {
        if (!RaycastClient.renderObjects) return;
        Vec3d camPos = client.getBlockEntityRenderDispatcher().camera.getPos();
        matrixStack.translate(-camPos.x, -camPos.y, -camPos.z);
    }

    public void applyRegionalRenderOffset(MatrixStack matrixStack) {
        if (!RaycastClient.renderObjects) return;
        Vec3d camPos = client.getBlockEntityRenderDispatcher().camera.getPos();
        BlockPos camBlockPos = client.getBlockEntityRenderDispatcher().camera.getBlockPos();

        int regionX = (camBlockPos.getX() >> 9) * 512;
        int regionZ = (camBlockPos.getZ() >> 9) * 512;

        matrixStack.translate(regionX - camPos.x, -camPos.y, regionZ - camPos.z);
    }
}
