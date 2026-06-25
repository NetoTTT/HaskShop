# HaskShop

A lightweight, fully GUI-driven shop plugin for **Minecraft 1.8.8 PaperSpigot**. Admins write any text they want on signs, register them as shops, and configure everything through in-game GUIs — no config file editing required. Also supports **NPC-based shops** via Citizens integration.

## Features

- **Custom sign text** — the sign's appearance is 100% up to you; shop logic is stored separately
- **GUI-based configuration** — item, price, quantity, buy/sell mode, enable/disable, all in one chest GUI
- **Free quantity mode** — players choose how many units to buy/sell; supports `all`/`todos`/`tudo`/`max` shortcuts
- **Confirmation screen** — always shows total price before any transaction is charged
- **Spawner support** — sell mob spawners with the correct mob type via a visual mob picker (29 mobs, powered by NMS NBT)
- **NPC shops** (Citizens) — configure a catalog of items per NPC in YAML; left-click to buy, right-click to sell
- **Pagination** — NPC shop GUIs support unlimited items across multiple pages (28 per page)
- **Vault economy** — works with any Vault-compatible economy plugin
- **Sign protection** — registered shop signs cannot be broken; admins must `/hs remove` first
- **Inventory validation** — checks available space and balance before opening the confirmation screen
- **Cooldown** — 2-second click cooldown per player to prevent spam

## Requirements

| Dependency | Version |
|---|---|
| PaperSpigot / Spigot | 1.8.8 |
| [Vault](https://github.com/milkbowl/Vault) | Any |
| Economy plugin | EssentialsX Economy, etc. |
| [Citizens](https://citizensnpcs.co/) | 2.x *(optional, for NPC shops)* |

> **Spawner support** works natively via NMS. [SilkSpawners](https://github.com/timbru31/SilkSpawners) is compatible but not required.

## Installation

1. Drop `HaskShop.jar` into your `/plugins` folder
2. Make sure Vault and an economy plugin are loaded
3. *(Optional)* Load Citizens for NPC shop support
4. Start the server — `shops.yml` and `npc-shops.yml` are created automatically
5. Grant the `shopsign.admin` permission to staff

## Commands

| Command | Description |
|---|---|
| `/hs add` | Activate creation mode — right-click any sign to register it as a shop |
| `/hs info` | Activate info mode — right-click a shop sign to see its ID and settings |
| `/hs edit <ID>` | Open the configuration GUI for a shop |
| `/hs remove <ID>` | Unregister a shop (allows the sign to be broken afterwards) |
| `/hs list` | List all registered shops |
| `/hs shop <shopId>` | Open an NPC shop catalog (used internally by Citizens `/npc command`) |
| `/hs npcshop` | List all configured NPC shops |
| `/hs reload` | Reload `npc-shops.yml` without restarting the server |

## Permissions

| Permission | Default | Description |
|---|---|---|
| `shopsign.admin` | OP | Create, edit, and remove shop signs |
| `shopsign.use` | Everyone | Use (buy/sell from) shop signs |

## How to Create a Shop Sign

1. **Place a sign** and write whatever you like (colors, custom text — fully free)
2. Run `/hs add`, then **right-click the sign** → shop is registered and assigned an ID
3. Run `/hs edit <ID>` to open the configuration GUI:

```
┌─────────────────────────────┐
│  [MOB]  [────]  [QTY FREE]  │  ← mob picker (spawners), quantity toggle
│  [ITEM] [AMT]  [TYPE] [PRC] │  ← item, amount per unit, buy/sell, price
│  [────] [────] [ON/OFF][DEL]│  ← enable/disable, remove
└─────────────────────────────┘
```

4. Set the **item** (type the name in chat, e.g. `DIAMOND`)
5. Set the **price** and **amount**
6. Toggle **BUY** or **SELL** mode
7. Click **ACTIVATE** — the shop is live

## Free Quantity Mode

When enabled on a sign or NPC item, the player is prompted to type how many they want before checkout:

- Type a number → that exact amount
- Type `all`, `todos`, `tudo`, `max`, `total`, `full`, `everything` → **auto-fills the maximum** (for sell: all items in inventory; for buy: max affordable that fit in inventory)

A confirmation screen always appears before charging:

```
[ CANCEL ]   [Item — 5x DIAMOND — Total: 25000 coins]   [ CONFIRM ]
```

## NPC Shops (Citizens)

1. Create an NPC: `/npc create <name>`
2. Select it: `/npc select` — note the ID shown on screen
3. Attach the shop command: `/npc command add hs shop <shopId>`
4. Configure `plugins/HaskShop/npc-shops.yml`:

```yaml
shops:
  loja_recursos:
    name: "§6§lResources"
    items:
      - item: DIAMOND
        buy_price: 5000.0
        sell_price: 3500.0
        amount: 1
        quantity_free: true
      - mob: ZOMBIE
        buy_price: 50000.0
        amount: 1
```

5. Run `/hs reload` — done! No restart needed.

**Left-click** a catalog item to buy · **Right-click** to sell · Pages scroll automatically when there are more than 28 items.

## Spawner Shops

1. Set item to `MOB_SPAWNER`
2. Click the **MOB TYPE** slot that appears in the GUI
3. A 6-row GUI opens with all 29 available mobs as spawn eggs, color-coded:
   - **Red** — Hostile (Zombie, Skeleton, Blaze, Spider…)
   - **Yellow** — Neutral (Wolf, Bat, Ocelot)
   - **Green** — Passive (Pig, Cow, Horse, Villager…)
4. Players receive a named spawner item: `Spawner » Zombie` with correct NBT

## Building from Source

**Requirements:** JDK 8+, `spigot-1.8.8.jar`, `Vault.jar`, `Citizens.jar` *(optional)*

```bash
javac --release 8 -encoding UTF-8 \
  -cp "deps/spigot.jar:deps/Vault.jar:deps/Citizens.jar" \
  -d classes \
  $(find src -name "*.java")

cp plugin.yml classes/
jar cf HaskShop.jar -C classes .
```

## License

[MIT](LICENSE) — © 2026 NetoTTT

---

*Built with assistance from [Claude](https://claude.ai) (Anthropic) — AI pair programming.*
