package dk.os2opgavefordeler.auth;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.deltaspike.security.api.authorization.Secures;
import org.slf4j.Logger;

/**
 * @author rro@miracle.dk.
 */
@ApplicationScoped
public class MunicipalityAdminAuthorizer {

	@Inject
	private AuthService authService;

	@Inject
	Logger logger;

	@Secures
	@MunicipalityAdminRequired
	public boolean doMunicipalityAdminCheck() throws Exception {
		return authService.isMunicipalityAdmin();
	}
}
