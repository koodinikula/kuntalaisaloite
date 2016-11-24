package fi.om.municipalityinitiative.conf.saml;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class RedirectingAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private String baseUrl;

    public RedirectingAuthenticationFailureHandler(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {

        exception.printStackTrace();

        String targetUri = TargetStoringFilter.popTarget(request, response);

        System.out.println(targetUri);


    }
}
