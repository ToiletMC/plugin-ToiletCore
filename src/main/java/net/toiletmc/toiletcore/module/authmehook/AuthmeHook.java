package net.toiletmc.toiletcore.module.authmehook;

import fr.xephi.authme.api.v3.AuthMeApi;
import fr.xephi.authme.events.PasswordEncryptionEvent;
import fr.xephi.authme.security.PasswordSecurity;
import net.toiletmc.toiletcore.ToiletCore;
import net.toiletmc.toiletcore.module.AbstractModule;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class AuthmeHook extends AbstractModule implements Listener {
    AuthMeApi authMeApi = AuthMeApi.getInstance();

    public AuthmeHook(ToiletCore plugin) {
        super(plugin);
    }

    @Override
    public void enable() {
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        reloadAuthme();
    }

    @Override
    public void disable() {
        HandlerList.unregisterAll(this);
        reloadAuthme();
    }

    private void reloadAuthme() {
        try {
            Class<?> clazz = authMeApi.getClass();
            Field privateField = clazz.getDeclaredField("passwordSecurity");
            privateField.setAccessible(true);
            PasswordSecurity fieldValue = (PasswordSecurity) privateField.get(authMeApi);
            Method publicReloadMethod = fieldValue.getClass().getMethod("reload");
            publicReloadMethod.invoke(fieldValue);
            plugin.getLogger().info("authme reloaded.");
        } catch (NoSuchFieldException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @EventHandler
    private void onPwdEncryption(PasswordEncryptionEvent event) {
        event.setMethod(new SaltedSha512Twice());
    }
}
