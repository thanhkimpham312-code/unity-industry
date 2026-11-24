package com.unityindustry.gameplay;

import com.unityindustry.rules.CoreType;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

/**
 * Converts RAW metal drops near a freshly mined matching ore into ingots.
 * Uses energy only, no coal/fuel. Silk Touch produces the ore block rather than raw
 * metal and therefore isn't converted. Fortune is applied by Vanilla before this.
 * 2 UE / ingot is a provisional cost pending final tuning.
 */
public final class UnityRefinery {
    private record Pending(ServerLevel level, UUID player, BlockPos pos,
                           Item raw, Item ingot, long dueTick) { }
    private static final List<Pending> QUEUE = new ArrayList<>();

    public static void initialize() {
        PlayerBlockBreakEvents.AFTER.register((level, user, pos, oldState, blockEntity) -> {
            if (!(level instanceof ServerLevel world) || !(user instanceof ServerPlayer player)) return;
            if (!ChestplateCores.get(player.getItemBySlot(EquipmentSlot.CHEST)).contains(CoreType.REFINERY)) return;
            Item raw = rawForOre(oldState.getBlock());
            Item ingot = ingotFor(raw);
            if (raw != null && ingot != null)
                QUEUE.add(new Pending(world, player.getUUID(), pos.immutable(), raw, ingot,
                    world.getGameTime() + 2));
        });
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            var iter = QUEUE.iterator();
            while (iter.hasNext()) {
                Pending data = iter.next();
                if (data.level().getGameTime() < data.dueTick()) continue;
                iter.remove();
                ServerPlayer player = server.getPlayerList().getPlayer(data.player());
                if (player == null || player.level() != data.level() ||
                    !ChestplateCores.get(player.getItemBySlot(EquipmentSlot.CHEST)).contains(CoreType.REFINERY)) continue;
                AABB near = new AABB(data.pos()).inflate(1.6);
                // Converts an existing drop, never creates loot from thin air.
                for (ItemEntity entity : data.level().getEntitiesOfClass(ItemEntity.class, near,
                        e -> e.getItem().is(data.raw()))) {
                    ItemStack stack = entity.getItem();
                    int count = stack.getCount();
                    long cost = 2L * count;
                    if (cost <= 0 || !UnityEnergy.consume(player, cost)) continue;
                    entity.setItem(new ItemStack(data.ingot(), count));
                }
            }
        });
    }
    /** Refine drops vacuumed by Excavation before they reach the inventory.
     *  Vanilla block-break listener handles ordinary hand-mining separately.
     */
    public static ItemStack refineExcavated(ServerPlayer player,
                                           net.minecraft.world.level.block.state.BlockState state,
                                           ItemStack drop) {
        if (drop.isEmpty() || !ChestplateCores.get(player.getItemBySlot(EquipmentSlot.CHEST))
                .contains(CoreType.REFINERY)) return drop;
        Item raw = rawForOre(state.getBlock());
        Item ingot = ingotFor(raw);
        if (raw == null || ingot == null || !drop.is(raw)) return drop;
        long cost = 2L * drop.getCount();
        if (!UnityEnergy.consume(player, cost)) return drop;
        return new ItemStack(ingot, drop.getCount());
    }

    private static Item rawForOre(Block block) {
        if (block == Blocks.IRON_ORE || block == Blocks.DEEPSLATE_IRON_ORE) return Items.RAW_IRON;
        if (block == Blocks.GOLD_ORE || block == Blocks.DEEPSLATE_GOLD_ORE) return Items.RAW_GOLD;
        if (block == Blocks.COPPER_ORE || block == Blocks.DEEPSLATE_COPPER_ORE) return Items.RAW_COPPER;
        return null;
    }
    private static Item ingotFor(Item raw) {
        if (raw == Items.RAW_IRON) return Items.IRON_INGOT;
        if (raw == Items.RAW_GOLD) return Items.GOLD_INGOT;
        if (raw == Items.RAW_COPPER) return Items.COPPER_INGOT;
        return null;
    }
    private UnityRefinery() { }
}
