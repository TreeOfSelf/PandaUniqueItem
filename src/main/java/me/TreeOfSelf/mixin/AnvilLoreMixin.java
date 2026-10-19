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
		if (!result.isEmpty() && result.has(DataComponents.CUSTOM_NAME)) {
			panda$setForgedLore(result, access.panda$player());
		} else if (!result.isEmpty() && panda$hasForgedLore(result.get(DataComponents.LORE))) {
			// Name cleared: the forged lore goes with it, any other lore stays
			List<Component> kept = panda$withoutForgedLore(result.get(DataComponents.LORE));
			if (kept.isEmpty()) result.remove(DataComponents.LORE);
			else result.set(DataComponents.LORE, new ItemLore(kept));
		}
	}

	// Keeps the item's own lore (head death lines, other mods' lore) and puts the forged lines under it,
	// replacing forged lines from an earlier rename
	private void panda$setForgedLore(ItemStack stack, Player player) {
		List<Component> forged = panda$generateLore(player);
		List<Component> own = panda$withoutForgedLore(stack.get(DataComponents.LORE));
		List<Component> lore = new ArrayList<>(own.subList(0, Math.min(own.size(), Math.max(0, ItemLore.MAX_LINES - forged.size()))));
		lore.addAll(forged);
		stack.set(DataComponents.LORE, new ItemLore(lore));
	}

	private static List<Component> panda$withoutForgedLore(ItemLore lore) {
		if (lore == null) return List.of();
		List<Component> lines = lore.lines();
		if (!panda$hasForgedLore(lore)) return lines;
		return lines.subList(0, lines.size() - panda$loreFormat().size());
	}

	private static List<String> panda$loreFormat() {
		return PandaUniqueItem.config != null && PandaUniqueItem.config.loreFormat != null
				? PandaUniqueItem.config.loreFormat : List.of("", "Forged by %player_name%", "%date%");
	}

	// Lore ends with lines this mod wrote: one line per loreFormat line, matching it with any player name and date
	private static boolean panda$hasForgedLore(ItemLore lore) {
		List<String> format = panda$loreFormat();
		if (lore == null || lore.lines().size() < format.size()) return false;
		int offset = lore.lines().size() - format.size();
		for (int i = 0; i < format.size(); i++) {
			String plain = format.get(i).replaceAll("<[^>]*>", "").replaceAll("&[0-9a-fk-or]", "");
			StringBuilder regex = new StringBuilder();
			for (String part : plain.split("((?=%player_name%)|(?<=%player_name%)|(?=%date%)|(?<=%date%))")) {
				if (part.equals("%player_name%")) regex.append("\\S{1,16}");
				else if (part.equals("%date%")) regex.append("[A-Za-z]{3} \\d{2}, \\d{4}");
				else if (!part.isEmpty()) regex.append(java.util.regex.Pattern.quote(part));
			}
			if (!lore.lines().get(offset + i).getString().matches(regex.toString())) return false;
		}
		return true;
	}

	@Inject(method = "setItemName", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/AnvilMenu;createResult()V", shift = At.Shift.BEFORE))
	private void panda$renamePreviewLore(String name, CallbackInfoReturnable<Boolean> cir) {
		Slot slot = ((AnvilMenu) (Object) this).getSlot(2);
		if (slot.hasItem()) {
			ItemStack stack = slot.getItem();
			panda$setForgedLore(stack, ((ItemCombinerMenuAccessor) (Object) this).panda$player());
		}
	}
}
