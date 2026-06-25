package com.hask.shop;

import com.hask.shop.gui.GUIListener;
import net.milkbowl.vault.economy.Economy;
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

    public Set<UUID> pendingAdd = new HashSet<>();
    public Set<UUID> pendingInfo = new HashSet<>();
    public Map<UUID, EditSession> pendingEdit = new HashMap<>();
    public Map<UUID, ShopData> pendingPurchaseQty = new HashMap<>();
    public Map<UUID, ConfirmSession> pendingConfirm = new HashMap<>();
    public Map<UUID, NpcConfirmSession> pendingNpcConfirm = new HashMap<>();
    public Map<UUID, NpcQtySession> pendingNpcQty = new HashMap<>();
    public Map<UUID, Integer> npcShopPage = new HashMap<>();
    public Map<UUID, Long> cooldowns = new HashMap<>();

    @Override
    public void onEnable() {
        instance = this;

        if (!setupEconomy()) {
            getLogger().severe("Vault/Economy nao encontrado! Desabilitando HaskShop.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        shopManager = new ShopManager(this);
        shopManager.load();

        npcShopManager = new NpcShopManager(this);
        npcShopManager.load();

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
        getLogger().info("HaskShop desativado.");
    }

    public void reload() {
        npcShopManager.load();
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) return false;
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        economy = rsp.getProvider();
        return economy != null;
    }
}
