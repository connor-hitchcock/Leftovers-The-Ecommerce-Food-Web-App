Feature: U22 - List Sale

  Background:
    Given a user exists
    And the business "Biz" exists
    And the business has the following products in its catalogue:
      | product_id | name  |
      | FISH       | fish  |
      | APPLE      | apple |
    And the business has the following items in its inventory:
      | product_id | quantity | expires    |
      | FISH       | 17       | 2022-01-19 |
      | APPLE      | 1        | 2022-05-20 |

  Scenario: AC1 - When not logged in I cannot view sale items
    Given the business is listing the following items
      | product_id | price | quantity |
      | FISH       | 10    | 5        |
    When I look a the business sale listings
    Then I expect to be unauthorised

  Scenario: AC1 - When logged in I can view sale items
    Given the business is listing the following items
      | product_id | price | quantity |
      | FISH       | 10    | 5        |
      | FISH       | 100   | 2        |
      | APPLE      | 1     | 1        |
    And I am an not an administrator of the business
    And I am logged into my account
    When I look a the business sale listings
    Then I expect to be see the sales listings

  Scenario: AC2 - When logged in as a business administrator I can add a sale item
    Given I am an administrator of the business
    And I am logged into my account
    When I create a sale item for product code "FISH", quantity 10, price 100.0
    Then I expect the sale item to be created

  Scenario: AC2 - When not logged in as a business administrator I cannot add a sale item
    Given I am an not an administrator of the business
    And I am logged into my account
    When I create a sale item for product code "FISH", quantity 10, price 100.0
    Then I expect the sale item not to be created, due to being forbidden

  Scenario: AC2 - When logged in as a business administrator I can add a sale item with more info and a closing time
    Given I am an administrator of the business
    And I am logged into my account
    When I create a sale item for product code "FISH", quantity 10, price 100.0, more info "This is fish", closing "2022-01-10"
    Then I expect the sale item to be created

  Scenario: AC2 - When logged in as a business administrator I cannot add a sale item with invalid info
    Given I am an administrator of the business
    And I am logged into my account
    When I create a sale item for product code "FISH", quantity -10, price 100.0
    Then I expect the sale item not to be created, due to being a bad request