package com.zzhoujay.html;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.AlignmentSpan;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.ParagraphStyle;
import android.text.style.RelativeSizeSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.SubscriptSpan;
import android.text.style.SuperscriptSpan;
import android.text.style.TextAppearanceSpan;
import android.text.style.TypefaceSpan;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;

import com.zzhoujay.html.style.ZBulletSpan;
import com.zzhoujay.html.style.ZCodeBlockSpan;
import com.zzhoujay.html.style.ZCodeSpan;
import com.zzhoujay.html.style.ZIndentSpan;
import com.zzhoujay.html.style.ZQuoteSpan;

import org.ccil.cowan.tagsoup.Parser;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zhou on 2018/3/11.
 * HtmlToSpannedConverter
 */

@SuppressWarnings("unused")
class MamaHtmlToSpannedConverter implements ContentHandler {

    private static final String TAG = "HtmlToSpannedConverter";

    private static final float[] HEADING_SIZES = {
            1.5f, 1.4f, 1.3f, 1.2f, 1.1f, 1f,
    };
    /**
     * Name-value mapping of HTML/CSS colors which have different values in {@link Color}.
     */
    private static final Map<String, Integer> sColorMap;
    private static Pattern sTextAlignPattern;
    private static Pattern sForegroundColorPattern;
    private static Pattern sBackgroundColorPattern;
    private static Pattern sTextDecorationPattern;
    private static Pattern sRgbColorPattern;
    private static Pattern sArgbColorPattern;
    private static Pattern sHexColorPattern;
    private static Pattern sTextIndentPattern;
    private static Pattern sFontSizePattern;

    static {
        sColorMap = new HashMap<>();
        sColorMap.put("darkgray", 0xFFA9A9A9);
        sColorMap.put("gray", 0xFF808080);
        sColorMap.put("lightgray", 0xFFD3D3D3);
        sColorMap.put("darkgrey", 0xFFA9A9A9);
        sColorMap.put("grey", 0xFF808080);
        sColorMap.put("lightgrey", 0xFFD3D3D3);
        sColorMap.put("green", 0xFF008000);
    }

    private String mSource;
    private XMLReader mReader;
    private SpannableStringBuilder mSpannableStringBuilder;
    private android.text.Html.ImageGetter mImageGetter;
    private android.text.Html.TagHandler mTagHandler;
    private int mFlags;
    private boolean mCodeStart;
    private boolean mPreStart;

    MamaHtmlToSpannedConverter(String source, android.text.Html.ImageGetter imageGetter,
                               android.text.Html.TagHandler tagHandler, Parser parser, int flags) {
        mSource = source;
        mSpannableStringBuilder = new SpannableStringBuilder();
        mImageGetter = imageGetter;
        mTagHandler = tagHandler;
        mReader = parser;
        mFlags = flags;
    }

    private static Pattern getArgbColorPattern() {
        if (sArgbColorPattern == null) {
            sArgbColorPattern = Pattern.compile("^\\s*rgba\\(\\s*(\\d{1,3})\\s*,\\s*(\\d{1,3})\\s*,\\s*(\\d{1,3})\\s*,\\s*([\\d.]+)\\b");
        }
        return sArgbColorPattern;
    }

    private static Pattern getHexColorPattern() {
        if (sHexColorPattern == null) {
            sHexColorPattern = Pattern.compile("^\\s*(#[A-Za-z0-9]{6,8})");
        }
        return sHexColorPattern;
    }

    private static Pattern getRgbColorPattern() {
        if (sRgbColorPattern == null) {
            sRgbColorPattern = Pattern.compile("^\\s*rgb\\(\\s*(\\d{1,3})\\s*,\\s*(\\d{1,3})\\s*,\\s*(\\d{1,3})\\b");
        }
        return sRgbColorPattern;
    }

    private static Pattern getTextAlignPattern() {
        if (sTextAlignPattern == null) {
            sTextAlignPattern = Pattern.compile("(?:\\s+|\\A|;\\s*)text-align\\s*:\\s*(\\S*)\\b");
        }
        return sTextAlignPattern;
    }

    private static Pattern getForegroundColorPattern() {
        if (sForegroundColorPattern == null) {
            sForegroundColorPattern = Pattern.compile(
                    "(?:\\s+|\\A|;\\s*)color\\s*:\\s*(.*)\\b");
        }
        return sForegroundColorPattern;
    }

    private static Pattern getBackgroundColorPattern() {
        if (sBackgroundColorPattern == null) {
            sBackgroundColorPattern = Pattern.compile(
                    "(?:\\s+|\\A|;\\s*)background(?:-color)?\\s*:\\s*(.*)\\b");
        }
        return sBackgroundColorPattern;
    }


    private static Pattern getFontSizePattern() {
        if (sFontSizePattern == null) {
            sFontSizePattern = Pattern.compile(
                    "(?:\\s+|\\A|;\\s*)font-size\\s*:\\s*(.*)\\b");
        }
        return sFontSizePattern;
    }

    private static Pattern getTextDecorationPattern() {
        if (sTextDecorationPattern == null) {
            sTextDecorationPattern = Pattern.compile(
                    "(?:\\s+|\\A|;\\s*)text-decoration(?:-line)?\\s*:\\s*(\\S*)\\b");
        }
        return sTextDecorationPattern;
    }

    private static Pattern getTextIndentPattern() {
        if (sTextIndentPattern == null) {
            sTextIndentPattern = Pattern.compile(
                    "(?:\\s+|\\A|;\\s*)text-indent\\s*:\\s*(\\d*)px\\b"
            );
        }
        return sTextIndentPattern;
    }

    private static void appendNewlines(Editable text, int minNewline) {
        final int len = text.length();

        if (len == 0) {
            return;
        }

        int existingNewlines = 0;
        for (int i = len - 1; i >= 0 && text.charAt(i) == '\n'; i--) {
            existingNewlines++;
        }

        for (int j = existingNewlines; j < minNewline; j++) {
            text.append("\n");
        }
    }


    private static void startBlockElement(Editable text, Attributes attributes, int margin) {
        startBlockElement(text, attributes, margin, false);
    }
    private static void startBlockElement(Editable text, Attributes attributes, int margin, boolean isParagraph) {
        final int len = text.length();
        if (margin > 0 && !isParagraph) {
            appendNewlines(text, margin);
            start(text, new Newline(margin));
        }
        //<p>标签单独处理
        if(isParagraph){
            appendNewlines(text, 1);
        }

        String style = attributes.getValue("", "style");
        if (style != null) {
            Matcher m = getTextAlignPattern().matcher(style);
            if (m.find()) {
                String alignment = m.group(1);
                if (alignment.equalsIgnoreCase("start")) {
                    start(text, new Alignment(Layout.Alignment.ALIGN_NORMAL));
                } else if (alignment.equalsIgnoreCase("center")) {
                    start(text, new Alignment(Layout.Alignment.ALIGN_CENTER));
                } else if (alignment.equalsIgnoreCase("end")) {
                    start(text, new Alignment(Layout.Alignment.ALIGN_OPPOSITE));
                }
            }

            m = getTextIndentPattern().matcher(style);
            if (m.find()) {
                String textIndent = m.group(1);
                try {
                    int tab = Integer.valueOf(textIndent);
                    start(text, new Indent(tab));
                } catch (NumberFormatException ignore) {
                }
            }
        }
    }

    private static void endBlockElement(Editable text) {
        Newline n = getLast(text, Newline.class);
        if (n != null) {
            appendNewlines(text, n.mNumNewlines);
            text.removeSpan(n);
        }

        Indent indent = getLast(text, Indent.class);
        if (indent != null) {
            setSpanFromMark(text, indent, new ZIndentSpan(indent.mIndentSize));
        }

        Alignment a = getLast(text, Alignment.class);
        if (a != null) {
            setSpanFromMark(text, a, new AlignmentSpan.Standard(a.mAlignment));
        }
    }

    private static void handleBr(Editable text) {
        text.append('\n');
    }

    private static void endLi(Editable text) {
        endCssStyle(text);
        endBlockElement(text);
        end(text, Bullet.class, new ZBulletSpan());
    }

    private static void endBlockquote(Editable text) {
        endBlockElement(text);
        end(text, Blockquote.class, new ZQuoteSpan());
    }

    private static void endHeading(Editable text) {
        // RelativeSizeSpan and StyleSpan are CharacterStyles
        // Their ranges should not include the newlines at the end
        Heading h = getLast(text, Heading.class);
        if (h != null) {
            setSpanFromMark(text, h, new RelativeSizeSpan(HEADING_SIZES[h.mLevel]),
                    new StyleSpan(Typeface.BOLD));
        }

        endBlockElement(text);
    }

    /**
     * 返回最后一个span
     * */
    private static <T> T getLast(Spanned text, Class<T> kind) {
        T[] objs = text.getSpans(0, text.length(), kind);

        if (objs.length == 0) {
            return null;
        } else {
            return objs[objs.length - 1];
        }
    }


    /**
     * 设置span属性
     * */
    private static void setSpanFromMark(Spannable text, Object mark, Object... spans) {
        int where = text.getSpanStart(mark);
        int len = text.length();
        boolean isHandle = false;
//        boolean needRemove = true;
        // 设置完颜色后，移除自添加的前景色
//        if (mark instanceof Foreground) {
//            if (((Foreground) mark).isStub) {
//                needRemove = false;
//            }
//        }
        // 获取当前区间所有前景Span
//        Foreground[] objs1 = text.getSpans(0, text.length(), Foreground.class);
//        if (needRemove) {
            text.removeSpan(mark);
//        }
        // step1：获取所有span的起始结束位置
        if (mark instanceof Foreground) {
            ForegroundColorSpan[] objsTest = text.getSpans(where, text.length(), ForegroundColorSpan.class);
            List<String> spanIndex = new ArrayList();
            boolean[] stateArray = new boolean[text.length()];
            for (Object objTest : objsTest) {
                int startTest = text.getSpanStart(objTest);
                int endTest = text.getSpanEnd(objTest);
                spanIndex.add(startTest + "-" + endTest);
                // Log.i("test", "startTest=" + startTest + " endTest=" + endTest);
            }
            // step2：将带有前景色的区间位置记录下来
            if (spanIndex.size() > 0) {
                List<String> resultArray = new ArrayList();
                int tmpStart = where;
                for (int i = 0; i < spanIndex.size(); i++) {
                    String[] posArray = spanIndex.get(i).split("-");
                    //  重新计算包含关系
                    int start = Integer.valueOf(posArray[0]);
                    int end = Integer.valueOf(posArray[1]);
                    // 将span区间置为选中状态
                    for (int j = start; j < end; j++) {
                        stateArray[j] = true;
                    }
                }
                // step3：遍历区间数组,对未赋值的区间进行处理
                int start = 0;
                int end;
                boolean hasStart = false;
                for (int i = where; i < stateArray.length; i++) {
                    if (!stateArray[i] && (i != stateArray.length - 1)) {
                        //找到span初始位置
                        if (!hasStart) {
                            start = i;
                            hasStart = true;
                        }
                    } else {
                        if (hasStart) {
                            // 对span结束位置做边缘处理
                            if ((i == stateArray.length - 1) && !stateArray[i]) {
                                end = i + 1;
                            } else {
                                end = i;
                            }
                            hasStart = false;
                            // 颜色赋值，过滤空区间，注意span不能复用需重建
                            if (start != end) {
                                for (Object span : spans) {
                                    text.setSpan(new ForegroundColorSpan(((Foreground) mark).mForegroundColor),
                                            start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                }
                            }
                        }
                    }
                }
                // 已处理标识，不再处理
                isHandle = true;
            }
        }
        // step4: 未处理的情况沿用之前处理方式
        if (where != len && !isHandle) {
            for (Object span : spans) {
                text.setSpan(span, where, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    private static void start(Editable text, Object mark) {
        int len = text.length();
        text.setSpan(mark, len, len, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
    }

    private static void end(Editable text, Class kind, Object repl) {
        int len = text.length();
        Object obj = getLast(text, kind);
        if (obj != null) {
            setSpanFromMark(text, obj, repl);
        }
    }

    private static void endCodeBlock(Editable text) {
        int len = text.length();
        Code code = getLast(text, Code.class);
        if (code != null) {
            int spanStart = text.getSpanStart(code);
            int spanEnd = text.length();
            CharSequence codeContent = text.subSequence(spanStart, spanEnd);
            text.removeSpan(code);
            text.setSpan(new ZCodeBlockSpan(codeContent), spanStart, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            URLSpan urlSpan = new URLSpan("code://" + codeContent);
            text.setSpan(urlSpan, spanStart, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            text.replace(spanStart, spanEnd, "${code}\n");
        }
    }

    private static void endCssStyle(Editable text) {
        Underline u = getLast(text, Underline.class);
        if (u != null) {
            setSpanFromMark(text, u, new UnderlineSpan());
        }
        Strikethrough s = getLast(text, Strikethrough.class);
        if (s != null) {
            setSpanFromMark(text, s, new StrikethroughSpan());
        }

        Background b = getLast(text, Background.class);
        if (b != null) {
            setSpanFromMark(text, b, new BackgroundColorSpan(b.mBackgroundColor));
        }

        Foreground f = getLast(text, Foreground.class);
        if (f != null) {
            setSpanFromMark(text, f, new ForegroundColorSpan(f.mForegroundColor));
        }

        FontSize fs = getLast(text, FontSize.class);
        if (fs != null) {
            //setSpanFromMark(text, fs, new TextAppearanceSpan(null, Typeface.NORMAL, fs.fontSize, null, null));
            //setSpanFromMark(text, fs, new RelativeSizeSpan(2.0f));
            setSpanFromMark(text, fs, new AbsoluteSizeSpan(fs.fontSize));
        }
    }

    private static void startImg(Editable text, Attributes attributes, android.text.Html.ImageGetter img) {
        String src = attributes.getValue("", "src");
        Drawable d = null;

        if (img != null) {
            d = img.getDrawable(src);
        }

        if (d == null) {
            d = new ColorDrawable(Color.LTGRAY);
            d.setBounds(0, 0, 100, 100);
//            d = Resources.getSystem().
//                    getDrawable(com.android.internal.R.drawable.unknown_image);
//            d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
        }

        int len = text.length();
        text.append("\uFFFC");

        text.setSpan(new ImageSpan(d, src), len, text.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private static void endFont(Editable text) {
        Font font = getLast(text, Font.class);
        if (font != null) {
            setSpanFromMark(text, font, new TypefaceSpan(font.mFace));
        }

        Foreground foreground = getLast(text, Foreground.class);
        if (foreground != null) {
            setSpanFromMark(text, foreground,
                    new ForegroundColorSpan(foreground.mForegroundColor));
        }
    }

    private static void startA(Editable text, Attributes attributes) {
        String href = attributes.getValue("", "href");
        start(text, new Href(href));
    }

    private static void endA(Editable text) {
        Href h = getLast(text, Href.class);
        if (h != null) {
            if (h.mHref != null) {
                setSpanFromMark(text, h, new URLSpan((h.mHref)));
            }
        }
    }

    Spanned convert() {

        mReader.setContentHandler(this);
        try {
            mReader.parse(new InputSource(new StringReader(mSource)));
        } catch (IOException e) {
            // We are reading from a string. There should not be IO problems.
            throw new RuntimeException(e);
        } catch (SAXException e) {
            // TagSoup doesn't throw parse exceptions.
            throw new RuntimeException(e);
        }

        // Fix flags and range for paragraph-type markup.
        Object[] obj = mSpannableStringBuilder.getSpans(0, mSpannableStringBuilder.length(), ParagraphStyle.class);
        for (Object anObj : obj) {
            int start = mSpannableStringBuilder.getSpanStart(anObj);
            int end = mSpannableStringBuilder.getSpanEnd(anObj);

            // If the last line of the range is blank, back off by one.
            if (end - 2 >= 0) {
                if (mSpannableStringBuilder.charAt(end - 1) == '\n' &&
                        mSpannableStringBuilder.charAt(end - 2) == '\n') {
                    end--;
                }
            }

            if (end == start) {
                mSpannableStringBuilder.removeSpan(anObj);
            } else {
                mSpannableStringBuilder.setSpan(anObj, start, end, Spannable.SPAN_PARAGRAPH);
            }
        }
        // 如果内容最后以\n\n结尾，那么去除一个，避免底部出现大部分空白
//        if (mSpannableStringBuilder.length() > 2) {
//            if (mSpannableStringBuilder.charAt(mSpannableStringBuilder.length()-1) == '\n' &&
//                    mSpannableStringBuilder.charAt(mSpannableStringBuilder.length() - 2) == '\n') {
//                mSpannableStringBuilder.delete(mSpannableStringBuilder.length() - 1, mSpannableStringBuilder.length());
//            }
//        }
        return mSpannableStringBuilder;
    }

    private void handleStartTag(String tag, Attributes attributes) {
        //noinspection StatementWithEmptyBody
        if (tag.equalsIgnoreCase("br")) {
            // We don't need to handle this. TagSoup will ensure that there's a </br> for each <br>
            // so we can safely emit the linebreaks when we handle the close tag.
        } else if (tag.equalsIgnoreCase("p")) {
            startBlockElement(mSpannableStringBuilder, attributes, getMarginParagraph(),true);
            startCssStyle(mSpannableStringBuilder, attributes);
//        } else if (tag.equalsIgnoreCase("ul")) {
//            startBlockElement(mSpannableStringBuilder, attributes, getMarginList());
//            startCssStyle(mSpannableStringBuilder, attributes);
//        } else if (tag.equalsIgnoreCase("li")) {
//            startLi(mSpannableStringBuilder, attributes);
        } else if (tag.equalsIgnoreCase("div")) {
            startBlockElement(mSpannableStringBuilder, attributes, getMarginDiv());
            startCssStyle(mSpannableStringBuilder, attributes);
        } else if (tag.equalsIgnoreCase("span")) {
            startCssStyle(mSpannableStringBuilder, attributes);
        } else if (tag.equalsIgnoreCase("strong")) {
            start(mSpannableStringBuilder, new Bold());
        } else if (tag.equalsIgnoreCase("b")) {
            start(mSpannableStringBuilder, new Bold());
        } else if (tag.equalsIgnoreCase("em")) {
            start(mSpannableStringBuilder, new Italic());
        } else if (tag.equalsIgnoreCase("cite")) {
            start(mSpannableStringBuilder, new Italic());
        } else if (tag.equalsIgnoreCase("dfn")) {
            start(mSpannableStringBuilder, new Italic());
        } else if (tag.equalsIgnoreCase("i")) {
            start(mSpannableStringBuilder, new Italic());
        } else if (tag.equalsIgnoreCase("big")) {
            start(mSpannableStringBuilder, new Big());
        } else if (tag.equalsIgnoreCase("small")) {
            start(mSpannableStringBuilder, new Small());
        } else if (tag.equalsIgnoreCase("font")) {
            startFont(mSpannableStringBuilder, attributes);
        } else if (tag.equalsIgnoreCase("blockquote")) {
            startBlockquote(mSpannableStringBuilder, attributes);
        } else if (tag.equalsIgnoreCase("tt")) {
            start(mSpannableStringBuilder, new Monospace());
        } else if (tag.equalsIgnoreCase("a")) {
            startA(mSpannableStringBuilder, attributes);
        } else if (tag.equalsIgnoreCase("u")) {
            start(mSpannableStringBuilder, new Underline());
        } else if (tag.equalsIgnoreCase("del")) {
            start(mSpannableStringBuilder, new Strikethrough());
        } else if (tag.equalsIgnoreCase("s")) {
            start(mSpannableStringBuilder, new Strikethrough());
        } else if (tag.equalsIgnoreCase("strike")) {
            start(mSpannableStringBuilder, new Strikethrough());
        } else if (tag.equalsIgnoreCase("sup")) {
            start(mSpannableStringBuilder, new Super());
        } else if (tag.equalsIgnoreCase("sub")) {
            start(mSpannableStringBuilder, new Sub());
        } else if (tag.length() == 2 &&
                Character.toLowerCase(tag.charAt(0)) == 'h' &&
                tag.charAt(1) >= '1' && tag.charAt(1) <= '6') {
            startHeading(mSpannableStringBuilder, attributes, tag.charAt(1) - '1');
        } else if (tag.equalsIgnoreCase("img")) {
            startImg(mSpannableStringBuilder, attributes, mImageGetter);
        } else if (tag.equalsIgnoreCase("code")) {
            if (mPreStart) {
                appendNewlines(mSpannableStringBuilder, 1);
            }
            start(mSpannableStringBuilder, new Code());
            mCodeStart = true;
        } else if (tag.equalsIgnoreCase("pre")) {
            mPreStart = true;
        } else if (mTagHandler != null) {
            mTagHandler.handleTag(true, tag, mSpannableStringBuilder, mReader);
        }
    }

    private void handleEndTag(String tag) {
        if (tag.equalsIgnoreCase("br")) {
            handleBr(mSpannableStringBuilder);
        } else if (tag.equalsIgnoreCase("p")) {
            endCssStyle(mSpannableStringBuilder);
            endBlockElement(mSpannableStringBuilder);
//        } else if (tag.equalsIgnoreCase("ul")) {
//            endCssStyle(mSpannableStringBuilder);
//            endBlockElement(mSpannableStringBuilder);
//        } else if (tag.equalsIgnoreCase("li")) {
//            endLi(mSpannableStringBuilder);
        } else if (tag.equalsIgnoreCase("div")) {
            endCssStyle(mSpannableStringBuilder);
            endBlockElement(mSpannableStringBuilder);
        } else if (tag.equalsIgnoreCase("span")) {
            endCssStyle(mSpannableStringBuilder);
        } else if (tag.equalsIgnoreCase("strong")) {
            end(mSpannableStringBuilder, Bold.class, new StyleSpan(Typeface.BOLD));
        } else if (tag.equalsIgnoreCase("b")) {
            end(mSpannableStringBuilder, Bold.class, new StyleSpan(Typeface.BOLD));
        } else if (tag.equalsIgnoreCase("em")) {
            end(mSpannableStringBuilder, Italic.class, new StyleSpan(Typeface.ITALIC));
        } else if (tag.equalsIgnoreCase("cite")) {
            end(mSpannableStringBuilder, Italic.class, new StyleSpan(Typeface.ITALIC));
        } else if (tag.equalsIgnoreCase("dfn")) {
            end(mSpannableStringBuilder, Italic.class, new StyleSpan(Typeface.ITALIC));
        } else if (tag.equalsIgnoreCase("i")) {
            end(mSpannableStringBuilder, Italic.class, new StyleSpan(Typeface.ITALIC));
        } else if (tag.equalsIgnoreCase("big")) {
            end(mSpannableStringBuilder, Big.class, new RelativeSizeSpan(1.25f));
        } else if (tag.equalsIgnoreCase("small")) {
            end(mSpannableStringBuilder, Small.class, new RelativeSizeSpan(0.8f));
        } else if (tag.equalsIgnoreCase("font")) {
            endFont(mSpannableStringBuilder);
        } else if (tag.equalsIgnoreCase("blockquote")) {
            endBlockquote(mSpannableStringBuilder);
        } else if (tag.equalsIgnoreCase("tt")) {
            end(mSpannableStringBuilder, Monospace.class, new TypefaceSpan("monospace"));
        } else if (tag.equalsIgnoreCase("a")) {
            endA(mSpannableStringBuilder);
        } else if (tag.equalsIgnoreCase("u")) {
            end(mSpannableStringBuilder, Underline.class, new UnderlineSpan());
        } else if (tag.equalsIgnoreCase("del")) {
            end(mSpannableStringBuilder, Strikethrough.class, new StrikethroughSpan());
        } else if (tag.equalsIgnoreCase("s")) {
            end(mSpannableStringBuilder, Strikethrough.class, new StrikethroughSpan());
        } else if (tag.equalsIgnoreCase("strike")) {
            end(mSpannableStringBuilder, Strikethrough.class, new StrikethroughSpan());
        } else if (tag.equalsIgnoreCase("sup")) {
            end(mSpannableStringBuilder, Super.class, new SuperscriptSpan());
        } else if (tag.equalsIgnoreCase("sub")) {
            end(mSpannableStringBuilder, Sub.class, new SubscriptSpan());
        } else if (tag.length() == 2 &&
                Character.toLowerCase(tag.charAt(0)) == 'h' &&
                tag.charAt(1) >= '1' && tag.charAt(1) <= '6') {
            endHeading(mSpannableStringBuilder);
        } else if (tag.equalsIgnoreCase("code")) {
            if (mPreStart) {
                endCodeBlock(mSpannableStringBuilder);
            } else {
                end(mSpannableStringBuilder, Code.class, new ZCodeSpan());
            }
            mCodeStart = false;
        } else if (tag.equalsIgnoreCase("pre")) {
            mPreStart = false;
        } else if (mTagHandler != null) {
            mTagHandler.handleTag(false, tag, mSpannableStringBuilder, mReader);
        }
    }

    private int getMarginParagraph() {
        return getMargin(Html.FROM_HTML_SEPARATOR_LINE_BREAK_PARAGRAPH);
    }

    private int getMarginHeading() {
        return getMargin(Html.FROM_HTML_SEPARATOR_LINE_BREAK_HEADING);
    }

    private int getMarginListItem() {
        return getMargin(Html.FROM_HTML_SEPARATOR_LINE_BREAK_LIST_ITEM);
    }

    private int getMarginList() {
        return getMargin(Html.FROM_HTML_SEPARATOR_LINE_BREAK_LIST);
    }

    private int getMarginDiv() {
        return getMargin(Html.FROM_HTML_SEPARATOR_LINE_BREAK_DIV);
    }

    private int getMarginBlockquote() {
        return getMargin(Html.FROM_HTML_SEPARATOR_LINE_BREAK_BLOCKQUOTE);
    }

    /**
     * Returns the minimum number of newline characters needed before and after a given block-level
     * element.
     *
     * @param flag the corresponding option flag defined in {@link android.text.Html} of a block-level element
     */
    private int getMargin(int flag) {
        if ((flag & mFlags) != 0) {
            return 1;
        }
        return 2;
    }

    private void startLi(Editable text, Attributes attributes) {
        startBlockElement(text, attributes, getMarginListItem());
        start(text, new Bullet());
        startCssStyle(text, attributes);
    }

    private void startBlockquote(Editable text, Attributes attributes) {
        startBlockElement(text, attributes, getMarginBlockquote());
        start(text, new Blockquote());
    }

    private void startHeading(Editable text, Attributes attributes, int level) {
        startBlockElement(text, attributes, getMarginHeading());
        start(text, new Heading(level));
    }

    private void startCssStyle(Editable text, Attributes attributes) {
        String style = attributes.getValue("", "style");
        if (style != null) {
            Matcher m = getForegroundColorPattern().matcher(style);
            if (m.find()) {
                int c = getHtmlColor(m.group(1));
                if (c != -1) {
                    start(text, new Foreground(c));
                }
            }else{
                //无匹配标签添加样式
                Foreground foreground = getLast(text, Foreground.class);
                if(foreground!=null){
                    Foreground newForeground = new Foreground(foreground.mForegroundColor);
                    newForeground.isStub = true;
                    start(text, newForeground);
                }
            }

            m = getBackgroundColorPattern().matcher(style);
            if (m.find()) {
                int c = getHtmlColor(m.group(1));
                if (c != -1) {
                    start(text, new Background(c));
                }
            }

            m = getTextDecorationPattern().matcher(style);
            if (m.find()) {
                String textDecoration = m.group(1);
                int i = textDecoration.indexOf(';');
                if (i > 0) {
                    textDecoration = textDecoration.substring(0, i).trim();
                }
                if (textDecoration.equalsIgnoreCase("line-through")) {
                    start(text, new Strikethrough());
                } else if (textDecoration.equalsIgnoreCase("underline")) {
                    start(text, new Underline());
                }
            }

            m = getFontSizePattern().matcher(style);
            if (m.find()) {
                String fontSize = m.group(1);
                int size = Integer.valueOf(fontSize.replace("px",""));
                start(text, new FontSize(size));
                //start(text, new TextAppearanceSpan(null, Typeface.NORMAL, size, null, null));
            }

        }else{
            //无匹配标签添加样式
            Foreground foreground = getLast(text, Foreground.class);
            if(foreground!=null) {
                Foreground newForeground = new Foreground(foreground.mForegroundColor);
                newForeground.isStub = true;
                start(text, newForeground);
            }
        }
    }

    private void startFont(Editable text, Attributes attributes) {
        String color = attributes.getValue("", "color");
        String face = attributes.getValue("", "face");

        if (!TextUtils.isEmpty(color)) {
            int c = getHtmlColor(color);
            if (c != -1) {
                start(text, new Foreground(c | 0xFF000000));
            }
        }

        if (!TextUtils.isEmpty(face)) {
            start(text, new Font(face));
        }
    }

    private int getHtmlColor(String color) {
        // 16进制颜色值
        try {
            Matcher hexMatcher = getHexColorPattern().matcher(color);
            if (hexMatcher.find()) {
                String hexColor = hexMatcher.group(1);
                return Color.parseColor(hexColor);
            }
        } catch (Exception ignore) {
        }
        // rgb进制颜色值
        try {
            Matcher rgbMatcher = getRgbColorPattern().matcher(color);
            if (rgbMatcher.find()) {
                int r = Integer.valueOf(rgbMatcher.group(1));
                int g = Integer.valueOf(rgbMatcher.group(2));
                int b = Integer.valueOf(rgbMatcher.group(3));
                return Color.rgb(r, g, b);
            }
        } catch (Exception ignore) {
        }
        // argb颜色值
        try {
            Matcher argbMatcher = getArgbColorPattern().matcher(color);
            if (argbMatcher.find()) {
                int r = Integer.valueOf(argbMatcher.group(1));
                int g = Integer.valueOf(argbMatcher.group(2));
                int b = Integer.valueOf(argbMatcher.group(3));
                float a = Float.valueOf(argbMatcher.group(4));
                return Color.argb((int) (a * 255), r, g, b);
            }
        } catch (Exception ignore) {
        }

        if ((mFlags & android.text.Html.FROM_HTML_OPTION_USE_CSS_COLORS)
                == android.text.Html.FROM_HTML_OPTION_USE_CSS_COLORS) {
            Integer i = sColorMap.get(color.toLowerCase(Locale.US));
            if (i != null) {
                return i;
            }
        }
        int htmlColor = Kit.getHtmlColor(color);
        if (htmlColor != -1) {
            return htmlColor;
        }
        return Color.BLACK;
    }

    public void setDocumentLocator(Locator locator) {
    }

    public void startDocument() {
    }

    public void endDocument() {
    }

    public void startPrefixMapping(String prefix, String uri) {
    }

    public void endPrefixMapping(String prefix) {
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        handleStartTag(localName, attributes);
    }

    public void endElement(String uri, String localName, String qName) {
        handleEndTag(localName);
    }

    public void characters(char ch[], int start, int length) {
        if (mCodeStart) {
            mSpannableStringBuilder.append(new String(ch, start, length));
            return;
        }
        StringBuilder sb = new StringBuilder();

        /*
         * Ignore whitespace that immediately follows other whitespace;
         * newlines count as spaces.
         */

        for (int i = 0; i < length; i++) {
            char c = ch[i + start];

            if (c == ' ' || c == '\n') {
                char pred;
                int len = sb.length();

                if (len == 0) {
                    len = mSpannableStringBuilder.length();

                    if (len == 0) {
                        pred = '\n';
                    } else {
                        pred = mSpannableStringBuilder.charAt(len - 1);
                    }
                } else {
                    pred = sb.charAt(len - 1);
                }

                if (pred != ' ' && pred != '\n') {
                    sb.append(' ');
                }
            } else {
                sb.append(c);
            }
        }

        mSpannableStringBuilder.append(sb);
    }

    public void ignorableWhitespace(char ch[], int start, int length) {
    }

    public void processingInstruction(String target, String data) {
    }

    public void skippedEntity(String name) {
    }

    private static class Bold {
    }

    private static class Italic {
    }

    private static class Underline {
    }

    private static class Strikethrough {
    }

    private static class Big {
    }

    private static class Small {
    }

    private static class Monospace {
    }

    private static class Blockquote {
    }

    private static class Super {
    }

    private static class Sub {
    }

    private static class Bullet {
    }

    private static class Code {
    }

    private static class FontSize {
        private int fontSize;
        FontSize(int fs) {
            fontSize = fs;
        }
    }

    private static class Font {
        String mFace;

        Font(String face) {
            mFace = face;
        }
    }

    private static class Href {
        String mHref;

        Href(String href) {
            mHref = href;
        }
    }

    private static class Foreground {
        private int mForegroundColor;
        private boolean isStub;

        Foreground(int foregroundColor) {
            mForegroundColor = foregroundColor;
        }

        Foreground(int foregroundColor, boolean stub) {
            mForegroundColor = foregroundColor;
            isStub = stub;
        }
    }

    private static class Background {
        private int mBackgroundColor;

        Background(int backgroundColor) {
            mBackgroundColor = backgroundColor;
        }
    }

    private static class Heading {
        private int mLevel;

        Heading(int level) {
            mLevel = level;
        }
    }

    private static class Newline {
        private int mNumNewlines;

        Newline(int numNewlines) {
            mNumNewlines = numNewlines;
        }
    }

    private static class Alignment {
        private Layout.Alignment mAlignment;

        Alignment(Layout.Alignment alignment) {
            mAlignment = alignment;
        }
    }

    private static class Indent {
        private final int mIndentSize;

        private Indent(int mIndentSize) {
            this.mIndentSize = mIndentSize;
        }
    }

}

