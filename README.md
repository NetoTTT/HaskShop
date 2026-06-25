# HaskShop

A lightweight, fully GUI-driven shop sign plugin for **Minecraft 1.8.8 PaperSpigot**. Admins write any text they want on signs, register them as shops, and configure everything through in-game GUIs — no config file editing required.

## Features

- **Custom sign text** — the sign's appearance is 100% up to you; shop logic is stored separately
- **GUI-based configuration** — item, price, quantity, buy/sell mode, enable/disable, all in one chest GUI
- **Free quantity mode** — players choose how many units to buy/sell; a confirmation screen shows the total before charging
- **Spawner support** — sell mob spawners with the correct mob type via a visual mob picker (29 mobs, powered by NMS NBT)
- **Vault economy** — works with any Vault-compatible economy plugin (EssentialsX Economy, etc.)
- **Sign protection** — registered shop signs cannot be broken; admins must `/cs remove` first
- **Inventory space validation** — checks available space before opening the confirmation screen
- **Cooldown** — 2-second click cooldown per player to prevent spam

## Requirements

| Dependency | Version |
|---|---|
| PaperSpigot / Spigot | 1.8.8 |
| [Vault](https://github.com/milkbowl/Vault) | Any |
| Economy plugin | EssentialsX, etc. |

> **Spawner support** works natively via NMS. [SilkSpawners](https://github.com/timbru31/SilkSpawners) is compatible but not required.

## Installation

1. Drop `HaskShop.jar` into your `/plugins` folder
2. Make sure Vault and an economy plugin are loaded
3. Start (or reload) the server
4. Grant the `shopsign.admin` permission to staff

## Commands

| Command | Description |
|---|---|
| `/cs add` | Activate creation mode — right-click any sign to register it as a shop |
| `/cs info` | Activate info mode — right-click a shop sign to see its ID and settings |
| `/cs edit <ID>` | Open the configuration GUI for a shop |
| `/cs remove <ID>` | Unregister a shop (allows the sign to be broken afterwards) |
| `/cs list` | List all registered shops with their IDs and locations |

## Permissions

| Permission | Default | Description |
|---|---|---|
| `shopsign.admin` | OP | Create, edit, and remove shop signs |
| `shopsign.use` | Everyone | Use (buy/sell from) shop signs |

## How to Create a Shop Sign

1. **Place a sign** and write whatever you like (colors, custom text — fully free)
2. Run `/cs add`, then **right-click the sign** → shop is registered and assigned an ID
3. Run `/cs edit <ID>` to open the configuration GUI:

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

When enabled, instead of selling a fixed amount per click, the player is prompted to type how many units they want. A confirmation screen appears before any transaction:

```
[ CANCEL ]   [Item — 5x DIAMOND — Total: 25000 coins]   [ CONFIRM ]
```

The inventory space is validated before the confirmation screen opens.

## Spawner Shops

1. Set item to `MOB_SPAWNER`
2. Click the **MOB TYPE** slot that appears in the GUI
3. A 6-row GUI opens with all 29 available mobs as spawn eggs, color-coded:
   - **Red** — Hostile (Zombie, Skeleton, Blaze, Spider…)
   - **Yellow** — Neutral (Wolf, Bat, Ocelot)
   - **Green** — Passive (Pig, Cow, Horse, Villager…)
4. Click a mob to select it
5. Players receive a named spawner item: `Spawner » Zombie` with correct NBT

## Data Storage

Shops are persisted in `plugins/HaskShop/shops.yml`. Each entry stores the world, coordinates, item, price, amount, type, and optional spawner mob type.

```yaml
shops:
  1:
    world: world
    x: 100
    y: 64
    z: -50
    type: BUY
    item: DIAMOND
    amount: 1
    price: 5000.0
    enabled: true
    ask-quantity: false
```

## Building from Source

**Requirements:** JDK 8+, `spigot-1.8.8.jar`, `Vault.jar`

```bash
javac --release 8 -encoding UTF-8 \
  -cp "deps/spigot.jar:deps/Vault.jar" \
  -d classes \
  $(find src -name "*.java")

cp plugin.yml classes/
jar cf HaskShop.jar -C classes .
```

## License

[MIT](LICENSE) — © 2026 NetoTTT
