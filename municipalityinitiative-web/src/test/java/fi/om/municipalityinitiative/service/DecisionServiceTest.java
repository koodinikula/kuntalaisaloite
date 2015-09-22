package fi.om.municipalityinitiative.service;


import fi.om.municipalityinitiative.dao.DecisionAttachmentDao;
import fi.om.municipalityinitiative.dao.TestHelper;
import fi.om.municipalityinitiative.dto.service.DecisionAttachmentFile;
import fi.om.municipalityinitiative.dto.ui.MunicipalityDecisionDto;
import fi.om.municipalityinitiative.exceptions.InvalidAttachmentException;
import org.aspectj.util.FileUtil;
import org.junit.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class DecisionServiceTest extends ServiceIntegrationTestBase  {



    @Resource
    private DecisionService decisionService;

    @Resource
    private DecisionAttachmentDao decisionAttachmentDao;

    private Long testMunicipalityId;

    private String DECISION_DESCRIPTION = "Kunnalla ei ole rahaa.";

    public static final File TEST_PDF_FILE = new File(System.getProperty("user.dir") + "/src/test/resources/testi.pdf");

    private String TESTI_PDF = "test.pdf";

    private String CONTENT_TYPE = "application/pdf";

    private String FILE_TYPE = "pdf";

    @Override
    protected void childSetup()  {
        testMunicipalityId = testHelper.createTestMunicipality("Some municipality");
    }

    @Test
    public void save_decision_and_get_decision() {
        Long initiativeId = createVerifiedInitiativeWithAuthor();

        MunicipalityDecisionDto decision;

        try {

            decision = createDefaultMunicipalityDecisionWithAttachment(initiativeId);

        } catch (Exception e) {

            e.printStackTrace();

        } finally {

            List<DecisionAttachmentFile> decisionAttachments = decisionService.getDecision(initiativeId);

            assertThat(decisionAttachments.size(), is(1));

            DecisionAttachmentFile fileInfo = decisionAttachments.get(0);

            assertThat(fileInfo.getFileType(), is(FILE_TYPE));

            assertThat(fileInfo.getContentType(), is(CONTENT_TYPE));

            assertThat(fileInfo.getFileName(), is(TESTI_PDF));

            assertThat(fileInfo.getInitiativeId(), is(initiativeId));

            assertThat(fileInfo.getFileName(), is(TESTI_PDF));

        }

    }
    @Test
    public void remove_attachment_from_decision() {

        Long initiativeId = createVerifiedInitiativeWithAuthor();

        MunicipalityDecisionDto decision;

        try {

            decision = createDefaultMunicipalityDecisionWithAttachment(initiativeId);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            List<DecisionAttachmentFile> decisionAttachments = decisionService.getDecision(initiativeId);

            assertThat(decisionAttachments.size(), is(1));

            DecisionAttachmentFile fileInfo = decisionAttachments.get(0);

            decisionService.removeAttachmentFromDecision(fileInfo.getAttachmentId(), initiativeId);

            decisionAttachments = decisionService.getDecision(initiativeId);

            assertThat(decisionAttachments.size(), is(0));


        }
    }

    private MunicipalityDecisionDto createDefaultMunicipalityDecisionWithAttachment(Long initiativeId) throws IOException, InvalidAttachmentException {
        MunicipalityDecisionDto decision = new MunicipalityDecisionDto();

        List<MultipartFile> files = new ArrayList<MultipartFile>();

        files.add(multiPartFileMock(
                TESTI_PDF, CONTENT_TYPE, TEST_PDF_FILE));

        decision.setFiles(files);

        decision.setDescription(DECISION_DESCRIPTION);

        decisionService.setDecision(decision, initiativeId);

        return decision;
    }



    private Long createVerifiedInitiativeWithAuthor() {
        return testHelper.createVerifiedInitiative(new TestHelper.InitiativeDraft(testMunicipalityId).applyAuthor().toInitiativeDraft());
    }

    private static MultipartFile multiPartFileMock(String fileName, String contentType, final File file) throws IOException {

        return new MockMultipartFile(fileName, fileName, contentType, FileUtil.readAsByteArray(file));

    }

}
