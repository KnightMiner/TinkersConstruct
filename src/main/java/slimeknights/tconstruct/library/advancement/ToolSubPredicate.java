package slimeknights.tconstruct.library.advancement;

import com.mojang.serialization.Codec;
import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.world.item.ItemStack;
import slimeknights.mantle.data.codec.LoadableCodec;
import slimeknights.mantle.data.predicate.IJsonPredicate;
import slimeknights.tconstruct.common.TinkerTags.Items;
import slimeknights.tconstruct.library.json.predicate.tool.ToolStackPredicate;
import slimeknights.tconstruct.library.tools.nbt.IToolContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

/** Variant of ItemPredicate for matching Tinker tools using {@link ToolStackPredicate} */
public record ToolSubPredicate(IJsonPredicate<IToolStackView> predicate) implements ItemSubPredicate {
  public static final Codec<ToolSubPredicate> CODEC = new LoadableCodec<>(ToolStackPredicate.LOADER).xmap(ToolSubPredicate::new, ToolSubPredicate::predicate);

  /** Creates a tool stack predicate */
  public static ToolSubPredicate ofTool(IJsonPredicate<IToolStackView> predicate) {
    return new ToolSubPredicate(predicate);
  }

  /** Creates a tool context predicate */
  public static ToolSubPredicate ofContext(IJsonPredicate<IToolContext> predicate) {
    return new ToolSubPredicate(ToolStackPredicate.context(predicate));
  }

  @Override
  public boolean matches(ItemStack stack) {
    // tag check is important to prevent accidentally modifying the NBT of non-tools
    return stack.is(Items.MODIFIABLE) && predicate.matches(ToolStack.from(stack));
  }
}
