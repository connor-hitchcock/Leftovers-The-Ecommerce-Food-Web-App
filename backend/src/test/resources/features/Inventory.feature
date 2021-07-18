Feature: U19 - Create Inventory

  Background:
    Given a user exists
    And the business "Biz" exists
    And the business has the following products in its catalogue:
      | product_id | name  |
      | FISH       | fish  |
      | APPLE      | apple |
    And the business has the following items in its inventory:
      | product_id | quantity | expires    |
      | FISH       | 3        | 2021-12-04 |
      | FISH       | 17       | 2022-01-19 |
      | APPLE      | 1        | 2022-05-20 |

  Scenario: AC1 - When logged in as a business administrator I can see my inventory.
    Given I am an administrator of the business
    And I am logged into my account
    When I try to access the inventory of the business
    Then the inventory of the business is returned to me

  Scenario: AC1 - When logged in as a user who is not a business administrator I cannot see the business's inventory.
    Given I am an not an administrator of the business
    And I am logged into my account
    When I try to access the inventory of the business
    Then I cannot view the inventory

  Scenario: AC3 - Inventory items require quantity and expiry
    Given I am an administrator of the business
    And I am logged into my account
    When I create an inventory item with product code "APPLE" and quantity 3 and expiry "2022-05-20"
    Then I expect the inventory item to be created

  Scenario: AC3 - Inventory items cannot be created without quantity and expiry
    Given I am an administrator of the business
    And I am logged into my account
    When I create an inventory item with product code "APPLE" and no other fields
    Then I expect to be prevented from creating the inventory item

  Scenario: AC3 - Inventory items have additional fields
    Given I am an administrator of the business
    And I am logged into my account
    When I create an inventory item with product code "APPLE" and quantity 5, expiry "2022-05-20", price per item 10 and total price 50
    Then I expect the inventory item to be created

  Scenario: AC4 - Inventory items have additional dates
    Given I am an administrator of the business
    And I am logged into my account
    When I create an inventory item with product code "APPLE", quantity 3, expiry "2022-05-21", manufactured on "2020-03-20", sell by "2022-05-19" and best before "2022-05-20"
    Then I expect the inventory item to be created

