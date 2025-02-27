<p align="center"><img src="./.idea/icon.png" alt="Logo" width="200">
<h1 align="center">Ponder<br>
	<a href="https://www.patreon.com/simibubi"><img src="https://img.shields.io/endpoint.svg?url=https%3A%2F%2Fshieldsio-patreon.vercel.app%2Fapi%3Fusername%3Dsimibubi%26type%3Dpatrons&style=flat&label=Supporters&color=ff5733" alt="Patreon"></a>
	<a href="https://github.com/Creators-of-Create/Ponder/blob/master/LICENSE"><img src="https://img.shields.io/github/license/Creators-of-Create/Ponder?style=flat&color=900c3f" alt="License"></a>
	<a href="https://discord.gg/hmaD7Se"><img src="https://img.shields.io/discord/620934202875183104?color=5865f2&label=Discord&style=flat" alt="Discord"></a>
</h1>

<p>
Ponder is a library for creating interactive, in-game guides to help players understand content added by your mod.
Ponder allows you to craft detailed and engaging tutorials that showcase mod mechanics in a way that makes it easier
for players to learn how to use your mod.
</p>

## Project Setup

Add the Create maven to your `repositories` block in your build script
```groovy
repositories {
    maven { url = "https://maven.createmod.net" }
}
```

Add a property in your gradle.properties with the latest ponder version
which can be found [here](https://ci.createmod.net/job/createmod/job/Ponder/),
it should look like the following with the x replaced with the version you got from the link:

```properties
...
ponder_version = x
...
```

Then add ponder to your project based on the loader and minecraft version you are using.

<details>
<summary>MultiLoader</summary>

### Architectury MultiLoader 1.20.1/1.21.1
```groovy
dependencies {
    modImplementation("net.createmod.ponder:Ponder-Common-${minecraft_version}:${ponder_version}")
}
```

### Jared's MultiLoader Template 1.20.1/1.21.1
```groovy
dependencies {
    implementation("net.createmod.ponder:Ponder-Common-${minecraft_version}:${ponder_version}")
}
```
</details>

<details>
<summary>Fabric</summary>

### Fabric 1.20.1/1.21.1
```groovy
dependencies {
    modImplementation("net.createmod.ponder:Ponder-Fabric-${minecraft_version}:${ponder_version}")
}
```
</details>

<details>
<summary>Forge</summary>

### Forge 1.20.1 \w ForgeGradle
```groovy
dependencies {
    implementation fg.deobf("net.createmod.ponder:Ponder-Forge-${minecraft_version}:${ponder_version}")
}
```

### Forge 1.20.1 \w ModDevGradle
```groovy
dependencies {
    modImplementation("net.createmod.ponder:Ponder-Forge-${minecraft_version}:${ponder_version}")
}
```
</details>

<details>
<summary>NeoForge</summary>

### NeoForge 1.21.1 \w ModDevGradle
```groovy
dependencies {
    modImplementation("net.createmod.ponder:Ponder-NeoForge-${minecraft_version}:${ponder_version}")
}
```
</details>
