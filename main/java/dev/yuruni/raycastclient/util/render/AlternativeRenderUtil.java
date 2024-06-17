package dev.yuruni.raycastclient.util.render;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.yuruni.raycastclient.util.math.RegionPos;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.Chunk;
import org.joml.Matrix4f;

import static dev.yuruni.raycastclient.util.render.RenderUtil.getCameraBlockPos;
import static dev.yuruni.raycastclient.util.render.RenderUtil.getCameraPos;

public class AlternativeRenderUtil {

    private static final Box DEFAULT_BOX = new Box(0, 0, 0, 1, 1, 1);

    public static void applyRegionalRenderOffset(MatrixStack matrixStack)
    {
        applyRegionalRenderOffset(matrixStack, getCameraRegion());
    }

    public static void applyRegionalRenderOffset(MatrixStack matrixStack,
                                                 Chunk chunk)
    {
        applyRegionalRenderOffset(matrixStack, RegionPos.of(chunk.getPos()));
    }

    public static void applyRegionalRenderOffset(MatrixStack matrixStack,
                                                 RegionPos region)
    {
        Vec3d offset = region.toVec3d().subtract(getCameraPos());
        matrixStack.translate(offset.x, offset.y, offset.z);
    }

    public static void applyRenderOffset(MatrixStack matrixStack)
    {
        Vec3d camPos = getCameraPos();
        matrixStack.translate(-camPos.x, -camPos.y, -camPos.z);
    }

    public static RegionPos getCameraRegion()
    {
        return RegionPos.of(getCameraBlockPos());
    }

    public static void drawSolidBox(MatrixStack matrixStack)
    {
        drawSolidBox(DEFAULT_BOX, matrixStack);
    }

    public static void drawSolidBox(Box bb, MatrixStack matrixStack)
    {
        Matrix4f matrix = matrixStack.peek().getPositionMatrix();
        Tessellator tessellator = RenderSystem.renderThreadTesselator();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        RenderSystem.setShader(GameRenderer::getPositionProgram);

        bufferBuilder.begin(VertexFormat.DrawMode.QUADS,
                VertexFormats.POSITION);
        bufferBuilder
                .vertex(matrix, (float)bb.minX, (float)bb.minY, (float)bb.minZ)
                .next();
        bufferBuilder
                .vertex(matrix, (float)bb.maxX, (float)bb.minY, (float)bb.minZ)
                .next();
        bufferBuilder
                .vertex(matrix, (float)bb.maxX, (float)bb.minY, (float)bb.maxZ)
                .next();
        bufferBuilder
                .vertex(matrix, (float)bb.minX, (float)bb.minY, (float)bb.maxZ)
                .next();

        bufferBuilder
                .vertex(matrix, (float)bb.minX, (float)bb.maxY, (float)bb.minZ)
                .next();
        bufferBuilder
                .vertex(matrix, (float)bb.minX, (float)bb.maxY, (float)bb.maxZ)
                .next();
        bufferBuilder
                .vertex(matrix, (float)bb.maxX, (float)bb.maxY, (float)bb.maxZ)
                .next();
        bufferBuilder
                .vertex(matrix, (float)bb.maxX, (float)bb.maxY, (float)bb.minZ)
                .next();

        bufferBuilder
                .vertex(matrix, (float)bb.minX, (float)bb.minY, (float)bb.minZ)
                .next();
        bufferBuilder
                .vertex(matrix, (float)bb.minX, (float)bb.maxY, (float)bb.minZ)
                .next();
        bufferBuilder
                .vertex(matrix, (float)bb.maxX, (float)bb.maxY, (float)bb.minZ)
                .next();
        bufferBuilder
                .vertex(matrix, (float)bb.maxX, (float)bb.minY, (float)bb.minZ)
                .next();

        bufferBuilder
                .vertex(matrix, (float)bb.maxX, (float)bb.minY, (float)bb.minZ)
                .next();
        bufferBuilder
                .vertex(matrix, (float)bb.maxX, (float)bb.maxY, (float)bb.minZ)
                .next();
        bufferBuilder
                .vertex(matrix, (float)bb.maxX, (float)bb.maxY, (float)bb.maxZ)
                .next();
        bufferBuilder
                .vertex(matrix, (float)bb.maxX, (float)bb.minY, (float)bb.maxZ)
                .next();

        bufferBuilder
                .vertex(matrix, (float)bb.minX, (float)bb.minY, (float)bb.maxZ)
                .next();
        bufferBuilder
                .vertex(matrix, (float)bb.maxX, (float)bb.minY, (float)bb.maxZ)
                .next();
        bufferBuilder
                .vertex(matrix, (float)bb.maxX, (float)bb.maxY, (float)bb.maxZ)
                .next();
        bufferBuilder
                .vertex(matrix, (float)bb.minX, (float)bb.maxY, (float)bb.maxZ)
                .next();

        bufferBuilder
                .vertex(matrix, (float)bb.minX, (float)bb.minY, (float)bb.minZ)
                .next();
        bufferBuilder
                .vertex(matrix, (float)bb.minX, (float)bb.minY, (float)bb.maxZ)
                .next();
        bufferBuilder
                .vertex(matrix, (float)bb.minX, (float)bb.maxY, (float)bb.maxZ)
                .next();
        bufferBuilder
                .vertex(matrix, (float)bb.minX, (float)bb.maxY, (float)bb.minZ)
                .next();
        tessellator.draw();
    }

    public static void drawSolidBox(Box bb, VertexBuffer vertexBuffer)
    {
        Tessellator tessellator = RenderSystem.renderThreadTesselator();
        BufferBuilder bufferBuilder = tessellator.getBuffer();

        bufferBuilder.begin(VertexFormat.DrawMode.QUADS,
                VertexFormats.POSITION);
        drawSolidBox(bb, bufferBuilder);
        BufferBuilder.BuiltBuffer buffer = bufferBuilder.end();

        vertexBuffer.bind();
        vertexBuffer.upload(buffer);
        VertexBuffer.unbind();
    }

    public static void drawSolidBox(Box bb, BufferBuilder bufferBuilder)
    {
        bufferBuilder.vertex(bb.minX, bb.minY, bb.minZ).next();
        bufferBuilder.vertex(bb.maxX, bb.minY, bb.minZ).next();
        bufferBuilder.vertex(bb.maxX, bb.minY, bb.maxZ).next();
        bufferBuilder.vertex(bb.minX, bb.minY, bb.maxZ).next();

        bufferBuilder.vertex(bb.minX, bb.maxY, bb.minZ).next();
        bufferBuilder.vertex(bb.minX, bb.maxY, bb.maxZ).next();
        bufferBuilder.vertex(bb.maxX, bb.maxY, bb.maxZ).next();
        bufferBuilder.vertex(bb.maxX, bb.maxY, bb.minZ).next();

        bufferBuilder.vertex(bb.minX, bb.minY, bb.minZ).next();
        bufferBuilder.vertex(bb.minX, bb.maxY, bb.minZ).next();
        bufferBuilder.vertex(bb.maxX, bb.maxY, bb.minZ).next();
        bufferBuilder.vertex(bb.maxX, bb.minY, bb.minZ).next();

        bufferBuilder.vertex(bb.maxX, bb.minY, bb.minZ).next();
        bufferBuilder.vertex(bb.maxX, bb.maxY, bb.minZ).next();
        bufferBuilder.vertex(bb.maxX, bb.maxY, bb.maxZ).next();
        bufferBuilder.vertex(bb.maxX, bb.minY, bb.maxZ).next();

        bufferBuilder.vertex(bb.minX, bb.minY, bb.maxZ).next();
        bufferBuilder.vertex(bb.maxX, bb.minY, bb.maxZ).next();
        bufferBuilder.vertex(bb.maxX, bb.maxY, bb.maxZ).next();
        bufferBuilder.vertex(bb.minX, bb.maxY, bb.maxZ).next();

        bufferBuilder.vertex(bb.minX, bb.minY, bb.minZ).next();
        bufferBuilder.vertex(bb.minX, bb.minY, bb.maxZ).next();
        bufferBuilder.vertex(bb.minX, bb.maxY, bb.maxZ).next();
        bufferBuilder.vertex(bb.minX, bb.maxY, bb.minZ).next();
    }

    public static void drawOutlinedBox(MatrixStack matrixStack)
    {
        drawOutlinedBox(DEFAULT_BOX, matrixStack);
    }

    public static void drawOutlinedBox(Box bb, MatrixStack matrixStack)
    {
        Matrix4f matrix = matrixStack.peek().getPositionMatrix();
        Tessellator tessellator = RenderSystem.renderThreadTesselator();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        RenderSystem.setShader(GameRenderer::getPositionProgram);

        bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINES,
                VertexFormats.POSITION);
        bufferBuilder
                .vertex(matrix, (float)bb.minX, (float)bb.minY, (float)bb.minZ)
                .next();
        bufferBuilder
                .vertex(matrix, (float)bb.maxX, (float)bb.minY, (float)bb.minZ)
                .next();

        bufferBuilder
                .vertex(matrix, (float)bb.maxX, (float)bb.minY, (float)bb.minZ)
                .next();
        bufferBuilder
                .vertex(matrix, (float)bb.maxX, (float)bb.minY, (float)bb.maxZ)
                .next();

        bufferBuilder
                .vertex(matrix, (float)bb.maxX, (float)bb.minY, (float)bb.maxZ)
                .next();
        bufferBuilder
                .vertex(matrix, (float)bb.minX, (float)bb.minY, (float)bb.maxZ)
                .next();

        bufferBuilder
                .vertex(matrix, (float)bb.minX, (float)bb.minY, (float)bb.maxZ)
                .next();
        bufferBuilder
                .vertex(matrix, (float)bb.minX, (float)bb.minY, (float)bb.minZ)
                .next();

        bufferBuilder
                .vertex(matrix, (float)bb.minX, (float)bb.minY, (float)bb.minZ)
                .next();
        bufferBuilder
                .vertex(matrix, (float)bb.minX, (float)bb.maxY, (float)bb.minZ)
                .next();

        bufferBuilder
                .vertex(matrix, (float)bb.maxX, (float)bb.minY, (float)bb.minZ)
                .next();
        bufferBuilder
                .vertex(matrix, (float)bb.maxX, (float)bb.maxY, (float)bb.minZ)
                .next();

        bufferBuilder
                .vertex(matrix, (float)bb.maxX, (float)bb.minY, (float)bb.maxZ)
                .next();
        bufferBuilder
                .vertex(matrix, (float)bb.maxX, (float)bb.maxY, (float)bb.maxZ)
                .next();

        bufferBuilder
                .vertex(matrix, (float)bb.minX, (float)bb.minY, (float)bb.maxZ)
                .next();
        bufferBuilder
                .vertex(matrix, (float)bb.minX, (float)bb.maxY, (float)bb.maxZ)
                .next();

        bufferBuilder
                .vertex(matrix, (float)bb.minX, (float)bb.maxY, (float)bb.minZ)
                .next();
        bufferBuilder
                .vertex(matrix, (float)bb.maxX, (float)bb.maxY, (float)bb.minZ)
                .next();

        bufferBuilder
                .vertex(matrix, (float)bb.maxX, (float)bb.maxY, (float)bb.minZ)
                .next();
        bufferBuilder
                .vertex(matrix, (float)bb.maxX, (float)bb.maxY, (float)bb.maxZ)
                .next();

        bufferBuilder
                .vertex(matrix, (float)bb.maxX, (float)bb.maxY, (float)bb.maxZ)
                .next();
        bufferBuilder
                .vertex(matrix, (float)bb.minX, (float)bb.maxY, (float)bb.maxZ)
                .next();

        bufferBuilder
                .vertex(matrix, (float)bb.minX, (float)bb.maxY, (float)bb.maxZ)
                .next();
        bufferBuilder
                .vertex(matrix, (float)bb.minX, (float)bb.maxY, (float)bb.minZ)
                .next();
        tessellator.draw();
    }

    public static void drawOutlinedBox(Box bb, VertexBuffer vertexBuffer)
    {
        Tessellator tessellator = RenderSystem.renderThreadTesselator();
        BufferBuilder bufferBuilder = tessellator.getBuffer();

        bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINES,
                VertexFormats.POSITION);
        drawOutlinedBox(bb, bufferBuilder);
        BufferBuilder.BuiltBuffer buffer = bufferBuilder.end();

        vertexBuffer.bind();
        vertexBuffer.upload(buffer);
        VertexBuffer.unbind();
    }

    public static void drawOutlinedBox(Box bb, BufferBuilder bufferBuilder)
    {
        bufferBuilder.vertex(bb.minX, bb.minY, bb.minZ).next();
        bufferBuilder.vertex(bb.maxX, bb.minY, bb.minZ).next();

        bufferBuilder.vertex(bb.maxX, bb.minY, bb.minZ).next();
        bufferBuilder.vertex(bb.maxX, bb.minY, bb.maxZ).next();

        bufferBuilder.vertex(bb.maxX, bb.minY, bb.maxZ).next();
        bufferBuilder.vertex(bb.minX, bb.minY, bb.maxZ).next();

        bufferBuilder.vertex(bb.minX, bb.minY, bb.maxZ).next();
        bufferBuilder.vertex(bb.minX, bb.minY, bb.minZ).next();

        bufferBuilder.vertex(bb.minX, bb.minY, bb.minZ).next();
        bufferBuilder.vertex(bb.minX, bb.maxY, bb.minZ).next();

        bufferBuilder.vertex(bb.maxX, bb.minY, bb.minZ).next();
        bufferBuilder.vertex(bb.maxX, bb.maxY, bb.minZ).next();

        bufferBuilder.vertex(bb.maxX, bb.minY, bb.maxZ).next();
        bufferBuilder.vertex(bb.maxX, bb.maxY, bb.maxZ).next();

        bufferBuilder.vertex(bb.minX, bb.minY, bb.maxZ).next();
        bufferBuilder.vertex(bb.minX, bb.maxY, bb.maxZ).next();

        bufferBuilder.vertex(bb.minX, bb.maxY, bb.minZ).next();
        bufferBuilder.vertex(bb.maxX, bb.maxY, bb.minZ).next();

        bufferBuilder.vertex(bb.maxX, bb.maxY, bb.minZ).next();
        bufferBuilder.vertex(bb.maxX, bb.maxY, bb.maxZ).next();

        bufferBuilder.vertex(bb.maxX, bb.maxY, bb.maxZ).next();
        bufferBuilder.vertex(bb.minX, bb.maxY, bb.maxZ).next();

        bufferBuilder.vertex(bb.minX, bb.maxY, bb.maxZ).next();
        bufferBuilder.vertex(bb.minX, bb.maxY, bb.minZ).next();
    }
}
