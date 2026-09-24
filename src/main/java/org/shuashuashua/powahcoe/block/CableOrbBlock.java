package org.shuashuashua.powahcoe.block;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import org.shuashuashua.powahcoe.screen.CableOrbContainer;
import owmii.powah.block.Tier;
import owmii.powah.lib.client.handler.IHud;
import owmii.powah.lib.util.Util;

import java.util.EnumMap;
import java.util.Map;

public class CableOrbBlock extends Block implements EntityBlock, IHud, SimpleWaterloggedBlock {

    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    private static final Map<Direction, VoxelShape> SHAPES = new EnumMap<>(Direction.class);
    static {
        SHAPES.put(Direction.DOWN, Shapes.join(
                Block.box(3.5, 5.0, 3.5, 12.5, 14.23, 12.5),
                Block.box(2.5, 0, 2.5, 13.5, 1, 13.5), BooleanOp.OR));
        SHAPES.put(Direction.UP, Shapes.join(
                Block.box(3.5, 1.77, 3.5, 12.5, 11.0, 12.5),
                Block.box(2.5, 15, 2.5, 13.5, 16, 13.5), BooleanOp.OR));
        SHAPES.put(Direction.NORTH, Shapes.join(
                Block.box(3.5, 3.5, 5.0, 12.5, 12.5, 14.23),
                Block.box(2.5, 2.5, 0, 13.5, 13.5, 1), BooleanOp.OR));
        SHAPES.put(Direction.SOUTH, Shapes.join(
                Block.box(3.5, 3.5, 1.77, 12.5, 12.5, 11.0),
                Block.box(2.5, 2.5, 15, 13.5, 13.5, 16), BooleanOp.OR));
        SHAPES.put(Direction.WEST, Shapes.join(
                Block.box(5.0, 3.5, 3.5, 14.23, 12.5, 12.5),
                Block.box(0, 2.5, 2.5, 1, 13.5, 13.5), BooleanOp.OR));
        SHAPES.put(Direction.EAST, Shapes.join(
                Block.box(1.77, 3.5, 3.5, 11.0, 12.5, 12.5),
                Block.box(15, 2.5, 2.5, 16, 13.5, 13.5), BooleanOp.OR));
    }

    private final Tier variant;
    private final int multiplier;

    public CableOrbBlock(Properties properties, Tier variant) {
        this(properties, variant, 1);
    }

    public CableOrbBlock(Properties properties, Tier variant, int multiplier) {
        super(properties);
        this.variant = variant;
        this.multiplier = multiplier;
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.DOWN)
                .setValue(WATERLOGGED, false));
    }

    public Tier getVariant() {
        return variant;
    }

    public int getMultiplier() {
        return multiplier;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());
        return defaultBlockState()
                .setValue(FACING, context.getClickedFace().getOpposite())
                .setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.getOrDefault(state.getValue(FACING), Shapes.block());
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(FACING, mirror.mirror(state.getValue(FACING)));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CableOrbTile(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                    BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return (lvl, pos, st, te) -> {
            if (te instanceof CableOrbTile tile) {
                CableOrbTile.tick(lvl, pos, st, tile);
            }
        };
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moved) {
        super.onPlace(state, level, pos, oldState, moved);
        level.updateNeighborsAt(pos, this);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                  Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            BlockEntity tileentity = level.getBlockEntity(pos);
            if (tileentity instanceof CableOrbTile orb) {
                serverPlayer.openMenu(new SimpleMenuProvider(
                        (containerId, playerInv, p) ->
                                new CableOrbContainer(containerId, playerInv, orb),
                        Component.translatable(getDescriptionId())));
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock())) {
            BlockEntity tileentity = level.getBlockEntity(pos);
            if (tileentity instanceof CableOrbTile orb) {
                orb.getInventory().drop(level, pos);
            }
            super.onRemove(state, level, pos, newState, moved);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean renderHud(GuiGraphics gui, BlockState state, Level world, BlockPos pos, Player player,
                              BlockHitResult result, @Nullable BlockEntity te) {
        if (te instanceof CableOrbTile orb) {
            if (orb.getBuffer().getCapacity() > 0 && orb.containRecipe()) {
                Minecraft mc = Minecraft.getInstance();
                Font font = mc.font;
                int x = mc.getWindow().getGuiScaledWidth() / 2;
                int y = mc.getWindow().getGuiScaledHeight();
                String pct = "" + ChatFormatting.GREEN + orb.getBuffer().getPercent() + "%";
                String fe = ChatFormatting.GRAY + I18n.get("info.lollipop.fe.stored",
                        Util.addCommas(orb.getBuffer().getEnergyStored()),
                        Util.numFormat(orb.getBuffer().getCapacity()));
                gui.drawString(font, pct, Math.round(x - (font.width(pct) / 2.0f)), y - 90, 0xffffff);
                gui.drawString(font, fe, Math.round(x - (font.width(fe) / 2.0f)), y - 75, 0xffffff);
            }
        }
        return true;
    }
}
