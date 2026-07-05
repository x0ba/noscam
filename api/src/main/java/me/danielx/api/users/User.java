package me.danielx.api.users;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @NotNull
    private Long id;

    @Builder.Default
    @NotNull
    @Column(nullable = false, unique = true, updatable = false)
    private UUID publicId = UUID.randomUUID();

    @Column(nullable = false, unique = true, length = 254)
    @NotNull
    @Length(max = 254)
    private String email;

    @Column(nullable = false)
    private boolean emailVerified = false;

    @Column(nullable = false, length = 255)
    @NotNull
    @Length(max = 255)
    private String passwordHash;

    @Column(nullable = false, length = 100)
    @NotNull
    @Length(max = 100)
    private String firstName;

    @Column(nullable = false, length = 100)
    @NotNull
    @Length(max = 100)
    private String lastName;

    @Column(nullable = false)
    @NotNull
    private Instant createdAt;

    @Column(nullable = false)
    @NotNull
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

}
