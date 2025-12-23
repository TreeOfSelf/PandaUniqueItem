package me.TreeOfSelf.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import me.TreeOfSelf.PandaUniqueItem;
import me.TreeOfSelf.TextFormattingHelper;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.ForgingScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
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

@Mixin(ForgingScreenHandler.class)
interface ForgingScreenHandlerAccessor {
	@Accessor("output")
	CraftingResultInventory getOutput();
	@Accessor("player")
	PlayerEntity getPlayer();
}

@Mixin(AnvilScreenHandler.class)
public class AnvilLoreMixin  {
	@Unique
	private List<Text> generateLore(PlayerEntity player) {
		List<Text> lore = new ArrayList<>();

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

				Text formattedText = TextFormattingHelper.formatTextWithCustomCodes(processedLine);
				lore.add(formattedText.copy().setStyle(formattedText.getStyle().withItalic(false)));
			}
		} else {
			lore.add(Text.literal(""));
			lore.add(Text.literal("§fForged by " + playerName));
			lore.add(Text.literal("§6" + formattedDate));
		}

		return lore;
	}
	@Inject(
			method = "updateResult",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;set(Lnet/minecraft/component/ComponentType;Ljava/lang/Object;)Ljava/lang/Object;", shift = At.Shift.BEFORE, ordinal = 0)
	)
	private void updateResultWithLore(CallbackInfo info, @Local(ordinal = 1) ItemStack outputStack) {
		PlayerEntity player = ((ForgingScreenHandlerAccessor) this).getPlayer();
		List<Text> lore = generateLore(player);
		LoreComponent loreComponent = new LoreComponent(lore);
		outputStack.set(DataComponentTypes.LORE, loreComponent);
	}

	@Inject(
			method = "updateResult",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;remove(Lnet/minecraft/component/ComponentType;)Ljava/lang/Object;", shift = At.Shift.BEFORE, ordinal = 0)
	)
	private void removeLore(CallbackInfo info, @Local(ordinal = 1) ItemStack outputStack) {
		outputStack.remove(DataComponentTypes.LORE);
	}

	@Inject(
			method = "setNewItemName",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;set(Lnet/minecraft/component/ComponentType;Ljava/lang/Object;)Ljava/lang/Object;", shift = At.Shift.BEFORE, ordinal = 0)
	)
	private void updateNewItemNameWithLore(String newItemName, CallbackInfoReturnable<String> info, @Local(ordinal = 0) ItemStack itemStack) {
		PlayerEntity player = ((ForgingScreenHandlerAccessor) this).getPlayer();
		List<Text> lore = generateLore(player);
		LoreComponent loreComponent = new LoreComponent(lore);
		itemStack.set(DataComponentTypes.LORE, loreComponent);
	}
}