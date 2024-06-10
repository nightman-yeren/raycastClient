package dev.yuruni.raycastclient.config;

import com.google.gson.*;
import com.lukflug.panelstudio.config.IConfigList;
import com.lukflug.panelstudio.config.IPanelConfig;

import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class GUIConfig implements IConfigList {

    private final String fileLocation;
    public static Properties config;
    public static JsonObject panelObject;

    public GUIConfig(String fileLocation) {
        this.fileLocation = fileLocation;
    }

    @Override
    public void begin(boolean loading) {
        if (loading) {
            if (!Files.exists(Paths.get(fileLocation + "ClickGUI" + ".json"))) {
                return;
            }
            config = new Properties();
            try {
                //InputStream inputStream;
                //inputStream = Files.newInputStream(Paths.get(fileLocation + "ClickGUI" + ".json"));
                //System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(JsonParser.parseReader(new InputStreamReader(inputStream))).getClass());
                //JsonObject mainObject = JsonParser.parseReader(new InputStreamReader(inputStream)).getAsJsonObject();
                //if (mainObject.get("Panels") == null) {
                    //return;
                //}
                //panelObject = mainObject.get("Panels").getAsJsonObject();
                //inputStream.close();
                config.loadFromXML(new FileInputStream(fileLocation + "ClickGUI" + ".xml"));
                JsonObject mainObject = JsonParser.parseString(config.getProperty("panels", null)).getAsJsonObject();
                if (mainObject.get("Panels") == null) {
                    return;
                }
                panelObject = mainObject.get("Panels").getAsJsonObject();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            panelObject = new JsonObject();
        }
    }

    @Override
    public void end(boolean loading) {
        if (panelObject == null) return;
        if (!loading) {
            config = new Properties();
            try {
                //Gson gson = new GsonBuilder().setPrettyPrinting().create();
                //OutputStreamWriter fileOutputStreamWriter = new OutputStreamWriter(new FileOutputStream(fileLocation + "ClickGUI" + ".json"), StandardCharsets.UTF_8);
                //JsonObject mainObject = new JsonObject();
                //mainObject.add("Panels", panelObject);
                //String jsonString = gson.toJson(JsonParser.parseString(mainObject.toString()));
                //fileOutputStreamWriter.write(jsonString);
                //fileOutputStreamWriter.close();
                config.setProperty("panels", panelObject.toString());
                config.storeToXML(new FileOutputStream(fileLocation + "ClickGUI" + ".xml"), null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        panelObject = null;
    }

    @Override
    public IPanelConfig addPanel(String title) {
        if (panelObject == null) return null;
        JsonObject valueObject = new JsonObject();
        panelObject.add(title, valueObject);
        return new GSPanelConfig(valueObject);
    }

    @Override
    public IPanelConfig getPanel(String title) {
        if (panelObject == null) return null; //Stops here for some reason
        JsonElement configObject = panelObject.get(title);
        if (configObject != null && configObject.isJsonObject()) {
            return new GSPanelConfig(configObject.getAsJsonObject());
        }
        return null;
    }


    public record GSPanelConfig(JsonObject configObject) implements IPanelConfig {

        @Override
            public void savePositon(Point position) {
                configObject.add("PosX", new JsonPrimitive(position.x));
                configObject.add("PosY", new JsonPrimitive(position.y));
            }

            @Override
            public void saveSize(Dimension size) {

            }

            @Override
            public Point loadPosition() {
                Point point = new Point();
                JsonElement panelPosXObject = configObject.get("PosX");
                if (panelPosXObject != null && panelPosXObject.isJsonPrimitive()) {
                    point.x = panelPosXObject.getAsInt();
                } else return null;
                JsonElement panelPosYObject = configObject.get("PosY");
                if (panelPosYObject != null && panelPosYObject.isJsonPrimitive()) {
                    point.y = panelPosYObject.getAsInt();
                } else return null;
                return point;
            }

            @Override
            public Dimension loadSize() {
                return null;
            }

            @Override
            public void saveState(boolean state) {
                configObject.add("State", new JsonPrimitive(state));
            }

            @Override
            public boolean loadState() {
                JsonElement panelOpenObject = configObject.get("State");
                if (panelOpenObject != null && panelOpenObject.isJsonPrimitive()) {
                    return panelOpenObject.getAsBoolean();
                }
                return false;
            }
        }
}
