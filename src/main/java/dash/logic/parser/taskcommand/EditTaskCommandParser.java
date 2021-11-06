package dash.logic.parser.taskcommand;

import static dash.commons.core.Messages.MESSAGE_ARGUMENT_EMPTY;
import static dash.commons.core.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static dash.commons.core.Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX;
import static dash.commons.core.Messages.MESSAGE_INVALID_TASK_DISPLAYED_INDEX;
import static dash.logic.parser.CliSyntax.PREFIX_PERSON;
import static dash.logic.parser.CliSyntax.PREFIX_TAG;
import static dash.logic.parser.CliSyntax.PREFIX_TASK_DATE;
import static dash.logic.parser.CliSyntax.PREFIX_TASK_DESCRIPTION;
import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import dash.commons.core.index.Index;
import dash.logic.commands.taskcommand.AssignPeopleCommand;
import dash.logic.commands.taskcommand.EditTaskCommand;
import dash.logic.commands.taskcommand.EditTaskCommand.EditTaskDescriptor;
import dash.logic.parser.ArgumentMultimap;
import dash.logic.parser.ArgumentTokenizer;
import dash.logic.parser.ParserRequiringPersonList;
import dash.logic.parser.ParserUtil;
import dash.logic.parser.exceptions.ParseException;
import dash.model.person.Person;
import dash.model.tag.Tag;
import dash.model.task.TaskDate;
import javafx.collections.ObservableList;

/**
 * Parses input arguments and creates a new EditCommand object
 */
public class EditTaskCommandParser implements ParserRequiringPersonList<EditTaskCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the EditTaskCommand
     * and returns an EditTaskCommand object for execution.
     *
     * @throws ParseException if the user input does not conform the expected format
     */
    @Override
    public EditTaskCommand parse(String args, ObservableList<Person> filteredPersonList) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap argMultimap =
                ArgumentTokenizer.tokenize(args, PREFIX_TASK_DESCRIPTION, PREFIX_PERSON, PREFIX_TAG, PREFIX_TASK_DATE);

        Index index;

        try {
            index = ParserUtil.parseIndex(argMultimap.getPreamble());
        } catch (ParseException pe) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                    EditTaskCommand.MESSAGE_USAGE),
                    pe);
        } catch (NumberFormatException nfe) {
            throw new ParseException(MESSAGE_INVALID_TASK_DISPLAYED_INDEX);
        }

        EditTaskDescriptor editTaskDescriptor = new EditTaskDescriptor();
        if (argMultimap.getValue(PREFIX_TASK_DESCRIPTION).isPresent()) {
            editTaskDescriptor.setTaskDescription(ParserUtil
                    .parseTaskDescription(argMultimap.getValue(PREFIX_TASK_DESCRIPTION).get()));
        }

        if (argMultimap.getValue(PREFIX_TASK_DATE).isPresent()) {
            TaskDate taskdate = ParserUtil.parseTaskDate(argMultimap.getValue(PREFIX_TASK_DATE).get(), true);
            editTaskDescriptor.setTaskDate(taskdate);
        }

        if (argMultimap.getValue(PREFIX_PERSON).isPresent()) {
            if (argMultimap.getValue(PREFIX_PERSON).get().isEmpty()) {
                throw new ParseException(MESSAGE_ARGUMENT_EMPTY);
            }
            Set<Index> personIndices;
            try {
                personIndices = ParserUtil.parsePersonIndex(argMultimap.getAllValues(PREFIX_PERSON));
            } catch (ParseException pe) {
                throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                        AssignPeopleCommand.MESSAGE_USAGE),
                        pe);
            } catch (NumberFormatException nfe) {
                throw new ParseException(MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
            }
            Set<Person> people = new HashSet<>();
            for (Index i : personIndices) {
                if (i.getZeroBased() < 0 || i.getZeroBased() >= filteredPersonList.size()) {
                    throw new ParseException(MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
                }
                people.add(filteredPersonList.get(i.getZeroBased()));
            }
            editTaskDescriptor.setPeople(people);
        }

        parseTagsForEdit(argMultimap.getAllValues(PREFIX_TAG)).ifPresent(editTaskDescriptor::setTags);

        if (!editTaskDescriptor.isAnyFieldEdited()) {
            throw new ParseException(EditTaskCommand.MESSAGE_NOT_EDITED);
        }

        return new EditTaskCommand(index, editTaskDescriptor);
    }

    @Override
    public EditTaskCommand parse(String args) throws ParseException {
        return null;
    }

    /**
     * Parses {@code Collection<String> tags} into a {@code Set<Tag>} if {@code tags} is non-empty.
     * If {@code tags} contain only one element which is an empty string, it will be parsed into a
     * {@code Set<Tag>} containing zero tags.
     */
    private Optional<Set<Tag>> parseTagsForEdit(Collection<String> tags) throws ParseException {
        assert tags != null;

        if (tags.isEmpty()) {
            return Optional.empty();
        }
        Collection<String> tagSet = tags.size() == 1 && tags.contains("") ? Collections.emptySet() : tags;
        return Optional.of(ParserUtil.parseTags(tagSet));
    }
}
