package io.github.dziodzi.service;

import io.github.dziodzi.entity.Event;
import io.github.dziodzi.entity.EventResponse;
import io.github.dziodzi.tools.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
@LogExecutionTime
public class EventService {
    
    private final RestTemplate restTemplate;
    private final CurrencyService currencyService;
    
    @Value("${custom.api.events}")
    private String eventApiUrl;
    
    @Value("${custom.filters.events}")
    private String urlFilter;
    
    private static final Pattern PRICE_PATTERN = Pattern.compile("\\d+");
    
    public Mono<List<Event>> getFilteredEvents(double budget, String currency, Long dateFrom, Long dateTo) {
        if (dateFrom == null && dateTo == null) {
            LocalDate nowLocalDate = LocalDate.now();
            LocalDate oneWeekAgo = nowLocalDate.minusDays(7);
            dateFrom = oneWeekAgo.atStartOfDay(ZoneId.of("UTC")).toInstant().getEpochSecond();
            dateTo = nowLocalDate.atStartOfDay(ZoneId.of("UTC")).toInstant().getEpochSecond();
        }
        
        Mono<List<Event>> eventsMono = getEvents(dateFrom, dateTo);
        Mono<Double> convertedBudgetMono = Mono.fromCallable(() ->
                currencyService.getValueOfCurrencyByCode(currency) * budget);
        
        return Mono.zip(eventsMono, convertedBudgetMono)
                .flatMap(tuple -> {
                    List<Event> events = tuple.getT1();
                    double convertedBudget = tuple.getT2();
                    
                    events.forEach(event ->
                            event.setParsedPrice(parsePrice(event.getPrice(), event.isFree()))
                    );
                    
                    return Mono.just(filterEventsByBudget(events, convertedBudget));
                });
    }
    
    public Mono<List<Event>> getEvents(Long dateFrom, Long dateTo) {
        return Mono.fromCallable(() -> {
            StringBuilder urlBuilder = new StringBuilder(String.format("%s%s", eventApiUrl, urlFilter));
            
            if (dateFrom != null) {
                urlBuilder.append("&actual_since=").append(dateFrom);
            }
            if (dateTo != null) {
                urlBuilder.append("&actual_until=").append(dateTo);
            }
            
            String url = urlBuilder.toString();
            log.info("Fetching events from URL: {}", url);
            
            EventResponse response = restTemplate.getForObject(url, EventResponse.class);
            return response != null ? response.getResults() : List.of();
        });
    }
    
    public List<Event> filterEventsByBudget(List<Event> events, double budget) {
        return events.stream()
                .filter(event -> event.isFree() || (event.getParsedPrice() != null && event.getParsedPrice() <= budget))
                .toList();
    }
    
    private Double parsePrice(String priceString, boolean isFree) {
        if (priceString == null || priceString.isEmpty()) {
            return isFree ? 0.0 : null;
        }
        
        priceString = priceString.replaceAll("\\s+", "");
        
        Matcher matcher = PRICE_PATTERN.matcher(priceString);
        if (matcher.find()) {
            return Double.parseDouble(matcher.group());
        } else {
            return isFree ? 0.0 : null;
        }
    }
}
