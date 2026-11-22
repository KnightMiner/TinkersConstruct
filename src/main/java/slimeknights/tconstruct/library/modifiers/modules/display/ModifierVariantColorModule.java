package slimeknights.tconstruct.library.modifiers.modules.display;

import lombok.RequiredArgsConstructor;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import slimeknights.mantle.client.ResourceColorManager;
import slimeknights.mantle.data.loadable.mapping.SimpleRecordLoadable;
import slimeknights.mantle.data.loadable.primitive.EnumLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.registration.holder.DeferredDataComponent;
import slimeknights.tconstruct.library.TinkerData;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.display.DisplayNameModifierHook;
import slimeknights.tconstruct.library.modifiers.modules.ModifierModule;
import slimeknights.tconstruct.library.module.HookProvider;
import slimeknights.tconstruct.library.module.ModuleHook;
import slimeknights.tconstruct.library.tools.component.ResourceMap;
import slimeknights.tconstruct.library.tools.stack.ToolStackView;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Module that colors the display name based on the variant.
 * @see ModifierVariantNameModule
 * @see MaterialVariantColorModule
 */
@RequiredArgsConstructor
public enum ModifierVariantColorModule implements ModifierModule, DisplayNameModifierHook {
  STRING(TinkerData.MODIFIER_STRING),
  INTEGER(TinkerData.MODIFIER_INTEGER);

  private final DeferredDataComponent<? extends ResourceMap<?>> component;
  public static final RecordLoadable<ModifierVariantColorModule> LOADER = new SimpleRecordLoadable<>(new EnumLoadable<>(ModifierVariantColorModule.class), "component");
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<ModifierVariantColorModule>defaultHooks(ModifierHooks.DISPLAY_NAME);

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public RecordLoadable<ModifierVariantColorModule> getLoader() {
    return LOADER;
  }

  @Override
  public Component getDisplayName(ToolStackView tool, ModifierEntry entry, Component name, @Nullable RegistryAccess access) {
    Object variant = tool.getOrDefault(component, ResourceMap.empty()).get(entry.id());
    if (variant != null) {
      TextColor color = ResourceColorManager.getOrNull(entry.getModifier().getTranslationKey() + '.' + variant);
      if (color != null) {
        return name.copy().withStyle(name.getStyle().withColor(color));
      }
    }
    return name;
  }
}
