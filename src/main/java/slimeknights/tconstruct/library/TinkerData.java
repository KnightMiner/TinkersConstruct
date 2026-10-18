package slimeknights.tconstruct.library;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.gadgets.item.PiggybackHandler;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;

/** Handles registration of data components and data attachments added by Tinkers. */
public class TinkerData {
  private TinkerData() {}

  private static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, TConstruct.MOD_ID);
  private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, TConstruct.MOD_ID);

  @Internal
  public static void register(IEventBus bus) {
    DATA_COMPONENTS.register(bus);
    ATTACHMENTS.register(bus);
  }

  /** Handles modifiers storing extra tool data on entities. */
  public static final DeferredHolder<AttachmentType<?>,AttachmentType<ModDataNBT>> PERSISTENT_DATA = ATTACHMENTS.register("persistent_data", () -> AttachmentType.builder(ModDataNBT::new).serialize(ModDataNBT.CODEC).sync(ModDataNBT.STREAM_CODEC).copyOnDeath().build());
  /** Handles syncing the entity list for piggybackpacks */
  public static final DeferredHolder<AttachmentType<?>,AttachmentType<PiggybackHandler>> PIGGYBACK_HANDLER = ATTACHMENTS.register("piggyback_handler", () -> AttachmentType.builder(PiggybackHandler::new).build());
}
