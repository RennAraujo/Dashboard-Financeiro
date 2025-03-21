package com.dashboard.financeiro.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;
    
    @Column(name = "category_type")
    @Enumerated(EnumType.STRING)
    private CategoryType type;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    private List<Transaction> transactions = new ArrayList<>();
    
    public enum CategoryType {
        INCOME("Receita"),
        EXPENSE("Despesa");
        
        private final String description;
        
        CategoryType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}
