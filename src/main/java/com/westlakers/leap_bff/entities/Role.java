package com.westlakers.leap_bff.entities;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Role {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long roleId;

    @Column(name="role_name", length=50, nullable=false, unique=true)
    private String roleName;
}
