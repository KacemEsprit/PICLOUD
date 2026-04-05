package com.transittn.organization_partner.controller;

import com.transittn.organization_partner.entity.Partner;
import com.transittn.organization_partner.entity.PartnerContract;
import com.transittn.organization_partner.repository.PartnerContractRepository;
import com.transittn.organization_partner.repository.PartnerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/pdf")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PdfController {

    private final PartnerRepository partnerRepository;
    private final PartnerContractRepository contractRepository;

    @GetMapping("/contract/{partnerId}")
    public ResponseEntity<byte[]> generateContract(@PathVariable Long partnerId) {

        Partner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new RuntimeException("Partner not found"));

        List<PartnerContract> contracts = contractRepository.findByPartnerId(partnerId);
        PartnerContract contract = contracts.isEmpty() ? null : contracts.get(0);

        String html = buildContractHtml(partner, contract);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_HTML);
        headers.set("Content-Disposition", "inline; filename=contract_" + partnerId + ".html");

        return ResponseEntity.ok()
                .headers(headers)
                .body(html.getBytes());
    }

    private String buildContractHtml(Partner partner, PartnerContract contract) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
              <meta charset="UTF-8">
              <style>
                body { font-family: Arial, sans-serif; margin: 40px; color: #333; }
                .header { text-align: center; border-bottom: 3px solid #1a237e; padding-bottom: 20px; margin-bottom: 30px; }
                .logo { font-size: 2rem; font-weight: bold; color: #1a237e; }
                .title { font-size: 1.5rem; color: #1a73e8; margin: 10px 0; }
                .section { margin: 20px 0; padding: 15px; background: #f8f9ff; border-radius: 8px; border-left: 4px solid #1a73e8; }
                .section h3 { color: #1a237e; margin: 0 0 10px; }
                .row { display: flex; justify-content: space-between; margin: 8px 0; }
                .label { font-weight: bold; color: #555; }
                .signature-section { margin-top: 60px; display: flex; justify-content: space-between; }
                .signature-box { text-align: center; width: 200px; }
                .signature-line { border-top: 2px solid #333; margin-top: 50px; padding-top: 10px; }
                .badge { background: #e8f5e9; color: #2e7d32; padding: 4px 12px; border-radius: 20px; font-weight: bold; }
                .footer { margin-top: 40px; text-align: center; color: #999; font-size: 0.85rem; border-top: 1px solid #eee; padding-top: 15px; }
              </style>
            </head>
            <body>
              <div class="header">
                <div class="logo">🚌 TransitTN</div>
                <div class="title">PARTNERSHIP CONTRACT</div>
                <div>Tunisia Public Transport Platform</div>
              </div>

              <div class="section">
                <h3>📋 Contract Information</h3>
                <div class="row">
                  <span class="label">Contract Type:</span>
                  <span>%s</span>
                </div>
                <div class="row">
                  <span class="label">Status:</span>
                  <span class="badge">%s</span>
                </div>
                <div class="row">
                  <span class="label">Start Date:</span>
                  <span>%s</span>
                </div>
                <div class="row">
                  <span class="label">End Date:</span>
                  <span>%s</span>
                </div>
              </div>

              <div class="section">
                <h3>🤝 Partner Information</h3>
                <div class="row">
                  <span class="label">Name:</span>
                  <span>%s</span>
                </div>
                <div class="row">
                  <span class="label">Industry:</span>
                  <span>%s</span>
                </div>
                <div class="row">
                  <span class="label">Email:</span>
                  <span>%s</span>
                </div>
                <div class="row">
                  <span class="label">Phone:</span>
                  <span>%s</span>
                </div>
                <div class="row">
                  <span class="label">Partnership Type:</span>
                  <span>%s</span>
                </div>
              </div>

              <div class="section">
                <h3>📝 Description</h3>
                <p>%s</p>
              </div>

              <div class="signature-section">
                <div class="signature-box">
                  <div class="signature-line">
                    <strong>Partner Signature</strong><br>
                    <small>%s</small>
                  </div>
                </div>
                <div class="signature-box">
                  <div class="signature-line">
                    <strong>TransitTN Signature</strong><br>
                    <small>🚌 TransitTN Platform</small>
                  </div>
                </div>
              </div>

              <div class="footer">
                Generated by TransitTN Platform — %s<br>
                Tunisia Public Transport Management System
              </div>
            </body>
            </html>
            """.formatted(
                contract != null ? contract.getContractType() : "COMMERCIAL",
                contract != null ? contract.getStatus() : "DRAFT",
                contract != null ? contract.getStartDate() : LocalDate.now(),
                contract != null ? contract.getEndDate() : LocalDate.now().plusYears(1),
                partner.getName(),
                partner.getIndustrySector(),
                partner.getEmail(),
                partner.getPhoneNumber(),
                partner.getPartnershipType(),
                contract != null ? contract.getDescription() : "Partnership Agreement",
                partner.getName(),
                LocalDate.now()
        );
    }
}