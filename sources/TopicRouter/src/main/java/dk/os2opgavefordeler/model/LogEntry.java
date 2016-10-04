package dk.os2opgavefordeler.model;

import javax.json.Json;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import java.text.SimpleDateFormat;

import java.util.Date;

/**
 * Log entry describing a system event like a deletion of a record
 */
@Entity
@Table(name = "auditlog")
public class LogEntry {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name="eventtime", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date timeStamp;

    @Column(name="kle")
    private String kle;

    @Column(name="username")
    private String user;

    @Column(name="operation")
    private String operation;

    @Column(name="eventtype")
    private String type;

    @Column(name="eventdata")
    private String data;

    @Column(name = "orgunit")
    private String orgUnit;

    @Column(name = "employment")
    private String employment;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    private Municipality municipality;

    // operations
    public static final String CREATE_TYPE = "Opret";
    public static final String DELETE_TYPE = "Slet";
    public static final String UPDATE_TYPE = "Rediger";

    // types
    public static final String RESPONSIBILITY_TYPE = "Ansvar";
    public static final String DISTRIBUTION_TYPE = "Fordeling";
    public static final String EXTENDED_DISTRIBUTION_TYPE = "Udvidet fordeling";
    public static final String PARAMETER_NAME_TYPE = "Parameternavn";

    // date format
    @Transient
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");

    public LogEntry() {
        //for JPA
        this.timeStamp = new Date();

        this.kle = null;
        this.user = null;
        this.operation = null;
        this.type = null;
        this.data = null;
        this.orgUnit = null;
        this.employment = null;
        this.municipality = null;

    }

    public LogEntry(String kle, String user, String operation, String type, String data, String orgUnit, String employment, Municipality municipality) {
        this.timeStamp = new Date();

        this.kle = kle;
        this.user = user;
        this.operation = operation;
        this.type = type;
        this.data = data;
        this.orgUnit = orgUnit;
        this.employment = employment;
        this.municipality = municipality;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getTimestamp() {
        return timeStamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timeStamp = timestamp;
    }

    public String getKle() {
        return kle;
    }

    public void setKle(String kle) {
        this.kle = kle;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getOrgUnit() {
        return orgUnit;
    }

    public void setOrgUnit(String orgUnit) {
        this.orgUnit = orgUnit;
    }

    public String getEmployment() {
        return employment;
    }

    public void setEmployment(String employment) {
        this.employment = employment;
    }

    private String getReadableTimeStamp() {
        return dateFormat.format(timeStamp);
    }

    @Override
    public String toString() {
        return "LogEntry{" +
                "id=" + id +
                ", timeStamp=" + getReadableTimeStamp() +
                ", kle='" + kle + '\'' +
                ", user='" + user + '\'' +
                ", operation='" + operation + '\'' +
                ", type='" + type + '\'' +
                ", data='" + data + '\'' +
                ", orgUnit='" + orgUnit + '\'' +
                ", employment='" + employment + '\'' +
                '}';
    }

    public String toJson() {
        return Json.createObjectBuilder().
                add("id", id).
                add("timeStamp", getReadableTimeStamp()).
                add("kle", kle).
                add("user", user).
                add("operation", operation).
                add("type", type).
                add("data", data).
                add("orgUnit", orgUnit).
                add("employment", employment).build().toString();
    }
}