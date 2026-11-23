package com.unityindustry.block;

import com.unityindustry.menu.StationMenu;
import com.unityindustry.menu.ImplantMenu;
import com.unityindustry.menu.ImplantMenu;
import com.unityindustry.menu.ImplantMenu;
import com.unityindustry.menu.ImplantMenu;
import com.unityindustry.menu.ImplantMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Two-block-wide workstation. One item places two horizontal halves; each half
 * renders a different texture. Breaking either half clears the other half.
 * Research opens the server-backed Research menu. Implant GUI is not yet coded.
 */
public final class WideStationBlock extends Block {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<BedPart> PART = BlockStateProperties.BED_PART;
    private final StationMenu.Station station;
    private final ImplantMenu.Mode mode;
    private final ImplantMenu.Mode mode;
    private final ImplantMenu.Mode mode;
    private final ImplantMenu.Mode mode;
    private final ImplantMenu.Mode mode;

    public WideStationBlock(BlockBehaviour.Properties properties, StationMenu.Station station, ImplantMenu.Mode mode) {
        super(properties);
        this.station = station;
        this.mode = mode;
        this.mode = mode;
        this.mode = mode;
        this.mode = mode;
        this.mode = mode;
        registerDefaultState(stateDefinition.any()
            .setValue(FACING, Direction.NORTH)
            .setValue(PART, BedPart.FOOT));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART);
    }

    private static BlockPos sibling(BlockPos pos, BlockState state) {
        Direction right = state.getValue(FACING).getClockWise();
        return pos.relative(state.getValue(PART) == BedPart.FOOT ? right : right.getOpposite());
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction face = context.getHorizontalDirection().getOpposite();
        BlockPos second = context.getClickedPos().relative(face.getClockWise());
        if (!context.getLevel().getBlockState(second).canBeReplaced(context)) return null;
        return defaultBlockState().setValue(FACING, face).setValue(PART, BedPart.FOOT);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide()) {
            BlockPos second = sibling(pos, state);
            level.setBlock(second, state.setValue(PART, BedPart.HEAD), 3);
        }
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide()) {
            BlockPos other = sibling(pos, state);
            BlockState otherState = level.getBlockState(other);
            if (otherState.is(this)
                && otherState.getValue(PART) != state.getValue(PART)
                && otherState.getValue(FACING) == state.getValue(FACING)) {
                // Remove counterpart without generating a second item drop.
                level.setBlock(other, Blocks.AIR.defaultBlockState(), 35);
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                                 Player player, BlockHitResult hit) {
        if (station == null && mode == null) return InteractionResult.PASS;
        if (!level.isClientSide()) player.openMenu(getMenuProvider(state, level, pos));
        return InteractionResult.SUCCESS;
    }

    @Override
    protected MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        String name = station != null ? station.id() : (mode == ImplantMenu.Mode.IMPLANT ? "implant_table" : "reverse_implant_table");
        return new SimpleMenuProvider((id, inventory, player) ->
            station != null
                ? new StationMenu(id, inventory, ContainerLevelAccess.create(level, pos), station)
                : new ImplantMenu(id, inventory, ContainerLevelAccess.create(level, pos), mode),
            Component.translatable("block.unityindustry." + name));
    }
}
