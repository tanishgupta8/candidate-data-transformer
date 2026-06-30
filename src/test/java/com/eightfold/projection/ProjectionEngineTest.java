package com.eightfold.projection;

import com.eightfold.exception.ProjectionException;
import com.eightfold.model.CandidateProfile;
import com.eightfold.model.Experience;
import com.eightfold.model.Provenance;
import com.eightfold.model.Skill;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public final class ProjectionEngineTest {
    private ProjectionEngine engine;
    private CandidateProfile profile;

    @BeforeEach
    public void setUp() {
        engine = new ProjectionEngine();
        profile = CandidateProfile.builder()
                .candidateId("uuid-1234")
                .fullName("John Doe")
                .emails(List.of("john.doe@email.com", "backup@email.com"))
                .phones(List.of("+14155552671"))
                .location("US")
                .headline("Software Engineer")
                .yearsExperience(5.5)
                .skills(List.of(new Skill("Java"), new Skill("Node.js")))
                .experience(List.of(new Experience("Senior Software Engineer", "Google", "2021-06", "Present", "Description")))
                .putProvenance("fullName", new Provenance("fullName", "Resume PDF", "source_priority"))
                .putProvenance("emails", new Provenance("emails", "Resume PDF, Recruiter CSV", "merge"))
                .putFieldConfidence("fullName", 0.95)
                .putFieldConfidence("emails", 0.98)
                .overallConfidence(0.96)
                .build();
    }

    @Test
    public void testDefaultProjection() {
        // Empty config projects all fields
        Map<String, Object> projected = engine.project(profile, Collections.emptyMap());

        assertEquals("uuid-1234", projected.get("candidate_id"));
        assertEquals("John Doe", projected.get("full_name"));
        assertEquals("US", projected.get("location"));
        assertEquals(5.5, projected.get("years_experience"));
    }

    @Test
    public void testCustomFieldSelectionAndRename() {
        Map<String, Object> config = new LinkedHashMap<>();
        List<Map<String, Object>> fields = new ArrayList<>();

        fields.add(Map.of("path", "id", "from", "candidate_id", "required", true));
        fields.add(Map.of("path", "name", "from", "full_name"));
        fields.add(Map.of("path", "primary_email", "from", "emails[0]"));
        fields.add(Map.of("path", "employer", "from", "experience[0].company"));

        config.put("fields", fields);
        config.put("include_confidence", true);
        config.put("include_provenance", true);
        config.put("on_missing", "null");

        Map<String, Object> projected = engine.project(profile, config);

        assertEquals("uuid-1234", projected.get("id"));
        assertEquals("John Doe", projected.get("name"));
        assertEquals("john.doe@email.com", projected.get("primary_email"));
        assertEquals("Google", projected.get("employer"));

        // Confidence and Provenance verification
        Map<?, ?> confidenceMap = (Map<?, ?>) projected.get("_confidence");
        assertNotNull(confidenceMap);
        assertEquals(0.95, confidenceMap.get("name"));

        Map<?, ?> provenanceMap = (Map<?, ?>) projected.get("_provenance");
        assertNotNull(provenanceMap);
        Provenance emailProv = (Provenance) provenanceMap.get("primary_email");
        assertNotNull(emailProv);
        assertEquals("emails", emailProv.getField());
    }

    @Test
    public void testMissingFieldOmitBehavior() {
        Map<String, Object> config = new LinkedHashMap<>();
        List<Map<String, Object>> fields = new ArrayList<>();
        // Headline isn't mapped, but links is missing
        fields.add(Map.of("path", "name", "from", "full_name"));
        fields.add(Map.of("path", "missing_links", "from", "links"));

        config.put("fields", fields);
        config.put("on_missing", "omit");

        Map<String, Object> projected = engine.project(profile, config);
        assertTrue(projected.containsKey("name"));
        assertFalse(projected.containsKey("missing_links"));
    }

    @Test
    public void testMissingRequiredFieldErrorThrows() {
        Map<String, Object> config = new LinkedHashMap<>();
        List<Map<String, Object>> fields = new ArrayList<>();
        // missing_field is required and not present
        fields.add(Map.of("path", "non_existent", "from", "non_existent", "required", true));

        config.put("fields", fields);
        config.put("on_missing", "error");

        assertThrows(ProjectionException.class, () -> engine.project(profile, config));
    }
}
