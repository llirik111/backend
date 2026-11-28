package app.domain;

public final class DomainLock {
    public static final Object INSTANCE = new Object();
    private DomainLock() {}
}
