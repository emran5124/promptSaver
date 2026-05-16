package com.promptvault;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class JsonSyncManager {

    private static final String TAG = "JsonSyncManager";
    private static final String DEFAULT_FILENAME = "prompts.json";

    public interface SyncCallback {
        void onSuccess(int imported, int updated);
        void onError(String message);
    }

    public static void syncFromDownloads(Context context, SyncCallback callback) {
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File jsonFile = new File(downloadsDir, DEFAULT_FILENAME);

        if (!jsonFile.exists()) {
            callback.onError("فایل " + DEFAULT_FILENAME + " در پوشه Downloads یافت نشد");
            return;
        }

        try {
            String json = readFile(jsonFile);
            processJson(context, json, callback);
        } catch (IOException e) {
            Log.e(TAG, "Error reading file", e);
            callback.onError("خطا در خواندن فایل: " + e.getMessage());
        }
    }

    public static void syncFromUri(Context context, Uri uri, SyncCallback callback) {
        try {
            InputStream is = context.getContentResolver().openInputStream(uri);
            if (is == null) {
                callback.onError("نمی‌توان فایل را باز کرد");
                return;
            }
            String json = readStream(is);
            processJson(context, json, callback);
        } catch (IOException e) {
            Log.e(TAG, "Error reading URI", e);
            callback.onError("خطا در خواندن فایل: " + e.getMessage());
        }
    }

    private static void processJson(Context context, String json, SyncCallback callback) {
        try {
            JsonElement root = JsonParser.parseString(json);
            List<Prompt> prompts = new ArrayList<>();

            if (root.isJsonArray()) {
                JsonArray arr = root.getAsJsonArray();
                for (JsonElement el : arr) {
                    Prompt p = parsePrompt(el.getAsJsonObject());
                    if (p != null) prompts.add(p);
                }
            } else if (root.isJsonObject()) {
                JsonObject obj = root.getAsJsonObject();
                if (obj.has("prompts") && obj.get("prompts").isJsonArray()) {
                    JsonArray arr = obj.getAsJsonArray("prompts");
                    for (JsonElement el : arr) {
                        Prompt p = parsePrompt(el.getAsJsonObject());
                        if (p != null) prompts.add(p);
                    }
                } else {
                    Prompt p = parsePrompt(obj);
                    if (p != null) prompts.add(p);
                }
            }

            if (prompts.isEmpty()) {
                callback.onError("هیچ پرامپتی در فایل یافت نشد");
                return;
            }

            PromptDatabase db = PromptDatabase.getInstance(context);
            List<Prompt> existing = db.getAll();

            int newCount = 0, updateCount = 0;
            for (Prompt incoming : prompts) {
                boolean found = false;
                for (Prompt ex : existing) {
                    if (ex.getId().equals(incoming.getId())) {
                        found = true;
                        if (incoming.getUpdatedAt() > ex.getUpdatedAt()) updateCount++;
                        break;
                    }
                }
                if (!found) newCount++;
            }

            db.insertOrUpdateAll(prompts);
            callback.onSuccess(newCount, updateCount);

        } catch (Exception e) {
            Log.e(TAG, "Error parsing JSON", e);
            callback.onError("فرمت فایل JSON نامعتبر است: " + e.getMessage());
        }
    }

    private static Prompt parsePrompt(JsonObject obj) {
        try {
            String id = obj.has("id") ? obj.get("id").getAsString() : java.util.UUID.randomUUID().toString();
            String title = obj.has("title") ? obj.get("title").getAsString() : "بدون عنوان";
            String content = obj.has("content") ? obj.get("content").getAsString() :
                    (obj.has("prompt") ? obj.get("prompt").getAsString() : "");
            String category = obj.has("category") ? obj.get("category").getAsString() : "";
            String tags = obj.has("tags") ? obj.get("tags").getAsString() : "";
            String aiModel = obj.has("aiModel") ? obj.get("aiModel").getAsString() :
                    (obj.has("model") ? obj.get("model").getAsString() : "");
            long createdAt = obj.has("createdAt") ? obj.get("createdAt").getAsLong() : System.currentTimeMillis();
            long updatedAt = obj.has("updatedAt") ? obj.get("updatedAt").getAsLong() : System.currentTimeMillis();
            boolean isFavorite = obj.has("isFavorite") && obj.get("isFavorite").getAsBoolean();

            if (content.isEmpty()) return null;

            return new Prompt(id, title, content, category, tags, aiModel, createdAt, updatedAt, isFavorite);
        } catch (Exception e) {
            Log.w(TAG, "Skipping invalid prompt entry", e);
            return null;
        }
    }

    public static String exportToJson(Context context) {
        List<Prompt> prompts = PromptDatabase.getInstance(context).getAll();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonObject root = new JsonObject();
        root.add("prompts", gson.toJsonTree(prompts));
        root.addProperty("exportedAt", System.currentTimeMillis());
        root.addProperty("version", "1.0");
        return gson.toJson(root);
    }

    private static String readFile(File file) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        while ((line = br.readLine()) != null) sb.append(line).append("\n");
        br.close();
        return sb.toString();
    }

    private static String readStream(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line;
        while ((line = br.readLine()) != null) sb.append(line).append("\n");
        br.close();
        return sb.toString();
    }
}