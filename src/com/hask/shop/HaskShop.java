package com.hask.shop;

import com.hask.shop.custom.CombatTracker;
import com.hask.shop.custom.CustomItemRegistry;
import com.hask.shop.custom.PlacedBedrockTracker;
import com.hask.shop.custom.dig.DigManager;
import com.hask.shop.custom.booster.BoosterAdminCommand;
import com.hask.shop.custom.booster.BoosterManager;
import com.hask.shop.custom.booster.DebugXpCommand;
import com.hask.shop.custom.item.BauExtraItem;
import com.hask.shop.custom.item.BedrockGeneratorItem;
import com.hask.shop.custom.item.BoosterItem;
import com.hask.shop.custom.item.HoleDiggerItem;
import com.hask.shop.custom.item.LauncherItem;
import com.hask.shop.custom.item.PowerItem;
import com.hask.shop.custom.item.ReparadorItem;
import com.hask.shop.custom.item.SilkTouch2Item;
import com.hask.shop.custom.item.TotemImortalidadeItem;
import com.hask.shop.custom.item.TotemMorteItem;
import com.hask.shop.custom.item.ChunkBorderTask;
import com.hask.shop.custom.item.SpecialItemListener;
import com.hask.shop.custom.item.SpecialItemRegistry;
import com.hask.shop.gui.GUIListener;
import com.hask.shop.shop.CashShopCommand;
import com.hask.shop.shop.CashShopManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class HaskShop extends JavaPlugin {
    public static HaskShop instance;
    public static Economy economy;

    public ShopManager shopManager;
    public NpcShopManager npcShopManager;
    public CustomItemRegistry customItemRegistry;
    public SpecialItemRegistry specialItemRegistry;
    public PlacedBedrockTracker bedrockTracker;
    public BoosterManager boosterManager;
    public CashShopManager cashShopManager;
    // public LavaBucketItem lavaBucketItem; // ARQUIVADO - item balde_lava_bedrock desativado

    public Set<UUID> pendingAdd = new HashSet<>();
    public Set<UUID> pendingInfo = new HashSet<>();
    public Map<UUID, EditSession> pendingEdit = new HashMap<>();
    public Map<UUID, ShopData> pendingPurchaseQty = new HashMap<>();
    public Map<UUID, ConfirmSession> pendingConfirm = new HashMap<>();
    public Map<UUID, NpcConfirmSession> pendingNpcConfirm = new HashMap<>();
    public Map<UUID, NpcQtySession> pendingNpcQty = new HashMap<>();
    public Map<UUID, Integer> npcShopPage = new HashMap<>();
    public Map<UUID, Long> cooldowns = new HashMap<>();

    // Sessões temporárias
    public Map<UUID, ItemStack> pendingBooster = new HashMap<>();

    @Override
    public void onEnable() {
        instance = this;

        if (!setupEconomy()) {
            getLogger().severe("Vault/Economy nao encontrado! Desabilitando HaskShop.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        customItemRegistry = new CustomItemRegistry(this);
        customItemRegistry.load();

        bedrockTracker = new PlacedBedrockTracker(this);
        bedrockTracker.load();
        getServer().getPluginManager().registerEvents(bedrockTracker, this);

        shopManager = new ShopManager(this);
        shopManager.load();

        npcShopManager = new NpcShopManager(this);
        npcShopManager.load();

        specialItemRegistry = new SpecialItemRegistry(this);
        specialItemRegistry.register(new HoleDiggerItem());

        cashShopManager = new CashShopManager(this);
        cashShopManager.load();
        specialItemRegistry.register(new BedrockGeneratorItem());
        specialItemRegistry.register(new LauncherItem());
        specialItemRegistry.register(new PowerItem());
        specialItemRegistry.register(new BauExtraItem());

        // ARQUIVADO: balde_lava_bedrock - lógica pesada com scans repetitivos e limitação do 1.8.8
        // lavaBucketItem = new LavaBucketItem();
        // getServer().getPluginManager().registerEvents(lavaBucketItem, this);

        getServer().getPluginManager().registerEvents(new SpecialItemListener(specialItemRegistry), this);

        ChunkBorderTask borderTask = new ChunkBorderTask();
        getServer().getPluginManager().registerEvents(borderTask, this);
        getServer().getScheduler().runTaskTimer(this, borderTask, 5L, 5L);

        CombatTracker combatTracker = new CombatTracker();
        getServer().getPluginManager().registerEvents(combatTracker, this);

        specialItemRegistry.register(new ReparadorItem(combatTracker));

        DigManager digManager = new DigManager();
        getServer().getPluginManager().registerEvents(digManager, this);

        getServer().getPluginManager().registerEvents(new SilkTouch2Item(digManager), this);
        getServer().getPluginManager().registerEvents(new TotemMorteItem(), this);
        getServer().getPluginManager().registerEvents(new TotemImortalidadeItem(), this);

        boosterManager = new BoosterManager();
        getServer().getPluginManager().registerEvents(boosterManager, this);
        specialItemRegistry.register(new BoosterItem());

        DebugXpCommand debugXp = new DebugXpCommand();
        getServer().getPluginManager().registerEvents(debugXp, this);
        if (getCommand("xpdebug") != null) {
            getCommand("xpdebug").setExecutor(debugXp);
        }
        if (getCommand("booster") != null) {
            getCommand("booster").setExecutor(new BoosterAdminCommand());
        }
        if (getCommand("shop") != null) {
            getCommand("shop").setExecutor(new CashShopCommand());
        }

        getServer().getPluginManager().registerEvents(new ShopListener(this), this);
        getServer().getPluginManager().registerEvents(new GUIListener(this), this);
        getCommand("hs").setExecutor(new ShopCommand(this));

        org.bukkit.plugin.Plugin citizensPlugin = getServer().getPluginManager().getPlugin("Citizens");
        if (citizensPlugin != null) {
            getServer().getPluginManager().registerEvents(new NpcShopListener(this), this);
            getLogger().info("Citizens detectado (v" + citizensPlugin.getDescription().getVersion() + ") - lojas de NPC ativas.");
        } else {
            getLogger().warning("Citizens NAO encontrado - lojas de NPC desativadas.");
        }

        getLogger().info("HaskShop ativo! " + shopManager.count() + " placa(s), "
            + npcShopManager.getAll().size() + " NPC(s).");
    }

    @Override
    public void onDisable() {
        if (shopManager != null) shopManager.save();
        if (bedrockTracker != null) bedrockTracker.save();
        getLogger().info("HaskShop desativado.");
    }

    public void reload() {
        npcShopManager.load();
        customItemRegistry.load();
        cashShopManager.load();
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) return false;
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        economy = rsp.getProvider();
        return economy != null;
    }
}
