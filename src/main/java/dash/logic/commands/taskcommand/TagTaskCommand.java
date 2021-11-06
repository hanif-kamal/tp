package dash.logic.commands.taskcommand;

import static dash.logic.parser.CliSyntax.PREFIX_TAG;
import static java.util.Objects.requireNonNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dash.commons.core.Messages;
import dash.commons.core.index.Index;
import dash.logic.commands.Command;
import dash.logic.commands.CommandResult;
import dash.logic.commands.exceptions.CommandException;
import dash.logic.commands.taskcommand.EditTaskCommand.EditTaskDescriptor;
import dash.model.Model;
import dash.model.tag.Tag;
import dash.model.task.Task;

public class TagTaskCommand extends Command {

    public static final String COMMAND_WORD = "tag";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Tags task with provided additional tags "
            + "by the index number used in the displayed task list. "
            + "Existing tags will not be overwritten.\n"
            + "Parameters: INDEX (must be a positive integer) "
            + "[" + PREFIX_TAG + "TAG]...\n"
            + "Example: " + COMMAND_WORD + " 1 "
            + PREFIX_TAG + "lecture "
            + PREFIX_TAG + "important ";

    public static final String MESSAGE_EDIT_TASK_SUCCESS = "Tags added to: %1$s";
    public static final String MESSAGE_NOT_EDITED = "At least one tag to add must be provided.";

    private final Index index;
    private final EditTaskDescriptor editTaskDescriptor;

    /**
     * @param index of the task in the filtered task list to tag
     * @param editTaskDescriptor tags to add to the task
     */
    public TagTaskCommand(Index index, EditTaskDescriptor editTaskDescriptor) {
        requireNonNull(index);
        requireNonNull(editTaskDescriptor);

        this.index = index;
        this.editTaskDescriptor = new EditTaskDescriptor(editTaskDescriptor);
    }
    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Task> lastShownList = model.getFilteredTaskList();

        if (index.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_TASK_DISPLAYED_INDEX);
        }

        Task taskToTag = lastShownList.get(index.getZeroBased());
        Task tagsAddedTask = addTags(taskToTag, editTaskDescriptor);

        model.setTask(index.getZeroBased(), tagsAddedTask);
        model.updateFilteredTaskList(Model.PREDICATE_SHOW_ALL_TASKS);
        return new CommandResult(String.format(MESSAGE_EDIT_TASK_SUCCESS, tagsAddedTask));
    }

    /**
     * Creates and returns a {@code Task} with the details of {@code taskToEdit}
     * edited with {@code editTaskDescriptor}.
     */
    private static Task addTags(Task taskToEdit, EditTaskDescriptor editTaskDescriptor) {
        assert taskToEdit != null;

        Set<Tag> oldTags = taskToEdit.getTags();
        Set<Tag> newTags = editTaskDescriptor.getTags().orElse(new HashSet<>());
        Set<Tag> updatedTags = new HashSet<>(oldTags);
        updatedTags.removeIf(newTags::contains);
        updatedTags.addAll(newTags);

        return new Task(taskToEdit.getTaskDescription(), taskToEdit.getCompletionStatus(),
                taskToEdit.getTaskDate(), taskToEdit.getPeople(), updatedTags);
    }

    @Override
    public boolean equals(Object other) {
        // short circuit if same object
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof TagTaskCommand)) {
            return false;
        }

        // state check
        TagTaskCommand e = (TagTaskCommand) other;
        return index.equals(e.index)
                && editTaskDescriptor.equals(e.editTaskDescriptor);
    }
}
