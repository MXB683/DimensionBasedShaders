![Available for Fabric](https://cdn.modrinth.com/data/cached_images/4e5bf78b0cb5c0aa53e4787ef7deef0fe0b6d599.webp)

## [Download on Modrinth](https://modrinth.com/mod/dimension-based-shaders)

# Dimension Based Shaders

**Automatically switch Iris shader packs per dimension.**  
Set one shader pack for the **Overworld**, another for the **Nether**, and a third for **The End**—then let the mod handle the rest whenever you join a world or change dimensions.

---

## Features

- **Per-dimension shader packs**
- **Automatic switching**
  - No manual pack swapping mid-session
- **Iris integration**
  - Applies the selected shader pack through Iris and reloads shaders as needed
- **Simple configuration**
  - Configure once, enjoy consistent visuals everywhere

---

## Configuration

A config file is stored at `config/dimension_based_shaders.json`, however it is recommended to use [ModMenu](https://modrinth.com/mod/modmenu) for an in-game visual settings screen.

### ModMenu integration
![Settings page with inputs for each dimension](https://cdn.modrinth.com/data/cached_images/98129108d94f2bc11bc81bc3e1ac1029fb7f28b4_0.webp)
The settings page (accesible through ModMenu) contains an input for each dimension. By specifying the **exact name** of the shader.

However, typing the whole name is tedious and could cause mispellings, so you can click "Get Current" to use to shader pack currently loaded.

---

## Requirements

- **Minecraft:** `1.21.11`
- **Mod Loader:** Fabric
- **Required:** Fabric API, Iris (therefore, also Sodium)
- **Recommended:** Mod Menu

---

## Notes / Known behavior

- Switching shader packs may (and most likely **will**) cause a **brief stutter** due to shader reload (depends on pack and hardware).
- If a configured shader pack name is missing or misspelled, Iris will unload all shader packs (other dimensions' packs will still work). Avoid this by clicking "Get Current".
- If you manually switch shaders in a dimension, the mod will switch again when entering another.

---

## Compatibility

- Client-side only (visual change).
- Generally compatible with common rendering/performance mods as long as Iris is present and working.

---

## FAQ

**Q: Does this work on servers?**  
A: Yes—this is client-side visual behavior. Servers don’t need to install anything.

**Q: Can I disable shaders in one dimension?**  
A: You can avoid using a shader by leaving the input field empty.

**Q: Do I need Mod Menu?**  
A: Not _required_, but recommended for convenient in-game configuration.

**Q: Does this mod support custom dimensions?**
A: Yes, however some dimensions may only appear in the configuration menu only if you are present in that dimension. However, if you change the setting, it will be preserved even when exiting the dimension.

---

## Credits

- **Iris Shaders** — required dependency and shader system  
- **Mod Menu** — optional settings integration

---

## Support / Issues

If you run into issues, please make sure you're using the correct:
- Minecraft version
- Fabric loader version
- Iris version
