package com.dashboard.financeiro.service;

import com.dashboard.financeiro.model.Category;
import com.dashboard.financeiro.model.Transaction;
import com.dashboard.financeiro.model.User;
import com.dashboard.financeiro.repository.CategoryRepository;
import com.dashboard.financeiro.repository.TransactionRepository;
import com.dashboard.financeiro.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class TransactionServiceImpl implements TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Override
    @Transactional
    public Transaction save(Transaction transaction, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        
        Category category = categoryRepository.findById(transaction.getCategory().getId())
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));
        
        // Verifica se a categoria pertence ao usuário ou é uma categoria global
        if (category.getUser() != null && !category.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Acesso negado à categoria");
        }
        
        transaction.setUser(user);
        transaction.setCategory(category);
        
        return transactionRepository.save(transaction);
    }
    
    @Override
    public List<Transaction> findAllByUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        
        return transactionRepository.findByUserOrderByDateDesc(user);
    }
    
    @Override
    public List<Transaction> findByFilters(String username, Long categoryId, LocalDate startDate, LocalDate endDate) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        
        // Se não houver datas definidas, utilize o mês atual
        if (startDate == null) {
            startDate = LocalDate.now().withDayOfMonth(1);
        }
        
        if (endDate == null) {
            endDate = startDate.plusMonths(1).minusDays(1);
        }
        
        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));
            
            return transactionRepository.findByUserAndCategoryAndDateBetween(user, category, startDate, endDate);
        } else {
            return transactionRepository.findByUserAndDateBetween(user, startDate, endDate);
        }
    }
    
    @Override
    @Transactional
    public Transaction update(Long id, Transaction transactionDetails, String username) {
        Transaction transaction = findById(id, username);
        
        // Atualiza os campos
        transaction.setAmount(transactionDetails.getAmount());
        transaction.setDescription(transactionDetails.getDescription());
        transaction.setDate(transactionDetails.getDate());
        transaction.setType(transactionDetails.getType());
        
        // Verifica se a categoria foi alterada
        if (!transaction.getCategory().getId().equals(transactionDetails.getCategory().getId())) {
            Category newCategory = categoryRepository.findById(transactionDetails.getCategory().getId())
                    .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));
            
            // Verifica se a nova categoria pertence ao usuário ou é uma categoria global
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
            
            if (newCategory.getUser() != null && !newCategory.getUser().getId().equals(user.getId())) {
                throw new AccessDeniedException("Acesso negado à categoria");
            }
            
            transaction.setCategory(newCategory);
        }
        
        return transactionRepository.save(transaction);
    }
    
    @Override
    @Transactional
    public void delete(Long id, String username) {
        Transaction transaction = findById(id, username);
        transactionRepository.delete(transaction);
    }
    
    @Override
    public Transaction findById(Long id, String username) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transação não encontrada"));
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        
        // Verifica se a transação pertence ao usuário correto
        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Acesso negado a esta transação");
        }
        
        return transaction;
    }
}
