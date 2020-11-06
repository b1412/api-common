package com.github.b1412.json


@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class GraphRender(
        val entity: String
)