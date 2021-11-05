package dash.logic.commands.taskcommand;

import static dash.logic.parser.CliSyntax.PREFIX_TAG;
import static dash.logic.parser.CliSyntax.PREFIX_TASK_DATE;
import static dash.logic.parser.CliSyntax.PREFIX_TASK_DESCRIPTION;
import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import dash.commons.core.Messages;
import dash.commons.core.index.Index;
import dash.commons.util.CollectionUtil;
import dash.logic.commands.Command;
import dash.logic.commands.CommandResult;
import dash.logic.commands.exceptions.CommandException;
import dash.model.Model;
import dash.model.person.Person;
import dash.model.tag.Tag;
import dash.model.task.CompletionStatus;
import dash.model.task.Task;
import dash.model.task.TaskDate;
import dash.model.task.TaskDescription;

/**
 * Edits the details of an existing task in the task list.
 */
public class EditTaskCommand extends Command {

    public static final String COMMAND_WORD = "edit";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Edits the details of the task identified "
            + "by the index number used in the displayed task list. "
            + "Existing values will be overwritten by the input values.\n"
            + "Parameters: INDEX (must be a positive integer) "
            + "[" + PREFIX_TASK_DESCRIPTION + "TASK_DESCRIPTION] "
            + "[" + PREFIX_TASK_DATE + "DATE]\n"
            + "[" + PREFIX_TASK_DATE + "TIME]\n"
            + "[" + PREFIX_TASK_DATE + "DATE, TIME]\n"
            + "[" + PREFIX_TAG + "TAG]...\n"
            + "Example: " + COMMAND_WORD + " 1 "
            + PREFIX_TASK_DESCRIPTION + "Watch ST2334 Lecture 9 "
            + PREFIX_TASK_DATE + "10/10/2021, 1400 "
            + PREFIX_TAG + "lecture";

    public static final String MESSAGE_EDIT_TASK_SUCCESS = "Task is now: %1$s";
    public static final String MESSAGE_NOT_EDITED = "At least one field to edit must be provided.";

    private final Index index;
    private final EditTaskDescriptor editTaskDescriptor;

    /**
     * @param index of the task in the filtered task list to edit
     * @param editTaskDescriptor details to edit the task with
     */
    public EditTaskCommand(Index index, EditTaskDescriptor editTaskDescriptor) {
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

        Task taskToEdit = lastShownList.get(index.getZeroBased());
        Task editedTask = createEditedTask(taskToEdit, editTaskDescriptor);
        int indexOfTaskToEdit = model.getIndexToEdit(index.getZeroBased(), taskToEdit, lastShownList);

        model.setTask(indexOfTaskToEdit, editedTask);
        model.updateFilteredTaskList(Model.PREDICATE_SHOW_ALL_TASKS);
        return new CommandResult(String.format(MESSAGE_EDIT_TASK_SUCCESS, editedTask));
    }

    /**
     * Creates and returns a {@code Task} with the details of {@code taskToEdit}
     * edited with {@code editTaskDescriptor}.
     */
    private static Task createEditedTask(Task taskToEdit, EditTaskDescriptor editTaskDescriptor) {
        assert taskToEdit != null;

        TaskDescription updatedDescription = editTaskDescriptor.getTaskDescription()
                .orElse(taskToEdit.getTaskDescription());
        CompletionStatus updatedCompletionStatus = editTaskDescriptor.getCompletionStatus()
                .orElse(taskToEdit.getCompletionStatus());

        TaskDate updatedTaskDate = getUpdatedTaskDate(editTaskDescriptor.getTaskDate(), taskToEdit.getTaskDate());

        Set<Person> updatedPeople = editTaskDescriptor.getPeople().orElse(taskToEdit.getPeople());
        Set<Tag> updatedTags = editTaskDescriptor.getTags().orElse(taskToEdit.getTags());

        return new Task(updatedDescription, updatedCompletionStatus,
                updatedTaskDate, updatedPeople, updatedTags);
    }

    private static TaskDate getUpdatedTaskDate(Optional<TaskDate> taskDate, TaskDate taskDateToEdit) {
        if (taskDate.isEmpty()) {
            return taskDateToEdit;
        }
        TaskDate editTaskDate = taskDate.get();
        if (!editTaskDate.hasDate()) {
            if (taskDateToEdit.hasDate()) {
                return new TaskDate(taskDateToEdit.toDateString() + "," + editTaskDate.toTimeString());
            }
        }
        if (!editTaskDate.hasTime()) {
            if (taskDateToEdit.hasDate() && taskDateToEdit.hasTime()) {
                return new TaskDate(editTaskDate.toDateString() + "," + taskDateToEdit.toTimeString());
            }
        }

        return new TaskDate(editTaskDate.toString());
    }

    @Override
    public boolean equals(Object other) {
        // short circuit if same object
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof EditTaskCommand)) {
            return false;
        }

        // state check
        EditTaskCommand e = (EditTaskCommand) other;
        return index.equals(e.index)
                && editTaskDescriptor.equals(e.editTaskDescriptor);
    }

    /**
     * Stores the details to edit the task with. Each non-empty field value will replace the
     * corresponding field value of the task.
     */
    public static class EditTaskDescriptor {
        private TaskDescription taskDescription;
        private Set<Person> people;
        private Set<Tag> tags;
        private CompletionStatus completionStatus;
        private TaskDate taskDate;

        public EditTaskDescriptor() {
        }

        /**
         * Copy constructor.
         * A defensive copy of {@code tags} is used internally.
         */
        public EditTaskDescriptor(EditTaskDescriptor toCopy) {
            setTaskDescription(toCopy.taskDescription);
            setPeople(toCopy.people);
            setTags(toCopy.tags);
            setCompletionStatus(toCopy.completionStatus);
            setTaskDate(toCopy.taskDate);
        }

        /**
         * Returns true if at least one field is edited.
         */
        public boolean isAnyFieldEdited() {
            return CollectionUtil.isAnyNonNull(taskDescription, completionStatus, people, taskDate, tags);
        }

        /**
         * Sets {@code description} to this object's {@code description}.
         * A defensive copy of {@code people} is used internally.
         */
        public void setTaskDescription(TaskDescription description) {
            this.taskDescription = description;
        }

        public Optional<TaskDescription> getTaskDescription() {
            return Optional.ofNullable(taskDescription);
        }

        /**
         * Sets {@code people} to this object's {@code people}.
         * A defensive copy of {@code people} is used internally.
         */
        public void setPeople(Set<Person> people) {
            this.people = (people != null) ? new HashSet<>(people) : null;
        }

        /**
         * Returns an unmodifiable people set, which throws {@code UnsupportedOperationException}
         * if modification is attempted.
         * Returns {@code Optional#empty()} if {@code people} is null.
         */
        public Optional<Set<Person>> getPeople() {
            return (people != null) ? Optional.of(Collections.unmodifiableSet(people)) : Optional.empty();
        }

        /**
         * Sets {@code tags} to this object's {@code tags}.
         * A defensive copy of {@code tags} is used internally.
         */
        public void setTags(Set<Tag> tags) {
            this.tags = (tags != null) ? new HashSet<>(tags) : null;
        }


        /**
         * Returns an unmodifiable tag set, which throws {@code UnsupportedOperationException}
         * if modification is attempted.
         * Returns {@code Optional#empty()} if {@code tags} is null.
         */
        public Optional<Set<Tag>> getTags() {
            return (tags != null) ? Optional.of(Collections.unmodifiableSet(tags)) : Optional.empty();
        }

        /**
         * Sets {@code completionStatus} to this object's {@code completionStatus}.
         * A defensive copy of {@code completionStatus} is used internally.
         */
        public void setCompletionStatus(CompletionStatus completionStatus) {
            this.completionStatus = completionStatus;
        }

        /**
         * Returns an unmodifiable tag set, which throws {@code UnsupportedOperationException}
         * if modification is attempted.
         * Returns {@code Optional#empty()} if {@code completionStatus} is null.
         */
        public Optional<CompletionStatus> getCompletionStatus() {
            return Optional.ofNullable(completionStatus);
        }

        /**
         * Sets {@code taskDate} to this object's {@code taskDate}.
         * A defensive copy of {@code taskDate} is used internally.
         */
        public void setTaskDate(TaskDate taskDate) {
            this.taskDate = taskDate;
        }

        /**
         * Returns an unmodifiable tag set, which throws {@code UnsupportedOperationException}
         * if modification is attempted.
         * Returns {@code Optional#empty()} if {@code taskDate} is null.
         */
        public Optional<TaskDate> getTaskDate() {
            return Optional.ofNullable(taskDate);
        }

        @Override
        public boolean equals(Object other) {
            // short circuit if same object
            if (other == this) {
                return true;
            }

            // instanceof handles nulls
            if (!(other instanceof EditTaskDescriptor)) {
                return false;
            }

            // state check
            EditTaskDescriptor e = (EditTaskDescriptor) other;

            return getTaskDescription().equals(e.getTaskDescription())
                    && getCompletionStatus().equals(e.getCompletionStatus())
                    && getPeople().equals(e.getPeople())
                    && getTags().equals(e.getTags())
                    && getTaskDate().equals(e.getTaskDate());
        }
    }
}
