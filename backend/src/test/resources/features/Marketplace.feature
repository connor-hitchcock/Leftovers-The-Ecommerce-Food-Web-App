Feature: UCM1 - Community Marketplace

  Background:
    Given a user exists
    And a card exists

  Scenario: AC3 - Cards in section Wanted populate in the wanted table
    Given the card has section "Wanted"
    And I am logged into my account
    When I request cards in the "Wanted" section
    Then I expect the card to be returned

  Scenario: AC3 - Cards in section For Sale populate in the For Sale table
    Given the card has section "ForSale"
    And I am logged into my account
    When I request cards in the "ForSale" section
    Then I expect the card to be returned

  Scenario: AC3 - Cards in section Exchange populate in the Exchange table
    Given the card has section "Exchange"
    And I am logged into my account
    When I request cards in the "Exchange" section
    Then I expect the card to be returned


