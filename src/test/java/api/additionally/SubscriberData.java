package api.additionally;

public class SubscriberData {
    private Integer subscriberId;
    private String msisdn;
    private String firstName;
    private String secondName;
    private String surname;
    private Integer tariffId;
    private Double balance;

    public SubscriberData(String msisdn, String firstName, String surname) {
        this.msisdn = msisdn;
        this.firstName = firstName;
        this.surname = surname;
    }

    public SubscriberData(Integer subscriberId, String msisdn, String firstName, String surname, Integer tariffId) {
        this.subscriberId = subscriberId;
        this.msisdn = msisdn;
        this.firstName = firstName;
        this.surname = surname;
        this.tariffId = tariffId;
    }

    public SubscriberData(Integer subscriberId, String msisdn, String firstName, String secondName,
                          String surname, Integer tariffId, Double balance) {
        this.subscriberId = subscriberId;
        this.msisdn = msisdn;
        this.firstName = firstName;
        this.secondName = secondName;
        this.surname = surname;
        this.tariffId = tariffId;
        this.balance = balance;
    }

    // Геттеры

    public Integer getSubscriberId() { return subscriberId; }

    public String getMsisdn() {
        return msisdn;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getSecondName() {
        return secondName;
    }

    public String getSurname() {
        return surname;
    }

    public Integer getTariffId() {
        return tariffId;
    }

    public Double getBalance() {
        return balance;
    }

    // Сеттеры для необязательных полей
    public void setSecondName(String secondName) {
        this.secondName = secondName;
    }

    public void setTariffId(Integer tariffId) {
        this.tariffId = tariffId;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }
}