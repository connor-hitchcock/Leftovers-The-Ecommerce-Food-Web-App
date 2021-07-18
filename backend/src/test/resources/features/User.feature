Feature: User

    Scenario: Register a user account
        Given the user with the email "connor@gmail.com" does not exist
        When a user is getting created the first name is "Fergus"
        And their middle name is "Connor"
        And their last name is "Hitchcock"
        And their nickname is "Con"
        And their email is "connor@gmail.com"
        And their password is "gottaGetGoodGrad3$"
        And their bio is "Your average fourth year software engineer"
        And their date of birth is "1999-07-17"
        And their phone number is "0271234567"
        And their address is "69,Riccarton Road,Ashburton,Christchurch,New Zealand,Canterbury,8041"
        And the user is created
        Then a user with the email "connor@gmail.com" exists
        Then the user "connor@gmail.com" was created now
        Then the user has the first name "Fergus"
        Then the user has the middle name "Connor"
        Then the user has the last name "Hitchcock"
        Then the user has the nickname "Con"
        Then the user has the email "connor@gmail.com"
        Then the user has the password "gottaGetGoodGrad3$"
        Then the user has the bio "Your average fourth year software engineer"
        Then the user has the date of birth "1999-07-17"
        Then the user has the phone number "0271234567"
        Then the user has the address "69,Riccarton Road,Ashburton,Christchurch,New Zealand,Canterbury,8041"

    Scenario: Register a second user account
        Given the user with the email "roseyrose@gmail.com" does not exist
        When a user is getting created the first name is "Rosey"
        And their middle name is "Red"
        And their last name is "Rose"
        And their nickname is "Peach"
        And their email is "roseyrose@gmail.com"
        And their password is "prettyRoseSweetP3@ch"
        And their bio is "My friends say I am as sweet as a peach with the looks of a rose"
        And their date of birth is "1998-03-01"
        And their phone number is "0279876543"
        And their address is "100,Ocean View Crescent,Ashburton,Auckland,New Zealand,Rakino Island,1010"
        And the user is created
        Then a user with the email "roseyrose@gmail.com" exists
        Then the user has the first name "Rosey"
        Then the user has the middle name "Red"
        Then the user has the last name "Rose"
        Then the user has the nickname "Peach"
        Then the user has the email "roseyrose@gmail.com"
        Then the user has the password "prettyRoseSweetP3@ch"
        Then the user has the bio "My friends say I am as sweet as a peach with the looks of a rose"
        Then the user has the date of birth "1998-03-01"
        Then the user has the phone number "0279876543"
        Then the user has the address "100,Ocean View Crescent,Ashburton,Auckland,New Zealand,Rakino Island,1010"

    Scenario: Login a user account
        Given the user possesses the email "dan@thomp.com"
        And the user possesses the password "thompyboi9"
        And the user is created
        When the user logs in
        Then the user is logged in

    Scenario: Login a second user account
        Given the user possesses the email "james@hotmail.com"
        And the user possesses the password "jamesishotmail1#"
        And the user is created
        When the user logs in
        Then the user is logged in

    Scenario: Login but not registered
        Given the user with the email "james@hotmail.com" does not exist
        And the user possesses the email "james@hotmail.com"
        And the user possesses the password "jamesishotmail1#"
        When the user logs in badly
        Then the user is not logged in

    Scenario: Invalid register
        Given the user with the email "roseyrose@gmail.com" does not exist
        When a user is getting created the first name is "Rosey"
        And their middle name is "Red"
        And their last name is "Rose"
        And their nickname is "Peach"
        And their email is "InvalidEmailForm"
        And their password is "prettyRoseSweetP3@ch"
        And their bio is "My friends say I am as sweet as a peach with the looks of a rose"
        And their date of birth is "1998-03-01"
        And their phone number is "0279876543"
        And their address is "100,Ocean View Crescent,Ashburton,Auckland,New Zealand,Rakino Island,1010"
        And the user is created
        Then a user with the email "roseyrose@gmail.com" does not exist

    Scenario: Only required values filled
        Given the user with the email "roseyrose@gmail.com" does not exist
        When a user is getting created the first name is "Rosey"
        And their last name is "Rose"
        And their email is "roseyrose@gmail.com"
        And their password is "prettyRoseSweetP3@ch"
        And their date of birth is "1998-03-01"
        And their address is "100,Ocean View Crescent,Ashburton,Auckland,New Zealand,Rakino Island,1010"
        And no optional values are set
        And the user is created
        Then a user with the email "roseyrose@gmail.com" exists

    Scenario: Register with already existing email
        Given the user possesses the email "james@hotmail.com"
        And the user possesses the password "jamesishotmail1#"
        And their bio is "Hey don't replace me!"
        And the user is created
        When a user is getting created the first name is "Rosey"
        And their middle name is "Red"
        And their last name is "Rose"
        And their nickname is "Peach"
        And their email is "james@hotmail.com"
        And their password is "prettyRoseSweetP3@ch"
        And their bio is "Mwhahaha your spot in the database is mine!"
        And their date of birth is "1998-03-01"
        And their phone number is "0279876543"
        And their address is "100,Ocean View Crescent,Ashburton,Auckland,New Zealand,Rakino Island,1010"
        And the user is created
        Then only the first account with "james@hotmail.com" remains with bio "Hey don't replace me!"

    Scenario: The user is too young
        Given the user with the email "roseyrose@gmail.com" does not exist
        When a user is getting created the first name is "Rosey"
        And their last name is "Rose"
        And their email is "roseyrose@gmail.com"
        And their password is "prettyRoseSweetP3@ch"
        And their date of birth is "2020-03-01"
        And their address is "100,Ocean View Crescent,Ashburton,Auckland,New Zealand,Rakino Island,1010"
        Then a user with the email "roseyrose@gmail.com" does not exist