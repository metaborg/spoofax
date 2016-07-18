package org.metaborg.core.test.language;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.metaborg.core.language.LanguageVersion;

public class LanguageVersionTest {
    @Test public void testParse() {
        assertEquals(new LanguageVersion(0, 0, 0, ""), LanguageVersion.parse(""));
        assertEquals(new LanguageVersion(1, 0, 0, ""), LanguageVersion.parse("1"));
        assertEquals(new LanguageVersion(1, 2, 0, ""), LanguageVersion.parse("1.2"));
        assertEquals(new LanguageVersion(1, 2, 3, ""), LanguageVersion.parse("1.2.3"));
        assertEquals(new LanguageVersion(1, 2, 3, "SNAPSHOT"), LanguageVersion.parse("1.2.3-SNAPSHOT"));
        assertEquals(new LanguageVersion(1, 2, 3, "baseline-20150607"),
            LanguageVersion.parse("1.2.3-baseline-20150607"));
    }

    @Test public void testCompare() {
        int result;
        result = new LanguageVersion(1, 0, 0, "").compareTo(new LanguageVersion(1, 0, 0, ""));
        assertTrue(result == 0);
        result = new LanguageVersion(1, 2, 0, "").compareTo(new LanguageVersion(2, 1, 0, ""));
        assertTrue(result < 0);
        result = new LanguageVersion(2, 1, 0, "").compareTo(new LanguageVersion(1, 2, 0, ""));
        assertTrue(result > 0);
        
        result = new LanguageVersion(1, 2, 0, "SNAPSHOT").compareTo(new LanguageVersion(1, 2, 0, ""));
        assertTrue(result > 0);
        result = new LanguageVersion(1, 2, 0, "SNAPSHOT").compareTo(new LanguageVersion(1, 2, 0, "baseline-1234"));
        assertTrue(result > 0);
        result = new LanguageVersion(1, 2, 0, "SNAPSHOT").compareTo(new LanguageVersion(1, 2, 0, "snapshot"));
        assertTrue(result == 0);
        
        result = new LanguageVersion(1, 0, 0, "").compareTo(new LanguageVersion(1, 0, 0, "SNAPSHOT"));
        assertTrue(result < 0);
        result = new LanguageVersion(1, 0, 0, "").compareTo(new LanguageVersion(1, 0, 0, "baseline-1234"));
        assertTrue(result > 0);
    }
}
