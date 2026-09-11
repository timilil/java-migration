package com.example.config;

import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

/**
 * Serves the OpenAPI 3 JSON document at /v3/api-docs.
 * <p>
 * Since this application does not use Spring Boot auto-configuration, springdoc's
 * own Boot-conditional beans (which rely on spring-boot-autoconfigure) are not
 * available. This controller builds a minimal but accurate OpenAPI document by
 * inspecting the application's own {@code com.example.controller} handler
 * mappings at request time, so the served Swagger UI reflects the real API
 * instead of the swagger-ui webjar's bundled Petstore demo document.
 */
@RestController
public class OpenApiDocsController {

    private static final String CONTROLLER_BASE_PACKAGE = "com.example.controller";

    private final OpenAPI baseOpenApi;
    private final RequestMappingHandlerMapping handlerMapping;

    @Autowired
    public OpenApiDocsController(OpenAPI baseOpenApi, RequestMappingHandlerMapping handlerMapping) {
        this.baseOpenApi = baseOpenApi;
        this.handlerMapping = handlerMapping;
    }

    @GetMapping(value = "/v3/api-docs", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getApiDocs() {
        OpenAPI openApi = new OpenAPI()
            .info(baseOpenApi.getInfo())
            .paths(buildPaths());
        return Json.pretty(openApi);
    }

    private Paths buildPaths() {
        Paths paths = new Paths();

        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMapping.getHandlerMethods().entrySet()) {
            HandlerMethod handlerMethod = entry.getValue();
            if (!handlerMethod.getBeanType().getPackageName().startsWith(CONTROLLER_BASE_PACKAGE)) {
                continue;
            }

            RequestMappingInfo mappingInfo = entry.getKey();
            Set<String> patterns = resolvePatterns(mappingInfo);
            Set<RequestMethod> httpMethods = mappingInfo.getMethodsCondition().getMethods();
            Operation operation = buildOperation(handlerMethod.getMethod());

            for (String pattern : patterns) {
                PathItem pathItem = paths.computeIfAbsent(pattern, key -> new PathItem());
                addOperationToPathItem(pathItem, httpMethods, operation);
            }
        }

        return paths;
    }

    private Set<String> resolvePatterns(RequestMappingInfo mappingInfo) {
        if (mappingInfo.getPatternsCondition() != null) {
            return mappingInfo.getPatternsCondition().getPatterns();
        }
        if (mappingInfo.getPathPatternsCondition() != null) {
            return mappingInfo.getPathPatternsCondition().getPatternValues();
        }
        return Set.of();
    }

    private void addOperationToPathItem(PathItem pathItem, Set<RequestMethod> httpMethods, Operation operation) {
        if (httpMethods.isEmpty()) {
            pathItem.get(operation);
            return;
        }
        for (RequestMethod httpMethod : httpMethods) {
            switch (httpMethod) {
                case GET -> pathItem.get(operation);
                case POST -> pathItem.post(operation);
                case PUT -> pathItem.put(operation);
                case DELETE -> pathItem.delete(operation);
                case PATCH -> pathItem.patch(operation);
                case OPTIONS -> pathItem.options(operation);
                case HEAD -> pathItem.head(operation);
                default -> {
                }
            }
        }
    }

    private Operation buildOperation(Method method) {
        Operation operation = new Operation();

        io.swagger.v3.oas.annotations.Operation opAnnotation =
            method.getAnnotation(io.swagger.v3.oas.annotations.Operation.class);
        if (opAnnotation != null && !opAnnotation.summary().isEmpty()) {
            operation.setSummary(opAnnotation.summary());
        } else {
            operation.setSummary(method.getName());
        }
        if (opAnnotation != null && !opAnnotation.description().isEmpty()) {
            operation.setDescription(opAnnotation.description());
        }

        io.swagger.v3.oas.annotations.tags.Tag tagAnnotation =
            method.getDeclaringClass().getAnnotation(io.swagger.v3.oas.annotations.tags.Tag.class);
        if (tagAnnotation != null) {
            operation.addTagsItem(tagAnnotation.name());
        }

        ApiResponses responses = new ApiResponses();
        responses.addApiResponse("200", new ApiResponse().description("Successful operation"));
        operation.setResponses(responses);

        return operation;
    }
}
