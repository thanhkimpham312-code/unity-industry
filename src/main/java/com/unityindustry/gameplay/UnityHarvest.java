package com.unityindustry.gameplay;

import com.unityindustry.rules.CoreType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;

/** Automatically harvests adult wheat/carrot/potato/beetroot and replants them.
 * Manual crop breaking also gets an automatic replant. Each crop takes one seed
 * from inventory plus exactly two UE, without duplicating loot or bypassing
 * valid farmland and interaction checks.
 */
public final class UnityHarvest {
    private static final int RADIUS = 3;
    private static final int MAX_PER_PASS = 3;

    public static void initialize() {
        PlayerBlockBreakEvents.AFTER.register((level, player, pos, oldState, blockEntity) -> {
            if (level instanceof ServerLevel world && player instanceof ServerPlayer sp
                    && hasCore(sp) && mature(oldState) && world.getBlockState(pos).isAir()) {
                replant(sp, world, pos, oldState); // normal mining already produced drops
            }
        });
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (server.getTickCount() % 10 != 0) return;
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                if (!hasCore(player) || !(player.level() instanceof ServerLevel level)) continue;
                int harvested = 0;
                BlockPos center = player.blockPosition();
                for (int y = -1; y <= 1 && harvested < MAX_PER_PASS; y++) {
                    for (int dx = -RADIUS; dx <= RADIUS && harvested < MAX_PER_PASS; dx++) {
                        for (int dz = -RADIUS; dz <= RADIUS && harvested < MAX_PER_PASS; dz++) {
                            BlockPos pos = center.offset(dx, y, dz);
                            if (!level.hasChunkAt(pos) || !player.mayInteract(level, pos)) continue;
                            BlockState oldState = level.getBlockState(pos);
                            if (!mature(oldState) || !canReplant(player,level,pos,oldState)) continue;
                            if (UnityEnergy.get(player) < 2L) break;
                            // Drops calculated from the original crop exactly once.
                            Block.dropResources(oldState, level, pos, null, player, player.getMainHandItem());
                            if (replant(player,level,pos,oldState)) harvested++;
                        }
                    }
                }
            }
        });
    }

    private static boolean hasCore(ServerPlayer p) {
        return ChestplateCores.get(p.getItemBySlot(EquipmentSlot.CHEST)).contains(CoreType.AGRICULTURE);
    }
    private static boolean mature(BlockState state) {
        return state.getBlock() instanceof CropBlock crop && crop.isMaxAge(state) && seedFor(state.getBlock()) != null;
    }
    private static boolean canReplant(ServerPlayer p,ServerLevel world,BlockPos pos,BlockState state) {
        Item seed = seedFor(state.getBlock());
        return seed != null && world.getBlockState(pos.below()).is(Blocks.FARMLAND)
            && p.mayInteract(world,pos) && findSeed(p,seed) >= 0;
    }
    private static boolean replant(ServerPlayer p,ServerLevel world,BlockPos pos,BlockState state) {
        if (!canReplant(p,world,pos,state) || UnityEnergy.get(p) < 2L) return false;
        int slot = findSeed(p,seedFor(state.getBlock()));
        if (slot < 0 || !UnityEnergy.consume(p,2L)) return false;
        ItemStack seeds = p.getInventory().getItem(slot);
        seeds.shrink(1);
        p.getInventory().setChanged();
        world.setBlock(pos,state.getBlock().defaultBlockState(),3);
        return true;
    }
    private static Item seedFor(Block block) {
        if (block == Blocks.WHEAT) return Items.WHEAT_SEEDS;
        if (block == Blocks.CARROTS) return Items.CARROT;
        if (block == Blocks.POTATOES) return Items.POTATO;
        if (block == Blocks.BEETROOTS) return Items.BEETROOT_SEEDS;
        return null;
    }
    private static int findSeed(ServerPlayer player, Item seed) {
        // Use the player's 36 inventory slots only; never consume worn armor slots.
        for (int i=0; i<36; i++) if (player.getInventory().getItem(i).is(seed)) return i;
        return -1;
    }
    private UnityHarvest() { }
}
