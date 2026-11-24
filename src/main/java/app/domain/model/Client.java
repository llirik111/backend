package app.domain.model;

import app.domain.exception.ValidationException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "clients")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "phone", nullable = false, unique = true)
    private String phone;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ClientStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "preferred_locker")
    private String preferredLocker;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<Membership> memberships = new ArrayList<>();

    public Client() {
    }

    public Client(Long id) {
        this.id = id;
    }

    public Client(Long id, String fullName, String phone, String email, LocalDate birthDate, ClientStatus status, Instant createdAt, Instant updatedAt, String preferredLocker) {
        this.id = id;
        this.fullName = validateFullName(fullName);
        this.phone = validatePhone(phone);
        this.email = validateEmail(email);
        this.birthDate = validateBirthDate(birthDate);
        this.status = status != null ? status : ClientStatus.ACTIVE;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
        this.preferredLocker = preferredLocker;
    }

    public static Client create(String fullName, String phone, String email, LocalDate birthDate) {
        return create(fullName, phone, email, birthDate, null);
    }

    public static Client create(String fullName, String phone, String email, LocalDate birthDate, String preferredLocker) {
        Instant now = Instant.now();
        return new Client(null, fullName, phone, email, birthDate, ClientStatus.ACTIVE, now, now, preferredLocker);
    }

    public void update(String fullName, String phone, String email, LocalDate birthDate) {
        update(fullName, phone, email, birthDate, this.preferredLocker);
    }

    public void update(String fullName, String phone, String email, LocalDate birthDate, String preferredLocker) {
        String validFullName = validateFullName(fullName);
        String validPhone = validatePhone(phone);
        String validEmail = validateEmail(email);
        LocalDate validBirthDate = validateBirthDate(birthDate);
        this.fullName = validFullName;
        this.phone = validPhone;
        this.email = validEmail;
        this.birthDate = validBirthDate;
        this.preferredLocker = preferredLocker;
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
    public String getPreferredLocker() { return preferredLocker; }
    public void setPreferredLocker(String preferredLocker) { this.preferredLocker = preferredLocker; }
    public List<Membership> getMemberships() { return memberships; }
    public void setMemberships(List<Membership> memberships) { this.memberships = memberships; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Client client = (Client) o;
        return Objects.equals(id, client.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
