package com.unityindustry.menu;

import com.unityindustry.gameplay.ChestplateCores;
import com.unityindustry.gameplay.UnityProgress;
import net.minecraft.server.level.ServerPlayer;
import com.unityindustry.registry.ModItems;
import net.minecraft.network.chat.Component;
import com.unityindustry.registry.ModBlocks;
import com.unityindustry.registry.ModMenus;
import com.unityindustry.rules.CoreRules;
import com.unityindustry.rules.CoreType;
import java.util.List;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Implant:  chestplate + 1 Core -> modified chestplate. Inputs are consumed once.
 * Reverse:  chestplate in first input -> selected removed Core in output; chestplate
 *           stays in input with all remaining Cores and original damage/enchantments.
 * Selected Core is changed by the screen's arrow button (id=0). No duplicate item
 * is required as a selector.
 */
public final class ImplantMenu extends AbstractContainerMenu {
    public enum Mode { IMPLANT, REVERSE }
    private final Mode mode;
    private final ContainerLevelAccess access;
    private final Container input;
    private final ResultContainer output = new ResultContainer();
    private final ContainerData selection = new SimpleContainerData(1);
    private boolean changing = false;
    private final Player viewer;

    public ImplantMenu(int id, Inventory playerInventory, Mode mode) {
        this(id, playerInventory, ContainerLevelAccess.NULL, mode);
    }

    public ImplantMenu(int id, Inventory playerInventory, ContainerLevelAccess access, Mode mode) {
        super(mode == Mode.IMPLANT ? ModMenus.IMPLANT : ModMenus.REVERSE, id);
        this.mode = mode;
        this.access = access;
        this.viewer = playerInventory.player;
        this.input = new SimpleContainer(2) {
            @Override public void setChanged() {
                super.setChanged();
                if (!changing) ImplantMenu.this.slotsChanged(this);
            }
        };
        addSlot(new Slot(input, 0, 38, 35) {
            @Override public boolean mayPlace(ItemStack stack) { return ChestplateCores.validArmor(stack); }
            @Override public int getMaxStackSize() { return 1; }
        });
        addSlot(new Slot(input, 1, 75, 35) {
            @Override public boolean mayPlace(ItemStack stack) {
                return mode == Mode.IMPLANT && (ChestplateCores.typeOf(stack) != null || stack.is(ModItems.PRIME_CORE));
            }
            @Override public int getMaxStackSize() { return 1; }
        });
        addSlot(new Slot(output, 0, 124, 35) {
            @Override public boolean mayPlace(ItemStack stack) { return false; }
            @Override public void onTake(Player player, ItemStack result) {
                if (!player.level().isClientSide()) processTake();
                super.onTake(player, result);
            }
        });
        addStandardInventorySlots(playerInventory, 8, 84);
        addDataSlots(selection);
    }

    public Mode mode() { return mode; }
    public int selectedIndex() { return selection.get(0); }

    @Override
    public boolean clickMenuButton(Player player, int button) {
        if (mode != Mode.REVERSE || button != 0) return false;
        List<CoreType> cores = ChestplateCores.get(input.getItem(0));
        if (cores.isEmpty()) return false;
        selection.set(0, (selection.get(0) + 1) % cores.size());
        slotsChanged(input);
        broadcastChanges();
        return true;
    }

    private void processTake() {
        changing = true;
        ItemStack armor = input.getItem(0);
        if (mode == Mode.IMPLANT) {
            // Check again server-side before consuming; invalid client output cannot destroy inputs.
            CoreType next = ChestplateCores.typeOf(input.getItem(1));
            if (next != null && CoreRules.implant(ChestplateCores.get(armor), next).success()) {
                List<CoreType> installed = CoreRules.implant(ChestplateCores.get(armor), next).cores();
                if (viewer instanceof ServerPlayer sp) {
                    UnityProgress.award(sp, "first_implant");
                    if (installed.size() == 3) UnityProgress.award(sp, "three_cores");
                    if (next == CoreType.ULTRA) UnityProgress.award(sp, "ultra_implant");
                }
                input.removeItem(0, 1);
                input.removeItem(1, 1);
            }
        } else if (ChestplateCores.validArmor(armor)) {
            List<CoreType> cores = ChestplateCores.get(armor);
            int selected = selection.get(0);
            if (selected >= 0 && selected < cores.size()) {
                ItemStack changed = ChestplateCores.with(armor, CoreRules.removeOne(cores, cores.get(selected)));
                input.setItem(0, changed);
                if (viewer instanceof ServerPlayer sp) UnityProgress.award(sp, "reverse_implant");
                selection.set(0, 0);
            }
        }
        changing = false;
        slotsChanged(input);
    }

    @Override public void slotsChanged(Container container) {
        super.slotsChanged(container);
        if (container != input || changing) return;
        access.execute((level, pos) -> {
            if (level.isClientSide()) return;
            ItemStack armor = input.getItem(0);
            ItemStack result = ItemStack.EMPTY;
            if (ChestplateCores.validArmor(armor)) {
                List<CoreType> cores = ChestplateCores.get(armor);
                if (mode == Mode.IMPLANT) {
                    CoreType next = ChestplateCores.typeOf(input.getItem(1));
                    if (next != null) {
                        CoreRules.ImplantResult proposal = CoreRules.implant(cores, next);
                        if (proposal.success()) result = ChestplateCores.with(armor, proposal.cores());
                        else if (!input.getItem(1).isEmpty()) {
                            String message = switch (proposal.status()) {
                                case OVERLOAD -> "Quá tải!";
                                case DUPLICATE -> "Core trùng loại!";
                                case ULTRA_REQUIRES_EMPTY -> "Không đủ dung lượng! Ultra Core cần 3 ô trống";
                                case ULTRA_OCCUPIED -> "Ultra Core đã chiếm 3 ô!";
                                case OK -> "";
                            };
                            if (!message.isEmpty()) viewer.displayClientMessage(Component.literal(message), true);
                        }
                    } else if (input.getItem(1).is(ModItems.PRIME_CORE)) {
                        viewer.displayClientMessage(Component.literal("Prime Core chưa được nghiên cứu!"), true);
                    }
                } else if (!cores.isEmpty()) {
                    int i = Math.min(selection.get(0), cores.size() - 1);
                    result = ChestplateCores.itemFor(cores.get(i));
                }
            }
            output.setItem(0, result);
            broadcastChanges();
        });
    }

    @Override public boolean stillValid(Player player) {
        return stillValid(access, player, mode == Mode.IMPLANT
            ? ModBlocks.IMPLANT_TABLE : ModBlocks.REVERSE_IMPLANT_TABLE);
    }

    @Override public ItemStack quickMoveStack(Player player, int index) {
        // Disallow shift-clicking result until exhaustive transaction checks pass.
        return ItemStack.EMPTY;
    }

    @Override public void removed(Player player) {
        super.removed(player);
        access.execute((level, pos) -> clearContainer(player, input));
    }
}
