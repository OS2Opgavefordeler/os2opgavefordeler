package dk.os2opgavefordeler.distribution;

import com.google.common.collect.Iterables;
import dk.os2opgavefordeler.Kle.KleRepository;
import dk.os2opgavefordeler.distribution.dto.CprDistributionRuleFilterDTO;
import dk.os2opgavefordeler.model.DistributionRule;
import dk.os2opgavefordeler.model.Kle;
import dk.os2opgavefordeler.model.Municipality;
import dk.os2opgavefordeler.model.OrgUnit;
import dk.os2opgavefordeler.service.ConfigService;
import dk.os2opgavefordeler.test.UnitTest;
import org.apache.deltaspike.core.api.projectstage.ProjectStage;
import org.apache.deltaspike.testcontrol.api.TestControl;
import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Category(UnitTest.class)
@RunWith(CdiTestRunner.class)
@TestControl(projectStage = ProjectStage.Development.class)
public class DistributionRuleControllerTest {

    @Inject
    private DistributionRuleController controller;

    @Inject
    private DistributionRuleRepository repository;

    @Inject
    private KleRepository kleRepository;

    @Test
    @Ignore
    public void testCanCreateFilter() throws Exception {
        ConfigService configService = mock(ConfigService.class);
        when(configService.isAuditLogEnabled()).thenReturn(false);
        controller.setConfigService(configService);

        assertTrue(repository.findAll().size() == 0);

        Municipality m = new Municipality("test");
        OrgUnit o = OrgUnit.builder()
                .businessKey("123")
                .municipality(m)
                .build();

        DistributionRule rule = new DistributionRule();
        rule.setMunicipality(m);
        rule.setAssignedOrg(o);
        rule.setResponsibleOrg(o);

        repository.save(rule);

        CprDistributionRuleFilterDTO dto = new CprDistributionRuleFilterDTO();
        dto.name = "TestFilter";
        dto.assignedEmployeeId = -1;
        dto.assignedOrgId = o.getId();
        dto.days = "1-15";
        dto.months = "1-3";
        dto.distributionRuleId = rule.getId();

        controller.createFilter(dto);

        assertTrue(repository.findAll().size() == 1);
        assertEquals(Iterables.size(repository.findBy(rule.getId()).getFilters()), 1);
    }

    @Test
    @Ignore
    public void testInvalidOrgThrowsException() {
        ConfigService configService = mock(ConfigService.class);
        when(configService.isAuditLogEnabled()).thenReturn(false);
        controller.setConfigService(configService);

        Municipality m = new Municipality("test");
        OrgUnit o = OrgUnit.builder()
                .businessKey("123")
                .municipality(m)
                .build();

        DistributionRule rule = new DistributionRule();
        rule.setMunicipality(m);
        rule.setAssignedOrg(o);
        rule.setResponsibleOrg(o);

        repository.save(rule);

        CprDistributionRuleFilterDTO dto = new CprDistributionRuleFilterDTO();
        dto.name = "TestFilter";
        dto.assignedEmployeeId = -1;
        dto.assignedOrgId = 1000;
        dto.days = "1-15";
        dto.months = "1-3";
        dto.distributionRuleId = rule.getId();
        boolean called = false;
        try {
            controller.createFilter(dto);
        } catch (Exception e) {
            called = true;
        }
        assertTrue(called);
    }

    @Test
    @Ignore
    public void testCanDeleteFilter() throws Exception {
        ConfigService configService = mock(ConfigService.class);
        when(configService.isAuditLogEnabled()).thenReturn(false);
        controller.setConfigService(configService);

        Municipality m = new Municipality("test");
        OrgUnit o = OrgUnit.builder()
                .businessKey("123")
                .municipality(m)
                .build();

        Kle kle = new Kle();
        kle.setNumber("00.00");
        kle.setTitle("Kommunens styrelse");

        kleRepository.save(kle);

        DistributionRule rule = new DistributionRule();
        rule.setMunicipality(m);
        rule.setAssignedOrg(o);
        rule.setResponsibleOrg(o);
        rule.setKle(kle);

        repository.save(rule);

        CprDistributionRuleFilterDTO dto = new CprDistributionRuleFilterDTO();
        dto.name = "TestFilter";
        dto.assignedEmployeeId = -1;
        dto.assignedOrgId = o.getId();
        dto.days = "1-15";
        dto.months = "1-3";
        dto.distributionRuleId = rule.getId();

        controller.createFilter(dto);

        assertTrue(repository.findAll().size() == 1);
        assertEquals(Iterables.size(repository.findBy(rule.getId()).getFilters()), 1);

        controller.deleteFilter(rule.getId(), Iterables.get(rule.getFilters(), 0).getId());
        assertEquals(Iterables.size(repository.findBy(rule.getId()).getFilters()), 0);
    }
}