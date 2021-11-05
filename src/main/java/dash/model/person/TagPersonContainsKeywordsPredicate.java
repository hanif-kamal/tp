package dash.model.person;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import dash.commons.util.StringUtil;
import dash.model.tag.Tag;

/**
 * Tests that a {@code Person}'s {@code Name} matches any of the keywords given.
 */
public class TagPersonContainsKeywordsPredicate implements Predicate<Person> {
    private final List<String> keywords;

    public TagPersonContainsKeywordsPredicate(List<String> keywords) {
        this.keywords = keywords;
    }

    @Override
    public boolean test(Person person) {
        return keywords.stream()
                .allMatch(keyword -> {
                    Set<Tag> tagSet = person.getTags();
                    boolean match = false;
                    for (Tag t : tagSet) {
                        String i = t.tagName;
                        boolean condition = StringUtil.containsWordIgnoreCase(i, keyword);
                        match = condition;
                        if (condition) {
                            break;
                        }
                    }
                    return match;
                });
    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof TagPersonContainsKeywordsPredicate // instanceof handles nulls
                && keywords.equals(((TagPersonContainsKeywordsPredicate) other).keywords)); // state check
    }

}
