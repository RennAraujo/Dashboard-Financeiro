# Dashboard Financeiro

Um sistema completo para gerenciamento de finanças pessoais, com controle de receitas, despesas, metas financeiras e relatórios.

## 📋 Funcionalidades

- **Controle de Transações**: Registro e categorização de receitas e despesas
- **Resumo Financeiro**: Visão geral da situação financeira atual
- **Metas Financeiras**: Planejamento e acompanhamento de objetivos financeiros
- **Relatórios**: Geração de relatórios em PDF
- **Gráficos**: Visualização de dados em gráficos para análise
- **Conversão de Moedas**: Integração com API de câmbio (Open Exchange Rates)

## 🔧 Requisitos

- [Docker](https://www.docker.com/get-started)
- [Docker Compose](https://docs.docker.com/compose/install/)
- Chave de API do [Open Exchange Rates](https://openexchangerates.org/) (opcional, para função de conversão de moedas)

## 🚀 Instalação e Execução

### 1. Clone o repositório

```bash
git clone https://github.com/seu-usuario/dashboard-financeiro.git
cd dashboard-financeiro
```

### 2. Configuração da API de câmbio (opcional)

Para utilizar a funcionalidade de conversão de moedas, obtenha uma chave de API gratuita em [Open Exchange Rates](https://openexchangerates.org/signup/free) e defina a variável de ambiente:

```bash
# No Linux/Mac
export OPENEXCHANGERATES_APP_ID=sua_chave_api

# No Windows (PowerShell)
$env:OPENEXCHANGERATES_APP_ID="sua_chave_api"
```

### 3. Iniciar a aplicação com Docker

```bash
docker-compose up -d
```

Este comando irá:
- Criar e iniciar o container do PostgreSQL com os dados de exemplo
- Compilar e iniciar a aplicação Spring Boot
- Configurar a rede e volumes necessários

A aplicação estará disponível em: http://localhost:8080

### 4. Acesso à aplicação

**Credenciais de demonstração:**
- **Usuário comum**:
  - Login: `usuario`
  - Senha: `password`
  
- **Administrador**:
  - Login: `admin`
  - Senha: `password`

## 🔍 Utilização da API

### Autenticação

Para utilizar a API, é necessário obter um token JWT:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "usuario", "password": "password"}'
```

O token recebido deve ser incluído no cabeçalho das requisições:

```bash
curl -X GET http://localhost:8080/api/transactions \
  -H "Authorization: Bearer seu_token_jwt"
```

### Endpoints principais

#### Transações

- `GET /api/transactions` - Listar todas as transações do usuário
- `POST /api/transactions` - Criar nova transação
- `GET /api/transactions/{id}` - Obter transação por ID
- `PUT /api/transactions/{id}` - Atualizar transação
- `DELETE /api/transactions/{id}` - Remover transação
- `GET /api/transactions/filter` - Filtrar transações por categoria e período

#### Categorias

- `GET /api/categories` - Listar todas as categorias do usuário
- `POST /api/categories` - Criar nova categoria
- `GET /api/categories/{id}` - Obter categoria por ID
- `PUT /api/categories/{id}` - Atualizar categoria
- `DELETE /api/categories/{id}` - Remover categoria

#### Metas Financeiras

- `GET /api/goals` - Listar todas as metas do usuário
- `POST /api/goals` - Criar nova meta
- `GET /api/goals/{id}` - Obter meta por ID
- `PUT /api/goals/{id}` - Atualizar meta
- `PUT /api/goals/{id}/progress` - Atualizar progresso da meta
- `DELETE /api/goals/{id}` - Remover meta
- `GET /api/goals/achieved` - Listar metas alcançadas

#### Resumo Financeiro

- `GET /api/financial-summary` - Obter resumo financeiro do período informado
- `GET /api/financial-summary/monthly` - Obter resumo financeiro do mês atual
- `GET /api/financial-summary/annual` - Obter resumo financeiro do ano atual

#### Relatórios

- `GET /api/reports/monthly` - Gerar relatório do mês atual em PDF
- `GET /api/reports/annual` - Gerar relatório do ano atual em PDF
- `POST /api/reports/custom` - Gerar relatório personalizado por período

#### Conversão de Moedas

- `POST /api/currency/convert` - Converter valor entre moedas
- `GET /api/currency/convert-summary` - Obter resumo financeiro convertido para outra moeda
- `GET /api/currency/available` - Listar moedas disponíveis para conversão

### Exemplos de requisições

#### Criar nova transação:

```bash
curl -X POST http://localhost:8080/api/transactions \
  -H "Authorization: Bearer seu_token_jwt" \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Supermercado",
    "amount": 250.00,
    "date": "2023-03-15",
    "type": "EXPENSE",
    "notes": "Compras da semana",
    "categoryId": 1
  }'
```

#### Obter resumo financeiro mensal:

```bash
curl -X GET http://localhost:8080/api/financial-summary/monthly \
  -H "Authorization: Bearer seu_token_jwt"
```

## 🛠️ Desenvolvimento

### Estrutura do projeto

```
dashboard-financeiro/
├── src/
│   ├── main/java/com/dashboard/financeiro/
│   │   ├── config/         # Configurações do Spring e segurança
│   │   ├── controller/     # Controllers REST
│   │   ├── dto/            # Objetos de transferência de dados
│   │   ├── model/          # Entidades JPA
│   │   ├── repository/     # Repositórios JPA
│   │   ├── service/        # Lógica de negócios
│   │   └── util/           # Classes utilitárias
│   ├── resources/          # Recursos do Spring Boot
│   └── test/               # Testes unitários e integração
├── db/init/               # Scripts SQL para inicialização do banco
├── docker-compose.yml     # Configuração Docker Compose
└── Dockerfile             # Configuração Docker
```

### Parar e remover os containers

```bash
docker-compose down
```

Para remover também os volumes (apaga todos os dados):

```bash
docker-compose down -v
```

## 📊 Próximos passos

- Frontend em React ou Angular
- Notificações por email
- Integração com bancos via Open Banking
- Aplicativo mobile

## 📄 Licença

Este projeto está licenciado sob a licença MIT - veja o arquivo LICENSE para mais detalhes.
