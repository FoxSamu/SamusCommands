package dev.runefox.mc.cmd;

import com.mojang.authlib.GameProfile;
import dev.runefox.mc.cmd.net.ServerHandler;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public interface ModPlayer {
    boolean isSynced();
    void setSynced();

    ServerHandler net();

    void setHome(String name, TeleportPos pos);
    void setHome(String name);
    void setHome(TeleportPos pos);
    void setHome();

    TeleportPos getHome(String name);
    TeleportPos getHome();

    void deleteHome(String name);
    void deleteHome();

    boolean hasHome(String name);
    boolean hasHome();

    boolean teleportToHome(String name);
    boolean teleportToHome();

    Map<String, TeleportPos> homes();
    int homeCount();
    void clearHomes(boolean keepMain);


    void teleportToSpawn();


    boolean requestTpFrom(ServerPlayer source);
    boolean callForTpTo(ServerPlayer source);

    boolean acceptTpRequest(ServerPlayer source);
    TeleportRequest acceptLastTpRequest();
    boolean acceptTpRequests();

    boolean denyTpRequest(ServerPlayer source);
    TeleportRequest denyLastTpRequest();
    boolean denyTpRequests();

    void addToTpBlacklist(GameProfile profile);
    void removeFromTpBlacklist(GameProfile profile);
    boolean isTpBlacklisted(GameProfile profile);

    void addToTpWhitelist(GameProfile profile);
    void removeFromTpWhitelist(GameProfile profile);
    boolean isTpWhitelisted(GameProfile profile);

    boolean canRequest(GameProfile profile);

    void useTpWhitelist(boolean on);
    boolean useTpWhitelist();

    Stream<TeleportRequest> tpRequests();


    int note(String text);
    int lastNoteLine();
    String getNote(int line);
    String editNote(int line, String text);
    String removeNote(int line);
    List<String> removeNotes(int from, int to);
    List<String> getNotes(int from, int to);
    String moveNote(int from, int to);
    List<String> clearNotes();
    int noteCount();
    List<String> notes();

    static ModPlayer get(ServerPlayer player) {
        return PlayerController.get(player);
    }
}
