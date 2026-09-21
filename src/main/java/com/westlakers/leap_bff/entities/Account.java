package com.westlakers.leap_bff.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name="accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Account {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long accountId;
    
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="user_id", nullable=false)
    private User user;
    
    @Column(name="account_type_id", nullable=false)
    private Long accountTypeId;
    
    @Column(name="account_status_id", nullable=false)
    private Long accountStatusId;
    
    @Column(name="created_at", insertable=false, updatable=false)
    private LocalDateTime createdAt;
    
    @Column(name="currency", length=10, nullable=false)
    private String currency;
    
    @Column(name="settled_cash", precision=19, scale=4)
    private BigDecimal settledCash;
}
