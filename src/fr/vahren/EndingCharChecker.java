package fr.vahren;

public class EndingCharChecker implements EndingChecker<String> {

    private final char end;

    public EndingCharChecker(final char end) {
        this.end = end;
    }

    @Override
    public boolean isEnd(final String t) {
        return t.charAt(t.length() - 1) == this.end;
    }

}
