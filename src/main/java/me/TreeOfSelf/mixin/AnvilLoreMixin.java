package me.TreeOfSelf.mixin;

import me.TreeOfSelf.PandaUniqueItem;
import me.TreeOfSelf.TextFormattingHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Mixin(AnvilMenu.class)
public abstract class AnvilLoreMixin {
	private List<Component> panda$generateLore(Player player) {
		List<Component> lore = new ArrayList<>();

		long unixTimestamp = Instant.now().getEpochSecond();

		LocalDate currentDate = Instant.ofEpochSecond(unixTimestamp)
				.atZone(ZoneId.systemDefault())
				.toLocalDate();

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
		String formattedDate = currentDate.format(formatter);

		String playerName = player.getName().getString();

		if (PandaUniqueItem.config != null && PandaUniqueItem.config.loreFormat != null) {
			for (String line : PandaUniqueItem.config.loreFormat) {
				String processedLine = line
						.replace("%player_name%", playerName)
						.replace("%date%", formattedDate);

				Component formattedText = TextFormattingHelper.formatTextWithCustomCodes(processedLine);
				lore.add(formattedText.copy().setStyle(formattedText.getStyle().withItalic(false)));
			}
		} else {
			lore.add(Component.empty());
			lore.add(Component.literal("Forged by " + playerName).withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE)));
			lore.add(Component.literal(formattedDate).withStyle(Style.EMPTY.withColor(ChatFormatting.GOLD)));
		}

		return lore;
	}

	@Inject(method = "createResult", at = @At("RETURN"))
	private void panda$applyResultLore(CallbackInfo ci) {
		ItemCombinerMenuAccessor access = (ItemCombinerMenuAccessor) (Object) this;
		ItemStack result = access.panda$resultSlots().getItem(0);
		if (!result.isEmpty()) {
			Player player = access.panda$player();
			List<Component> lore = panda$generateLore(player);
			result.set(DataComponents.LORE, new ItemLore(lore));
		}
	}

	@Inject(method = "setItemName", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/AnvilMenu;createResult()V", shift = At.Shift.BEFORE))
	private void panda$renamePreviewLore(String name, CallbackInfoReturnable<Boolean> cir) {
		Slot slot = ((AnvilMenu) (Object) this).getSlot(2);
		if (slot.hasItem()) {
			ItemStack stack = slot.getItem();
			Player player = ((ItemCombinerMenuAccessor) (Object) this).panda$player();
			List<Component> lore = panda$generateLore(player);
			stack.set(DataComponents.LORE, new ItemLore(lore));
		}
	}
}
