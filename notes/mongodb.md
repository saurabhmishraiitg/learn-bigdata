# MongoDB

- [MongoDB](#mongodb)
  - [mongosh](#mongosh)
  - [Use Cases](#use-cases)

## mongosh

- Installation
  - `brew install mongosh`
- Connect to a mongoDB instance
  - `mongosh "mongosh "mongodb://mongodb0.example.com:28015"`
- Use database
  - `use glm-CA`
- Delete all documents from a collection
  - `use <db-name>`
  - `db.<collection>.deleteMany({})`
- Quit Shell
  - `exit`
- Show collections
  - `show collections`
- Show databases
  - `show databases`
- Find a document in a collection
  - `db.getCollection("3 test").find()`
  - `db.myCollection.find()`
  - Return all documents in a collection
    - `db.movies.find()`
  - Search document with equality condition
    - `db.movies.find( { "title": "Titanic" } )`
      - e.g. `db.licenses.find({licenseId: "1"})`
- Find documents with field missing/null
  - [reference](https://docs.mongodb.com/manual/tutorial/query-for-null-fields/)
  - Equality Check
    - `db.inventory.find( { item: null } )`
  - Type Check
    - `db.inventory.find( { item : { $type: 10 } } )`
  - Existence Check
    - `db.inventory.find( { item : { $exists: false } } )`
- Find query return select fields from the collection
  - [reference](https://docs.mongodb.com/manual/tutorial/project-fields-from-query-results/)
  - `db.inventory.find( { status: "A" }, { item: 1, status: 1 } )`
  - Multiple conditions
    - `db.tasks.find ({ "payment.paymentId" : {$exists : true }, "payment.glAccountNumber" : 123})`
- Aggregate queries on a collection
  - Distinct from a given collection
    - `db.collection.distinct()`
      - e.g. `db.licenses.distinct("createdBy")`
  - Count of documents in a collection
    - `db.collection.count()`
  - Estimated document count
    - `db.collection.estimatedDocumentCount()`
  - General aggregation pipeline

    ```mongosh
    db.orders.aggregate([
    { $match: { status: "A" } },
    { $group: { _id: "$cust_id", total: { $sum: "$amount" } } }
    ])
    ```

- Iterate over multiple records returned from find
  - [reference](https://docs.mongodb.com/manual/tutorial/iterate-a-cursor/)

    ```mongosh
    var myCursor = db.users.find( { type: 2 } );

    myCursor.next()
    ```

  - OR `myCursor.forEach(printjson);`
  - OR `while (myCursor.hasNext()) {printjson(myCursor.next());}`

- Clear screen
  - `cls`

## Use Cases

- Identify the request date for each of the tasks in given collection to see if they are in past
