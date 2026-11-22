package slimeknights.tconstruct.library.modifiers.modules.display;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.tconstruct.library.client.materials.MaterialTooltipCache;
import slimeknights.tconstruct.library.materials.MaterialVariant;
import slimeknights.tconstruct.library.materials.MaterialId;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.display.DisplayNameModifierHook;
import slimeknights.tconstruct.library.modifiers.modules.ModifierModule;
import slimeknights.tconstruct.library.module.HookProvider;
import slimeknights.tconstruct.library.module.ModuleHook;
import slimeknights.tconstruct.library.tools.stack.ToolStackView;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Module that colors the display name based on the variant.
 * @see ModifierVariantColorModule
 */
public record MaterialVariantColorModule(MaterialId material) implements ModifierModule, DisplayNameModifierHook {
  public static final RecordLoadable<MaterialVariantColorModule> LOADER = RecordLoadable.create(MaterialId.LOADABLE.requiredField("material", MaterialVariantColorModule::material), MaterialVariantColorModule::new);
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<MaterialVariantColorModule>defaultHooks(ModifierHooks.DISPLAY_NAME);

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public RecordLoadable<MaterialVariantColorModule> getLoader() {
    return LOADER;
  }

  @Override
  public Component getDisplayName(ToolStackView tool, ModifierEntry entry, Component name, @Nullable RegistryAccess access) {
    // use the color of the first match found
    for (MaterialVariant material : tool.getMaterials()) {
      if (this.material.equals(material.material())) {
        return name.copy().withStyle(style -> style.withColor(MaterialTooltipCache.getColor(material.id())));
      }
    }
    return name;
  }
}
