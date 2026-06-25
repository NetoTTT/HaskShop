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
    public static Economy economy;

    public ShopManager shopManager;
    public Set<UUID> pendingAdd = new HashSet<>();
    public Set<UUID> pendingInfo = new HashSet<>();
    public Map<UUID, EditSession> pendingEdit = new HashMap<>();
    public Map<UUID, ShopData> pendingPurchaseQty = new HashMap<>();
    public Map<UUID, ConfirmSession> pendingConfirm = new HashMap<>();
    public Map<UUID, Long> cooldowns = new HashMap<>();

    @Override
    public void onEnable() {
        if (!setupEconomy()) {
            getLogger().severe("Vault/Economy não encontrado! Desabilitando HaskShop.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        shopManager = new ShopManager(this);
        shopManager.load();
        getServer().getPluginManager().registerEvents(new ShopListener(this), this);
        getServer().getPluginManager().registerEvents(new GUIListener(this), this);
        getCommand("cs").setExecutor(new ShopCommand(this));
        getLogger().info("HaskShop ativo! " + shopManager.count() + " loja(s) carregada(s).");
    }

    @Override
    public void onDisable() {
        if (shopManager != null) shopManager.save();
        getLogger().info("HaskShop desativado.");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) return false;
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        economy = rsp.getProvider();
        return economy != null;
    }
}
