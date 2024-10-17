package io.github.dziodzi.controller.api;

import io.github.dziodzi.entity.Event;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;

import java.util.List;

public interface EventAPI {
    
    @Operation(summary = "Get a list of events based on budget and date range")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful retrieval of events",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "[\n" +
                                    "  {\n" +
                                    "    \"id\": 211191,\n" +
                                    "    \"title\": \"пробное занятие по актёрскому мастерству в креативном центре Kometa lab\",\n" +
                                    "    \"price\": \"пробное занятие — 500 руб.\",\n" +
                                    "    \"parsedPrice\": 500,\n" +
                                    "    \"free\": false\n" +
                                    "  }\n" +
                                    "]"))),
            @ApiResponse(responseCode = "404", description = "Currency code is not found",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\n" +
                                    "  \"timestamp\": \"2024-10-11T12:06:49.711883629\",\n" +
                                    "  \"error\": \"Currency Not Found\",\n" +
                                    "  \"message\": \"Currency not found by code: USDЫ\",\n" +
                                    "  \"errorCode\": \"CURRENCY_NOT_FOUND\",\n" +
                                    "  \"status\": 404\n" +
                                    "}")))
    })
    @GetMapping("/events")
    Mono<ResponseEntity<List<Event>>> getEvents(
            @Parameter(description = "Budget to filter events")
            @RequestParam @Min(0) double budget,
            @Parameter(description = "Currency code (e.g., USD, EUR)", example = "USD")
            @RequestParam @Size(min = 3, max = 3) String currency,
            @Parameter(description = "Start date of the interval")
            @RequestParam(required = false) Long dateFrom,
            @Parameter(description = "End date of the interval")
            @RequestParam(required = false) Long dateTo
    );
}
