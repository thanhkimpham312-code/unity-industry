package com.unityindustry.block;

import com.unityindustry.menu.StationMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.BlockHitResult;

/** Opens the custom 3x3 Industry menu or the 2-input Research menu. */
public final class StationBlock extends Block {
    private final StationMenu.Station station;

    public StationBlock(BlockBehaviour.Properties properties, StationMenu.Station station) {
        super(properties);
        this.station = station;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
            Player player, BlockHitResult hit) {
        if (!level.isClientSide()) player.openMenu(getMenuProvider(state, level, pos));
        return InteractionResult.SUCCESS;
    }

    @Override
    protected MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return new SimpleMenuProvider((id, inventory, player) ->
                new StationMenu(id, inventory, ContainerLevelAccess.create(level, pos), station),
                Component.translatable("block.unityindustry." + station.id()));
    }
}
