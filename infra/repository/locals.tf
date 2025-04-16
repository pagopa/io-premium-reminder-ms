locals {
  project = "io-p-itn-msgs"

  repo_secrets = {
    "ARM_TENANT_ID"       = data.azurerm_client_config.current.tenant_id,
    "ARM_SUBSCRIPTION_ID" = data.azurerm_subscription.current.subscription_id
  }

  infra_ci = {
    secrets = {
      "ARM_CLIENT_ID" = data.azurerm_user_assigned_identity.prod_ci.client_id
    }
  }

  infra_cd = {
    secrets = {
      "ARM_CLIENT_ID" = data.azurerm_user_assigned_identity.prod_cd.client_id
    }
  }

  jira_boards_ids = ["CES", "IO-COM"]

  tags = {
    CreatedBy      = "Terraform"
    Environment    = "Prod"
    BusinessUnit   = "App IO"
    ManagementTeam = "IO Comunicazione"
    Source         = "https://github.com/pagopa/io-premium-reminder-ms/blob/main/infra/repository"
    CostCenter     = "TS000 - Tecnologia e Servizi"
  }
}
