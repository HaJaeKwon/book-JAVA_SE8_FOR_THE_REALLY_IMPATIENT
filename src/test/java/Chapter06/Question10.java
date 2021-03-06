package Chapter06;

import com.sun.xml.internal.ws.util.CompletedFuture;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class Question10 {

    /**
     * 사용자에게 URL을 요구하고 해당 URL에서 웹 페이지를 읽어서 모든 링크를 표시하는 프로그램을 작성하라.
     * 각 단계에 CompletableFuture를 사용하고, get은 호출하지 않아야 한다.
     * 프로그램이 너무 일찍 종료하지 않도록 다음과 같이 호출한다.
     * ForkJoinPool.commonPool().awaitQuiescence(10, TimeUnit.SECONDS);
     */
    @Test
    public void solution() {

        try {
            URL url = new URL("https://www.naver.com");

//            CompletableFuture<String> contents = CompletableFuture.supplyAsync(() -> blockingReadPage(url));
            CompletableFuture<String> contents = readPage(url);
            CompletableFuture<List<String>> links = contents.thenApply(this::getLinks);

            links.thenAccept(list -> {
                list.forEach(System.out::println);
            });

            ForkJoinPool.commonPool().awaitQuiescence(10, TimeUnit.SECONDS);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public String blockingReadPage(URL url) {
        StringBuffer content = new StringBuffer();
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content.toString();
    }

    public List<String> getLinks(String html) {
        final String HTML_TAG_PATTERN = "(?i)<a([^>]+)>(.+?)</a>";
        final String HTML_HREF_TAG_PATTERN = "\\s*(?i)href\\s*=\\s*(\"([^\"]*\")|'[^']*'|([^'\">\\s]+))";
//        final String HTML_HREF_TAG_PATTERN = "\\s*(?i)href\\s*=\\s*\"([^\"]*)\"";

        List<String> links = new ArrayList<>();

        Pattern aPattern = Pattern.compile(HTML_TAG_PATTERN);
        Pattern hrefPattern = Pattern.compile(HTML_HREF_TAG_PATTERN);
        Matcher m = aPattern.matcher(html);
        while (m.find()) {
            String a = m.group(1);
            Matcher m2 = hrefPattern.matcher(a);
            while (m2.find()) {
                links.add(m2.group(1));
            }
        }

        return links;
    }

    public CompletableFuture<String> readPage(URL url) {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            StringBuffer content = new StringBuffer();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
                Stream<String> lines = reader.lines();
                lines.forEach(s -> content.append(s));
            } catch (IOException e) {
                e.printStackTrace();
            }
//            try {
//                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//                connection.setRequestMethod("GET");
//
//                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//                String inputLine;
//                while( (inputLine = in.readLine()) != null ) {
//                    content.append(inputLine);
//                }
//                connection.disconnect();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
            return content.toString();
        });
        return future;
    }

    /**
     * html 파싱을 쉽게 하는 방법으로는 tag를 먼저 찾고! 그 안에서 다시 필요한 영역을 파싱하는 것이다.
     * [^\"]* 로 하면 따옴표를 제외하고 capture 가능하다
     *
     * (?i)는 대소문자 구분을 하지 않겠다는 뜻
     * href, HREF 모두 capture 가능하다
     *
     * try with resources 방식을 통해 url에서 입력객체를 열고 닫을 수 있다.
     * try with resources 는 AutoClosable 인터페이스 구현체들만 들어갈 수 있다
     */

}
