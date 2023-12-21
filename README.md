![Logo](media/logo.png)

# Smart Recipes

[![GitHub Build Status](https://img.shields.io/github/actions/workflow/status/Kir-Antipov/smart-recipes/build-artifacts.yml?style=flat&logo=github&cacheSeconds=3600)](https://github.com/Kir-Antipov/smart-recipes/actions/workflows/build-artifacts.yml)
[![Version](https://img.shields.io/github/v/release/Kir-Antipov/smart-recipes?sort=date&style=flat&label=version&cacheSeconds=3600)](https://github.com/Kir-Antipov/smart-recipes/releases/latest)
[![Modrinth](https://img.shields.io/badge/dynamic/json?color=00AF5C&label=Modrinth&query=title&url=https://api.modrinth.com/v2/project/smart-recipes&style=flat&cacheSeconds=3600&logo=modrinth)](https://modrinth.com/mod/smart-recipes)
[![CurseForge](https://img.shields.io/badge/dynamic/json?color=F16436&label=CurseForge&query=title&url=https://api.cfwidget.com/522139&cacheSeconds=3600&logo=curseforge)](https://www.curseforge.com/minecraft/mc-mods/smart-recipes)
[![License](https://img.shields.io/github/license/Kir-Antipov/smart-recipes?style=flat&cacheSeconds=36000)](https://github.com/Kir-Antipov/smart-recipes/blob/HEAD/LICENSE.md)

Do you find the standard Minecraft recipe format boring? Or maybe you wish to make the game more event-based, like Terraria? If so, `Smart Recipes` is what you need! Craft your own unique experience with our new conditional recipe format!

----

## Format

If you prefer a practical example over explanations, feel free to skip directly to the [Example](#example) section. However, don't forget to revisit the [Conditions](#conditions) and [Reload Conditions](#reload-conditions) sections later.

### Conditions Format

The `Smart Recipes` mod extends the [vanilla recipe format](https://minecraft.wiki/Recipe). It's crucial to understand how the vanilla format works; otherwise, the following descriptions may not make sense.

This mod introduces a new `smart_recipes:conditions` property to the recipe format. It's somewhat similar to the conditions in loot tables. Let's take a closer look it:

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

This example is identical to how conditions in loot tables are usually written. However, I find them too verbose. Let's explore how we can improve our conditions.

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

This is even better than before, but there is still some room for imporvement. Let's consolidate our conditions into a single object:

```jsonc
"smart_recipes:conditions": [
  {
    "conditionName0": [4, "2"],
    "conditionName1": ["foo"]
  }
]
```

Finally, if an array contains only one object, the `Smart Recipes` mod allows you to omit its declaration. Here's the final result:

```jsonc
"smart_recipes:conditions": {
  "conditionName0": [4, "2"],
  "conditionName1": "foo"
}
```

I should note that `Smart Recipes` can parse **every** single one of the examples above, so you can choose the style you prefer.

### Reload Conditions Format

Reload conditions allow recipe conditions to be re-evaluated on different occasions. It can be useful if your recipe relies on weather, time and/or other volatile entities.

This process is surprisingly straightforward. All you need to do is provide the names of the reload conditions that your recipe depends on:

```jsonc
"smart_recipes:reload_conditions": [
  "weather_changed",
  "time_changed"
]
```

----

## Conditions

### `false`

Doesn't require arguments, always returns `false`.

```jsonc
"smart_recipes:conditions": [
  "false"
]
```

### `true`

Doesn't require arguments, always returns `true`.

```jsonc
"smart_recipes:conditions": [
  "true"
]
```

### `or` and `any`

Return `true` if any of the specified conditions returns `true`.

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

Return `true` if all the specified conditions return `true`.

```jsonc
"smart_recipes:conditions": {
  "and": {
    "true": {},
    "false": null
  }
}
```

### `not` and `none`

Return `true` if none of the specified conditions return `true`.

```jsonc
"smart_recipes:conditions": {
  "not": {
    "false": null
  }
}
```

### `is_hardcore`

Doesn't require arguments, returns `true` if the server is in Hardcore mode.

```jsonc
"smart_recipes:conditions": {
  "not": "is_hardcore"
}
```

### `is_peaceful_difficulty`

Doesn't require arguments, returns `true` if the current difficulty is Peaceful.

```jsonc
"smart_recipes:conditions": {
  "or": [
    "is_peaceful_difficulty",
    "is_hardcore"
  ]
}
```

### `is_easy_difficulty`

Doesn't require arguments, returns `true` if the current difficulty is Easy.

```jsonc
"smart_recipes:conditions": {
  "not": ["is_easy_difficulty"]
}
```

### `is_normal_difficulty`

Doesn't require arguments, returns `true` if the current difficulty is Normal.

```jsonc
"smart_recipes:conditions": "is_normal_difficulty"
```

### `is_hard_difficulty`

Doesn't require arguments, returns `true` if the current difficulty is Hard.

```jsonc
"smart_recipes:conditions": {
  "is_hard_difficulty": null
}
```

### `difficulty_check`

Returns `true` if one of the specified values matches the current difficulty.

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

Doesn't require arguments, returns `true` if the default gamemode is Survival.

```jsonc
"smart_recipes:conditions": "is_survival"
```

### `is_creative`

Doesn't require arguments, returns `true` if the default gamemode is Creative.

```jsonc
"smart_recipes:conditions": ["is_creative"]
```

### `is_adventure`

Doesn't require arguments, returns `true` if the default gamemode is Adventure.

```jsonc
"smart_recipes:conditions": {
  "is_adventure": {}
}
```

### `is_spectator`

Doesn't require arguments, returns `true` if the default gamemode is Spectator.

```jsonc
"smart_recipes:conditions": {
  "not": "is_spectator"
}
```

### `gamemode_check`

Returns `true` if one of the specified values matches the default gamemode.

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

Returns `true` if one of the specified strings matches the current [weather in the Overworld dimension](https://minecraft.wiki/Weather).

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

Returns `true` if one of the specified strings matches the current [time in the Overworld dimension](https://minecraft.wiki/Daylight_cycle).

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

Returns `true` if all the specified players are online.

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

Returns `true` if all the specified blocks are registered.

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

Returns `true` if all the specified items are registered.

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

Returns `true` if all the specified block entities are registered.

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

Returns `true` if all the specified registry entries are registered.

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

Returns `true` if all the specified mods are loaded.

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

Reloads recipes when the current difficulty changes.

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

Reloads recipes when the current weather changes.

```jsonc
"smart_recipes:conditions": {
  "weather_check": "thunder"
},
"smart_recipes:reload_conditions": "weather_changed"
```

### `time_changed`

Reloads recipes when the current time changes.

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

Consider a scenario where we want to add a simplified TNT recipe:

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

Now, let's add some conditions. Here are our objectives:

 1) This recipe should only be available at `midnight` and `sunrise`, because nobody crafts explosives in the light of day.
 2) The recipe should only be available when the `weather is clear`, to prevent the gunpowder from getting damp.
 3) The recipe should `not` be available when `Vladimir is online`, as we don't trust Vladimir with deadly weapons.
 4) The recipe should only be available on the `hard` difficulty, because... why not?

Time to put our plan into action:

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
    // Note that `weather_check` usually consumes an array (just like `time_check` does),
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
    // These reload the recipe when a player joins or disconnects from the server.
    // I may combine these two into a single one in the future.
    "player_joined",
    "player_disconnected",

    // #4
    // Reloads the recipe when difficulty changes.
    // To be honest, I don't think you really need it,
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
 - Minecraft `1.19.x`
 - Fabric Loader `>=0.14.0`
 - Fabric API `>=0.55.2`

You can download the mod from:

 - [GitHub Releases](https://github.com/Kir-Antipov/smart-recipes/releases/) <sup><sub>(recommended)</sub></sup>
 - [Modrinth](https://modrinth.com/mod/smart-recipes)
 - [CurseForge](https://www.curseforge.com/minecraft/mc-mods/smart-recipes)
 - [GitHub Actions](https://github.com/Kir-Antipov/smart-recipes/actions/workflows/build-artifacts.yml) *(these builds may be unstable, but they represent the actual state of the development)*

## Using as a Dependency

You can include `Smart Recipes` in your mod to use the new conditional format without requiring players to download it separately.

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
