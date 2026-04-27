package tn.esprit.pidev.dto;

public class PaymentInitRequest {
    private Long passengerId;
    private Long pricingPlanId;
    private String codeReduction;

    public PaymentInitRequest() {}
    public Long getPassengerId() { return passengerId; }
    public void setPassengerId(Long passengerId) { this.passengerId = passengerId; }
    public Long getPricingPlanId() { return pricingPlanId; }
    public void setPricingPlanId(Long pricingPlanId) { this.pricingPlanId = pricingPlanId; }
    public String getCodeReduction() { return codeReduction; }
    public void setCodeReduction(String codeReduction) { this.codeReduction = codeReduction; }
}