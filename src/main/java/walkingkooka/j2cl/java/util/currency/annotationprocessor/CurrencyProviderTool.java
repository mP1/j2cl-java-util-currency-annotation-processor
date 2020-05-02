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
import walkingkooka.j2cl.locale.WalkingkookaLanguageTag;
import walkingkooka.text.CharSequences;
import walkingkooka.text.Indentation;
import walkingkooka.text.LineEnding;
import walkingkooka.text.printer.IndentingPrinter;
import walkingkooka.text.printer.Printer;
import walkingkooka.text.printer.Printers;

import java.util.Currency;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A simple tool that generates each and every currency known to the system.
 */
public final class CurrencyProviderTool {

    public static void main(final String[] args) {
        final IndentingPrinter printer = Printers.sysOut().indenting(Indentation.with("  "));
        new CurrencyProviderTool(printer).print(WalkingkookaLanguageTag.all("*"), Sets.of("".split(",")));
        printer.flush();
    }

    static String generateMethod(final Set<String> languageTags, final Set<String> currencyCodes) {
        final StringBuilder output = new StringBuilder();
        try (final Printer printer = Printers.stringBuilder(output, LineEnding.SYSTEM)) {
            new CurrencyProviderTool(printer.indenting(Indentation.with("  "))).print(languageTags, currencyCodes);
        }

        return output.toString();
    }

    private CurrencyProviderTool(final IndentingPrinter printer) {
        super();
        this.printer = printer;
    }

    private void print(final Set<String> languageTags, final Set<String> currencyCodes) {
        this.print0(languageTags.stream()
                        .map(Locale::forLanguageTag)
                        .collect(Collectors.toCollection(Sets::ordered)),
                currencyCodes
        );
    }

    private void print0(final Set<Locale> locales, final Set<String> currencyCodes) {
        this.print("// locales: " + locales.stream().map(Locale::toLanguageTag).collect(Collectors.joining(", ")));
        this.print("// currency codes: " + currencyCodes.stream().collect(Collectors.joining(", ")));

        this.indent();
        {
            this.print("static void register(final java.util.function.Consumer<CurrencyProvider> registry) {");
            this.indent();
            {
                final Set<Currency> ignore = Sets.ordered();
                this.printCurrenciesWithLocales(locales, ignore);
                this.printCurrencyWithoutLocales(currencyCodes, locales, ignore);

                this.print(); // emptyLine
            }
            this.outdent();
            this.print("}");
        }
        this.outdent();
    }

    private void printCurrenciesWithLocales(final Set<Locale> locales, final Set<Currency> ignore) {
        final Map<Currency, Set<Locale>> currencyToLocales = Maps.sorted(CurrencyProviderTool::compareCurrencyCodes);

        for (final Locale locale : locales) {
            try {
                final Currency currency = Currency.getInstance(locale);
                Set<Locale> localesForCurrency = currencyToLocales.get(currency);
                if (null == localesForCurrency) {
                    localesForCurrency = Sets.ordered();
                    currencyToLocales.put(currency, localesForCurrency);
                }
                localesForCurrency.add(locale);
            } catch (final Exception unsupported) {
                // locale hasnt have a currency skip it.
            }
        }

        currencyToLocales.forEach((c, l) -> this.printCurrenciesWithLocales(c, locales));
        ignore.addAll(currencyToLocales.keySet());
    }

    /**
     * Comparator used to sort by {@link Currency#getCurrencyCode()}
     */
    private static int compareCurrencyCodes(final Currency left, final Currency right) {
        return left.getCurrencyCode().compareTo(right.getCurrencyCode());
    }

    private void printCurrenciesWithLocales(final Currency currency,
                                            final Set<Locale> filteredLocales) {
        final Map<String, Set<Locale>> symbolToLocales = symbolToLocales(currency, filteredLocales);

        this.print();
        symbolToLocales.forEach((k, v) -> this.print("// " + k + "=" + v.stream().map(Locale::toLanguageTag).collect(Collectors.joining(", "))));

        this.print("registry.accept(new CurrencyProvider(" + quote(currency.getCurrencyCode()) + ", // currencyCode");
        this.indent();
        {
            this.print(currency.getDefaultFractionDigits() + ", // defaultFractionDigits");
            this.print(currency.getNumericCode() + ", // numericCode");

            final String defaultSymbol = defaultSymbol(currency);
            this.print(quote(defaultSymbol) + (symbolToLocales.size() >= 1 ? "," : "") + " // defaultSymbol"); // defaultSymbol

            final Set<Locale> locales = Sets.sorted(CurrencyProviderTool::compareLocaleLanguageTag);

            for (final Locale possible : filteredLocales) {
                try {
                    final Currency possibleCurrency = Currency.getInstance(possible);
                    if (currency.getCurrencyCode().equals(possibleCurrency.getCurrencyCode())) {
                        locales.add(possible);
                    }
                } catch (final Exception ignore) {

                }
            }

            symbolToLocales.remove(defaultSymbol);

            this.print(quote(locales.stream()
                    .map(Locale::toLanguageTag)
                    .collect(Collectors.joining(","))) + (symbolToLocales.isEmpty() ? "" : ",") + " // locales");

            this.printSymbolsToLocales(symbolToLocales);
        }
        this.outdent();
        this.print("));");
    }

    /**
     * Finds the default symbol for a {@link Currency} which seems to be the most popular symbol for all JRE locales.
     */
    private String defaultSymbol(final Currency currency) {
        String most = null;
        int mostCount = -1;

        for (final Entry<String, Set<Locale>> symbolAndLocales : symbolToLocales(currency, WalkingkookaLanguageTag.locales()).entrySet()) {
            final int count = symbolAndLocales.getValue().size();
            if (count > mostCount) {
                mostCount = count;
                most = symbolAndLocales.getKey();
            }
        }

        return most;
    }

    private void printCurrencyWithoutLocales(final Set<String> currencyCodes,
                                             final Set<Locale> locales,
                                             final Set<Currency> ignore) {
        for (final String currencyCode : currencyCodes) {
            try {
                final Currency currency = Currency.getInstance(currencyCode);

                // dont output $currency if it has already been consumed
                if (ignore.add(currency)) {
                    printCurrencyWithoutLocales0(currency, locales);
                }
            } catch (final Exception unsupported) {
            }
        }
    }

    private void printCurrencyWithoutLocales0(final Currency currency, final Set<Locale> locales) {
        final String currencyCode = currency.getCurrencyCode();

        this.print();
        this.print("// " + CharSequences.quoteAndEscape(currencyCode));

        this.print("registry.accept(new CurrencyProvider(" + quote(currencyCode) + ", // currencyCode");
        this.indent();
        {
            this.print(currency.getDefaultFractionDigits() + ", // defaultFractionDigits");
            this.print(currency.getNumericCode() + ", // numericCode");
            this.print(quote(currencyCode) + ", // defaultSymbol"); // defaultSymbol

            final Map<String, Set<Locale>> symbolToLocales = symbolToLocales(currency, locales);
            symbolToLocales.remove(currencyCode);

            this.print(quote("") + (symbolToLocales.isEmpty() ? "": ",") + " // locales");
            this.printSymbolsToLocales(symbolToLocales);
        }
        this.outdent();
        this.print("));");

        this.print();
    }

    private void printSymbolsToLocales(final Map<String, Set<Locale>> symbolToLocales) {
        int i = 0;
        for (final Entry<String, Set<Locale>> symbolAndLocales : symbolToLocales.entrySet()) {
            final String separator = (i < symbolToLocales.size() - 1) ?
                    "," :
                    "";
            this.print(quote(symbolAndLocales.getValue()
                    .stream()
                    .map(Locale::toLanguageTag)
                    .collect(Collectors.joining(",", symbolAndLocales.getKey() + ",", ""))) + separator + " // symbolToLocales");
            i++;
        }
    }

    /**
     * Builds an mapping of symbol to the locales for this currency
     */
    private static Map<String, Set<Locale>> symbolToLocales(final Currency currency, final Set<Locale> locales) {
        final Map<String, Set<Locale>> symbolToLocales = Maps.sorted();

        // gather all symbol to locales
        for (final Locale locale : locales) {
            final String symbol = currency.getSymbol(locale);
            Set<Locale> localesForSymbol = symbolToLocales.get(symbol);
            if (null == localesForSymbol) {
                localesForSymbol = Sets.sorted(CurrencyProviderTool::compareLocaleLanguageTag);
                symbolToLocales.put(symbol, localesForSymbol);
            }
            localesForSymbol.add(locale);
        }

        return symbolToLocales;
    }

    private static int compareLocaleLanguageTag(final Locale left, final Locale right) {
        return left.toLanguageTag().compareTo(right.toLanguageTag());
    }

    private static CharSequence quote(final String text) {
        return CharSequences.quoteAndEscape(text);
    }

    private void indent() {
        this.printer.indent();
    }

    private void outdent() {
        this.printer.outdent();
    }

    private void print() {
        this.print("");
    }

    private void print(final String text) {
        this.printer.lineStart();
        this.printer.print(text);
    }

    private final IndentingPrinter printer;
}
