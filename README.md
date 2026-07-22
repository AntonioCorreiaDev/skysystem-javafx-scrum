![Logo](image.png)

## Contents

- [1. Team](#1-team)
- [2. Vision and Scope](#2-vision-and-scope)
  - [2.1 Problem Statement](#21-problem-statement)
  - [2.2 Vision \& Scope of the Solution](#22-vision--scope-of-the-solution)
- [3. Requirements](#3-requirements)
  - [3.1 Use Case Diagram](#31-use-case-diagram)
  - [3.2 Mockups](#32-mockups)
  - [3.3 User Stories](#33-user-stories)
- [4. Definition of done](#4-definition-of-done)
- [5. Architecture and Design](#5-architecture-and-design)
  - [5.1 Domain Model](#51-domain-model)
  - [5.2 Sequence Diagram](#52-sequence-diagram)
- [6. Risk Plan](#6-risk-plan)
- [7. Pre-Game](#7-pre-game)
- [8. Release Plan](#8-release-plan)
  - [8.1 Release 1](#81-release-1)
  - [8.2 Release 2](#82-release-2)
- [9. Increments](#9-increments)
  - [9.1 Sprint 1](#91-sprint-1)
  - [9.2 Sprint 2](#92-sprint-2)
  - [9.3 Sprint 3](#93-sprint-3)
  - [9.4 Sprint 4](#94-sprint-4)

## 1. Team

- José de Jesus Félix Moreira, 2022132718, a2022132718@isec.pt
- António Maria Nascimento Cruz Martinho Correia, 2022141330, a2022141330@isec.pt
- Beatriz Filipa Santos Marques, 2022137934, a2022137934@isec.pt
- Pedro Teixeira Amorim, 2022157609, a2022157609@isec.pt
- Tiago Santos Oliveira, 2023129464, ograndetiago@gmail.com

***

## 2. Vision and Scope

### 2.1 Problem Statement

#### Project background

The aviation and tourism industry is increasingly competitive, with customer experience becoming a key factor in retaining travelers. Modern users expect convenience, personalization, and seamless digital interactions when planning their journeys.

However, existing flight reservation and control systems remain largely transactional. They focus on operational tasks—such as displaying flight schedules, availability, and prices—while overlooking the broader user experience. These systems fail to address the earlier stages of travel planning, such as inspiration and destination discovery, which are crucial for engagement.

A major issue is the underuse of available data. Although platforms collect extensive information about users’ travel history and preferences, this data is rarely used to enhance personalization or create adaptive interactions. As a result, most systems offer a generic experience that treats all users alike.

This lack of personalization contributes to weak customer loyalty. Travelers can easily switch between platforms since most offer similar, functional features without added value. Consequently, the current gap lies not in technological capability, but in how existing systems apply it to meet modern travelers’ expectations for smarter and more engaging digital experiences.

#### Stakeholders

  Airlines – Need an efficient system to optimize reservation management and enhance customer experience.

  Project Managers – Responsible for ensuring project delivery within deadlines and defined requirements.

  Technical Team (developers, analysts, designers) – Responsible for implementing features, maintaining the platform, and ensuring scalability.

#### Users

Users – Travelers who want to search and book flights easily while receiving personalized destination suggestions based on their preferences and travel history. They value convenience, efficiency, and a tailored experience throughout the booking process.

System administrators – Require tools for monitoring and managing platform performance.


### 2.2 Vision & Scope of the Solution

#### Vision statement

To transform travel planning from a chore into an inspiring, personalized journey. Our intelligent Flight Reservation System will not just book flights but will understand each user's travel personality. By proactively suggesting perfect destinations and tailored experiences based on their history and preferences, we aim to increase customer satisfaction, optimize the booking process, and become the indispensable, loyalty-driving tool for every traveler.

#### List of features

User Profile & Management: Secure user accounts to store travel history, preferences (e.g., beach, city, adventure), and payment methods.

Intelligent Flight Search & Booking: Core functionality for searching, comparing, and booking flights based on destination, date, and price.

Personalized Destination Discovery: A dedicated "Discover" feed that suggests destinations and trips based on user profile, past travel, and stated interests.

Smart Travel Dashboard: A central hub for users to view their upcoming trips, past travel history, and personalized alerts.

Proactive Notification System: Alerts for flight status (delays, gates), price drops for saved routes, and personalized destination promotions.

Administrative Dashboard: For internal users to monitor bookings, manage flight data, and view system analytics.

#### Features that will not be developed

Hotel & Accommodation Bookings: Focus is exclusively on flights for the initial release.

Ground Transportation: Rental cars, trains, or taxi integrations will not be included.

Virtual Assistant/Chatbot: AI-driven conversational booking is beyond the current scope.

Advanced Data Security Audits: While basic security is mandatory, extensive third-party security compliance audits are not a primary goal for the MVP.

#### Assumptions

Users have reliable internet access and a modern web browser.

Airlines and data providers will have accessible APIs for flight information.

Users are willing to provide basic preference data to get better recommendations.

#### Constraints

The system must be a web application.

The initial version (MVP) will be available only in English.

Development must adhere to the defined project timeline and academic scope.

***

## 3. Requirements

### 3.1 Use Case Diagram

![img_5.png](img_5.png)

***

### 3.2 Mockups

![dashboard1](dashboard1.png)
![dashboard2](dashboard2.png)
![options](options.png)
![quiz](quiz.png)
![ai](ai.png)
![pesquisa](pesquisa.png)
![preferences](Preferences.png)


### 3.3 User Stories

1: User Account & Profile Management:
Goal: To allow users to create an account and manage their personal data and preferences.

US1: As a new user, I want to register for an account with my email and a password so that I can have a personal profile.

US2: As a registered user, I want to set and update my travel preferences (e.g., budget, interests, climate) so that I can receive personalized destination recommendations.
 
US3: As a user, I want to see a dashboard with my upcoming and past trips so that I can easily access my travel itinerary and history.

2: Intelligent Discovery:
Goal: Deliver the core personalized recommendation engine.

US4: As a new user, I want to have a quick, interactive process to find out about my travel preferences (e.g., interests, budget, travel style) so that the system can immediately generate personalized destination recommendations perfect for me, and simplify my flight search process.

US5: As a user, I want to see a personalized "Discover" feed on my homepage, so that I can get inspired by destinations that match my profile.

US6: As a user without a specific destination, I want to enter my departure city and dates to receive personalized destination suggestions, so that I can explore options tailored to me.

3: Flight Search:
Goal: Provide essential flight search.

US7: As a user, I want to search for flights by entering departure/arrival cities, dates and price.

US8: As a user, I want to filter and sort the list of flights by price, airline, and duration so that I can easily compare and find the best option for me.

4: Trip Management & Notifications:
Goal: To help users manage their trips and stay informed.

US9: As a traveler, I want to be notified about flight status changes (e.g., delays, gate changes) for my upcoming trips so that I can plan accordingly.

***

## 4. Definition of done

It is a collection of criteria that must be completed for a User Story to be considered “done.”

1. All tasks done:
  - CI – built, tested (Junit), reviewed (SonarCloud)
  - Push to DEV - ready for review
2. Approved by team
  - Merge request to QA - ready for acceptance test
3. Accepted by the QA
4. Code merged to main


## 5. Architecture and Design

### 5.1 Domain Model

![img_6.png](img_6.png)

### 5.2 Sequence Diagram

![img_2.png](img_2.png)

***

## 6. Risk Plan

##### Threshhold of Success
The project is considered successful if:

- The user successfully completed the "Happy Path" flow: Register -> Answer the Quiz -> Receive Recommendation -> View Flight.​

- The "Inspire Me" feature will generate at least 3 destinations that match the preferences entered.​

- All critical User Stories (US1, US2, US4, US6, US7) are "Done" according to the Definition of Done.​

- The development team is satisfied with both the final product and their collaborative work process, as evidenced by a final team retrospective and satisfaction.

##### Risk List
- RSK01 – PxI: 5x4=20; AI Hallucinations. By prompting the AI to generate "out of the norm" recommendations regarding prices, the model may produce unrealistic data (hallucinations) that does not match actual daily budget expendure.

Probability: 5; Impact: 4

- RSK02 – PxI: 4x5=20; API Limit Rate. The reliance on external flight APIs (e.g., Amadeus) with strict rate limits may cause the application to block requests during intensive testing or the final demonstration, rendering the search feature unusable.

Probability: 4; Impact: 5

- RSK03 – PxI: 5x2=10; LLM Performance. The integration with the LLM (Gemini) may introduce high latency in server responses, causing long loading times for the "Inspire Me" feature and negatively impacting the user experience.

Probability: 5; Impact: 2

- RSK04 - PXI: 2x5=10; API Maintenance. Our flight offer retrieval service is susceptible to intermittent unavailability due to unscheduled maintenance periods by the third-party API provider (Amadeus).
During these interruptions, calls to the flight search endpoint fail, resulting in the inability to present real-time flight results to users.


##### Mitigation Actions (risk level>=20)
RSK01 – MS (Minimization Strategy);

- Robust Prompt Engineering: Refine the Gemini prompt to ensure that, even when "out of the ordinary," the output format (JSON) is strict and price values are clearly marked as estimates.​

RSK02 – MS (Minimization Strategy);
- The key used for the API is on the free version, having the option to buy the key for unlimited API usage.

RSK04 – MS (Minimization Strategy);
Risk04 - The Test Version (free version) of the API, casually have Maintenance, having the option to buy the key prevent this usage.


## 7. Pre-Game
### Sprint 0 Plan

Sprint Goal: Establish the project foundation, define the product vision, and prepare the development environment to enable value delivery starting from Sprint 1.
Dates: October 2nd - October 9th (1 week)

- Sprint 0 Backlog:
  - Task1 – Write Vision and Scope (V&S) Touch up;
  - Task2 – Make the use case diagrams;
  - Task3 – Think on the Architecture and Design, and make exemples;
  - Task4 – Make the Mockups;
  - Task5 – Create user stories;
  - Task6 – Preapare development enviroment;
  - Task7 – Define roles (officially);
  - Task8 – Plan Sprint one;

- Sprint 0 plan and KOM: (link to file with Sprint Plan and KOM)

***

## 8. Release Plan

### 8.1 Release 1

Strategic Objective:
Establish a functional foundation for the SkySystem application that delivers core value to early users while enabling real-world testing and feedback collection.

Key Deliverables: 
Real-time flight search, basic user interface, flight search results display, and the personalized destination suggestions using the preference-gathering mechanism.

Key Deliverables:
  - Real-time flight search integration with external API
  - Basic user interface with navigable layout
  - Flight search results display system
  - User preference gathering mechanism
  - Initial "Inspire Me" destination suggestions using LLM technology

Technical Scope:
  - User Stories: US2, US4, US6, US7, US8
  - Story Points Allocation: 1 XL + 1 L + 1 M + 2 S
  - Primary Focus: Preference gathering, core recommendation engine, API integration, basic flight search, and flight filtering.
  - Team capacity = 80 hours (4 Weeks * 5 Team Elements * 4 Hours/Week)

Release Metrics:
  - Target Date: November 20, 2025
  - Version: V1.0
  - Success Criteria: Stable flight search functionality, responsive basic interface, functional preference system.


***

### 8.2 Release 2

Strategic Objective:
Deliver a fully-featured, production-ready application with advanced functionality, enhanced user experience, and comprehensive trip management capabilities.

Key Deliverables: 
Comprehensive user trip dashboard, user registration and authentication, real-time flight status notifications, and the personalized "Discover" feed.

Key Deliverables:
  - Advanced search filters and sorting capabilities
  - Real-time flight status notifications system
  - Comprehensive user trip dashboard
  - User registration and authentication system
  - Travel history and profile management
  - Performance optimizations and security enhancements

Technical Scope:
  - User Stories: US1, US3, US9, US5
  - Story Points Allocation: 2 L + 2 M
  - Primary Focus: Advanced UI features, user authentication, trip management, notifications, and personalized discovery feed.
  - Team capacity = 80 hours (4 Weeks * 5 Team Elements * 4 Hours/Week)

Release Metrics:
  - Target Date: December 18, 2025
  - Version: V2.0
  - Success Criteria: All planned features implemented, performance benchmarks met, security protocols established.

***

## 9. Increments

### 9.1 Sprint 1
##### Sprint Plan

- Goal:  Deliver the complete user preference-gathering flow, build the foundational front-end UI components, and establish the backend connectivity by researching 3rd party APIs and implementing mock backends for the core MVP features.

- Dates: from 23/Oct to 06/Nov, 2 weeks

- Team capacity = 40 hours (2 Weeks * 5 Team Elements * 4 Hours/Week)

- Roles:
  - Product Owner: António Correia
  - Scrum Master: Tiago Oliveira
  - QA Engineer: Beatriz Marques

- To do:
  - Prepare Sprint 2 BL;
  - Prepare Sprint 1 Review with Client;
  - Start of UI
  - New User Onboarding Preference Quiz - Backend Development;
  - New User Onboarding Preference Quiz - Frontend Development;
  - US6: "Inspire Me" Destination Suggestions;
  - US7: Basic Flight Search - Backend Development;
  - US7: Basic Flight Search - Frontend Development;
  - US2: User Travel Preferences;
  
- Story Points: 1XL+2L+1M+2S

- Analysis: The team is confident about the sprint and the overal project.

- Sprint plan: (link to file with Sprint Plan)

##### Sprint Review

- Analysis:
  - Not Done (In Progress): US7 (Basic Flight Search) & US6 ("Inspire Me" Suggestions).
  - Added (to next Release):US5 & US8 were officially moved from Release 1 (MVP) to Release 2 to protect the MVP deadline.

- Story Points:
  - Planned: 1XL+1L+1M+1S
  - Completed: 1S (US2) + 1M (US4) + 1L (US7) partially.
  - Carried Over: 1L (US7) + 1XL (US6).

- Version: 0.1
  - Delivered Features: US2 (User Travel Preferences) & US4 (Onboarding Quiz).

- Client analysis: client feedback


- Conclusions:
  - Review: We must re-prioritize the backlog to place US7 and US6 at the top for the next sprint.
  - Add: The Release Plan is officially updated. US5 are now part of Release 2.

##### Sprint Retrospective

- What went well:
  - We successfully delivered two complete features (US2, US4) with both backend and frontend complete for US4.
  - Good teamwork on the Onboarding (US4, US7 and US2) features.
  - We communicated the sprint outcome clearly and proposed a realistic plan update.

- What could be improved:
  - Our estimation was too optimistic. We underestimated the true scope and complexity of the L (US7) and XL (US6) stories.
  - We committed to too many large stories in a single sprint.
  - The planned story sizes (e.g., "2L") did not match the actual story sizes (e.g., "1L + 1XL"), which shows a flaw in our planning/estimation.

- What will we commit to improve in the next sprint:
  - We will commit to breaking down all 'L' and 'XL' User Stories into smaller, testable stories that can be completed within a single sprint.
  - We will not pull any L/XL stories into a sprint without first breaking them down.
  - We will be more realistic, and less optimistic, about our "velocity" (how many points we can complete) in the next sprint planning.

- Sprint review and retrospective: (link to file with Sprint Review and Retrospective)

### 9.2 Sprint 2
##### Sprint Plan
- Goal: Guarantee a robust MVP launch with all planned features on the release plan.
- Dates: from 06/Nov to 20/Nov.
- Team capacity = 40 hours.

- Roles: 
- Product Owner: José Moreira
- Scrum Master: Beatriz Marques
- QA Engineer: Pedro Amorim

- To do:
- US6: "Inspire Me" Destination Suggestions (XL);
- US7: Basic Flight Search (Backend & Frontend) (L);
- US8: Filter and Sort Flight Results (L);
- Task 4: Add persistent Files/Data;
- Task 5: Create Home Page and Navigation;
- Task 6: Create Preferences Page;
- Task 7: Update MVP Definition and Final Release.

- Story Points: 1XL + 2L + 1S.
- Analysis: The team focused on recovering the delayed features from Sprint 1 and ensuring the MVP is ready for release.

##### Sprint Review
- Analysis:
- Completed all 3 planned User Stories (US6, US7, US8).
- Completed all 5 additional tasks that weren't present in the initial sprint backlog.

- Story Points:
- Planned: 1XL + 2L + 1S.
- Completed: 1XL + 2L + 1S.

- Delivered Features: Real-time flight search, "Inspire Me" suggestions, Filtering/Sorting, and basic persistence.

- Conclusions:

- Everything planned and more were completed this sprint. The gap between initial forecast (20h) and utilized capacity (40h) was filled by emergent requirements.

##### Sprint Retrospective
- What went well:
- We successfully delivered three complete User Stories, providing real value.
- The workflow continues to improve every sprint.

- What could be improved:
- Gap between initial forecast and utilized capacity due to a conservative sprint plan.

- What will we commit to improve in the next sprint:
- Preemptively create tasks for the next sprint to better deal with sprint forecast.

### 9.3 Sprint 3
#### Sprint Plan
- Goal: Implement the favorites Flights and Notification Core, alongside DB integration.
- Dates: from 20/Nov to 04/Dec.
- Team capacity = 40 hours.

- Roles: 
- Product Owner: Tiago Oliveira
- Scrum Master: José Moreira
- QA Engineer: António Correia

- To do:
- US5: Personalized "Discover" Feed (L);
- US9: Flight Status Notifications (M);
- Task: Create JUnit Tests;
- Task: UI Enhancements and Persistence.

- Story Points: 1L + 1M.
- Analysis: Focus shifted to "Release 2" features, specifically user engagement (Discover feed) and retention (Notifications).

#### Sprint Review
- Analysis:
  - Focus was on completing Release 2 features: User engagement via "Discover Feed" and Notification Core.
  - Successfully managed to create tasks ahead of time, hindering typical bottlenecks.

- Story Points:
  - Planned: 1L + 1M.
  - Completed: 1L + 1M.

- Metrics:
  - Planned:
    - User Stories: 2
    - Tasks: 6
    - Sprint Capacity: 40h (PO: 4h, SM: 2h, QA: 8h, Team: 26h)
  - Done:
    - User Stories: 2
    - Tasks: 7
    - Time Spent: 40h (PO: 2h, SM: 2h, QA: 9h, Team: 27h)

#### Sprint Retrospective
- What went well:
  - Pre-created tasks helped distribute work more evenly across the team.
  - Improved clarity in task scope made sprint planning smoother and faster.

- What caused problems:
  - No significant problems arose during the sprint.

- What will we commit to improve in the next sprint:
  - We will improve our focus on remaining high-priority items to guarantee a stable and timely release (António Correia).
  - We will minimize last-minute scope changes to keep the release on track (Pedro Amorim).

### 9.4 Sprint 4
#### Sprint Plan
- Goal: Complete all planned features for release 2, including a rigorous final product review to guarantee a robust delivery.
- Dates: from 04/Dec to 18/Dec.
- Team capacity = 40 hours.

- Roles:
  - Product Owner: Pedro Amorim
  - Scrum Master: António Correia
  - QA Engineer: Tiago Oliveira

- To do:
  - US9: Flight Status Notifications (M).
  - US10: Price Drop Notifications (M).
  - Tasks: 10 Planned Tasks.
  - Focus: Final Polish, Release 2 delivery.

- Story Points: 2M (Planned).

#### Sprint Review
- Analysis:
  - Completed all planned features for Release 2.0.
  - Successfully delivered the "Happy Path" flow.
  - Unit Tests: 62 tests with 100% pass rate.

- Story Points:
  - Planned: 2M.
  - Completed: 2M.

- Metrics:
  - User Stories Done: 2.
  - Tasks Done: 15 (vs 10 planned).
  - Time Spent: 40h (vs 40h capacity).
    - Team: 33h.
    - SM: 1h.
    - PO: 3h.
    - QA: 3h.

#### Sprint Retrospective
- What went well:
  - Successful Release Delivery: All critical features for Release 2.0 were deployed and function as expected.

- What caused problems:
  - Academic Bottlenecks: Heavy workload from concurrent courses reduced team availability slightly.

- What have we learned:
  - Structured Task Ownership: Importance of assigning specific owners to every task.
  - Realistic Goal Management: Value of setting clear, achievable milestones.

***
