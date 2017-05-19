package dk.os2opgavefordeler.rest;

import dk.os2opgavefordeler.LoggedInUser;
import dk.os2opgavefordeler.model.User;
import dk.os2opgavefordeler.model.ValidationException;
import dk.os2opgavefordeler.model.presentation.KlePO;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 * Super class for endpoints providing general methods for returning responses etc.
 *
 * @author hlo@miracle.dk
 */
public abstract class Endpoint {

	static final String TEXT_PLAIN = "text/plain";

	private static final Status BAD_REQUEST = Status.BAD_REQUEST;
	public static final String NOT_AUTHORIZED = "Not authorized";

	@Inject
	Logger logger;

	@Inject @LoggedInUser
	private User currentUser;

	void verifyMunicipalityIdForMunicipalityAdmin(long municipalityId) throws ValidationException {
		if( municipalityId < 1 ){
			throw new ValidationException("Municipality id is not set.");
		} else if( municipalityId != currentUser.getMunicipality().getId() ) {
			logger.warn("user: {} tried to use municipality id: {}", currentUser, municipalityId);
			throw new ValidationException("Municipality id does not match current users.");
		}
	}

	void verifyKle(KlePO kle) throws ValidationException {
		if( kle == null) {
			throw new ValidationException("kle cannot be null.");
		}
	}

	void verifyKle(long kleId) throws ValidationException {
		if( kleId < 1) {
			throw new ValidationException("kle cannot be null.");
		}
	}

	/**
	 * Creates an error response.
	 *
	 * @param reason The plain text error message wanted as 'entity'.
	 * @return a built Response with status 'bad request', type 'text/plain' and 'reason' as entity.
	 */
	protected Response badRequest(String reason) {
		return Response.status(BAD_REQUEST).type(TEXT_PLAIN).entity(reason).build();
	}

	/**
	 * Creates an ok response.
	 *
	 * @return a built Response with status ok and no entity set.
	 */
	protected Response ok() {
		return Response.ok().build();
	}

	/**
	 * Builds an ok response with object
	 *
	 * @param result The Object wanted as 'entity'
	 * @return a built Response with status ok and 'result' as entity.
	 */
  protected Response ok(Object result) {
		return Response.ok().entity(result).build();
	}

	/**
	 *
	 */
	protected Response notAuthorized() {
		return Response.status(Response.Status.UNAUTHORIZED).entity(NOT_AUTHORIZED).build();
	}

	/**
	 * Creates a forbidden response.
	 *
	 * @return a built Response with status forbidden and no entity set.
	 */
	Response forbidden() {
		return Response.status(Response.Status.FORBIDDEN).build();
	}

	/**
	 * Creates a not found response.
	 *
	 * @return a built Response with not found and no entity set.
	 */
	Response notFound() {
		return Response.status(Status.NOT_FOUND).build();
	}

	/**
	 * Creates a not found response with given message.
	 *
	 * @return a built Response with not found and entity set with message.
	 */
	Response notFound(String message) {
		return Response.status(Status.NOT_FOUND).entity(message).build();
	}


	Response internalServerError() { return Response.status(Status.INTERNAL_SERVER_ERROR).build(); }
}
