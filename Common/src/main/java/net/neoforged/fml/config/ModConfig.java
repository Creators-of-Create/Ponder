package net.neoforged.fml.config;

import java.nio.file.Path;

// TODO - Find a way to get rid of this or throw it into a stubs sourceset
public final class ModConfig {
    public Type getType() {
		throw new UnsupportedOperationException();
    }

    public String getFileName() {
		throw new UnsupportedOperationException();
    }

    public IConfigSpec getSpec() {
		throw new UnsupportedOperationException();
    }

    public String getModId() {
		throw new UnsupportedOperationException();
    }

    public Path getFullPath() {
		throw new UnsupportedOperationException();
    }

    public enum Type {
        COMMON,
        CLIENT,
        SERVER,
        STARTUP;

        public String extension() {
            throw new UnsupportedOperationException();
        }
    }
}
