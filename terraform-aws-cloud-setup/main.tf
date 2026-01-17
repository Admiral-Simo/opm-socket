terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "6.28.0"
    }
  }
}

provider "aws" {
  region = "eu-west-3"
}

# -------------------------------------------------------------------
# 1. S3 BUCKET
# -------------------------------------------------------------------

resource "aws_s3_bucket" "public_bucket" {
  bucket        = "opm-socket-chat-files"
  force_destroy = false
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
    actions = ["s3:GetObject"]
    resources = ["${aws_s3_bucket.public_bucket.arn}/*"]
  }
}

resource "aws_s3_bucket_policy" "public_bucket_policy" {
  bucket = aws_s3_bucket.public_bucket.id
  policy = data.aws_iam_policy_document.public_read_policy.json
}

# -------------------------------------------------------------------
# 2. COGNITO USER POOL
# -------------------------------------------------------------------

resource "aws_cognito_user_pool" "main" {
  name = "opm-socket-user-pool"

  username_attributes      = ["email"]
  auto_verified_attributes = ["email"]

  password_policy {
    minimum_length    = 8
  }

  verification_message_template {
    default_email_option = "CONFIRM_WITH_CODE"
    email_subject        = "Account Confirmation"
    email_message        = "Your confirmation code is {####}"
  }

  # --- REQUIRED: Setting this to TRUE makes it appear on the form ---
  schema {
    attribute_data_type = "String"
    name                = "name"
    required            = true
    mutable             = true

    string_attribute_constraints {
      min_length = 2
      max_length = 2048
    }
  }
}

# -------------------------------------------------------------------
# 3. COGNITO APP CLIENT
# -------------------------------------------------------------------

resource "aws_cognito_user_pool_client" "client" {
  name = "nextjs-chat-client"

  user_pool_id    = aws_cognito_user_pool.main.id
  generate_secret = true

  supported_identity_providers = ["COGNITO"]

  allowed_oauth_flows_user_pool_client = true
  allowed_oauth_flows                  = ["code"]
  allowed_oauth_scopes                 = ["email", "openid", "profile"]

  read_attributes  = ["email", "email_verified", "name", "profile", "updated_at"]
  write_attributes = ["email", "name", "profile"]

  callback_urls = [
    "http://localhost:3000/api/auth/callback/cognito",
    "http://127.0.0.1:3000/api/auth/callback/cognito"
  ]

  logout_urls = [
    "http://localhost:3000",
    "http://127.0.0.1:3000"
  ]

  default_redirect_uri = "http://localhost:3000/api/auth/callback/cognito"
}

# -------------------------------------------------------------------
# 4. HOSTED UI DOMAIN
# -------------------------------------------------------------------

resource "aws_cognito_user_pool_domain" "main" {
  domain       = "opm-socket-auth-unique-123"
  user_pool_id = aws_cognito_user_pool.main.id
}

# -------------------------------------------------------------------
# 5. UI CUSTOMIZATION (Professional Style)
# -------------------------------------------------------------------

resource "aws_cognito_user_pool_ui_customization" "ui" {
  user_pool_id = aws_cognito_user_pool.main.id
  client_id    = aws_cognito_user_pool_client.client.id

  depends_on = [
    aws_cognito_user_pool_domain.main
  ]

  # Only verified, allowed classes
  css = <<EOF
    /* 1. Primary Button */
    .submitButton-customizable {
      background-color: #2563eb !important;
      color: #ffffff !important;
      border: none !important;
      border-radius: 8px !important;
      font-size: 16px !important;
      font-weight: 600 !important;
      text-transform: none !important;
      padding: 14px 24px !important;
      width: 100% !important;
      margin-top: 20px !important;
    }

    .submitButton-customizable:hover {
      background-color: #1d4ed8 !important;
    }

    /* 2. Input Fields */
    .inputField-customizable {
      border: 1px solid #e5e7eb !important;
      border-radius: 8px !important;
      padding: 12px 16px !important;
      font-size: 15px !important;
      color: #1f2937 !important;
      background-color: #f9fafb !important;
      width: 100% !important;
      height: 48px !important;
      margin-bottom: 15px !important;
    }

    .inputField-customizable:focus {
      border-color: #2563eb !important;
      background-color: #ffffff !important;
      box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.15) !important;
      outline: none !important;
    }

    /* 3. Labels */
    .label-customizable {
      font-weight: 500 !important;
      color: #374151 !important;
      margin-bottom: 8px !important;
    }
  EOF
}

# -------------------------------------------------------------------
# 6. OUTPUTS
# -------------------------------------------------------------------

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
