package org.PowahCOE.coe.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
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
import net.minecraft.world.level.block.SoundType;
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

import java.util.EnumMap;
import java.util.Map;

import com.lowdragmc.lowdraglib2.gui.factory.BlockUIMenuType;
import com.lowdragmc.lowdraglib2.gui.factory.BlockUIMenuType.BlockUIHolder;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Toggle;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.inventory.InventorySlots;
import com.lowdragmc.lowdraglib2.gui.ui.style.StylesheetManager;
import com.lowdragmc.lowdraglib2.gui.texture.ColorBorderTexture;
import com.lowdragmc.lowdraglib2.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib2.gui.texture.GuiTextureGroup;
import dev.vfyjxf.taffy.style.FlexDirection;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import owmii.powah.block.Tier;
import owmii.powah.lib.client.handler.IHud;
import owmii.powah.util.Util;

public class CableOrbBlock extends Block implements EntityBlock, IHud, SimpleWaterloggedBlock, BlockUIMenuType.BlockUI {

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
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.getOrDefault(state.getValue(FACING), Shapes.block());
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
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
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moved) {
        super.onPlace(state, level, pos, oldState, moved);
        level.updateNeighborsAt(pos, this);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack held, BlockState state, Level level, BlockPos pos,
                                               Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            BlockUIMenuType.openUI(serverPlayer, pos);
        }
        return ItemInteractionResult.SUCCESS;
    }

    // BlockUI implementation

    @Override
    public ModularUI createUI(BlockUIHolder holder) {
        CableOrbTile tile = getOrbTile(holder);
        if (tile == null) return null;

        var inventory = asModifiable(tile.getInventory());

        var root = new UIElement();
        root.layout(layout -> {
            layout.width(176f).height(245f);
            layout.marginTop(-20f);
            layout.paddingTop(7f).paddingBottom(7f).paddingLeft(8f).paddingRight(8f);
            layout.gapAll(4f);
        });
        root.style(style -> style.backgroundTexture(new ColorRectTexture(0xFFC6C6C6)));

        // Title
        root.addChild(new Label()
                .setText(Component.translatable(getDescriptionId()))
                .textStyle(s -> s.textColor(0xFF_EAEAEA)));

        // Main content row: input slots | output slots + upgrade
        var content = new UIElement();
        content.layout(layout -> {
            layout.flexDirection(FlexDirection.ROW);
            layout.gapAll(16f);
        });

        // Input slots panel (3x2 grid)
        var inputPanel = new UIElement();
        inputPanel.layout(l -> { l.flexDirection(FlexDirection.COLUMN); l.gapAll(2f); l.paddingAll(4f); });
        inputPanel.style(s -> s.backgroundTexture(GuiTextureGroup.of(
                        new ColorRectTexture(0xFF8B8B8B),
                        new ColorBorderTexture(1, 0xFF373737))));
        inputPanel.addChild(new Label()
                .setText(Component.translatable("gui.powahcoe.input"))
                .textStyle(s -> s.fontSize(10f).textColor(0xFF_B5B5B5)));
        for (int row = 0; row < 3; row++) {
            var rowEl = new UIElement();
            rowEl.layout(l -> { l.flexDirection(FlexDirection.ROW); l.gapAll(2f); });
            for (int col = 0; col < 3; col++) {
                int slotIdx = CableOrbTile.INPUT_START + row * 3 + col;
                rowEl.addChild(new ItemSlot().bind(inventory,slotIdx));
            }
            inputPanel.addChild(rowEl);
        }
        content.addChild(inputPanel);

        // Output slots panel (3x2 grid)
        var outputPanel = new UIElement();
        outputPanel.layout(l -> { l.flexDirection(FlexDirection.COLUMN); l.gapAll(2f); l.paddingAll(4f); });
        outputPanel.style(s -> s.backgroundTexture(GuiTextureGroup.of(
                        new ColorRectTexture(0xFF8B8B8B),
                        new ColorBorderTexture(1, 0xFF373737))));
        outputPanel.addChild(new Label()
                .setText(Component.translatable("gui.powahcoe.output"))
                .textStyle(s -> s.fontSize(10f).textColor(0xFF_B5B5B5)));
        for (int row = 0; row < 3; row++) {
            var rowEl = new UIElement();
            rowEl.layout(l -> { l.flexDirection(FlexDirection.ROW); l.gapAll(2f); });
            for (int col = 0; col < 3; col++) {
                int slotIdx = CableOrbTile.OUTPUT_START + row * 3 + col;
                var itemSlot = new ItemSlot().bind(inventory,slotIdx);
                rowEl.addChild(itemSlot);
            }
            outputPanel.addChild(rowEl);
        }

        // Upgrade slot (below output panel)
        var upgradePanel = new UIElement();
        upgradePanel.layout(l -> { l.flexDirection(FlexDirection.COLUMN); l.gapAll(2f); l.paddingAll(4f); });
        upgradePanel.style(s -> s.backgroundTexture(GuiTextureGroup.of(
                        new ColorRectTexture(0xFF8B8B8B),
                        new ColorBorderTexture(1, 0xFF373737))));
        upgradePanel.addChild(new Label()
                .setText(Component.translatable("slot.powahcoe.upgrade"))
                .textStyle(s -> s.fontSize(10f).textColor(0xFF_B5B5B5)));
        upgradePanel.addChild(new ItemSlot().bind(inventory,CableOrbTile.UPGRADE_SLOT));

        var rightColumn = new UIElement();
        rightColumn.layout(l -> { l.flexDirection(FlexDirection.COLUMN); l.gapAll(4f); });
        rightColumn.addChild(outputPanel);
        rightColumn.addChild(upgradePanel);
        content.addChild(rightColumn);

        root.addChild(content);

        // Auto-eject toggle
        var autoEjectToggle = new Toggle()
                .setText(Component.translatable("button.powahcoe.auto_eject"))
                .setOn(tile != null && tile.isAutoEject())
                .toggleButton(btn -> btn.setOnServerClick(e -> {
                    if (tile != null) {
                        tile.setAutoEject(!tile.isAutoEject());
                    }
                }));
        root.addChild(autoEjectToggle);

        // Player inventory
        root.addChild(new InventorySlots());

        var ui = UI.of(root, StylesheetManager.INSTANCE.getStylesheetSafe(StylesheetManager.MC));
        return new ModularUI(ui, holder.player);
    }

    @SuppressWarnings("deprecation")
    private static net.neoforged.neoforge.items.IItemHandlerModifiable asModifiable(owmii.powah.lib.logistics.inventory.Inventory inv) {
        return new net.neoforged.neoforge.items.IItemHandlerModifiable() {
            @Override public void setStackInSlot(int slot, ItemStack stack) { inv.setStackInSlot(slot, stack); }
            @Override public int getSlots() { return inv.getSlots(); }
            @Override public ItemStack getStackInSlot(int slot) { return inv.getStackInSlot(slot); }
            @Override public ItemStack insertItem(int slot, ItemStack stack, boolean sim) { return inv.insertItem(slot, stack, sim); }
            @Override public ItemStack extractItem(int slot, int amount, boolean sim) { return inv.extractItem(slot, amount, sim); }
            @Override public int getSlotLimit(int slot) { return inv.getSlotLimit(slot); }
            @Override public boolean isItemValid(int slot, ItemStack stack) { return inv.isItemValid(slot, stack); }
        };
    }

    private static CableOrbTile getOrbTile(BlockUIHolder holder) {
        if (holder.player.level().getBlockEntity(holder.pos) instanceof CableOrbTile tile) {
            return tile;
        }
        return null;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
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
                net.minecraft.client.gui.Font font = mc.font;
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
