![Logo](media/logo.png)

# Smart Recipes
[![GitHub tag](https://img.shields.io/github/v/tag/Kir-Antipov/smart-recipes.svg?cacheSeconds=3600&sort=date)](https://github.com/Kir-Antipov/smart-recipes/releases/latest)
[![GitHub build status](https://img.shields.io/github/actions/workflow/status/Kir-Antipov/smart-recipes/build-artifacts.yml?branch=1.19.x/dev&cacheSeconds=3600)](https://github.com/Kir-Antipov/smart-recipes/actions/workflows/build-artifacts.yml)
[![Modrinth](https://img.shields.io/badge/dynamic/json?color=00AF5C&label=Modrinth&query=title&url=https://api.modrinth.com/v2/project/smart-recipes&style=flat&cacheSeconds=3600&logo=modrinth)](https://modrinth.com/mod/smart-recipes)
[![CurseForge](https://img.shields.io/badge/dynamic/json?color=%23f16436&label=CurseForge&query=title&url=https%3A%2F%2Fapi.cfwidget.com%2F522139&cacheSeconds=3600&logo=curseforge)](https://www.curseforge.com/minecraft/mc-mods/smart-recipes)
[![GitHub license](https://img.shields.io/github/license/Kir-Antipov/smart-recipes.svg?cacheSeconds=36000)](https://github.com/Kir-Antipov/smart-recipes#readme)

Have you ever found the standard Minecraft recipe format boring? Or have you ever wanted to make the game a little more event-based, like Terraria? Then `Smart Recipes` is what you need! Cook your own unique experience with the new conditional recipe format!

----

## Format

*If you prefer a working example to explanations, you can skip straight to the [Example](#example) section. Don't forget to revisit [Conditions](#conditions) and [Reload Conditions](#reload-conditions) when you're done, though.*

### Conditions Format

`Smart Recipes` mod extends [vanilla recipe format](https://minecraft.fandom.com/wiki/Recipe), so make sure that you understand how it works; otherwise, everything I describe here won't make any sense.

This mod adds brand new `smart_recipes:conditions` property to the recipe format. It's similar*(-ish)* to loot tables' conditions. Let's take a look at it:

```jsonc
"smart_recipes:conditions": []
```

This property determines conditions for the recipe to be loaded. If multiple conditions are specified, all must pass.

```jsonc
"smart_recipes:conditions": [
  {
    "condition": "conditionName0",
    "args": [
      // Arguments to be passed to some condition named `conditionName0`.
      4,
      "2"
    ]
  },
  {
    "condition": "conditionName1",
    "args": [
      // Arguments to be passed to some condition named `conditionName1`.
      "foo"
    ]
  }
]
```

Exactly the same as loot tables' conditions. But do you know what the problem is? I completely dislike them, because they're too verbose. How can we make things better?

```jsonc
// Let's turn this condition declaration:
{
  "condition": "conditionName1",
  "args": [
    "foo"
  ]
}

// Into this one:
"conditionName1": ["foo"]
```

Much better, don't you think?

```jsonc
"smart_recipes:conditions": [
  {
    "conditionName0": [4, "2"]
  },
  {
    "conditionName1": ["foo"]
  }
]
```

Better, but still can be better! Let's combine our conditions into a single object:

```jsonc
"smart_recipes:conditions": [
  {
    "conditionName0": [4, "2"],
    "conditionName1": ["foo"]
  }
]
```

And here goes one last thing. If an array contains only one object, the `Smart Recipes` mod allows you to omit its declaration! So here's the final result:

```jsonc
"smart_recipes:conditions": {
  "conditionName0": [4, "2"],
  "conditionName1": "foo"
}
```

I should note, that `Smart Recipes` can parse **every** single one of the examples above, so you can choose whichever style you like best.

### Reload Conditions Format

Reload conditions allow recipe conditions to be re-evaluated on some occasions. It can be useful if your recipe relies on weather, time and/or other volatile entities.

There's nothing complex here at all. Everything you need to do is provide names of the available reload conditions that your recipe depends on:

```jsonc
"smart_recipes:reload_conditions": [
  "weather_changed",
  "time_changed"
]
```

----

## Conditions

### `false`

Doesn't require arguments, always returns false.

```jsonc
"smart_recipes:conditions": [
  "false"
]
```

### `true`

Doesn't require arguments, always returns (you guessed it) true.

```jsonc
"smart_recipes:conditions": [
  "true"
]
```

### `or` and `any`

Return true if any of the specified conditions returns true.

```jsonc
"smart_recipes:conditions": {
  "or": [
    { "false": {} },
    false,
    "true"
  ]
}
```

### `and` and `all`

Return true if all the specified conditions return true.

```jsonc
"smart_recipes:conditions": {
  "and": {
    "true": {},
    "false": null
  }
}
```

### `not` and `none`

Return true if none of the specified conditions return true.

```jsonc
"smart_recipes:conditions": {
  "not": {
    "false": null
  }
}
```

### `is_hardcore`

Doesn't require arguments, returns true if the server is in the hardcore mode.

```jsonc
"smart_recipes:conditions": {
  "not": "is_hardcore"
}
```

### `is_peaceful_difficulty`

Doesn't require arguments, returns true if the current difficulty is peaceful.

```jsonc
"smart_recipes:conditions": {
  "or": [
    "is_peaceful_difficulty",
    "is_hardcore"
  ]
}
```

### `is_easy_difficulty`

Doesn't require arguments, returns true if the current difficulty is easy.

```jsonc
"smart_recipes:conditions": {
  "not": ["is_easy_difficulty"]
}
```

### `is_normal_difficulty`

Doesn't require arguments, returns true if the current difficulty is normal.

```jsonc
"smart_recipes:conditions": "is_normal_difficulty"
```

### `is_hard_difficulty`

Doesn't require arguments, returns true if the current difficulty is hard.

```jsonc
"smart_recipes:conditions": {
  "is_hard_difficulty": null
}
```

### `difficulty_check`

Returns true if one of the specified values matches the current difficulty.

```jsonc
"smart_recipes:conditions": {
  "difficulty_check": [
    0, // Ordinal number of the Peaceful difficulty
    1, // Ordinal number of the Easy difficulty
    2, // Ordinal number of the Normal difficulty
    3, // Ordinal number of the Hard difficulty
    "peaceful",
    "easy",
    "normal",
    "HarD" // Case doesn't matter
  ]
}
```

### `is_survival`

Doesn't require arguments, returns true if the default gamemode is survival.

```jsonc
"smart_recipes:conditions": "is_survival"
```

### `is_creative`

Doesn't require arguments, returns true if the default gamemode is creative.

```jsonc
"smart_recipes:conditions": ["is_creative"]
```

### `is_adventure`

Doesn't require arguments, returns true if the default gamemode is adventure.

```jsonc
"smart_recipes:conditions": {
  "is_adventure": {}
}
```

### `is_spectator`

Doesn't require arguments, returns true if the default gamemode is spectator.

```jsonc
"smart_recipes:conditions": {
  "not": "is_spectator"
}
```

### `gamemode_check`

Returns true if one of the specified values matches the default gamemode.

```jsonc
"smart_recipes:conditions": {
  "gamemode_check": [
    0, // Ordinal number of the Survival gamemode
    1, // Ordinal number of the Creative gamemode
    2, // Ordinal number of the Adventure gamemode
    3, // Ordinal number of the Spectator gamemode
    "survival",
    "creative",
    "adventure",
    "SpEcTaToR" // Case doesn't matter
  ]
}
```

### `weather_check`

Returns true if one of the specified strings matches the current [weather in the Overworld dimension](https://minecraft.fandom.com/wiki/Weather).

```jsonc
"smart_recipes:conditions": {
  "weather_check": [
    "clear",    // Weather is clear
    "rain",     // It's raining or thundering
    "thunder"   // It's thundering
  ]
}
```

### `time_check`

Returns true if one of the specified strings matches the current [time in the Overworld dimension](https://minecraft.fandom.com/wiki/Daylight_cycle).

```jsonc
"smart_recipes:conditions": {
  "time_check": [
    "day",      // from 1000  to 12999 ticks
    "noon",     // from 5000  to 6999  ticks
    "sunset",   // from 11000 to 12999 ticks
    "midnight", // from 17000 to 18999 ticks
    "sunrise",  // from 22000 to 23999 ticks
    "night"     // from 13000 to 23999 ticks, from 0 to 999 ticks
  ]
}
```

### `players_online`

Returns true if all the specified players are online.

```jsonc
"smart_recipes:conditions": {
  "players_online": [
    "Notch",
    "Dinnerbone",
    "jeb_"
  ]
}
```

### `blocks_registered`

Returns true if all the specified blocks are registered.

```jsonc
"smart_recipes:conditions": {
  "blocks_registered": [
    "stone",
    "minecraft:dirt",
    "aether:aether_dirt"
  ]
}
```

### `items_registered`

Returns true if all the specified items are registered.

```jsonc
"smart_recipes:conditions": {
  "items_registered": [
    "stone",
    "minecraft:dirt",
    "aether:aether_dirt"
  ]
}
```

### `block_entities_registered`

Returns true if all the specified block entities are registered.

```jsonc
"smart_recipes:conditions": {
  "block_entities_registered": [
    "furnance",
    "minecraft:chest",
    "aether:incubator"
  ]
}
```

### `entries_registered`

Returns true if all the specified registry entries are registered.

```jsonc
"smart_recipes:conditions": {
  "entries_registered": [
    {
      "registry": "block",
      "entry": "stone"
    },
    "minecraft:dirt", // `registry`'s default value is "block",
    {
      "registry": "item",
      "entry": "aether:incubator"
    },
    {
      "entry": "air"  // `registry`'s default value is "block",
    }
  ]
}
```

### `fabric:mods_loaded`

Returns true if all the specified mods are loaded.

```jsonc
"smart_recipes:conditions": {
  "fabric:mods_loaded": [
    {
      "id": "smart-recipes",
      "version": "1.0"
    },
    "fabric-transfer-api-v1",       // `version`'s default value is "*"
    {
      "id": "fabric-command-api-v1" // `version`'s default value is "*"
    }
  ]
}
```

## Reload Conditions

### `player_joined` and `player_disconnected`

Reload recipes when player joins to/disconnects from the server.

```jsonc
"smart_recipes:conditions": {
  "not": {
    "players_online": "Nickname"
  }
},
"smart_recipes:reload_conditions": [
  "player_joined",
  "player_disconnected"
]
```

### `difficulty_changed`

Reloads recipes when difficulty changes.

```jsonc
"smart_recipes:conditions": "is_easy_difficulty",
"smart_recipes:reload_conditions": "difficulty_changed"
```

### `gamemode_changed`

Reloads recipes when the default gamemode changes.

```jsonc
"smart_recipes:conditions": {
  "gamemode_check": [
    "survival",
    "creative"
  ]
},
"smart_recipes:reload_conditions": ["gamemode_changed"]
```

### `weather_changed`

Reloads recipes when weather changes.

```jsonc
"smart_recipes:conditions": {
  "weather_check": "thunder"
},
"smart_recipes:reload_conditions": "weather_changed"
```

### `time_changed`

Reloads recipes when time changes.

```jsonc
"smart_recipes:conditions": {
  "time_check": [
    "noon",
    "midnight"
  ]
},
"smart_recipes:reload_conditions": [
  "time_changed"
]
```

----

## Example

Let's imagine that we want to add a simplified TNT recipe:

```jsonc
{
  "type": "minecraft:crafting_shaped",
  "pattern": [
    "# X",
    "   ",
    "X #"
  ],
  "key": {
    "#": {
      "item": "minecraft:sand"
    },
    "X": {
      "item": "minecraft:gunpowder"
    }
  },
  "result": {
    "item": "minecraft:tnt"
  }
}
```

Now it's time to add some sweet conditions. Here are our goals:

 1) This recipe should be available only at `midnight` and `sunrise`, 'cause nobody crafts explosives in the light of day
 2) The recipe should be available only when `weather's clear`, 'cause we don't want our gunpowder to get damp
 3) The recipe should `not` be available when `Vladimir is online`, 'cause we don't trust Vladimir with deadly weapons
 4) The recipe should be available only on the `hard` difficulty, 'cause... why not?

And from words to deeds:

```jsonc
{
  // The same part as above, nothing interesting to see here
  "type": "minecraft:crafting_shaped",
  "pattern": [
    "# X",
    "   ",
    "X #"
  ],
  "key": {
    "#": {
      "item": "minecraft:sand"
    },
    "X": {
      "item": "minecraft:gunpowder"
    }
  },
  "result": {
    "item": "minecraft:tnt"
  },

  // Here's where the fun begins.
  // We need to describe our conditions for this recipe to be loaded in the
  // `conditions` section. If multiple conditions are specified, all must pass.
  "smart_recipes:conditions": {
    // #1
    "time_check": [
      "midnight",
      "sunrise"
    ],

    // #2
    // Note, that `weather_check` usually consumes an array (just like `time_check` does),
    // but since we need to pass only one value, it can be omitted.
    "weather_check": "clear",

    // #3
    "not": {
      // We can omit an array declaration here too, but
      // I left it just to show that it's not necessary.
      "players_online": ["Vladimir"]
    },

    // #4
    // Can be replaced with:
    // - `"is_hard_difficulty": null`
    // - `"is_hard_difficulty"` (in arrays)
    // - `{ "condition": "is_hard_difficulty" }` (in arrays)
    // - `{ "condition": "difficulty_check", "args": ["hard"] }` (in arrays)
    "difficulty_check": "hard"
  },

  // By default, conditions are evaluated only when the server starts.
  // So, if we want to keep our recipes up-to-date with the actual
  // state of the game, we need to add `reload_conditions`.
  "smart_recipes:reload_conditions": [
    // #1
    // Reloads the recipe when time changes.
    "time_changed",

    // #2
    // Reloads the recipe when weather changes.
    "weather_changed",

    // #3
    // These reload the recipe when player joins to/disconnects from the server.
    // I may combine these two into a single one in the future.
    "player_joined",
    "player_disconnected",

    // #4
    // Reloads the recipe when difficulty changes.
    // Tbh, I don't think you really need it,
    // because difficulty is locked on most servers,
    // so it's fine to evaluate difficulty-based conditions
    // only on server start.
    "difficulty_changed"
  ]
}
```

----

## Installation

Requirements:
 - Minecraft `1.18.x`
 - Fabric Loader `>=0.12.0`
 - Fabric API `>=0.43.1`

You can download the mod from:

 - [GitHub Releases](https://github.com/Kir-Antipov/smart-recipes/releases/) <sup><sub>(recommended)</sub></sup>
 - [Modrinth](https://modrinth.com/mod/smart-recipes)
 - [CurseForge](https://www.curseforge.com/minecraft/mc-mods/smart-recipes)
 - [GitHub Actions](https://github.com/Kir-Antipov/smart-recipes/actions/workflows/build-artifacts.yml) *(these builds may be unstable, but they represent the actual state of the development)*

## Using as a dependency

You can include `Smart Recipes` into your mod to use the new conditional format without forcing players to download it separately.

`build.gradle`:

```groovy
repositories {
    maven {
        name = "Modrinth"
        url = "https://api.modrinth.com/maven"
        content {
            includeGroup "maven.modrinth"
        }
    }
}

dependencies {
    modImplementation "maven.modrinth:smart-recipes:${project.smart_recipes_version}"
}
```

`gradle.properties`:

```properties
smart_recipes_version=/* latest version goes here */
```

## Building from sources

Requirements:
 - JDK `17`

### Linux/MacOS

```cmd
git clone https://github.com/Kir-Antipov/smart-recipes.git
cd smart-recipes

chmod +x ./gradlew
./gradlew build
cd build/libs
```
### Windows

```cmd
git clone https://github.com/Kir-Antipov/smart-recipes.git
cd smart-recipes

gradlew build
cd build/libs
```
