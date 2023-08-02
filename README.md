# ISEL Projects

In this repository, I present some of the most important and complete projects developed during my Licentiate's Degree in Computer Science and Engineering at ISEL.

## [PC Exercises](PC-Exercises/)
My resolution of the exercises given by the professor for each section of the subjects learned in the "Concurrent Programming" course. Some of the subjects learned were:
* Data synchronization and locking
* Monitors (implicit and explicit) in the JVM and in the .NET platform
* Java Memory Model
* Synchronizers using lock-free techniques and lock-free data structures
* Asynchronous I/O and asynchronous programming models
* Thread pools and asynchronous programming models in the .NET platform
* Asynchronous methods in the C# language


## [PI Project](PI-Project/)
As an introduction to Web applications in the "Internet Programming" course, we developed a simple Web App with the objective of enabling users to search for and create their own lists of board games. To achieve this, we utilized an external API that provided all the necessary information about the games.

Throughout the course, I gained insights into the architecture of the World Wide Web, including how content is distributed across the web and how to set up an infrastructure with client and server components to support the desired functionalities. Additionally, I learned about the importance of modularization, separating code into different modules based on their responsibilities, and applying best practices in the programming model, among other valuable concepts.   

To accomplish the Web App objectives the technologies used were the following: `express` to handle requests, `node-fetch` to make requests, `jest` to make unit tests, `supertest` to make integration tests and `passport` to handle the authentication/state management. In the front-end was used `handlebars` to build the HTML pages with the fetched data. The project was deployed to the Heroku cloud platform and all the API documentation is described in the OpenAPI/Swagger file under the directory `docs/borga-api-spec.yaml`.


## [PDM Project](PDM-Project/)
In "Mobile Devices Programming," we developed a native application for Android devices using `Kotlin`. The application had the following objectives:
1. Fetch a puzzle of the day: The app allowed users to retrieve a new puzzle each day for users to solve.
2. Puzzle resolution: Users could attempt to solve the daily puzzle and receive feedback on their solutions.
3. Puzzles history: The app provided a view where users could access the history of previously solved puzzles.
4. Local chess game: Players could play a chess game on the same device, against the computer or with another local player.
5. Distributed chess game: The app enabled two players to play chess with each other, each using their distinct Android devices.


## [DAW Project](DAW-Project/)
In the "Web Application Development" course, we created a system to manage the states of a specific project, incorporating the following features:

* Project management: The system allowed users to manage different projects, including tracking their states and progress.
* Issues and comments: Within each project, users could create and manage issues, along with adding comments for effective collaboration.
* Authentication mechanism: A simple authentication system was implemented to ensure secure access and protect sensitive data.

To implement the system, we designed and implemented a Web API with the characteristics of `API evolvability`, making it easy to use for multiple client types. The API was thoroughly tested, including `unit and integration tests`, to ensure its reliability and functionality. `Comprehensive documentation` was created, describing all the resources implemented, their vocabulary, and other relevant aspects to facilitate understanding and usage.

For the backend development, we utilized the `Spring framework in Kotlin`, taking advantage of its robust features and flexibility. On the frontend side, we used `ReactJS` with `TypeScript`, enabling us to build a responsive and dynamic user interface.


## [CN Project](CN-Projects/)
In the "Cloud Computing" course, we undertook a project to design and develop a system for submissions and execution of distributed tasks. The main objective was to create a system capable of detecting multiple objects in submitted images and generating new annotated images with the identified object zones.

To accomplish these goals, we utilized the following Google Cloud Platform services:
* **Cloud Storage**: Google Cloud Storage was used to store the submitted images and manage their storage and retrieval efficiently.
* **Firestore**: Firestore, a NoSQL Database provided by GCP, was used to store all the relevant information about the request as well as the final results obtained. This allowed for scalable and real-time querying of the images.
* **Pub/Sub**: Google Cloud Pub/Sub was employed as the messaging service to handle communication between different components of the system, ensuring seamless integration and data flow.
* **Compute Engine**: Google Compute Engine was utilized to deploy virtual machines and handle the distributed task execution, which involved object detection in the images.
* **Cloud Functions**: Google Cloud Functions were used to implement serverless functions, enabling automatic lookup for the IPs of the main server and handling the deployment of virtual machines.
* **Vision API**: Google Cloud Vision API played a crucial role in object detection within the submitted images, providing accurate and efficient analysis.

A more detailed description of the project is present in the ['CN-Project/docs'](CN-Project/docs) directory.


## [PS Project/Final Project](https://github.com/Radnar9/QRreport)
For the final project of my Licentiate's Degree we developed **QRreport**, which aims to simplify the process of reporting anomalies within a building by implementing a system based on QR Codes. Users who frequent the building can easily report problems by scanning a QR Code placed near the anomaly. The designated managers for each building can then assign the reported anomalies to individuals with the necessary skills to address and resolve the issues.

The system has a robust backend and an intuitive frontend. In the **backend**, the following technologies were employed:

* `Spring Boot`: To facilitate the development of the backend application with Kotlin.
* `Spring Web MVC`: To build the web application using the Model-View-Controller design pattern.
* `Spring Security` with BCrypt: For secure user authentication and password storage.
* `Kotlin`: As a modern programming language, enhancing productivity and code readability.
* `PostgreSQL`: To store and manage data efficiently in a relational database.

The backend contains a well-structured database, whose model can be observed under the directory ['docs/database/model'](https://github.com/Radnar9/QRreport/blob/main/docs/database/model/er_model.png). For all requests, the database returns a JSON object with all the needed information, eliminating the need for multiple accesses to the database per request. Additionally, unit tests have been implemented for all database functionalities, i.e., functions, stored procedures and triggers. For accessing the PostgreSQL database, the server uses the [`Jdbi`](https://jdbi.org/) library, built on top of JDBC, which improves its low-level interface and provides a more natural API that is easy to bind to our domain data types. All transactions are declared with the appropriate isolation level.

The API is `hypermedia-driven`, and consequently, all responses have a defined structure, whether they are successful or unsuccessful (error responses). The structure of the responses can be consulted in the ['docs/api'](https://github.com/Radnar9/QRreport/tree/main/docs/api) directory. This way, the frontend doesn't need to contain business logic; its only responsibility is presenting the received data. Therefore, when changes are made to the business logic, only the backend needs to be updated, resulting in faster and simpler updates, especially if many different types of devices are being used, such as mobile phones, tablets, or even smart TVs (which are not very useful in the context of this project).

All the developed functionalities are **highly tested through both unit and integration tests**.

On the **frontend** side, the project utilized the following technologies:

* `ReactJS`: As a popular JavaScript library for building user interfaces, enabling a dynamic and responsive frontend.
* `TailwindCSS`: A utility-first CSS framework to style and design the user interface.
* `TypeScript`: To add static typing to JavaScript, enhancing code quality and maintainability.

The system also includes a robust authentication mechanism and different authorizations based on the role of the authenticated user. `JSON Web Tokens (JWTs)` are used in Cookies to maintain user information for making requests, ensuring a secure and smooth user experience.

One standout feature of the project is the [`highly detailed API documentation`](https://github.com/Radnar9/QRreport/blob/main/docs/api/README.md). All available resources, their parameters, relations, response representations, and vocabulary are thoroughly described. Additionally, the documentation covers all possible error representations, ensuring smooth integration with the API.

More details about the project functionalities can be read in this [report]().