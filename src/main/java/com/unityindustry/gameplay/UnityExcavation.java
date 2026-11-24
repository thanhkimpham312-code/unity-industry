package com.unityindustry.gameplay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Movement-driven 3x3 excavation; vanilla game mode owns the actual break.
 * Drops are vacuumed into the miner's inventory, with full-inventory overflow
 * returned next to the player. The vanilla break is never performed twice.
 */
public final class UnityExcavation {
    private static final double MIN_SPEED_SQUARED = 0.025 * 0.025;
    private record Move(Vec3 vector, long tick) { }
    private record Pending(ServerLevel level, UUID miner, BlockPos origin,
                           BlockState oldState, Set<UUID> alreadyThere, long expires) { }
    private static final Map<UUID, Vec3> PREVIOUS_POS = new HashMap<>();
    private static final Map<UUID, Move> LAST_MOVE = new HashMap<>();
    private static final List<Pending> PENDING = new ArrayList<>();

    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            // Vanilla / other mod drops may enter the world slightly after destroyBlock.
            var iter = PENDING.iterator();
            while (iter.hasNext()) {
                Pending pending = iter.next();
                ServerPlayer miner = server.getPlayerList().getPlayer(pending.miner());
                if (miner == null || miner.level() != pending.level()) {
                    iter.remove();
                    continue;
                }
                vacuum(miner, pending.level(), pending.origin(), pending.oldState(),
                    pending.alreadyThere());
                if (pending.level().getGameTime() >= pending.expires()) iter.remove();
            }
        });
    }

    public static void trackMovement(ServerPlayer player) {
        UUID id = player.getUUID();
        Vec3 current = player.position();
        Vec3 previous = PREVIOUS_POS.put(id, current);
        if (previous == null) return;
        Vec3 delta = current.subtract(previous);
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
        if (Math.abs(y) > Math.max(Math.abs(x), Math.abs(z)) * 1.3 && Math.abs(y) > 0.06)
            return y > 0 ? Direction.UP : Direction.DOWN;
        if (Math.abs(x) >= Math.abs(z)) return x > 0 ? Direction.EAST : Direction.WEST;
        return z > 0 ? Direction.SOUTH : Direction.NORTH;
    }

    public static int tunnel(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level) || player.isSpectator()) return 0;
        Move movement = LAST_MOVE.get(player.getUUID());
        if (movement == null || level.getGameTime() - movement.tick() > 2) return 0;
        Direction direction = movementDirection(movement.vector());
        if (direction == null) return 0;
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
            if (!level.hasChunkAt(at) || !level.isInWorldBounds(at)
                    || !player.mayInteract(level, at)) continue;
            BlockState state = level.getBlockState(at);
            if (state.isAir() || !state.getFluidState().isEmpty()
                    || state.getDestroySpeed(level, at) < 0.0F) continue;

            AABB around = new AABB(at).inflate(0.65);
            Set<UUID> seen = new HashSet<>();
            for (ItemEntity e : level.getEntitiesOfClass(ItemEntity.class, around))
                seen.add(e.getUUID());

            // With an invalid held tool, vanilla will break ores without any drop.
            // For those blocks only, precompute the loot with a virtual proper pick.
            // Skip block entities: never duplicate a chest's stored inventory.
            List<ItemStack> properToolLoot = List.of();
            ItemStack held = player.getMainHandItem();
            boolean needsVirtualDrops = player.isCreative()
                || (state.requiresCorrectToolForDrops() && !held.isCorrectToolForDrops(state));
            if (needsVirtualDrops && !state.hasBlockEntity()) {
                BlockEntity blockEntity = level.getBlockEntity(at);
                ItemStack effectiveTool = held.isCorrectToolForDrops(state)
                    ? held : new ItemStack(Items.NETHERITE_PICKAXE);
                properToolLoot = Block.getDrops(state, level, at, blockEntity, player, effectiveTool);
            }

            // Important: only destroy after the permissions and energy checks.
            // This triggers vanilla block-break events and applies any real tool wear.
            if (!player.gameMode.destroyBlock(at)) continue;
            UnityEnergy.consume(player, 1L);
            broken++;

            // Preserve vanilla/fortune/silk/modded drops, but never collect items
            // that were on the ground before the ability broke this block.
            vacuum(player, level, at, state, seen);
            PENDING.add(new Pending(level, player.getUUID(), at.immutable(), state, seen,
                level.getGameTime() + 2));
            for (ItemStack stack : properToolLoot) give(player,
                UnityRefinery.refineExcavated(player, state, stack));
        }
        return broken;
    }

    private static void vacuum(ServerPlayer player, ServerLevel level, BlockPos pos,
                               BlockState oldState, Set<UUID> existing) {
        AABB around = new AABB(pos).inflate(0.65);
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, around,
                e -> e.isAlive() && !existing.contains(e.getUUID()))) {
            // Discard before insertion to avoid re-entrant pickup/duplication.
            ItemStack loot = item.getItem().copy();
            item.discard();
            give(player, UnityRefinery.refineExcavated(player, oldState, loot));
        }
    }

    private static void give(ServerPlayer player, ItemStack stack) {
        if (stack.isEmpty()) return;
        // Vanilla method inserts what fits, dropping overflow by the miner.
        player.getInventory().placeItemBackInInventory(stack);
        player.getInventory().setChanged();
    }

    private UnityExcavation() { }
}
