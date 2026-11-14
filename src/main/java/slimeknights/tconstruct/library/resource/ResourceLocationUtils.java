package slimeknights.tconstruct.library.resource;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;

/** Helpers for working with resource locations */
public class ResourceLocationUtils {
  /** Splits the given string into a namespace and path, using the given default mod ID */
  public static String[] decompose(String defaultDomain, String location, char separator) {
    String[] parts = { defaultDomain, location };
    int loc = location.indexOf(separator);
    if (loc >= 0) {
      parts[1] = location.substring(loc + 1);
      if (loc >= 1) {
        parts[0] = location.substring(0, loc);
      }
    }

    return parts;
  }

  /** Splits the given string into a namespace and path, using the given default mod ID */
  public static String[] decompose(String defaultDomain, String location) {
    return decompose(defaultDomain, location, ':');
  }

  /**
   * Attempts to read a resource ID from the given string reader
   * @param defaultDomain  Domain to use if the domain is unset
   * @param reader         Reader to read from
   * @return  Resource location, or exception if invalid
   * @throws CommandSyntaxException  If parsing fails
   */
  public static ResourceLocation read(String defaultDomain, StringReader reader) throws CommandSyntaxException {
    int start = reader.getCursor();
    while(reader.canRead() && ResourceLocation.isAllowedInResourceLocation(reader.peek())) {
      reader.skip();
    }
    String string = reader.getString().substring(start, reader.getCursor());
    String[] parts = decompose(defaultDomain, string);
    try {
      return ResourceLocation.fromNamespaceAndPath(parts[0], parts[1]);
    } catch (ResourceLocationException ex) {
      reader.setCursor(start);
      throw ResourceLocation.ERROR_INVALID.createWithContext(reader);
    }
  }
}
