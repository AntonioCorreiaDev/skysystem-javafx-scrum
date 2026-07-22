# SkySystem

SkySystem is an intelligent flight search and destination discovery platform, designed to transform travel planning into a personalized and inspiring experience.

Instead of focusing solely on transactional tasks like showing flights, schedules, and prices, SkySystem seeks to help users discover destinations, receive recommendations tailored to their profile, and manage their trips more intelligently.

## Overview

The project was developed as part of the Software Project Management course.

SkySystem addresses a common flaw in current travel platforms: most systems are functional but not personalized. They help users search for flights, but do little to inspire destination discovery or adapt the experience to each traveler's profile.

The platform combines flight search, user preference collection, personalized suggestions, and travel management features into a single system.

## Problem

Current flight and travel systems are mostly transactional. They focus on schedules, logistics, and prices, neglecting the earliest and most emotional phase of travel planning: inspiration.

SkySystem was created to solve several problems:

*   Lack of personalization in existing booking platforms.
*   Limited support for destination discovery.
*   Underutilization of traveler data and preferences.
*   Poor user engagement and retention caused by generic experiences.

## Vision

To transform travel planning from a mechanical task into an inspiring and personalized journey.

SkySystem was developed to understand each user's travel personality and provide destination and flight suggestions that best match their interests, preferences, and travel history.

## Features

*   User registration and profile management.
*   Travel preference configuration.
*   Initial quiz for preference collection.
*   Personalized destination suggestions.
*   Real-time flight search.
*   Flight filtering and sorting.
*   Personalized discovery feed.
*   Dashboard with upcoming and past trips.
*   Flight status notifications.
*   Price drop notifications.

## Main Objectives

*   Improve the user experience in flight discovery and booking.
*   Provide smarter and more engaging travel recommendations.
*   Combine inspiration and booking on the same platform.
*   Increase user satisfaction and retention through personalization.

## Scope

### Included

*   Features oriented towards flight search and booking support.
*   Personalized recommendation flows.
*   Dashboard for travel and profile management.
*   Notification features related to travel activity.

## Architecture and Technology

SkySystem is built on a modular architecture, with a strong separation of concerns and support for quality validation through automated testing. It includes:

*   Frontend in JavaFX.
*   Backend in Java managed with Maven.
*   Database managed through DAOs with separate environments for production and testing.
*   Integration with Gemini LLM for personalized recommendations.
*   Integration with the Amadeus API for real-time flight data.

## Software Quality and Testing

The system's development adopted strong quality assurance practices, ensuring code stability and reliability through unit and integration tests across the various application layers:

*   **Business Logic and Data Tests:** Coverage of structural calculations (such as verifying round trips and number of stops) and safeguarding the system's state.
*   **Data Access (DAOs):** Real validation of writing and reading trips and users ensured in isolation, using a SQLite database dedicated to testing rebuilt with each test cycle.
*   **AI Recommendations Tests:** Validation of correct processing of the response JSON, error management, and prevention of API failures.
*   **Presentation and Interaction Layer (ViewModels):**
    *   Rigorous validation of the flight search logic, ensuring the correct operation of filters by budget, direct flights, and airline.
    *   Verification of the correct sorting of flights by price and form validation rules, such as preventing searches with return dates prior to departure or empty fields.
    *   Tests in the inspiration module to ensure the extraction, separation, and correct sorting of unique tags from the generated suggestions.
    *   Exhaustive testing of the onboarding quiz flow, validating the initial state, preference selection (e.g., "Beach & Sun"), and navigation (forward/backward) between the different steps.
*   **System Facade (DataFacade):**
    *   Guarantee of the correct implementation of the Singleton pattern for the main data instance.
    *   Validation of session management, ensuring that the current user is correctly registered, authenticated, and removed upon logout.
    *   Tests on custom tag manipulation and security validation in the onboarding process, ensuring it is only successfully completed when an authenticated user exists.

## Covered User Stories

The project includes user stories focused on:

*   Account creation and login.
*   Preference management.
*   Personalized recommendation flows.
*   Flight search and filtering.
*   Dashboard access.
*   Travel notifications and updates.

## Development Process

The project was planned and developed incrementally, with several sprints and release milestones.

Key highlights of the process include:

*   Definition of vision and scope.
*   Use cases and mockups.
*   Sprint and release planning.
*   Continuous development guided by unit testing and refactoring.
*   Delivery of the MVP with core features of intelligent discovery and flight search.
*   Final release with dashboard, notifications, and improved personalization.

## Team

*   António Correia
*   Pedro Amorim
*   Tiago Oliveira
*   Beatriz Marques
*   José Moreira
