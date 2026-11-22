package slimeknights.tconstruct.library.recipe.modifiers.adding.variant;

import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.MutableDataComponentHolder;
import slimeknights.mantle.data.registry.GenericLoaderRegistry;
import slimeknights.mantle.data.registry.GenericLoaderRegistry.IHaveLoader;
import slimeknights.tconstruct.library.modifiers.ModifierHolder;

/** Variant for {@link slimeknights.tconstruct.library.recipe.modifiers.adding.SwappableModifierRecipe}. Handles adding data to the tool and display for JEI. */
public interface SwappableVariant extends IHaveLoader {
  /** Loader instance for variants */
  GenericLoaderRegistry<SwappableVariant> LOADER = new GenericLoaderRegistry<>("Swappable Variant", false);

  /** Checks if the variant is already present on the tool. */
  boolean isPresent(DataComponentHolder tool, ModifierHolder modifier);

  /** Sets the variant on the tool. */
  void applyToTool(MutableDataComponentHolder tool, ModifierHolder modifier);

  /** Sets the variant on the tool. */
  void removeFromTool(MutableDataComponentHolder tool, ModifierHolder modifier);

  /** Gets the variant string to display in JEI for the given modifier. */
  Component formatVariant(ModifierHolder modifier);
}
