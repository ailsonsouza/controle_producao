# CIA MNT - Sistema de Gestão de Produção e Manutenção

O **CIA MNT** é uma solução backend desenvolvida para otimizar o fluxo de ordens de serviço e manutenção industrial. O projeto nasceu do desafio de criar uma arquitetura robusta e autoral, saindo da zona de conforto de tutoriais prontos para aplicar padrões de mercado em um cenário de negócio real.

## O Desafio do Projeto
Este projeto representa minha transição para o desenvolvimento de aplicações independentes. A arquitetura está sendo pensada para ser escalável, utilizando o ecossistema moderno do Java para garantir que a lógica de negócio seja o coração da aplicação.

## Tecnologias Utilizadas
* **Java 21** (Uso de Records para imutabilidade)
* **Spring Boot 3**
* **Spring Data JPA** (Hibernate)
* **H2 Database** (Ambiente de desenvolvimento)
* **Jakarta Validation** (Bean Validation para integridade dos dados)
* **Maven** (Gerenciamento de dependências)

## Arquitetura e Boas Práticas
* **Camada de DTOs:** Uso de **Java Records** para transferir dados de forma segura, evitando a exposição das entidades de banco de dados e garantindo a imutabilidade.
* **Service Layer:** Implementação de serviços com injeção de dependência via construtor, facilitando a testabilidade e o desacoplamento.
* **Tratamento de Exceções:** Criação de exceções personalizadas para retornar respostas HTTP claras e padronizadas.
* **Mapeamento de Domínio:** Uso estratégico de relacionamentos JPA para refletir a hierarquia da empresa.
* **Transacionalidade:** Garantia de integridade das operações com a anotação `@Transactional`.

## Status do Desenvolvimento
O projeto está em fase ativa de evolução. Atualmente, a base organizacional do sistema está concluída:

- [x] **Módulo de Departamentos:** Estrutura completa de CRUD para gestão dos setores da empresa.
- [x] **Módulo de Categorias:** Classificação de tipos de manutenção com validações rigorosas.
- [ ] **Módulo de Ordens de Serviço (Em desenvolvimento):** Implementação de lógica de itens, subtotais e cálculos com `BigDecimal`.
- [ ] **Módulo de Usuários e Técnicos:** Gestão de permissões e atribuição de serviços.

## Como Rodar o Projeto
1. Clone este repositório:
   ```bash
   git clone [https://github.com/ailson-pereira/cia-manutencao.git](https://github.com/ailson-pereira/cia-manutencao.git)