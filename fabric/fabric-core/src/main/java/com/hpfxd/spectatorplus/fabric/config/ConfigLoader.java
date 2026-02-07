package com.hpfxd.spectatorplus.fabric.config;

import com.google.common.base.Suppliers;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;

public class ConfigLoader<T> {
    private static final Supplier<Gson> DEFAULT_GSON = Suppliers.memoize(() -> new GsonBuilder()
            .setPrettyPrinting()
            .create());

    private final Class<T> configClass;
    private final Path file;
    private final Gson gson;

    public static <T> ConfigLoader<T> create(Class<T> clazz, String name) throws IOException {
        final Path containerDirectory = FabricLoader.getInstance().getConfigDir().resolve("spectatorplus");
        try {
            Files.createDirectory(containerDirectory);
        } catch (FileAlreadyExistsException ignored) {
        }

        return new ConfigLoader<>(clazz, containerDirectory.resolve(name + ".json"), DEFAULT_GSON.get());
    }

    public ConfigLoader(Class<T> configClass, Path file, Gson gson) {
        this.configClass = configClass;
        this.file = file;
        this.gson = gson;
    }

    public Path getFile() {
        return this.file;
    }

    public T load() throws IOException {
        try (final Reader reader = Files.newBufferedReader(this.file)) {
            return this.gson.fromJson(reader, this.configClass);
        }
    }

    public void save(T config) throws IOException {
        try (final Writer writer = Files.newBufferedWriter(this.file)) {
            this.gson.toJson(config, writer);
        }
    }
}
