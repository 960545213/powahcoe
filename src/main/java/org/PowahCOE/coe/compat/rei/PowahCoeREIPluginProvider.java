package org.PowahCOE.coe.compat.rei;

import java.util.Collection;
import java.util.List;
import me.shedaniel.rei.api.common.plugins.REIPluginProvider;
import me.shedaniel.rei.forge.REIPluginLoaderClient;

@REIPluginLoaderClient
public class PowahCoeREIPluginProvider implements REIPluginProvider<PowahCoeREIPlugin> {
    @Override
    public Collection<PowahCoeREIPlugin> provide() {
        return List.of(new PowahCoeREIPlugin());
    }

    @Override
    public Class<PowahCoeREIPlugin> getPluginProviderClass() {
        return PowahCoeREIPlugin.class;
    }
}
