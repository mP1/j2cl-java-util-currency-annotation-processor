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
import walkingkooka.j2cl.java.io.string.StringDataInputDataOutput;
import walkingkooka.j2cl.locale.WalkingkookaLanguageTag;
import walkingkooka.reflect.ClassTesting;
import walkingkooka.reflect.JavaVisibility;
import walkingkooka.text.CaseSensitivity;
import walkingkooka.text.CharSequences;
import walkingkooka.text.Indentation;
import walkingkooka.text.LineEnding;
import walkingkooka.text.printer.Printer;
import walkingkooka.text.printer.Printers;

import java.io.DataInput;
import java.io.EOFException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class CurrencyProviderToolTest implements ClassTesting<CurrencyProviderTool> {

    @Test
    public void testCurrencyCount3Locales() throws Exception {
        generateAndReadDataAndCheck(Sets.of("EN-AU", "EN-NZ", "EN-CA"));
    }

    @Test
    public void testCurrencyCount5Locales() throws Exception {
        generateAndReadDataAndCheck(Sets.of("EN-AU", "EN-NZ", "EN-CA", "EN-GB", "EN-US"));
    }

    @Test
    public void testCurrencyCountAllLocales() throws Exception {
        generateAndReadDataAndCheck(WalkingkookaLanguageTag.all());
    }

    private void generateAndReadDataAndCheck(final Set<String> locales) throws Exception {
        final StringBuilder dataOutput = new StringBuilder();
        CurrencyProviderTool.generate(locales,
                Sets.empty(),
                StringDataInputDataOutput.output(dataOutput::append),
                Printers.sink().indenting(Indentation.with("  ")));

        final DataInput data = StringDataInputDataOutput.input(dataOutput.toString());
        final int count = data.readInt();

        final long currencyCount = locales.stream()
                .flatMap(l -> {
                    Stream<Currency> keep;
                    try {
                        keep = Stream.of(Currency.getInstance(Locale.forLanguageTag(l)));
                    } catch (final Exception without) {
                        keep = Stream.empty();
                    }
                    return keep;
                })
                .distinct()
                .count();
        assertEquals(currencyCount,
                count,
                () -> "locale record count " + dataOutput);

        for (int i = 0; i < count; i++) {
            final String currencyCode = data.readUTF();
            final int defaultFractionDigits = data.readInt();
            final int numericCode = data.readInt();
            final String defaultSymbol = data.readUTF();

            final int ii = i;
            assertNotEquals("", currencyCode, () -> "currencyCode for " + ii);
            assertTrue(defaultFractionDigits >= 0, () -> "defaultFractionDigits for " + ii + " currencyCode: " + currencyCode);
            assertNotEquals(0, numericCode, () -> "numericCode for " + ii + " currencyCode: " + currencyCode);
            assertNotEquals("", defaultSymbol, () -> "defaultSymbol for " + ii+ " currencyCode: " + currencyCode);

            final Currency currency = Currency.getInstance(currencyCode);
            assertEquals(currency.getDefaultFractionDigits(), defaultFractionDigits, () -> "defaultFractionDigits for " + ii + " currencyCode: " + currencyCode);
            assertEquals(currency.getNumericCode(), numericCode, () -> "numericCode for " + ii + " currencyCode: " + currencyCode);

            readLocales(data);
            readSymbolToLocales(data);
        }

        assertThrows(EOFException.class, () -> data.readBoolean(), "DataInput be empty should throw EOF");
    }

    private static void readLocales(final DataInput data) throws IOException {
        final int count = data.readInt();
        for (int i = 0; i < count; i++) {
            checkLocale(data.readUTF());
        }
    }

    private static void readSymbolToLocales(final DataInput data) throws IOException {
        final int symbolToLocaleCount = data.readInt();

        for (int i = 0; i < symbolToLocaleCount; i++) {
            final String symbol = data.readUTF();
            assertNotEquals("", symbol, "symbol");

            final int localeCount = data.readInt();
            assertTrue(localeCount >= 0, "localeCount must be >= 0 was " + localeCount);

            for (int j = 0; j < localeCount; j++) {
                checkLocale(data.readUTF());
            }
        }
    }

    private static void checkLocale(final String locale) {
        assertEquals(locale,
                Locale.forLanguageTag(locale).toLanguageTag(),
                "locale languageTag must match");
    }

    @Test
    public void testCurrencyDEWildcard() throws Exception {
        this.generateAndCheck("DE*",
                "",
                "// currencyCode=CHF\n" +
                        "// defaultFractionDigits=2\n" +
                        "// numericCode=756\n" +
                        "// defaultSymbol=CHF\n" +
                        "// locales=de-CH,de-LI\n" +
                        "// \n" +
                        "// currencyCode=EUR\n" +
                        "// defaultFractionDigits=2\n" +
                        "// numericCode=978\n" +
                        "// defaultSymbol=€\n" +
                        "// locales=de-AT,de-BE,de-DE,de-LU\n" +
                        "// EUR=de-CH,de-LI\n" +
                        "// \n" +
                        "\n" +
                        "\n" +
                        "2,CHF,2,756,CHF,2,de-CH,de-LI,0,EUR,2,978,€,4,de-AT,de-BE,de-DE,de-LU,1,EUR,2,de-CH,de-LI");
    }

    @Test
    public void testCurrencyENAU() throws Exception {
        this.generateAndCheck("en-AU",
                "",
                "// currencyCode=AUD\n" +
                        "// defaultFractionDigits=2\n" +
                        "// numericCode=36\n" +
                        "// defaultSymbol=A$\n" +
                        "// locales=en-AU\n" +
                        "// $=en-AU\n" +
                        "// \n" +
                        "\n" +
                        "\n" +
                        "1,AUD,2,36,A$,1,en-AU,1,$,1,en-AU");
    }

    @Test
    public void testCurrencyENNZ() throws Exception {
        this.generateAndCheck("en-NZ",
                "",
                "// currencyCode=NZD\n" +
                        "// defaultFractionDigits=2\n" +
                        "// numericCode=554\n" +
                        "// defaultSymbol=NZ$\n" +
                        "// locales=en-NZ\n" +
                        "// $=en-NZ\n" +
                        "// \n" +
                        "\n" +
                        "\n" +
                        "1,NZD,2,554,NZ$,1,en-NZ,1,$,1,en-NZ");
    }

    @Test
    public void testCurrencyCodeXXX() throws Exception {
        this.generateAndCheck("",
                "XXX",
                "// currencyCode=XXX\n" +
                        "// defaultFractionDigits=-1\n" +
                        "// numericCode=999\n" +
                        "// defaultSymbol=XXX\n" +
                        "// locales=\n" +
                        "// \n" +
                        "\n" +
                        "\n" +
                        "1,XXX,-1,999,XXX,0,0");
    }

    @Test
    public void testCurrencyENNZAndCurrencyCodeXXX() throws Exception {
        this.generateAndCheck("EN-NZ",
                "XXX",
                "// currencyCode=NZD\n" +
                        "// defaultFractionDigits=2\n" +
                        "// numericCode=554\n" +
                        "// defaultSymbol=NZ$\n" +
                        "// locales=en-NZ\n" +
                        "// $=en-NZ\n" +
                        "// \n" +
                        "// currencyCode=XXX\n" +
                        "// defaultFractionDigits=-1\n" +
                        "// numericCode=999\n" +
                        "// defaultSymbol=XXX\n" +
                        "// locales=\n" +
                        "// \n" +
                        "\n" +
                        "\n" +
                        "2,NZD,2,554,NZ$,1,en-NZ,1,$,1,en-NZ,XXX,-1,999,XXX,0,0");
    }

    @Test
    public void testCurrencyWithDuplicate() throws Exception {
        this.generateAndCheck("EN-NZ",
                "NZD",
                "// currencyCode=NZD\n" +
                        "// defaultFractionDigits=2\n" +
                        "// numericCode=554\n" +
                        "// defaultSymbol=NZ$\n" +
                        "// locales=en-NZ\n" +
                        "// $=en-NZ\n" +
                        "// \n" +
                        "\n" +
                        "\n" +
                        "2,NZD,2,554,NZ$,1,en-NZ,1,$,1,en-NZ");
    }

    private void generateAndCheck(final String filter,
                                  final String currencyCode,
                                  final String expected) throws Exception {
        assertEquals(expected,
                generate(filter, currencyCode),
                () -> "filter=" + CharSequences.quoteAndEscape(filter) + " currencyCode=" + CharSequences.quoteAndEscape(currencyCode));
    }

    @Test
    public void testGeneratedCodeWithoutXXX() throws Exception {
        final String generated = generate("*", "");

        assertEquals(false,
                CaseSensitivity.INSENSITIVE.contains(generated, "XXX"),
                () -> "generated code should not contain currency code \"XXX\"\n" + generated);
    }

    @Test
    public void testGeneratedCodeWithoutYYY() throws Exception {
        final String generated = generate("*", "");

        assertEquals(false,
                CaseSensitivity.INSENSITIVE.contains(generated, "YYY"),
                () -> "generated code should not contain currency code \"YYY\"\n" + generated);
    }

    @Test
    public void testGeneratedCodeIncludesAllCurrenciesWithLocalesCurrencyCode() throws Exception {
        final String generated = generate("*", "");

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
                .filter(cc -> false == CaseSensitivity.INSENSITIVE.contains(generated, cc))
                .collect(Collectors.toList());

        assertEquals(Lists.empty(),
                missing,
                () -> "generated code missing the following locales with currency\n" + generated);
    }

    @Test
    public void testGeneratedCodeIncludesLocalesLanguageTagLiterals() throws Exception {
        final String generated = generate("*", "");

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

    private String generate(final String filter,
                            final String currencyCode) throws Exception {
        final StringBuilder comments = new StringBuilder();
        final StringBuilder data = new StringBuilder();
        final LineEnding eol = LineEnding.NL;

        try (final Printer printer = Printers.stringBuilder(comments, eol)) {
            CurrencyProviderTool.generate(filter.isEmpty() ? Sets.empty() : WalkingkookaLanguageTag.all(filter),
                    currencyCode.isEmpty() ? Sets.empty() : Sets.of(currencyCode),
                    StringDataInputDataOutput.output(data::append),
                    CurrencyProviderAnnotationProcessor.comments(printer));
            printer.print(eol);
            printer.flush();
            printer.close();

            return "" + comments + eol + data;
        }
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
