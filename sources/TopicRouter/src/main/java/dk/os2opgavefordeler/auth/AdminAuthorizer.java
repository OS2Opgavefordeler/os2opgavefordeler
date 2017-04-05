package dk.os2opgavefordeler.auth;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.deltaspike.security.api.authorization.Secures;
import org.slf4j.Logger;

/**
 * @author rro@miracle.dk
 */
@ApplicationScoped
public class AdminAuthorizer {

	@Inject
	private AuthService authService;

	@Secures
	@AdminRequired
	public boolean doAdminCheck() throws Exception {
		return authService.isAdmin();
	}
}
