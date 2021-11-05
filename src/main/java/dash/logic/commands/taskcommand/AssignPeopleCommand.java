package dash.logic.commands.taskcommand;

import static dash.logic.parser.CliSyntax.PREFIX_PERSON;
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
import dash.model.person.Person;
import dash.model.task.Task;

/**
 * Assigns people to a task. Multiple people can be assigned to a task at once.
 */
public class AssignPeopleCommand extends Command {

    public static final String COMMAND_WORD = "assign";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Assigns people to task "
            + "by the index number used in the displayed task list. "
            + "Existing people will not be overwritten.\n"
            + "Parameters: INDEX (must be a positive integer) "
            + "[" + PREFIX_PERSON + "INDEX]...\n"
            + "Example: " + COMMAND_WORD + " 1 "
            + PREFIX_PERSON + "2 "
            + PREFIX_PERSON + "4 ";

    public static final String MESSAGE_EDIT_TASK_SUCCESS = "People assigned to: %1$s";
    public static final String MESSAGE_NOT_EDITED = "At least one person to add must be provided.";

    private final Index index;
    private final EditTaskDescriptor editTaskDescriptor;

    /**
     * @param index of the task in the filtered task list to assign people
     * @param editTaskDescriptor people to add to the task
     */
    public AssignPeopleCommand(Index index, EditTaskDescriptor editTaskDescriptor) {
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

        Task taskToAssign = lastShownList.get(index.getZeroBased());
        Task peopleAssignedTask = assignPeople(taskToAssign, editTaskDescriptor);

        model.setTask(index.getZeroBased(), peopleAssignedTask);
        model.updateFilteredTaskList(Model.PREDICATE_SHOW_ALL_TASKS);
        return new CommandResult(String.format(MESSAGE_EDIT_TASK_SUCCESS, peopleAssignedTask));
    }

    /**
     * Creates and returns a {@code Task} with the people list in {@code editTaskDescriptor}
     * added on to {@code taskToEdit}.
     */
    private static Task assignPeople(Task taskToEdit, EditTaskDescriptor editTaskDescriptor) {
        assert taskToEdit != null;

        Set<Person> oldPeople = taskToEdit.getPeople();
        Set<Person> newPeople = editTaskDescriptor.getPeople().orElse(new HashSet<>());
        Set<Person> updatedPeople = new HashSet<>(oldPeople);
        updatedPeople.removeIf(newPeople::contains);
        updatedPeople.addAll(newPeople);

        return new Task(taskToEdit.getTaskDescription(), taskToEdit.getCompletionStatus(),
                taskToEdit.getTaskDate(), updatedPeople, taskToEdit.getTags());
    }
}
