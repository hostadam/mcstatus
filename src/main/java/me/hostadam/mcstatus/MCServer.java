package me.hostadam.mcstatus;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class MCServer {

    private String host;
    private int port;
    private boolean online, eulaBlocked;
    private String versionName;
    private int versionProtocol;
    private int onlinePlayers, maxPlayers;
    private List<String> onlinePlayerList = new ArrayList<>();
    private String motd;
    private String icon;
    private String software;
    private List<String> plugins = new ArrayList<>();
    private String srvHost;
    private int srvPort;

    public MCServer(String jsonString) {
        JsonObject object = new Gson().fromJson(jsonString, JsonObject.class);

        this.host = object.get("host").getAsString();
        this.port = object.get("port").getAsInt();
        this.online = object.get("online").getAsBoolean();
        this.eulaBlocked = object.get("eula_blocked").getAsBoolean();

        if(this.online) {
            JsonObject versionObject = object.get("version").getAsJsonObject();
            this.versionName = versionObject.get("name_clean").getAsString();
            this.versionProtocol = versionObject.get("protocol").getAsInt();

            JsonObject playersObject = object.get("players").getAsJsonObject();
            this.onlinePlayers = playersObject.get("online").getAsInt();
            this.maxPlayers = playersObject.get("max").getAsInt();

            JsonArray array = playersObject.get("list").getAsJsonArray();
            if(!array.isEmpty()) {
                for(JsonElement element : array) {
                    JsonObject playerObject = element.getAsJsonObject();
                    this.onlinePlayerList.add(playerObject.get("name_raw").getAsString());
                }
            }

            JsonObject motdObject = object.get("motd").getAsJsonObject();
            this.motd = motdObject.get("clean").getAsString();
            this.icon = object.has("icon") ? object.get("icon").getAsString() : "";
            if(object.has("software")) {
                JsonElement element = object.get("software");
                if(!element.isJsonNull()) this.software = element.getAsString();
            }

            JsonArray pluginArray = object.get("plugins").getAsJsonArray();
            if(!array.isEmpty()) {
                for(JsonElement element : pluginArray) {
                    JsonObject pluginObject = element.getAsJsonObject();
                    this.plugins.add(pluginObject.get("name").getAsString());
                }
            }

            JsonObject srvObject = object.get("srv_record").getAsJsonObject();
            this.srvHost = srvObject.get("host").getAsString();
            this.srvPort = srvObject.get("port").getAsInt();
        }
    }
}
