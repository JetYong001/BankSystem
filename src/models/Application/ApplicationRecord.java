package models.Application;

public class ApplicationRecord {
    private final String applicationId;
    private final String applicationType;
    private final String applicantUsername;
    private final String status;
    private final String submittedAt;
    private final String fullName;
    private final String email;
    private final String phoneNumber;
    private final String identityNumber;
    private final String addressLine;
    private final String city;
    private final String state;
    private final String postCode;
    private final String initialDeposit;


    private final String tenureMonths;

    private final String applicationPurpose;
    private final String dateOfBirth;
    private final String employerName;
    private final String occupation;
    private final String monthlyIncome;
    private final String existingCustomer;
    private final String cardType;
    private final String requestedLimit;
    private final String primaryCardholderName;
    private final String primaryCardNumber;
    private final String relationshipToPrimary;
    private final String subcardName;

    public ApplicationRecord(
            String applicationId,
            String applicationType,
            String applicantUsername,
            String status,
            String submittedAt,
            String fullName,
            String email,
            String phoneNumber,
            String identityNumber,
            String addressLine,
            String city,
            String state,
            String postCode,
            String initialDeposit,
            String tenureMonths, // 2. 构造函数增加参数
            String applicationPurpose,
            String dateOfBirth,
            String employerName,
            String occupation,
            String monthlyIncome,
            String existingCustomer,
            String cardType,
            String requestedLimit,
            String primaryCardholderName,
            String primaryCardNumber,
            String relationshipToPrimary,
            String subcardName
    ) {
        this.applicationId = applicationId;
        this.applicationType = applicationType;
        this.applicantUsername = applicantUsername;
        this.status = status;
        this.submittedAt = submittedAt;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.identityNumber = identityNumber;
        this.addressLine = addressLine;
        this.city = city;
        this.state = state;
        this.postCode = postCode;
        this.initialDeposit = initialDeposit;
        this.tenureMonths = tenureMonths; // 3. 初始化
        this.applicationPurpose = applicationPurpose;
        this.dateOfBirth = dateOfBirth;
        this.employerName = employerName;
        this.occupation = occupation;
        this.monthlyIncome = monthlyIncome;
        this.existingCustomer = existingCustomer;
        this.cardType = cardType;
        this.requestedLimit = requestedLimit;
        this.primaryCardholderName = primaryCardholderName;
        this.primaryCardNumber = primaryCardNumber;
        this.relationshipToPrimary = relationshipToPrimary;
        this.subcardName = subcardName;
    }

    public String getApplicationId() { return applicationId; }
    public String getApplicationType() { return applicationType; }
    public String getApplicantUsername() { return applicantUsername; }
    public String getStatus() { return status; }
    public String getSubmittedAt() { return submittedAt; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getIdentityNumber() { return identityNumber; }
    public String getAddressLine() { return addressLine; }
    public String getCity() { return city; }
    public String getState() { return state; }
    public String getPostCode() { return postCode; }
    public String getInitialDeposit() { return initialDeposit; }

    // 4. 增加 Getter
    public String getTenureMonths() { return tenureMonths; }

    public String getApplicationPurpose() { return applicationPurpose; }
    public String getDateOfBirth() { return dateOfBirth; }
    public String getEmployerName() { return employerName; }
    public String getOccupation() { return occupation; }
    public String getMonthlyIncome() { return monthlyIncome; }
    public String getExistingCustomer() { return existingCustomer; }
    public String getCardType() { return cardType; }
    public String getRequestedLimit() { return requestedLimit; }
    public String getPrimaryCardholderName() { return primaryCardholderName; }
    public String getPrimaryCardNumber() { return primaryCardNumber; }
    public String getRelationshipToPrimary() { return relationshipToPrimary; }
    public String getSubcardName() { return subcardName; }
}