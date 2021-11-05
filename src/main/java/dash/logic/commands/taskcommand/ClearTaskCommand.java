package dash.logic.commands.taskcommand;

import static java.util.Objects.requireNonNull;

import dash.logic.commands.Command;
import dash.logic.commands.CommandResult;
import dash.logic.commands.exceptions.CommandException;
import dash.model.Model;
import dash.model.task.TaskList;

/**
 * Clears all tasks from the task list permanently.
 */
public class ClearTaskCommand extends Command {

    public static final String COMMAND_WORD = "clear";
    public static final String MESSAGE_SUCCESS = "Task List has been cleared!";

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        model.setTaskList(new TaskList());
        return new CommandResult(MESSAGE_SUCCESS);
    }
}
