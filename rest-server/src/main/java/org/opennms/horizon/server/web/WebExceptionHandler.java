/*
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2023 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2023 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */

package org.opennms.horizon.server.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.result.view.ViewResolver;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.stream.Collectors;

import static org.opennms.horizon.server.web.WebExceptionHandler.BEFORE_OTHER_WEB_EXCEPTION_HANDLERS;

/**
 * Handles web-level exceptions, ensuring that appropriate data is returned.
 * <p>
 * This class implements the {@link org.springframework.web.server.WebExceptionHandler}
 * interface instead of being annotated with
 * {@link org.springframework.web.bind.annotation.ControllerAdvice}
 * because unlike ControllerAdvice, WebExceptionHandlers are capable of handling
 * exceptions that occur *before* or *after* the controller, ie in a
 * {@link org.springframework.web.server.WebFilter}.
 * <p>
 * Note That this exception handler will not handle exceptions that occur during
 * GraphQL data fetching. Those are instead handled with the
 * {@link org.opennms.horizon.server.service.graphql.BffDataFetchExceptionHandler}.
 *
 * @see org.springframework.boot.autoconfigure.web.reactive.error.ErrorWebFluxAutoConfiguration#errorWebExceptionHandler
 * @see org.springframework.web.server.handler.ExceptionHandlingWebHandler
 */
@Component
@Order(BEFORE_OTHER_WEB_EXCEPTION_HANDLERS)
@Slf4j
public class WebExceptionHandler extends AbstractErrorWebExceptionHandler {

    /**
     * An ordering to allow this handler to take precedence over other
     * {@link org.springframework.web.server.WebExceptionHandler}s.
     *
     * @see org.springframework.boot.autoconfigure.web.reactive.error.ErrorWebFluxAutoConfiguration#errorWebExceptionHandler
     * @see org.springframework.web.server.handler.ExceptionHandlingWebHandler
     */
    static final int BEFORE_OTHER_WEB_EXCEPTION_HANDLERS = -2;

    /** {@inheritDoc} */
    public WebExceptionHandler(
        ErrorAttributes errorAttributes,
        WebProperties webProperties,
        ApplicationContext applicationContext,
        ObjectProvider<ViewResolver> viewResolvers,
        ServerCodecConfigurer serverCodecConfigurer
    ) {
        super(errorAttributes, webProperties.getResources(), applicationContext);

        // Additional required config. See org.springframework.boot.autoconfigure
        // .web.reactive.error.ErrorWebFluxAutoConfiguration.errorWebExceptionHandler
        this.setViewResolvers(viewResolvers.orderedStream().collect(Collectors.toList()));
        this.setMessageReaders(serverCodecConfigurer.getReaders());
        this.setMessageWriters(serverCodecConfigurer.getWriters());
    }

    /** {@inheritDoc} */
    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
    }

    /**
     * @see org.springframework.web.server.WebExceptionHandler#handle(ServerWebExchange, Throwable)
     */
    private Mono<ServerResponse> renderErrorResponse(ServerRequest request) {
        Throwable t = getError(request);

        if (t instanceof UnsupportedMediaTypeStatusException e) {
            return handle(e, request);
        } else if (t instanceof ResponseStatusException e) {
            return handle(e, request);
        } else if (t instanceof Exception e) {
            return handle(e, request);
        }
        // Anything not handled is a java.lang.Error. Let another handler handle
        // it, if at all.
        return Mono.error(t);
    }

    public Mono<ServerResponse> handle(UnsupportedMediaTypeStatusException e, ServerRequest request) {
        return createResponse(e, request, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    public Mono<ServerResponse> handle(ResponseStatusException e, ServerRequest request) {
        HttpStatus status = HttpStatus.resolve(e.getStatusCode().value());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        String message = e.getReason() != null ? e.getReason() : status.getReasonPhrase();

        return createResponse(e, request, status, message);
    }

    public Mono<ServerResponse> handle(Exception e, ServerRequest request) {
        return createResponse(e, request, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private Mono<ServerResponse> createResponse(
        Throwable t,
        ServerRequest request,
        HttpStatus status
    ) {
        return createResponse(t, request, status, status.getReasonPhrase());
    }

    private Mono<ServerResponse> createResponse(
        Throwable t,
        ServerRequest request,
        HttpStatus status,
        String message
    ) {
        if (status.is5xxServerError()) {
            log.error("Exception occurred during request processing", t);
        }
        Map<String, Object> attributes = getErrorAttributes(
            request, ErrorAttributeOptions.defaults()
        );
        attributes.put("message", message);

        return ServerResponse.status(status)
            .contentType(MediaType.APPLICATION_JSON)
            .body(BodyInserters.fromValue(attributes));
    }
}
