package bo.org.tecnoweb.cpms.users.application.commandhelp;

import static org.assertj.core.api.Assertions.assertThat;

import bo.org.tecnoweb.cpms.mail.domain.ReceivedEmail;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Email contract of the help command (FR-066). */
class ShowHelpQueryHandlerTest {

    private String requester;

    private final ShowHelpQueryHandler handler = new ShowHelpQueryHandler(
            new CommandHelpUseCase(null, List.of(), null) {
                @Override
                public void showHelp(String requesterEmail) {
                    requester = requesterEmail;
                }
            });

    @Test
    void declaresTheCommandContract() {
        assertThat(handler.actionKey()).isEqualTo("ayuda");
        assertThat(handler.requiredArgs()).isZero();
        assertThat(handler.requiredFiles()).isZero();
    }

    @Test
    void theSenderIsTheRequester() {
        handler.handle(ReceivedEmail.builder().fromSender("ana@uagrm.bo").withSubject("ayuda[]"));

        assertThat(requester).isEqualTo("ana@uagrm.bo");
    }

    @Test
    void theHelpGuideNamesTheAction() {
        assertThat(CommandHelpUseCase.HELP_GUIDE.action()).isEqualTo(ShowHelpQueryHandler.ACTION);
        assertThat(CommandHelpUseCase.HELP_GUIDE.syntax()).isEqualTo("ayuda[]");
        assertThat(CommandHelpUseCase.HELP_GUIDE.permissions()).isEmpty();
    }
}
