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

import walkingkooka.collect.map.Maps;
import walkingkooka.collect.set.Sets;
import walkingkooka.j2cl.java.io.string.StringDataInputDataOutput;
import walkingkooka.j2cl.java.util.locale.support.LocaleSupport;
import walkingkooka.j2cl.locale.WalkingkookaLanguageTag;
import walkingkooka.j2cl.locale.annotationprocessor.LocaleAwareAnnotationProcessor;
import walkingkooka.j2cl.locale.annotationprocessor.LocaleAwareAnnotationProcessorTool;
import walkingkooka.text.CharSequences;
import walkingkooka.text.printer.IndentingPrinter;
import walkingkooka.text.printer.Printer;
import walkingkooka.text.printer.Printers;

import java.io.DataOutput;
import java.io.IOException;
import java.util.Currency;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A simple tool that generates each and every currency known to the system.
 */
public final class CurrencyProviderTool {

    public static void main(final String[] args) throws IOException {
        try (final Printer printer = Printers.sysOut()) {
            final StringBuilder data = new StringBuilder();
            generate(WalkingkookaLanguageTag.all("*"),
                    Sets.of("XXX"),
                    StringDataInputDataOutput.output(data::append),
                    LocaleAwareAnnotationProcessor.comments(printer));
            printer.print(CharSequences.quoteAndEscape(data));
            printer.flush();
        }
    }

    static void generate(final Set<String> languageTags,
                         final Set<String> currencyCodes,
                         final DataOutput data,
                         final IndentingPrinter comments) throws IOException {
        generate0(languageTags.stream()
                        .map(Locale::forLanguageTag)
                        .collect(Collectors.toCollection(Sets::ordered)),
                currencyCodes,
                data,
                comments);
    }

    static void generate0(final Set<Locale> locales,
                          final Set<String> currencyCodes,
                          final DataOutput data,
                          final IndentingPrinter comments) throws IOException {
        data.writeInt(currencyToLocale(locales, Sets.hash()).size() + currencyCodes.size());

        generate1(locales,
                currencyCodes,
                data,
                comments);
    }

    private static void generate1(final Set<Locale> locales,
                          final Set<String> currencyCodes,
                          final DataOutput data,
                          final IndentingPrinter comments) throws IOException {
        final Set<Currency> ignore = Sets.ordered();
        generateCurrenciesWithLocales(locales, ignore, data, comments);
        generateCurrenciesWithoutLocales(currencyCodes, locales, ignore, data, comments);
    }

    private static void generateCurrenciesWithLocales(final Set<Locale> locales,
                                                      final Set<Currency> ignore,
                                                      final DataOutput data,
                                                      final IndentingPrinter comments) {
        final Map<Currency, Set<Locale>> currencyToLocales = currencyToLocale(locales, ignore);
        currencyToLocales.forEach((c, l) -> generateCurrenciesWithLocales(c, locales, data, comments));
    }

    private static Map<Currency, Set<Locale>> currencyToLocale(final Set<Locale> locales,
                                                               final Set<Currency> ignore) {
        final Map<Currency, Set<Locale>> currencyToLocales = Maps.sorted(CurrencyProviderTool::compareCurrencyCodes);

        for (final Locale locale : locales) {
            try {
                final Currency currency = Currency.getInstance(locale);
                Set<Locale> localesForCurrency = currencyToLocales.get(currency);
                if (null == localesForCurrency) {
                    localesForCurrency = Sets.ordered();
                    currencyToLocales.put(currency, localesForCurrency);
                }
                if (ignore.contains(currency)) {
                    continue;
                }
                localesForCurrency.add(locale);
                ignore.add(currency);
            } catch (final Exception unsupported) {
                // locale doesnt have a currency skip it.
            }
        }

        return currencyToLocales;
    }

    /**
     * Comparator used to sort by {@link Currency#getCurrencyCode()}
     */
    private static int compareCurrencyCodes(final Currency left, final Currency right) {
        return left.getCurrencyCode().compareTo(right.getCurrencyCode());
    }

    private static void generateCurrenciesWithLocales(final Currency currency,
                                                      final Set<Locale> filteredLocales,
                                                      final DataOutput data,
                                                      final IndentingPrinter comments) {
        try {
            generateCurrenciesWithLocales0(currency,
                    filteredLocales,
                    data,
                    comments);
        } catch (final IOException cause) {
            throw new RuntimeException(cause);
        }
    }

    private static void generateCurrenciesWithLocales0(final Currency currency,
                                                       final Set<Locale> filteredLocales,
                                                       final DataOutput data,
                                                       final IndentingPrinter comments) throws IOException {
        final String defaultSymbol = defaultSymbol(currency);

        generateCurrencyAttributes(currency,
                defaultSymbol,
                data,
                comments);

        final Set<Locale> locales = Sets.sorted(LocaleAwareAnnotationProcessorTool.LOCALE_COMPARATOR);

        for (final Locale possible : filteredLocales) {
            try {
                final Currency possibleCurrency = Currency.getInstance(possible);
                if (currency.getCurrencyCode().equals(possibleCurrency.getCurrencyCode())) {
                    locales.add(possible);
                }
            } catch (final Exception ignore) {
                // ignore
            }
        }

        LocaleSupport.generateLocales(locales, data, comments);

        final Map<String, Set<Locale>> symbolToLocales = buildSymbolToLocales(currency, filteredLocales);
        symbolToLocales.remove(defaultSymbol);
        generateSymbolsToLocales(symbolToLocales, data, comments);

        comments.lineStart();
        comments.print(comments.lineEnding());
    }

    /**
     * Finds the default symbol for a {@link Currency} which seems to be the most popular symbol for all JRE locales.
     */
    private static String defaultSymbol(final Currency currency) {
        String most = null;
        int mostCount = -1;

        for (final Entry<String, Set<Locale>> symbolAndLocales : buildSymbolToLocales(currency, WalkingkookaLanguageTag.locales()).entrySet()) {
            final int count = symbolAndLocales.getValue().size();
            if (count > mostCount) {
                mostCount = count;
                most = symbolAndLocales.getKey();
            }
        }

        return most;
    }

    private static void generateCurrenciesWithoutLocales(final Set<String> currencyCodes,
                                                         final Set<Locale> locales,
                                                         final Set<Currency> ignore,
                                                         final DataOutput data,
                                                         final IndentingPrinter comments) throws IOException {
        for (final String currencyCode : currencyCodes) {
            try {
                final Currency currency = Currency.getInstance(currencyCode);

                // dont output $currency if it has already been consumed
                if (ignore.add(currency)) {
                    generateCurrencyWithoutLocales0(currency, locales, data, comments);
                }
            } catch (final Exception unsupported) {
            }
        }
    }

    private static void generateCurrencyWithoutLocales0(final Currency currency,
                                                        final Set<Locale> locales,
                                                        final DataOutput data,
                                                        final IndentingPrinter comments) throws IOException {
        final String currencyCode = currency.getCurrencyCode();
        generateCurrencyAttributes(currency,
                currencyCode,
                data,
                comments);

        LocaleSupport.generateLocales(Sets.empty(), data, comments);

        final Map<String, Set<Locale>> symbolToLocales = buildSymbolToLocales(currency, locales);
        symbolToLocales.remove(currencyCode);
        generateSymbolsToLocales(symbolToLocales, data, comments);

        comments.lineStart();
        comments.print(comments.lineEnding());
    }

    private static Map<String, Set<Locale>> buildSymbolToLocales(final Currency currency,
                                                                 final Set<Locale> locales) {
        return LocaleAwareAnnotationProcessorTool.buildMultiLocaleMap(localeToSymbol(currency), locales);
    }

    private static Function<Locale, String> localeToSymbol(final Currency currency) {
        return l -> currency.getSymbol(l);
    }

    private static void generateCurrencyAttributes(final Currency currency,
                                                   final String defaultSymbol,
                                                   final DataOutput data,
                                                   final IndentingPrinter comments) throws IOException {
        final String currencyCode = currency.getCurrencyCode();
        comments.lineStart();
        comments.print("currencyCode=" + currencyCode);
        data.writeUTF(currencyCode);

        final int defaultFractionDigits = currency.getDefaultFractionDigits();
        comments.lineStart();
        comments.print("defaultFractionDigits=" + defaultFractionDigits);
        data.writeInt(defaultFractionDigits);

        final int numericCode = currency.getNumericCode();
        comments.lineStart();
        comments.print("numericCode=" + numericCode);
        data.writeInt(numericCode);

        comments.lineStart();
        comments.print("defaultSymbol=" + defaultSymbol);
        data.writeUTF(defaultSymbol);
    }

    /**
     * For each symbol to locale, write the symbol and then a csv string of locales.
     */
    private static void generateSymbolsToLocales(final Map<String, Set<Locale>> symbolToLocales,
                                                 final DataOutput data,
                                                 final IndentingPrinter comments) throws IOException {
        data.writeInt(symbolToLocales.size());

        for (final Entry<String, Set<Locale>> symbolAndLocales : symbolToLocales.entrySet()) {
            final String symbol = symbolAndLocales.getKey();
            data.writeUTF(symbol);

            LocaleSupport.generateLocales(symbolAndLocales.getValue(),
                    data,
                    symbol,
                    comments);
        }
    }

    /**
     * Stop creation
     */
    private CurrencyProviderTool() {
        super();
    }
}
