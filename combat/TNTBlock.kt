/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.client.EntityLookup
import net.minecraft.client.settings.GameSettings
import net.minecraft.entity.item.EntityTNTPrimed
import net.minecraft.item.ItemSword

object TNTBlock : Module("TNTBlock", Category.COMBAT, spacedName = "TNT Block") {
    private val fuse by int("Fuse", 10, 0..80)
    private val range by float("Range", 9F, 1F..20F)
    private val autoSword by boolean("AutoSword", true)
    private var blocked = false

    private val entities by EntityLookup<EntityTNTPrimed>()
        .filter { it.fuse <= fuse }
        .filter { mc.thePlayer.getDistanceSqToEntity(it) <= range * range }

    val onMotion = handler<MotionEvent> {
        val thePlayer = mc.thePlayer ?: return@handler
        val theWorld = mc.theWorld ?: return@handler

        for (entity in entities) {
            if (autoSword) {
                var slot = -1
                var bestDamage = 1f
                for (i in 0..8) {
                    val itemStack = thePlayer.inventory.getStackInSlot(i)

                    if (itemStack?.item is ItemSword) {
                        val itemDamage = (itemStack.item as ItemSword).damageVsEntity + 4F

                        if (itemDamage > bestDamage) {
                            bestDamage = itemDamage
                            slot = i
                        }
                    }
                }

                if (slot != -1 && slot != thePlayer.inventory.currentItem) {
                    thePlayer.inventory.currentItem = slot
                    mc.playerController.syncCurrentPlayItem()
                }
            }

            if (mc.thePlayer.heldItem?.item is ItemSword) {
                mc.gameSettings.keyBindUseItem.pressed = true
                blocked = true
            }

            return@handler
        }

        if (blocked && !GameSettings.isKeyDown(mc.gameSettings.keyBindUseItem)) {
            mc.gameSettings.keyBindUseItem.pressed = false
            blocked = false
        }
    }
}