package dash.logic.parser.personcommand;

import static dash.commons.core.Messages.MESSAGE_ARGUMENT_EMPTY;
import static dash.commons.core.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static dash.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static dash.logic.parser.CliSyntax.PREFIX_EMAIL;
import static dash.logic.parser.CliSyntax.PREFIX_NAME;
import static dash.logic.parser.CliSyntax.PREFIX_PHONE;
import static dash.logic.parser.CliSyntax.PREFIX_TAG;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import dash.logic.commands.personcommand.FindPersonCommand;
import dash.logic.commands.personcommand.FindPersonCommand.FindPersonDescriptor;
import dash.logic.parser.ArgumentMultimap;
import dash.logic.parser.ArgumentTokenizer;
import dash.logic.parser.Parser;
import dash.logic.parser.ParserUtil;
import dash.logic.parser.exceptions.ParseException;


/**
 * Parses input arguments and creates a new FindPersonCommand object
 */
public class FindPersonCommandParser implements Parser<FindPersonCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the FindPersonCommand
     * and returns a FindPersonCommand object for execution.
     *
     * @throws ParseException if the user input does not conform the expected format
     */
    public FindPersonCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap argMultimap =
                ArgumentTokenizer.tokenize(args, PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_ADDRESS, PREFIX_TAG);
        String preamble = argMultimap.getPreamble();

        FindPersonDescriptor findPersonDescriptor = new FindPersonDescriptor();
        boolean namePresent = argMultimap.getValue(PREFIX_NAME).isPresent();
        boolean phonePresent = argMultimap.getValue(PREFIX_PHONE).isPresent();
        boolean emailPresent = argMultimap.getValue(PREFIX_EMAIL).isPresent();
        boolean addressPresent = argMultimap.getValue(PREFIX_ADDRESS).isPresent();
        boolean tagPresent = parseTagsForFind(argMultimap.getAllValues(PREFIX_TAG)).isPresent();
        if (preamble.isEmpty() && !namePresent && !phonePresent && !emailPresent && !addressPresent && !tagPresent) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindPersonCommand.MESSAGE_USAGE));
        } else if (!preamble.isEmpty()) {
            String[] preambleKeywords = preamble.split("\\s+");
            findPersonDescriptor.setName(Arrays.asList(preambleKeywords));
        }
        //if both preamble and name prefix specified, name prefix will override
        if (namePresent) {
            if (argMultimap.getValue(PREFIX_NAME).get().isEmpty()) {
                throw new ParseException(MESSAGE_ARGUMENT_EMPTY);
            }
            String[] nameKeywords = argMultimap.getValue(PREFIX_NAME).get().split("\\s+");
            findPersonDescriptor.setName(Arrays.asList(nameKeywords));
        }
        if (phonePresent) {
            if (argMultimap.getValue(PREFIX_PHONE).get().isEmpty()) {
                throw new ParseException(MESSAGE_ARGUMENT_EMPTY);
            }
            String[] phoneKeywords = argMultimap.getValue(PREFIX_PHONE).get().split("\\s+");
            findPersonDescriptor.setPhone(Arrays.asList(phoneKeywords));
        }
        if (emailPresent) {
            if (argMultimap.getValue(PREFIX_EMAIL).get().isEmpty()) {
                throw new ParseException(MESSAGE_ARGUMENT_EMPTY);
            }
            String[] emailKeywords = argMultimap.getValue(PREFIX_EMAIL).get().split("\\s+");
            findPersonDescriptor.setEmail(Arrays.asList(emailKeywords));
        }
        if (addressPresent) {
            if (argMultimap.getValue(PREFIX_ADDRESS).get().isEmpty()) {
                throw new ParseException(MESSAGE_ARGUMENT_EMPTY);
            }
            String[] addressKeywords = argMultimap.getValue(PREFIX_ADDRESS).get().split("\\s+");
            findPersonDescriptor.setAddress(Arrays.asList(addressKeywords));
        }
        if (tagPresent) {
            if (argMultimap.getValue(PREFIX_TAG).get().isEmpty()) {
                throw new ParseException(MESSAGE_ARGUMENT_EMPTY);
            }
            parseTagsForFind(argMultimap.getAllValues(PREFIX_TAG)).ifPresent(findPersonDescriptor::setTags);
        }

        return new FindPersonCommand(findPersonDescriptor);

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
