package com.sbsolutions.rilybricoule.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "clients")
public class Client extends User {

    @Column(length = 500)
    private String address;

    private Double latitude;

    private Double longitude;
}
