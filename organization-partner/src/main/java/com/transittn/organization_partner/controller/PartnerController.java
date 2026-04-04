package com.transittn.organization_partner.controller;

import com.transittn.organization_partner.dto.PartnerDTO;
import com.transittn.organization_partner.enums.PartnerStatus;
import com.transittn.organization_partner.service.PartnerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/partners")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PartnerController {

    private final PartnerService partnerService;

    @PostMapping
    public ResponseEntity<PartnerDTO> create(@Valid @RequestBody PartnerDTO dto) {
        return ResponseEntity.ok(partnerService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PartnerDTO> update(@PathVariable Long id,
                                             @Valid @RequestBody PartnerDTO dto) {
        return ResponseEntity.ok(partnerService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        partnerService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PartnerDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(partnerService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<PartnerDTO>> getAll() {
        return ResponseEntity.ok(partnerService.getAll());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<PartnerDTO>> getByStatus(@PathVariable PartnerStatus status) {
        return ResponseEntity.ok(partnerService.getByStatus(status));
    }
}