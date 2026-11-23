package com.unityindustry.gameplay;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;

/**
 * Controlled 3x3 tunnel. No ore, storage, block entity or industry station is mined.
 * Exactly 1 UE is spent for each block successfully broken. No repeating cooldown.
 * Uses the actual server player game mode so Vanilla tool loot and block hooks apply.
 * Region protection must still be verified with the server's installed plugins.
 */
public final class UnityExcavation {
    private static boolean stoneOnly(BlockState state) {
        return state.is(Blocks.STONE) || state.is(Blocks.DEEPSLATE)
            || state.is(Blocks.COBBLESTONE) || state.is(Blocks.COBBLED_DEEPSLATE)
            || state.is(Blocks.ANDESITE) || state.is(Blocks.DIORITE)
            || state.is(Blocks.GRANITE) || state.is(Blocks.TUFF)
            || state.is(Blocks.CALCITE);
    }
    public static int tunnel(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)) return 0;
        ItemStack pickaxe = player.getMainHandItem();
        if (!pickaxe.is(ItemTags.PICKAXES)) return 0;
        // Avoid accidentally breaking an almost-broken tool while applying Vanilla mining.
        if (pickaxe.isDamageableItem() && pickaxe.getDamageValue() >= pickaxe.getMaxDamage() - 2) return 0;
        Direction dir = player.getDirection();
        if (!dir.getAxis().isHorizontal()) return 0;
        // Movement must be toward the face we plan to excavate.
        double forwardMotion = player.getDeltaMovement().x * dir.getStepX()
                             + player.getDeltaMovement().z * dir.getStepZ();
        if (forwardMotion < 0.015) return 0;
        BlockPos origin = player.blockPosition().relative(dir, 2).above();
        int broken = 0;
        for (int y = -1; y <= 1; y++) for (int horizontal = -1; horizontal <= 1; horizontal++) {
            BlockPos at = dir.getAxis() == Direction.Axis.X
                ? origin.offset(0, y, horizontal) : origin.offset(horizontal, y, 0);
            BlockState state = level.getBlockState(at);
            if (!stoneOnly(state) || level.getBlockEntity(at) != null) continue;
            if (!player.mayInteract(level, at) || UnityEnergy.get(player) < 1) continue;
            int damageBefore = pickaxe.getDamageValue();
            if (player.gameMode.destroyBlock(at)) {
                UnityEnergy.consume(player, 1);
                broken++;
                if (!pickaxe.isEmpty() && pickaxe.isDamageableItem())
                    pickaxe.setDamageValue(damageBefore); // Unity Energy replaces tool wear
            }
        }
        return broken;
    }
    private UnityExcavation() { }
}
