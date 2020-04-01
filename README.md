# Task description
In Java, write a mini version of the translator from SQL to the MongoDB shell commands.

The input translator receives a string with an SQL query and returns a string with the MongoDB command.
Example `SELECT * FROM sales LIMIT 10 -> db.sales.find({}).limit(10)`

- It is enough to support only SELECT queries
- The list of columns in SELECT must be translated into the projection of the find command: 
`SELECT name, surname FROM collection -> db.collection.find({}, {name: 1, surname: 1})`
- SKIP/OFFSET and LIMIT are translated into the corresponding functions of MongoDB cursors:
`SELECT * FROM collection OFFSET 5 LIMIT 10 -> db.collection.find({}).skip(5).limit(10)`
- Predicates in WHERE turn into predicates of the find command:
`SELECT * FROM customers WHERE age > 22 -> db.customers.find({age: {$gt: 22}})`
- Implement only comparison operations: = <> <>. If there are several predicates, then they are connected by the AND operation

Do not forget to write tests.
