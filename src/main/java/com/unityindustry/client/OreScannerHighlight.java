package com.unityindustry.client;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.unityindustry.network.OreScanPayload;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

/** Real x-ray highlights (no terrain edits): a translucent cyan volume is drawn
 *  with depth testing disabled, following Fabric's 1.21.11 world render example.
 *  Only positions explicitly revealed by the server are drawn, max 96 per scan.
 */
public final class OreScannerHighlight {
    private static List<BlockPos> revealed = List.of();
    private static int ticksLeft;
    private static final RenderPipeline XRAY = RenderPipelines.register(
        RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath("unityindustry", "pipeline/ore_scanner_xray"))
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(false)
            .build());
    private static final ByteBufferBuilder ALLOCATOR = new ByteBufferBuilder(RenderType.SMALL_BUFFER_SIZE);
    private static final Vector4f MODULATOR = new Vector4f(1f, 1f, 1f, 1f);
    private static final Vector3f MODEL_OFFSET = new Vector3f();
    private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
    private static MappableRingBuffer gpuBuffer;

    public static void initialize() {
        ClientPlayNetworking.registerGlobalReceiver(OreScanPayload.ID, (payload, context) ->
            context.client().execute(() -> receive(payload.packedPositions())));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.level == null || client.player == null) {
                revealed = List.of(); ticksLeft = 0;
            } else if (ticksLeft > 0 && --ticksLeft == 0) revealed = List.of();
        });
        WorldRenderEvents.BEFORE_TRANSLUCENT.register(OreScannerHighlight::render);
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            if (gpuBuffer != null) { gpuBuffer.close(); gpuBuffer = null; }
            ALLOCATOR.close();
        });
    }

    private static void render(WorldRenderContext ctx) {
        if (revealed.isEmpty() || ticksLeft <= 0) return;
        Minecraft client = Minecraft.getInstance();
        if (client.level == null) return;
        PoseStack matrices = ctx.matrices();
        Vec3 camera = ctx.worldState().cameraRenderState.pos;
        matrices.pushPose();
        matrices.translate(-camera.x, -camera.y, -camera.z);
        BufferBuilder vertices = new BufferBuilder(ALLOCATOR,
            XRAY.getVertexFormatMode(), XRAY.getVertexFormat());
        Matrix4fc pose = matrices.last().pose();
        // See-through overlay on all six faces, so enclosed ores remain visible.
        // Color is cyan; alpha softens overlap when many ores share the screen.
        for (BlockPos p : revealed) {
            float x=p.getX(), y=p.getY(), z=p.getZ();
            addBox(vertices, pose, x+0.05f,y+0.05f,z+0.05f,
                x+0.95f,y+0.95f,z+0.95f, 0.19f,0.96f,1f,0.36f);
            // Tiny high-contrast central core improves distant readability.
            addBox(vertices, pose, x+0.39f,y+0.39f,z+0.39f,
                x+0.61f,y+0.61f,z+0.61f, 0.68f,1f,1f,0.83f);
        }
        matrices.popPose();
        try (MeshData mesh = vertices.buildOrThrow()) { draw(client, mesh); }
    }

    private static void quad(BufferBuilder b, Matrix4fc m, float r,float g,float bl,float a,
                             float x1,float y1,float z1, float x2,float y2,float z2,
                             float x3,float y3,float z3, float x4,float y4,float z4) {
        b.addVertex(m,x1,y1,z1).setColor(r,g,bl,a);
        b.addVertex(m,x2,y2,z2).setColor(r,g,bl,a);
        b.addVertex(m,x3,y3,z3).setColor(r,g,bl,a);
        b.addVertex(m,x4,y4,z4).setColor(r,g,bl,a);
    }

    private static void addBox(BufferBuilder b, Matrix4fc m,
                               float x,float y,float z, float X,float Y,float Z,
                               float r,float g,float blue,float alpha) {
        quad(b,m,r,g,blue,alpha, x,y,Z, X,y,Z, X,Y,Z, x,Y,Z);
        quad(b,m,r,g,blue,alpha, X,y,z, x,y,z, x,Y,z, X,Y,z);
        quad(b,m,r,g,blue,alpha, x,y,z, x,y,Z, x,Y,Z, x,Y,z);
        quad(b,m,r,g,blue,alpha, X,y,Z, X,y,z, X,Y,z, X,Y,Z);
        quad(b,m,r,g,blue,alpha, x,Y,Z, X,Y,Z, X,Y,z, x,Y,z);
        quad(b,m,r,g,blue,alpha, x,y,z, X,y,z, X,y,Z, x,y,Z);
    }

    /** Match Fabric's 1.21.11 custom-pipeline upload/draw path. */
    private static void draw(Minecraft client, MeshData mesh) {
        MeshData.DrawState state = mesh.drawState();
        VertexFormat format = state.format();
        int bytes = state.vertexCount() * format.getVertexSize();
        if (gpuBuffer == null || gpuBuffer.size() < bytes) {
            if (gpuBuffer != null) gpuBuffer.close();
            gpuBuffer = new MappableRingBuffer(() -> "UnityIndustry ore scanner x-ray",
                GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_MAP_WRITE, bytes);
        }
        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
        try (GpuBuffer.MappedView map = encoder.mapBuffer(
                gpuBuffer.currentBuffer().slice(0, mesh.vertexBuffer().remaining()), false, true)) {
            MemoryUtil.memCopy(mesh.vertexBuffer(), map.data());
        }
        // Position+color translucent quads; sort in the same way as Fabric's sample.
        mesh.sortQuads(ALLOCATOR, RenderSystem.getProjectionType().vertexSorting());
        GpuBuffer indexBuffer = XRAY.getVertexFormat().uploadImmediateIndexBuffer(mesh.indexBuffer());
        GpuBufferSlice uniforms = RenderSystem.getDynamicUniforms().writeTransform(
            RenderSystem.getModelViewMatrix(), MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);
        try (RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                () -> "UnityIndustry Ore Scanner x-ray", client.getMainRenderTarget().getColorTextureView(),
                OptionalInt.empty(), client.getMainRenderTarget().getDepthTextureView(), OptionalDouble.empty())) {
            pass.setPipeline(XRAY);
            RenderSystem.bindDefaultUniforms(pass);
            pass.setUniform("DynamicTransforms", uniforms);
            pass.setVertexBuffer(0, gpuBuffer.currentBuffer());
            pass.setIndexBuffer(indexBuffer, mesh.drawState().indexType());
            pass.drawIndexed(0, 0, state.indexCount(), 1);
        }
        gpuBuffer.rotate();
    }

    private static void receive(String packed) {
        if (packed.isEmpty()) { revealed = List.of(); ticksLeft = 0; return; }
        List<BlockPos> result = new ArrayList<>(96);
        for (String chunk : packed.split(";")) {
            if (result.size() == 96) break;
            try { result.add(BlockPos.of(Long.parseLong(chunk))); }
            catch (NumberFormatException ignored) { }
        }
        revealed = List.copyOf(result);
        ticksLeft = 200;
    }
    private OreScannerHighlight() { }
}
