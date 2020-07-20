package dev.vrba.minecraftdeathmaze.generators

import org.bukkit.inventory.ItemStack
import org.bukkit.Material._
import org.bukkit.enchantments.Enchantment
import org.bukkit.enchantments.Enchantment._
import org.bukkit.inventory.meta.{Damageable, ItemMeta}

import scala.util.Random

object LootGenerator {

  private case class WeightedItem(stack: ItemStack, weight: Int)

  private implicit class ItemStackOps(stack: ItemStack) {
    def withRandomDurability: ItemStack = when(stack.getItemMeta) {
      case meta: Damageable => {
        meta.setDamage(Random.nextInt(stack.getType.getMaxDurability))
        stack.setItemMeta(meta.asInstanceOf[ItemMeta])
        stack
      }
      case _ => stack
    }

    def withWeight(weight: Int): WeightedItem = WeightedItem(stack, weight)

    def withLuck(luck: Int): ItemStack = if (Random.nextInt(luck) == 0) stack
    else new ItemStack(AIR)
  }

  private def weightedRandom(items: WeightedItem*): ItemStack = {
    val totalWeight = items.map(_.weight).sum
    val selected = Random.nextInt(totalWeight)

    var accumulator = 0

    items foreach { item =>
      if ((accumulator += item.weight) >= selected)
        return item.stack
    }

    // Default case, shouldn't happen but ya know
    new ItemStack(AIR)
  }

  def generateLootBox(): Array[ItemStack] =
    Array(
      randomArmor,
      randomWeapon,
      randomBonusItems
    ).flatten

  private def randomArmor: Array[ItemStack] =
    Array(
      weightedRandom(
        new ItemStack(LEATHER_HELMET) withWeight 12,
        new ItemStack(GOLDEN_HELMET) withWeight 8,
        new ItemStack(CHAINMAIL_HELMET) withWeight 6,
        new ItemStack(IRON_HELMET) withWeight 4,
        new ItemStack(DIAMOND_HELMET) withWeight 1,
      ),

      weightedRandom(
        new ItemStack(LEATHER_CHESTPLATE) withWeight 12,
        new ItemStack(GOLDEN_CHESTPLATE) withWeight 8,
        new ItemStack(CHAINMAIL_CHESTPLATE) withWeight 6,
        new ItemStack(IRON_CHESTPLATE) withWeight 4,
        new ItemStack(DIAMOND_CHESTPLATE) withWeight 1,
      ),

      weightedRandom(
        new ItemStack(LEATHER_LEGGINGS) withWeight 12,
        new ItemStack(GOLDEN_LEGGINGS) withWeight 8,
        new ItemStack(CHAINMAIL_LEGGINGS) withWeight 6,
        new ItemStack(IRON_LEGGINGS) withWeight 4,
        new ItemStack(DIAMOND_LEGGINGS) withWeight 1,
      ),

      weightedRandom(
        new ItemStack(LEATHER_BOOTS) withWeight 12,
        new ItemStack(GOLDEN_BOOTS) withWeight 8,
        new ItemStack(CHAINMAIL_BOOTS) withWeight 6,
        new ItemStack(IRON_BOOTS) withWeight 4,
        new ItemStack(DIAMOND_BOOTS) withWeight 1,
      ),
    )
      // There is a 4/5 chance, that the armor piece will turn into air
      .filter(_ => Random.nextInt(4) == 0)
      .map(enchant(_).withRandomDurability)

  private def randomWeapon: Array[ItemStack] = {
    val weapon = weightedRandom(
      new ItemStack(AIR) withWeight 6,
      new ItemStack(BOW) withWeight 2,
      new ItemStack(CROSSBOW) withWeight 1,
      weightedRandom(
        new ItemStack(WOODEN_SWORD) withWeight 9,
        new ItemStack(STONE_SWORD) withWeight 7,
        new ItemStack(GOLDEN_SWORD) withWeight 6,
        new ItemStack(IRON_SWORD) withWeight 4,
        new ItemStack(DIAMOND_SWORD) withWeight 1,
      ) withWeight 3,
      weightedRandom(
        new ItemStack(WOODEN_AXE) withWeight 9,
        new ItemStack(STONE_AXE) withWeight 7,
        new ItemStack(GOLDEN_AXE) withWeight 6,
        new ItemStack(IRON_AXE) withWeight 4,
        new ItemStack(DIAMOND_AXE) withWeight 1,
      ) withWeight 3
    )

    val stack = enchant(weapon).withRandomDurability

    // Add some arrows to chests with bow
    weapon.getType match {
      case BOW => Array(stack, new ItemStack(ARROW, Random.nextInt(10) + 3))
      case _ => Array(stack)
    }
  }

  private def randomBonusItems: Array[ItemStack] =
    Array(
      new ItemStack(COOKED_BEEF, 1 + Random.nextInt(4)) withLuck 5,
      new ItemStack(APPLE, 1 + Random.nextInt(4)) withLuck 5,
      new ItemStack(GOLDEN_APPLE, 1 + Random.nextInt(2)) withLuck 20,
      new ItemStack(ENCHANTED_GOLDEN_APPLE) withLuck 5,
      // TODO: pickaxes/magic?
    )

  private def enchant(item: ItemStack): ItemStack = {
    val bowEnchantments = Array(
      DURABILITY,
      ARROW_FIRE,
    )

    val axeEnchantments = Array(
      DAMAGE_ALL,
      DURABILITY,
    )

    val enchantments: Array[Enchantment] = item.getType match {
      // Armor enchantments
      case LEATHER_HELMET
           | GOLDEN_HELMET
           | CHAINMAIL_HELMET
           | IRON_HELMET
           | DIAMOND_HELMET
           | LEATHER_CHESTPLATE
           | GOLDEN_CHESTPLATE
           | CHAINMAIL_CHESTPLATE
           | IRON_CHESTPLATE
           | DIAMOND_CHESTPLATE
           | LEATHER_LEGGINGS
           | GOLDEN_LEGGINGS
           | CHAINMAIL_LEGGINGS
           | IRON_LEGGINGS
           | DIAMOND_LEGGINGS
           | LEATHER_BOOTS
           | GOLDEN_BOOTS
           | CHAINMAIL_BOOTS
           | IRON_BOOTS
           | DIAMOND_BOOTS => Array(
        PROTECTION_ENVIRONMENTAL,
        PROTECTION_PROJECTILE,
        PROTECTION_FIRE,
        DURABILITY,
        THORNS,
        BINDING_CURSE,
      )

      // Axe enchantments
      case WOODEN_AXE
           | STONE_AXE
           | GOLDEN_AXE
           | IRON_AXE
           | DIAMOND_AXE => axeEnchantments

      // Sword enchantments
      case WOODEN_SWORD
           | STONE_SWORD
           | GOLDEN_SWORD
           | IRON_SWORD
           | DIAMOND_SWORD => axeEnchantments ++ Array(KNOCKBACK, FIRE_ASPECT, SWEEPING_EDGE)

      // Bow enchantments
      case BOW => bowEnchantments

      // Crossbow enchantments
      case CROSSBOW => bowEnchantments ++ Array(QUICK_CHARGE, MULTISHOT, PIERCING)

      // Otherwise do not apply any enchantment
      case _ => return item
    }

    val enchantment = Random.shuffle(enchantments.toList).head

    item.addEnchantment(enchantment, 1 + Random.nextInt(enchantment.getMaxLevel))
    item
  }
}
