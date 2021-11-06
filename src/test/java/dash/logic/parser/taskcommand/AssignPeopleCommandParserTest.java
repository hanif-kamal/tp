package dash.logic.parser.taskcommand;

import static dash.commons.core.Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX;
import static dash.logic.parser.CommandParserTestUtil.assertParseFailureWithPersonList;
import static dash.logic.parser.CommandParserTestUtil.assertParseSuccessWithPersonList;

import org.junit.jupiter.api.Test;

import dash.commons.core.Messages;
import dash.commons.core.index.Index;
import dash.logic.commands.CommandTestUtil;
import dash.logic.commands.taskcommand.AssignPeopleCommand;
import dash.logic.commands.taskcommand.EditTaskCommand;
import dash.logic.parser.CliSyntax;
import dash.model.person.Person;
import dash.testutil.EditTaskDescriptorBuilder;
import dash.testutil.TypicalIndexes;
import dash.testutil.TypicalPersons;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

class AssignPeopleCommandParserTest {
    private static final String MESSAGE_INVALID_FORMAT =
            String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT, AssignPeopleCommand.MESSAGE_USAGE);

    private static final String PREFIX_EMPTY = " " + CliSyntax.PREFIX_PERSON;

    private AssignPeopleCommandParser parser = new AssignPeopleCommandParser();
    private ObservableList<Person> people = FXCollections.observableList(TypicalPersons.getTypicalPersons());

    @Test
    public void parse_missingParts_failure() {
        // no index specified
        assertParseFailureWithPersonList(parser, PREFIX_EMPTY + " 1", people,
                MESSAGE_INVALID_FORMAT);

        // no field specified
        assertParseFailureWithPersonList(parser, "1", people, AssignPeopleCommand.MESSAGE_NOT_EDITED);

        // no index and no field specified
        assertParseFailureWithPersonList(parser, "", people, MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_invalidPreamble_failure() {
        // negative index
        assertParseFailureWithPersonList(parser, "-5" + CommandTestUtil.TASK_DESC_ASSIGNMENT, people,
                MESSAGE_INVALID_FORMAT);

        // zero index
        assertParseFailureWithPersonList(parser, "0" + CommandTestUtil.TASK_DESC_ASSIGNMENT, people,
                MESSAGE_INVALID_FORMAT);

        // invalid arguments being parsed as preamble
        assertParseFailureWithPersonList(parser, "1 some random string", people, MESSAGE_INVALID_FORMAT);

        // invalid prefix being parsed as preamble
        assertParseFailureWithPersonList(parser, "1 i/ string", people, MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_invalidValue_failure() {
        assertParseFailureWithPersonList(parser, "1" + CommandTestUtil.INVALID_PERSON_INDEX, people,
                MESSAGE_INVALID_FORMAT); // non-numeric person index

        assertParseFailureWithPersonList(parser, "1" + PREFIX_EMPTY + "-1", people,
                MESSAGE_INVALID_PERSON_DISPLAYED_INDEX); // negative person index

        assertParseFailureWithPersonList(parser, "1" + PREFIX_EMPTY, people,
                Messages.MESSAGE_ARGUMENT_EMPTY); // no person index

        assertParseFailureWithPersonList(parser, "1" + PREFIX_EMPTY + "100", people,
                MESSAGE_INVALID_PERSON_DISPLAYED_INDEX); // out of range person index

    }

    @Test
    public void parse_singleAssigned_success() {
        Index taskIndex = TypicalIndexes.INDEX_FIRST;
        Index personIndex = TypicalIndexes.INDEX_SECOND;

        String userInput = taskIndex.getOneBased()
                + PREFIX_EMPTY + personIndex.getOneBased();

        EditTaskCommand.EditTaskDescriptor editTaskDescriptor =
                new EditTaskDescriptorBuilder().withPeople(this.people.get(personIndex.getZeroBased())).build();

        AssignPeopleCommand expectedCommand = new AssignPeopleCommand(taskIndex, editTaskDescriptor);

        assertParseSuccessWithPersonList(parser, userInput, this.people, expectedCommand);
    }

    @Test
    public void parse_multipleAssigned_success() {
        Index taskIndex = TypicalIndexes.INDEX_FIRST;
        Index firstPersonIndex = TypicalIndexes.INDEX_THIRD;
        Index secondPersonIndex = TypicalIndexes.INDEX_SECOND;

        String userInput = taskIndex.getOneBased()
                + PREFIX_EMPTY + firstPersonIndex.getOneBased()
                + PREFIX_EMPTY + secondPersonIndex.getOneBased();

        EditTaskCommand.EditTaskDescriptor editTaskDescriptor =
                new EditTaskDescriptorBuilder().withPeople(this.people.get(firstPersonIndex.getZeroBased()),
                        this.people.get(secondPersonIndex.getZeroBased())).build();

        AssignPeopleCommand expectedCommand = new AssignPeopleCommand(taskIndex, editTaskDescriptor);

        assertParseSuccessWithPersonList(parser, userInput, this.people, expectedCommand);

    }

}
