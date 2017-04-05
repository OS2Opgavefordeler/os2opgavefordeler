package dk.os2opgavefordeler.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import dk.os2opgavefordeler.LoggedInUser;
import dk.os2opgavefordeler.auth.AuthService;
import dk.os2opgavefordeler.auth.MunicipalityAdminRequired;
import dk.os2opgavefordeler.auth.UserLoggedIn;
import dk.os2opgavefordeler.logging.AuditLogged;
import dk.os2opgavefordeler.repository.UserRepository;
import dk.os2opgavefordeler.model.Employment;
import dk.os2opgavefordeler.model.Municipality;
import dk.os2opgavefordeler.model.OrgUnit;
import dk.os2opgavefordeler.model.User;
import dk.os2opgavefordeler.model.presentation.OrgUnitPO;
import dk.os2opgavefordeler.model.presentation.SimpleMessage;
import dk.os2opgavefordeler.orgunit.ImportService;
import dk.os2opgavefordeler.orgunit.OrgUnitDTO;
import dk.os2opgavefordeler.service.BadRequestArgumentException;
import dk.os2opgavefordeler.service.OrgUnitService;
import dk.os2opgavefordeler.service.UserService;
import dk.os2opgavefordeler.util.Validate;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.slf4j.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@UserLoggedIn
@AuditLogged
@Path("/org-units")
@RequestScoped
public class OrgUnitEndpoint extends Endpoint {
	public static final String FILE = "file";

	@Inject
	Logger log;

	@Inject
	OrgUnitService orgUnitService;

	@Inject
	private ImportService importService;

	@Inject
	UserService userService;

	@Context
	private HttpServletRequest request;

	@Inject @LoggedInUser
	private User user;

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response listAll(@QueryParam("municipalityId") Long municipalityId, @QueryParam("employmentId") Long employmentId) {
		// if employment, scope by that.
		try {
			Validate.nonZero(municipalityId, "Invalid municipalityId");
			List<OrgUnitPO> ou = new ArrayList<>();
			if (employmentId != null && employmentId > 0L) {
				ou = orgUnitService.getManagedOrgUnitsPO(municipalityId, employmentId);
			} else {
				ou = orgUnitService.getToplevelOrgUnitPO(municipalityId);
			}
			if (!ou.isEmpty()) {
				return ok(ou);
			} else {
				return notFound();
			}
		} catch (BadRequestArgumentException e) {
			return badRequest(e.getMessage());
		}
	}

	@GET
	@Path("/display")
	@Produces(MediaType.TEXT_PLAIN)
	public Response displayTree(@QueryParam("municipalityId") Long municipalityId) {
		try {
			Validate.nonZero(municipalityId, "Invalid municipalityId");
			final Optional<OrgUnit> result = orgUnitService.getToplevelOrgUnit(municipalityId);

			return result.map(
					ou -> Response.ok().entity(printOrg(new StringBuilder(), 0, ou))
			).orElseGet(
					() -> Response.status(404)
			).build();
		} catch (BadRequestArgumentException e) {
			return badRequest(e.getMessage());
		}
	}

	@GET
	@Path("/{orgId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response get(@PathParam("orgId") Long orgId) {
		try {
			Validate.nonZero(orgId, "Invalid orgId");
			final Optional<OrgUnitPO> result = orgUnitService.getOrgUnitPO(orgId);

			return result.map(
					ou -> Response.ok().entity(ou)
			).orElseGet(
					() -> Response.status(404)
			).build();
		} catch (BadRequestArgumentException e) {
			return badRequest(e.getMessage());
		}
	}

	@POST
	@Path("/import")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Deprecated
	@MunicipalityAdminRequired
	public Response importOrg(OrgUnit input) {
		fixupOrgUnit(input, user.getMunicipality());
		orgUnitService.importOrganization(input);
		return ok();
	}


	@POST
	@Path("/fileImport")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	@MunicipalityAdminRequired
	public Response fileImport(MultipartFormDataInput multipartInput) {
		Map<String, List<InputPart>> uploadForm = multipartInput.getFormDataMap();
		List<InputPart> inputParts = uploadForm.get(FILE);
		StringBuilder completeString = new StringBuilder();
		for (InputPart inputPart : inputParts) {
			try {
				completeString.append(inputPart.getBodyAsString());
			} catch (IOException e) {
				log.error("Error while reading import file: ", e);
			}
		}

		try {
			ObjectMapper mapper = new ObjectMapper();
			OrgUnitDTO orgUnitDTO = mapper.readValue(completeString.toString(), OrgUnitDTO.class);
			OrgUnit o = importService.importOrganization(user.getMunicipality().getId(), orgUnitDTO);
			log.info("Imported the following OrgUnit: {}", o.toString());
			return Response
					.ok()
					.entity(o.getId())
					.build();
		} catch (ImportService.InvalidMunicipalityException e) {
			log.error("Invalid municipality for import: {}", e);

			return badRequest("ERROR");
		} catch (IOException e) {
			log.error("Error while mapping JSON: ", e);
		}

		return ok();
	}

	private void fixupOrgUnit(OrgUnit input, Municipality municipality) {
		deduplicateManager(input);

		input.getEmployees().stream().forEach(this::fixupEmployee);

		input.setMunicipality(municipality);

		for (OrgUnit orgUnit : input.getChildren()) {
			fixupOrgUnit(orgUnit, municipality);
		}
	}

	private void deduplicateManager(OrgUnit input) {
		input.getManager().ifPresent(manager -> {
			if (input.getEmployees().contains(manager)) {
				// replace copy with reference
				input.removeEmployee(manager);
				input.addEmployee(manager);
			}
		});
	}

	private void fixupEmployee(Employment employee) {
		if (Strings.isNullOrEmpty(employee.getInitials())) {
			employee.setInitials("");
		}
		if (Strings.isNullOrEmpty(employee.getEmail())) {
			employee.setEmail(employee.getInitials() + "@syddjurs.dk");
		}
		if (Strings.isNullOrEmpty(employee.getPhone())) {
			employee.setPhone("");
		}
	}

	private StringBuilder printOrg(StringBuilder sb, int indent, OrgUnit org) {
		final String tabs = Strings.repeat("\t", indent);

		sb.append(tabs).append(String.format("%s - manager: %s\n", org, org.getManager()));

		sb.append(tabs).append("\tEmployees:\n");
		org.getEmployees().stream().filter(Employment::isActive).forEach(e -> sb.append(tabs).append("\t\t").append(e).append("\n"));

		sb.append(tabs).append("\tChildren:\n");
		org.getChildren().forEach(t -> printOrg(sb, indent + 2, t));

		return sb;
	}
}
