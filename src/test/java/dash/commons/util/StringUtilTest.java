package dash.commons.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileNotFoundException;

import org.junit.jupiter.api.Test;

import dash.testutil.Assert;

public class StringUtilTest {

    //---------------- Tests for isNumeric --------------------------------------

    @Test
    public void isNumeric() {
        // EP: empty strings
        assertFalse(StringUtil.isNumeric("")); // Boundary value
        assertFalse(StringUtil.isNumeric("  "));

        // EP: not a number
        assertFalse(StringUtil.isNumeric("a"));
        assertFalse(StringUtil.isNumeric("aaa"));
        assertFalse(StringUtil.isNumeric("p/1"));

        // EP: leading character not minus sign
        assertFalse(StringUtil.isNumeric("+1"));
        assertFalse(StringUtil.isNumeric("/1"));

        // EP: numbers with white space
        assertFalse(StringUtil.isNumeric(" 10 ")); // Leading/trailing spaces
        assertFalse(StringUtil.isNumeric("1 0")); // Spaces in the middle

        // EP: numeric values, should return true
        assertTrue(StringUtil.isNumeric("1"));
        assertTrue(StringUtil.isNumeric("10"));
        assertTrue(StringUtil.isNumeric("01"));
        assertTrue(StringUtil.isNumeric(Long.toString(Long.MAX_VALUE)));
        assertTrue(StringUtil.isNumeric("-1"));
        assertTrue(StringUtil.isNumeric("1.2"));
    }


    //---------------- Tests for containsWordIgnoreCase --------------------------------------

    /*
     * Invalid equivalence partitions for word: null, empty, multiple words
     * Invalid equivalence partitions for sentence: null
     * The four test cases below test one invalid input at a time.
     */

    @Test
    public void containsWordIgnoreCase_nullWord_throwsNullPointerException() {
        Assert.assertThrows(NullPointerException.class, () -> StringUtil.containsWordIgnoreCase(
                "typical sentence", null));
    }

    @Test
    public void containsWordIgnoreCase_emptyWord_throwsIllegalArgumentException() {
        Assert.assertThrows(IllegalArgumentException.class,
                "Word parameter cannot be empty", () -> StringUtil.containsWordIgnoreCase(
                        "typical sentence", "  "));
    }

    @Test
    public void containsWordIgnoreCase_multipleWords_throwsIllegalArgumentException() {
        Assert.assertThrows(IllegalArgumentException.class,
                "Word parameter should be a single word", () -> StringUtil.containsWordIgnoreCase(
                        "typical sentence", "aaa BBB"));
    }

    @Test
    public void containsWordIgnoreCase_nullSentence_throwsNullPointerException() {
        Assert.assertThrows(NullPointerException.class, () -> StringUtil.containsWordIgnoreCase(
                null, "abc"));
    }

    /*
     * Valid equivalence partitions for word:
     *   - any word
     *   - word containing symbols/numbers
     *   - word with leading/trailing spaces
     *
     * Valid equivalence partitions for sentence:
     *   - empty string
     *   - one word
     *   - multiple words
     *   - sentence with extra spaces
     *
     * Possible scenarios returning true:
     *   - matches first word in sentence
     *   - last word in sentence
     *   - middle word in sentence
     *   - matches multiple words
     *
     * Possible scenarios returning false:
     *   - query word matches part of a sentence word
     *   - sentence word matches part of the query word
     *
     * The test method below tries to verify all above with a reasonably low number of test cases.
     */

    @Test
    public void containsWordIgnoreCase_validInputs_correctResult() {

        // Empty sentence
        assertFalse(StringUtil.containsWordIgnoreCase("", "abc")); // Boundary case
        assertFalse(StringUtil.containsWordIgnoreCase("    ", "123"));

        // Matches a partial word only
        assertFalse(StringUtil.containsWordIgnoreCase("aaa bbb ccc", "bb")); // Sentence word bigger than query word
        assertFalse(StringUtil.containsWordIgnoreCase("aaa bbb ccc", "bbbb")); // Query word bigger than sentence word

        // Matches word in the sentence, different upper/lower case letters
        assertTrue(StringUtil.containsWordIgnoreCase("aaa bBb ccc", "Bbb")); // First word (boundary case)
        assertTrue(StringUtil.containsWordIgnoreCase("aaa bBb ccc@1", "CCc@1")); // Last word (boundary case)
        assertTrue(StringUtil.containsWordIgnoreCase("  AAA   bBb   ccc  ", "aaa")); // Sentence has extra spaces
        assertTrue(StringUtil.containsWordIgnoreCase("Aaa", "aaa")); // Only one word in sentence (boundary case)
        assertTrue(StringUtil.containsWordIgnoreCase("aaa bbb ccc", "  ccc  ")); // Leading/trailing spaces

        // Matches multiple words in sentence
        assertTrue(StringUtil.containsWordIgnoreCase("AAA bBb ccc  bbb", "bbB"));
    }

    //---------------- Tests for getDetails --------------------------------------

    /*
     * Equivalence Partitions: null, valid throwable object
     */

    @Test
    public void getDetails_exceptionGiven() {
        assertTrue(StringUtil.getDetails(new FileNotFoundException("file not found"))
                .contains("java.io.FileNotFoundException: file not found"));
    }

    @Test
    public void getDetails_nullGiven_throwsNullPointerException() {
        Assert.assertThrows(NullPointerException.class, () -> StringUtil.getDetails(null));
    }

}
