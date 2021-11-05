package dash.model.task;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import dash.commons.util.StringUtil;
import dash.model.tag.Tag;

/**
 * Tests that a {@code Task}'s {@code TaskDescription} matches any of the keywords given.
 */
public class TagTaskContainsKeywordsPredicate implements Predicate<Task> {
    private final List<String> keywords;

    public TagTaskContainsKeywordsPredicate(List<String> keywords) {
        this.keywords = keywords;
    }

    @Override
    public boolean test(Task task) {
        return keywords.stream()
                .allMatch(keyword -> {
                    Set<Tag> tagSet = task.getTags();
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
                || (other instanceof TagTaskContainsKeywordsPredicate // instanceof handles nulls
                && keywords.equals(((TagTaskContainsKeywordsPredicate) other).keywords)); // state check
    }

}
