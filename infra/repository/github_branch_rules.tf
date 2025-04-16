resource "github_branch_default" "master" {
  repository = github_repository.this.name
  branch     = "master"
}
