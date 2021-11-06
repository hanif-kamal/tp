package dash.logic.parser.taskcommand;

import static dash.logic.parser.CommandParserTestUtil.assertParseFailureWithPersonList;
import static dash.logic.parser.CommandParserTestUtil.assertParseSuccessWithPersonList;

import org.junit.jupiter.api.Test;

import dash.commons.core.Messages;
import dash.commons.core.index.Index;
import dash.logic.commands.CommandTestUtil;
import dash.logic.commands.taskcommand.EditTaskCommand;
import dash.logic.parser.CliSyntax;
import dash.model.person.Person;
import dash.model.tag.Tag;
import dash.model.task.TaskDescription;
import dash.testutil.EditTaskDescriptorBuilder;
import dash.testutil.TypicalIndexes;
import dash.testutil.TypicalPersons;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

class EditTaskCommandParserTest {

    private static final String TAG_EMPTY = " " + CliSyntax.PREFIX_TAG;

    private static final String MESSAGE_INVALID_FORMAT =
            String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT, EditTaskCommand.MESSAGE_USAGE);

    private EditTaskCommandParser parser = new EditTaskCommandParser();
    private ObservableList<Person> people = FXCollections.observableList(TypicalPersons.getTypicalPersons());

    @Test
    public void parse_missingParts_failure() {
        // no index specified
        assertParseFailureWithPersonList(parser, CommandTestUtil.VALID_TASK_DESCRIPTION_TP, people,
                MESSAGE_INVALID_FORMAT);

        // no field specified
        assertParseFailureWithPersonList(parser, "1", people, EditTaskCommand.MESSAGE_NOT_EDITED);

        // no index and no field specified
        assertParseFailureWithPersonList(parser, "", people, MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_invalidPreamble_failure() {
        // negative index
        assertParseFailureWithPersonList(parser, "-5" + CommandTestUtil.TASK_DESC_ASSIGNMENT, people,
                Messages.MESSAGE_INVALID_TASK_DISPLAYED_INDEX);

        // zero index
        assertParseFailureWithPersonList(parser, "0" + CommandTestUtil.TASK_DESC_ASSIGNMENT, people,
                Messages.MESSAGE_INVALID_TASK_DISPLAYED_INDEX);

        // invalid arguments being parsed as preamble
        assertParseFailureWithPersonList(parser, "1 some random string", people, MESSAGE_INVALID_FORMAT);

        // invalid prefix being parsed as preamble
        assertParseFailureWithPersonList(parser, "1 i/ string", people, MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_invalidValue_failure() {
        assertParseFailureWithPersonList(parser, "1" + CommandTestUtil.INVALID_TASK_DESC, people,
                TaskDescription.MESSAGE_CONSTRAINTS); // invalid task description
        assertParseFailureWithPersonList(parser, "1" + CommandTestUtil.INVALID_TAG_DESC, people,
                Tag.MESSAGE_CONSTRAINTS); // invalid tag

        // while parsing {@code PREFIX_TAG} alone will reset the tags of the {@code Person} being edited,
        // parsing it together with a valid tag results in error
        assertParseFailureWithPersonList(parser,
                "1" + CommandTestUtil.TAG_DESC_UNGRADED + TAG_EMPTY, people, Tag.MESSAGE_CONSTRAINTS);

        // multiple invalid values, but only the first invalid value is captured
        assertParseFailureWithPersonList(parser,
                "1" + CommandTestUtil.INVALID_TASK_DESC + CommandTestUtil.TAG_DESC_UNGRADED + TAG_EMPTY,
                people,
                TaskDescription.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_allFieldsSpecified_success() {
        Index targetIndex = TypicalIndexes.INDEX_SECOND;
        String userInput = targetIndex.getOneBased() + CommandTestUtil.TASK_DESC_ASSIGNMENT
                + CommandTestUtil.TAG_DESC_UNGRADED;

        EditTaskCommand.EditTaskDescriptor descriptor =
                new EditTaskDescriptorBuilder().withTaskDescription(CommandTestUtil.VALID_TASK_DESCRIPTION_ASSIGNMENT)
                        .withTags(CommandTestUtil.VALID_TAG_UNGRADED).build();
        EditTaskCommand expectedCommand = new EditTaskCommand(targetIndex, descriptor);

        assertParseSuccessWithPersonList(parser, userInput, people, expectedCommand);
    }

    @Test
    public void parse_oneFieldsSpecified_success() {
        Index targetIndex = TypicalIndexes.INDEX_FIRST;

        // task description
        String userInput = targetIndex.getOneBased() + CommandTestUtil.TASK_DESC_ASSIGNMENT;

        EditTaskCommand.EditTaskDescriptor descriptor =
                new EditTaskDescriptorBuilder()
                        .withTaskDescription(CommandTestUtil.VALID_TASK_DESCRIPTION_ASSIGNMENT).build();
        EditTaskCommand expectedCommand = new EditTaskCommand(targetIndex, descriptor);

        assertParseSuccessWithPersonList(parser, userInput, people, expectedCommand);

        // tags
        userInput = targetIndex.getOneBased() + CommandTestUtil.TAG_DESC_QUIZ;

        descriptor =
                new EditTaskDescriptorBuilder().withTags(CommandTestUtil.VALID_TAG_QUIZ).build();
        expectedCommand = new EditTaskCommand(targetIndex, descriptor);

        assertParseSuccessWithPersonList(parser, userInput, people, expectedCommand);

    }

    @Test
    public void parse_multipleRepeatedFields_acceptsLast() {
        Index targetIndex = TypicalIndexes.INDEX_FIRST;
        String userInput =
                targetIndex.getOneBased() + CommandTestUtil.TASK_DESC_LECTURE + CommandTestUtil.TAG_DESC_UNGRADED
                        + CommandTestUtil.TASK_DESC_ASSIGNMENT + CommandTestUtil.TAG_DESC_QUIZ;

        EditTaskCommand.EditTaskDescriptor descriptor =
                new EditTaskDescriptorBuilder().withTaskDescription(CommandTestUtil.VALID_TASK_DESCRIPTION_ASSIGNMENT)
                        .withTags(CommandTestUtil.VALID_TAG_UNGRADED, CommandTestUtil.VALID_TAG_QUIZ).build();
        EditTaskCommand expectedCommand = new EditTaskCommand(targetIndex, descriptor);

        assertParseSuccessWithPersonList(parser, userInput, people, expectedCommand);
    }

    @Test
    public void parse_invalidValueFollowedByValidValue_success() {
        // no other valid values specified
        Index targetIndex = TypicalIndexes.INDEX_FIRST;
        String userInput =
                targetIndex.getOneBased() + CommandTestUtil.INVALID_TASK_DESC + CommandTestUtil.TASK_DESC_ASSIGNMENT;
        EditTaskCommand.EditTaskDescriptor descriptor =
                new EditTaskDescriptorBuilder()
                        .withTaskDescription(CommandTestUtil.VALID_TASK_DESCRIPTION_ASSIGNMENT).build();
        EditTaskCommand expectedCommand = new EditTaskCommand(targetIndex, descriptor);
        assertParseSuccessWithPersonList(parser, userInput, people, expectedCommand);

        // other valid values specified
        userInput =
                targetIndex.getOneBased() + CommandTestUtil.INVALID_TASK_DESC + CommandTestUtil.TAG_DESC_GROUPWORK
                        + CommandTestUtil.TASK_DESC_ASSIGNMENT;
        descriptor =
                new EditTaskDescriptorBuilder().withTaskDescription(CommandTestUtil.VALID_TASK_DESCRIPTION_ASSIGNMENT)
                        .withTags(CommandTestUtil.VALID_TAG_GROUPWORK).build();
        expectedCommand = new EditTaskCommand(targetIndex, descriptor);
        assertParseSuccessWithPersonList(parser, userInput, people, expectedCommand);
    }

    @Test
    public void parse_resetTags_success() {
        Index targetIndex = TypicalIndexes.INDEX_THIRD;
        String userInput = targetIndex.getOneBased() + TAG_EMPTY;

        EditTaskCommand.EditTaskDescriptor descriptor = new EditTaskDescriptorBuilder().withTags().build();
        EditTaskCommand expectedCommand = new EditTaskCommand(targetIndex, descriptor);

        assertParseSuccessWithPersonList(parser, userInput, people, expectedCommand);
    }

}
