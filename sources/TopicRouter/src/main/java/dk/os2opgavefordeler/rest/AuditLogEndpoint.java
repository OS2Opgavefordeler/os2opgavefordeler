package dk.os2opgavefordeler.rest;

import java.io.IOException;
import java.io.StringWriter;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.RequestScoped;

import javax.inject.Inject;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import dk.os2opgavefordeler.LoggedInUser;
import dk.os2opgavefordeler.auth.UserLoggedIn;
import org.slf4j.Logger;

import au.com.bytecode.opencsv.CSVWriter;

import dk.os2opgavefordeler.auth.AuthService;

import dk.os2opgavefordeler.model.User;

import dk.os2opgavefordeler.service.AuditLogService;


/**
 * Endpoint for retrieving audit log information
 */
@UserLoggedIn
@Path("/auditlog")
@RequestScoped
public class AuditLogEndpoint extends Endpoint {

	@Inject
	private AuthService authService;

	@Inject
	AuditLogService auditLogService;

	@Inject
	@LoggedInUser
	private User currentUser;

	@Inject
	Logger log;

	private static final String NOT_LOGGED_IN = "Not logged in";
	private static final String NOT_AUTHORIZED = "Not authorized";
	private static final String USER_NOT_FOUND = "User not found";

	private static final char CSV_SEPARATOR_CHAR = ';';
	private static final char BOM_CHAR = '\uFEFF';

	/**
	 * Returns the full list of audit log entries for the user's municipality
	 *
	 * @return list of audit log entries in JSON format
	 */
	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response getLogEntries() {
		if (accessGranted(currentUser.getId())) { // only managers and admins can fetch audit log data
			return ok(auditLogService.getAllLogEntries(currentUser.getMunicipality().getId()));
		} else {
			return Response.status(Response.Status.UNAUTHORIZED).entity(NOT_AUTHORIZED).build();
		}
	}

	/**
	 * Returns the full list of audit log entries for the user's municipality
	 *
	 * @return list of audit log entries in CSV format
	 */
	@GET
	@Path("/csv")
	@Produces("text/csv; charset=UTF-8")
	public Response getLogEntriesCsv() {
		if (accessGranted(currentUser.getId())) { // only managers and admins can fetch audit log data
			String myCsvText = "";

			try {
				List<String[]> valuesList = new ArrayList<>();

				// add header row
				valuesList.add(new String[]{"ID", "Tidspunkt", "KLE", "Bruger", "Operation", "Type", "Data", "Org", "Ansættelse"});

				// add content rows
				auditLogService.getAllLogEntries(currentUser.getMunicipality().getId()).forEach(e -> valuesList.add(e.toStringArray()));

				StringWriter stringWriter = new StringWriter();

				stringWriter.append(BOM_CHAR); // we need to append a byte order mark (BOM) to help Excel's horrible handling of CSV files

				CSVWriter csvWriter = new CSVWriter(stringWriter, CSV_SEPARATOR_CHAR);
				csvWriter.writeAll(valuesList);

				csvWriter.flush();
				csvWriter.close();

				String csvString = stringWriter.toString();

				myCsvText = new String(csvString.getBytes(), Charset.forName("UTF-8"));
			} catch (IOException e) {
				log.error("Error while generating CSV log data", e);
			}

			return Response.ok(myCsvText).build();
		} else {
			return Response.status(Response.Status.UNAUTHORIZED).entity(NOT_AUTHORIZED).build();
		}
	}

	private boolean accessGranted(long userId) {
		return authService.isAdmin() || authService.isMunicipalityAdmin() || currentUser.isManager();
	}

}
