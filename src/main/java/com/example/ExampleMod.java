package com.example;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class ExampleMod implements ClientModInitializer {
    private static KeyBinding toggleKey;
    private boolean isEnabled = false;
    private int tickCounter = 0;
    private boolean wasForcedPressed = false;

    @Override
    public void onInitializeClient() {
        // "M" Tuşunu oyuna entegre ediyoruz
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.fastbow.toggle", // İç kimliği
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_M, // Varsayılan tuş: M
            "category.fastbow" // Ayarlardaki kategori başlığı
        ));

        // Oyundaki her anı takip eden döngü
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) {
                return;
            }

            // M tuşuna basılıp basılmadığını kontrol et (Her basışta durum değişir)
            while (toggleKey.wasPressed()) {
                isEnabled = !isEnabled;
                
                // Ekranın alt ortasında (Actionbar) durum bildirimi gösterir
                String status = isEnabled ? "§aAÇIK" : "§cKAPALI";
                client.player.sendMessage(Text.literal("Hızlı Ok Atma: " + status), true);
                
                // Eğer mod kapatıldıysa ve o sırada sağ tık basılı kalmışsa serbest bırak
                if (!isEnabled && wasForcedPressed) {
                    client.options.useKey.setPressed(false);
                    wasForcedPressed = false;
                }
            }

            // Mod aktifse ok atma mekanizmasını çalıştır
            if (isEnabled) {
                if (client.player.getStackInHand(Hand.MAIN_HAND).isOf(Items.BOW)) {
                    if (!client.player.isUsingItem()) {
                        // Sağ tıkı basılı tutarak yayı germeye başla
                        client.options.useKey.setPressed(true);
                        wasForcedPressed = true;
                        tickCounter = 0;
                    } else {
                        tickCounter++;
                        // 3 tick sonra sağ tıkı bırak ve oku fırlat
                        if (tickCounter >= 3) {
                            client.options.useKey.setPressed(false);
                            wasForcedPressed = false;
                            tickCounter = 0;
                        }
                    }
                } else if (wasForcedPressed) {
                    // Eğer elinden yayı bıraktıysan sağ tık basılı kalmasın
                    client.options.useKey.setPressed(false);
                    wasForcedPressed = false;
                }
            }
        });
    }
}
