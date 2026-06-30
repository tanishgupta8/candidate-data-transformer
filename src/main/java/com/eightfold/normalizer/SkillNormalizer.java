package com.eightfold.normalizer;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Normalizes and canonicalizes skill names.
 */
public final class SkillNormalizer implements NormalizerStrategy<String> {
    private static final Map<String, String> CANONICAL_MAP = new HashMap<>();

    public static final SkillNormalizer INSTANCE = new SkillNormalizer();

    static {
        // Define common canonical mappings
        putMapping(new String[]{"nodejs", "node.js", "node js", "node-js"}, "Node.js");
        putMapping(new String[]{"javascript", "js", "java script"}, "JavaScript");
        putMapping(new String[]{"typescript", "ts", "type script"}, "TypeScript");
        putMapping(new String[]{"java"}, "Java");
        putMapping(new String[]{"python", "py"}, "Python");
        putMapping(new String[]{"react", "reactjs", "react.js", "react js"}, "React");
        putMapping(new String[]{"angular", "angularjs", "angular.js"}, "Angular");
        putMapping(new String[]{"vue", "vuejs", "vue.js"}, "Vue.js");
        putMapping(new String[]{"docker"}, "Docker");
        putMapping(new String[]{"kubernetes", "k8s"}, "Kubernetes");
        putMapping(new String[]{"aws", "amazon web services", "amazonwebservices"}, "AWS");
        putMapping(new String[]{"gcp", "google cloud", "google cloud platform"}, "GCP");
        putMapping(new String[]{"azure", "microsoft azure"}, "Azure");
        putMapping(new String[]{"html", "html5"}, "HTML");
        putMapping(new String[]{"css", "css3"}, "CSS");
        putMapping(new String[]{"c++", "cpp", "cplusplus"}, "C++");
        putMapping(new String[]{"c#", "csharp"}, "C#");
        putMapping(new String[]{"sql", "mysql", "postgresql", "postgres"}, "SQL");
        putMapping(new String[]{"mongodb", "mongo"}, "MongoDB");
        putMapping(new String[]{"git", "github"}, "Git");
    }

    private SkillNormalizer() {}

    private static void putMapping(String[] variations, String canonical) {
        for (String variation : variations) {
            CANONICAL_MAP.put(cleanKey(variation), canonical);
        }
    }

    private static String cleanKey(String str) {
        if (str == null) return "";
        return str.toLowerCase()
                  .replaceAll("[^a-z0-9+#]", "")
                  .trim();
    }

    @Override
    public Optional<String> normalize(String skill) {
        if (skill == null || skill.isBlank()) {
            return Optional.empty();
        }

        String cleaned = skill.trim();
        String key = cleanKey(cleaned);
        String canonical = CANONICAL_MAP.get(key);

        if (canonical != null) {
            return Optional.of(canonical);
        }

        // Fallback: title-case the words
        String[] words = cleaned.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                sb.append(Character.toUpperCase(word.charAt(0)));
                if (word.length() > 1) {
                    sb.append(word.substring(1).toLowerCase());
                }
                sb.append(" ");
            }
        }
        return Optional.of(sb.toString().trim());
    }
}
