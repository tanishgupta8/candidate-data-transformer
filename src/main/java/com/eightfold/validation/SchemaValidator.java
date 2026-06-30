package com.eightfold.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Set;

/**
 * Validates projected candidate JSON data against a JSON Schema.
 */
public final class SchemaValidator {
    private static final Logger logger = LoggerFactory.getLogger(SchemaValidator.class);
    private final JsonSchema jsonSchema;
    private final ObjectMapper objectMapper;

    /**
     * Initializes the SchemaValidator using a schema input stream.
     *
     * @param schemaStream the InputStream for the schema file.
     */
    public SchemaValidator(InputStream schemaStream) {
        this.objectMapper = new ObjectMapper();
        if (schemaStream == null) {
            throw new IllegalArgumentException("Schema stream cannot be null");
        }
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
        this.jsonSchema = factory.getSchema(schemaStream);
    }

    /**
     * Validates the provided JSON string against the loaded schema.
     *
     * @param jsonContent the raw JSON content to validate.
     * @return true if valid, false otherwise.
     */
    public boolean validate(String jsonContent) {
        if (jsonContent == null || jsonContent.isBlank()) {
            logger.error("JSON content is empty or null");
            return false;
        }

        try {
            JsonNode jsonNode = objectMapper.readTree(jsonContent);
            Set<ValidationMessage> validationMessages = jsonSchema.validate(jsonNode);

            if (validationMessages.isEmpty()) {
                logger.info("Validation successful");
                return true;
            } else {
                logger.warn("JSON Schema validation failed: {} errors found.", validationMessages.size());
                for (ValidationMessage message : validationMessages) {
                    logger.warn("  - Path: {}, Type: {}, Message: {}", message.getPath(), message.getType(), message.getMessage());
                }
                return false;
            }
        } catch (Exception e) {
            logger.error("Failed to execute JSON Schema validation: {}", e.getMessage());
            return false;
        }
    }
}
