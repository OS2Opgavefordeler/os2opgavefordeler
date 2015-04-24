package dk.os2opgavefordeler.service;

import dk.os2opgavefordeler.model.kle.KleMainGroup;

import java.util.List;

public interface PersistanceService {
	List<KleMainGroup> fetchAllKleMainGroups();
	void storeAllKleMainGroups(List<KleMainGroup> groups);
}
