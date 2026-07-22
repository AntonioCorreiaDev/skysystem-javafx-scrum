# SkySystem

O SkySystem é uma plataforma inteligente de pesquisa de voos e descoberta de destinos, concebida para transformar o planeamento de viagens numa experiência personalizada e inspiradora.

Em vez de se focar apenas em tarefas transacionais, como mostrar voos, horários e preços, o SkySystem procura ajudar os utilizadores a descobrir destinos, receber recomendações ajustadas ao seu perfil e gerir as suas viagens de forma mais inteligente.

## Visão Geral

O projeto foi desenvolvido no âmbito da unidade curricular de Gestão de Projetos de Software.

O SkySystem responde a uma falha comum nas plataformas de viagens atuais: a maioria dos sistemas é funcional, mas não personalizada. Ajudam os utilizadores a procurar voos, mas fazem pouco para inspirar a descoberta de destinos ou adaptar a experiência ao perfil de cada viajante.

A plataforma combina pesquisa de voos, recolha de preferências do utilizador, sugestões personalizadas e funcionalidades de gestão de viagens num único sistema.

## Problema

Os sistemas atuais de voos e viagens são maioritariamente transacionais. Focam-se em horários, logística e preços, negligenciando a fase mais inicial e emocional do planeamento de viagens: a inspiração.

O SkySystem foi criado para resolver vários problemas:

*   Falta de personalização nas plataformas de reservas existentes.
*   Suporte limitado à descoberta de destinos.
*   Subaproveitamento dos dados e preferências dos viajantes.
*   Fraco envolvimento e fidelização dos utilizadores causado por experiências genéricas.

## Visão

Transformar o planeamento de viagens de uma tarefa mecânica numa jornada inspiradora e personalizada.

O SkySystem foi desenvolvido para compreender a personalidade de viagem de cada utilizador e fornecer sugestões de destinos e voos que se ajustem melhor aos seus interesses, preferências e histórico de viagens.

## Funcionalidades

*   Registo de utilizadores e gestão de perfil.
*   Configuração de preferências de viagem.
*   Quiz inicial para recolha de preferências.
*   Sugestões personalizadas de destinos.
*   Pesquisa de voos em tempo real.
*   Filtragem e ordenação de voos.
*   Feed de descoberta personalizado.
*   Dashboard com viagens futuras e passadas.
*   Notificações sobre o estado dos voos.
*   Notificações de descida de preço.

## Objetivos Principais

*   Melhorar a experiência do utilizador na descoberta e reserva de voos.
*   Fornecer recomendações de viagem mais inteligentes e envolventes.
*   Combinar inspiração e reserva na mesma plataforma.
*   Aumentar a satisfação e fidelização dos utilizadores através da personalização.

## Âmbito

### Incluído

*   Funcionalidades orientadas para pesquisa e apoio à reserva de voos.
*   Fluxos de recomendação personalizada.
*   Dashboard para gestão de viagens e perfil.
*   Funcionalidades de notificação relacionadas com a atividade de viagem.

## Arquitetura e Tecnologia

O SkySystem assenta numa arquitetura modular, com forte separação de responsabilidades e suporte para validação de qualidade através de testes automatizados. Inclui:

*   Frontend em JavaFX.
*   Backend em Java gerido com Maven.
*   Base de Dados gerida através de DAOs com ambientes separados para produção e testes.
*   Integração com Gemini LLM para recomendações personalizadas.
*   Integração com a API Amadeus para dados de voos em tempo real.

## Qualidade e Testes de Software

O desenvolvimento do sistema adotou fortes práticas de garantia de qualidade, garantindo a estabilidade e a fiabilidade do código através de testes unitários e de integração nas várias camadas da aplicação:

*   **Testes de Lógica de Negócio e Dados:** Cobertura de cálculos estruturais (como verificação de viagens de ida/volta e número de paragens) e salvaguarda do estado do sistema.
*   **Acesso a Dados (DAOs):** Validação real de escrita e leitura de viagens e utilizadores assegurada de forma isolada, recorrendo a uma base de dados SQLite dedicada aos testes reconstruída a cada ciclo de teste.
*   **Testes de Recomendações com IA:** Validação do processamento correto do JSON de resposta, gestão de erros e prevenção de falhas da API.
*   **Camada de Apresentação e Interação (ViewModels):**
    *   Validação rigorosa da lógica de pesquisa de voos, assegurando o correto funcionamento dos filtros por orçamento, voos diretos e companhia aérea.
    *   Verificação da ordenação correta de voos por preço e das regras de validação de formulários, como a prevenção de pesquisas com datas de regresso anteriores à partida ou campos vazios.
    *   Testes no módulo de inspiração para garantir a extração, separação e ordenação correta de *tags* únicas a partir das sugestões geradas.
    *   Testes exaustivos ao fluxo do questionário de *onboarding*, validando o estado inicial, a seleção de preferências (ex: "Beach & Sun") e a navegação (avanço/recuo) entre os diferentes passos.
*   **Fachada do Sistema (DataFacade):**
    *   Garantia da correta implementação do padrão *Singleton* para a instância principal de dados.
    *   Validação da gestão de sessões, garantindo que o utilizador atual é corretamente registado, autenticado e removido no momento do *logout*.
    *   Testes à manipulação de *tags* personalizadas e validação de segurança no processo de *onboarding*, garantindo que este apenas é concluído com sucesso quando existe um utilizador autenticado.

## User Stories Abrangidas

O projeto inclui user stories focadas em:

*   Criação de conta e login.
*   Gestão de preferências.
*   Fluxos de recomendação personalizada.
*   Pesquisa e filtragem de voos.
*   Acesso ao dashboard.
*   Notificações e atualizações de viagem.

## Processo de Desenvolvimento

O projeto foi planeado e desenvolvido de forma incremental, com vários sprints e marcos de release.

Os principais destaques do processo incluem:

*   Definição da visão e do âmbito.
*   Casos de uso e mockups.
*   Planeamento de sprints e releases.
*   Desenvolvimento contínuo guiado por testes unitários e refatoração.
*   Entrega do MVP com funcionalidades centrais de descoberta inteligente e pesquisa de voos.
*   Release final com dashboard, notificações e personalização melhorada.

## Equipa

*   António Correia
*   Pedro Amorim
*   Tiago Oliveira
*   Beatriz Marques
*   José Moreira
