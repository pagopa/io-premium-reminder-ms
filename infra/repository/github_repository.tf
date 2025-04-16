import {
  to = github_repository.this
  id = "io-premium-reminder-ms"
}

resource "github_repository" "this" {
  name        = "io-premium-reminder-ms"
  description = "Java application to send reminders to agencies with premium support."

  visibility = "public"

  allow_auto_merge            = true
  allow_rebase_merge          = false
  allow_merge_commit          = false
  allow_squash_merge          = true
  squash_merge_commit_title   = "PR_TITLE"
  squash_merge_commit_message = "PR_BODY"

  delete_branch_on_merge = true

  has_projects = true

  has_issues    = false
  has_downloads = true

  vulnerability_alerts = true

  template {
    include_all_branches = false
    owner = "pagopa-archive"
    repository = "template-java-microservice"
  }
}

resource "github_repository_autolink_reference" "jira" {
  for_each = toset(local.jira_boards_ids)

  key_prefix          = format("%s-", each.value)
  repository          = github_repository.this.name
  target_url_template = "https://pagopa.atlassian.net/browse/${each.value}-<num>"
}
