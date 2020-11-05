package com.vaultionizer.vaultapp.test

import cucumber.api.CucumberOptions

@CucumberOptions(features = ["features/create_vault.feature"], glue = ["com.vaultionizer.vaultapp.cucumber.steps"])
@SuppressWarnings("unused")
class CucumberTestCase