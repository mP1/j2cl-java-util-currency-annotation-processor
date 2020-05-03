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
import walkingkooka.collect.list.Lists;
import walkingkooka.collect.set.Sets;
import walkingkooka.j2cl.locale.WalkingkookaLanguageTag;
import walkingkooka.reflect.ClassTesting;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.text.CaseSensitivity;
import walkingkooka.text.LineEnding;

import java.util.Arrays;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public final class CurrencyProviderToolTest implements ClassTesting<CurrencyProviderTool> {

    @Test
    public void testCurrencyDEWildcard() {
        final String expected = "// locales: de, de-AT, de-BE, de-CH, de-DE, de-LI, de-LU\n" +
                "// currency codes: \n" +
                "  static void register(final java.util.function.Consumer<CurrencyProvider> registry) {\n" +
                "    // CHF=de, de-AT, de-BE, de-CH, de-DE, de-LI, de-LU\n" +
                "    registry.accept(new CurrencyProvider(\"CHF\", // currencyCode\n" +
                "      2, // defaultFractionDigits\n" +
                "      756, // numericCode\n" +
                "      \"CHF\", // defaultSymbol\n" +
                "      \"de-CH,de-LI\" // locales\n" +
                "    ));\n" +
                "    // EUR=de-CH, de-LI\n" +
                "    // €=de, de-AT, de-BE, de-DE, de-LU\n" +
                "    registry.accept(new CurrencyProvider(\"EUR\", // currencyCode\n" +
                "      2, // defaultFractionDigits\n" +
                "      978, // numericCode\n" +
                "      \"€\", // defaultSymbol\n" +
                "      \"de-AT,de-BE,de-DE,de-LU\", // locales\n" +
                "      \"EUR,de-CH,de-LI\" // symbolToLocales\n" +
                "    ));\n" +
                "  }";
        assertEquals(expected, CurrencyProviderTool.generateMethod(WalkingkookaLanguageTag.all("DE*"), Sets.empty()));
    }

    @Test
    public void testCurrencyENAU() {
        final String expected = "// locales: en-AU\n" +
                "// currency codes: \n" +
                "  static void register(final java.util.function.Consumer<CurrencyProvider> registry) {\n" +
                "    // $=en-AU\n" +
                "    registry.accept(new CurrencyProvider(\"AUD\", // currencyCode\n" +
                "      2, // defaultFractionDigits\n" +
                "      36, // numericCode\n" +
                "      \"A$\", // defaultSymbol\n" +
                "      \"en-AU\", // locales\n" +
                "      \"$,en-AU\" // symbolToLocales\n" +
                "    ));\n" +
                "  }";
        assertEquals(expected, CurrencyProviderTool.generateMethod(Sets.of("EN-AU"), Sets.empty()));
    }

    @Test
    public void testCurrencyENNZ() {
        final String expected = "// locales: en-NZ\n" +
                "// currency codes: \n" +
                "  static void register(final java.util.function.Consumer<CurrencyProvider> registry) {\n" +
                "    // $=en-NZ\n" +
                "    registry.accept(new CurrencyProvider(\"NZD\", // currencyCode\n" +
                "      2, // defaultFractionDigits\n" +
                "      554, // numericCode\n" +
                "      \"NZ$\", // defaultSymbol\n" +
                "      \"en-NZ\", // locales\n" +
                "      \"$,en-NZ\" // symbolToLocales\n" +
                "    ));\n" +
                "  }";
        assertEquals(expected, CurrencyProviderTool.generateMethod(Sets.of("EN-NZ"), Sets.empty()));
    }

    @Test
    public void testCurrencyCodeXXX() {
        final String expected = "// locales: \n" +
                "// currency codes: XXX\n" +
                "  static void register(final java.util.function.Consumer<CurrencyProvider> registry) {\n" +
                "    // \"XXX\"\n" +
                "    registry.accept(new CurrencyProvider(\"XXX\", // currencyCode\n" +
                "      -1, // defaultFractionDigits\n" +
                "      999, // numericCode\n" +
                "      \"XXX\", // defaultSymbol\n" +
                "      \"\" // locales\n" +
                "    ));\n" +
                "  }";
        assertEquals(expected, CurrencyProviderTool.generateMethod(Sets.empty(), Sets.of("XXX")));
    }

    @Test
    public void testCurrencyENNZAndCurrencyCodeXXX() {
        final String expected = "// locales: en-NZ\n" +
                "// currency codes: XXX\n" +
                "  static void register(final java.util.function.Consumer<CurrencyProvider> registry) {\n" +
                "    // $=en-NZ\n" +
                "    registry.accept(new CurrencyProvider(\"NZD\", // currencyCode\n" +
                "      2, // defaultFractionDigits\n" +
                "      554, // numericCode\n" +
                "      \"NZ$\", // defaultSymbol\n" +
                "      \"en-NZ\", // locales\n" +
                "      \"$,en-NZ\" // symbolToLocales\n" +
                "    ));\n" +
                "    // \"XXX\"\n" +
                "    registry.accept(new CurrencyProvider(\"XXX\", // currencyCode\n" +
                "      -1, // defaultFractionDigits\n" +
                "      999, // numericCode\n" +
                "      \"XXX\", // defaultSymbol\n" +
                "      \"\" // locales\n" +
                "    ));\n" +
                "  }";
        assertEquals(expected, CurrencyProviderTool.generateMethod(Sets.of("EN-NZ"), Sets.of("XXX")));
    }

    @Test
    public void testCurrencyWithDuplicate() {
        final String expected = "// locales: en-NZ\n" +
                "// currency codes: NZD\n" +
                "  static void register(final java.util.function.Consumer<CurrencyProvider> registry) {\n" +
                "    // $=en-NZ\n" +
                "    registry.accept(new CurrencyProvider(\"NZD\", // currencyCode\n" +
                "      2, // defaultFractionDigits\n" +
                "      554, // numericCode\n" +
                "      \"NZ$\", // defaultSymbol\n" +
                "      \"en-NZ\", // locales\n" +
                "      \"$,en-NZ\" // symbolToLocales\n" +
                "    ));\n" +
                "  }";
        assertEquals(expected, CurrencyProviderTool.generateMethod(Sets.of("EN-NZ"), Sets.of("NZD")));
    }

    @Test
    public void testGeneratedCodeWithoutXXX() {
        final String generated = CurrencyProviderTool.generateMethod(WalkingkookaLanguageTag.all(), Sets.empty());

        assertEquals(false,
                CaseSensitivity.INSENSITIVE.contains(removeHeaderComments(generated), "XXX"),
                () -> "generated code should not contain currency code \"XXX\"\n" + generated);
    }

    @Test
    public void testGeneratedCodeWithoutYYY() {
        final String generated = CurrencyProviderTool.generateMethod(WalkingkookaLanguageTag.all(), Sets.empty());

        assertEquals(false,
                CaseSensitivity.INSENSITIVE.contains(removeHeaderComments(generated), "YYY"),
                () -> "generated code should not contain currency code \"YYY\"\n" + generated);
    }

    @Test
    public void testGeneratedCodeIncludesAllCurrenciesWithLocalesCurrencyCode() {
        final String generated = CurrencyProviderTool.generateMethod(WalkingkookaLanguageTag.all(), Sets.empty());
        final String generated2 = removeHeaderComments(generated);

        final Set<String> currencyCodeWithLocales = Currency.getAvailableCurrencies()
                .stream()
                .filter(c -> {
                    boolean keep = false;

                    for (final Locale locale : Locale.getAvailableLocales()) {
                        try {
                            keep = c.equals(Currency.getInstance(locale));
                            if (keep) {
                                break;
                            }
                        } catch (final Exception e) {
                        }
                    }
                    return keep;
                })
                .map(Currency::getCurrencyCode)
                .collect(Collectors.toCollection(Sets::sorted));

        assertEquals(true,
                currencyCodeWithLocales.size() > 25,
                () -> "There should be at least 25 currencies with locales " + currencyCodeWithLocales);
        assertNotEquals(Sets.empty(),
                currencyCodeWithLocales,
                "Should have found many currencies with locales");

        final List<String> missing = currencyCodeWithLocales.stream()
                .filter(cc -> false == CaseSensitivity.INSENSITIVE.contains(generated2, cc))
                .collect(Collectors.toList());

        assertEquals(Lists.empty(),
                missing,
                () -> "generated code missing the following locales with currency\n" + generated);
    }

    private String removeHeaderComments(final String generated) {
        // The first two lines which hold locales and currency code lists
        final String generated1 = generated.substring(generated.indexOf(LineEnding.SYSTEM.toString()) + 1);
        return generated.substring(generated1.indexOf(LineEnding.SYSTEM.toString()) + 1);
    }

    @Test
    public void testGeneratedCodeIncludesLocalesLanguageTagLiterals() {
        final String generated = CurrencyProviderTool.generateMethod(WalkingkookaLanguageTag.all(), Sets.empty());

        final List<String> with = Arrays.stream(Locale.getAvailableLocales())
                .filter(l -> {
                    boolean keep = false;

                    try {
                        Currency.getInstance(l);
                        keep = true;
                    } catch (final Exception e) {
                    }
                    return keep;
                })
                .map(Locale::toLanguageTag)
                .collect(Collectors.toList());
        assertNotEquals(Lists.empty(),
                with,
                () -> "All locales should have appeared in generated code\n" + generated);
    }

    // ClassTesting.....................................................................................................

    @Override
    public Class<CurrencyProviderTool> type() {
        return CurrencyProviderTool.class;
    }

    @Override
    public JavaVisibility typeVisibility() {
        return JavaVisibility.PUBLIC;
    }
}
