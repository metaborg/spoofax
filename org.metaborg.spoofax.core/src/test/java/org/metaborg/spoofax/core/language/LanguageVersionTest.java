/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.metaborg.spoofax.core.language;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author hendrik
 */
public class LanguageVersionTest {
    
    @Test
    public void testParse() {
        assertEquals(new LanguageVersion(0, 0, 0, ""), LanguageVersion.parse(""));
        assertEquals(new LanguageVersion(1, 0, 0, ""), LanguageVersion.parse("1"));
        assertEquals(new LanguageVersion(1, 2, 0, ""), LanguageVersion.parse("1.2"));
        assertEquals(new LanguageVersion(1, 2, 3, ""), LanguageVersion.parse("1.2.3"));
        assertEquals(new LanguageVersion(1, 2, 3, "SNAPSHOT"), LanguageVersion.parse("1.2.3-SNAPSHOT"));
    }
    
    @Test
    public void testCompare() {
        int result;
        result = new LanguageVersion(1, 0, 0, "").compareTo(new LanguageVersion(1, 0, 0, ""));
        assertTrue(result == 0);
        result = new LanguageVersion(1, 2, 0, "").compareTo(new LanguageVersion(2, 1, 0, ""));
        assertTrue(result < 0);
        result = new LanguageVersion(2, 1, 0, "").compareTo(new LanguageVersion(1, 2, 0, ""));
        assertTrue(result > 0);
        result = new LanguageVersion(1, 2, 0, "SNAPSHOT").compareTo(new LanguageVersion(1, 2, 0, ""));
        assertTrue(result < 0);
        result = new LanguageVersion(1, 2, 0, "SNAPSHOT").compareTo(new LanguageVersion(1, 2, 0, "Z"));
        assertTrue(result < 0);
        result = new LanguageVersion(1, 2, 0, "SNAPSHOT").compareTo(new LanguageVersion(1, 2, 0, "snapshot"));
        assertTrue(result == 0);
    }

}
