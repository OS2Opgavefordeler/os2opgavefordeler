package dk.os2opgavefordeler.rest;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import dk.os2opgavefordeler.LoggedInUser;
import dk.os2opgavefordeler.model.User;
import org.slf4j.Logger;

import dk.os2opgavefordeler.auth.KleAssignerRequired;
import dk.os2opgavefordeler.auth.UserLoggedIn;
import dk.os2opgavefordeler.model.Municipality;
import dk.os2opgavefordeler.model.OrgUnit;
import dk.os2opgavefordeler.model.presentation.KleAssignmentType;
import dk.os2opgavefordeler.model.presentation.OrgUnitListPO;
import dk.os2opgavefordeler.model.presentation.OrgUnitTreePO;
import dk.os2opgavefordeler.model.presentation.OrgUnitWithKLEPO;
import dk.os2opgavefordeler.service.KleService;
import dk.os2opgavefordeler.service.OrgUnitService;
import dk.os2opgavefordeler.service.OrgUnitWithKLEService;


@Path("/ou")
@RequestScoped
@UserLoggedIn
public class OUEndpoint extends Endpoint {
	
	@Inject
	private Logger log;

	@Inject
	private OrgUnitWithKLEService orgUnitWithKLEService;

	@Inject
	private OrgUnitService orgUnitService;

	@Inject
	private KleService kleService;

	@Inject @LoggedInUser
	private User currentUser;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/list")
	public Response list() {
		List<OrgUnitListPO> result = orgUnitWithKLEService.getList(getMunicipality().getId());
		return ok(result);
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response tree() {
		Optional<OrgUnit> result = orgUnitService.getToplevelOrgUnit(getMunicipality().getId());
		if (result.isPresent()) {
			OrgUnitTreePO value = new OrgUnitTreePO(result.get());
			return ok(Collections.singletonList(value));
		}
		return notFound("No data found.");
	}

	private Municipality getMunicipality() {
		return currentUser.getMunicipality();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{Id}")
	public Response get(@PathParam("Id") long id) {
		OrgUnitWithKLEPO result = orgUnitWithKLEService.get(id,getMunicipality());
		if (result != null) {
			return ok(result);
		} else {
			return notFound("Entity not found for ID: " + id);
		}
	}

	@POST
	@Path("/{ouId}/{assignmentType}/{kleNumber}")
	@Produces(MediaType.TEXT_PLAIN)
	@KleAssignerRequired
	public Response assignKLE(@PathParam("ouId") long ouId, @PathParam("assignmentType") String assignmentTypeString, @PathParam("kleNumber") String kleNumber) {

		// Check if ou exists
		Optional<OrgUnit> ou = orgUnitService.getOrgUnit(ouId,getMunicipality());
		if (!ou.isPresent()) {
			return badRequest("OrgUnit not found for ID: " + ouId);
		}

		// Check if assignment type is correct
		KleAssignmentType assignmentType;
		try {
			assignmentType = KleAssignmentType.fromString(assignmentTypeString);
		} catch (Exception e) {
			return badRequest("No assignment type with a name: \"" + assignmentTypeString + "\" found");
		}

		// Check if kle exists
		try {
			kleService.getKle(kleNumber);
		} catch (Exception e) {
			log.error("Failed to lookup a KLE from database.",e);
			return badRequest("No KLE for number: \"" + kleNumber + "\" found");
		}

		boolean kleUpdateSucceded = orgUnitWithKLEService.addKLE(ouId, kleNumber, assignmentType);
		if (kleUpdateSucceded) {
			return ok();
		}	else {
			return internalServerError();
		}

	}

	@DELETE
	@Path("/{ouId}/{assignmentType}/{kleNumber}")
	@Produces(MediaType.TEXT_PLAIN)
	@KleAssignerRequired
	public Response unassignKLE(@PathParam("ouId") long ouId, @PathParam("assignmentType") String assignmentTypeString, @PathParam("kleNumber") String kleNumber) {

		// Check if ou exists
		Optional<OrgUnit> ou = orgUnitService.getOrgUnit(ouId,getMunicipality());
		if (!ou.isPresent()) {
			return badRequest("OrgUnit not found for ID: " + ouId);
		}

		// Check if assignment type is correct
		KleAssignmentType assignmentType;
		try {
			assignmentType = KleAssignmentType.fromString(assignmentTypeString);
		} catch (Exception e) {
			return badRequest("No assignment type with a name: \"" + assignmentTypeString + "\" found");
		}

		//Check if OU contains that kleNumber
		if(!orgUnitWithKLEService.containsKLE(ou.get(),assignmentType,kleNumber)){
			log.info("Not removing KLE " + kleNumber + " from " + ou.get().getName() + " because it was not assigned previously");
			return ok();
		}

		boolean removeKleSucceded = orgUnitWithKLEService.removeKLE(ouId, kleNumber, assignmentType);
		if (removeKleSucceded) {
			return ok();
		} else {
			return internalServerError();
		}
	}
}
