package fr.vahren;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MarkovCharGenerator {

    private static final int NB_LINES = 500000;
    private final Map<String, Probas<String>> probas = new HashMap<>();
    private final int range;
    private final char end;
    private final int maxWordLength;
    private final EndingChecker<String> checker;

    public MarkovCharGenerator(final int range, final char end, final int max) {
        if (range < 1) {
            throw new IllegalArgumentException("range must be > 1");
        }
        this.range = range;
        this.end = end;
        this.maxWordLength = max;
        this.checker = new EndingCharChecker(end);
    }

    // TEST
    public static void main(final String[] args) {
        generateFor("res/japanese.txt", "res/japanese.json", 3, '$', 6, new JapaneseRomanizer());
        generateFor("res/korean.txt", "res/korean.json", 3, '$', 5, new KoreanRomanizer());
        generateFor("res/norsk.txt", "res/norsk.json", 4, '$', 12, null);
        generateFor("res/french.txt", "res/french.json", 4, '$', 12, null);
        generateFor("res/english.txt", "res/english.json", 4, '$', 10, null);
        generateFor("res/french.txt", "res/french2.json", 3, '$', 12, null);
        generateFor("res/english.txt", "res/english2.json", 3, '$', 10, null);
        generateFor("res/prenoms.txt", "res/prenoms.json", 2, '$', 8, null);
    }

    private static void generateFor(final String fileName, final String outFileName, final int factor, final char end,
        final int max, final Romanizer romanizer) {
        final MarkovCharGenerator markov = new MarkovCharGenerator(factor, end, max);
        try {
            markov.loadWordsFromFile(fileName);
            final Path path = Paths.get(outFileName);
            int c = 0;
            try (BufferedWriter writer = Files.newBufferedWriter(path)) {
                writer.write("[");
                while (c < NB_LINES) {
                    writer.write("\"");
                    String word = markov.generateWord();
                    if (romanizer != null) {
                        word += " (*" + romanizer.toRoman(word) + "*)";
                    }
                    writer.write(word);
                    if (c != NB_LINES - 1) {
                        writer.write("\",\n");
                    } else {
                        writer.write("\"\n");
                    }
                    c++;
                    if (c % (NB_LINES / 100) == 0) {
                        System.out.print("*");
                    }
                }
                writer.write("]");
            } catch (final IOException e) {
                e.printStackTrace();
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
        System.out.println("done for " + outFileName);
    }

    // OUT
    public String generateWord() {
        // starting with empty token
        String token = "";

        final StringBuffer word = new StringBuffer();
        char nextChar = '\0';
        // only stop when reaching end character or max length
        while (true) {
            // get next char chain
            token = this.probas.get(token).random(word.length() > this.maxWordLength);

            // get the last char
            nextChar = token.charAt(token.length() - 1);

            if (nextChar == this.end) {
                // end char, we stop!
                break;
            }

            // append it to the word
            word.append(nextChar);
        }

        return word.toString();
    }

    // IN
    public void loadWord(final String w) {
        // get chars from string and append the end char at then end
        final char[] chars = new char[w.length() + 1];
        w.getChars(0, w.length(), chars, 0);
        chars[chars.length - 1] = this.end;

        final char[] charChain = new char[this.range];
        String currentToken = "";
        String nextToken = "";

        for (final char c : chars) {
            addToCharChain(c, charChain);
            nextToken = new String(charChain);
            final Probas<String> p = this.probas.get(currentToken);
            if (p == null) {
                // new proba
                this.probas.put(currentToken, new Probas<>(nextToken, this.checker));
            } else {
                p.addValue(nextToken);
            }
            currentToken = nextToken;
        }
    }

    public void loadWords(final String... words) {
        for (final String w : words) {
            loadWord(w);
        }
    }

    public void loadWords(final Collection<String> words) {
        for (final String w : words) {
            loadWord(w);
        }
    }

    public void loadWordsFromFile(final String fileName) throws IOException {
        loadWords(readResource(fileName));
    }

    private List<String> readResource(final String fileName) throws IOException {
        final BufferedReader reader = new BufferedReader(new FileReader(fileName));
        final List<String> s = new LinkedList<>();
        String readLine;
        while ((readLine = reader.readLine()) != null) {
            s.add(readLine);
        }
        reader.close();
        return s;
    }

    private void addToCharChain(final char c, final char[] charChain) {
        // shifts left
        for (int i = 0; i < charChain.length - 1; i++) {
            charChain[i] = charChain[i + 1];
        }
        // add at the end
        charChain[charChain.length - 1] = c;
    }

}
