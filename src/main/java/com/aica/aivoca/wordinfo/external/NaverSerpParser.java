package com.aica.aivoca.wordinfo.external;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Pattern;

@Component
@Slf4j
public class NaverSerpParser {

    private static final Pattern SPACE = Pattern.compile("\\s+");

    private static String fix(String s) {
        if (s == null) return null;
        s = SPACE.matcher(s.trim()).replaceAll(" ");
        return s.replaceAll("\\s+([,.!?;:])", "$1");
    }

    private static String normalizeHead(String s) {
        if (s == null) return "";
        return s.toLowerCase().replaceAll("[^a-z]+", " ").trim();
    }

    public Map<String, Object> parse(String html, String query) {
        Document doc = Jsoup.parse(html);

        Element section = findEnglishSection(doc);
        if (section == null) {
            return Map.of("ok", false, "reason", "영어사전 섹션을 찾지 못했습니다.");
        }

        Map<String, Object> out = parseFromSection(section, query);
        if (Boolean.TRUE.equals(out.get("ok"))) {
            return out;
        }
        return Map.of("ok", false, "reason", "사전 구조를 해석하지 못했습니다.");
    }

    private Element findEnglishSection(Document doc) {
        // 1) 흔한 케이스
        for (Element bx : doc.select(".api_subject_bx")) {
            Element h = bx.selectFirst("h2.title, h3.title, .title_area");
            if (h != null && h.text().contains("영어사전")) {
                return bx;
            }
        }
        // 2) 다른 모듈 이름
        for (Element bx : doc.select(".dictionary_module, .api_module, .bx_subject, .dict_subject")) {
            if (bx.text().contains("영어사전")) {
                return bx;
            }
        }
        // 3) 마지막 시도
        Element hit = doc.selectFirst("div:containsOwn(영어사전), section:containsOwn(영어사전)");
        if (hit != null) {
            return hit.parent() != null ? hit.parent() : hit;
        }
        return null;
    }

    private Map<String, Object> parseFromSection(Element section, String query) {
        // 표제어
        String word = null;
        Element headEl =
                section.selectFirst("dt.dic_word_title a.title") != null
                        ? section.selectFirst("dt.dic_word_title a.title")
                        : section.selectFirst(".dic_word_title a") != null
                        ? section.selectFirst(".dic_word_title a")
                        : section.selectFirst(".word, strong.word, b.word");
        if (headEl != null) {
            word = headEl.text().trim();
        }

        String queryHead = query.split(" ", 2)[0];
        boolean matches = normalizeHead(word).equals(normalizeHead(queryHead));

        List<Map<String, Object>> meanings = new ArrayList<>();

        // dd.word_dsc = 품사 한 블록
        for (Element dsc : section.select("dd.word_dsc")) {

            // 품사
            String pos = Optional.ofNullable(dsc.selectFirst("strong.part"))
                    .map(Element::text)
                    .orElse("");

            // 품사 안의 첫번째 뜻
            Element li = dsc.selectFirst(".mean_list .mean_list_item, li");
            if (li == null) continue;

            // li 전체 텍스트 (백업용으로 먼저 받아둠)
            String liFullText = li.text(); // 여기엔 "발음듣기" 포함돼 있을 수 있음

            // 예문 엘리먼트
            Element exEl = li.selectFirst(".example, .exp_example, .dict-example");

            String exEn = null;
            String exKo = null;
            if (exEl != null) {
                // 영어 예문
                exEn = Optional.ofNullable(exEl.selectFirst("p.text [lang=en], [lang=en], .en"))
                        .map(Element::text).map(NaverSerpParser::fix).orElse(null);

                // 한국어 예문 (1차 시도: 보이는 클래스들)
                exKo = Optional.ofNullable(
                                exEl.selectFirst("p.text_mean, span.text_mean, .text_mean, .ko, .kor, p.text span.mean")
                        )
                        .map(Element::text).map(NaverSerpParser::fix).orElse(null);
            }

            // 2차 백업: 한국어 예문이 위에서 못 잡혔으면 li 전체 텍스트에서 영어 예문 이후를 자른다
            if ((exKo == null || exKo.isBlank()) && exEn != null && !exEn.isBlank()) {
                // li 전체 텍스트 안에 영어 예문이 있으면 그 이후를 한국어로 본다
                int idx = liFullText.indexOf(exEn);
                if (idx != -1) {
                    String after = liFullText.substring(idx + exEn.length()).trim();
                    // "발음듣기" 제거
                    after = after.replace("발음듣기", "").trim();
                    // 공백 정리
                    after = after.replaceAll("\\s+", " ").trim();
                    if (!after.isBlank()) {
                        exKo = after;
                    }
                }
            }

            // 이제 뜻만 추출 (예문은 제외된 상태로)
            String rawMeaning;
            {
                Element exClone = null;
                if (exEl != null) {
                    exClone = exEl.clone();
                    exEl.remove(); // 잠깐 제거
                }
                Element desc = li.selectFirst(".desc p.mean.api_txt_lines, .desc, p");
                if (desc != null) {
                    rawMeaning = fix(desc.text());
                } else {
                    rawMeaning = fix(li.text());
                }
                // 다시 원래대로
                if (exEl != null && exClone != null) {
                    li.appendChild(exClone);
                }
            }

            if (rawMeaning == null || rawMeaning.isBlank()) continue;

            // 뜻에서 "발음듣기" 제거
            rawMeaning = rawMeaning.replace("발음듣기", "").trim();
            rawMeaning = rawMeaning.replaceAll("\\s+", " ").trim();

            Map<String, Object> m = new LinkedHashMap<>();
            m.put("meaning", rawMeaning);
            m.put("partsOfSpeech", pos.isBlank() ? List.of() : List.of(pos));

            List<Map<String, String>> exampleList = new ArrayList<>();
            if ((exEn != null && !exEn.isBlank()) || (exKo != null && !exKo.isBlank())) {
                Map<String, String> exMap = new LinkedHashMap<>();
                exMap.put("sentence", exEn == null ? "" : exEn);
                exMap.put("meaning", exKo == null ? "" : exKo);   // ✅ 여기 최종 키는 meaning
                exampleList.add(exMap);
            }
            m.put("exampleSentences", exampleList);

            meanings.add(m);
        }

        if (meanings.isEmpty()) {
            return Map.of("ok", false, "reason", "사전 항목을 찾지 못했습니다.");
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("ok", true);
        out.put("word", (word != null && !word.isBlank()) ? word : queryHead);
        out.put("matches_query", matches);
        out.put("meanings", meanings);
        return out;
    }
}
