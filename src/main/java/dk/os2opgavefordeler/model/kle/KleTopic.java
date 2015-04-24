package dk.os2opgavefordeler.model.kle;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
public class KleTopic implements Serializable {
	private static final long serialVersionUID = 1L;

	@ManyToOne
	private KleGroup group;

	@Id
	@Column(nullable = false, updatable = false)
	private final String number;

	@Column(nullable = false)
	private final String title;

	@Column(nullable = false)
	private final String description;

	@Column(nullable = false)
	@Temporal(TemporalType.DATE)
	private final Date dateCreated;



	private KleTopic() {
		//for JPA
		this.number = null;
		this.title = null;
		this.description = null;
		this.dateCreated = null;
	}

	public KleTopic(String number, String title, String description, Date dateCreated) {
		this.number = number;
		this.title = title;
		this.description = description;
		this.dateCreated = dateCreated;
	}



	public String getNumber() {
		return number;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public Date getDateCreated() {
		return new Date(dateCreated.getTime());
	}



	@Override
	public String toString() {
		return String.format("KleTopic<%s,%s>", number, title);
	}
}
