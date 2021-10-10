package dash.logic.commands.taskcommand;

import static dash.logic.parser.CliSyntax.PREFIX_TAG;
import static dash.logic.parser.CliSyntax.PREFIX_TASK_DESCRIPTION;
import static java.util.Objects.requireNonNull;

import dash.logic.commands.Command;
import dash.logic.commands.CommandResult;
import dash.logic.commands.exceptions.CommandException;
import dash.model.Model;
import dash.model.task.Task;

public class AddTaskCommand extends Command {

    public static final String COMMAND_WORD = "add";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Adds a task to the task list. \n"
            + "Parameters: "
            + PREFIX_TASK_DESCRIPTION + "DESCRIPTION\n"
            + "[" + PREFIX_TAG + "TAG]...\n"
            + "Example: " + COMMAND_WORD + " "
            + PREFIX_TASK_DESCRIPTION + "CS2103T Quiz 9"
            + PREFIX_TAG + "quizzes";

    public static final String MESSAGE_SUCCESS = "New task added: %1$s";
    private final Task toAdd;

    /**
     * Creates an AddCommand to add the specified {@code Task}
     */
    public AddTaskCommand(Task task) {
        requireNonNull(task);
        toAdd = task;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        model.addTask(toAdd);
        return new CommandResult(String.format(MESSAGE_SUCCESS, toAdd));
    }

    @Override
    public boolean equals(Object otherTask) {
        return otherTask == this // short circuit if same object
                || (otherTask instanceof AddTaskCommand // instanceof handles nulls
                && toAdd.equals(((AddTaskCommand) otherTask).toAdd));
    }
}
