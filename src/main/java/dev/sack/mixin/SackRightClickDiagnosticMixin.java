package dev.sack.mixin;

import dev.sack.SackMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Diagnostic only. Minecraft Beta 1.7.3 (the base of BTA 8.0.1) handles both mouse buttons
 * through clickMouse(int button), with button 1 representing right-click. This probe only logs
 * right-clicks and does not cancel or otherwise change gameplay.
 */
@Mixin(targets = "net.minecraft.client.Minecraft", remap = false)
public abstract class SackRightClickDiagnosticMixin {
    @Inject(method = "clickMouse(I)V", at = @At("HEAD"), remap = false, require = 1)
    private void sack$rightClickHead(int button, CallbackInfo ci) {
        if (button != 1) return;
        Object mc = this;
        SackMod.LOGGER.info("SACK_DIAGNOSTIC: Minecraft.clickMouse(button=1) HEAD; {}", sack$describeHeldStack(mc));
    }

    @Inject(method = "clickMouse(I)V", at = @At("RETURN"), remap = false, require = 1)
    private void sack$rightClickReturn(int button, CallbackInfo ci) {
        if (button != 1) return;
        Object mc = this;
        Object screen = sack$readField(mc, "currentScreen", "screen", "currentGui");
        SackMod.LOGGER.info("SACK_DIAGNOSTIC: Minecraft.clickMouse(button=1) RETURN; currentScreen={}",
            screen == null ? "none/unresolved" : screen.getClass().getName());
    }

    private static String sack$describeHeldStack(Object mc) {
        Object player = sack$readField(mc, "thePlayer", "player", "localPlayer");
        if (player == null) player = sack$findFieldValueByTypeName(mc, "net.minecraft.core.entity.player.Player", ".Player");
        if (player == null) return "heldStack=UNKNOWN (player field not located reflectively)";

        Object inventory = sack$readField(player, "inventory", "playerInventory");
        if (inventory == null) inventory = sack$findFieldValueByTypeName(player, "Inventory", "inventory");
        if (inventory == null) return "player=" + player.getClass().getName() + "; heldStack=UNKNOWN (inventory not found)";

        Object stack = sack$invokeNoArg(inventory, "getCurrentItem", "getHeldItem", "getCurrentEquippedItem", "getSelectedItem");
        if (stack == null) {
            Object indexObj = sack$readField(inventory, "currentItem", "selected", "selectedSlot", "currentSlot");
            int index = indexObj instanceof Number ? ((Number) indexObj).intValue() : -1;
            Object stackArray = sack$readField(inventory, "mainInventory", "items", "hotbar", "mainItems");
            if (stackArray != null && stackArray.getClass().isArray() && index >= 0 && index < Array.getLength(stackArray)) {
                stack = Array.get(stackArray, index);
            }
        }
        if (stack == null) {
            return "player=" + player.getClass().getName() + "; inventory=" + inventory.getClass().getName()
                + "; heldStack=UNKNOWN; inventoryFields=" + sack$listFields(inventory.getClass());
        }

        Object id = sack$readField(stack, "itemID", "itemId", "id");
        Object count = sack$readField(stack, "stackSize", "count", "amount");
        return "heldStackClass=" + stack.getClass().getName() + "; itemId=" + id + "; count=" + count;
    }

    private static Object sack$readField(Object owner, String... names) {
        if (owner == null) return null;
        for (String name : names) {
            for (Class<?> type = owner.getClass(); type != null; type = type.getSuperclass()) {
                try {
                    Field field = type.getDeclaredField(name);
                    field.setAccessible(true);
                    return field.get(owner);
                } catch (Throwable ignored) { }
            }
        }
        return null;
    }

    private static Object sack$findFieldValueByTypeName(Object owner, String exactOrContains, String alsoContains) {
        if (owner == null) return null;
        for (Class<?> type = owner.getClass(); type != null; type = type.getSuperclass()) {
            for (Field field : type.getDeclaredFields()) {
                String fieldType = field.getType().getName();
                String fieldName = field.getName().toLowerCase();
                if (fieldType.equals(exactOrContains) || fieldType.contains(exactOrContains)
                    || (!alsoContains.isEmpty() && fieldName.contains(alsoContains.toLowerCase()))) {
                    try {
                        field.setAccessible(true);
                        Object value = field.get(owner);
                        if (value != null) return value;
                    } catch (Throwable ignored) { }
                }
            }
        }
        return null;
    }

    private static Object sack$invokeNoArg(Object owner, String... names) {
        if (owner == null) return null;
        for (String name : names) {
            for (Class<?> type = owner.getClass(); type != null; type = type.getSuperclass()) {
                for (Method method : type.getDeclaredMethods()) {
                    if (!method.getName().equals(name) || method.getParameterCount() != 0) continue;
                    try {
                        method.setAccessible(true);
                        Object result = method.invoke(owner);
                        if (result != null) return result;
                    } catch (Throwable ignored) { }
                }
            }
        }
        return null;
    }

    private static String sack$listFields(Class<?> type) {
        StringBuilder out = new StringBuilder();
        for (Field field : type.getDeclaredFields()) {
            if (out.length() > 0) out.append(", ");
            out.append(field.getName()).append(":").append(field.getType().getSimpleName());
        }
        return out.toString();
    }
}
