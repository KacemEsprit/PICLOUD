package tn.esprit.pidev.controller;

import tn.esprit.pidev.entity.Organization;
import tn.esprit.pidev.entity.Partner;
import tn.esprit.pidev.entity.PartnerContract;
import tn.esprit.pidev.repository.PartnerContractRepository;
import tn.esprit.pidev.repository.PartnerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
        Organization organization = (contract != null) ? contract.getOrganization() : null;

        String html = buildContractHtml(partner, contract, organization);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_HTML);
        headers.set("Content-Disposition", "inline; filename=contract_" + partnerId + ".html");

        return ResponseEntity.ok()
                .headers(headers)
                .body(html.getBytes());
    }

    private String buildContractHtml(Partner partner, PartnerContract contract, Organization org) {
        String contractType = contract != null ? contract.getContractType().toString() : "N/A";
        String status = contract != null ? contract.getStatus().toString() : "N/A";
        String startDate = contract != null ? contract.getStartDate().toString() : "N/A";
        String endDate = contract != null ? contract.getEndDate().toString() : "N/A";
        String description = contract != null && contract.getDescription() != null ? contract.getDescription() : "";

        String orgName = org != null ? org.getName() : "N/A";
        String orgEmail = org != null ? org.getEmail() : "N/A";
        String orgPhone = org != null ? org.getPhoneNumber() : "N/A";
        String orgType = org != null ? org.getType().toString() : "N/A";

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
                .parties { display: flex; gap: 20px; margin: 20px 0; }
                .party-box { flex: 1; padding: 15px; border-radius: 8px; border-left: 4px solid #1a73e8; background: #f8f9ff; }
                .party-box h3 { color: #1a237e; margin: 0 0 10px; }
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
                <div class="row"><span class="label">Contract Type:</span><span>%s</span></div>
                <div class="row"><span class="label">Status:</span><span class="badge">%s</span></div>
                <div class="row"><span class="label">Start Date:</span><span>%s</span></div>
                <div class="row"><span class="label">End Date:</span><span>%s</span></div>
              </div>

              <div class="parties">
                <div class="party-box">
                  <h3>🏢 Party 1 — Organization</h3>
                  <div class="row"><span class="label">Name:</span><span>%s</span></div>
                  <div class="row"><span class="label">Email:</span><span>%s</span></div>
                  <div class="row"><span class="label">Phone:</span><span>%s</span></div>
                  <div class="row"><span class="label">Type:</span><span>%s</span></div>
                </div>
                <div class="party-box">
                  <h3>🤝 Party 2 — Partner</h3>
                  <div class="row"><span class="label">Name:</span><span>%s</span></div>
                  <div class="row"><span class="label">Industry:</span><span>%s</span></div>
                  <div class="row"><span class="label">Email:</span><span>%s</span></div>
                  <div class="row"><span class="label">Phone:</span><span>%s</span></div>
                  <div class="row"><span class="label">Partnership Type:</span><span>%s</span></div>
                </div>
              </div>

              <div class="section">
                <h3>📝 Description</h3>
                <p>%s</p>
              </div>

              <div class="signature-section">
                <div class="signature-box">
                  <div class="signature-line">Partner Signature</div>
                  <div>%s</div>
                </div>
                <div class="signature-box">
                  <div class="signature-line">Organization Signature</div>
                  <div>%s</div>
                </div>
                <div class="signature-box">
                  <div class="signature-line">TransitTN Signature</div>
                  <div>🚌 TransitTN Platform</div>
                </div>
              </div>

              <div class="footer">
                <p>Generated by TransitTN Platform — %s</p>
                <p>Tunisia Public Transport Management System</p>
              </div>
            </body>
            </html>
            """.formatted(
                contractType, status, startDate, endDate,
                orgName, orgEmail, orgPhone, orgType,
                partner.getName(), partner.getIndustrySector(),
                partner.getEmail(), partner.getPhoneNumber(), partner.getPartnershipType(),
                description,
                partner.getName(), orgName,
                java.time.LocalDate.now().toString()
        );
    }
}
