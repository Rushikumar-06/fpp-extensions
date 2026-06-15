package me.bill.fppcommand;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import me.bill.fakePlayerPlugin.api.FppAddonCommand;
import me.bill.fakePlayerPlugin.api.FppApi;
import me.bill.fakePlayerPlugin.api.FppBot;
import me.bill.fakePlayerPlugin.api.FppExtension;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class FppCommandExtension implements FppExtension {
  private FppApi api;
  private FppAddonCommand command;

  @Override
  public @NotNull String getName() {
    return "FPP-Command";
  }

  @Override
  public @NotNull String getVersion() {
    return "1.1.1";
  }

  @Override
  public @NotNull String getDescription() {
    return "Adds /fpp cmd for executing commands as fake players.";
  }

  @Override
  public @NotNull List<String> getAuthors() {
    return List.of("Bill");
  }

  @Override
  public void onEnable(@NotNull FppApi api) {
    this.api = api;
    saveDefaultConfig();
    command = new CmdAddonCommand();
    api.registerCommand(command);
    api.getPlugin().getLogger().info("[FPP-Command] Enabled.");
  }

  @Override
  public void onDisable() {
    if (api != null && command != null) api.unregisterCommand(command);
    command = null;
    api = null;
  }

  private boolean enabled() {
    return getConfig().getBoolean("enabled", true);
  }

  private String prefix() {
    return ChatColor.translateAlternateColorCodes(
        '&', getConfig().getString("messages.prefix", "&8[&dFPP Command&8]&r "));
  }

  private String permission() {
    return getConfig().getString("permissions.execute", "fpp.command.execute");
  }

  private final class CmdAddonCommand implements FppAddonCommand {
    @Override
    public @NotNull String getName() {
      return "cmd";
    }

    @Override
    public @NotNull String getUsage() {
      return "<bot> <command> | <bot> --clear";
    }

    @Override
    public @NotNull String getDescription() {
      return "Execute a server command as a bot.";
    }

    @Override
    public @NotNull String getPermission() {
      return permission();
    }

    @Override
    public @NotNull Material getIcon() {
      return Material.COMMAND_BLOCK;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
      if (!enabled()) {
        sender.sendMessage(prefix() + ChatColor.RED + "Command extension is disabled.");
        return true;
      }

      if (args.length < 2) {
        sender.sendMessage(prefix() + ChatColor.RED + "Usage: /fpp cmd " + getUsage());
        return true;
      }

      Optional<FppBot> found = api.getBot(args[0]);
      if (found.isEmpty()) {
        sender.sendMessage(prefix() + ChatColor.RED + "Bot not found: " + ChatColor.YELLOW + args[0]);
        return true;
      }

      FppBot bot = found.get();
      if (sender instanceof Player player && !api.canControlBot(player, bot)) {
        sender.sendMessage(prefix() + ChatColor.RED + "You do not have permission to control that bot.");
        return true;
      }

      if (args.length == 2 && args[1].equalsIgnoreCase("--clear")) {
        api.removeBotExtensionData(bot, "command", "last-command");
        sender.sendMessage(
            prefix()
                + ChatColor.GREEN
                + "Cleared stored command data for "
                + ChatColor.YELLOW
                + bot.getName()
                + ChatColor.GREEN
                + ".");
        return true;
      }

      String commandLine = normalizeCommand(String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
      if (commandLine.isBlank()) {
        sender.sendMessage(prefix() + ChatColor.RED + "Command cannot be empty.");
        return true;
      }

      if (!bot.isOnline() || bot.isBodyless()) {
        sender.sendMessage(prefix() + ChatColor.RED + "Bot must have an online body to run commands.");
        return true;
      }

      boolean success = api.runAsBot(bot, commandLine);
      if (success) {
        api.setBotExtensionData(bot, "command", "last-command", commandLine);
        sender.sendMessage(
            prefix()
                + ChatColor.GREEN
                + "Ran command as "
                + ChatColor.YELLOW
                + bot.getName()
                + ChatColor.GREEN
                + ": "
                + ChatColor.WHITE
                + commandLine);
      } else {
        sender.sendMessage(
            prefix()
                + ChatColor.RED
                + "Failed to run command as "
                + ChatColor.YELLOW
                + bot.getName()
                + ChatColor.RED
                + ".");
      }
      return true;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
      if (args.length == 1) {
        String partial = args[0].toLowerCase(Locale.ROOT);
        List<String> options = new ArrayList<>();
        for (FppBot bot : api.getBots()) {
          if (sender instanceof Player player && !api.canControlBot(player, bot)) continue;
          if (bot.getName().toLowerCase(Locale.ROOT).startsWith(partial)) options.add(bot.getName());
        }
        return options;
      }

      if (args.length == 2) {
        String partial = args[1].toLowerCase(Locale.ROOT);
        if ("--clear".startsWith(partial)) return List.of("--clear");
      }

      return List.of();
    }

    private String normalizeCommand(String commandLine) {
      String out = commandLine.trim();
      while (out.startsWith("/")) out = out.substring(1).trim();
      return out;
    }
  }
}
