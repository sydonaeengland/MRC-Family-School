package utils;

import java.util.Arrays;
import java.util.List;

/**
 * SUBJECTS UTILITY
 * - Provides a list of available subjects.
 * - Used throughout the system for subject selection and validation.
 */

public class Subjects {
    /**
     * GET AVAILABLE SUBJECTS
     * - Returns a list of subjects.
     */

  public static List<String> getAvailableSubjects() {
        return Arrays.asList(
            "Mathematics",
            "Physics",
            "Chemistry",
            "Biology"
        );
    }
  
}
