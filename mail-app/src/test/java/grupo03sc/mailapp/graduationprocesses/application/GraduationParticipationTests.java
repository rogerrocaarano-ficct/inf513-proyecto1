package grupo03sc.mailapp.graduationprocesses.application;

import static org.assertj.core.api.Assertions.assertThat;

import grupo03sc.mailapp.TestcontainersConfiguration;
import java.util.EnumSet;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class GraduationParticipationTests {

    @Autowired
    private GraduationParticipation participation;

    @Test
    void findsNoProcessesWhileThereAreNone() {
        assertThat(participation.markPendingReassignment(UUID.randomUUID(), EnumSet.allOf(ParticipantKind.class)))
                .isEmpty();
    }
}
