package dk.os2opgavefordeler.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import dk.os2opgavefordeler.orgunit.ImportService;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.slf4j.Logger;

import com.google.common.collect.ImmutableList;

import dk.os2opgavefordeler.assigneesearch.Assignee;
import dk.os2opgavefordeler.assigneesearch.FindAssignedForKleService;
import dk.os2opgavefordeler.auth.AuthService;
import dk.os2opgavefordeler.logging.AuditLogged;
import dk.os2opgavefordeler.model.Kle;
import dk.os2opgavefordeler.model.Municipality;
import dk.os2opgavefordeler.model.OrgUnit;
import dk.os2opgavefordeler.model.api.DistributionRuleApiResultPO;
import dk.os2opgavefordeler.model.api.EmploymentApiResultPO;
import dk.os2opgavefordeler.model.presentation.KleAssignmentType;
import dk.os2opgavefordeler.orgunit.OrgUnitDTO;
import dk.os2opgavefordeler.repository.OrgUnitRepository;
import dk.os2opgavefordeler.service.DistributionService;
import dk.os2opgavefordeler.service.EmploymentService;
import dk.os2opgavefordeler.service.KleService;
import dk.os2opgavefordeler.service.MunicipalityService;
import dk.os2opgavefordeler.service.OrgUnitService;

/**
 * This class supports the endpoints that are part of the external/programmatic API.
 *
 * @author hlo@miracle.dk
 */
@AuditLogged
@Path("/api")
@RequestScoped
public class ApiEndpoint extends Endpoint {

	private static String NOT_AUTHORIZED = "Not authorized";
	private static String DID_NOT_FIND_A_MUNICIPALITY_BASED_ON_GIVEN_AUTHORIZATION = "Did not find a municipality based on given authorization.";
	private static String YOUR_SUBSCRIPTION_IS_NOT_ACTIVE_AND_THEREFOR_THE_API_CANNOT_BE_USED = "Your subscription is not active and therefor the api cannot be used.";
	private static String NO_ORG_UNIT_FOUND_FOR_PNUMBER = "No org unit found for pnumber";
	private static String NO_ONE_SEEMS_TO_BE_HANDLING_THE_GIVEN_KLE_FOR_MUNICIPALITY = "No one seems to be handling the given kle for municipality.";
	private static String DID_NOT_FIND_A_KLE_BASED_ON_GIVEN_NUMBER = "Did not find a Kle based on given number.";
	private static String ASSIGNMENT_TYPE_DOES_NOT_EXIST = "Assignment type does not exist.";

	@Inject
	Logger log;

	@Inject
	KleService kleService;

	@Inject
	MunicipalityService municipalityService;

	@Inject
	DistributionService distributionService;

	@Inject
	OrgUnitService orgUnitService;

	@Inject
	OrgUnitRepository orgUnitRepo;

	@Inject
	EmploymentService employmentService;

	@Inject
	private FindAssignedForKleService findAssignedForKleService;

	@Inject
	private AuthService authService;

	@Inject
	ImportService importService;

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response lookup(@QueryParam("kle") String kleNumber, @Context UriInfo uriInfo, @Context HttpServletRequest request) {
		try {
			Municipality municipality = authorize();
			Optional<Kle> kleMaybe = kleService.fetchMainGroup(kleNumber, municipality.getId());
			if (!kleMaybe.isPresent()) {
				return badRequest(DID_NOT_FIND_A_KLE_BASED_ON_GIVEN_NUMBER);
			}
			Kle kle = kleMaybe.get();

			Map<String, String> parameters = new HashMap<>();
			for (Map.Entry<String, List<String>> m : uriInfo.getQueryParameters().entrySet()) {
				parameters.put(m.getKey(), m.getValue().get(0));
			}

			Assignee assignee = findAssignedForKleService.findAssignedForKle(kle, municipality, parameters);
			if (assignee == null) {
				return notFound(NO_ONE_SEEMS_TO_BE_HANDLING_THE_GIVEN_KLE_FOR_MUNICIPALITY);
			}

			EmploymentApiResultPO manager = new EmploymentApiResultPO(orgUnitService.findResponsibleManager(assignee.getOrgUnit()).orElse(null));
			EmploymentApiResultPO employee = assignee.getEmployment().map(EmploymentApiResultPO::new).orElse(null);
			Optional<OrgUnit> distributionOrgUnit = assignee.getRule().getAssignedOrg();
			OrgUnit assignedOrg;
			if (distributionOrgUnit.isPresent()) {
				if (distributionOrgUnit.get().equals(assignee.getOrgUnit())) {
					assignedOrg = distributionOrgUnit.get();
				} else {
					assignedOrg = assignee.getOrgUnit();
				}
			} else {
				assignedOrg = assignee.getOrgUnit();
			}
			DistributionRuleApiResultPO resultPO = new DistributionRuleApiResultPO(assignee.getRule().getKle(), assignedOrg, manager, employee);
			log.info("API endpoint called by {} for KLE: {} with result: {}", authService.getAuthentication().getEmail(), resultPO.getKle().getNumber(), resultPO.getOrg().getName());
			return ok(resultPO);
		} catch (ApiException ae) {
			log.warn("rejected api call with reason: {}", ae.getReason());
			return ae.getResponse();
		}
	}


	@GET
	@Path("/orgunit/pNumber/{pNumber}")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response getByPNumber(@PathParam("pNumber") String pNumber) {
		try {
			Municipality municipality = authorize();
			OrgUnit orgUnit = orgUnitRepo.findByPNumberAndMunicipalityId(pNumber, municipality.getId());
			return ok(new OrgUnitDTO(orgUnit, OrgUnitDTO.Option.DO_NOT_INCLUDE_CHILDREN));
		} catch (NoResultException nre) {
			log.info("did not find org unit for pnumber: {}", pNumber);
			return notFound(NO_ORG_UNIT_FOUND_FOR_PNUMBER);
		} catch (ApiException ae) {
			log.warn("rejected api call with reason: " + ae.getReason());
			return ae.getResponse();
		}
	}

	@GET
	@Path("/ou/{businessKey}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response lookupOrgUnit(@PathParam("businessKey") String businessKey,
																@QueryParam("assignmentType") String assignmentTypeString,
																@DefaultValue("false") @QueryParam("showExpanded") boolean showExpanded) {

		try {
			Municipality municipality = authorize();
			List<KleAssignmentType> assignmentTypes = parseAssignmentTypes(assignmentTypeString);

			Optional<OrgUnit> ou = orgUnitService.findByBusinessKeyAndMunicipality(businessKey, municipality);
			if (!ou.isPresent()) {
				return notFound("Entity not found for BusinessKey: " + businessKey);
			}

			Map<KleAssignmentType, Set<String>> result = getKleForAssignmentTypes(assignmentTypes, ou.get(), showExpanded);
			return ok(result);
		} catch (ApiException ae) {
			log.warn("rejected api call with reason: {}", ae.getReason());
			return ae.getResponse();
		}
	}


	@GET
	@Path("/healthcheck")
	@Produces(MediaType.TEXT_PLAIN + "; charset=UTF-8")
	@NoCache
	public Response healthCheck() {
		//TODO: perform (light-weight) sanity checks.
		boolean everythingIsOk = true;

		if (everythingIsOk) {
			return ok("We get signal.");
		} else {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Somebody set up us the bomb.").build();
		}
	}

	@POST
	@Consumes("application/json")
	@Produces("application/json")
	@Path("/org-unit-import")
	public Response import_(OrgUnitDTO orgUnitDTO) {
		try {
			Municipality municipality = authorize();
			OrgUnit o = importService.importOrganization(municipality.getId(), orgUnitDTO);
			return ok(o);
		} catch (ApiException e) {
			log.warn("rejected api call with reason: {}", e.getReason());
			return e.getResponse();
		} catch (ImportService.InvalidMunicipalityException e) {
			log.warn("got InvalidMunicipalityException from ImportService: {}", e);
			return badRequest("InvalidMunicipalityException raised.");
		}
	}

		/**
		 * Authorizes the incoming call to the endpoint
		 *
		 * @return AuthorizeResult containing the municipality if success, or HTTP error status and message if not success.
		 */
	private Municipality authorize() throws ApiException {
		String token = authService.getAuthentication().getToken();
		if (token == null || token.isEmpty()) {
			throw new ApiException(Response.Status.UNAUTHORIZED, NOT_AUTHORIZED);
		}
		Optional<Municipality> municipalityMaybe = municipalityService.getMunicipalityFromToken(token);
		if (!municipalityMaybe.isPresent()) {
			throw new ApiException(Response.Status.UNAUTHORIZED, DID_NOT_FIND_A_MUNICIPALITY_BASED_ON_GIVEN_AUTHORIZATION);
		}

		Municipality municipality = municipalityMaybe.get();
		if (!municipality.isActive()) {
			throw new ApiException(Response.Status.PAYMENT_REQUIRED, YOUR_SUBSCRIPTION_IS_NOT_ACTIVE_AND_THEREFOR_THE_API_CANNOT_BE_USED);
		}
		return municipality;
	}

	private List<KleAssignmentType> parseAssignmentTypes(String assignmentTypeString) throws ApiException {
		List<KleAssignmentType> result = new ArrayList<>();
		if (assignmentTypeString != null) {
			try {
				result.add(KleAssignmentType.fromString(assignmentTypeString));
			} catch (IllegalArgumentException e) {
				throw new ApiException(Response.Status.BAD_REQUEST, ASSIGNMENT_TYPE_DOES_NOT_EXIST);
			}
		} else {
			result.add(KleAssignmentType.INTEREST);
			result.add(KleAssignmentType.PERFORMING);
		}
		return result;
	}

	private Map<KleAssignmentType, Set<String>> getKleForAssignmentTypes(List<KleAssignmentType> assignmentTypes, OrgUnit ou, boolean showExpanded){
		Map<KleAssignmentType, Set<String>> result = new HashMap<>();
		for (KleAssignmentType assignmentType : assignmentTypes) {
			Set<String> listKLE = new TreeSet<>();
			for (Kle kle : ou.getKles(assignmentType)) {
				addKle(showExpanded, listKLE, kle);
			}
			result.put(assignmentType, listKLE);
		}
		return result;
	}

	private void addKle(boolean showExpanded, Set<String> listKLE, Kle kle) {
		listKLE.add(kle.getNumber());
		if (showExpanded) {
			ImmutableList<Kle> subKLEs = kle.getChildren();
			if (subKLEs != null && !subKLEs.isEmpty()) {
				for (Kle sub : subKLEs) {
					addKle(true, listKLE, sub);
				}
			}
		}
	}

	private class ApiException extends Exception {
		Response response;
		private String reason;

		ApiException(Response.Status status, String reason) {
			response = Response.status(status).type(TEXT_PLAIN).entity(reason).build();
			this.reason = reason;
		}

		public Response getResponse() {
			return response;
		}

		String getReason() {
			return reason;
		}
	}
}
