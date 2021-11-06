package dash.logic.parser.taskcommand;

import static dash.commons.core.Messages.MESSAGE_ARGUMENT_EMPTY;
import static dash.commons.core.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static dash.commons.core.Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX;
import static dash.commons.core.Messages.MESSAGE_INVALID_TASK_DISPLAYED_INDEX;
import static dash.logic.parser.CliSyntax.PREFIX_PERSON;
import static java.util.Objects.requireNonNull;

import java.util.HashSet;
import java.util.Set;

import dash.commons.core.index.Index;
import dash.logic.commands.taskcommand.AssignPeopleCommand;
import dash.logic.commands.taskcommand.EditTaskCommand.EditTaskDescriptor;
import dash.logic.parser.ArgumentMultimap;
import dash.logic.parser.ArgumentTokenizer;
import dash.logic.parser.ParserRequiringPersonList;
import dash.logic.parser.ParserUtil;
import dash.logic.parser.exceptions.ParseException;
import dash.model.person.Person;
import javafx.collections.ObservableList;

public class AssignPeopleCommandParser implements ParserRequiringPersonList<AssignPeopleCommand> {

    @Override
    public AssignPeopleCommand parse(String args) throws ParseException {
        return null;
    }

    @Override
    public AssignPeopleCommand parse(String args, ObservableList<Person> filteredPersonList) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args, PREFIX_PERSON);

        Index index;

        try {
            index = ParserUtil.parseIndex(argMultimap.getPreamble());
        } catch (ParseException pe) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                    AssignPeopleCommand.MESSAGE_USAGE),
                    pe);
        } catch (NumberFormatException nfe) {
            throw new ParseException(MESSAGE_INVALID_TASK_DISPLAYED_INDEX);
        }

        EditTaskDescriptor editTaskDescriptor = new EditTaskDescriptor();

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

        if (!editTaskDescriptor.isAnyFieldEdited()) {
            throw new ParseException(AssignPeopleCommand.MESSAGE_NOT_EDITED);
        }

        return new AssignPeopleCommand(index, editTaskDescriptor);
    }
}
