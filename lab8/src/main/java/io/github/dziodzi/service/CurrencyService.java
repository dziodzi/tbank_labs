package io.github.dziodzi.service;

import io.github.dziodzi.entity.ConvertRequest;
import io.github.dziodzi.exception.ConvertingException;
import io.github.dziodzi.exception.NotFoundException;
import io.github.dziodzi.tools.LogExecutionTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.text.DecimalFormat;

@Service
@Slf4j
@Validated
@LogExecutionTime
public class CurrencyService {
    private final ApiService apiService;
    private final CurrencyParserService parserService;
    private final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("###.###");
    
    @Autowired
    public CurrencyService(ApiService apiService, CurrencyParserService parserService) {
        this.apiService = apiService;
        this.parserService = parserService;
    }
    
    public double getValueOfCurrencyByCode(String code) {
        try {
            String currencyData = apiService.fetchCurrencyDataWithCode(code);
            String result = parserService.getCurrencyValueByCode(code, currencyData);
            double parsedResult = Double.parseDouble(result.replace(",", "."));
            log.info("Successfully got rate for {}: {}", code, parsedResult);
            return parsedResult;
        } catch (NotFoundException e) {
            log.error("Failed to get rate for currency code: {}", code);
            throw new NotFoundException(e.getMessage());
        } catch (Exception e) {
            throw new ConvertingException(e.getMessage());
        }
    }
    
    public double convertCurrency(ConvertRequest request) {
        if (request.getFromCurrency() == null || request.getToCurrency() == null) {
            throw new ConvertingException("Currency codes can't be null");
        }
        
        try {
            String currencyData = apiService.fetchCurrencyDataWithRequest(request);
            double fromValue = Double.parseDouble(parserService.getCurrencyValueByCode(request.getFromCurrency(), currencyData).replace(",", "."));
            double toValue = Double.parseDouble(parserService.getCurrencyValueByCode(request.getToCurrency(), currencyData).replace(",", "."));
            
            double conversionResult = Double.parseDouble(DECIMAL_FORMAT.format((fromValue / toValue) * request.getAmount()).replace(",", "."));
            
            log.info("Successfully converted {}: {} -> {}: {}",
                    request.getFromCurrency(), request.getAmount(), request.getToCurrency(), conversionResult);
            
            return conversionResult;
        } catch (NotFoundException e) {
            log.error("Failed to convert from {}: {} to {}",
                    request.getFromCurrency(), request.getAmount(), request.getToCurrency());
            throw new NotFoundException(e.getMessage());
        } catch (Exception e) {
            throw new ConvertingException(e.getMessage());
        }
    }
}
