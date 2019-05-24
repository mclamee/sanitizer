package com.mclamee.tools.sanitizer.util;

import java.util.Arrays;
import java.util.regex.Pattern;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * The util to sanitize the white spaces in the user inputs
 */
public class WhiteSpaceUtil {
    private static Pattern lineBreaksRegexPattern = Pattern.compile("[\\n\\r\\v]+");
    private static Pattern emptyRegexStrPattern = Pattern.compile("[" + SpecHtmlCharacterEnum.emptyAppearanceRegexStr() + "]+");
    private static Pattern spaceRegexStrPattern = Pattern.compile("[\\f\\t" + SpecHtmlCharacterEnum.spaceAppearanceRegexStr() + "]+");

    /**
     * To sanitize string and merge lines
     *
     * @param str the input string
     */
    public static String sanitize(String str) {
        return sanitize(str, true);
    }

    /**
     * To sanitize string and merge lines
     *
     * @param str               the input string
     * @param mergeLinesBySpace merge indicator
     */
    public static String sanitize(String str, boolean mergeLinesBySpace) {
        if (StringUtils.isEmpty(str)) {
            return str;
        }
        StringBuilder sb = new StringBuilder();
        String[] splitList = lineBreaksRegexPattern.split(str);
        if (splitList != null && splitList.length > 0) {
            for (int i = 0; i < splitList.length; i++) {
                String splitStr = splitList[i];
                String subStr = sanitizeLine(splitStr);
                if (i > 0) {
                    sb.append(mergeLinesBySpace ? " " : System.lineSeparator());
                }
                sb.append(subStr);
            }
        }
        return sb.toString();
    }

    /**
     * To sanitize single string line
     *
     * @param str the input string
     */
    public static String sanitizeLine(String str) {
        if (StringUtils.isEmpty(str)) {
            return str;
        }

        // replace all space-like chars to ASCII#32(space)
        String spaced = spaceRegexStrPattern.matcher(str).replaceAll(" ");

        // delete all empty chars
        String sanitized = emptyRegexStrPattern.matcher(spaced).replaceAll("");

        // trim spaces
        return StringUtils.trim(sanitized);
    }

    /**
     * The enum Spec html character enum.
     */
    @AllArgsConstructor
    @Getter
    public enum SpecHtmlCharacterEnum {
        /**
         * The Character tabulation.
         */
        CHARACTER_TABULATION("character tabulation", "&Tab;", "&#9;", "\\u0009", "empty"),
        /**
         * The Line feed.
         */
        LINE_FEED("line feed", "&NewLine;", "&#10;", "\\u000A", "empty"),
        /**
         * The Line tabulation.
         */
        LINE_TABULATION("line tabulation", null, "&#11;", "\\u000B", "empty"),
        /**
         * The Form feed.
         */
        FORM_FEED("form feed", null, "&#12;", "\\u000C", "empty"),
        /**
         * The Carriage return.
         */
        CARRIAGE_RETURN("carriage return", null, "&#13;", "\\u000D", "empty"),
        /**
         * The Next line.
         */
        NEXT_LINE("next line", null, "&#133;", "\\u0085", "empty"),

        /**
         * Space spec html character enum.
         */
        SPACE("space", null, "&#32;", "\\u0020", "space"),
        /**
         * The Non breaking space.
         */
        NON_BREAKING_SPACE("Non-Breaking Space", "&nbsp;", "&#160;", "\\u00A0", "space"),
        /**
         * The Ogham space mark.
         */
        OGHAM_SPACE_MARK("ogham space mark", null, "&#5760;", "\\u1680", "space"),
        /**
         * The En quad.
         */
        EN_QUAD("En Quad", null, "&#8192;", "\\u2000", "space"),
        /**
         * The Em quad.
         */
        EM_QUAD("Em Quad", null, "&#8193;", "\\u2001", "space"),
        /**
         * The En space.
         */
        EN_SPACE("En Space", "&ensp;", "&#8194;", "\\u2002", "space"),
        /**
         * The Em space.
         */
        EM_SPACE("Em Space", "&emsp;", "&#8195;", "\\u2003", "space"),
        /**
         * The Three per em space.
         */
        THREE_PER_EM_SPACE("Three-Per-Em Space", "&emsp13;", "&#8196;", "\\u2004", "space"),
        /**
         * The Four per em space.
         */
        FOUR_PER_EM_SPACE("Four-Per-Em Space", "&emsp14;", "&#8197;", "\\u2005", "space"),
        /**
         * The Six per em space.
         */
        SIX_PER_EM_SPACE("Six-Per-Em Space", null, "&#8198;", "\\u2006", "space"),
        /**
         * The Figure space.
         */
        FIGURE_SPACE("Figure Space", "&numsp;", "&#8199;", "\\u2007", "space"),
        /**
         * The Punctuation space.
         */
        PUNCTUATION_SPACE("Punctuation Space", "&puncsp;", "&#8200;", "\\u2008", "space"),
        /**
         * The Thin space.
         */
        THIN_SPACE("Thin Space", "&thinsp;", "&#8201;", "\\u2009", "space"),
        /**
         * The Hair space.
         */
        HAIR_SPACE("Hair Space", "&hairsp;", "&#8202;", "\\u200A", "space"),
        /**
         * The Line separator.
         */
        LINE_SEPARATOR("line separator", null, "&#8232;", "\\u2028", "space"),
        /**
         * The Paragraph separator.
         */
        PARAGRAPH_SEPARATOR("paragraph separator", null, "&#8233;", "\\u2029", "space"),
        /**
         * The Narrow no break space.
         */
        NARROW_NO_BREAK_SPACE("paragraph separator", null, "&#8239;", "\\u202F", "space"),
        /**
         * The Medium mathematical space.
         */
        MEDIUM_MATHEMATICAL_SPACE("paragraph separator", "&MediumSpace;", "&#8287;", "\\u205F", "space"),
        /**
         * The Ideographic space.
         */
        IDEOGRAPHIC_SPACE("ideographic space", null, "&#12288;", "\\u3000", "space"),

        /**
         * The Mongolian vowel separator.
         */
        MONGOLIAN_VOWEL_SEPARATOR("mongolian vowel separator", null, "&#6158;", "\\u180E", "empty"),
        /**
         * The Zero width space.
         */
        ZERO_WIDTH_SPACE("Zero-Width Space", "&NegativeMediumSpace;", "&#8203;", "\\u200B", "empty"),
        /**
         * The Zero width non joiner.
         */
        ZERO_WIDTH_NON_JOINER("Zero Width Non-Joiner", "&zwnj;", "&#8204;", "\\u200C", "empty"),
        /**
         * The Zero width joiner.
         */
        ZERO_WIDTH_JOINER("Zero Width Joiner", "&zwj;", "&#8205;", "\\u200D", "empty"),
        /**
         * The Word joiner.
         */
        WORD_JOINER("word joiner", null, "&#8288;", "\\u2060", "empty"),
        /**
         * The Zero width non breaking space.
         */
        ZERO_WIDTH_NON_BREAKING_SPACE("zero width non-breaking space", null, "&#65279;", "\\uFEFF", "empty"),
        /**
         * The Left to right mark.
         */
        LEFT_TO_RIGHT_MARK("Left-To-Right Mark", null, "&#8206;", "\\u200E", "empty"),
        /**
         * The Right to left mark.
         */
        RIGHT_TO_LEFT_MARK("Right-To-Left Mark", null, "&#8207;", "\\u200F", "empty");

        /**
         * The Name.
         */
        String name;
        /**
         * The Entity.
         */
        String entity;
        /**
         * The Entity code.
         */
        String entityCode;
        /**
         * The Unicode regex.
         */
        String unicodeRegex;
        /**
         * The Appearance.
         */
        String appearance;

        /**
         * Space appearance regex str string.
         *
         * @return the string
         */
        public static String spaceAppearanceRegexStr() {
            StringBuilder stringBuilder = new StringBuilder();
            Arrays.stream(SpecHtmlCharacterEnum.values()).filter(i -> "space".equals(i.getAppearance())).forEach(i -> stringBuilder.append(i.getUnicodeRegex()));
            return stringBuilder.toString();
        }

        /**
         * Empty appearance regex str string.
         *
         * @return the string
         */
        public static String emptyAppearanceRegexStr() {
            StringBuilder stringBuilder = new StringBuilder();
            Arrays.stream(SpecHtmlCharacterEnum.values()).filter(i -> "empty".equals(i.getAppearance())).forEach(i -> stringBuilder.append(i.getUnicodeRegex()));
            return stringBuilder.toString();
        }
    }

}


