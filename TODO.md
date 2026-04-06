
## TODO create tests for:

- test create user
  - [x] valid registration
  - [x] invalid registration (email already exists)
  - [x] invalid registration (name is missing)
  - [x] invalid registration (email is missing)
  - [x] invalid registration (password is missing)
  - [x] invalid registration (type is missing)
  - [x] invalid registration (address is missing)
  - [x] all types of users (client, restaurant owner, driver)

- test login user
  - [x] invalid login (email is missing)
  - [x] valid login (email and password are correct)
  - [x] valid login (email and password are correct, but user doesn't exist)

- manage users
  - [x] update user
  - [x] get all users
  - [x] delete user
  - [x] get user by id
  - [x] get user by email
  - [x] update user with email that already exists
  - [x] update user with email that doesn't exist
  - [x] update a user with a name that already exists
  - [x] update a user with a name that doesn't exist
  - [x] update a user with a password that doesn't match
  - [x] update a user with a password that matches
  - [x] update a user with a type that doesn't exist
  - [x] update a user with a type that exists
  - [x] update a user with an address that doesn't exist
  - [x] update a user with an address that exists
  - [x] update a user with an address that is null
  - [x] update a user with an address that is not null
