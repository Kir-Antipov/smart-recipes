package dev.kir.smartrecipes.config;

import dev.kir.smartrecipes.SmartRecipes;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

public class SmartRecipesConfig {
    private final Map<String, Boolean> options;

    private SmartRecipesConfig() {
        this.options = new HashMap<>();
        this.options.put("debug", true);
    }

    public Optional<Boolean> getValue(String key) {
        return Optional.ofNullable(this.options.get(key));
    }

    public SmartRecipesConfig restore(File file) {
        if (file.exists()) {
            Properties props = new Properties();
            try (FileInputStream stream = new FileInputStream(file)) {
                props.load(stream);
            } catch (IOException e) {
                throw new RuntimeException("Could not load config file", e);
            }

            for (Map.Entry<Object, Object> entry : props.entrySet()) {
                this.options.computeIfPresent((String)entry.getKey(), (a, b) -> ((String)entry.getValue()).equalsIgnoreCase("true"));
            }
        } else {
            try {
                return this.save(file);
            } catch (IOException e) {
                SmartRecipes.LOGGER.warn("Could not write default configuration file", e);
            }
        }
        return this;
    }

    public SmartRecipesConfig save(File file) throws IOException {
        File directory = file.getParentFile();
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                throw new IOException("Could not create parent directories");
            }
        } else if (!directory.isDirectory()) {
            throw new IOException("The parent file is not a directory");
        }

        try (Writer writer = new FileWriter(file, false)) {
            writer.write("# This is the configuration file for SmartRecipes.\n#\n");
            for (Map.Entry<String, Boolean> entry : this.options.entrySet()) {
                writer.write(String.format("%s=%s\n", entry.getKey(), entry.getValue()));
            }
        }
        return this;
    }

    public static SmartRecipesConfig load() {
        return load(new File(String.format("./config/%s.properties", SmartRecipes.MOD_ID)));
    }

    public static SmartRecipesConfig load(File file) {
        return new SmartRecipesConfig().restore(file);
    }
}