package dash.logic.parser.personcommand;

import static dash.commons.core.Messages.MESSAGE_ARGUMENT_EMPTY;

import org.junit.jupiter.api.Test;

import dash.commons.core.Messages;
import dash.commons.core.index.Index;
import dash.logic.commands.CommandTestUtil;
import dash.logic.commands.personcommand.EditPersonCommand;
import dash.logic.commands.personcommand.TagPersonCommand;
import dash.logic.parser.CliSyntax;
import dash.logic.parser.CommandParserTestUtil;
import dash.model.tag.Tag;
import dash.testutil.EditPersonDescriptorBuilder;
import dash.testutil.TypicalIndexes;

class TagPersonCommandParserTest {

    private static final String TAG_EMPTY = " " + CliSyntax.PREFIX_TAG;

    private static final String MESSAGE_INVALID_FORMAT =
            String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT, TagPersonCommand.MESSAGE_USAGE);

    private TagPersonCommandParser parser = new TagPersonCommandParser();

    @Test
    public void parse_missingParts_failure() {
        // no index specified
        CommandParserTestUtil.assertParseFailure(parser, TAG_EMPTY + "", MESSAGE_INVALID_FORMAT);

        // no field specified
        CommandParserTestUtil.assertParseFailure(parser, "1", TagPersonCommand.MESSAGE_NOT_EDITED);

        // no index and no field specified
        CommandParserTestUtil.assertParseFailure(parser, "", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_invalidPreamble_failure() {
        // negative index
        CommandParserTestUtil.assertParseFailure(parser,
                "-5" + TAG_EMPTY + " test", Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);

        // zero index
        CommandParserTestUtil.assertParseFailure(parser,
                "0" + TAG_EMPTY + " test", Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);

        // invalid arguments being parsed as preamble
        CommandParserTestUtil.assertParseFailure(parser, "1 some random string", MESSAGE_INVALID_FORMAT);

        // invalid prefix being parsed as preamble
        CommandParserTestUtil.assertParseFailure(parser, "1 i/ string", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_invalidValue_failure() {
        CommandParserTestUtil.assertParseFailure(parser, "1" + CommandTestUtil.INVALID_TAG_DESC,
                Tag.MESSAGE_CONSTRAINTS); // invalid tag

        CommandParserTestUtil.assertParseFailure(parser, "1 n/ test",
                MESSAGE_INVALID_FORMAT); // invalid prefix

        CommandParserTestUtil.assertParseFailure(parser, "1" + TAG_EMPTY,
                MESSAGE_ARGUMENT_EMPTY); // empty prefix

    }

    @Test
    public void parse_singleTag_success() {
        EditPersonCommand.EditPersonDescriptor descriptor =
                new EditPersonDescriptorBuilder().withTags("test").build();
        Index taskIndex = TypicalIndexes.INDEX_FIRST;

        TagPersonCommand expectedCommand = new TagPersonCommand(taskIndex, descriptor);

        String userInput = taskIndex.getOneBased() + TAG_EMPTY + "test";

        CommandParserTestUtil.assertParseSuccess(parser, userInput, expectedCommand);
    }

    @Test
    public void parse_multipleTags_success() {
        EditPersonCommand.EditPersonDescriptor descriptor =
                new EditPersonDescriptorBuilder().withTags("test1", "test2").build();
        Index taskIndex = TypicalIndexes.INDEX_FIRST;

        TagPersonCommand expectedCommand = new TagPersonCommand(taskIndex, descriptor);

        String userInput = taskIndex.getOneBased() + TAG_EMPTY + "test1"
                + TAG_EMPTY + "test2";

        CommandParserTestUtil.assertParseSuccess(parser, userInput, expectedCommand);
    }

}
