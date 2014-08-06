package org.metaborg.spoofax.core;

import org.junit.After;
import org.junit.Before;

import rx.schedulers.Schedulers;
import rx.schedulers.TestScheduler;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class SpoofaxTest {
    protected static TestScheduler testScheduler;

    protected SpoofaxSession session;

    public static void initialize() {
        testScheduler = Schedulers.test();
    }

    @Before public void setUp() {
        final Injector injector = Guice.createInjector(new SpoofaxModule());
        session = injector.getInstance(SpoofaxSession.class);
    }

    @After public void tearDown() {
        testScheduler.triggerActions();
        session = null;
    }
}
