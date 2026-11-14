package slimeknights.tconstruct.library.materials.traits;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.network.ISimplePacket;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.materials.definition.MaterialId;

import java.util.HashMap;
import java.util.Map;

/** Packet sending material traits from server to client. */
@Internal
public record UpdateMaterialTraitsPacket(Map<MaterialId,MaterialTraits> materialToTraits) implements ISimplePacket {
  public static final Type<UpdateMaterialTraitsPacket> TYPE = new Type<>(TConstruct.getResource("update_material_traits"));
  public static final StreamCodec<FriendlyByteBuf,UpdateMaterialTraitsPacket> CODEC = new StreamCodec<>() {
    @Override
    public UpdateMaterialTraitsPacket decode(FriendlyByteBuf buffer) {
      int materialCount = buffer.readInt();
      Map<MaterialId,MaterialTraits> materialToTraits = new HashMap<>(materialCount);
      for (int i = 0; i < materialCount; i++) {
        MaterialId id = MaterialId.create(buffer.readResourceLocation());
        MaterialTraits traits = MaterialTraits.read(buffer);
        materialToTraits.put(id, traits);
      }
      return new UpdateMaterialTraitsPacket(Map.copyOf(materialToTraits));
    }

    @Override
    public void encode(FriendlyByteBuf buffer, UpdateMaterialTraitsPacket packet) {
      buffer.writeInt(packet.materialToTraits.size());
      for (Map.Entry<MaterialId,MaterialTraits> entry : packet.materialToTraits.entrySet()) {
        buffer.writeResourceLocation(entry.getKey());
        entry.getValue().write(buffer);
      }
    }
  };

  @Override
  public Type<UpdateMaterialTraitsPacket> type() {
    return TYPE;
  }

  @Override
  public void handle(IPayloadContext context) {
    MaterialRegistry.updateMaterialTraitsFromServer(this);
  }
}
