package slimeknights.tconstruct.library.tools.nbt;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.library.recipe.RecipeResult;

import javax.annotation.Nullable;

/**
 * Helper which contains a lazily loaded tool stack, used for recipe output to reduce NBT parsing
 * TODO: is this still necessary with data components making tool stack free?
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LazyToolStack {
  @Getter
  private ItemStack stack;
  @Nullable
  private ToolStack tool;

  /* Constructors */

  /** Creates from a stack, lazily loading the tool stack */
  public static LazyToolStack from(ItemStack stack) {
    return new LazyToolStack(stack, null);
  }

  /** Creates from a tool with the given count, lazily loading the stack */
  public static LazyToolStack from(ToolStack tool) {
    return new LazyToolStack(tool.getStack(), tool);
  }


  /* Result helpers */

  /** Creates a success for a tinker station or modifier worktable recipe */
  public static RecipeResult<LazyToolStack> success(ItemStack stack) {
    return RecipeResult.success(LazyToolStack.from(stack));
  }

  /** Creates a success for a tinker station or modifier worktable recipe */
  public static RecipeResult<LazyToolStack> success(ToolStack tool) {
    return RecipeResult.success(LazyToolStack.from(tool));
  }


  /** Gets the item inside this stack without needing to resolve. */
  public Item getItem() {
    return stack.getItem();
  }

  /** Gets the count without needing to resolve. */
  public int getCount() {
    return stack.getCount();
  }

  /** Checks if the stack has the given tag without resolving. */
  public boolean hasTag(TagKey<Item> tag) {
    if (stack != null) {
      return stack.is(tag);
    }
    if (tool != null) {
      return tool.hasTag(tag);
    }
    return false;
  }

  /** Gets the tool for this instance */
  public ToolStack getTool() {
    if (tool == null) {
      assert stack != null;
      tool = ToolStack.from(stack);
    }
    return tool;
  }
}
