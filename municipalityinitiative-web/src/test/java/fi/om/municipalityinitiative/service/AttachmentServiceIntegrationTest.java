package fi.om.municipalityinitiative.service;

import fi.om.municipalityinitiative.dao.TestHelper;
import fi.om.municipalityinitiative.exceptions.AccessDeniedException;
import fi.om.municipalityinitiative.exceptions.FileUploadException;
import fi.om.municipalityinitiative.exceptions.InvalidAttachmentException;
import fi.om.municipalityinitiative.exceptions.OperationNotAllowedException;
import fi.om.municipalityinitiative.sql.QAttachment;
import fi.om.municipalityinitiative.util.FixState;
import fi.om.municipalityinitiative.util.InitiativeState;
import org.aspectj.util.FileUtil;
import org.im4java.core.InfoException;
import org.junit.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;

import static fi.om.municipalityinitiative.util.TestUtil.precondition;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.fail;

public class AttachmentServiceIntegrationTest extends ServiceIntegrationTestBase {

    public static final String JPG = "jpG";
    public static final String DESCRIPTION = "asd";
    public static final String FILE_NAME = DESCRIPTION + "." + JPG;
    public static final File TEST_JPG_FILE = new File(System.getProperty("user.dir") + "/src/test/resources/jpg-content-type.jpg");
    public static final File TEST_PNG_FILE = new File(System.getProperty("user.dir") + "/src/test/resources/png-content-type.png");
    public static final File TEST_PDF_FILE = new File(System.getProperty("user.dir") + "/src/test/resources/testi.pdf");
    public static final File TEST_TXT_FILE_CONTENT_WITH_JPG_SUFFIX = new File(System.getProperty("user.dir") + "/src/test/resources/text-content-type.jpg");

    @Resource
    TestHelper testHelper;

    @Resource
    AttachmentService attachmentService;

    private Long initiativeId;

    @Override
    protected void childSetup() {
        initiativeId = testHelper.createDefaultInitiative(new TestHelper.InitiativeDraft(testHelper.createTestMunicipality("mun"))
                .withState(InitiativeState.DRAFT)
                .applyAuthor()
                .toInitiativeDraft());
    }

    @Test
    public void find_all_attachments_is_ok_with_om_or_management_rights() {
        testHelper.addAttachment(initiativeId, "ok", false, JPG);
        assertThat(attachmentService.findAllAttachments(initiativeId, TestHelper.authorLoginUserHolder).getAll(), hasSize(1));
        assertThat(attachmentService.findAllAttachments(initiativeId, TestHelper.omLoginUser).getAll(), hasSize(1));
    }

    @Test(expected = AccessDeniedException.class)
    public void find_all_attachments_fails_if_normal_user() {
        testHelper.addAttachment(initiativeId, "ok", false, JPG);
        attachmentService.findAllAttachments(initiativeId, TestHelper.unknownLoginUserHolder);
    }

    @Test(expected = AccessDeniedException.class)
    public void saving_file_requires_management_rights() throws IOException, InfoException, FileUploadException, InvalidAttachmentException {
        attachmentService.addAttachment(initiativeId, TestHelper.unknownLoginUserHolder, multiPartFileMock("anyname", "anycontenttype"), "asd");
    }

    @Test
    public void saving_file_succeeds() throws IOException, InfoException, FileUploadException, InvalidAttachmentException {
        precondition(attachmentService.findAllAttachments(initiativeId, TestHelper.authorLoginUserHolder).getAll(), hasSize(0));
        attachmentService.addAttachment(initiativeId, TestHelper.authorLoginUserHolder, multiPartFileMock("anyfile.jpG", "image/jpeg", TEST_JPG_FILE), "someDescription");
        attachmentService.addAttachment(initiativeId, TestHelper.authorLoginUserHolder, multiPartFileMock("anyfile.pnG", "image/pnG", TEST_PNG_FILE), "someDescription");
        attachmentService.addAttachment(initiativeId, TestHelper.authorLoginUserHolder, multiPartFileMock("anyfile.pdF", "application/pdF", TEST_PDF_FILE), "someDescription");
        assertThat(attachmentService.findAllAttachments(initiativeId, TestHelper.authorLoginUserHolder).getAll(), hasSize(3));
    }

    @Test(expected = InvalidAttachmentException.class)
    public void saving_file_fails_if_invalid_filename() throws IOException, InfoException, FileUploadException, InvalidAttachmentException {
        attachmentService.addAttachment(initiativeId, TestHelper.authorLoginUserHolder, multiPartFileMock("anyfile.gif", "image/jpeg"), "someDescription");
    }

    @Test(expected = InvalidAttachmentException.class)
    public void saving_file_fails_if_invalid_content_type() throws IOException, InfoException, FileUploadException, InvalidAttachmentException {
        attachmentService.addAttachment(initiativeId, TestHelper.authorLoginUserHolder, multiPartFileMock("anyfile.jpG", "text"), "someDescription");
    }

    @Test(expected = InvalidAttachmentException.class)
    public void saving_file_fails_if_invalid_file_content_for_jpg() throws IOException, FileUploadException, InvalidAttachmentException {
        attachmentService.addAttachment(initiativeId, TestHelper.authorLoginUserHolder, multiPartFileMock("anyfile.jpG", "image/jpeg", TEST_PNG_FILE), "someDescription");
    }

    @Test(expected = InvalidAttachmentException.class)
    public void saving_file_fails_if_invalid_file_content_for_png() throws IOException, FileUploadException, InvalidAttachmentException {
        attachmentService.addAttachment(initiativeId, TestHelper.authorLoginUserHolder, multiPartFileMock("anyfile.pnG", "image/pnG", TEST_JPG_FILE), "someDescription");
    }

    @Test(expected = InvalidAttachmentException.class)
    public void saving_file_fails_if_invalid_file_content_for_pdf() throws IOException, FileUploadException, InvalidAttachmentException {
        attachmentService.addAttachment(initiativeId, TestHelper.authorLoginUserHolder, multiPartFileMock("anyfile.pdF", "application/pdF", TEST_PNG_FILE), "someDescription");
    }

    @Test(expected = OperationNotAllowedException.class)
    public void saving_file_is_disabllowed_if_at_published_state() throws IOException, InfoException, FileUploadException, InvalidAttachmentException {
        Long initiativeId = testHelper.createDefaultInitiative(new TestHelper.InitiativeDraft(testHelper.createTestMunicipality("someMunicipality"))
                .withState(InitiativeState.PUBLISHED)
                .withFixState(FixState.OK)
                .applyAuthor()
                .toInitiativeDraft());

        attachmentService.addAttachment(initiativeId, TestHelper.authorLoginUserHolder, null, null);
    }

    @Test(expected = AccessDeniedException.class)
    public void get_unaccepted_attachment_is_forbidden_if_unknown() throws IOException {
        Long attachmentId = testHelper.addAttachment(initiativeId, DESCRIPTION, false, JPG);
        attachmentService.getAttachment(attachmentId, FILE_NAME, TestHelper.unknownLoginUserHolder);
    }

    @Test
    public void get_unaccepted_attachment_is_allowed_if_author_or_om() throws IOException {
        Long attachmentId = testHelper.addAttachment(initiativeId, DESCRIPTION, false, JPG);

        createDummyTempAttachmentFile(attachmentId);

        attachmentService.getAttachment(attachmentId, FILE_NAME, TestHelper.omLoginUser);
        attachmentService.getAttachment(attachmentId, FILE_NAME, TestHelper.authorLoginUserHolder);
    }

    @Test
    public void get_attachment_with_invalid_filename_throws_access_denied_exception() throws IOException {
        Long attachmentId = testHelper.addAttachment(initiativeId, DESCRIPTION, true, JPG);

        createDummyTempAttachmentFile(attachmentId);
        attachmentService.getAttachment(attachmentId, FILE_NAME, TestHelper.omLoginUser);
        try {
            attachmentService.getAttachment(attachmentId, FILE_NAME + "ASD", TestHelper.unknownLoginUserHolder);
            fail("Should have failed due invalid attachment filename");
        } catch (AccessDeniedException e) {
            assertThat(e.getMessage(), containsString("Invalid filename"));
        }
    }

    @Test
    public void get_accepted_attachment_is_allowed() throws IOException {
        Long attachmentId = testHelper.addAttachment(initiativeId, DESCRIPTION, true, JPG);

        createDummyTempAttachmentFile(attachmentId);
        attachmentService.getAttachment(attachmentId, FILE_NAME, TestHelper.omLoginUser);
        attachmentService.getAttachment(attachmentId, FILE_NAME, TestHelper.authorLoginUserHolder);
        attachmentService.getAttachment(attachmentId, FILE_NAME, TestHelper.unknownLoginUserHolder);
    }

    @Test(expected = AccessDeniedException.class)
    public void get_unaccepted_thumbnail_is_forbidden_if_unknown() throws IOException {
        Long attachmentId = testHelper.addAttachment(initiativeId, DESCRIPTION, false, JPG);
        attachmentService.getThumbnail(attachmentId, TestHelper.unknownLoginUserHolder);
    }

    @Test
    public void get_unaccepted_thumbnail_is_allowed_if_author_or_om() throws IOException {
        Long attachmentId = testHelper.addAttachment(initiativeId, DESCRIPTION, false, JPG);

        createDummyTempAttachmentFile(attachmentId);

        attachmentService.getThumbnail(attachmentId, TestHelper.omLoginUser);
        attachmentService.getThumbnail(attachmentId, TestHelper.authorLoginUserHolder);
    }

    @Test
    public void get_accepted_thumbnail_is_allowed() throws IOException {
        Long attachmentId = testHelper.addAttachment(initiativeId, DESCRIPTION, true, JPG);

        createDummyTempAttachmentFile(attachmentId);
        attachmentService.getThumbnail(attachmentId, TestHelper.omLoginUser);
        attachmentService.getThumbnail(attachmentId, TestHelper.authorLoginUserHolder);
        attachmentService.getThumbnail(attachmentId, TestHelper.unknownLoginUserHolder);
    }

    @Test(expected = AccessDeniedException.class)
    public void delete_attachment_requires_management_rights() {
        Long attachmentId = testHelper.addAttachment(initiativeId, DESCRIPTION, true, JPG);
        attachmentService.deleteAttachment(attachmentId, TestHelper.unknownLoginUserHolder);
    }

    @Test
    public void delete_attachment_allows_deleting_if_author() {
        Long attachmentId = testHelper.addAttachment(initiativeId, "moi", true, JPG);

        precondition(testHelper.countAll(QAttachment.attachment), is(1L));
        attachmentService.deleteAttachment(attachmentId, TestHelper.authorLoginUserHolder);
        assertThat(testHelper.countAll(QAttachment.attachment), is(0L));
    }

    @Test
    public void check_file_is_jpeg() throws IOException {
        assertThat(AttachmentUtil.isJPEG(TEST_JPG_FILE), is(true));
        assertThat(AttachmentUtil.isJPEG(TEST_PNG_FILE), is(false));
        assertThat(AttachmentUtil.isJPEG(TEST_PDF_FILE), is(false));
        assertThat(AttachmentUtil.isJPEG(TEST_TXT_FILE_CONTENT_WITH_JPG_SUFFIX), is(false));
    }

    @Test
    public void check_file_is_png() throws IOException {
        assertThat(AttachmentUtil.isPNG(TEST_PNG_FILE), is(true));
        assertThat(AttachmentUtil.isPNG(TEST_PDF_FILE), is(false));
        assertThat(AttachmentUtil.isPNG(TEST_JPG_FILE), is(false));
        assertThat(AttachmentUtil.isPNG(TEST_TXT_FILE_CONTENT_WITH_JPG_SUFFIX), is(false));
    }

    @Test
    public void check_file_is_pdf() throws IOException {
        assertThat(AttachmentUtil.isPDF(TEST_PDF_FILE), is(true));
        assertThat(AttachmentUtil.isPDF(TEST_PNG_FILE), is(false));
        assertThat(AttachmentUtil.isPDF(TEST_JPG_FILE), is(false));
        assertThat(AttachmentUtil.isPDF(TEST_TXT_FILE_CONTENT_WITH_JPG_SUFFIX), is(false));
    }

    private void createDummyTempAttachmentFile(Long attachmentId) {
        FileUtil.writeAsString(new File(attachmentService.getAttachmentDir() + attachmentId + "." + JPG), DESCRIPTION); // Just some dummy file which this test will find
        FileUtil.writeAsString(new File(attachmentService.getAttachmentDir() + attachmentId + "_thumbnail." + JPG), DESCRIPTION); // Just some dummy file which this test will find
    }

    private static MultipartFile multiPartFileMock(String fileName, String contentType) throws IOException {

        return new MockMultipartFile(fileName, fileName, contentType, new byte[0]);

        /*MultipartFile multiPartFileMock = mock(MultipartFile.class);
        stub(multiPartFileMock.getOriginalFilename()).toReturn(fileName);
        stub(multiPartFileMock.getContentType()).toReturn(contentType);
        stub(multiPartFileMock.getBytes()).toReturn(new byte[0]);
        return multiPartFileMock;*/
    }

    private static MultipartFile multiPartFileMock(String fileName, String contentType, final File file) throws IOException {

        return new MockMultipartFile(fileName, fileName, contentType, FileUtil.readAsByteArray(file));

        /*MultipartFile multiPartFileMock = mock(MultipartFile.class);
        stub(multiPartFileMock.getOriginalFilename()).toReturn(fileName);
        stub(multiPartFileMock.getContentType()).toReturn(contentType);
        stub(multiPartFileMock.getBytes()).toReturn(Files.toByteArray(file));
        stub(multiPartFileMock.getInputStream()).toReturn(new FileInputStream(file));
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object argument = invocation.getArguments()[0];
                File givenFile = (File) argument;
                FileUtil.copyDir(file, givenFile);
                return null;
            }
        }).when(multiPartFileMock).transferTo(Mockito.any(File.class));
        return multiPartFileMock;*/

    }


}
