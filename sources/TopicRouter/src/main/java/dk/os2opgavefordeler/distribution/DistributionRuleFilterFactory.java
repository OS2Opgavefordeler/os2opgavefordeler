package dk.os2opgavefordeler.distribution;

import javax.enterprise.context.RequestScoped;
import javax.persistence.EntityManager;

import javax.inject.Inject;

import dk.os2opgavefordeler.distribution.dto.CprDistributionRuleFilterDTO;
import dk.os2opgavefordeler.distribution.dto.DistributionRuleFilterDTO;
import dk.os2opgavefordeler.distribution.dto.TextDistributionRuleFilterDTO;

import dk.os2opgavefordeler.repository.EmploymentRepository;
import dk.os2opgavefordeler.repository.OrgUnitRepository;

import dk.os2opgavefordeler.model.CprDistributionRuleFilter;
import dk.os2opgavefordeler.model.DistributionRule;
import dk.os2opgavefordeler.model.DistributionRuleFilter;
import dk.os2opgavefordeler.model.TextDistributionRuleFilter;

@RequestScoped
public class DistributionRuleFilterFactory {

    @Inject
    private EntityManager entityManager;

    @Inject
    private OrgUnitRepository orgUnitRepository;

    @Inject
    private EmploymentRepository employmentRepository;

    @Inject
    private DistributionRuleRepository repository;

	/**
	 * Creates a DistributionRuleFilter from the specified DTO object
	 *
	 * @param dto DTO object
	 * @return the resulting DistributionRuleFilter
	 */
    public DistributionRuleFilter fromDto(DistributionRuleFilterDTO dto) {
        // try to create from existing
        DistributionRule rule = repository.findBy(dto.distributionRuleId);

        if (CprDistributionRuleFilterDTO.FILTER_TYPE.equals(dto.type)) {
            CprDistributionRuleFilter filter = new CprDistributionRuleFilter();

            if (rule.getFilterById(dto.filterId) != null) {
                filter = (CprDistributionRuleFilter) rule.getFilterById(dto.filterId);
            }

            filter.setMonths(dto.months);
            filter.setDays(dto.days);

            filter.setName(dto.name);
            filter.setAssignedEmployee(employmentRepository.findBy(dto.assignedEmployeeId));
            filter.setAssignedOrg(orgUnitRepository.findBy(dto.assignedOrgId));
            filter.setDistributionRule(rule);

            return filter;
        }
        else if (TextDistributionRuleFilterDTO.FILTER_TYPE.equals(dto.type)) {
            TextDistributionRuleFilter filter = new TextDistributionRuleFilter();

            if (rule.getFilterById(dto.filterId) != null) {
                filter = (TextDistributionRuleFilter) rule.getFilterById(dto.filterId);
            }

            filter.setText(dto.text);
            filter.setName(dto.name);
            filter.setAssignedEmployee(employmentRepository.findBy(dto.assignedEmployeeId));
            filter.setAssignedOrg(orgUnitRepository.findBy(dto.assignedOrgId));
            filter.setDistributionRule(rule);

            return filter;
        }

        throw new RuntimeException("Bad data");
    }

}
