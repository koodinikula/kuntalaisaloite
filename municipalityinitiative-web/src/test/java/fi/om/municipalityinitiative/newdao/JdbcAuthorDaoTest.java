package fi.om.municipalityinitiative.newdao;

import fi.om.municipalityinitiative.conf.IntegrationTestConfiguration;
import fi.om.municipalityinitiative.dao.NotFoundException;
import fi.om.municipalityinitiative.dao.TestHelper;
import fi.om.municipalityinitiative.newdto.Author;
import fi.om.municipalityinitiative.newdto.service.AuthorInvitation;
import fi.om.municipalityinitiative.util.ReflectionTestUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={IntegrationTestConfiguration.class})
public class JdbcAuthorDaoTest {

    @Resource
    AuthorDao authorDao;

    @Resource
    TestHelper testHelper;
    private Long testMunicipality;

    @Before
    public void setUp() throws Exception {
        testHelper.dbCleanup();
        testMunicipality = testHelper.createTestMunicipality("what");
    }

    @Test
    public void create_and_get_and_delete() {
        AuthorInvitation original = ReflectionTestUtils.modifyAllFields(new AuthorInvitation());
        original.setInitiativeId(testHelper.createCollectableAccepted(testMunicipality));

        // Create
        authorDao.addAuthorInvitation(original);

        // Get
        AuthorInvitation gotFromDao = authorDao.getAuthorInvitation(original.getInitiativeId(), original.getConfirmationCode());
        ReflectionTestUtils.assertReflectionEquals(original, gotFromDao);

        // Delete
        authorDao.deleteAuthorInvitation(original.getInitiativeId(), original.getConfirmationCode());

        try {
            authorDao.getAuthorInvitation(original.getInitiativeId(), original.getConfirmationCode());
            fail("Should have failed due not found");
        } catch (NotFoundException e) { }
    }

    @Test
    public void find_authors_returns_at_least_one() {
        Long initiativeId = testHelper.createCollectableAccepted(testMunicipality);
        assertThat(authorDao.findAuthors(initiativeId), hasSize(1));
    }

    @Test
    public void get_author_information() {
        Long id = testHelper.createSingleSent(testMunicipality);

        Author author = authorDao.getAuthorInformation(id, TestHelper.TEST_MANAGEMENT_HASH);
        assertThat(author.getContactInfo().getAddress(), is(TestHelper.DEFAULT_AUTHOR_ADDRESS));
        assertThat(author.getContactInfo().getName(), is(TestHelper.DEFAULT_AUTHOR_NAME));
        assertThat(author.getContactInfo().getEmail(), is(TestHelper.DEFAULT_AUTHOR_EMAIL));
        assertThat(author.getContactInfo().getPhone(), is(TestHelper.DEFAULT_AUTHOR_PHONE));
        assertThat(author.getMunicipality().getId(), is(testMunicipality));

        ReflectionTestUtils.assertNoNullFields(author);
    }
}
