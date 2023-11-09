package org.metaborg.core.test.language;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import jakarta.annotation.Nullable;

import org.junit.Test;
import org.metaborg.core.language.LanguageVersion;

public class LanguageVersionTest {
    public void testParseEmpty() {
        assertEquals(version(null, null, null, null), parse(""));
    }

    public void testParseCharacterFail() {
        assertEquals(version(null, null, null, "a"), parse("a"));
    }

    public void testParseNegativeNumberFail() {
        assertEquals(version(null, null, null, "-1"), parse("-1"));
    }

    @Test public void testParse() {
        assertEquals(version(1), parse("1"));
        assertEquals(version(1, 2), parse("1.2"));
        assertEquals(version(1, 2, 3), parse("1.2.3"));
        assertEquals(version(1, 2, 3, "-SNAPSHOT"), parse("1.2.3-SNAPSHOT"));
        assertEquals(version(1, 2, 3, "-baseline-20150607"), parse("1.2.3-baseline-20150607"));
    }

    @Test public void testToString() {
        assertEquals(version(1).toString(), "1");
        assertEquals(version(1, 2).toString(), "1.2");
        assertEquals(version(1, 2, 3).toString(), "1.2.3");
        assertEquals(version(1, 2, 3, "-SNAPSHOT").toString(), "1.2.3-SNAPSHOT");
        assertEquals(version(1, 2, 3, "-baseline-20150607").toString(), "1.2.3-baseline-20150607");
    }

    @Test public void testCompare() {
        assertCompareEquals(version(1, 0, 0), version(1, 0, 0));
        assertCompareSmaller(version(1, 2, 0), version(2, 1, 0));
        assertCompareLarger(version(2, 1, 0), version(1, 2, 0));
        assertCompareLarger(version(1, 2, 0, "SNAPSHOT"), version(1, 2, 0));
        assertCompareLarger(version(1, 2, 0, "SNAPSHOT"), version(1, 2, 0, "baseline-1234"));
        assertCompareEquals(version(1, 2, 0, "SNAPSHOT"), version(1, 2, 0, "snapshot"));
        assertCompareSmaller(version(1, 0, 0), version(1, 0, 0, "SNAPSHOT"));
        assertCompareLarger(version(1, 0, 0), version(1, 0, 0, "baseline-1234"));
    }


    private LanguageVersion parse(String version) {
        return LanguageVersion.parse(version);
    }

    private LanguageVersion version(int major) {
        return version(major, null, null, null);
    }

    private LanguageVersion version(int major, int minor) {
        return version(major, minor, null, null);
    }

    private LanguageVersion version(int major, int minor, int patch) {
        return version(major, minor, patch, null);
    }

    private LanguageVersion version(@Nullable Integer major, @Nullable Integer minor, @Nullable Integer patch,
        @Nullable String qualifier) {
        return new LanguageVersion(major, minor, patch, qualifier);
    }


    private static <T extends Comparable<T>> void assertCompareLarger(T left, T right, String message) {
        final int result = left.compareTo(right);
        if(result < 1) {
            final String formatted = preformat(message);
            fail(formatted + "expected that: " + left + " > " + right + " but was: " + compareIntToChar(result));
        }
    }

    private static <T extends Comparable<T>> void assertCompareLarger(T left, T right) {
        assertCompareLarger(left, right, null);
    }

    private static <T extends Comparable<T>> void assertCompareSmaller(T left, T right, String message) {
        final int result = left.compareTo(right);
        if(result > -1) {
            final String formatted = preformat(message);
            fail(formatted + "expected that: " + left + " < " + right + " but was: " + compareIntToChar(result));
        }
    }

    private static <T extends Comparable<T>> void assertCompareSmaller(T left, T right) {
        assertCompareSmaller(left, right, null);
    }

    private static <T extends Comparable<T>> void assertCompareEquals(T left, T right, String message) {
        final int result = left.compareTo(right);
        if(result != 0) {
            final String formatted = preformat(message);
            fail(formatted + "expected that: " + left + " = " + right + " but was: " + compareIntToChar(result));
        }
    }

    private static <T extends Comparable<T>> void assertCompareEquals(T left, T right) {
        assertCompareEquals(left, right, null);
    }

    private static char compareIntToChar(int compareInt) {
        if(compareInt > 0) {
            return '>';
        } else if(compareInt < 0) {
            return '<';
        } else {
            return '=';
        }
    }


    private static String preformat(String message) {
        String formatted = "";
        if(message != null && !message.equals("")) {
            formatted = message + " ";
        }
        return formatted;
    }
}
