package dash.logic.parser.taskcommand;

import static dash.commons.core.Messages.MESSAGE_ARGUMENT_EMPTY;
import static dash.commons.core.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static dash.commons.core.Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX;
import static dash.logic.parser.CliSyntax.PREFIX_COMPLETION_STATUS;
import static dash.logic.parser.CliSyntax.PREFIX_PERSON;
import static dash.logic.parser.CliSyntax.PREFIX_TAG;
import static dash.logic.parser.CliSyntax.PREFIX_TASK_DATE;
import static dash.logic.parser.CliSyntax.PREFIX_TASK_DESCRIPTION;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import dash.commons.core.index.Index;
import dash.logic.commands.taskcommand.AssignPeopleCommand;
import dash.logic.commands.taskcommand.FindTaskCommand;
import dash.logic.commands.taskcommand.FindTaskCommand.FindTaskDescriptor;
import dash.logic.parser.ArgumentMultimap;
import dash.logic.parser.ArgumentTokenizer;
import dash.logic.parser.ParserRequiringPersonList;
import dash.logic.parser.ParserUtil;
import dash.logic.parser.exceptions.ParseException;
import dash.model.person.Person;
import dash.model.task.TaskDate;
import javafx.collections.ObservableList;


/**
 * Parses input arguments and creates a new FindCommand object
 */
public class FindTaskCommandParser implements ParserRequiringPersonList<FindTaskCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the FindCommand
     * and returns a FindCommand object for execution.
     *
     * @throws ParseException if the user input does not conform the expected format
     */
    @Override
    public FindTaskCommand parse(String args, ObservableList<Person> filteredPersonList) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap argMultimap =
                ArgumentTokenizer.tokenize(args, PREFIX_TASK_DESCRIPTION, PREFIX_TASK_DATE, PREFIX_TAG, PREFIX_PERSON,
                        PREFIX_COMPLETION_STATUS);
        String preamble = argMultimap.getPreamble();
        Index index;

        FindTaskDescriptor findTaskDescriptor = new FindTaskDescriptor();
        boolean descPresent = argMultimap.getValue(PREFIX_TASK_DESCRIPTION).isPresent();
        boolean datePresent = argMultimap.getValue(PREFIX_TASK_DATE).isPresent();
        boolean tagPresent = parseTagsForFind(argMultimap.getAllValues(PREFIX_TAG)).isPresent();
        boolean personPresent = argMultimap.getValue(PREFIX_PERSON).isPresent();
        boolean completionStatusPresent = argMultimap.getValue(PREFIX_COMPLETION_STATUS).isPresent();
        if (preamble.isEmpty() && !descPresent && !tagPresent && !datePresent && !personPresent
                && !completionStatusPresent) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindTaskCommand.MESSAGE_USAGE));
        } else if (!preamble.isEmpty()) {
            String[] preambleKeywords = preamble.split("\\s+");
            findTaskDescriptor.setDesc(Arrays.asList(preambleKeywords));
        }
        //if both preamble and desc prefix specified, desc prefix will override
        if (descPresent) {
            if (argMultimap.getValue(PREFIX_TASK_DESCRIPTION).get().isEmpty()) {
                throw new ParseException(MESSAGE_ARGUMENT_EMPTY);
            }
            String[] nameKeywords = argMultimap.getValue(PREFIX_TASK_DESCRIPTION).get().split("\\s+");
            findTaskDescriptor.setDesc(Arrays.asList(nameKeywords));
        }
        if (datePresent) {
            if (argMultimap.getValue(PREFIX_TASK_DATE).get().isEmpty()) {
                throw new ParseException(MESSAGE_ARGUMENT_EMPTY);
            }
            TaskDate taskDateArg = ParserUtil.parseTaskDate(argMultimap.getValue(PREFIX_TASK_DATE).get(), true);
            findTaskDescriptor.setDate(taskDateArg);
        }
        if (tagPresent) {
            if (argMultimap.getValue(PREFIX_TAG).get().isEmpty()) {
                throw new ParseException(MESSAGE_ARGUMENT_EMPTY);
            }
            parseTagsForFind(argMultimap.getAllValues(PREFIX_TAG)).ifPresent(findTaskDescriptor::setTags);
        }
        if (personPresent) {
            if (argMultimap.getValue(PREFIX_PERSON).get().isEmpty()) {
                throw new ParseException(MESSAGE_ARGUMENT_EMPTY);
            }
            List<String> personKeywords = new ArrayList<>();
            if (argMultimap.getValue(PREFIX_PERSON).isPresent()) {
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
                Set<String> peopleNames = new HashSet<>();
                for (Index i : personIndices) {
                    if (i.getZeroBased() < 0 || i.getZeroBased() >= filteredPersonList.size()) {
                        throw new ParseException(MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
                    }
                    Person person = (filteredPersonList.get(i.getZeroBased()));
                    peopleNames.add(person.getName().fullName);
                }
                personKeywords = new ArrayList<>(peopleNames);
            }

            findTaskDescriptor.setPerson(personKeywords);
        }
        if (completionStatusPresent) {
            if (argMultimap.getValue(PREFIX_COMPLETION_STATUS).get().isEmpty()) {
                throw new ParseException(MESSAGE_ARGUMENT_EMPTY);
            }
            String[] completionStatusKeywords = argMultimap.getValue(PREFIX_COMPLETION_STATUS).get().split("\\s+");
            String firstKeyword = completionStatusKeywords[0];
            findTaskDescriptor.setCompletionStatus(ParserUtil.parseCompletionStatus(firstKeyword).get());
        }

        return new FindTaskCommand(findTaskDescriptor);

    }

    @Override
    public FindTaskCommand parse(String userInput) throws ParseException {
        return null;
    }

    /**
     * Parses {@code Collection<String> tags} into a {@code Set<Tag>} if {@code tags} is non-empty.
     * If {@code tags} contain only one element which is an empty string, it will be parsed into a
     * {@code Set<Tag>} containing zero tags.
     */
    private Optional<List<String>> parseTagsForFind(Collection<String> tags) throws ParseException {
        if (tags.isEmpty()) {
            return Optional.empty();
        }

        List<String> tagList = new ArrayList<>();
        if (tags.size() == 1 && tags.contains("")) {
            tagList = Collections.emptyList();
        } else {
            tagList.addAll(tags);
        }
        return Optional.of(ParserUtil.parseTagList(tagList));
    }

}
