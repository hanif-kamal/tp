package dash.logic.commands.personcommand;

import static java.util.Objects.requireNonNull;

import dash.logic.commands.Command;
import dash.logic.commands.CommandResult;
import dash.model.AddressBook;
import dash.model.Model;

/**
 * Clears all persons from the address book permanently.
 */
public class ClearPeopleCommand extends Command {

    public static final String COMMAND_WORD = "clear";
    public static final String MESSAGE_SUCCESS = "Address book has been cleared!";


    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.setAddressBook(new AddressBook());
        return new CommandResult(MESSAGE_SUCCESS);
    }
}
