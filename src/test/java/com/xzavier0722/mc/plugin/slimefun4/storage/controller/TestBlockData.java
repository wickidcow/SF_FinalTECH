package com.xzavier0722.mc.plugin.slimefun4.storage.controller;

import org.bukkit.Location;
import org.bukkit.World;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

/** Uses real Slimefun storage semantics with persistence scheduling captured in memory. */
public final class TestBlockData extends SlimefunBlockData {
    private final List<String> updates = new ArrayList<>();

    public TestBlockData() {
        super(new Location(world(), 1, 64, 2), "FINALTECH_TEST");
        setIsDataLoaded(true);
    }

    private static World world() {
        return (World) Proxy.newProxyInstance(World.class.getClassLoader(), new Class<?>[] { World.class },
                (proxy, method, args) -> {
                    if (method.getName().equals("getName")) {
                        return "ticker-test";
                    }
                    throw new UnsupportedOperationException("Unexpected world access: " + method.getName());
                });
    }

    @Override
    public void scheduleUpdateData(String key) {
        updates.add(key);
    }

    public List<String> updates() {
        return updates;
    }
}
