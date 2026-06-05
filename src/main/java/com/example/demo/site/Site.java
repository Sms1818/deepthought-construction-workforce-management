package com.example.demo.site;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sites")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Site {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "site_sequence")
    @SequenceGenerator(name = "site_sequence", sequenceName = "site_sequence", allocationSize = 1)
    private Long id;

    @Column(nullable = false)
    private String siteName;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private Boolean active = true;
}
