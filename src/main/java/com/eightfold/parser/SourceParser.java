package com.eightfold.parser;

import com.eightfold.model.CandidateProfile;
import java.io.File;
import java.io.IOException;

/**
 * Interface representing a generic parser for candidate data sources.
 */
public interface SourceParser {
    /**
     * Parses the given candidate source file.
     *
     * @param file the source file.
     * @return a raw, unnormalized CandidateProfile.
     * @throws IOException if an error occurs reading the file.
     */
    CandidateProfile parse(File file) throws IOException;

    /**
     * Returns the name of the source (e.g. "Recruiter CSV", "Resume PDF").
     *
     * @return the source name.
     */
    String getSourceName();
}
