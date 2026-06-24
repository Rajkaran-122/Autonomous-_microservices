variable "aws_region" {
  description = "The AWS region to deploy the SRE platform into"
  type        = string
  default     = "us-east-1"
}

variable "cluster_name" {
  description = "Name of the EKS cluster"
  type        = string
  default     = "sre-platform-eks"
}
