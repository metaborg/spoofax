package org.metaborg.spoofax.meta.core.pluto.build.main;

import mb.pie.api.Pie;

public interface IPieProvider {
    public Pie pie();

    public void setLogLevelWarn();

    public void setLogLevelTrace();
}
