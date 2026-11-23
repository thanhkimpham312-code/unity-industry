package com.unityindustry.menu;

import com.unityindustry.registry.ModBlocks;
import com.unityindustry.registry.ModMenus;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** Real server-authoritative station menus: 3x3 industrial or 2-input research. */
public final class StationMenu extends AbstractContainerMenu {
    public enum Station {
        INDUSTRY("industry_crafting_table",9), RESEARCH("research_desk",2);
        private final String id;
        private final int count;
        Station(String id, int count) { this.id=id; this.count=count; }
        public String id() { return id; }
        public int count() { return count; }
    }
    private final Station station;
    private final ContainerLevelAccess access;
    private final Container input;
    private final ResultContainer output = new ResultContainer();
    private final Player player;
    private boolean consuming = false;

    /** Invoked on client when server opens this menu. */
    public StationMenu(int id, Inventory inventory, Station station) {
        this(id, inventory, ContainerLevelAccess.NULL, station);
    }

    /** Invoked on server by StationBlock. */
    public StationMenu(int id, Inventory inventory, ContainerLevelAccess access, Station station) {
        super(station == Station.INDUSTRY ? ModMenus.INDUSTRY : ModMenus.RESEARCH, id);
        this.station = station;
        this.access = access;
        this.player = inventory.player;
        this.input = new SimpleContainer(station.count) {
            @Override public void setChanged() {
                super.setChanged();
                if (!consuming) StationMenu.this.slotsChanged(this);
            }
        };
        if (station == Station.INDUSTRY) {
            for (int i = 0; i < 9; i++) addSlot(new Slot(input, i, 30 + (i % 3)*18, 17 + (i / 3)*18));
        } else {
            addSlot(new Slot(input, 0, 40, 35));
            addSlot(new Slot(input, 1, 74, 35));
        }
        addSlot(new Slot(output, 0, 124, 35) {
            @Override public boolean mayPlace(ItemStack stack) { return false; }
            @Override public void onTake(Player player, ItemStack result) {
                StationMenu.this.takeResult(player, result);
                super.onTake(player, result);
            }
        });
        addStandardInventorySlots(inventory, 8, 84);
    }

    public Station station() { return station; }

    private void takeResult(Player player, ItemStack result) {
        // Only server updates ingredients. All input items consumed exactly once.
        if (player.level().isClientSide()) return;
        consuming = true;
        for (int i = 0; i < station.count(); i++) {
            if (!input.getItem(i).isEmpty()) input.removeItem(i, 1);
        }
        consuming = false;
        slotsChanged(input);
    }

    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);
        if (container != input || consuming) return;
        access.execute((level, blockPos) -> {
            if (level.isClientSide()) return;
            ItemStack result = StationCrafting.result(station, input);
            output.setItem(0, result);
            broadcastChanges();
        });
    }

    @Override public boolean stillValid(Player player) {
        return stillValid(access, player, station == Station.INDUSTRY
                ? ModBlocks.INDUSTRY_CRAFTING_TABLE : ModBlocks.RESEARCH_DESK);
    }

    /** No output shift-click: prevents duplicate result exploits in the prototype. */
    @Override public ItemStack quickMoveStack(Player player, int slotIndex) {
        return ItemStack.EMPTY;
    }

    @Override public void removed(Player player) {
        super.removed(player);
        access.execute((level, pos) -> clearContainer(player, input));
    }
}
