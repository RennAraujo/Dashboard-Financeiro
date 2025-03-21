package com.dashboard.financeiro.repository;

import com.dashboard.financeiro.model.Category;
import com.dashboard.financeiro.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByUser(User user);
}
