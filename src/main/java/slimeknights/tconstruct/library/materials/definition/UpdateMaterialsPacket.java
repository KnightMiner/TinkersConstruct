package slimeknights.tconstruct.library.materials.definition;

import com.google.common.collect.ImmutableMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.network.ISimplePacket;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.utils.GenericTagUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/** Packet sending the material definitions, redirects, and tags from the server to client */
@Internal
public record UpdateMaterialsPacket(Map<MaterialId,IMaterial> materials, Map<MaterialId,MaterialId> redirects, Map<TagKey<IMaterial>,List<IMaterial>> tags) implements ISimplePacket {
  public static final Type<UpdateMaterialsPacket> TYPE = new Type<>(TConstruct.getResource("update_materials"));
  public static final StreamCodec<FriendlyByteBuf,UpdateMaterialsPacket> CODEC = new StreamCodec<>() {
    @Override
    public UpdateMaterialsPacket decode(FriendlyByteBuf buffer) {
      int materialCount = buffer.readInt();
      ImmutableMap.Builder<MaterialId,IMaterial> materialBuilder = ImmutableMap.builder();

      for (int i = 0; i < materialCount; i++) {
        MaterialId id = new MaterialId(buffer.readResourceLocation());
        int tier = buffer.readVarInt();
        int sortOrder = buffer.readVarInt();
        Rarity rarity = buffer.readEnum(Rarity.class);
        boolean craftable = buffer.readBoolean();
        boolean hidden = buffer.readBoolean();
        materialBuilder.put(id, new Material(id, tier, sortOrder, rarity, craftable, hidden));
      }
      Map<MaterialId,IMaterial> materials = materialBuilder.build();
      // process redirects
      int redirectCount = buffer.readVarInt();
      Map<MaterialId,MaterialId> redirects;
      if (redirectCount == 0) {
        redirects = Map.of();
      } else {
        redirects = new HashMap<>(redirectCount);
        for (int i = 0; i < redirectCount; i++) {
          redirects.put(new MaterialId(buffer.readUtf()), new MaterialId(buffer.readUtf()));
        }
        redirects = Map.copyOf(redirects);
      }
      Map<TagKey<IMaterial>,List<IMaterial>> tags = GenericTagUtil.decodeTags(buffer, MaterialManager.REGISTRY_KEY, id -> materials.get(new MaterialId(id)));
      return new UpdateMaterialsPacket(materials, redirects, tags);
    }

    @Override
    public void encode(FriendlyByteBuf buffer, UpdateMaterialsPacket packet) {
      buffer.writeInt(packet.materials.size());
      for (IMaterial material : packet.materials.values()) {
        buffer.writeResourceLocation(material.getIdentifier());
        buffer.writeVarInt(material.getTier());
        buffer.writeVarInt(material.getSortOrder());
        buffer.writeEnum(material.getRarity());
        buffer.writeBoolean(material.isCraftable());
        buffer.writeBoolean(material.isHidden());
      }
      buffer.writeVarInt(packet.redirects.size());
      for (Entry<MaterialId,MaterialId> entry : packet.redirects.entrySet()) {
        buffer.writeUtf(entry.getKey().toString());
        buffer.writeUtf(entry.getValue().toString());
      }
      GenericTagUtil.encodeTags(buffer, IMaterial::getIdentifier, packet.tags);
    }
  };

  @Override
  public Type<UpdateMaterialsPacket> type() {
    return TYPE;
  }

  @Override
  public void handle(IPayloadContext context) {
    MaterialRegistry.updateMaterialsFromServer(this);
  }
}
