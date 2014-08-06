package org.metaborg.spoofax.core;

import org.junit.After;
import org.junit.Before;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class SpoofaxTest {
    protected SpoofaxSession session;

    @Before
    public void setUp() {
        final Injector injector = Guice.createInjector(new SpoofaxModule());
        session = injector.getInstance(SpoofaxSession.class);
    }

    @After
    public void tearDown() {
        session = null;
    }
}
