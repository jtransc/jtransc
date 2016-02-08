/*
 * Copyright 2016 Carlos Ballesteros Velasco
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.text;

import java.math.RoundingMode;
import java.util.Currency;

public class DecimalFormat extends NumberFormat {
    public DecimalFormat() {
    }

    public DecimalFormat(String pattern) {
    }

    public DecimalFormat(String pattern, DecimalFormatSymbols symbols) {
    }

    @Override
    native public final StringBuffer format(Object number, StringBuffer toAppendTo, FieldPosition pos);

    @Override
    native public StringBuffer format(double value, StringBuffer result, FieldPosition fieldPosition);

    @Override
    native public StringBuffer format(long value, StringBuffer result, FieldPosition fieldPosition);

    @Override
    native public AttributedCharacterIterator formatToCharacterIterator(Object obj);

    @Override
    native public Number parse(String text, ParsePosition pos);

    native public DecimalFormatSymbols getDecimalFormatSymbols();

    native public void setDecimalFormatSymbols(DecimalFormatSymbols newSymbols);

    native public String getPositivePrefix();

    native public void setPositivePrefix(String newValue);

    native public String getNegativePrefix();

    native public void setNegativePrefix(String newValue);

    native public String getPositiveSuffix();

    native public void setPositiveSuffix(String newValue);

    native public String getNegativeSuffix();

    native public void setNegativeSuffix(String newValue);

    native public int getMultiplier();

    native public void setMultiplier(int newValue);

    @Override
    native public void setGroupingUsed(boolean newValue);

    native public int getGroupingSize();

    native public void setGroupingSize(int newValue);

    native public boolean isDecimalSeparatorAlwaysShown();

    native public void setDecimalSeparatorAlwaysShown(boolean newValue);

    native public boolean isParseBigDecimal();

    native public void setParseBigDecimal(boolean newValue);

    @Override
    native public Object clone();

    @Override
    native public boolean equals(Object obj);

    @Override
    native public int hashCode();

    native public String toPattern();

    native public String toLocalizedPattern();

    native public void applyPattern(String pattern);

    native public void applyLocalizedPattern(String pattern);

    @Override
    native public void setMaximumIntegerDigits(int newValue);

    @Override
    native public void setMinimumIntegerDigits(int newValue);

    @Override
    native public void setMaximumFractionDigits(int newValue);

    @Override
    native public void setMinimumFractionDigits(int newValue);

    @Override
    native public int getMaximumIntegerDigits();

    @Override
    native public int getMinimumIntegerDigits();

    @Override
    native public int getMaximumFractionDigits();

    @Override
    native public int getMinimumFractionDigits();

    @Override
    native public Currency getCurrency();

    @Override
    native public void setCurrency(Currency currency);

    @Override
    native public RoundingMode getRoundingMode();

    @Override
    native public void setRoundingMode(RoundingMode roundingMode);
}
