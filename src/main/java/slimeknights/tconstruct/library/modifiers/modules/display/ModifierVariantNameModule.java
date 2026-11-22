package slimeknights.tconstruct.library.modifiers.modules.display;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.display.DisplayNameModifierHook;
import slimeknights.tconstruct.library.modifiers.modules.ModifierModule;
import slimeknights.tconstruct.library.modifiers.modules.build.SwappableSlotModule;
import slimeknights.tconstruct.library.module.HookProvider;
import slimeknights.tconstruct.library.module.ModuleHook;
import slimeknights.tconstruct.library.recipe.modifiers.adding.variant.VariantFormatter;
import slimeknights.tconstruct.library.tools.stack.ToolStackView;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Module for formatting the modifier variant name using a variant formatter
 * @see ModifierVariantColorModule
 */
public record ModifierVariantNameModule(VariantFormatter formatter) implements ModifierModule, DisplayNameModifierHook {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<ModifierVariantNameModule>defaultHooks(ModifierHooks.DISPLAY_NAME);
  public static final RecordLoadable<ModifierVariantNameModule> LOADER = RecordLoadable.create(
    VariantFormatter.LOADER.requiredField("formatter", ModifierVariantNameModule::formatter),
    ModifierVariantNameModule::new);

  @Override
  public Component getDisplayName(ToolStackView tool, ModifierEntry entry, Component name, @Nullable RegistryAccess access) {
    Component variant = this.formatter.format(tool, entry.getHolder());
    if (variant != null) {
      return Component.translatable(SwappableSlotModule.FORMAT, name, variant).withStyle(name.getStyle());
    }
    return name;
  }

  @Override
  public RecordLoadable<ModifierVariantNameModule> getLoader() {
    return LOADER;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }
}
