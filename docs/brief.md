# Assignment brief (as received)

**E-commerce Order Management**

Time limit: 48 hours from assignment. Stack: Spring Boot.

## The Project

An e-commerce order management system at scale with a multi-category product catalog, customer cart and
checkout, inventory tracked across multiple warehouses, payment processing, and an order fulfillment
lifecycle (placed → confirmed → packed → shipped → delivered → returned). Inventory must be reserved
correctly under concurrent purchases so the same unit cannot be oversold across warehouses. Order placement
should atomically reflect cart, inventory, and payment state, and the downstream pipeline (fulfillment
routing, customer notifications, audit logging) should run without blocking the customer's checkout
response. Discounts, taxes, returns, and refunds should be supported.

Roles: admin (manage catalog, warehouses, inventory, and discounts), customer (browse, cart, checkout,
return, and track orders), and warehouse staff (update fulfillment status).

## Instructions

### Framing

The Product Requirement above is intentionally open-ended. You own the scoping decisions — which entities
to model, which APIs to expose, which edge cases to handle, and what to leave out. Your interpretation of
the requirement and the features you choose to build are themselves part of what is being evaluated. You
are encouraged to interpret the requirement generously and submit a feature-rich solution — both the
breadth and the depth of the features you build contribute to the evaluation. Document every meaningful
assumption in the README.md and explain your reasoning in the recorded video.

### In Scope

- REST APIs covering the core flows
- Persistence to a database of your choice
- Basic role-based access control for the roles defined in the requirement
- Input validation and error handling
- Unit & Integration Tests for the core flows

### Out of Scope

- UI or frontend
- Deployment, containerization, or CI/CD
- Distributed systems or microservices
- Advanced authentication (OAuth, SSO, MFA)
- Production-grade observability, monitoring, or alerting

## What to Submit

- GitHub repository (mandatory)
  - Your personal GitHub project repository link
  - Multiple commits are expected during the development phase
  - Must include a README.md
  - Must include the Agents.md / Claude.md file used during development
  - Must include the skills used during development
  - Must include all raw files used during development
- Video recording (maximum 10 minutes, Loom) in which you explain:
  - How you approached the problem and the solution at a high level
  - The tech stack used and the reasoning behind it
  - The AI workflow used
  - The testing approach
