package info.simplecloud.scimproxy.compliance;

import info.simplecloud.core.Group;
import info.simplecloud.core.User;
import info.simplecloud.scimproxy.compliance.enteties.AuthMetod;
import info.simplecloud.scimproxy.compliance.enteties.Result;
import info.simplecloud.scimproxy.compliance.enteties.Statistics;
import info.simplecloud.scimproxy.compliance.enteties.TestResult;
import info.simplecloud.scimproxy.compliance.exception.CritialComplienceException;
import info.simplecloud.scimproxy.compliance.test.AttributeTest;
import info.simplecloud.scimproxy.compliance.test.ConfigTest;
import info.simplecloud.scimproxy.compliance.test.DeleteTest;
import info.simplecloud.scimproxy.compliance.test.FilterTest;
import info.simplecloud.scimproxy.compliance.test.PatchTest;
import info.simplecloud.scimproxy.compliance.test.PostTest;
import info.simplecloud.scimproxy.compliance.test.PutTest;
import info.simplecloud.scimproxy.compliance.test.ResourceCache;
import info.simplecloud.scimproxy.compliance.test.SortTest;

import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;

@Path("/test")
public class Compliance extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Result runTests(@FormParam("url") String url, @FormParam("username") String username, @FormParam("password") String password,
            @FormParam("clientId") String clientId, @FormParam("clientSecret") String clientSecret,
            @FormParam("authorizationServer") String authorizationServer, @FormParam("authorizationHeader") String authorizationHeader,
            @FormParam("authMethod") String authMethod) throws InterruptedException, ServletException {

        // TODO: remove when done coding!
        if (url == null || url.isEmpty()) {
            url = "http://127.0.0.1:8080";
        }

        ArrayList<TestResult> results = new ArrayList<TestResult>();


        String[] schemes = { "http", "https" };
        UrlValidator urlValidator = new UrlValidator(schemes);
        if (!urlValidator.isValid(url)) {
        	results.add(new TestResult(TestResult.ERROR, "Invalid service provider URL.", "", null));
            
            Statistics statistics = new Statistics();
            statistics.incFailed();
            return new Result(statistics, results);
        }

        // create a CSP to use to connect to the server
        CSP csp = new CSP();
        csp.setUrl(url);
        csp.setVersion("/v1");
        csp.setAuthentication(authMethod);
        csp.setUsername(username);
        csp.setPassword(password);
        csp.setOAuth2AuthorizationServer(authorizationServer);
        csp.setoAuth2ClientId(clientId);
        csp.setoAuth2ClientSecret(clientSecret);
        csp.setoAuth2GrantType("password");
        csp.setAuthorizationHeader(authorizationHeader);

        // get the configuration
        try {
            // start with the critical tests (will throw exception and test will
            // stop if fails)
            ConfigTest configTest = new ConfigTest();
            results.add(configTest.getConfiguration(csp));

            if ((AuthMetod.AUTH_BASIC.equalsIgnoreCase(authMethod) && StringUtils.isEmpty(username) && StringUtils.isEmpty(password))
                    || (AuthMetod.AUTH_OAUTH.equalsIgnoreCase(authMethod) && StringUtils.isEmpty(username) && StringUtils.isEmpty(password)
                            && StringUtils.isEmpty(authorizationServer) && StringUtils.isEmpty(clientId) && StringUtils
                            .isEmpty(clientSecret))) {
                ServiceProviderConfig spc = csp.getSpc();
                return new Result(spc.getAuthenticationSchemes());
            }

            results.add(configTest.getSchema("Users", csp));
            results.add(configTest.getSchema("Groups", csp));

            // TODO: add the required attributes in userSchema and groupSchema
            // that server wanted

            ResourceCache<User> userCache = new ResourceCache<User>();
            ResourceCache<Group> groupCache = new ResourceCache<Group>();

            results.addAll(new PostTest(csp, userCache, groupCache).run());
            results.addAll(new FilterTest(csp, userCache, groupCache).run());
            results.addAll(new PatchTest(csp, userCache, groupCache).run());
            results.addAll(new PutTest(csp, userCache, groupCache).run());
            results.addAll(new SortTest(csp, userCache, groupCache).run());
            results.addAll(new AttributeTest(csp, userCache, groupCache).run());
            results.addAll(new DeleteTest(csp, userCache, groupCache).run());

        } catch (CritialComplienceException e) {
            results.add(((CritialComplienceException) e).getResult());
        } catch (Throwable e) {
            results.add(new TestResult(TestResult.ERROR, "Unknown Test", e.getMessage(), ComplienceUtils.getWire(e)));
        }

        Statistics statistics = new Statistics();
        for (TestResult result : results) {

            switch (result.getStatus()) {
            case TestResult.ERROR:
                statistics.incFailed();
                break;
            case TestResult.SUCCESS:
                statistics.incSuccess();
                break;
            case TestResult.SKIPPED:
                statistics.incSkipped();
                break;
            }
        }
        return new Result(statistics, results);
    }
}
