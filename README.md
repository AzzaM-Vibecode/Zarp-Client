# Zarp Client

Fondasi mod client-side untuk Minecraft **1.21.11** dengan Fabric. Mod ini menyediakan keybind awal yang bisa dikembangkan menjadi fitur-fitur Zarp berikutnya.

## Fitur saat ini

- Tekan **G** untuk mengganti status Zarp Client (aktif/nonaktif); status tampil sebagai pesan overlay dalam game.
- Berjalan sebagai mod client-side Fabric.

## Kebutuhan

- Java/JDK 21
- Minecraft 1.21.11
- Fabric Loader 0.18.1 atau lebih baru
- Fabric API 0.140.2+1.21.11
- Gradle 8.14 atau lebih baru

> Catatan: Fabric Loader `0.20.1` bukan versi loader resmi yang dipakai untuk Minecraft 1.21.11. Jika yang dimaksud Fabric API, versinya juga menggunakan format berbeda; gunakan dependency yang tercantum di atas.

## Build

Dari root repository, jalankan:

```sh
gradle build
```

JAR mod akan dibuat di `build/libs/`. Salin JAR tersebut ke folder `mods` bersama Fabric API. Pastikan launcher menggunakan profil Fabric untuk Minecraft 1.21.11.

## Keybind

Keybind **Toggle Zarp Client** dapat diubah dari **Options → Controls → Key Binds**. Default-nya adalah **G**.
