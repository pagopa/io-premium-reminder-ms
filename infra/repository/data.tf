data "azurerm_user_assigned_identity" "prod_ci" {
  name                = "${local.project}-infra-github-ci-id-01"
  resource_group_name = "${local.project}-rg-01"
}

data "azurerm_user_assigned_identity" "prod_cd" {
  name                = "${local.project}-infra-github-cd-id-01"
  resource_group_name = "${local.project}-rg-01"
}

data "github_repository" "this" {
  full_name = "pagopa/dx"
}
