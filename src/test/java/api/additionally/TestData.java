package api.additionally;

public class TestData {
    public static SubscriberData createValidSubscriber() {
        return new SubscriberData("79241263770", "Алина", "Михайлова");
    }

    public static SubscriberData createInvalidPhoneSubscriber() {
        return new SubscriberData("792412623", "Алина", "Михайлова");
    }

    public static SubscriberData createSubscriberWithEmptyName() {
        return new SubscriberData("79241263770", "", "Михайлова");
    }

    public static SubscriberData createSubscriberWithInvalidTariff() {
        SubscriberData s = new SubscriberData("79241263770", "Иван", "Петров");
        s.setTariffId(0);
        return s;
    }

    public static SubscriberData createExistingSubscriber() {
        SubscriberData s = new SubscriberData("79241263770", "Алина", "Михайлова");
        s.setTariffId(2);
        s.setBalance(100.0);
        return s;
    }

    public static SubscriberData existValidSubscriber() {
        return new SubscriberData(2,"79241263770", "Алина", "Михайлова", 1);
    }

    public static SubscriberData nonExistSubscriber() {
        return new SubscriberData(19990,"75000000000", "Алина", "Михайлова", 1);
    }


}
