# OneCX Document-Management-Service

## Licence
This software is licensed under the [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0). You may obtain a copy of the license in the corresponding LICENSE file or visit the [Apache website](https://www.apache.org/licenses/LICENSE-2.0) for more information.

## Contributing
We welcome contributions from the community. If you would like to contribute to the development of OneCX Document Management Software, please follow our [contribution guidelines (tbd)]().

## What is Document Management
Document management refers to the systematic process of capturing, organizing, storing, and retrieving documents within an organization. It involves the efficient handling of digital documents, ensuring secure access, version control, collaboration based on TM-Forum standard [TMF 667](https://github.com/tmforum-apis/TMF667_Document).

With OneCX-Document-Management Software, you can streamline your document management workflows, reduce manual efforts, and enhance productivity. Whether it's managing contracts, invoices, legal documents, or any other form of digital content, OneCX Document Management provides a centralized and user-friendly solution for effectively managing your documents.

## Issue tracking
All OneCX Document Management issues are tracked and maintained here [issue tracking tool]().

## Overview
OneCX-Document-Management is a comprehensive solution for managing documents in a user-friendly and efficient manner. It is a solution that consists of three main components which are explained in more detail in the general documentation. Please refer to the following documentation to learn more [Document-Management Documenation](https://github.com/onecx-apps/onecx-document-management). However, in this document we are only referring to one of the three components, the OneCX-Document-Management-Service (Backend) of OneCX-Document-Management. 

This component provides the core functionality for document management. It handles tasks such as document storage, retrieval, metadata management, and access control. The backend is built cloud native using Quarkus.

## Getting Started
To start developing the OneCX Document Management Service, you need to set up your local development environment. It's recommended that you use WSL as the runtime for your application, as shown in the figure below. If you are using a different runtime, please check that you meet the requirements below.

### Prerequisites
Before you begin, ensure you have the following installed:

* Java Development Kit (JDK) version 17
* Maven build tool
* Git
* Docker + Docker Compose (Windows Subsystem for Linux (WSL) recommended)

### Clone the Repository
Start by cloning the required repositories to your local machine using the following command:

```bash
git clone https://github.com/onecx-apps/onecx-document-management-svc.git
git clone https://github.com/onecx-apps/document-management-dev.git
```
The repository ```onecx-document-management``` contains the source code of the document management backend.
The repository ```document-management-dev``` contains the necessary OneCX platform dependencies and the docker-compose script required to run the OneCX Document-Management-Service on your local machine.

### Build the Project
Navigate to the project directory and build the application using Maven:

```bash
cd onecx-document-management-svc
mvn clean install
```
Downloading the required Maven dependencies for the first time may take some time.

### Update local DNS resolution
Assuming you use WSL, Updating the local host file for local development allows you to map domain names to specific IP addresses, making it easier to test and debug applications using custom domain names instead of IP addresses. To achieve this, it is recommended that the entries in the Linux subsystem and Windows are aligned.

#### Update Windows host file
Open the file "C:\Windows\System32\drivers\etc\hosts" in your favorite editor and add the following entries:

```bash
127.0.0.1       pgadmin
127.0.0.1       postgresdb
127.0.0.1       keycloak-app
127.0.0.1       traefik
127.0.0.1       sonar
127.0.0.1       jaeger
127.0.0.1       tkit-portal
127.0.0.1       tkit-portal-app
127.0.0.1       data-mgmt-ui-app
127.0.0.1       data-mgmt-ui
127.0.0.1       tkit-portal-server
```
#### Update WSL host file
Open the file "\etc\hosts" in your favorite linux editor and add the same entries like above.

### Add Mageta Trusted Repository
In order to resolve and download the required docker images defined in the docker-compose file an additional docker registry must be defined.
```bash
docker login mtr.devops.telekom.de
username: [your MTR username]
password: [your MTR access token]
```

### Starting OneCX dependencies
In a local development environment, Docker Compose is used to define and manage multiple containers as a single application stack. It enables developers to easily start, stop, and configure all the necessary services and dependencies required by OneCX Document Management using a simple configuration file.

```bash
cd tkit-dev-env
docker-compose up -d traefik postgresdb pgadmin keycloak-app tkit-portal-server minio apm
```

* traefik:
Traefik is an ingress controller for Kubernetes deployments that enables dynamic traffic routing and load balancing based on defined rules and configurations.

* postgresdb:
PostgreSQL is an open-source relational database management system. It is used as persistence layer for storing and managing data in OneCX Document Management, providing reliability and scalability.

* pgadmin:
pgAdmin is an open-source administration and development platform that offers a user-friendly graphical interface for managing and interacting with the local PostgreSQL database.

* keycloak:
Keycloak is an open-source identity and access management system that simplifies authentication, authorization, and single sign-on for web and mobile applications.

* tkit-portal-server:
This micro-service is responsible mostly for handling the logic of portals and their meunitems and user data and settings.

* minio:
We use MinIO as a facade or abstraction layer to decouple our applications from the underlying cloud storage provider, providing greater flexibility and allowing us to seamlessly switch between different providers without changing our application code. It acts as a unified interface, enabling us to interact with various cloud storage systems using the standardized Amazon S3 API. 

* apm:
In this backend, user permissions are stored in a structured manner and an endpoint is provided to import permissions via CSV files. Each application can be assigned a set of roles and permissions, managed through an association table in the APM database. Roles are assigned in the Keycloak admin console and are retrieved from tokens, while strings defined in APM are used to grant access to specific components or views on the frontend.

### Stopping OneCX dependencies
The ```docker-compose stop``` command is used to stop the containers defined in a Docker Compose file. It gracefully stops the running containers by sending a stop signal, allowing them to perform any necessary cleanup tasks before shutting down. 

```
docker-compose stop
```
