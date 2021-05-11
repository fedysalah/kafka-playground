package org.fsalah.config

import org.fsalah.config.domain.Env
import org.fsalah.handlers.RouterHandler
import org.fsalah.logger
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router

@Configuration
internal class RouterConfig {

    @Bean
    fun staticRoutes(
            env: Env,
            routerHandler: RouterHandler
    ): RouterFunction<ServerResponse> {

        return router {
            GET("/events/_start", routerHandler::startEmitting)
            GET("/events/_stop", routerHandler::stopEmitting)
            GET("/events", routerHandler::records)
            GET("/inject-fault", routerHandler::injectFault)
            GET("/recover", routerHandler::recover)
        }.filter { request, next ->
            try {
                next.handle(request)
            } catch (ex: Exception) {
                logger.error("an error occurred while processing your request", ex)
                ServerResponse.status(500).build()
            }
        }
    }
}
