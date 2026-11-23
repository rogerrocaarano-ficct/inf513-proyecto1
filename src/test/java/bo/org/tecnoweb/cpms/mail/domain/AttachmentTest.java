package bo.org.tecnoweb.cpms.mail.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class AttachmentTest {

    @Test
    void contentIsEqualByItsBytes() {
        Attachment.Content a = new Attachment.Content("carta.pdf", "application/pdf", new byte[] {1, 2});
        Attachment.Content b = new Attachment.Content("carta.pdf", "application/pdf", new byte[] {1, 2});
        Attachment.Content other = new Attachment.Content("carta.pdf", "application/pdf", new byte[] {1, 3});

        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b).isNotEqualTo(other);
    }

    @Test
    void contentDescribesItsSizeInsteadOfItsBytes() {
        assertThat(new Attachment.Content("carta.pdf", "application/pdf", new byte[] {1, 2}))
                .hasToString("Content[name=carta.pdf, contentType=application/pdf, size=2]");
    }

    @Test
    void contentWithoutBytesIsEmpty() {
        assertThat(new Attachment.Content("a.pdf", "application/pdf", null).isEmpty()).isTrue();
        assertThat(new Attachment.Content("a.pdf", "application/pdf", new byte[0]).isEmpty()).isTrue();
        assertThat(new Attachment.Content("a.pdf", "application/pdf", new byte[] {1}).isEmpty()).isFalse();
    }

    @Test
    void storedIsEqualByItsFile() {
        UUID id = UUID.randomUUID();

        assertThat(new Attachment.Stored(id)).isEqualTo(new Attachment.Stored(id));
    }

    @Test
    void storedRequiresTheFile() {
        assertThatThrownBy(() -> new Attachment.Stored(null)).isInstanceOf(NullPointerException.class);
    }
}
