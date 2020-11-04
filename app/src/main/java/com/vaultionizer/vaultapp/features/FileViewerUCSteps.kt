package com.vaultionizer.vaultapp.features

import io.cucumber.api.java8.En

class FileViewerUCSteps : En {
    init {
        And("the file is appended onto the list of files that should the downloaded <OUT_2beDownloaded>") {
        print("hallo");
        }
        Given("a user was logged in successfully") {

        }
        And("the user has opened the FileViewer") {

        }
        And("a list with all selected files exists <IN_2beDownloaded>") {

        }
        And("the user's client has decrypted the given RefFile") {

        }
        When("the user clicks on a file") {

        }
        Then("the file gets selected <file>") {

        }
        And("at least one file is selected <IN_2beDownloaded>") {

        }
        When("a the user clicks on the file <file>") {

        }
        And("the file is selected") {

        }
        Then("the file is deselected") {

        }
        And("the file is removed from the <OUT_2beDownloaded>") {

        }
        When("the user clicks on the \"Download\"-button") {

        }
        And("at least one file is selected") {

        }
        Then("the client sends a download request with <2beDownloaded> to the server") {

        }
        And("no file is selected") {

        }
    }
}