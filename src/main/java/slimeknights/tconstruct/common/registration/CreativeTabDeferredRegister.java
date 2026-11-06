package slimeknights.tconstruct.common.registration;

import net.minecraft.Util;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.UnaryOperator;

/** {@link DeferredRegister} for {@link CreativeModeTab} */
public class CreativeTabDeferredRegister extends DeferredRegister<CreativeModeTab> {
  public CreativeTabDeferredRegister(String namespace) {
    super(Registries.CREATIVE_MODE_TAB, namespace);
  }

  /** Registers a creative tab after running the given builder */
  public DeferredHolder<CreativeModeTab,CreativeModeTab> registerTab(String name, UnaryOperator<CreativeModeTab.Builder> builder) {
    return register(name, id -> builder.apply(CreativeModeTab.builder().title(Component.translatable(Util.makeDescriptionId("itemGroup", id)))).build());
  }
}
