# Sack — BTA 8.0.1

A small portable-storage mod for Better Than Adventure! BTA 8.0.1 / Babric.

## Intended behavior

- Adds a non-stackable item named **Sack**.
- Right-clicking the held sack opens a chest-style inventory screen.
- Stores six inventory slots, with a maximum of 16 items per slot.
- Stores contents in a small custom payload injected into the individual sack ItemStack's NBT read/write path, so different sacks can keep independent inventories.
- Appears in the BTA creative inventory under the **Drops** category.
- Crafting recipe: three leather and one string (shapeless).
- Prevents putting a Sack item inside another Sack.
- Adds a shapeless recipe using three leather and one string.

## Target versions

- BTA: `8.0.1`
- Fabric Loader / Babric: `0.18.4-bta.11`
- HalpLibe: `6.1.3+8.0` (the version declared in the matching source project)
- Java bytecode target: Java 17

The Gradle wrapper is the one from the supplied Mo' Creatures BTA 8.0.1 source project. Its Gradle daemon/toolchain setup may use JDK 21 even though the mod is compiled for Java 17 and runs with Java 17. Set IntelliJ's **Gradle JVM** to JDK 21 if your Gradle version requires it; set the game/runtime Java to the version required by your BTA instance.

## Build

On a machine with network access and the required JDK/toolchain installed:

```bash
./gradlew build
```

On Windows:

```bat
gradlew.bat build
```

The mod JAR should be written to `build/libs/`.

## Build on GitHub Actions (online runner)

The `.github/workflows/build.yml` workflow runs `./gradlew build` on GitHub's hosted runner whenever code is pushed, and can also be started manually from the repository's **Actions** tab using **Build Sack Mod → Run workflow**. If the build succeeds, download the `sack-mod-jar` artifact from that workflow run. This uses GitHub's networked build environment; it does not imply the project has already compiled successfully.

## Install

Copy the resulting `sack-1.0.0+8.0.1.jar` into the `mods` directory for your BTA 8.0.1 Babric instance.

## Implementation notes

The source is based on the BTA 8.0.1 project scaffold and inventory conventions from the supplied Mo' Creatures repository. This revision addresses the API mismatches from the first GitHub Actions compiler log by avoiding old ItemStack tag accessor methods and the unsupported ContainerSimple.canPlaceItem override. The right-click hook and serialization Mixin must still be verified against BTA 8.0.1 at runtime. Copying sack contents avoids CompoundTag.copy(), which is not part of the target BTA API, and clones the saved item list through ItemStack's NBT read/write methods. The build could not be run in the disconnected build environment used to prepare this source archive: the Gradle wrapper failed while resolving `services.gradle.org`. As a result, the source is packaged but **not yet verified by a successful compilation or in-game test**. Check the first Gradle compiler error, if any, against your local mappings and report it to continue refining compatibility.


## Runtime fix after first test

If the game crashes during mixin application with `non-static callback method ... targets a static method` for `ItemStack.copyItemStack`, use the updated `SackItemStackMixin` from this source revision. The callback for this static method must itself be static.


## Runtime GUI fix (testing revision)

The interaction hook no longer cancels the client-side `ItemStack.useItemRightClick` call. It waits for the logical server to open the container, and the container uses BTA's standard `container.chest` identifier to select the built-in chest-style UI. A server-side log entry is emitted when the hook opens the sack. This revision requires a GitHub Actions build and in-game testing; it has not been compiled in this disconnected environment.


## Update 1.0.1

- Moves the sack's right-click behavior into `SackItem.onItemRightClick`, the BTA item-use callback.
- Emits client/server diagnostic log messages when the callback fires.
- Keeps persistent per-sack inventory NBT handling in the ItemStack mixin.
- Refines the 16x16 leather sack texture with a small highlighted tie and stitched fold.
- The shapeless recipe remains 3 leather + 1 string; the Sack remains in the Drops creative category.

Build with the project's GitHub Actions workflow. This revision has not been confirmed in-game yet.
