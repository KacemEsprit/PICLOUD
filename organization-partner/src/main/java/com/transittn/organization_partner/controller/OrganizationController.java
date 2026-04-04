package com.transittn.organization_partner.controller;

import com.transittn.organization_partner.dto.OrganizationDTO;
import com.transittn.organization_partner.enums.CoverageType;
import com.transittn.organization_partner.enums.OrgStatus;
import com.transittn.organization_partner.service.OrganizationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class OrganizationController {

    private final OrganizationService organizationService;

    @PostMapping
    public ResponseEntity<OrganizationDTO> create(@Valid @RequestBody OrganizationDTO dto) {
        return ResponseEntity.ok(organizationService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrganizationDTO> update(@PathVariable Long id,
                                                  @Valid @RequestBody OrganizationDTO dto) {
        return ResponseEntity.ok(organizationService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        organizationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrganizationDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(organizationService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<OrganizationDTO>> getAll() {
        return ResponseEntity.ok(organizationService.getAll());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrganizationDTO>> getByStatus(@PathVariable OrgStatus status) {
        return ResponseEntity.ok(organizationService.getByStatus(status));
    }

    @GetMapping("/coverage/{coverageType}")
    public ResponseEntity<List<OrganizationDTO>> getByCoverageType(
            @PathVariable CoverageType coverageType) {
        return ResponseEntity.ok(organizationService.getByCoverageType(coverageType));
    }
}