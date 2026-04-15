package tn.esprit.pidev.service;

import tn.esprit.pidev.entity.PartnerContract;
import tn.esprit.pidev.repository.PartnerContractRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class DigitalSignatureService {

    private static final Logger logger = LoggerFactory.getLogger(DigitalSignatureService.class);
    private final PartnerContractRepository contractRepository;

    /**
     * Generate SHA-256 hash of contract content
     */
    public String generateContentHash(PartnerContract contract) {
        try {
            String content = buildContractContent(contract);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            logger.error("Error generating content hash: {}", e.getMessage());
            throw new RuntimeException("Error generating hash", e);
        }
    }

    /**
     * Sign a contract digitally
     */
    public PartnerContract signContract(Long contractId, String signedBy) {
        PartnerContract contract = contractRepository.findById(contractId)
            .orElseThrow(() -> new RuntimeException("Contract not found"));

        if (Boolean.TRUE.equals(contract.getIsSigned())) {
            throw new RuntimeException("Contract is already signed");
        }

        // Generate content hash before signing
        String contentHash = generateContentHash(contract);

        // Generate signature hash (content + timestamp + signer)
        String signatureData = contentHash + LocalDateTime.now().toString() + signedBy;
        String signatureHash = generateHash(signatureData);

        contract.setContentHash(contentHash);
        contract.setSignatureHash(signatureHash);
        contract.setSignedAt(LocalDateTime.now());
        contract.setSignedBy(signedBy);
        contract.setIsSigned(true);
        contract.setSignatureValid(true);

        logger.info("Contract {} signed by {} with hash {}", contractId, signedBy, signatureHash);
        return contractRepository.save(contract);
    }

    /**
     * Verify contract signature integrity (fraud detection)
     */
    public FraudCheckResult verifySignature(Long contractId) {
        PartnerContract contract = contractRepository.findById(contractId)
            .orElseThrow(() -> new RuntimeException("Contract not found"));

        if (!Boolean.TRUE.equals(contract.getIsSigned())) {
            return new FraudCheckResult(false, false, "Contract is not signed", contract);
        }

        // Recalculate current content hash
        String currentHash = generateContentHash(contract);
        String originalHash = contract.getContentHash();

        boolean isValid = currentHash.equals(originalHash);

        if (!isValid) {
            // FRAUD DETECTED - update signature validity
            contract.setSignatureValid(false);
            contractRepository.save(contract);
            logger.warn("FRAUD DETECTED on contract {} - content has been modified after signing!", contractId);
            return new FraudCheckResult(true, false,
                "FRAUD DETECTED: Contract content was modified after signing!", contract);
        }

        logger.info("Contract {} signature verified successfully", contractId);
        return new FraudCheckResult(false, true, "Signature is valid - No fraud detected", contract);
    }

    private String buildContractContent(PartnerContract contract) {
        return String.format("%s|%s|%s|%s|%s|%s|%s",
            contract.getId(),
            contract.getContractType(),
            contract.getStatus(),
            contract.getStartDate(),
            contract.getEndDate(),
            contract.getDescription() != null ? contract.getDescription() : "",
            contract.getOrganization() != null ? contract.getOrganization().getId() : "",
            contract.getPartner() != null ? contract.getPartner().getId() : ""
        );
    }

    private String generateHash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Error generating hash", e);
        }
    }

    public record FraudCheckResult(
        boolean fraudDetected,
        boolean signatureValid,
        String message,
        PartnerContract contract
    ) {}
}
