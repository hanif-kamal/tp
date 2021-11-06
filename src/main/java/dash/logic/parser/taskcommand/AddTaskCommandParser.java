package dash.logic.parser.taskcommand;

import static dash.commons.core.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static dash.commons.core.Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX;
import static dash.logic.parser.CliSyntax.PREFIX_PERSON;
import static dash.logic.parser.CliSyntax.PREFIX_TAG;
import static dash.logic.parser.CliSyntax.PREFIX_TASK_DATE;
import static dash.logic.parser.CliSyntax.PREFIX_TASK_DESCRIPTION;

import java.util.HashSet;
import java.util.Set;

import dash.commons.core.index.Index;
import dash.logic.commands.taskcommand.AddTaskCommand;
import dash.logic.parser.ArgumentMultimap;
import dash.logic.parser.ArgumentTokenizer;
import dash.logic.parser.ParserRequiringPersonList;
import dash.logic.parser.ParserUtil;
import dash.logic.parser.exceptions.ParseException;
import dash.model.person.Person;
import dash.model.tag.Tag;
import dash.model.task.CompletionStatus;
import dash.model.task.Task;
import dash.model.task.TaskDate;
import dash.model.task.TaskDescription;
import javafx.collections.ObservableList;

/**
 * Parses input arguments and creates a new AddTaskCommand object
 */
public class AddTaskCommandParser implements ParserRequiringPersonList<AddTaskCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the AddTaskCommand
     * and returns an AddTaskCommand object for execution.
     *
     * @throws ParseException if the user input does not conform the expected format
     */
    public AddTaskCommand parse(String args, ObservableList<Person> filteredPersonList) throws ParseException {
        ArgumentMultimap argMultimap =
                ArgumentTokenizer.tokenize(args, PREFIX_TASK_DESCRIPTION, PREFIX_TASK_DATE, PREFIX_PERSON, PREFIX_TAG);
        boolean isDescriptionPrefixPresent = argMultimap.getValue(PREFIX_TASK_DESCRIPTION).isPresent();
        boolean isTaskDatePrefixPresent = argMultimap.getValue(PREFIX_TASK_DATE).isPresent();
        Set<Index> personIndices;
        try {
            personIndices = ParserUtil.parsePersonIndex(argMultimap.getAllValues(PREFIX_PERSON));
        } catch (ParseException pe) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddTaskCommand.MESSAGE_USAGE));
        } catch (NumberFormatException nfe) {
            throw new ParseException(MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }
        Set<Person> people = new HashSet<>();
        TaskDate taskDate;

        if (!isDescriptionPrefixPresent || !argMultimap.getPreamble().isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                    AddTaskCommand.MESSAGE_USAGE));
        }
        for (Index index : personIndices) {
            if (index.getZeroBased() < 0 || index.getZeroBased() >= filteredPersonList.size()) {
                throw new ParseException(MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
            }
            people.add(filteredPersonList.get(index.getZeroBased()));
        }

        if (isTaskDatePrefixPresent) {
            taskDate = ParserUtil.parseTaskDate(argMultimap.getValue(PREFIX_TASK_DATE).get(), false);
        } else {
            taskDate = new TaskDate();
        }

        TaskDescription description =
                ParserUtil.parseTaskDescription(argMultimap.getValue(PREFIX_TASK_DESCRIPTION).get());
        CompletionStatus completionStatus = new CompletionStatus(false);
        Set<Tag> tagList = ParserUtil.parseTags(argMultimap.getAllValues(PREFIX_TAG));

        Task task = new Task(description, completionStatus, taskDate, people, tagList);

        return new AddTaskCommand(task);
    }

    @Override
    public AddTaskCommand parse(String userInput) throws ParseException {
        return null;
    }
}
