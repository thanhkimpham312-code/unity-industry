package com.unityindustry.gameplay;

import com.unityindustry.rules.CoreType;
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

/**
 * Agriculture Core: after harvesting an adult vanilla crop, consumes one seed
 * (or carrot/potato) and 2 UE and replants only on unchanged farmland. The
 * normal loot and Fortune calculation are allowed to finish first.
 */
public final class UnityHarvest {
    public static void initialize() {
        PlayerBlockBreakEvents.AFTER.register((level, player, pos, oldState, blockEntity) -> {
            if (!(level instanceof ServerLevel world) || !(player instanceof ServerPlayer serverPlayer)) return;
            if (!ChestplateCores.get(serverPlayer.getItemBySlot(EquipmentSlot.CHEST))
                .contains(CoreType.AGRICULTURE)) return;
            if (!(oldState.getBlock() instanceof CropBlock crop) || !crop.isMaxAge(oldState)) return;
            Item seed = seedFor(oldState.getBlock());
            if (seed == null || UnityEnergy.get(serverPlayer) < 2L) return;
            if (!world.getBlockState(pos).isAir() || !world.getBlockState(pos.below()).is(Blocks.FARMLAND)) return;
            if (!serverPlayer.mayInteract(world, pos)) return;
            int slot = findSeed(serverPlayer, seed);
            if (slot < 0 || !UnityEnergy.consume(serverPlayer, 2L)) return;
            ItemStack stack = serverPlayer.getInventory().getItem(slot);
            stack.shrink(1);
            world.setBlock(pos, oldState.getBlock().defaultBlockState(), 3);
        });
    }
    private static Item seedFor(Block block) {
        if (block == Blocks.WHEAT) return Items.WHEAT_SEEDS;
        if (block == Blocks.CARROTS) return Items.CARROT;
        if (block == Blocks.POTATOES) return Items.POTATO;
        if (block == Blocks.BEETROOTS) return Items.BEETROOT_SEEDS;
        return null;
    }
    private static int findSeed(ServerPlayer player, Item item) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (player.getInventory().getItem(i).is(item)) return i;
        }
        return -1;
    }
    private UnityHarvest() { }
}
