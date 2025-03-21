package com.dashboard.financeiro.service;

import com.dashboard.financeiro.model.Category;
import com.dashboard.financeiro.model.FinancialGoal;
import com.dashboard.financeiro.model.User;
import com.dashboard.financeiro.repository.CategoryRepository;
import com.dashboard.financeiro.repository.FinancialGoalRepository;
import com.dashboard.financeiro.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class FinancialGoalService {

    @Autowired
    private FinancialGoalRepository financialGoalRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    public List<FinancialGoal> findAllByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
                
        return financialGoalRepository.findByUser(user);
    }
    
    public List<FinancialGoal> findAchievedGoalsByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
                
        return financialGoalRepository.findByUserAndAchievedTrue(user);
    }
    
    @Transactional
    public FinancialGoal save(FinancialGoal goal, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        
        // Se houver categoria, verifique se ela existe e pertence ao usuário
        if (goal.getCategory() != null && goal.getCategory().getId() != null) {
            Category category = categoryRepository.findById(goal.getCategory().getId())
                    .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));
            
            // Verifica se a categoria pertence ao usuário ou é global
            if (category.getUser() != null && !category.getUser().getId().equals(user.getId())) {
                throw new AccessDeniedException("Acesso negado à categoria");
            }
            
            goal.setCategory(category);
        }
        
        goal.setUser(user);
        
        // Verificar se a meta foi atingida
        if (goal.getCurrentAmount().compareTo(goal.getTargetAmount()) >= 0) {
            goal.setAchieved(true);
        }
        
        return financialGoalRepository.save(goal);
    }
    
    @Transactional
    public FinancialGoal updateProgress(Long goalId, BigDecimal newAmount, String username) {
        FinancialGoal goal = findById(goalId, username);
        
        goal.setCurrentAmount(newAmount);
        
        // Verificar se a meta foi atingida
        if (goal.getCurrentAmount().compareTo(goal.getTargetAmount()) >= 0) {
            goal.setAchieved(true);
        }
        
        return financialGoalRepository.save(goal);
    }
    
    public FinancialGoal findById(Long id, String username) {
        FinancialGoal goal = financialGoalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Meta financeira não encontrada"));
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        
        // Verificar se a meta pertence ao usuário
        if (!goal.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Acesso negado a esta meta financeira");
        }
        
        return goal;
    }
    
    @Transactional
    public void delete(Long id, String username) {
        FinancialGoal goal = findById(id, username);
        financialGoalRepository.delete(goal);
    }
}
