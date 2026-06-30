package com.eightfold.normalizer;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public final class SkillNormalizerTest {

    @Test
    public void testCanonicalSkills() {
        Optional<String> skill1 = SkillNormalizer.INSTANCE.normalize("node js");
        Optional<String> skill2 = SkillNormalizer.INSTANCE.normalize("Node.js");
        Optional<String> skill3 = SkillNormalizer.INSTANCE.normalize("NODE.JS");
        Optional<String> skill4 = SkillNormalizer.INSTANCE.normalize("nodejs");

        assertTrue(skill1.isPresent());
        assertEquals("Node.js", skill1.get());

        assertTrue(skill2.isPresent());
        assertEquals("Node.js", skill2.get());

        assertTrue(skill3.isPresent());
        assertEquals("Node.js", skill3.get());

        assertTrue(skill4.isPresent());
        assertEquals("Node.js", skill4.get());
    }

    @Test
    public void testCommonLanguageSkills() {
        Optional<String> java = SkillNormalizer.INSTANCE.normalize("JAVA");
        Optional<String> cpp = SkillNormalizer.INSTANCE.normalize("cplusplus");
        Optional<String> aws = SkillNormalizer.INSTANCE.normalize("amazon web services");

        assertTrue(java.isPresent());
        assertEquals("Java", java.get());

        assertTrue(cpp.isPresent());
        assertEquals("C++", cpp.get());

        assertTrue(aws.isPresent());
        assertEquals("AWS", aws.get());
    }

    @Test
    public void testUnknownSkillFallback() {
        Optional<String> skill = SkillNormalizer.INSTANCE.normalize("deep learning architecture");

        assertTrue(skill.isPresent());
        assertEquals("Deep Learning Architecture", skill.get());
    }

    @Test
    public void testEmptySkills() {
        Optional<String> empty = SkillNormalizer.INSTANCE.normalize("");
        Optional<String> nill = SkillNormalizer.INSTANCE.normalize(null);

        assertFalse(empty.isPresent());
        assertFalse(nill.isPresent());
    }
}
