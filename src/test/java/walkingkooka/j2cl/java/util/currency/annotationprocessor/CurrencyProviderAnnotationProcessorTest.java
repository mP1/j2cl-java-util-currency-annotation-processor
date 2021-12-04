/*
 * Copyright 2019 Miroslav Pokorny (github.com/mP1)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package walkingkooka.j2cl.java.util.currency.annotationprocessor;

import org.junit.jupiter.api.Test;
import walkingkooka.collect.set.Sets;
import walkingkooka.reflect.ClassTesting;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.text.CharSequences;

import java.util.Currency;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class CurrencyProviderAnnotationProcessorTest implements ClassTesting<CurrencyProviderAnnotationProcessor> {

    @Test
    public void testCurrencyCodesXXX() {
        this.currencyCodesAndCheck("XXX", "XXX");
    }

    @Test
    public void testCurrencyCodesAUD() {
        this.currencyCodesAndCheck("AUD", "AUD");
    }

    @Test
    public void testCurrencyCodesNZD() {
        this.currencyCodesAndCheck("NZD", "NZD");
    }

    @Test
    public void testCurrencyCodesAUD_NZD() {
        this.currencyCodesAndCheck("AUD,NZD", "AUD", "NZD");
    }

    @Test
    public void testCurrencyCodesNZD_AUD_() {
        this.currencyCodesAndCheck("NZD,AUD", "AUD", "NZD");
    }

    @Test
    public void testCurrencyCodesWildcard() {
        this.currencyCodesAndCheck("*",
                Currency.getAvailableCurrencies()
                        .stream()
                        .map(Currency::getCurrencyCode)
                        .collect(Collectors.toCollection(Sets::sorted)));
    }

    private void currencyCodesAndCheck(final String filter, final String... currencyCodes) {
        this.currencyCodesAndCheck(filter, Sets.of(currencyCodes));
    }

    private void currencyCodesAndCheck(final String filter, final Set<String> currencyCodes) {
        assertEquals(currencyCodes,
                CurrencyProviderAnnotationProcessor.currencyCodes(filter),
                () -> "filter " + CharSequences.quoteAndEscape(filter));
    }

    @Test
    public void testDefaultPublicConstructor() throws Exception {
        this.checkEquals(
                JavaVisibility.PUBLIC,
                JavaVisibility.of(CurrencyProviderAnnotationProcessor.class.getConstructor())
        );
    }

    @Override
    public Class<CurrencyProviderAnnotationProcessor> type() {
        return CurrencyProviderAnnotationProcessor.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PUBLIC;
    }
}
