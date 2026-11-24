package com.unityindustry.gameplay;

import com.unityindustry.rules.CoreType;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.core.component.DataComponents;

/** Nine server-authoritative slots persisted on the chestplate ItemStack while
 * implanted, and moved to the Storage Core item on reverse implantation.
 * Built-in vanilla 9x1 chest screen; no bespoke GUI or extra packet needed.
 */
public final class UnityStorage {
    public static void open(ServerPlayer player) {
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!ChestplateCores.validArmor(chest)
                || !ChestplateCores.get(chest).contains(CoreType.STORAGE)) return;
        StorageInventory contents = new StorageInventory(player, chest);
        player.openMenu(new SimpleMenuProvider((id, inv, owner) ->
                new ChestMenu(MenuType.GENERIC_9x1, id, inv, contents, 1) {
                    @Override public boolean stillValid(Player viewer) {
                        return contents.stillValid(viewer);
                    }
                    @Override public void removed(Player viewer) {
                        contents.setChanged(); // save any last cursor/shift-click mutations
                        super.removed(viewer);
                    }
                }, Component.literal("Unity Storage Core · 9 slots")));
    }

    private static final class StorageInventory extends SimpleContainer {
        private final ServerPlayer owner;
        private final ItemStack chest;
        private boolean loading = true;

        StorageInventory(ServerPlayer owner, ItemStack chest) {
            super(9);
            this.owner = owner;
            this.chest = chest;
            NonNullList<ItemStack> loaded = NonNullList.withSize(9, ItemStack.EMPTY);
            chest.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(loaded);
            for (int i=0; i<9; i++) super.setItem(i, loaded.get(i));
            loading = false;
        }

        @Override public boolean stillValid(Player player) {
            return player == owner && owner.isAlive()
                && owner.getItemBySlot(EquipmentSlot.CHEST) == chest
                && ChestplateCores.get(chest).contains(CoreType.STORAGE);
        }

        @Override public boolean canPlaceItem(int slot, ItemStack item) {
            return slot >= 0 && slot < 9 && !item.has(DataComponents.CONTAINER)
                && !item.has(DataComponents.BUNDLE_CONTENTS)
                && !item.is(Items.BUNDLE)
                && !(item.getItem() instanceof BlockItem blockItem
                    && blockItem.getBlock() instanceof ShulkerBoxBlock)
                && !item.is(com.unityindustry.registry.ModItems.STORAGE_CORE)
                && !ChestplateCores.validArmor(item);
        }

        @Override public void setChanged() {
            super.setChanged();
            if (loading || !stillValid(owner)) return;
            java.util.List<ItemStack> stacks = new java.util.ArrayList<>(9);
            for (int i=0;i<9;i++) stacks.add(getItem(i).copy());
            chest.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(stacks));
            owner.getInventory().setChanged();
        }
    }
    private UnityStorage() { }
}
