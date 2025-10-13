# Phoenix Phantoms
Stärkere Phantome die nach längerer Zeit ohne Schlaf spawnen.
- Wann spawnen sie? Nach 9 Minecraft-Tagen
- Jeden weiteren Tag werden sie stärker
- Command um Wachtimer hochzusetzen

### Commands
- `/awake <days>` - Stellt die Anzahl der Tage ohne Schlaf ein für einen selber

### Permissions
- `phoenixphantoms.admin` - Berechtigung um den Command zu nutzen

### conig.yml

```yaml
spawn_start: 9 # ab wieviel Minecraft-Tagen die Phantome anfangen zu spawnen
debug: false # ob Debug-Nachrichten im Chat geschrieben werden sollen
upgrades:
  1: # Ab 1 Tag drüber
    # Alle Eigenschaften sind optional
    name: "Kleines Phantom"
    speed: 1
    health: 9
  2: # Ab 2 Tagen drüber...
  5: # Ab 5 Tagen drüber...
    name: "Großes Phantom"
    speed: 2
    health: 20 #int
    damage: 15 #int
    knockback: 2
    size: 3 #int
    effects:
      fire_resistance: 1 #Level des Effekts. Namen entsprechen den PotionEffectTypes
      #...
    loot: # Alles an loot muss in der config definiert sein (bis auf default)
      - default # Erlaubt das Standard Loot droppen kann
      - diamond # 100% chance 2 diamonds zu kriegen; siehe unten
      - debug_stick:0.1 # 10% chance einen debug stick zu kriegen

# Hier wird alles definiert was als Loot droppen kann, per namen verwendet man es
loot:
  debug_stick:
    ==: org.bukkit.inventory.ItemStack
    v: 4189
    type: DEBUG_STICK
    meta:
      ==: ItemMeta
      meta-type: UNSPECIFIC
      display-name: '{"text":"Debug Stick","bold":true,"color":"yellow"}'
      lore:
        - '{"text":"_","color":"gray"}'
  diamond:
    ==: org.bukkit.inventory.ItemStack
    v: 4189
    type: DIAMOND
    amount: 2
```
