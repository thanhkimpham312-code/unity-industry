package com.unityindustry.gameplay;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Continuous excavation: the 3x3 plane is perpendicular to actual motion.
 * All breakable blocks are supported, including ores, logs and soil. The
 * server's usual destroyBlock implementation handles drops and block events.
 * The ability never bypasses the game's can-interact and destroy-speed checks.
 */
public final class UnityExcavation {
    private static final double MIN_SPEED_SQUARED = 0.025 * 0.025;
    private record Move(Vec3 vector, long tick) { }
    private static final Map<UUID, Vec3> PREVIOUS_POS = new HashMap<>();
    private static final Map<UUID, Move> LAST_MOVE = new HashMap<>();

    /** Use actual server-authoritative position differences, not just velocity:
     *  player network movement often leaves getDeltaMovement close to zero.
     */
    public static void trackMovement(ServerPlayer player) {
        UUID id = player.getUUID();
        Vec3 current = player.position();
        Vec3 previous = PREVIOUS_POS.put(id, current);
        if (previous == null) return;
        Vec3 delta = current.subtract(previous);
        // Ignore teleports; do not tunnel after a /tp or dimension transfer.
        if (delta.lengthSqr() > 16) { LAST_MOVE.remove(id); return; }
        if (delta.lengthSqr() >= MIN_SPEED_SQUARED)
            LAST_MOVE.put(id, new Move(delta, player.level().getGameTime()));
    }

    public static void forget(UUID id) {
        PREVIOUS_POS.remove(id);
        LAST_MOVE.remove(id);
    }

    private static Direction movementDirection(Vec3 motion) {
        double x = motion.x, y = motion.y, z = motion.z;
        if (x*x + y*y + z*z < MIN_SPEED_SQUARED) return null;
        // Prefer vertical faces only when the player is primarily moving up/down.
        if (Math.abs(y) > Math.max(Math.abs(x), Math.abs(z)) * 1.3 && Math.abs(y) > 0.06)
            return y > 0 ? Direction.UP : Direction.DOWN;
        if (Math.abs(x) >= Math.abs(z)) return x > 0 ? Direction.EAST : Direction.WEST;
        return z > 0 ? Direction.SOUTH : Direction.NORTH;
    }

    public static int tunnel(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level) || player.isSpectator()) return 0;
        Move movement = LAST_MOVE.get(player.getUUID());
        if (movement == null || player.level().getGameTime() - movement.tick() > 2) return 0;
        Direction direction = movementDirection(movement.vector());
        if (direction == null) return 0; // movement-driven, never dig when idle
        // Two blocks in the travel direction avoids destroying the player's own position.
        BlockPos center = player.blockPosition().relative(direction, 2);
        if (direction.getAxis().isHorizontal()) center = center.above();
        int broken = 0;
        for (int a=-1; a<=1; a++) for (int b=-1; b<=1; b++) {
            BlockPos at = switch (direction.getAxis()) {
                case X -> center.offset(0, a, b);
                case Y -> center.offset(a, 0, b);
                case Z -> center.offset(a, b, 0);
            };
            if (UnityEnergy.get(player) < 1L) return broken;
            if (!level.hasChunkAt(at) || !level.isInWorldBounds(at) || !player.mayInteract(level, at)) continue;
            BlockState state = level.getBlockState(at);
            if (state.isAir() || !state.getFluidState().isEmpty()) continue;
            if (state.getDestroySpeed(level, at) < 0.0F) continue; // unbreakable, incl. bedrock/barrier
            // Vanilla destroyBlock applies tool requirements, break hooks, tool wear and drops.
            if (player.gameMode.destroyBlock(at)) {
                UnityEnergy.consume(player, 1L);
                broken++;
            }
        }
        return broken;
    }
    private UnityExcavation() { }
}
