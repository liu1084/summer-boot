package org.summer.boot.plugin;

import java.util.ArrayList;
import java.util.List;

public class PluginManager {
    private final List<PluginInterface> plugins = new ArrayList<>();

    public void registerPlugin(PluginInterface plugin) {
        plugins.add(plugin);
    }

    public void applyPlugins() {
        for (PluginInterface plugin : plugins) {
            plugin.apply();
        }
    }
}

