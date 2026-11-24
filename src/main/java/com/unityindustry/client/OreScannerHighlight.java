package com.unityindustry.client;

import com.unityindustry.network.OreScanPayload;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;

/** Client-side luminous line outlines on the server-revealed nearby ores.
 * No fake ore blocks, world edits or persistent entities are generated.
 * X-ray visibility behind opaque terrain depends on the active renderer;
 * depth-independent pipeline is deferred until client in-game verification.
 */
public final class OreScannerHighlight {
    private static List<BlockPos> revealed = List.of();
    private static int ticksLeft;

    public static void initialize() {
        ClientPlayNetworking.registerGlobalReceiver(OreScanPayload.ID, (payload, context) ->
            context.client().execute(() -> receive(payload.packedPositions())));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.level == null || client.player == null) { revealed = List.of(); ticksLeft = 0; }
            else if (ticksLeft > 0 && --ticksLeft == 0) revealed = List.of();
        });
        WorldRenderEvents.BEFORE_DEBUG_RENDER.register(ctx -> {
            if (revealed.isEmpty() || ticksLeft <= 0) return;
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) return;
            Vec3 camera = mc.gameRenderer.getMainCamera().getPosition();
            var matrix = ctx.matrices();
            var lineBuffer = ctx.consumers().getBuffer(RenderTypes.linesTranslucent());
            // Bright cyan full-block contours. Rendered in camera-relative coordinates.
            for (BlockPos block : revealed) {
                ShapeRenderer.renderShape(matrix, lineBuffer, Shapes.block(),
                    block.getX() - camera.x, block.getY() - camera.y, block.getZ() - camera.z,
                    0xFF39F6FA, 2.5F);
            }
        });
    }

    private static void receive(String packed) {
        if (packed.isEmpty()) { revealed = List.of(); ticksLeft = 0; return; }
        List<BlockPos> coordinates = new ArrayList<>(96);
        for (String chunk : packed.split(";")) {
            if (coordinates.size() == 96) break;
            try { coordinates.add(BlockPos.of(Long.parseLong(chunk))); }
            catch (NumberFormatException ignored) { }
        }
        revealed = List.copyOf(coordinates);
        ticksLeft = 200; // ten seconds; server charges UE every ten ticks
    }
    private OreScannerHighlight() { }
}
