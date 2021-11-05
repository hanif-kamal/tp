package dash.logic.commands.personcommand;

import static java.util.Objects.requireNonNull;

import java.util.List;

import dash.commons.core.Messages;
import dash.commons.core.index.Index;
import dash.logic.commands.Command;
import dash.logic.commands.CommandResult;
import dash.logic.commands.exceptions.CommandException;
import dash.model.Model;
import dash.model.person.Person;

/**
 * Deletes a person identified using its index from the address book.
 */
public class DeletePersonCommand extends Command {

    public static final String COMMAND_WORD = "delete";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Deletes the person identified by the index number used in the displayed person list.\n"
            + "Parameters: INDEX (must be a positive integer)\n"
            + "Example: " + COMMAND_WORD + " 1";

    public static final String MESSAGE_DELETE_PERSON_SUCCESS = "Deleted Person: %1$s";

    private final Index targetIndex;

    public DeletePersonCommand(Index targetIndex) {
        this.targetIndex = targetIndex;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> lastShownList = model.getFilteredPersonList();

        if (targetIndex.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        Person personToDelete = lastShownList.get(targetIndex.getZeroBased());
        model.deletePeopleFromTasks(personToDelete);
        model.deletePerson(personToDelete);
        return new CommandResult(String.format(MESSAGE_DELETE_PERSON_SUCCESS, personToDelete));
    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof DeletePersonCommand // instanceof handles nulls
                && targetIndex.equals(((DeletePersonCommand) other).targetIndex)); // state check
    }
}
