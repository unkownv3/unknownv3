# Unknownv3's Bizzare SMP

A Minecraft 1.21.1 Paper plugin that gives each armor trim unique, powerful abilities.

## How It Works

Wear any armor piece with an armor trim applied. The **first trim found** on your armor determines your active ability set. Use your **right-click (Use Ability)** to activate your trim's special ability.

## Armor Trims & Abilities

### Silence Armor Trim
- **Sonic Boom** - Fire a warden blast. Hit 3 blasts on a player to become a Warden for 25 seconds
- **Unnoticeable** - You create no sounds and won't be noticed by Sculk Sensors or Wardens

### Eye Armor Trim
- **Piercing Stare** - Fire a beam that hurts targets and knocks them back
- **All-Seeing Eye** - After hitting a player with the beam, ender eyes track that player for 300 seconds

### Snout Armor Trim
- **Chop Lop** - Throw a golden axe that breaks shields and deals damage
- **Midas Touch** - Players you kill turn into a pile of gold

### Rib Armor Trim
- **Rib Rods** - Fire a soul-fireball with up to 2 charges, igniting yourself
- **Flaming Spirit** - Soul fire heals you instead of damaging you
- **Roast Resistant** - Immune to fire and lava damage

### Flow Armor Trim
- **Air Bubble** - Summon/despawn a rideable air bubble. Despawning heals. While riding, use ability for air burst
- **Double Jump** - You can double jump!
- **Projectile Immunity** - Immune to projectile damage
- *(Only trim that can use the mace)*

### Spire Armor Trim
- **Elytra Equipped** - Always able to use elytra, no kinetic damage
- **Shulker Strike** - Attacks summon a shulker bullet that causes levitation
- **Shadow Clone** - Summon clones that run in random directions

### Bolt Armor Trim
- **Thunderbolt** - Strike lightning that stuns targets, 3 charges with 16s recharge
- **Overclock** - Passive speed boost

### Host Armor Trim
- **Disguise Kit** - Disguise as another player (name your armor to set target)
- **Never Online** - Your name won't appear in the player list
- **Silent Arrival** - No join or leave messages

### Raiser Armor Trim
- **Remote Detonation** - Throw a bomb, right-click while looking at it to detonate (no self damage)
- **Delicate Drop** - Crouching gives slow falling

### Shaper Armor Trim
- **Shape Shifty** - Look up + right-click to grow, look down + right-click to shrink

### Tide Armor Trim
- **Riptide Rush** - Launch yourself forward with up to 3 charges
- **Splash Landing** - No fall damage
- **Aquatic Affinity** - Breathe underwater indefinitely

### Ward Armor Trim
- **Sound Power** - Activate for a huge boost to stats (Strength, Speed, Resistance, Regeneration)
- **Blending In** - Mobs won't target you

### Dune Armor Trim
- **Burrow** - Go underground for 5 seconds. Walk fast on shovel blocks, burrow even faster
- **Emergence** - Emerging stuns nearby players (7 blocks) for 2 seconds and deals 1.3 hearts
- **Dynamite** - Load a crossbow with TNT to fire dynamite

### Wayfinder Armor Trim
- **Clever Cloaking** - Become truly invisible to other players for 10 seconds (15s recharge)
- **Trail Tracking** - Compass on crosshair pointing at nearby players

### Coast Armor Trim
- **Wave** - Throw a wave forward, pushing and hurting targets
- **Waterwalking** - Walk quickly on water. Sneak to sink

### Wild Armor Trim
- **Vine Hook** - Shoot a vine that hooks entities for 5 seconds with Poison 2
- **Grabber** - While hooked: left-click pulls them to you, right-click pulls you to them
- **Jungle Immunity** - Immune to debuff status effects

### Vex Armor Trim
- **Fangfooted** - Toggle a trail of evoker fangs behind you
- **Thorned Strike** - Fully charged melee attacks summon an evoker fang
- **Allay Companion** - An allay follows you, heals you at low HP, and lets you glide

### Sentry Armor Trim
- **Ravager** - Summon/despawn a rideable Ravager. While riding, use ability to summon a Raid Party
- **Hero** - Hero of the Village 10 at all times

## Requirements

- Paper 1.21.1+
- Java 21

## Building

```bash
mvn package
```

The plugin JAR will be in `target/Unknownv3sBizzareSMP-1.0.0.jar`.

## Installation

Drop the JAR into your server's `plugins/` folder and restart.
