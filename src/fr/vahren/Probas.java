package fr.vahren;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class Probas<T> {

    private int sum;
    private final Map<T, Integer> possibleValues = new HashMap<>();
    private final EndingChecker<T> endingChecker;

    // when building a proba at least one value must be provided
    public Probas(final T firstPossibleValue, final EndingChecker<T> endingChecker) {
        this.endingChecker = endingChecker;
        addValue(firstPossibleValue);
    }

    // OUT
    public T random(final boolean endIfPossible) {
        // the word needs to end, get the first token that is valid
        if (endIfPossible) {
            for (final Entry<T, Integer> e : this.possibleValues.entrySet()) {
                if (this.endingChecker.isEnd(e.getKey())) {
                    return e.getKey();
                }
            }
        }

        // random int between 0 and sum
        final int r = (int) Math.floor(Math.random() * this.sum);

        int cumul = 0;
        for (final Entry<T, Integer> e : this.possibleValues.entrySet()) {
            cumul += e.getValue();
            if (r < cumul) {
                return e.getKey();
            }
        }
        // should not happen
        return null;
    }

    // IN
    public void addValue(final T value) {
        final Integer p = this.possibleValues.get(value);
        if (p != null) {
            // add
            this.possibleValues.put(value, p + 1);
        } else {
            // new
            this.possibleValues.put(value, 1);
        }
        // increment global sum
        this.sum++;
    }

    public void addValues(final T... values) {
        for (final T value : values) {
            addValue(value);
        }
    }

    public void addValues(final Collection<T> values) {
        for (final T value : values) {
            addValue(value);
        }
    }

}
