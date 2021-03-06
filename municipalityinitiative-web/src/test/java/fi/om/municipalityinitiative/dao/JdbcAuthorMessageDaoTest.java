package fi.om.municipalityinitiative.dao;

import fi.om.municipalityinitiative.conf.IntegrationTestConfiguration;
import fi.om.municipalityinitiative.dao.AuthorMessageDao;
import fi.om.municipalityinitiative.dao.TestHelper;
import fi.om.municipalityinitiative.dto.service.AuthorMessage;
import fi.om.municipalityinitiative.exceptions.NotFoundException;
import fi.om.municipalityinitiative.util.ReflectionTestUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import static fi.om.municipalityinitiative.util.TestUtil.precondition;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={IntegrationTestConfiguration.class})
@Transactional
public class JdbcAuthorMessageDaoTest {

    private static final String CONFIRMATION_CODE = "confirmation code";
    private static final String CONTACT_EMAIL = "contact@example.com";
    private static final String CONTACT_NAME = "Contact Name";
    private static final String MESSAGE = "Some message for all the authors";
    @Resource
    private TestHelper testHelper;

    @Resource
    private AuthorMessageDao authorMessageDao;

    @Before
    public void setUp() throws Exception {
        testHelper.dbCleanup();
    }

    @Test
    public void add_and_get() {

        Long initiative = testHelper.createSingleSent(testHelper.createTestMunicipality("Municipality"));
        AuthorMessage original = authorMessage(initiative);

        authorMessageDao.put(original);

        AuthorMessage result = authorMessageDao.pop(CONFIRMATION_CODE);
        ReflectionTestUtils.assertReflectionEquals(original, result);

    }

    @Test
    public void get_deletes_the_authorMessage() {
        Long initiative = testHelper.createSingleSent(testHelper.createTestMunicipality("Municipality"));
        AuthorMessage authorMessage = authorMessage(initiative);

        authorMessageDao.put(authorMessage);

        precondition(authorMessageDao.pop(CONFIRMATION_CODE), is(notNullValue()));
//        authorMessageDao.deleteAuthorMessage(CONFIRMATION_CODE);
        try {
            assertThat(authorMessageDao.pop(CONFIRMATION_CODE), is(nullValue()));
            fail("Should have thrown NotFoundException");
        } catch (NotFoundException e) {

        }

    }

    private static AuthorMessage authorMessage(Long initiative) {
        AuthorMessage original = new AuthorMessage();
        original.setConfirmationCode(CONFIRMATION_CODE);
        original.setContactEmail(CONTACT_EMAIL);
        original.setContactName(CONTACT_NAME);
        original.setMessage(MESSAGE);
        original.setInitiativeId(initiative);
        return original;
    }
}
