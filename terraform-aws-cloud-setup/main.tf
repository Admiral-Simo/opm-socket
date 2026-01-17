terraform {
  required_providers {
    aws = {
      source = "hashicorp/aws"
      version = "6.28.0"
    }
  }
}

provider "aws" {
    region = "eu-west-3"
}

resource "aws_s3_bucket" "public_bucket" {
    bucket = "opm-socket-chat-files"

}

resource "aws_s3_bucket_public_access_block" "public_bucket_access_block" {
  bucket = aws_s3_bucket.public_bucket.id

  block_public_acls       = false
  block_public_policy     = false
  ignore_public_acls      = false
  restrict_public_buckets = false
}

data "aws_iam_policy_document" "public_read_policy" {
  statement {
    principals {
      type        = "AWS"
      identifiers = ["*"]
    }

    actions = [
      "s3:GetObject",
    ]

    resources = [
      "${aws_s3_bucket.public_bucket.arn}/*",
    ]
  }
}

resource "aws_s3_bucket_policy" "public_bucket_policy" {
  bucket = aws_s3_bucket.public_bucket.id
  policy = data.aws_iam_policy_document.public_read_policy.json
}

resource "aws_cognito_user_pool" "main" {
  name = "opm-socket-user-pool"

  username_attributes      = ["email"]
  auto_verified_attributes = ["email"]

  password_policy {
    minimum_length    = 8
    require_lowercase = true
    require_numbers   = true
    require_symbols   = true
    require_uppercase = true
  }

  verification_message_template {
    default_email_option = "CONFIRM_WITH_CODE"
    email_subject        = "Account Confirmation"
    email_message        = "Your confirmation code is {####}"
  }
}

resource "aws_cognito_user_pool_client" "client" {
  name = "nextjs-chat-client"

  user_pool_id = aws_cognito_user_pool.main.id
  generate_secret = true

  supported_identity_providers = ["COGNITO"]

  allowed_oauth_flows_user_pool_client = true
  allowed_oauth_flows                  = ["code"]
  allowed_oauth_scopes                 = ["email", "openid", "profile"]

  callback_urls = [
    "http://localhost:3000/api/auth/callback/cognito",
    "http://127.0.0.1:3000/api/auth/callback/cognito"
  ]

  logout_urls   = ["http://localhost:3000", "http://127.0.0.1:3000"]

  default_redirect_uri = "http://localhost:3000/api/auth/callback/cognito"
}

resource "aws_cognito_user_pool_domain" "main" {
  domain       = "opm-socket-auth-unique-123"
  user_pool_id = aws_cognito_user_pool.main.id
}

output "cognito_user_pool_id" {
  value = aws_cognito_user_pool.main.id
}

output "cognito_client_id" {
  value = aws_cognito_user_pool_client.client.id
}

output "cognito_client_secret" {
  value     = aws_cognito_user_pool_client.client.client_secret
  sensitive = true
}

output "cognito_issuer" {
  value = "https://cognito-idp.eu-west-3.amazonaws.com/${aws_cognito_user_pool.main.id}"
}

output "cognito_domain" {
  value = "https://${aws_cognito_user_pool_domain.main.domain}.auth.eu-west-3.amazoncognito.com"
}
