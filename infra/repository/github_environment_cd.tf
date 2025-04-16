resource "github_repository_environment" "prod_cd" {
  environment = "prod-cd"
  repository  = github_repository.this.name

  deployment_branch_policy {
    protected_branches     = false
    custom_branch_policies = true
  }
}

resource "github_actions_environment_secret" "prod_cd" {
  for_each = local.infra_cd.secrets

  repository      = github_repository.this.name
  environment     = github_repository_environment.prod_cd.environment
  secret_name     = each.key
  plaintext_value = each.value
}
