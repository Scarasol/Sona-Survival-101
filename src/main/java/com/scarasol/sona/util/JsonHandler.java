package com.scarasol.sona.util;

import com.google.common.collect.Maps;
import com.google.gson.*;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

public class JsonHandler {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void writeListToJson(String modid, List<String> list, ResourceLocation resource) {
        try {
            // config/<modid> 根目录
            Path configDir = FMLPaths.CONFIGDIR.get().resolve(modid);

            // config/<modid>/<namespace>/
            Path namespaceDir = configDir.resolve(resource.getNamespace());
            if (Files.notExists(namespaceDir)) {
                Files.createDirectories(namespaceDir);
            }

            // 文件路径 config/<modid>/<namespace>/<path>.json
            Path file = namespaceDir.resolve(resource.getPath() + ".json");

            // 创建 JSON 对象
            JsonObject root = new JsonObject();
            JsonArray array = new JsonArray();
            for (String s : list) {
                array.add(s);
            }
            root.add("value", array);

            // 写入文件
            String jsonText = GSON.toJson(root);
            Files.writeString(file, jsonText, StandardCharsets.UTF_8);

            System.out.println("Created JSON: " + file.toAbsolutePath());

        } catch (IOException e) {
            throw new RuntimeException("Failed to write JSON for resource: " + resource, e);
        }
    }

    public static Map<String, List<String>> readAllJsonValues(String modid) {
        Path configDir = FMLPaths.CONFIGDIR.get().resolve(modid);
        Map<String, List<String>> result = new LinkedHashMap<>();

        if (Files.notExists(configDir) || !Files.isDirectory(configDir)) {
            return result;
        }

        try (Stream<Path> stream = Files.walk(configDir)) {
            stream.filter(Files::isRegularFile)
                    .filter(p -> p.toString().toLowerCase(Locale.ROOT).endsWith(".json"))
                    .forEach(p -> {
                        try {
                            String content = Files.readString(p, StandardCharsets.UTF_8);
                            JsonObject obj = GSON.fromJson(content, JsonObject.class);
                            if (obj == null || !obj.has("value")) {
                                return;
                            }

                            JsonElement valueElem = obj.get("value");
                            if (!valueElem.isJsonArray()) {
                                return;
                            }

                            JsonArray arr = valueElem.getAsJsonArray();
                            List<String> list = new ArrayList<>(arr.size());

                            for (JsonElement el : arr) {
                                if (el.isJsonPrimitive() && el.getAsJsonPrimitive().isString()) {
                                    list.add(el.getAsString());
                                } else {
                                    list.add(el.toString());
                                }
                            }

                            // 计算 ResourceLocation 风格 key
                            Path relativePath = configDir.relativize(p);
                            Path parent = relativePath.getParent();

                            String namespace;
                            if (parent == null) {
                                // 文件在根目录下，用 modid 作为 namespace
                                namespace = modid;
                            } else {
                                namespace = parent.toString().replace(FileSystems.getDefault().getSeparator(), "/");
                            }

                            String fileName = p.getFileName().toString();
                            if (fileName.toLowerCase(Locale.ROOT).endsWith(".json")) {
                                fileName = fileName.substring(0, fileName.length() - 5);
                            }

                            String key = namespace + ":" + fileName;
                            result.put(key, list);

                        } catch (IOException | JsonParseException ex) {
                            System.err.println("Failed to read/parse JSON file: " + p + " -> " + ex.getMessage());
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException("Failed to walk config directory: " + configDir, e);
        }

        return result;
    }



    public static List<String> readJsonList(String modid, ResourceLocation resource) {
        List<String> result = new ArrayList<>();
        Path file = FMLPaths.CONFIGDIR.get()
                .resolve(modid)
                .resolve(resource.getNamespace())
                .resolve(resource.getPath() + ".json");

        if (!Files.exists(file) || !Files.isRegularFile(file)) {
            return result;
        }

        try {
            String content = Files.readString(file, StandardCharsets.UTF_8);
            JsonObject obj = GSON.fromJson(content, JsonObject.class);
            if (obj == null || !obj.has("value")) {
                return result;
            }

            JsonElement valueElem = obj.get("value");
            if (!valueElem.isJsonArray()) {
                return result;
            }

            JsonArray array = valueElem.getAsJsonArray();
            for (JsonElement el : array) {
                if (el.isJsonPrimitive() && el.getAsJsonPrimitive().isString()) {
                    result.add(el.getAsString());
                } else {
                    result.add(el.toString());
                }
            }

        } catch (IOException | JsonParseException e) {
            System.err.println("Failed to read JSON file: " + file + " -> " + e.getMessage());
        }

        return result;
    }

}
