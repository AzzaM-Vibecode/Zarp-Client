# Zarp Client 1.0.0 — Zarp Finder

Mod Minecraft Fabric — ClickGUI client dengan settings framework dan 4 module Combat.

- **Minecraft:** 1.21.11
- **Fabric Loader:** 0.20.1
- **Fabric API:** 0.141.6+1.21.11
- **Loom:** 1.19-SNAPSHOT (`net.fabricmc.fabric-loom-remap`)
- **Java:** 21
- **Lisensi:** GPL-3.0

## Fitur

- ClickGUI 2D (buka dengan **Right Shift**) — logo, kategori Combat/Movement/DonutSMP/Visuals/Misc + Settings/Configs/Theme/Socials
- Klik kanan card module = buka panel setting (toggle, slider, mode)
- Config JSON otomatis (save/load di tab Configs)
- Tab Theme untuk ganti warna aksen
- Module batch 1: W-Tap, Triggerbot, Aim Assist, Auto Totem

## Build

```bash
gradlew build --stacktrace   # Windows
./gradlew build --stacktrace # Linux/macOS
```

Hasil: `build/libs/zarpfinder-1.0.0.jar` → taruh di folder `mods` bersama Fabric API.

## Struktur

```
Zarp-Client/
├── settings.gradle, build.gradle, gradle.properties, README.md
├── gradlew, gradlew.bat, gradle/wrapper/   (tambahkan dari fabric-example-mod branch 1.21.11)
└── src/main/
    ├── java/zarpfinder/         (ZarpFinderMod, Theme, ConfigManager)
    │   ├── gui/ClickGuiScreen
    │   └── module/ (Category, Module, ModuleManager,
    │       setting/ ×4, impl/combat/ ×4)
    └── resources/ (fabric.mod.json, assets/zarpfinder/lang + textures/logo.png)
```

## Sebelum build — tambahkan 2 hal

1. **Gradle wrapper**: copy `gradlew`, `gradlew.bat`, dan folder `gradle` dari [fabric-example-mod branch 1.21.11](https://github.com/FabricMC/fabric-example-mod/tree/1.21.11)
2. **Logo**: `src/main/resources/assets/zarpfinder/textures/logo.png` (PNG persegi, mis. 128×128)

## TODO

- [ ] Porting batch 2 Combat (ShieldBreaker, FastXp, JumpReset, KeyThrow, HoverTotem, FriendGuard, Mace, SpearLunge, KillAura)
- [ ] Porting Movement → Visuals → DonutSMP → Misc
- [ ] Keybind per module
