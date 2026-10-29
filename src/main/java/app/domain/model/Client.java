package app.domain.model;

import app.domain.exception.ValidationException;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

public class Client {
    private Long id;
    private String fullName;
    private String phone;
    private String email;
    private LocalDate birthDate;
    private ClientStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    public Client(Long id, String fullName, String phone, String email, LocalDate birthDate, ClientStatus status, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.fullName = validateFullName(fullName);
        this.phone = validatePhone(phone);
        this.email = validateEmail(email);
        this.birthDate = validateBirthDate(birthDate);
        this.status = status != null ? status : ClientStatus.ACTIVE;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
    }

    public static Client create(String fullName, String phone, String email, LocalDate birthDate) {
        Instant now = Instant.now();
        return new Client(null, fullName, phone, email, birthDate, ClientStatus.ACTIVE, now, now);
    }

    public void update(String fullName, String phone, String email, LocalDate birthDate) {
        this.fullName = validateFullName(fullName);
        this.phone = validatePhone(phone);
        this.email = validateEmail(email);
        this.birthDate = validateBirthDate(birthDate);
        this.updatedAt = Instant.now();
    }

    public void block() {
        this.status = ClientStatus.BLOCKED;
        this.updatedAt = Instant.now();
    }

    public void unblock() {
        this.status = ClientStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }

    private static String validateFullName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) throw new ValidationException("Full name must not be blank");
        return fullName.trim();
    }

    private static String validatePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) throw new ValidationException("Phone number must not be blank");
        return phone.trim();
    }

    private static String validateEmail(String email) {
        if (email == null || !email.matches("^[\\w.+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) throw new ValidationException("Email is invalid");
        return email.trim();
    }

    private static LocalDate validateBirthDate(LocalDate birthDate) {
        if (birthDate == null) throw new ValidationException("Birth date must not be null");
        if (birthDate.isAfter(LocalDate.now())) throw new ValidationException("Birth date cannot be in the future");
        return birthDate;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFullName() { return fullName; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public LocalDate getBirthDate() { return birthDate; }
    public ClientStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return Objects.equals(id, ((Client) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
