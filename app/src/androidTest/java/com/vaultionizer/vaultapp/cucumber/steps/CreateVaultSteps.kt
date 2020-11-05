package com.vaultionizer.vaultapp.cucumber.steps

import cucumber.api.PendingException
import cucumber.api.java.en.And
import cucumber.api.java.en.Given
import cucumber.api.java.en.Then
import cucumber.api.java.en.When

class CreateVaultSteps {


    @Given("^a user has logged in successfully$")
    fun a_user_has_logged_in_successfully() {
    }

    @And("^the user has pressed the \"Add new vault\"-button in the menu$")
    fun the_user_has_pressed_the_add_new_vault_button_in_the_menu() {

    }

    @And("^has chosen whether the vault should be shared (.*)$")
    fun the_user_has_chosen_whether_the_vault_should_be_shared(shared: String) {

    }

    @When("^the user entered a valid name (.*)$")
    fun the_user_entered_a_valid_name(name: String) {

    }

    @And("^has chosen the encryption type (.*)$")
    fun the_user_has_chosen_the_encryption_type(encryptionType: String) {

    }

    @And("^pressed the \"Generate Keys\"-button$")
    fun the_user_pressed_the_generate_keys_button() {

    }

    @When("^the user presses the \"Generate Keys\"-button$")
    fun the_user_presses_the_generate_keys_button() {

    }

    @And("^presses the \"create\"-button$")
    fun the_user_pressed_the_create_button() {

    }

    @Then("^a new vault is created on the server$")
    fun a_new_vault_is_created_on_the_server() {

    }

    @And("^an empty refFile is send to the server using the (.*)$")
    fun an_empty_ref_file_is_send_to_the_server_using_the_encryption_key(encryptionKey: String) {

    }

    @And("^the server is been told whether it is a shared vault$")
    fun the_server_is_been_told_whether_is_is_a_shared_vault() {

    }

    @And("^the (.*) is send to the server$")
    fun the_auth_key_send_to_the_servers(authKey: String) {

    }

    @Then("^a key for authentication (.*) is generated in case the vault is shared$")
    fun a_key_for_authentication_is_generated_in_case_the_vault_is_shared(key: String) {

    }

    @And("^a encryption key (.*) according to the selected encryption type is generated$")
    fun a_encryption_key_according_to_the_selected_encryption_type_is_generated(encryptionKey: String) {

    }

    @And("^the user is been shown the wordified key\\(s\\) (.*)$")
    fun the_user_is_been_shown_the_wordified_keys(wordifiedKey: String) {

    }
}