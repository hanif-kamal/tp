package dash.logic.commands.taskcommand;

import static java.util.Objects.requireNonNull;

import java.util.List;

import dash.commons.core.Messages;
import dash.commons.core.index.Index;
import dash.logic.commands.Command;
import dash.logic.commands.CommandResult;
import dash.logic.commands.exceptions.CommandException;
import dash.model.Model;
import dash.model.task.CompletionStatus;
import dash.model.task.Task;

/**
 * Sets a task in the task list as completed.
 */
public class CompleteTaskCommand extends Command {

    public static final String COMMAND_WORD = "complete";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Completes the task identified by the index number used in the task list.\n"
            + "Parameters: INDEX (must be a positive integer)\n"
            + "Example: " + COMMAND_WORD + " 1";

    public static final String MESSAGE_COMPLETE_TASK_SUCCESS = "Completed Task: %1$s";

    private final Index targetIndex;

    public CompleteTaskCommand(Index targetIndex) {
        this.targetIndex = targetIndex;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Task> lastShownList = model.getFilteredTaskList();

        if (targetIndex.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_TASK_DISPLAYED_INDEX);
        }

        Task taskToComplete = lastShownList.get(targetIndex.getZeroBased());
        Task completedTask = createCompletedTask(taskToComplete);

        model.setTask(targetIndex.getZeroBased(), completedTask);

        model.updateFilteredTaskList(Model.PREDICATE_SHOW_ALL_TASKS);
        return new CommandResult(String.format(MESSAGE_COMPLETE_TASK_SUCCESS, completedTask));
    }

    /**
     * Creates and returns a {@code Task} with the details of {@code taskToComplete}, but with CompletionStatus
     * encapsulating true.
     */
    private static Task createCompletedTask(Task taskToComplete) {
        return new Task(taskToComplete.getTaskDescription(), new CompletionStatus(true),
                taskToComplete.getTaskDate(), taskToComplete.getPeople(), taskToComplete.getTags());
    }

    @Override
    public boolean equals(Object other) {
        // short circuit if same object
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof CompleteTaskCommand)) {
            return false;
        }

        // state check
        CompleteTaskCommand c = (CompleteTaskCommand) other;
        return targetIndex.equals(c.targetIndex);
    }
}
