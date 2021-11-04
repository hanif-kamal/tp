package dash.logic.commands.personcommand;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import dash.commons.core.Messages;
import dash.commons.util.CollectionUtil;
import dash.logic.commands.Command;
import dash.logic.commands.CommandResult;
import dash.logic.parser.CliSyntax;
import dash.model.Model;
import dash.model.person.AddressContainsKeywordsPredicate;
import dash.model.person.EmailContainsKeywordsPredicate;
import dash.model.person.NameContainsKeywordsPredicate;
import dash.model.person.Person;
import dash.model.person.PhoneContainsKeywordsPredicate;
import dash.model.person.TagPersonContainsKeywordsPredicate;

/**
 * Finds and lists all persons in address book which match all of the given arguments.
 * Keyword matching is case insensitive.
 */
public class FindPersonCommand extends Command {

    public static final String COMMAND_WORD = "find";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Finds and lists all contacts whose fields match all "
            + "of the specified arguments (case-insensitive). For more information, you may refer to the help tab.\n"
            + "Example 1: " + COMMAND_WORD + " alice\n"
            + "Example 2: " + COMMAND_WORD + " [" + CliSyntax.PREFIX_PHONE + "91234567] " + "[" + CliSyntax.PREFIX_EMAIL
            + "johndoe@example.com]\n"
            + "Example 3: " + COMMAND_WORD + " Bob " + "[" + CliSyntax.PREFIX_PHONE + "91234567]\n";

    private final FindPersonDescriptor findPersonDescriptor;
    private final Predicate<Person> predicate;

    /**
     * Constructor for the FindPersonCommand that takes in a FindPersonDescriptor.
     */
    public FindPersonCommand(FindPersonDescriptor findPersonDescriptor) {
        requireNonNull(findPersonDescriptor);

        this.findPersonDescriptor = findPersonDescriptor;
        this.predicate = findPersonDescriptor.combinePredicates();
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredPersonList(predicate);
        return new CommandResult(
                String.format(Messages.MESSAGE_PERSONS_LISTED_OVERVIEW, model.getFilteredPersonList().size()));
    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof FindPersonCommand // instanceof handles nulls
                && findPersonDescriptor.equals(((FindPersonCommand) other).findPersonDescriptor)); // state check
    }

    /**
     * Stores the predicates to find a person with. Each non-empty field value will determine
     * the fields to search for a specific person.
     */
    public static class FindPersonDescriptor {
        private NameContainsKeywordsPredicate namePredicate;
        private PhoneContainsKeywordsPredicate phonePredicate;
        private AddressContainsKeywordsPredicate addressPredicate;
        private EmailContainsKeywordsPredicate emailPredicate;
        private TagPersonContainsKeywordsPredicate tagPredicate;

        public FindPersonDescriptor() {
        }

        /**
         * Returns true if at least one predicate is present.
         */
        public boolean isAnyFieldEdited() {
            return CollectionUtil.isAnyNonNull(namePredicate, phonePredicate, emailPredicate, addressPredicate,
                    tagPredicate);
        }

        public void setName(List<String> namePredicate) {
            this.namePredicate = new NameContainsKeywordsPredicate(namePredicate);
        }

        public Optional<NameContainsKeywordsPredicate> getName() {
            return Optional.ofNullable(namePredicate);
        }

        public void setPhone(List<String> phonePredicate) {
            this.phonePredicate = new PhoneContainsKeywordsPredicate(phonePredicate);
        }

        public Optional<PhoneContainsKeywordsPredicate> getPhone() {
            return Optional.ofNullable(phonePredicate);
        }

        public void setEmail(List<String> emailPredicate) {
            this.emailPredicate = new EmailContainsKeywordsPredicate(emailPredicate);
        }

        public Optional<EmailContainsKeywordsPredicate> getEmail() {
            return Optional.ofNullable(emailPredicate);
        }

        public void setAddress(List<String> addressPredicate) {
            this.addressPredicate = new AddressContainsKeywordsPredicate(addressPredicate);
        }

        public Optional<AddressContainsKeywordsPredicate> getAddress() {
            return Optional.ofNullable(addressPredicate);
        }

        public void setTags(List<String> tagPredicate) {
            this.tagPredicate = new TagPersonContainsKeywordsPredicate(tagPredicate);
        }

        public Optional<TagPersonContainsKeywordsPredicate> getTags() {
            return Optional.ofNullable(tagPredicate);
        }

        /**
         * This method takes all the conditions to check and combines them into one predicate.
         *
         * @return A combined predicate object to use on a filteredlist.
         */
        public Predicate<Person> combinePredicates() {
            Predicate<Person> result = x -> true;
            if (namePredicate != null) {
                result = result.and(namePredicate);
            }
            if (phonePredicate != null) {
                result = result.and(phonePredicate);
            }
            if (addressPredicate != null) {
                result = result.and(addressPredicate);
            }
            if (emailPredicate != null) {
                result = result.and(emailPredicate);
            }
            if (tagPredicate != null) {
                result = result.and(tagPredicate);
            }
            return result;
        }

        @Override
        public boolean equals(Object other) {
            // short circuit if same object
            if (other == this) {
                return true;
            }

            // instanceof handles nulls
            if (!(other instanceof FindPersonCommand.FindPersonDescriptor)) {
                return false;
            }

            // state check
            FindPersonCommand.FindPersonDescriptor f = (FindPersonCommand.FindPersonDescriptor) other;

            return getName().equals(f.getName())
                    && getPhone().equals(f.getPhone())
                    && getEmail().equals(f.getEmail())
                    && getAddress().equals(f.getAddress())
                    && getTags().equals(f.getTags());
        }
    }
}
