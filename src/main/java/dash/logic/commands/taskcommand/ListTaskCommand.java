package dash.logic.commands.taskcommand;

import static java.util.Objects.requireNonNull;

import dash.logic.commands.Command;
import dash.logic.commands.CommandResult;
import dash.model.Model;

/**
 * Lists all tasks in the task list.
 */
public class ListTaskCommand extends Command {

    public static final String COMMAND_WORD = "list";

    public static final String MESSAGE_SUCCESS = "Listed all tasks";


    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredTaskList(Model.PREDICATE_SHOW_ALL_TASKS);
        return new CommandResult(MESSAGE_SUCCESS);
    }
}
