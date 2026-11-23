package bo.org.tecnoweb.cpms.mail.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import bo.org.tecnoweb.cpms.common.DomainException;
import bo.org.tecnoweb.cpms.common.DomainValidationException;
import bo.org.tecnoweb.cpms.mail.domain.Attachment;
import bo.org.tecnoweb.cpms.mail.domain.ReceivedEmail;
import bo.org.tecnoweb.cpms.mail.domain.ReceivedEmailState;
import bo.org.tecnoweb.cpms.storage.application.FileStorage;
import bo.org.tecnoweb.cpms.storage.application.StoredFileService;
import bo.org.tecnoweb.cpms.storage.domain.StoredFile;
import bo.org.tecnoweb.cpms.storage.domain.StoredFileRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionOperations;

class InboxPollerServiceTest {

    private final FakeInbox inbox = new FakeInbox();
    private final InMemoryRepository repository = new InMemoryRepository();
    private final RecordingSender sender = new RecordingSender();
    private final RollbackTransactions transactions = new RollbackTransactions(repository);
    private final InMemoryStoredFiles storedFileRows = new InMemoryStoredFiles();
    private final InMemoryFileStorage disk = new InMemoryFileStorage();
    private final StoredFileService storedFiles = new StoredFileService(storedFileRows, disk);

    private InboxPollerService service(int batchSize, EmailCommandHandler... handlers) {
        return new InboxPollerService(inbox, List.of(handlers), batchSize, repository, sender, transactions,
                storedFiles);
    }

    private static Attachment.Content attachment(String name, byte... data) {
        return new Attachment.Content(name, "application/pdf", data);
    }

    private InboxPollerService service(EmailCommandHandler... handlers) {
        return service(10, handlers);
    }

    @Nested
    class BatchReading {

        @Test
        void readsAtMostTheBatchSizePerCycle() {
            for (int i = 1; i <= 15; i++) {
                inbox.add("uid-" + i, "ana@uagrm.bo", "cmd[]");
            }
            InboxPollerService service = service(10, new TestHandler("cmd", 0, 0));

            service.pollAndProcess();

            assertThat(repository.all()).hasSize(10);
            assertThat(inbox.size()).isEqualTo(5);

            service.pollAndProcess();

            assertThat(repository.all()).hasSize(15);
            assertThat(inbox.size()).isZero();
        }

        @Test
        void emptyMailboxStoresAndSendsNothing() {
            service().pollAndProcess();

            assertThat(repository.all()).isEmpty();
            assertThat(sender.sent).isEmpty();
        }

        @Test
        void mailboxFailureStillProcessesPendingEmails() {
            TestHandler handler = new TestHandler("cmd", 0, 0);
            repository.put(stored("uid-1", "cmd[]", ReceivedEmailState.RECEIVED));
            inbox.failOnPoll = true;

            service(handler).pollAndProcess();

            assertThat(handler.calls).isEqualTo(1);
        }
    }

    @Nested
    class OnlyOnce {

        @Test
        void messageReadAgainInAFinalStateIsOnlyDeleted() {
            TestHandler handler = new TestHandler("cmd", 0, 0);
            repository.put(stored("uid-1", "cmd[]", ReceivedEmailState.PROCESSED));
            inbox.add("uid-1", "ana@uagrm.bo", "cmd[]");

            service(handler).pollAndProcess();

            assertThat(handler.calls).isZero();
            assertThat(repository.all()).hasSize(1);
            assertThat(inbox.deleted).containsExactly("uid-1");
            assertThat(sender.sent).isEmpty();
        }

        @Test
        @ExtendWith(OutputCaptureExtension.class)
        void reusedUidlIsDeletedWithAWarning(CapturedOutput output) {
            TestHandler handler = new TestHandler("usuarios.crearcuenta", 1, 0);
            repository.put(stored("1", "usuarios.listar[]", ReceivedEmailState.DISCARDED));
            inbox.add("1", "ana@uagrm.bo", "usuarios.crearCuenta[Ana]");

            service(handler).pollAndProcess();

            assertThat(handler.calls).isZero();
            assertThat(inbox.deleted).containsExactly("1");
            assertThat(output.getOut())
                    .contains("WARN")
                    .contains("Deleting message 1 without executing it")
                    .contains("stored action usuarios.listar")
                    .contains("read action usuarios.crearcuenta")
                    .doesNotContain("usuarios.listar[]")
                    .doesNotContain("[Ana]");
        }

        @Test
        void validatedEmailAfterARestartIsExecutedOnce() {
            TestHandler handler = new TestHandler("cmd", 0, 0);
            repository.put(stored("uid-1", "cmd[]", ReceivedEmailState.VALIDATED));
            inbox.add("uid-1", "ana@uagrm.bo", "cmd[]");

            InboxPollerService service = service(handler);
            service.pollAndProcess();
            service.pollAndProcess();

            assertThat(handler.calls).isEqualTo(1);
            assertThat(repository.state("uid-1")).isEqualTo(ReceivedEmailState.PROCESSED);
            assertThat(inbox.size()).isZero();
        }

        @Test
        void receivedEmailReadAgainIsNotStoredTwice() {
            TestHandler handler = new TestHandler("cmd", 0, 0);
            repository.put(stored("uid-1", "cmd[]", ReceivedEmailState.RECEIVED));
            inbox.add("uid-1", "ana@uagrm.bo", "cmd[]");

            service(handler).pollAndProcess();

            assertThat(repository.all()).hasSize(1);
            assertThat(handler.calls).isEqualTo(1);
        }
    }

    @Nested
    class Validation {

        @Test
        void subjectWithoutTheFormIsDiscardedAndAnswered() {
            inbox.add("uid-1", "ana@uagrm.bo", "hola");

            service(new TestHandler("cmd", 0, 0)).pollAndProcess();

            assertThat(repository.state("uid-1")).isEqualTo(ReceivedEmailState.DISCARDED);
            assertThat(sender.only().body).contains("comando[arg1; arg2; …]");
            assertThat(sender.only().to).isEqualTo("ana@uagrm.bo");
            assertThat(sender.only().subject).isEqualTo("Re: hola");
            assertThat(inbox.deleted).containsExactly("uid-1");
        }

        @Test
        void unknownCommandIsDiscardedAndListsTheAvailableCommands() {
            inbox.add("uid-1", "ana@uagrm.bo", "usuarios.borrarTodo[]");

            service(new TestHandler("usuarios.listar", 0, 0), new TestHandler("usuarios.crearCuenta", 4, 0))
                    .pollAndProcess();

            assertThat(repository.state("uid-1")).isEqualTo(ReceivedEmailState.DISCARDED);
            assertThat(sender.only().body)
                    .contains("\"usuarios.borrartodo\" no existe")
                    .contains("- usuarios.listar")
                    .contains("- usuarios.crearcuenta");
        }

        @Test
        void missingArgumentsAreDiscardedWithTheExpectedCount() {
            TestHandler handler = new TestHandler("cmd", 4, 0);
            inbox.add("uid-1", "ana@uagrm.bo", "cmd[a; b; c]");

            service(handler).pollAndProcess();

            assertThat(handler.calls).isZero();
            assertThat(repository.state("uid-1")).isEqualTo(ReceivedEmailState.DISCARDED);
            assertThat(sender.only().body).contains("requiere 4 argumentos y se recibieron 3");
        }

        @Test
        void extraAttachmentIsDiscardedWithTheExpectedCount() {
            TestHandler handler = new TestHandler("cmd", 0, 0);
            inbox.add("uid-1", "ana@uagrm.bo", "cmd[]", attachment("a.pdf", (byte) 1));

            service(handler).pollAndProcess();

            assertThat(handler.calls).isZero();
            assertThat(repository.state("uid-1")).isEqualTo(ReceivedEmailState.DISCARDED);
            assertThat(sender.only().body).contains("requiere 0 adjuntos y se recibieron 1");
        }

        @Test
        void twoHandlersWithTheSameActionPreventStarting() {
            assertThatThrownBy(() -> service(new TestHandler("cmd", 0, 0), new TestHandler("CMD", 1, 0)))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("cmd");
        }
    }

    @Nested
    class Execution {

        @Test
        void validCommandIsExecutedOnceWithItsArgumentsAndNotAnswered() {
            TestHandler handler = new TestHandler("cmd", 2, 0);
            inbox.add("uid-1", "ana@uagrm.bo", "Cmd[ Ana María ; Pérez ]");

            service(handler).pollAndProcess();

            assertThat(handler.calls).isEqualTo(1);
            assertThat(handler.lastArgs).containsExactly("Ana María", "Pérez");
            assertThat(handler.lastFrom).isEqualTo("ana@uagrm.bo");
            assertThat(repository.state("uid-1")).isEqualTo(ReceivedEmailState.PROCESSED);
            assertThat(sender.sent).isEmpty();
            assertThat(inbox.deleted).containsExactly("uid-1");
        }

        @Test
        void businessRuleErrorRollsBackAndIsAnsweredWithItsMessage() {
            TestHandler handler = new TestHandler("cmd", 0, 0)
                    .failingWith(new RuleBroken("El correo ya pertenece a otra cuenta."));
            inbox.add("uid-1", "ana@uagrm.bo", "cmd[]");

            service(handler).pollAndProcess();

            assertThat(transactions.rolledBack).isEqualTo(1);
            assertThat(repository.state("uid-1")).isEqualTo(ReceivedEmailState.PROCESSED);
            assertThat(sender.only().body).contains("El correo ya pertenece a otra cuenta.");
            assertThat(inbox.deleted).containsExactly("uid-1");
        }

        @Test
        void validationErrorListsOneErrorPerField() {
            Map<String, String> errors = new LinkedHashMap<>();
            errors.put("email", "El correo no es válido.");
            errors.put("telefono", "El teléfono es obligatorio.");
            TestHandler handler = new TestHandler("cmd", 0, 0).failingWith(new DomainValidationException(errors));
            inbox.add("uid-1", "ana@uagrm.bo", "cmd[]");

            service(handler).pollAndProcess();

            assertThat(sender.only().body)
                    .contains("- email: El correo no es válido.")
                    .contains("- telefono: El teléfono es obligatorio.");
        }

        @Test
        void unexpectedErrorIsAnsweredWithoutTechnicalDetails() {
            TestHandler handler = new TestHandler("cmd", 0, 0)
                    .failingWith(new IllegalStateException("NullPointer en SomeClass.java:42"));
            inbox.add("uid-1", "ana@uagrm.bo", "cmd[]");

            service(handler).pollAndProcess();

            assertThat(repository.state("uid-1")).isEqualTo(ReceivedEmailState.PROCESSED);
            assertThat(sender.only().body).contains("error interno").doesNotContain("SomeClass");
        }

        @Test
        void failureWhileConfirmingDoesNotLeaveTheEmailProcessedInsideTheRolledBackChanges() {
            TestHandler handler = new TestHandler("cmd", 0, 0);
            inbox.add("uid-1", "ana@uagrm.bo", "cmd[]");
            repository.failNextStoreOf = ReceivedEmailState.PROCESSED;

            service(handler).pollAndProcess();

            assertThat(transactions.rolledBack).isEqualTo(1);
            // tx1 stores the new email; tx2 runs the command and is rolled back.
            assertThat(transactions.committedStates).doesNotContain(ReceivedEmailState.PROCESSED.name() + "@tx2");
            assertThat(repository.state("uid-1")).isEqualTo(ReceivedEmailState.PROCESSED);
        }

        @Test
        void pendingEmailIsNotDeletedFromTheServer() {
            inbox.add("uid-1", "ana@uagrm.bo", "cmd[]");
            repository.failNextStoreOf = ReceivedEmailState.VALIDATED;

            service(new TestHandler("cmd", 0, 0)).pollAndProcess();

            assertThat(repository.state("uid-1")).isEqualTo(ReceivedEmailState.RECEIVED);
            assertThat(inbox.deleted).isEmpty();
            assertThat(inbox.size()).isEqualTo(1);
        }

        @Test
        void failureSendingAReplyDoesNotStopTheBatch() {
            sender.fail = true;
            inbox.add("uid-1", "ana@uagrm.bo", "hola");
            inbox.add("uid-2", "ana@uagrm.bo", "otro");

            service(new TestHandler("cmd", 0, 0)).pollAndProcess();

            assertThat(repository.state("uid-1")).isEqualTo(ReceivedEmailState.DISCARDED);
            assertThat(repository.state("uid-2")).isEqualTo(ReceivedEmailState.DISCARDED);
        }
    }

    @Nested
    class Attachments {

        @Test
        void attachmentsAreStoredInOrderAndTheHandlerSeesTheirIds() {
            List<UUID> seen = new ArrayList<>();
            TestHandler handler = new TestHandler("cmd", 0, 2) {
                @Override
                public void handle(ReceivedEmail email) {
                    super.handle(email);
                    seen.addAll(email.getAttachmentIds());
                }
            };
            inbox.add("uid-1", "ana@uagrm.bo", "cmd[]", attachment("carta.pdf", (byte) 1),
                    attachment("cv.pdf", (byte) 2, (byte) 3));

            service(handler).pollAndProcess();

            assertThat(handler.calls).isEqualTo(1);
            assertThat(seen).hasSize(2);
            assertThat(seen).extracting(id -> storedFiles.get(id).getOriginalName()).containsExactly("carta.pdf",
                    "cv.pdf");
            assertThat(storedFiles.readContent(seen.get(1))).containsExactly(2, 3);
            assertThat(repository.findByMailboxId("uid-1").orElseThrow().getAttachmentIds())
                    .containsExactlyElementsOf(seen);
            // tx1 stores the attachments and the email, which leaves FETCHED with only references.
            assertThat(transactions.committedStates).contains(ReceivedEmailState.RECEIVED.name() + "@tx1");
        }

        @Test
        void pendingEmailIsExecutedWithItsStoredFilesWithoutStoringThemAgain() {
            TestHandler handler = new TestHandler("cmd", 0, 1);
            UUID storedId = UUID.randomUUID();
            repository.put(stored("uid-1", "cmd[]", ReceivedEmailState.VALIDATED).withAttachments(List.of(
                    new Attachment.Stored(storedId))));
            inbox.add("uid-1", "ana@uagrm.bo", "cmd[]", attachment("carta.pdf", (byte) 1));

            service(handler).pollAndProcess();

            assertThat(handler.calls).isEqualTo(1);
            assertThat(disk.writes).isZero();
            assertThat(repository.findByMailboxId("uid-1").orElseThrow().getAttachmentIds()).containsExactly(storedId);
        }

        @Test
        void discardedEmailKeepsItsStoredFiles() {
            inbox.add("uid-1", "ana@uagrm.bo", "noExiste[]", attachment("carta.pdf", (byte) 1));

            service(new TestHandler("cmd", 0, 0)).pollAndProcess();

            assertThat(repository.state("uid-1")).isEqualTo(ReceivedEmailState.DISCARDED);
            assertThat(disk.writes).isEqualTo(1);
            assertThat(repository.findByMailboxId("uid-1").orElseThrow().getAttachmentIds()).hasSize(1);
        }

        @Test
        void emailWithAnEmptyAttachmentIsDiscardedWithoutStoringFiles() {
            TestHandler handler = new TestHandler("cmd", 0, 2);
            inbox.add("uid-1", "ana@uagrm.bo", "cmd[]", attachment("carta.pdf", (byte) 1), attachment("boleta.pdf"));

            service(handler).pollAndProcess();

            assertThat(handler.calls).isZero();
            assertThat(disk.writes).isZero();
            assertThat(repository.state("uid-1")).isEqualTo(ReceivedEmailState.DISCARDED);
            assertThat(sender.only().body).contains("el adjunto \"boleta.pdf\" está vacío");
            assertThat(inbox.deleted).containsExactly("uid-1");
        }

        @Test
        void failureStoringAnEmailDoesNotStopTheCycleNorStoreIt() {
            TestHandler handler = new TestHandler("cmd", 0, 1);
            inbox.add("uid-1", "ana@uagrm.bo", "cmd[]", attachment("carta.pdf", (byte) 1));
            inbox.add("uid-2", "ana@uagrm.bo", "cmd[]", attachment("cv.pdf", (byte) 2));
            disk.failOnWrite = 1;

            service(handler).pollAndProcess();

            assertThat(repository.findByMailboxId("uid-1")).isEmpty();
            assertThat(inbox.deleted).containsExactly("uid-2");
            assertThat(inbox.size()).isEqualTo(1);
            assertThat(handler.calls).isEqualTo(1);
            assertThat(sender.sent).isEmpty();
        }
    }

    // ---- test doubles ----

    /** Keeps the metadata of the stored files, as the database would. */
    static class InMemoryStoredFiles implements StoredFileRepository {
        private final Map<UUID, StoredFile> rows = new LinkedHashMap<>();

        @Override
        public void insert(StoredFile file) {
            rows.put(file.getId(), file);
        }

        @Override
        public void update(StoredFile file) {
            rows.put(file.getId(), file);
        }

        @Override
        public Optional<StoredFile> getById(UUID id) {
            return Optional.ofNullable(rows.get(id));
        }

        @Override
        public Optional<StoredFile> getByIdForUpdate(UUID id) {
            return getById(id);
        }

        @Override
        public List<StoredFile> getByFirstVersionId(UUID firstVersionId) {
            return rows.values().stream().filter(f -> f.getFirstVersionId().equals(firstVersionId)).toList();
        }
    }

    /** Keeps the bytes in memory; can fail on the n-th write. */
    static class InMemoryFileStorage implements FileStorage {
        private final Map<String, byte[]> content = new HashMap<>();
        int writes;
        int failOnWrite;

        @Override
        public void write(String path, byte[] bytes) {
            if (failOnWrite > 0 && --failOnWrite == 0) {
                throw new IllegalStateException("disk full");
            }
            writes++;
            content.put(path, bytes.clone());
        }

        @Override
        public byte[] read(String path) {
            return content.get(path).clone();
        }
    }

    private static ReceivedEmail stored(String mailboxId, String subject, ReceivedEmailState state) {
        return ReceivedEmail.builder().withMailboxId(mailboxId).fromSender("ana@uagrm.bo").withSubject(subject)
                .withState(state);
    }

    private static ReceivedEmail copy(ReceivedEmail e) {
        return ReceivedEmail.rehydrate().withId(e.getId()).withMailboxId(e.getMailboxId()).fromSender(e.getFrom())
                .withSubject(e.getSubject()).withBody(e.getBody()).withAttachments(e.getAttachments())
                .withState(e.getState());
    }

    static class RuleBroken extends DomainException {
        RuleBroken(String message) {
            super("RULE", message);
        }
    }

    static class TestHandler implements EmailCommandHandler {
        private final String action;
        private final int args;
        private final int files;
        private RuntimeException failure;
        int calls;
        List<String> lastArgs;
        String lastFrom;

        TestHandler(String action, int args, int files) {
            this.action = action;
            this.args = args;
            this.files = files;
        }

        TestHandler failingWith(RuntimeException failure) {
            this.failure = failure;
            return this;
        }

        @Override
        public String actionKey() {
            return action;
        }

        @Override
        public void handle(ReceivedEmail email) {
            calls++;
            lastArgs = email.parseArgs();
            lastFrom = email.getFrom();
            if (failure != null) {
                throw failure;
            }
        }

        @Override
        public int requiredArgs() {
            return args;
        }

        @Override
        public int requiredFiles() {
            return files;
        }
    }

    static class FakeInbox implements EmailInbox {
        private final Map<String, ReceivedEmail> messages = new LinkedHashMap<>();
        final List<String> deleted = new ArrayList<>();
        boolean failOnPoll;

        void add(String mailboxId, String from, String subject, Attachment.Content... files) {
            messages.put(mailboxId, ReceivedEmail.builder().withMailboxId(mailboxId).fromSender(from)
                    .withSubject(subject).withAttachments(List.<Attachment>of(files)));
        }

        int size() {
            return messages.size();
        }

        @Override
        public List<ReceivedEmail> poll(int maxMessages) {
            if (failOnPoll) {
                throw new IllegalStateException("POP3 down");
            }
            // Each poll returns new objects, as reading the server again would.
            return messages.values().stream().limit(maxMessages)
                    .map(m -> ReceivedEmail.builder().withMailboxId(m.getMailboxId()).fromSender(m.getFrom())
                            .withSubject(m.getSubject()).withAttachments(m.getAttachments()))
                    .toList();
        }

        @Override
        public void delete(String mailboxId) {
            if (messages.remove(mailboxId) != null) {
                deleted.add(mailboxId);
            }
        }
    }

    static class InMemoryRepository implements ReceivedEmailRepository {
        private Map<String, ReceivedEmail> byMailboxId = new LinkedHashMap<>();
        ReceivedEmailState failNextStoreOf;

        void put(ReceivedEmail email) {
            byMailboxId.put(email.getMailboxId(), copy(email));
        }

        List<ReceivedEmail> all() {
            return List.copyOf(byMailboxId.values());
        }

        ReceivedEmailState state(String mailboxId) {
            return byMailboxId.get(mailboxId).getState();
        }

        Map<String, ReceivedEmail> snapshot() {
            Map<String, ReceivedEmail> snapshot = new LinkedHashMap<>();
            byMailboxId.forEach((k, v) -> snapshot.put(k, copy(v)));
            return snapshot;
        }

        void restore(Map<String, ReceivedEmail> snapshot) {
            byMailboxId = snapshot;
        }

        @Override
        public void store(ReceivedEmail email) {
            if (email.getState() == ReceivedEmailState.FETCHED) {
                throw new IllegalStateException("A fetched email cannot be stored");
            }
            if (failNextStoreOf != null && email.getState() == failNextStoreOf) {
                failNextStoreOf = null;
                throw new IllegalStateException("database failure");
            }
            ReceivedEmail existing = byMailboxId.get(email.getMailboxId());
            if (existing != null && !existing.getId().equals(email.getId())) {
                return;
            }
            byMailboxId.put(email.getMailboxId(), copy(email));
        }

        @Override
        public List<ReceivedEmail> getNotProcessed() {
            return byMailboxId.values().stream().filter(e -> !e.getState().isFinal()).map(InboxPollerServiceTest::copy)
                    .toList();
        }

        @Override
        public Optional<ReceivedEmail> findByMailboxId(String mailboxId) {
            return Optional.ofNullable(byMailboxId.get(mailboxId)).map(InboxPollerServiceTest::copy);
        }
    }

    /** Undoes the repository changes of a transaction that throws. */
    static class RollbackTransactions implements TransactionOperations {
        private final InMemoryRepository repository;
        int rolledBack;
        int count;
        final List<String> committedStates = new ArrayList<>();

        RollbackTransactions(InMemoryRepository repository) {
            this.repository = repository;
        }

        @Override
        public <T> T execute(TransactionCallback<T> action) {
            count++;
            Map<String, ReceivedEmail> before = repository.snapshot();
            try {
                T result = action.doInTransaction(null);
                repository.all().forEach(e -> committedStates.add(e.getState().name() + "@tx" + count));
                return result;
            } catch (RuntimeException e) {
                repository.restore(before);
                rolledBack++;
                throw e;
            }
        }

        @Override
        public void executeWithoutResult(Consumer<TransactionStatus> action) {
            execute(status -> {
                action.accept(status);
                return null;
            });
        }
    }

    static class RecordingSender implements EmailSender {
        record Sent(String to, String subject, String body) {
        }

        final List<Sent> sent = new ArrayList<>();
        boolean fail;

        Sent only() {
            assertThat(sent).hasSize(1);
            return sent.getFirst();
        }

        @Override
        public void send(String to, String subject, String body, List<UUID> attachmentIds) {
            if (fail) {
                throw new IllegalStateException("SMTP down");
            }
            sent.add(new Sent(to, subject, body));
        }
    }
}
