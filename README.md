# product-management-api
This project provides the backend API for managing products, users, and authentication. It is built using Java with Spring Boot and uses Spring Security for user authentication and role management. The application allows Admin users to add, update, delete, and manage product data.

### Key Features:
- User registration and login with role-based authentication (Admin/User).
- CRUD operations for managing products (Add, Update, Delete, View).
- Role-based access control (Admin can manage products, User can only view).
- Built with Spring Boot, Spring Security, and JPA (Hibernate).
- The system includes an inventory management feature, where products can be sold, reducing their stock accordingly.
- The application uses Spring Cache to cache frequently accessed data (e.g., product details) to improve performance.

## Run Locally
#### Follow the steps below to set up the project on your local machine:
##### Step 1: Clone the Repository
```bash
  git clone https://github.com/FayasAhmed-98/product-management-api

```
##### Step 2: Navigate to the Project Directory
```bash
  cd product-management-api
```
##### Step 3: Install Dependencies
The project uses Maven to manage dependencies. You can install the dependencies with the following command:
```bash
  mvn install
```
##### Step 4: Set up Application Properties
Update the application.properties file to configure your database connection and any other settings:
```bash
  # Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/product_db
spring.datasource.username=username
spring.datasource.password=password
spring.jpa.hibernate.ddl-auto=create
spring.jpa.show-sql=true

```

##### Step 5: Run the Application
Run the Spring Boot application with:
```bash
mvn spring-boot:run
```
##### The backend will start on http://localhost:8080

#### API Endpoints:
- POST /auth/register
  -Register a new user (Admin or User).
- POST /auth/login
  -User login to receive a JWT token.
- GET /api/products
  -Fetch all products (Admin/User).
- GET /api/products/{id}
  -Fetch a specific product by ID (Admin/User)
- POST /api/products
  -Add a new product (Admin only)
- PUT /api/products/{id}
  -Update an existing product (Admin only).
- DELETE /api/products/{id}
  -Delete a product (Admin only).
- POST /api/products/{id}/sell/{quantity}
  -Sell a product (Admin only).
Reduces the stock of the specified product by the quantity sold. If the stock is insufficient or the quantity is invalid, it returns a 400 Bad Request response.
  
