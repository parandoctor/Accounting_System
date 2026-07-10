package com.accounting.system.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "t_category")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private CategoryType type;

    @Column(length = 100)
    private String icon;

    @Column(name = "sort_order")
    private Integer sortOrder;

    public enum CategoryType {
        INCOME, EXPENSE
    }
}
