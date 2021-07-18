Deliverable for Sprint 3. Three sprints left to go before the completion of this project.

During this full year university project course I worked within a team of eight people to develop a web application to prevent kiwi’s from throwing away one third of our food. By providing food companies with an e-commerce platform to sell food products close to expiring to cost conscious individuals, our team hopes to address this issue. The project ran within an scrum and agile processing framework, where we closely communicated with the product owner to develop the application he had envisioned. The project consisted of six sprints. During each sprint we would initially plan out what stories we would take on, split them up into tasks in substantial detail and log the time and completion on Jira. Additionally, to keep everyone on the same page we had two standups a week with our scrum master. Furthermore, we used a range of strategies on our workflow to improve code quality and minimise risk. This Included the use of code reviews before finishing each task, the use of task branching to prevent merge conflicts, substantial automated unit and integration testing with Junit, and automated acceptance testing with cucumber. Moreover, to keep our team members accountable for their mistakes we created a wiki that includes strict code styles, decision making policies, definition of done, yellow card policy, git policy, user manuals, and our testing procedures. Our technology stack used a client-server pattern for our web application, where VueJS was used on the frontend, spring boot for the backend, RESTful APIs to connect the two, MarinaDB for storing data externally, and gradle, sonarqube, npm, git, and a CI/CD pipeline to improve code quality and for seamless collaboration within the team. I took on a leadership role within the team by helping teammates solve complex problems, completing admin tasks such as setting up the CI/CD pipeline and cucumber, and providing a bridge between our team and the product owner and scrum team. This project is still ongoing and will be finished in October.

# LEFT_OVERS
## Team Internal Error (500)

An application for buying and selling products which are close to their expiration date.

## Deployed application

The application is running in the staging environment at https://csse-s302g5.canterbury.ac.nz/prod.

You can create and account or login to the example account with the following credentials:
Username: example@seng302.com
Password: password123

You can also find the DGAA credentials on the git repository https://eng-git.canterbury.ac.nz/seng302-2021/team-500 by navigating to Settings -> CI/CD -> Variables. You must have maintainer access to the repository to do this.

## Basic Project Structure

A frontend sub-project (web GUI):

- `frontend/src` Frontend source code
- `frontend/src/api` Endpoints for backend and external apis (Typescript)
- `frontend/src/components` Vue components of application UI (Vue.js)
- `frontend/src/plugins` State control and navigation (Typescript)
- `frontend/tests/unit` Jest tests (Typescript)
- `frontend/public` Publicly accessible web assets (e.g., icons, images, style sheets)
- `frontend/dist` Frontend production build

A backend sub-project (business logic and persistence server):

- `backend/src` Backend source code (Java - Spring)
- `backend/src/main/controllers` Controllers for API endpoints
- `backend/src/main/entities` The data model classes
- `backend/src/main/persistence` Classes for interfacing with the database
- `backend/src/main/service` Implementation of Spring services
- `backend/src/main/tools` Perform operations such as processing and validation
- `backend/src/test` JUnit and Cucumber tests for the Java application
- `backend/out` Backend production build

## How to run

### Frontend / GUI

    $ cd frontend
    $ npm install
    $ npm run serve

Running on: http://localhost:9500/ by default

### Backend / server

    cd backend
    ./gradlew bootRun

Running on: http://localhost:9499/ by default

## Contributors

- SENG302 Team 500
    - Connor Hitchcock
    - Edward Wong
    - Ella Johnson
    - Henry Barrett
    - Josh Egan
    - Nathan Smithies
    - Sheng-He Phua
