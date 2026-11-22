package slimeknights.tconstruct.common.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.EnumValue;
import slimeknights.tconstruct.TConstruct;

import java.util.function.Supplier;

/** Helper for conditional stacktrace for logging based on a config option. */
public enum LogBadFunctionCall {
  STACKTRACE {
    @Override
    public void log(String config, String message) {
      TConstruct.LOG.warn("{}. Stacktrace can be disabled using config option {}.", config, message, new Exception("Stack trace"));
    }
  },
  WARNING {
    @Override
    public void log(String config, String message) {
      TConstruct.LOG.warn("{}. To debug this issue or disable the warning, use config option {}.", message, config);
    }
  },
  IGNORED {
    @Override
    public void log(String config, String message) {}

    /** Logs a message with arguments */
    @Override
    public void log(String config, Supplier<String> message) {}
  };

  /** Logs a message with no arguments */
  public abstract void log(String config, String message);

  /** Logs a message with arguments */
  public void log(String config, Supplier<String> message) {
    log(config, message.get());
  }

  /** Returns true if logging is enabled. */
  public boolean doesLog() {
    return this != IGNORED;
  }

  /** Wrapper for a config property */
  public static class ConfigValue {
    private final String name;
    private final EnumValue<LogBadFunctionCall> config;

    ConfigValue(String name, EnumValue<LogBadFunctionCall> config) {
      this.name = name;
      this.config = config;
    }

    /** Defines a config value */
    public static ConfigValue define(String name, ModConfigSpec.Builder builder) {
      return new ConfigValue(name, builder.defineEnum(name, WARNING));
    }

    /** Logs a message with no arguments */
    public void log(String message) {
      config.get().log(name, message);
    }

    /** Logs a message with arguments */
    public void log(Supplier<String> message) {
      config.get().log(name, message.get());
    }
  }
}
