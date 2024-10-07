# Social Sphere
### SocialSphere is a full-stack social networking application built using React for the frontend and Spring Boot for the backend. It offers a seamless user experience for creating accounts, connecting with friends, and sharing content through posts, comments, and likes.

## Key Features
### **1. User Authentication & Profiles**: Register, log in, and manage your personal profile.
### **2. Post Creation & Interaction**: Users can create posts, like and comment on posts, and engage with content shared by friends.
### **3. Friendship Management**: Find, add, and manage friends to build your personal network.
### **4. Search Functionality**: Easily search for friends and connect with people.
### **5. Real-Time Chat**: Communicate with friends through a real-time chat feature.
### **6. Real-Time Notifications**: Receive instant notifications for new likes, comments, and friend requests.


## Pages
## <p align="center">Login</p>
![login](https://github.com/user-attachments/assets/b9b29749-7f46-4f19-8d64-3d0443db833f)
## <p align="center">Sign up</p>
![sign_up](https://github.com/user-attachments/assets/828155c8-5758-478e-b163-f3d17f7ff034)
## <p align="center">Home</p>
![home](https://github.com/user-attachments/assets/5204db48-4fbb-426b-a0a6-a384b7b3bd11)
## <p align="center">Profile</p>
![profile](https://github.com/user-attachments/assets/fd66b848-18e8-40ff-a7b8-fbd462a10b3d)
## <p align="center">Chat</p>
![chat](https://github.com/user-attachments/assets/163596e0-da87-47e9-947b-8f56041a2866)
## <p align="center">Search Friends</p>
![friends](https://github.com/user-attachments/assets/fe69d2db-a829-456e-ab39-07eade77fa08)

## Tech Stack
[![My Skills](https://skillicons.dev/icons?i=java,spring,ts,react)](https://skillicons.dev) 
<a href="https://www.microsoft.com/en-us/sql-server" target="_blank" rel="noreferrer"> 
<img src="https://www.svgrepo.com/show/303229/microsoft-sql-server-logo.svg" alt="mssql" width="40" height="40"/> </a>
### Frontend
- **React.js v18**
- **TypeScript**
- **CSS**
### Backend
- **Java 17**
- **Spring Boot 3**
- **Spring MVC**
- **Spring Data JPA / Hibernate**
- **Spring Security**
- **JWT**
- **WebSocket**
- **Liquibase**
- **JUnit 5**
- **Mockito**
- **Maven**
### Database
- **Microsoft SQL Server 16**

## API DOC
<a href="http://localhost:8080/swagger-ui/index.html#/">Click here when API is running to see list of endpoints</a>

## DATABASE DIAGRAM
![diagram](https://github.com/user-attachments/assets/5cd4ab6e-f334-449c-b6aa-c21810d757af)

#

## Requirements
- **JDK 17**
- **Microsoft SQL Server 16**
- **node.js v16**
- **npm v7**

## TEST ACCOUNTS WITH EXAMPLE DATA
**Login&emsp;&emsp;Password**<br />
user&emsp;&emsp;&ensp;&nbsp;test<br />
user2&emsp;&emsp;&nbsp;test<br />
user3&emsp;&emsp;&nbsp;test<br />

## How to install API
1. Clone **SocialSphere-Server**
2. Configure the SQL database:<br />
\- Open SQL Server Management Studio<br />
\- Execute the following commands to create new database and user 
```` 
CREATE DATABASE SocialSphereDev;
CREATE LOGIN socialsphereuser WITH PASSWORD = 'pass';
GO
USE SocialSphereDev;
CREATE USER socialsphereuser FOR LOGIN socialsphereuser;
GO
USE SocialSphereDev;
EXEC sp_addrolemember 'db_owner', 'socialsphereuser';
GO
````
4. Ensure that your firewall allows traffic on port **1433**
5. Go to command line and navigate to project directory
6. Prompt **./mvnw spring-boot:run -Dspring-boot.run.profiles=dev**

## How to install Client
1. Clone **SocialSphere-Client**
2. In command line navigate to project directory
3. Prompt **npm install** then **npm run dev**