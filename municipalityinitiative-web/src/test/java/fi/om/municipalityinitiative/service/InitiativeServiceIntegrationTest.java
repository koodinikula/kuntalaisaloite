package fi.om.municipalityinitiative.service;

import fi.om.municipalityinitiative.conf.IntegrationTestConfiguration;
import fi.om.municipalityinitiative.dao.TestHelper;
import fi.om.municipalityinitiative.newdto.ui.*;
import fi.om.municipalityinitiative.sql.QMunicipalityInitiative;
import fi.om.municipalityinitiative.util.ParticipatingUnallowedException;
import fi.om.municipalityinitiative.util.TestUtils;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import java.util.Random;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={IntegrationTestConfiguration.class})
public class InitiativeServiceIntegrationTest {

    @Resource
    private InitiativeService service;

    @Resource
    TestHelper testHelper;

    private MunicipalityInfo testMunicipality;

    @Before
    public void setup() {
        testHelper.dbCleanup();
        testMunicipality = new MunicipalityInfo();
        testMunicipality.setName("Test municipality");
        testMunicipality.setId(testHelper.createTestMunicipality(testMunicipality.getName()));
    }


    @Test
    public void createNotCollectable() {
        service.createMunicipalityInitiative(createDto(false));
        assertThat(testHelper.countAll(QMunicipalityInitiative.municipalityInitiative), is(1L));
    }
    
    @Test
    public void createCollectable() {
        service.createMunicipalityInitiative(createDto(true));
        assertThat(testHelper.countAll(QMunicipalityInitiative.municipalityInitiative), is(1L));
    }    

    @Test
    public void all_fields_are_set_when_getting_municipalityInitiativeInfo() {
        Long initiativeId = service.createMunicipalityInitiative(createDto(true));
        InitiativeViewInfo initiative = service.getMunicipalityInitiative(initiativeId);
        TestUtils.assertNoNullFields(initiative);
    }

    @Test
    public void create_and_get() {
        InitiativeUICreateDto createDto = createDto(false);
        Long initiativeId = service.createMunicipalityInitiative(createDto);
        InitiativeViewInfo initiative = service.getMunicipalityInitiative(initiativeId);

        assertThat(initiative.getId(), is(initiativeId));
        assertThat(initiative.getAuthorName(), is(createDto.getContactInfo().getName()));
        assertThat(initiative.getName(), is(createDto.getName()));
        assertThat(initiative.getProposal(), is(createDto.getProposal()));
        assertThat(initiative.isShowName(), is(createDto.getShowName()));
        assertThat(initiative.getMunicipalityName(), is(testMunicipality.getName()));

        assertThat(initiative.getCreateTime(), is(notNullValue()));
        assertThat(initiative.getManagementHash(), is(org.hamcrest.Matchers.nullValue()));
        assertThat(initiative.isCollectable(), is(false));
        assertThat(initiative.getSentTime().isPresent(), is(true));

        // TODO: Verify all other values somehow
    }

    @Test
    public void creating_collectable_initiative_adds_hash() {
        InitiativeUICreateDto createDto = createDto(true);
        Long initiativeId = service.createMunicipalityInitiative(createDto);
        InitiativeViewInfo initiative = service.getMunicipalityInitiative(initiativeId);

        assertThat(initiative.getManagementHash(), is("0000000000111111111122222222223333333333"));
        assertThat(initiative.isCollectable(), is(true));
    }

    @Test
    public void creating_collectable_initiative_leaves_sent_time_null() {
        InitiativeUICreateDto createDto = createDto(true);
        Long initiativeId = service.createMunicipalityInitiative(createDto);
        InitiativeViewInfo initiative = service.getMunicipalityInitiative(initiativeId);

        assertThat(initiative.getSentTime().isPresent(), is(false));
    }

    @Test
    public void participating_to_non_collectable_initiative_is_forbidden() {
        Long initiative = testHelper.createTestInitiative(testMunicipality.getId());
        ParticipantUICreateDto participant = new ParticipantUICreateDto();
        participant.setParticipantName("Some Name");
        participant.setShowName(true);
        participant.setHomeMunicipality(testMunicipality.getId());
        try {
            service.createParticipant(participant, initiative);
            fail("Expected ParticipatingUnallowedException");
        } catch(ParticipatingUnallowedException e) {
            assertThat(e.getMessage(), containsString("Initiative not collectable"));
        }
    }

    @Test
    public void participating_to_sent_but_collectable_initiative_is_forbidden() {
        Long initiativeId = testHelper.createTestInitiative(testMunicipality.getId(), "initiative title", false, true);
        ParticipantUICreateDto participant = new ParticipantUICreateDto();
        participant.setParticipantName("Some Name");
        participant.setShowName(true);
        participant.setHomeMunicipality(testMunicipality.getId());

        testHelper.updateField(initiativeId, QMunicipalityInitiative.municipalityInitiative.sent, new DateTime());

        try {
            service.createParticipant(participant, initiativeId);
            fail("Expected ParticipatingUnallowedException");
        } catch(ParticipatingUnallowedException e) {
            assertThat(e.getMessage(), containsString("Initiative already sent"));
        }
    }

    private InitiativeUICreateDto createDto(boolean collectable) {
        InitiativeUICreateDto createDto = new InitiativeUICreateDto();
        createDto.setProposal("Proposal " + randomString());
        createDto.setName("Name " + randomString());
        createDto.setFranchise(true);
        createDto.setShowName(true);
        createDto.setMunicipality(testMunicipality.getId());
        createDto.setHomeMunicipality(testMunicipality.getId());
        createDto.setMunicipalMembership(true);
        createDto.setCollectable(collectable);

        createDto.setContactInfo(new ContactInfo());
        createDto.getContactInfo().setAddress("contact address " + randomString());
        createDto.getContactInfo().setPhone("contact phone " + randomString());
        createDto.getContactInfo().setName("contact name " + randomString());
        createDto.getContactInfo().setEmail("contact email " + randomString());

        TestUtils.assertNoNullFields(createDto);
        return createDto;
    }


    private static String randomString() {
        return String.valueOf(new Random().nextInt());
    }

}