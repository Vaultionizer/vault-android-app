Feature: Add-New-Vault
  Scenario Outline: User wants to create a new vault
    Given a user has logged in successfully
    And the user has pressed the "Add new vault"-button in the menu
    When the user has clicked on the name field
    And has entered a valid name <vaultname>
    And has chosen if the vault should be accessible for others
    And has chosen what kind of encryption should be used
    And pressed the "Generate Keys"-button
    And has pressed the
    Examples: