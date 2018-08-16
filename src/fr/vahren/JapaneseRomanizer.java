package fr.vahren;

import com.moji4j.MojiConverter;

/**
 * Created by fdroumaguet on 10/08/18.
 */

public class JapaneseRomanizer implements Romanizer {

    MojiConverter converter = new MojiConverter();

    @Override
    public String toRoman(final String word) {
        return this.converter.convertKanaToRomaji(word);
    }
}
