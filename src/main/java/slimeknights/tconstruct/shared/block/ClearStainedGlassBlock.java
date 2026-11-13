package slimeknights.tconstruct.shared.block;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class ClearStainedGlassBlock extends TransparentBlock {
  private final GlassColor glassColor;
  public ClearStainedGlassBlock(Properties properties, GlassColor glassColor) {
    super(properties);
    this.glassColor = glassColor;
  }

  @Nullable
  @Override
  public Integer getBeaconColorMultiplier(BlockState state, LevelReader world, BlockPos pos, BlockPos beaconPos) {
    return this.glassColor.getColor();
  }

  /** Enum used for registration of this and the pane block */
  @Getter
  @RequiredArgsConstructor
  public enum GlassColor implements StringRepresentable {
    WHITE(0xffffff, DyeColor.WHITE),
    ORANGE(0xd87f33, DyeColor.ORANGE),
    MAGENTA(0xb24cd8, DyeColor.MAGENTA),
    LIGHT_BLUE(0x6699d8, DyeColor.LIGHT_BLUE),
    YELLOW(0xe5e533, DyeColor.YELLOW),
    LIME(0x7fcc19, DyeColor.LIME),
    PINK(0xf27fa5, DyeColor.PINK),
    GRAY(0x4c4c4c, DyeColor.GRAY),
    LIGHT_GRAY(0x999999, DyeColor.LIGHT_GRAY),
    CYAN(0x4c7f99, DyeColor.CYAN),
    PURPLE(0x7f3fb2, DyeColor.PURPLE),
    BLUE(0x334cb2, DyeColor.BLUE),
    BROWN(0x664c33, DyeColor.BROWN),
    GREEN(0x667f33, DyeColor.GREEN),
    RED(0x993333, DyeColor.RED),
    BLACK(0x191919, DyeColor.BLACK);

    /** Variant color to reduce number of models */
    private final int color;
    /** Gets the vanilla dye color associated with this color */
    private final DyeColor dye;
    private final String serializedName = this.name().toLowerCase(Locale.US);

    @Override
    public String toString() {
      return serializedName;
    }
  }
}
