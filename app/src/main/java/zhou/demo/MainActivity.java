package zhou.demo;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import com.xie.rich.R;
import com.zzhoujay.richtext.CacheType;
import com.zzhoujay.richtext.LinkHolder;
import com.zzhoujay.richtext.RichText;
import com.zzhoujay.richtext.callback.LinkFixCallback;
import com.zzhoujay.richtext.callback.OnUrlClickListener;

import java.util.ArrayList;
import java.util.List;


/***
 * 代码原文档：https://github.com/zzhoujay/RichText/wiki
 * https://github.com/zzhoujay/Html
 * 注意：要避免混淆
 */
public class MainActivity extends AppCompatActivity {


    private static final String IMAGE = "<img title=\"\" src=\"http://g.hiphotos.baidu.com/image/pic/item/241f95cad1c8a7866f726fe06309c93d71cf5087.jpg\"  style=\"cursor: pointer;\"><br><br>" +
            "<img src=\"http://img.ugirls.com/uploads/cooperate/baidu/20160519menghuli.jpg\" width=\"1080\" height=\"1620\"/><a href=\"http://www.baidu.com\">baidu</a>" +
            "hello asdkjfgsduk <a href=\"http://www.jd.com\">jd</a>";
    private static final String IMAGE1 = "<h1>RichText</h1><p>Android平台下的富文本解析器</p><img title=\"\" src=\"http://g.hiphotos.baidu.com/image/pic/item/241f95cad1c8a7866f726fe06309c93d71cf5087.jpg\"  style=\"cursor: pointer;\"><br><br>" +
            "<h3>点击菜单查看更多Demo</h3><img src=\"http://ww2.sinaimg.cn/bmiddle/813a1fc7jw1ee4xpejq4lj20g00o0gnu.jpg\" /><p><a href=\"http://www.baidu.com\">baidu</a>" +
            "hello asdkjfgsduk <a href=\"http://www.jd.com\">jd</a></p><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br>bottom";


    private static final String GIF_TEST = "<img src=\"http://ww4.sinaimg.cn/large/5cfc088ejw1f3jcujb6d6g20ap08mb2c.gif\">";

    private static final String markdown_test = "image:![image](http://image.tianjimedia.com/uploadImages/2015/129/56/J63MI042Z4P8.jpg)[link](https://github.com/zzhoujay/RichText/issues)";

    private static final String gif_test = "<h3>Test1</h3><img src=\"http://www.aikf.com/ask/resources/images/facialExpression/qq/1.gif\" />" +
            "            <h3>Test2</h3><img src=\"http://www.aikf.com/ask/resources/images/facialExpression/qq/2.gif\" />" +
            "            <h3>Test3</h3><img src=\"http://www.aikf.com/ask/resources/images/facialExpression/qq/3.gif\" />" +
            "            <h3>Test4</h3><img src=\"http://www.aikf.com/ask/resources/images/facialExpression/qq/4.gif\" />" +
            "            <h3>Test5</h3><img src=\"http://www.aikf.com/ask/resources/images/facialExpression/qq/5.gif\" />" +
            "            <h3>Test6</h3><img src=\"http://www.aikf.com/ask/resources/images/facialExpression/qq/6.gif\" />" +
            "            <h3>Test7</h3><img src=\"http://www.aikf.com/ask/resources/images/facialExpression/qq/7.gif\" />" +
            "            <h3>Test8</h3><img src=\"http://www.aikf.com/ask/resources/images/facialExpression/qq/8.gif\" />" +
            "            <h3>Test9</h3><img src=\"http://www.aikf.com/ask/resources/images/facialExpression/qq/9.gif\" />";


    private static final String large_image = "<img src=\"http://static.tme.im/article_1_1471686391302fue\"/>";

    private static final String text = "";
    private static final String TAG = "MainActivityTest";
    private static final String assets_image_test = "<h1>Assets Image Test</h1><img src=\"file:///android_asset/doge.jpg\">";
    private final String issue142 = "<p><img src=\"http://image.wangchao.net.cn/it/1233190350268.gif?size=528*388\" width=\"528\" height=\"388\" /></p>";
    private final String issue143 = "<img src=\"file:///C:\\Users\\ADMINI~1\\AppData\\Local\\Temp\\ksohtml\\wpsB8DD.tmp.png\">";
    private final String issue147 = "<div class=\"pictureBox\"> " +
            " <img src=\"http://static.storeer.com/wlwb/productDetail/234be0ec-e90a-4eda-90bd-98d64b55280a_580x4339.jpeg\">" +
            "</div>";
    private final String issue149 = null;
    private final String issue150 = "<img src='http://cuncxforum-10008003.image.myqcloud.com/642def77-373f-434f-8e68-42dedbd9f880'/><br><img src='http://cuncxforum-10008003.image.myqcloud.com/bf153d9f-e8c3-4dcc-a23e-bfe0358cb429'/>";
    int loading = 0;
    int failure = 0;
    int ready = 0;
    int init = 0;


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RichText.initCacheDir(this);
        RichText.debugMode = true;

        final TextView textView = findViewById(R.id.text);
        final TextView textView1 = findViewById(R.id.text1);
        final TextView textView2 = findViewById(R.id.text2);


//        RichText.from(html)
//                .urlClick(new OnUrlClickListener() {
//                    @Override
//                    public boolean urlClicked(String url) {
//                        if (url.startsWith("code://")) {
//                            Toast.makeText(MainActivity.this, url.replaceFirst("code://", ""), Toast.LENGTH_SHORT).show();
//                            return true;
//                        }
//                        return false;
//                    }
//                })
//                .into(textView);

        //关闭缓存
//        RichText.from(testNew8, true).cache(CacheType.none).into(textView);
//        RichText.from(test1).into(textView1);


//        RichText.from(test3, true).into(textView1);
//        RichText.from(test4, true).into(textView);
        RichText.from(test13, true).cache(CacheType.none).into(textView2);
        Log.e("testRich", test13);
//        textView.setText(Html.fromHtml(list_test, Html.FROM_HTML_MODE_LEGACY));



        //配置内容富文本
//        String content = "<p>啊实打实<a href=\"https://uutool.cn/ueditor/\" target=\"_self\" title=\"擦撒大苏打\">打算发撒撒</a>发生</p>";
//        RichText.from(content).linkFix(new LinkFixCallback(){
//
//            @Override
//            public void fix(LinkHolder holder) {
//                //去除链接的下划线
//                holder.setUnderLine(true);
//            }
//        }).urlClick(new OnUrlClickListener() {
//            @Override
//            public boolean urlClicked(String url) {
//                return true;
//            }
//        }).into(textView2);
    }


    //【新增表情、有序无序列表、背景颜色、斜体、删除线】
    //1 支持有序列表
    String test3 = "<ol><li>啊<i>是大</i></li><li>十分士<strong>大</strong>夫撒大苏打f</li><li>巅<strong>峰</strong>时代</li></ol>";
    //2 支持无序列表
    String test4 = "<ul><li>阿<strong>萨的</strong></li><li>211231</li><li>爱思<strong>21</strong>发</li></ul>";
    //3 斜体
    String test5 = "<p>射手队asf<i>asfs啊沙发</i>沙发</p>";
    //4 删除线
    String test6 = "<p>阿萨啊<span style=\"text-decoration: line-through;\">沙发上</span>嘎嘎</p>";
    //5 背景颜色
    String test7 = "<p>啊啊首发申其<span style=\"background-color: rgb(155, 187, 89);\">购法发噶</span>他嘎洒算<span style=\"background-color: rgb(247, 150, 70);\">法</span>\n" +
            "</p><p><span style=\"background-color: rgb(247, 150, 70);\">a</span>ha<span style=\"background-color: rgb(75, 172, 198);\">shasa按</span><span style=\"background-color: rgb(192, 80, 77);\">时发</span>放</p>";
    //6 表情
    String test8 = "<p>2342asda是\uD83D\uDE07我去饿</p>";
    //7 图片
    String test9 = "<p><img src='http://wx1.sinaimg.cn/mw690/eaaf2affly1fihvjpekzwj21el0qotfq.jpg'/></p>";
    String gif = "<p><img src=\"https://cdn6.xiaoshuxiong.com/data/images/hygj/worthBuy/16642500087359451629753.gif\"/></p>";
    //8 文字大小
    String test13 = "<p>啊实打实大<span style=\"font-size: 24px;\">苏打实打</span>实的</p>";


    //特殊的有序列表
    String test10 =
            "<p>开始</p>"+  //加这行会有序变成无序(Bug)
            "<ol>" +
            "    <li>" +
            "        <span>A</span>" +
            "    </li>" +
            "    <li>" +
            "        <span>B</span>" +
            "    </li>" +
            "</ol>";

    //特殊的无序列表
    String test11 =
            "<p>开始</p>"+
            "<ul>" +
            "    <li>" +
            "        <span>A</span>" +
            "    </li>" +
            "    <li>" +
            "        <span>B</span>" +
            "    </li>" +
            "</ul>";


    String test12 = "<ol><li><p>" +
            "新闻（消息）写作，不同于拉家常，也不同于写论文、写总结报告，它有自己的写作规律。规律之一就是：“立片言以居要”，“开门见山”，把最重要、最新鲜的事实放在最前头。这就是所谓的消息导语。它是消息开头的第一句或第一段。</p>" +
            "</li></ol>";

    String testma = "<ol>\n" +
            "        <li>\n" +
            "                <p>\n" +
            "                        <strike><u><em><strong>新闻（消息）写作</strong></em></u></strike>，不同于拉家常，<a href=\"mmw://bMode?android_path=/app/b_mode&amp;ios_path=PHMMPrenatalForm/BModeUltrasoundDetailController\">也不同于写论文</a>、写总结报告，<span style=\"color:#ff0000;\">它有自己的写作规律</span>。<span style=\"font-size:16px;\"><strong>规律之一就</strong>是</span>：&ldquo;立片言以居要&rdquo;，&ldquo;开门见山&rdquo;，把最重要、最新鲜的事实放在最前头。这就是所谓的消息导语。它是消息开头的第一句或第一段。</p>\n" +
            "        </li>\n" +
            "</ol>";

    //特别的有序列表
    String testSpec = "<ol class=\" list-paddingleft-2\">\n" +
            "    <li>\n" +
            "        <p>\n" +
            "            <span style=\"color: rgb(75, 172, 198);\">啊<em>是</em></span><em>大啊沙发沙发啊算违法沙发沙<span style=\"color: rgb(192, 80, 77);\">发沙发沙发沙</span>发嘎斯v阿达阿飞噶沙司大哥<span style=\"color: rgb(247, 150, 70);\">说的VS大哥哥</span>萨尔官方案说法所说的噶沙<strong>司更</strong>多的</em>\n" +
            "        </p>\n" +
            "    </li>\n" +
            "    <li>\n" +
            "        <p>\n" +
            "            十分<span style=\"color: rgb(128, 100, 162);\">士<strong>大</strong>夫撒大苏打f</span>\n" +
            "        </p>\n" +
            "    </li>\n" +
            "    <li>\n" +
            "        <p>\n" +
            "            巅<span style=\"color: rgb(227, 108, 9);\"><strong>峰</strong>时</span><span style=\"color: rgb(31, 73, 125);\">代</span>\n" +
            "        </p>\n" +
            "    </li>\n" +
            "</ol>";



    String xx = "<p>" +
            "<span style=\"color: rgb(147, 137, 83);\"><span style=\"background-color: rgb(75, 172, 198);\">按</span>" +
            "<span style=\"background-color: rgb(192, 80, 77);\">时</span></span>" +
            "<span style=\"background-color: rgb(192, 80, 77);\">发</span>" +
            "放</p>";

    String xx1 = "<p><span style=\"color: rgb(155, 187, 89);\">啊<span style=\"color: rgb(79, 129, 189);\">撒撒<span style=\"color: rgb(247, 150, 70);\">发生</span>发是</span>否</span></p>";

    String xx2 = "<p>" +
            "<span style=\"background-color: rgb(247, 150, 70);\">a</span>ha" +
            "<span style=\"background-color: rgb(75, 172, 198);\">sh" +
            "<span style=\"background-color: rgb(75, 172, 198); color: rgb(195, 214, 155);\">as</span>a</span>" +
            "<span style=\"color: rgb(147, 137, 83);\"><span style=\"background-color: rgb(75, 172, 198);\">按</span>" +
            "<span style=\"background-color: rgb(192, 80, 77);\">时</span></span>" +
            "<span style=\"background-color: rgb(192, 80, 77);\">发</span>" +
            "<span>操</span>" +
            "放</p>";

    String testNew1 = "<p style=\"margin: 0px;  color: rgb(77, 222, 77); \">" +
            "<span style=\"font-family: PingFangSC-Medium, &quot;PingFang SC Medium&quot;, &quot;PingFang SC&quot;, sans-serif; color: rgb(242, 113, 142);\">抱睡</span>" +
            "<span>你</span>" +

            "<span style=\"font-family: PingFangSC-Medium, &quot;PingFang SC Medium&quot;, &quot;PingFang SC&quot;, sans-serif; color: rgb(242, 113, 142);\">睡眠倒退</span>" +
            "<span>我</span>" +
            "<span>哇</span>" +
            "</p>";

    String testNew2 =
"<p style=\"\\&quot;color:\"><span style=\"color: rgb(79, 129, 189);\">抱睡</span><span style=\"color: rgb(178, 162, 199);\">你as<span style=\"color: rgb(31, 73, 125);\">d</span></span></p><p style=\"\\&quot;color:\"><span style=\"color: rgb(31, 73, 125);\">阿<span style=\"color: rgb(147, 137, 83);\">萨斯</span>发as<span style=\"color: rgb(146, 205, 220);\">f</span></span></p>";

    String testNew3 = "<p><font color=\"#649632\">你好<span style=\"color: rgb(0, 0, 0);\">啊沙发沙发阿嘎<strong>爱国阿松大</strong></span></font></p>";

    String testNew4 = "<p><span style=\"color: rgb(111, 222, 0);\">你好<span style=\"color: rgb(0, 0, 0);\">啊沙发沙发阿嘎<strong>爱国阿松大</strong></span></span></p>";

    String testNew5 = "<p><span style=\"color: rgb(111, 222, 0);\">你<strong>好<span style=\"color: rgb(0, 0, 0);\">啊沙</span></strong><span style=\"color: rgb(0, 0, 0);\">发<span style=\"color: rgb(247, 150, 70);\">沙</span>发阿<span style=\"color: rgb(49, 133, 155);\">嘎<strong>爱国</strong></span><strong>阿松大</strong></span></span></p>";

    String testNew6 = "<p><span style=\"color: rgb(111, 222, 0);\">你</span><span style=\"color: rgb(111, 222, 0);\"><span style=\"color: rgb(195, 214, 155);\"><strong>好</strong></span><span style=\"color: rgb(192, 80, 77);\"><strong>啊沙</strong>发沙</span><em><span style=\"color: rgb(192, 80, 77);\">发</span><span style=\"color: rgb(128, 100, 162);\">阿</span></em><span style=\"color: rgb(128, 100, 162);\">嘎<strong>爱</strong></span><span style=\"color: rgb(0, 0, 0);\"><span style=\"color: rgb(49, 133, 155);\">国</span>阿松<strong>大</strong></span></span></p>";

    String testNew7 = "<p><span style=\"color: rgb(146, 205, 220);\">萨法<span style=\"color: rgb(155, 187, 89);\">沙发沙发沙</span>发沙发沙</span>发</p>";


    String testNew8 = "<table>" +
            "    <tbody>" +
            "        <tr class=\"firstRow\">" +
            "            <td width=\"394\" valign=\"top\" style=\"word-break: break-all;\">" +
            "                1" +
            "            </td>" +
            "            <td width=\"394\" valign=\"top\" style=\"word-break: break-all;\">" +
            "                2" +
            "            </td>" +
            "        </tr>" +
            "        <tr>" +
            "            <td width=\"394\" valign=\"top\" style=\"word-break: break-all;\">" +
            "                3" +
            "            </td>" +
            "            <td width=\"394\" valign=\"top\" style=\"word-break: break-all;\">" +
            "                4" +
            "            </td>" +
            "        </tr>" +
            "    </tbody>" +
            "</table>";



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, "RecyclerView");
        menu.add(0, 1, 1, "ListView");
        menu.add(0, 2, 2, "Gif");
        menu.add(0, 3, 3, "Test");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 0) {
            startActivity(new Intent(this, RecyclerViewActivity.class));
        } else if (item.getItemId() == 1) {
            startActivity(new Intent(this, ListViewActivity.class));
        } else if (item.getItemId() == 2) {
            startActivity(new Intent(this, GifActivity.class));
        } else if (item.getItemId() == 3) {
            startActivity(new Intent(this, TestActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RichText.recycle();
    }

    public int dip2px(float dpValue) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
