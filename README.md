My final project for a senior year Software Design Course.

For this project, we were introduced to GraphQL in order to build out a vanilla Java API that uses the default movie database.

I worked on all parts of the project, from the initial API design document to the finished API that returns movie/actor data from the GraphQL (Neo4J) database.

Below you can see how I approached designing the API according to the project requirements, and what this project was all about..

Summary Description of Project
This project, we believe, uses the law of “six degrees of
separation” to calculate a “bacon number”, which will show the
relationship(s) that actor Kevin Bacon has to other actors through
his movies. This phenomenon is when two people, say John and Alice,
can be shown to have a relationship between each other through
mutual relationships with other people. For example, John is friends
with Bob, Bob with Jill, Jill with Rachel, Rachel with Sam, and Sam
with Alice. Each person in this example can be represented by a node
in a graph, and Alice has a “John number” of 4, because they are
connected by 4 intermediate nodes, or people, in the graph. The
original law states that “any two people can be shown to have a
relationship in six degrees or less”.

To demonstrate the “six degrees of Bacon”, we will be using
Java (with maven) to build and test a REST API, Neo4j graph database
to store relationships, and a client-server application model that
will allow for creating, reading, updating, and deleting (CRUD) data
in the database. We are using Kevin Bacon, a Hollywood actor, as an
example to see how he relates to other actors (Bacon number) through
movies. In addition to the endpoints given in the project handout,
we will develop three new features to extend the functionality of
our app, and those are described below in our submission.

<img width="551" alt="Screenshot 2025-06-23 at 4 49 49 PM" src="https://github.com/user-attachments/assets/181fc621-80d2-425a-85a9-0e43a8742084" />

<img width="642" alt="Screenshot 2025-06-23 at 4 50 00 PM" src="https://github.com/user-attachments/assets/5763acf0-3638-4bf7-9119-e12f278afbe8" />

<img width="561" alt="Screenshot 2025-06-23 at 4 50 05 PM" src="https://github.com/user-attachments/assets/4ff690be-7a83-406e-9135-274840ed8db4" />

<img width="595" alt="Screenshot 2025-06-23 at 4 50 12 PM" src="https://github.com/user-attachments/assets/e8f1b56d-3ddd-4dba-aed5-da9d135eceda" />

<img width="566" alt="Screenshot 2025-06-23 at 4 50 37 PM" src="https://github.com/user-attachments/assets/1f25dc8f-611d-4c98-ace9-fdb2a0d4b084" />







