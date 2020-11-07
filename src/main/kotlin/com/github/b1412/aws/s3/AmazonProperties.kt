package com.github.b1412.aws.s3


import org.springframework.boot.context.properties.ConfigurationProperties
import javax.validation.constraints.NotBlank

@ConfigurationProperties("aws.s3")
data class AmazonProperties(
        @NotBlank
        var bucketName: String? = null,

        @NotBlank
        var accessKey: String? = null,

        @NotBlank
        var secretKey: String? = null,

        @NotBlank
        var key: String? = null,

        @NotBlank
        var regionLink: String? = null
)
