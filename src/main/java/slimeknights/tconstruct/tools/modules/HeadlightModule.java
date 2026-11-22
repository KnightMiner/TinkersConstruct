package slimeknights.tconstruct.tools.modules;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import slimeknights.mantle.data.loadable.primitive.IntLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.registration.holder.DeferredDataComponent;
import slimeknights.tconstruct.library.TinkerData;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHolder;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.build.ModifierRemovalHook;
import slimeknights.tconstruct.library.modifiers.hook.build.RawDataModifierHook;
import slimeknights.tconstruct.library.modifiers.modules.ModifierModule;
import slimeknights.tconstruct.library.module.HookProvider;
import slimeknights.tconstruct.library.module.ModuleHook;
import slimeknights.tconstruct.library.tools.component.ResourceMap;
import slimeknights.tconstruct.library.tools.stack.ToolStackView;

import javax.annotation.Nullable;
import java.util.List;

/** Module implementing headlight on helmets */
public record HeadlightModule(int defaultLight) implements ModifierModule, RawDataModifierHook, ModifierRemovalHook {
  private static final List<ModuleHook<?>> DEFAULT_HOOKS = HookProvider.<HeadlightModule>defaultHooks(ModifierHooks.RAW_DATA, ModifierHooks.REMOVE);
  public static final RecordLoadable<HeadlightModule> LOADER = RecordLoadable.create(
    IntLoadable.range(1, 15).requiredField("default_light", HeadlightModule::defaultLight),
    HeadlightModule::new);
  /** Reference to the light level component type from headlight - since its just integer its safe to use without a compile dependency */
  private static final DeferredDataComponent<Integer> LIGHT_LEVEL = DeferredDataComponent.createType(ResourceLocation.fromNamespaceAndPath("headlight", "level"));

  @Override
  public RecordLoadable<HeadlightModule> getLoader() {
    return LOADER;
  }

  @Override
  public List<ModuleHook<?>> getDefaultHooks() {
    return DEFAULT_HOOKS;
  }

  @Override
  public void addRawData(ToolStackView tool, ModifierEntry modifier) {
    tool.set(LIGHT_LEVEL, ResourceMap.get(tool, TinkerData.MODIFIER_INTEGER).getOrDefault(modifier.id(), defaultLight));
  }

  @Override
  public void removeRawData(ToolStackView tool, Modifier modifier) {
    tool.remove(LIGHT_LEVEL);
  }

  @Nullable
  @Override
  public Component onRemoved(ToolStackView tool, ModifierHolder modifier) {
    ResourceMap.remove(tool, TinkerData.MODIFIER_INTEGER, modifier.id());
    return null;
  }
}
