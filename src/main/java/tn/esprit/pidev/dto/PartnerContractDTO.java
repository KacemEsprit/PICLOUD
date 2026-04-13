package tn.esprit.pidev.dto;

import tn.esprit.pidev.enums.ContractStatus;
import tn.esprit.pidev.enums.ContractType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartnerContractDTO {
    private Long id;

    @NotNull(message = "Le type de contrat est obligatoire")
    private ContractType contractType;

    @NotNull(message = "Le statut est obligatoire")
    private ContractStatus status;

    @NotNull(message = "La date de debut est obligatoire")
    private Date startDate;

    @NotNull(message = "La date de fin est obligatoire")
    private Date endDate;

    @Size(max = 500, message = "La description ne doit pas depasser 500 caracteres")
    private String description;

    private Long organizationId;
    private String organizationName;

    private Long partnerId;
    private String partnerName;
}
