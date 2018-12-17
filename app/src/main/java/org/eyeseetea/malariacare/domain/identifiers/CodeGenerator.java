package org.eyeseetea.malariacare.domain.identifiers;

import java.security.SecureRandom;
import java.util.regex.Pattern;

//Class copied from Dhis2 SDK
public class CodeGenerator {

    private static final Pattern CODE_PATTERN = Pattern.compile("^[a-zA-Z]{1}[a-zA-Z0-9]{10}$");
    private static final String LETTERS = "abcdefghijklmnopqrstuvwxyz"
            + "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String ALLOWED_CHARS = "0123456789" + LETTERS;
    private static final int NUMBER_OF_CODE_POINTS = ALLOWED_CHARS.length();
    private static final int CODE_SIZE = 11;

    /**
     * Generates a pseudo random string using the allowed characters.
     * Code is 11 characters long.
     *
     * @return the code.
     */
    public static String generateCode() {
        return generateCode(CODE_SIZE);
    }

    /**
     * Generates a pseudo random string using the allowed characters.
     *
     * @param codeSize the number of characters in the code.
     * @return the code.
     */
    public static String generateCode(int codeSize) {
        // Using the system default algorithm and seed
        SecureRandom sr = new SecureRandom();

        char[] randomChars = new char[codeSize];

        // first char should be a letter
        randomChars[0] = LETTERS.charAt(sr.nextInt(LETTERS.length()));

        for (int i = 1; i < codeSize; ++i) {
            randomChars[i] = ALLOWED_CHARS.charAt(sr.nextInt(NUMBER_OF_CODE_POINTS));
        }

        return new String(randomChars);
    }

    /**
     * Tests whether the given code is valid.
     *
     * @param code the code to validate.
     * @return true if the code is valid.
     */
    public static boolean isValidCode(String code) {
        return code != null && CODE_PATTERN.matcher(code).matches();
    }
}
