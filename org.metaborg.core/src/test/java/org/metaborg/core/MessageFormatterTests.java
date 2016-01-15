package org.metaborg.core;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class MessageFormatterTests {

    @Test
    public void formatsStringWithoutPlaceholders() {
        String input = "String without placeholders.";
        String result = MessageFormatter.format(input);

        assertEquals(input, result);
    }

    @Test
    public void formatsStringWithOnePlaceholder() {
        String input = "String {} placeholders.";
        String result = MessageFormatter.format(input, "with");

        assertEquals("String with placeholders.", result);
    }

    @Test
    public void formatsStringWithTwoPlaceholders() {
        String input = "String {} {} placeholders.";
        String result = MessageFormatter.format(input, "with", "some");

        assertEquals("String with some placeholders.", result);
    }

    @Test
    public void formatsStringWithThreePlaceholders() {
        String input = "String {} {} {} placeholders.";
        String result = MessageFormatter.format(input, "with", "some", "more");

        assertEquals("String with some more placeholders.", result);
    }

    @Test
    public void formatsStringWithFourPlaceholders() {
        String input = "String {} {} {} {} placeholders.";
        String result = MessageFormatter.format(input, "with", "an", "array", "of");

        assertEquals("String with an array of placeholders.", result);
    }

    @Test
    public void formatsStringWithValueArray() {
        String input = "String {} {} {} {} placeholders.";
        String result = MessageFormatter.format(input, new Object[]{"with", "an", "array", "of"});

        assertEquals("String with an array of placeholders.", result);
    }
}
