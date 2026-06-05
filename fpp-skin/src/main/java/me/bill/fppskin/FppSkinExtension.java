package me.bill.fppskin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import me.bill.fakePlayerPlugin.FakePlayerPlugin;
import me.bill.fakePlayerPlugin.api.FppAddonCommand;
import me.bill.fakePlayerPlugin.api.FppApi;
import me.bill.fakePlayerPlugin.api.FppBot;
import me.bill.fakePlayerPlugin.api.FppBotSaveEvent;
import me.bill.fakePlayerPlugin.api.FppCommandExtension;
import me.bill.fakePlayerPlugin.api.FppExtension;
import me.bill.fakePlayerPlugin.api.FppNameTagService;
import me.bill.fakePlayerPlugin.api.event.FppBotDespawnEvent;
import me.bill.fakePlayerPlugin.api.event.FppBotSpawnEvent;
import me.bill.fakePlayerPlugin.config.Config;
import me.bill.fakePlayerPlugin.database.DatabaseManager;
import me.bill.fakePlayerPlugin.fakeplayer.FakePlayer;
import me.bill.fakePlayerPlugin.fakeplayer.FakePlayerManager;
import me.bill.fakePlayerPlugin.fakeplayer.SkinFetchService;
import me.bill.fakePlayerPlugin.fakeplayer.SkinManager;
import me.bill.fakePlayerPlugin.fakeplayer.SkinProfile;
import me.bill.fakePlayerPlugin.fakeplayer.SkinRepository;
import me.bill.fakePlayerPlugin.lang.Lang;
import me.bill.fakePlayerPlugin.util.FppScheduler;
import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public final class FppSkinExtension implements FppExtension, Listener {
  private FppApi api;
  private FakePlayerPlugin core;
  private FppAddonCommand command;
  private FppCommandExtension spawnSkinExtension;
  private SkinManager skinManager;
  private FppSkinFetcher skinFetcher;
  private File skinDataFile;
  private final Map<UUID, SavedSkin> savedByUuid = new ConcurrentHashMap<>();
  private final Map<String, SavedSkin> savedByName = new ConcurrentHashMap<>();
  private final Object skinDataSaveLock = new Object();
  private final AtomicInteger skinDataSaveGeneration = new AtomicInteger();
  private final AtomicInteger skinDataSavedGeneration = new AtomicInteger();
  private final AtomicBoolean skinDataSaveQueued = new AtomicBoolean(false);
  private ExecutorService skinDataSaveExecutor;

  @Override
  public @NotNull String getName() {
    return "FPP-Skin";
  }

  @Override
  public @NotNull String getVersion() {
    return "1.0.1";
  }

  @Override
  public @NotNull String getDescription() {
    return "Adds /fpp skin as a FakePlayerPlugin extension.";
  }

  @Override
  public @NotNull List<String> getAuthors() {
    return List.of("Bill");
  }

  @Override
  public void onEnable(@NotNull FppApi api) {
    this.api = api;
    if (!(api.getPlugin() instanceof FakePlayerPlugin fpp)) {
      api.getPlugin().getLogger().warning("[FPP-Skin] Unsupported host plugin instance.");
      return;
    }
    core = fpp;
    skinDataSaveExecutor = newSkinDataSaveExecutor();
    saveDefaultConfig();
    skinDataFile = new File(getDataFolder(), "skin-data.yml");
    loadSkinData();
    syncSkinConfig();
    ensureSkinFolder();
    skinFetcher = new FppSkinFetcher();
    core.setSkinFetchService(skinFetcher);
    SkinRepository.get().init(core);
    skinManager = new SkinManager(core);
    core.setSkinManager(skinManager);
    command = new SkinAddonCommand();
    api.registerCommand(command);
    spawnSkinExtension = new SpawnSkinCommandExtension();
    api.registerCommandExtension(spawnSkinExtension);
    Bukkit.getPluginManager().registerEvents(this, core);
    api.getPlugin().getLogger().info("[FPP-Skin] Enabled.");
  }

  @Override
  public void onDisable() {
    HandlerList.unregisterAll(this);
    if (api != null && command != null) api.unregisterCommand(command);
    if (api != null && spawnSkinExtension != null) api.unregisterCommandExtension(spawnSkinExtension);
    if (core != null && core.getSkinManager() == skinManager) {
      core.setSkinManager(null);
    }
    if (core != null && core.getSkinFetchService() == skinFetcher) {
      core.setSkinFetchService(null);
    }
    saveSkinDataNow();
    if (skinDataSaveExecutor != null) skinDataSaveExecutor.shutdownNow();
    skinDataSaveExecutor = null;
    skinFetcher = null;
    skinManager = null;
    savedByUuid.clear();
    savedByName.clear();
    skinDataFile = null;
    command = null;
    spawnSkinExtension = null;
    core = null;
    api = null;
  }

  private boolean enabled() {
    return getConfig().getBoolean("enabled", true);
  }

  private String permission() {
    return getConfig().getString("permissions.command", "fpp.skin");
  }

  private FakePlayerManager manager() {
    return core != null ? core.getFakePlayerManager() : null;
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onBotDespawn(FppBotDespawnEvent event) {
    checkpointSkin(event.getBot());
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onBotSpawn(FppBotSpawnEvent event) {
    if (!enabled()) return;
    FppBot bot = event.getBot();
    SavedSkin saved = savedSkin(bot.getUuid(), bot.getName());
    if (saved == null || !saved.isValid()) return;

    Player entity = bot.getEntity();
    if (entity != null) {
      FppScheduler.runAtEntity(core, entity, () -> applySavedSkin(bot, saved));
      FppScheduler.runAtEntityLaterWithId(core, entity, () -> applySavedSkin(bot, saved), 8L);
    } else {
      FppScheduler.runSync(core, () -> applySavedSkin(bot, saved));
      FppScheduler.runSyncLater(core, () -> applySavedSkin(bot, saved), 8L);
    }
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onBotSave(FppBotSaveEvent event) {
    checkpointSkin(event.getBot());
  }

  private void checkpointSkin(FppBot bot) {
    if (core == null || bot == null || core.getDatabaseManager() == null) return;
    FakePlayer fp = manager() != null ? manager().getByUuid(bot.getUuid()) : null;
    if (fp == null) return;

    SkinProfile skin = fp.getResolvedSkin();
    if (skin == null || !skin.isValid()) {
      skin = skinFromEntity(bot.getEntity(), bot.getName());
      if (skin != null && skin.isValid()) fp.setResolvedSkin(skin);
    }
    if (skin == null || !skin.isValid()) return;

    core.getDatabaseManager().updateBotSkin(bot.getUuid().toString(), skin.getValue(), skin.getSignature());
    saveExtensionSkin(bot.getUuid(), bot.getName(), skin.getValue(), skin.getSignature());
  }

  private void applySavedSkin(FppBot bot, SavedSkin saved) {
    if (bot == null || saved == null || !saved.isValid()) return;
    FakePlayer fp = manager() != null ? manager().getByUuid(bot.getUuid()) : null;
    if (fp == null) return;

    SkinProfile skin = new SkinProfile(saved.value(), saved.signature(), "fpp-skin-persist:" + bot.getName());
    if (skinManager != null) skinManager.applySkinFromProfile(fp, skin);
    else fp.setResolvedSkin(skin);
    if (core != null && core.getDatabaseManager() != null) {
      core.getDatabaseManager().updateBotSkin(bot.getUuid().toString(), saved.value(), saved.signature());
    }
  }

  private SavedSkin savedSkin(UUID uuid, String botName) {
    SavedSkin saved = uuid != null ? savedByUuid.get(uuid) : null;
    if (saved != null && saved.isValid()) return saved;
    return botName != null ? savedByName.get(botName.toLowerCase(Locale.ROOT)) : null;
  }

  private void saveExtensionSkin(UUID uuid, String botName, String value, String signature) {
    if (uuid == null && (botName == null || botName.isBlank())) return;
    if (value == null || value.isBlank()) {
      if (uuid != null) savedByUuid.remove(uuid);
      if (botName != null) savedByName.remove(botName.toLowerCase(Locale.ROOT));
    } else {
      SavedSkin saved = new SavedSkin(value, signature);
      if (uuid != null) savedByUuid.put(uuid, saved);
      if (botName != null && !botName.isBlank()) savedByName.put(botName.toLowerCase(Locale.ROOT), saved);
    }
    saveSkinDataAsync();
  }

  private void loadSkinData() {
    savedByUuid.clear();
    savedByName.clear();
    if (skinDataFile == null || !skinDataFile.isFile()) return;
    YamlConfiguration yaml = YamlConfiguration.loadConfiguration(skinDataFile);
    ConfigurationSection uuids = yaml.getConfigurationSection("uuids");
    if (uuids != null) {
      for (String rawUuid : uuids.getKeys(false)) {
        try {
          SavedSkin saved = loadSavedSkin(uuids.getConfigurationSection(rawUuid));
          if (saved != null && saved.isValid()) savedByUuid.put(UUID.fromString(rawUuid), saved);
        } catch (IllegalArgumentException ignored) {
        }
      }
    }
    ConfigurationSection names = yaml.getConfigurationSection("names");
    if (names != null) {
      for (String name : names.getKeys(false)) {
        SavedSkin saved = loadSavedSkin(names.getConfigurationSection(name));
        if (saved != null && saved.isValid()) savedByName.put(name.toLowerCase(Locale.ROOT), saved);
      }
    }
  }

  private SavedSkin loadSavedSkin(ConfigurationSection section) {
    if (section == null) return null;
    return new SavedSkin(section.getString("value", ""), section.getString("signature", ""));
  }

  private void saveSkinDataAsync() {
    if (skinDataFile == null || core == null) return;
    skinDataSaveGeneration.incrementAndGet();
    if (!skinDataSaveQueued.compareAndSet(false, true)) return;
    File target = skinDataFile;
    ExecutorService executor = skinDataSaveExecutor;
    if (executor == null) {
      skinDataSaveQueued.set(false);
      return;
    }
    try {
      executor.execute(() -> saveSkinDataLoop(target));
    } catch (RejectedExecutionException ignored) {
      skinDataSaveQueued.set(false);
    }
  }

  private void saveSkinDataLoop(File target) {
    try {
      while (true) {
        int generation = skinDataSaveGeneration.get();
        saveSkinDataSnapshot(target, new HashMap<>(savedByUuid), new HashMap<>(savedByName), generation);
        skinDataSavedGeneration.set(generation);
        if (generation == skinDataSaveGeneration.get()) return;
      }
    } finally {
      skinDataSaveQueued.set(false);
      if (skinDataFile != null && skinDataSavedGeneration.get() != skinDataSaveGeneration.get()) saveSkinDataAsync();
    }
  }

  private ExecutorService newSkinDataSaveExecutor() {
    return Executors.newSingleThreadExecutor(
        task -> {
          Thread thread = new Thread(task, "FPP-Skin-Storage");
          thread.setDaemon(true);
          return thread;
        });
  }

  private void saveSkinDataNow() {
    if (skinDataFile == null) return;
    int generation = skinDataSaveGeneration.incrementAndGet();
    saveSkinDataSnapshot(skinDataFile, new HashMap<>(savedByUuid), new HashMap<>(savedByName), generation);
  }

  private void saveSkinDataSnapshot(
      File target, Map<UUID, SavedSkin> uuidSnapshot, Map<String, SavedSkin> nameSnapshot, int generation) {
    if (target == null || generation != skinDataSaveGeneration.get()) return;
    YamlConfiguration yaml = new YamlConfiguration();
    for (Map.Entry<UUID, SavedSkin> entry : uuidSnapshot.entrySet()) {
      String base = "uuids." + entry.getKey() + ".";
      yaml.set(base + "value", entry.getValue().value());
      yaml.set(base + "signature", entry.getValue().signature());
    }
    for (Map.Entry<String, SavedSkin> entry : nameSnapshot.entrySet()) {
      String base = "names." + entry.getKey() + ".";
      yaml.set(base + "value", entry.getValue().value());
      yaml.set(base + "signature", entry.getValue().signature());
    }
    synchronized (skinDataSaveLock) {
      if (generation != skinDataSaveGeneration.get()) return;
      try {
        File parent = target.getParentFile();
        if (parent != null && !parent.isDirectory() && !parent.mkdirs()) return;
        yaml.save(target);
      } catch (IOException e) {
        if (core != null) core.getLogger().warning("[FPP-Skin] Could not save skin data: " + e.getMessage());
      }
    }
  }

  private SkinProfile skinFromEntity(Player player, String botName) {
    if (player == null) return null;
    for (ProfileProperty property : player.getPlayerProfile().getProperties()) {
      if (!"textures".equalsIgnoreCase(property.getName())) continue;
      String value = property.getValue();
      if (value == null || value.isBlank()) continue;
      String signature = property.getSignature();
      return new SkinProfile(value, signature, "despawn-checkpoint:" + botName);
    }
    return null;
  }

  private void syncSkinConfig() {
    org.bukkit.configuration.file.FileConfiguration ext = getConfig();
    org.bukkit.configuration.file.FileConfiguration coreConfig = core.getConfig();
    coreConfig.set("skin.mode", ext.getString("skin.mode", "player"));
    coreConfig.set("skin.guaranteed-skin", ext.getBoolean("skin.guaranteed-skin", true));
    coreConfig.set(
        "skin.clear-cache-on-reload", ext.getBoolean("skin.clear-cache-on-reload", true));
    org.bukkit.configuration.ConfigurationSection overrides =
        ext.getConfigurationSection("skin.overrides");
    coreConfig.set("skin.overrides", overrides != null ? overrides.getValues(false) : java.util.Map.of());
    coreConfig.set("skin.pool", ext.getStringList("skin.pool"));
    coreConfig.set("skin.use-skin-folder", ext.getBoolean("skin.use-skin-folder", true));
    coreConfig.set(
        "skin.mineskin.url-upload-enabled",
        ext.getBoolean("skin.mineskin.url-upload-enabled", true));
    coreConfig.set("skin.mineskin.api-key", ext.getString("skin.mineskin.api-key", ""));
    coreConfig.set("skin.mineskin.visibility", ext.getString("skin.mineskin.visibility", "public"));
  }

  private void ensureSkinFolder() {
    java.io.File root = core.getDataFolder();
    java.io.File skinsDir = new java.io.File(root, "skins");
    if (!skinsDir.exists()) skinsDir.mkdirs();
    java.io.File readme = new java.io.File(skinsDir, "README.txt");
    if (readme.exists()) return;
    try (java.io.PrintWriter w = new java.io.PrintWriter(readme)) {
      w.println("# FakePlayerPlugin - Skin Folder");
      w.println("#");
      w.println("# This folder is owned by the FPP-Skin extension.");
      w.println("# Place PNG skin files here to use them for bots.");
      w.println("# Requires the FPP-Skin extension to be installed and enabled.");
      w.println("#");
      w.println("# Naming rules:");
      w.println("#   <botname>.png  - assigned exclusively to the bot with that name");
      w.println("#   anything.png   - added to the random skin pool");
      w.println("#");
      w.println("# Skin files must be standard 64x64 or 64x32 Minecraft skin PNGs.");
    } catch (java.io.IOException e) {
      core.getLogger().fine("[FPP-Skin] Could not write skins/README.txt: " + e.getMessage());
    }
  }

  private boolean canControl(CommandSender sender, FakePlayer bot) {
    if (!(sender instanceof Player player)) return true;
    return api != null
        && api.getBot(bot.getUuid()).map(fppBot -> api.canControlBot(player, fppBot)).orElse(false);
  }

  private boolean isUrlSkin(String skinArg) {
    return skinArg.startsWith("http://")
        || skinArg.startsWith("https://")
        || skinArg.startsWith("data:image");
  }

  private void checkpointAppliedSkin(FakePlayer bot) {
    SkinProfile skin = bot.getResolvedSkin();
    if (skin == null || !skin.isValid()) skin = skinFromEntity(bot.getPlayer(), bot.getName());
    if (skin == null || !skin.isValid()) return;
    saveExtensionSkin(bot.getUuid(), bot.getName(), skin.getValue(), skin.getSignature());
    if (core != null && core.getDatabaseManager() != null) {
      core.getDatabaseManager().updateBotSkin(bot.getUuid().toString(), skin.getValue(), skin.getSignature());
    }
  }

  private final class SpawnSkinCommandExtension implements FppCommandExtension {
    @Override
    public @NotNull String getCommandName() {
      return "spawn";
    }

    @Override
    public @NotNull List<String> getAliases() {
      return List.of("sp");
    }

    @Override
    public @NotNull String getUsage() {
      return "... --skin <username|url>";
    }

    @Override
    public @NotNull String getDescription() {
      return "Adds --skin <username|url> to /fpp spawn.";
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
      SkinRequest request = extractSkin(args);
      if (request == null) return false;
      if (request.skin().isBlank()) {
        sender.sendMessage(Lang.get("skin-usage"));
        return true;
      }

      FakePlayerManager manager = manager();
      if (manager == null || core == null) return false;
      Set<UUID> before = activeUuids(manager);
      String commandLine = "fpp spawn" + (request.remainingArgs().isEmpty() ? "" : " " + String.join(" ", request.remainingArgs()));
      Bukkit.dispatchCommand(sender, commandLine);
      applySkinToNewBots(manager, before, request.skin().trim(), 0);
      return true;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
      String typed = args.length == 0 ? "" : args[args.length - 1].toLowerCase(Locale.ROOT);
      if ("--skin".startsWith(typed)) return List.of("--skin");
      if (args.length >= 2 && args[args.length - 2].equalsIgnoreCase("--skin")) {
        List<String> suggestions = new ArrayList<>();
        suggestions.add("https://");
        for (Player player : Bukkit.getOnlinePlayers()) suggestions.add(player.getName());
        return suggestions.stream()
            .filter(option -> option.toLowerCase(Locale.ROOT).startsWith(typed))
            .toList();
      }
      return List.of();
    }

    private SkinRequest extractSkin(String[] args) {
      List<String> remaining = new ArrayList<>();
      String skin = null;
      for (int i = 0; i < args.length; i++) {
        String arg = args[i];
        if (arg.equalsIgnoreCase("--skin")) {
          if (i + 1 >= args.length) return new SkinRequest("", remaining);
          skin = args[++i];
        } else if (arg.toLowerCase(Locale.ROOT).startsWith("--skin=")) {
          skin = arg.substring("--skin=".length());
        } else {
          remaining.add(arg);
        }
      }
      return skin == null ? null : new SkinRequest(skin, remaining);
    }

    private Set<UUID> activeUuids(FakePlayerManager manager) {
      Set<UUID> uuids = new HashSet<>();
      for (FakePlayer bot : manager.getActivePlayers()) uuids.add(bot.getUuid());
      return uuids;
    }

    private void applySkinToNewBots(FakePlayerManager manager, Set<UUID> before, String skin, int attempt) {
      if (core == null || skinManager == null || skin.isBlank()) return;
      Bukkit.getScheduler()
          .runTaskLater(
              core,
              () -> {
                boolean waitingForBodies = false;
                for (FakePlayer bot : manager.getActivePlayers()) {
                  if (before.contains(bot.getUuid())) continue;
                  if (bot.getPlayer() == null || !bot.getPlayer().isOnline()) {
                    waitingForBodies = true;
                    continue;
                  }
                  if (isUrlSkin(skin)) {
                    skinManager.applySkinByUrl(bot, skin).thenAccept(success -> checkpointIfApplied(bot, success));
                  } else {
                    skinManager.applySkinByPlayerName(bot, skin).thenAccept(success -> checkpointIfApplied(bot, success));
                  }
                }
                if (waitingForBodies && attempt < 6) applySkinToNewBots(manager, before, skin, attempt + 1);
              },
              attempt == 0 ? 5L : 10L);
    }

    private void checkpointIfApplied(FakePlayer bot, boolean success) {
      if (!success) return;
      FppScheduler.runSync(core, () -> checkpointAppliedSkin(bot));
    }
  }

  private final class SkinAddonCommand implements FppAddonCommand {
    @Override
    public @NotNull String getName() {
      return "skin";
    }

    @Override
    public @NotNull String getUsage() {
      return "<bot> <username|reset|--url <url>>";
    }

    @Override
    public @NotNull String getDescription() {
      return "Apply a skin to a bot from a Minecraft username, URL, or reset it.";
    }

    @Override
    public @NotNull String getPermission() {
      return permission();
    }

    @Override
    public @NotNull Material getIcon() {
      return Material.PLAYER_HEAD;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {
      if (!enabled()) {
        sender.sendMessage("Skin extension is disabled.");
        return true;
      }

      if (args.length < 1) {
        sender.sendMessage(Lang.get("skin-usage"));
        return true;
      }

      FakePlayerManager manager = manager();
      FakePlayer bot = manager != null ? manager.getByName(args[0]) : null;
      if (bot == null) {
        return executeOffline(sender, args);
      }

      if (args.length < 2) {
        sender.sendMessage(Lang.get("skin-usage"));
        return true;
      }

      if (!canControl(sender, bot)) {
        sender.sendMessage(Lang.get("no-permission"));
        return true;
      }

      SkinManager activeSkinManager = skinManager;
      if (activeSkinManager == null) {
        sender.sendMessage(Lang.get("skin-failed", "name", bot.getDisplayName()));
        return true;
      }

      FppNameTagService nameTagService = api.getService(FppNameTagService.class);
      if (nameTagService != null
          && nameTagService.isAvailable()
          && nameTagService.getSkin(bot.getUuid()) != null) {
        sender.sendMessage(Lang.get("skin-no-nametag"));
        return true;
      }

      String skinArg = args[1];
      if (skinArg.equalsIgnoreCase("reset")) {
        boolean ok = activeSkinManager.resetToDefaultSkin(bot);
        if (ok) saveExtensionSkin(bot.getUuid(), bot.getName(), null, null);
        sender.sendMessage(Lang.get(ok ? "skin-reset" : "skin-failed", "name", bot.getDisplayName()));
        return true;
      }

      if (skinArg.equalsIgnoreCase("--url")) {
        if (args.length < 3) {
          sender.sendMessage(Lang.get("skin-usage"));
          return true;
        }
        String url = args[2];
        sender.sendMessage(Lang.get("skin-applying", "name", bot.getDisplayName()));
        activeSkinManager
            .applySkinByUrl(bot, url)
            .thenAccept(success -> sendApplyResult(sender, bot, success, null));
        return true;
      }

      if (isUrlSkin(skinArg)) {
        sender.sendMessage(Lang.get("skin-applying", "name", bot.getDisplayName()));
        activeSkinManager
            .applySkinByUrl(bot, skinArg)
            .thenAccept(success -> sendApplyResult(sender, bot, success, null));
        return true;
      }

      sender.sendMessage(
          Lang.get("skin-applying-player", "name", bot.getDisplayName(), "player", skinArg));
      activeSkinManager
          .applySkinByUsername(bot, skinArg)
          .thenAccept(success -> sendApplyResult(sender, bot, success, skinArg));
      return true;
    }

    private boolean executeOffline(CommandSender sender, String[] args) {
      if (args.length < 2) {
        sender.sendMessage(Lang.get("skin-usage"));
        return true;
      }

      String botName = args[0];
      String skinArg = args[1];
      if (skinArg.equalsIgnoreCase("reset")) {
        saveOfflineSkin(botName, null, null);
        saveExtensionSkin(null, botName, null, null);
        sender.sendMessage(Lang.get("skin-reset", "name", botName));
        return true;
      }

      SkinFetchService fetcher = core != null ? core.getSkinFetchService() : SkinFetchService.NOOP;
      if (skinArg.equalsIgnoreCase("--url")) {
        if (args.length < 3) {
          sender.sendMessage(Lang.get("skin-usage"));
          return true;
        }
        sender.sendMessage(Lang.get("skin-applying", "name", botName));
        fetcher.fetchByUrl(args[2], (value, signature) -> finishOfflineApply(sender, botName, value, signature, null));
        return true;
      }

      if (isUrlSkin(skinArg)) {
        sender.sendMessage(Lang.get("skin-applying", "name", botName));
        fetcher.fetchByUrl(skinArg, (value, signature) -> finishOfflineApply(sender, botName, value, signature, null));
        return true;
      }

      sender.sendMessage(Lang.get("skin-applying-player", "name", botName, "player", skinArg));
      fetcher.fetchAsync(skinArg, (value, signature) -> finishOfflineApply(sender, botName, value, signature, skinArg));
      return true;
    }

    private void finishOfflineApply(
        CommandSender sender, String botName, String value, String signature, String playerName) {
      FppScheduler.runSync(
          core,
          () -> {
            if (value != null && !value.isBlank()) {
              saveOfflineSkin(botName, value, signature);
              saveExtensionSkin(null, botName, value, signature);
              sender.sendMessage(Lang.get("skin-applied", "name", botName));
            } else if (playerName != null) {
              sender.sendMessage(Lang.get("skin-player-not-found", "name", botName, "player", playerName));
            } else {
              sender.sendMessage(Lang.get("skin-failed", "name", botName));
            }
          });
    }

    private void saveOfflineSkin(String botName, String value, String signature) {
      if (core == null || core.getDatabaseManager() == null || botName == null || botName.isBlank()) return;
      DatabaseManager.DespawnSnapshotRow existing = findDespawnSnapshot(botName);
      core.getDatabaseManager()
          .saveDespawnSnapshot(
              botName.toLowerCase(Locale.ROOT),
              Config.serverId(),
              existing != null && existing.inventoryData() != null ? existing.inventoryData() : "",
              existing != null ? existing.xpTotal() : 0,
              existing != null ? existing.xpLevel() : 0,
              existing != null ? existing.xpProgress() : 0.0f,
              value,
              signature);
    }

    private DatabaseManager.DespawnSnapshotRow findDespawnSnapshot(String botName) {
      if (core == null || core.getDatabaseManager() == null) return null;
      for (DatabaseManager.DespawnSnapshotRow row :
          core.getDatabaseManager().loadDespawnSnapshotsForServer(Config.serverId())) {
        if (row.botName().equalsIgnoreCase(botName)) return row;
      }
      return null;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
      FakePlayerManager manager = manager();
      if (args.length == 1) {
        String prefix = args[0].toLowerCase(Locale.ROOT);
        if (manager == null) return List.of();
        return manager.getActiveNames().stream()
            .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(prefix))
            .toList();
      }

      if (args.length == 2) {
        String prefix = args[1].toLowerCase(Locale.ROOT);
        List<String> options = new ArrayList<>();
        options.add("reset");
        options.add("--url");
        for (Player player : Bukkit.getOnlinePlayers()) {
          if (manager == null || manager.getByUuid(player.getUniqueId()) == null) {
            options.add(player.getName());
          }
        }
        return options.stream()
            .filter(option -> option.toLowerCase(Locale.ROOT).startsWith(prefix))
            .toList();
      }

      if (args.length == 3 && args[1].equalsIgnoreCase("--url")) {
        return List.of("https://").stream()
            .filter(option -> option.startsWith(args[2].toLowerCase(Locale.ROOT)))
            .toList();
      }

      return List.of();
    }

    private boolean isUrlSkin(String skinArg) {
      return skinArg.startsWith("http://")
          || skinArg.startsWith("https://")
          || skinArg.startsWith("data:image");
    }

    private void sendApplyResult(
        CommandSender sender, FakePlayer bot, boolean success, String playerName) {
      FppScheduler.runSync(
          core,
          () -> {
            if (success) {
              checkpointAppliedSkin(bot);
              sender.sendMessage(Lang.get("skin-applied", "name", bot.getDisplayName()));
            } else if (playerName != null) {
              sender.sendMessage(
                  Lang.get(
                      "skin-player-not-found",
                      "name",
                      bot.getDisplayName(),
                      "player",
                      playerName));
            } else {
              sender.sendMessage(Lang.get("skin-failed", "name", bot.getDisplayName()));
            }
          });
    }

    private void checkpointAppliedSkin(FakePlayer bot) {
      SkinProfile skin = bot.getResolvedSkin();
      if (skin == null || !skin.isValid()) skin = skinFromEntity(bot.getPlayer(), bot.getName());
      if (skin == null || !skin.isValid()) return;
      saveExtensionSkin(bot.getUuid(), bot.getName(), skin.getValue(), skin.getSignature());
      if (core != null && core.getDatabaseManager() != null) {
        core.getDatabaseManager().updateBotSkin(bot.getUuid().toString(), skin.getValue(), skin.getSignature());
      }
    }
  }

  private record SkinRequest(String skin, List<String> remainingArgs) {}

  private record SavedSkin(String value, String signature) {
    private boolean isValid() {
      return value != null && !value.isBlank();
    }
  }
}
