package com.westlakers.leap_bff.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name="users")
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
public class User {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long userId;
    
    @Column(name="first_name", length=50, nullable=false)
    private String firstName;

    @Column(name="last_name", length=50, nullable=false)
    private String lastName;

    @Column(name="phone_number", length=20, nullable=false, unique=true)
    private String phoneNumber;

    @Column(name="tax_id", length=20, nullable=true, unique =true)
    private String taxId;
    
    @Column(name="date_of_birth", nullable=false)
    private LocalDate dateOfBirth;
    
    @Column(name="create_date",  insertable=false, updatable=false)
    private LocalDateTime createDate;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="status_id", nullable=false)
    private UserStatus status;

    @OneToMany(mappedBy = "user", cascade=CascadeType.ALL)
    private List<Account> accounts; 
    
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private UserCredentials credentials;
}