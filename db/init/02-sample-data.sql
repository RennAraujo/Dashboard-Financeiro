-- Dados de exemplo para o Dashboard Financeiro

-- Inserção de usuários de teste
-- Senha 'password' (bcrypt) = $2a$10$rJSMO6UfrGnvqj9Uv5bYWOTZ4Vj5.ZB1t9ZtOUmOVtXJbGIDgKOey
INSERT INTO users (username, password, email, full_name, role)
VALUES 
    ('usuario', '$2a$10$rJSMO6UfrGnvqj9Uv5bYWOTZ4Vj5.ZB1t9ZtOUmOVtXJbGIDgKOey', 'usuario@example.com', 'Usuário Demonstração', 'ROLE_USER'),
    ('admin', '$2a$10$rJSMO6UfrGnvqj9Uv5bYWOTZ4Vj5.ZB1t9ZtOUmOVtXJbGIDgKOey', 'admin@example.com', 'Administrador', 'ROLE_ADMIN')
ON CONFLICT (username) DO NOTHING;

-- Inserção de categorias para o usuário demonstração
INSERT INTO categories (name, description, color, user_id)
VALUES
    ('Alimentação', 'Gastos com refeições, supermercado e lanches', '#FF5733', (SELECT id FROM users WHERE username = 'usuario')),
    ('Moradia', 'Aluguel, condomínio, luz, água, etc', '#33A8FF', (SELECT id FROM users WHERE username = 'usuario')),
    ('Transporte', 'Combustível, transporte público, manutenção', '#33FF57', (SELECT id FROM users WHERE username = 'usuario')),
    ('Entretenimento', 'Cinema, shows, jogos, assinaturas', '#A633FF', (SELECT id FROM users WHERE username = 'usuario')),
    ('Saúde', 'Consultas médicas, remédios, plano de saúde', '#FF33A8', (SELECT id FROM users WHERE username = 'usuario')),
    ('Educação', 'Cursos, livros, material escolar', '#FFD700', (SELECT id FROM users WHERE username = 'usuario')),
    ('Salário', 'Renda mensal do trabalho', '#00FF00', (SELECT id FROM users WHERE username = 'usuario')),
    ('Investimentos', 'Rendimentos de aplicações financeiras', '#00FFFF', (SELECT id FROM users WHERE username = 'usuario')),
    ('Outros', 'Despesas e receitas diversas', '#CCCCCC', (SELECT id FROM users WHERE username = 'usuario'))
ON CONFLICT DO NOTHING;

-- Inserção de transações de exemplo para o último mês
-- Despesas
INSERT INTO transactions (description, amount, date, type, notes, category_id, user_id)
VALUES
    ('Supermercado', 450.00, CURRENT_DATE - INTERVAL '25 days', 'EXPENSE', 'Compras do mês', 
        (SELECT id FROM categories WHERE name = 'Alimentação' AND user_id = (SELECT id FROM users WHERE username = 'usuario')),
        (SELECT id FROM users WHERE username = 'usuario')),
    ('Aluguel', 1200.00, CURRENT_DATE - INTERVAL '20 days', 'EXPENSE', 'Aluguel de março', 
        (SELECT id FROM categories WHERE name = 'Moradia' AND user_id = (SELECT id FROM users WHERE username = 'usuario')),
        (SELECT id FROM users WHERE username = 'usuario')),
    ('Conta de luz', 150.00, CURRENT_DATE - INTERVAL '18 days', 'EXPENSE', '', 
        (SELECT id FROM categories WHERE name = 'Moradia' AND user_id = (SELECT id FROM users WHERE username = 'usuario')),
        (SELECT id FROM users WHERE username = 'usuario')),
    ('Abastecimento carro', 200.00, CURRENT_DATE - INTERVAL '15 days', 'EXPENSE', '', 
        (SELECT id FROM categories WHERE name = 'Transporte' AND user_id = (SELECT id FROM users WHERE username = 'usuario')),
        (SELECT id FROM users WHERE username = 'usuario')),
    ('Cinema', 80.00, CURRENT_DATE - INTERVAL '10 days', 'EXPENSE', 'Sessão com amigos', 
        (SELECT id FROM categories WHERE name = 'Entretenimento' AND user_id = (SELECT id FROM users WHERE username = 'usuario')),
        (SELECT id FROM users WHERE username = 'usuario')),
    ('Farmácia', 120.00, CURRENT_DATE - INTERVAL '5 days', 'EXPENSE', 'Medicamentos', 
        (SELECT id FROM categories WHERE name = 'Saúde' AND user_id = (SELECT id FROM users WHERE username = 'usuario')),
        (SELECT id FROM users WHERE username = 'usuario')),
    ('Restaurante', 150.00, CURRENT_DATE - INTERVAL '2 days', 'EXPENSE', 'Jantar de aniversário', 
        (SELECT id FROM categories WHERE name = 'Alimentação' AND user_id = (SELECT id FROM users WHERE username = 'usuario')),
        (SELECT id FROM users WHERE username = 'usuario'))
ON CONFLICT DO NOTHING;

-- Receitas
INSERT INTO transactions (description, amount, date, type, notes, category_id, user_id)
VALUES
    ('Salário', 5000.00, CURRENT_DATE - INTERVAL '30 days', 'INCOME', 'Pagamento mensal', 
        (SELECT id FROM categories WHERE name = 'Salário' AND user_id = (SELECT id FROM users WHERE username = 'usuario')),
        (SELECT id FROM users WHERE username = 'usuario')),
    ('Bônus', 1000.00, CURRENT_DATE - INTERVAL '29 days', 'INCOME', 'Bônus por resultado', 
        (SELECT id FROM categories WHERE name = 'Salário' AND user_id = (SELECT id FROM users WHERE username = 'usuario')),
        (SELECT id FROM users WHERE username = 'usuario')),
    ('Rendimento poupança', 200.00, CURRENT_DATE - INTERVAL '15 days', 'INCOME', '', 
        (SELECT id FROM categories WHERE name = 'Investimentos' AND user_id = (SELECT id FROM users WHERE username = 'usuario')),
        (SELECT id FROM users WHERE username = 'usuario'))
ON CONFLICT DO NOTHING;

-- Inserção de transações de meses anteriores para histórico
-- Mês passado - despesas
INSERT INTO transactions (description, amount, date, type, notes, category_id, user_id)
VALUES
    ('Supermercado', 420.00, CURRENT_DATE - INTERVAL '55 days', 'EXPENSE', 'Compras do mês', 
        (SELECT id FROM categories WHERE name = 'Alimentação' AND user_id = (SELECT id FROM users WHERE username = 'usuario')),
        (SELECT id FROM users WHERE username = 'usuario')),
    ('Aluguel', 1200.00, CURRENT_DATE - INTERVAL '50 days', 'EXPENSE', 'Aluguel de fevereiro', 
        (SELECT id FROM categories WHERE name = 'Moradia' AND user_id = (SELECT id FROM users WHERE username = 'usuario')),
        (SELECT id FROM users WHERE username = 'usuario')),
    ('Internet', 100.00, CURRENT_DATE - INTERVAL '48 days', 'EXPENSE', '', 
        (SELECT id FROM categories WHERE name = 'Moradia' AND user_id = (SELECT id FROM users WHERE username = 'usuario')),
        (SELECT id FROM users WHERE username = 'usuario'))
ON CONFLICT DO NOTHING;

-- Mês passado - receitas
INSERT INTO transactions (description, amount, date, type, notes, category_id, user_id)
VALUES
    ('Salário', 5000.00, CURRENT_DATE - INTERVAL '60 days', 'INCOME', 'Pagamento mensal', 
        (SELECT id FROM categories WHERE name = 'Salário' AND user_id = (SELECT id FROM users WHERE username = 'usuario')),
        (SELECT id FROM users WHERE username = 'usuario')),
    ('Freela', 800.00, CURRENT_DATE - INTERVAL '55 days', 'INCOME', 'Projeto extra', 
        (SELECT id FROM categories WHERE name = 'Outros' AND user_id = (SELECT id FROM users WHERE username = 'usuario')),
        (SELECT id FROM users WHERE username = 'usuario'))
ON CONFLICT DO NOTHING;

-- Inserção de metas financeiras
INSERT INTO financial_goals (name, description, target_amount, current_amount, start_date, end_date, achieved, user_id, category_id)
VALUES
    ('Viagem de férias', 'Economia para viagem no fim do ano', 5000.00, 2500.00, CURRENT_DATE - INTERVAL '60 days', CURRENT_DATE + INTERVAL '120 days', FALSE,
        (SELECT id FROM users WHERE username = 'usuario'),
        (SELECT id FROM categories WHERE name = 'Entretenimento' AND user_id = (SELECT id FROM users WHERE username = 'usuario'))),
    ('Fundo de emergência', 'Reserva para emergências', 10000.00, 8000.00, CURRENT_DATE - INTERVAL '120 days', CURRENT_DATE + INTERVAL '60 days', FALSE,
        (SELECT id FROM users WHERE username = 'usuario'),
        (SELECT id FROM categories WHERE name = 'Outros' AND user_id = (SELECT id FROM users WHERE username = 'usuario'))),
    ('Curso de especialização', 'Pós-graduação', 3000.00, 3000.00, CURRENT_DATE - INTERVAL '180 days', CURRENT_DATE - INTERVAL '30 days', TRUE,
        (SELECT id FROM users WHERE username = 'usuario'),
        (SELECT id FROM categories WHERE name = 'Educação' AND user_id = (SELECT id FROM users WHERE username = 'usuario')))
ON CONFLICT DO NOTHING;
