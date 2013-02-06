package fi.om.municipalityinitiative.web;

import org.junit.Ignore;
import org.junit.Test;

public class SendToMunicipalityWebTest extends WebTestBase {

    
    /**
     * Localization keys as constants.
     */
    private static final String MSG_SUCCESS_SEND = "success.send.title";
    private static final String MSG_BTN_SEND = "action.send";
    
    /**
     * Form values as constants.
     */
    private static final String MUNICIPALITY_1 = "Vantaa";
    private static final String COMMENT = "Saate kunnalle";
    
    @Test
    @Ignore("TODO")
    public void send_to_municipality() {
        Long municipality1Id = testHelper.createTestMunicipality(MUNICIPALITY_1);
//        Long municipality2Id = testHelper.createTestMunicipality(MUNICIPALITY_2);
        
        Long initiativeId = testHelper.createTestInitiative(municipality1Id, "Testi aloite", true, true);
        
        open(urls.management(initiativeId, "0000000000111111111122222222223333333333"));
        
        waitms(5000);
        
        clickLinkContaining(getMessage(MSG_BTN_SEND));
        
        waitms(5000);
        
        inputText("comment", COMMENT);
      /*  
        clickLinkContaining(getMessage(MSG_BTN_SEND));
        */
        
    }
    
}
