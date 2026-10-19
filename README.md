# Sack — BTA 8.0.1 (diagnostic build 1.0.6)

This build is intended to find which stage of right-click handling is failing. It is **not a confirmed GUI fix**.

## Diagnostic behavior

When you right-click in the world, the client mixin logs entry and return of `Minecraft.rightClickMouse`, attempts to report the selected hotbar stack, and logs the active screen. Additional hooks log if `ItemStack.useItemRightClick` or `Item.onUseItem` is reached. These hooks do not cancel normal game behavior.

After installing this diagnostic build:
1. Load a world.
2. Put the Sack in the selected hotbar slot.
3. Aim at empty air and right-click once.
4. Close the game and send the `latest.log` lines containing `SACK_DIAGNOSTIC`.

Interpretation:
- No `Minecraft.rightClickMouse HEAD`: the client input mixin isn't running (or the click isn't being processed in-world); inspect startup mixin application.
- `Minecraft.rightClickMouse` logs but held stack isn't item ID 18000: either the selected slot isn't the Sack or held-stack introspection couldn't resolve; logs include extra field information where possible.
- Click log shows item ID 18000 but no `ItemStack.useItemRightClick` or `Item.onUseItem`: the click's current game path isn't dispatching either expected stack/item use entry point, which means we should target the actual branch identified by the next log.
- `Item.onUseItem` logs but no menu: inspect the client/server side and container-opening line after it.

## Functional content retained

- Sack item, creative Drops category, item texture, shapeless crafting recipe (3 leather + 1 string).
- Six slots, maximum stack size 16, and per-stack NBT storage logic.

## Build and install

Build on GitHub Actions with `./gradlew build`, then download the `sack-mod-jar` artifact. Remove the older Sack JAR before installing this one. Expected version: `sack-1.0.6+8.0.1`.

This source revision has not been compiled in this environment; use GitHub Actions to verify compilation before testing.
