Feature: Add-New-Vault
  Scenario Outline: User creates a vault
    Given a user has logged in successfully
    And the user has pressed the "Add new vault"-button in the menu
    And has chosen whether the vault should be shared <shared>
    When the user entered a valid name <vaultname>
    And has chosen the encryption type <encryption>
    And pressed the "Generate Keys"-button
    And presses the "create"-button
    Then a new vault is created on the server
    And an empty refFile is send to the server using the <encryKey>
    And the server is been told whether it is a shared vault
    And the <authKey> is send to the server

    Examples:
    | shared  | encryption  | authKey | encryKey  | vaultname |
    | yes     | AES126      | 87zjjn  | 987ujt6z  | testOne   |
    | no      | rot13       |         | poiuhn    | testTwo   |

    Scenario Outline: User generates Keys for a vault
      Given a user has logged in successfully
      And the user has pressed the "Add new vault"-button in the menu
      And has chosen whether the vault should be shared <shared>
      And has chosen the encryption type <encryption>
      When the user presses the "Generate Keys"-button
      Then a key for authentication <authKey> is generated in case the vault is shared
      And a encryption key <encryKey> according to the selected encryption type is generated
      And the user is been shown the wordified key(s) <wordified_authKey> <wordified_encryKey>

      Examples:
      | shared  | encryption  | authKey | encryKey  | wordified_authKey | wordified_encryKey  |
      | yes     | AES256      | 456zh   | 8zhbtt6z  | Huhn_Berg_laufen  | Ich_haben_Spaß      |
      | no      | rot13       |         | 7zghjmn   |                   | Leben_ist_Leiden    |