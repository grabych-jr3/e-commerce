# E-Commerce API
REST API for managing products, cart and payments(Stripe integration)

Roadmap challange - https://roadmap.sh/projects/ecommerce-api

## ⚒️ Techologies
1. Java 25
2. Spring Boot
3. Spring Security + JWT
4. Spring Data JPA
5. PostgreSQL
6. Stripe API

## :eight_spoked_asterisk: Features
+ User registration and authentication (JWT)
+ Product management (CRUD)
+ Shopping cart system
+ Order creation
+ Stripe payment integration
+ Webhook processing for payment status

## :unlock: Endpoints
### Auth
+ POST /api/v1/auth/register - register a user
+ POST /api/v1/auth/login - sign in user

### Product
+ GET /api/v1/products - get all products
+ GET /api/v1/products/search - get all products by name using request param **name**
+ GET /api/v1/products/{id} - get product by id

ADMIN METHODS:
+ POST /api/v1/products - create new product
+ PATCH /api/v1/products/{id} - update product by id
+ DELETE /api/v1/products/{id} - delete product by id

### Cart
+ GET /api/v1/cart - get cart by customer id
+ POST /api/v1/cart/add - add CartItem to the cart
+ PATCH /api/v1/cart/change-quantity - change quantity of specified CartItem
+ DELETE /api/v1/cart/delete-item - remove CartItem from the cart
+ DELETE /api/v1/cart/clear-cart - remove all CartItems from the cart

### Orders
+ GET /api/v1/orders - get all orders by customer id
+ GET /api/v1/orders/{id} - get order by id
+ POST /api/v1/orders/create-order - create new order

### Payment
+ POST /api/v1/payments/checkout - create checkout session with Stripe
+ POST /api/v1/payments/webhook - webhook for handling paymnet status

## :heavy_exclamation_mark: How to use
Clone the repo:
``` bash
git clone https://github.com/grabych-jr3/e-commerce.git
```
Configure the DB:
``` java
spring.datasource.driver-class-name=
spring.datasource.url=
spring.datasource.username=
spring.datasource.password=
```
Get stripe secret from your Stripe account:
``` java
stripe.secret=
```
Generate stripe webhook signing secret:
``` bash
1. stripe listen --forward-to localhost:8080/api/v1/payments/webhook
2. copy secret code
3. stripe.webhook.signing.secret=
```
Run the project:
``` bash
mvn spring-boot:run
```
