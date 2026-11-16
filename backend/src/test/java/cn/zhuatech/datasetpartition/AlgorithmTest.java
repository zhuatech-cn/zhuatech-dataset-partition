/* 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ */
package cn.zhuatech.datasetpartition;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import org.junit.jupiter.api.*;
import tools.jackson.databind.ObjectMapper;

/** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
@SuppressWarnings("unchecked")
class AlgorithmTest {
  Map<String, Object> cfg;
  List<Map<String, Object>> data;

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ · 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  @BeforeEach
  void load() throws Exception {
    var fixture =
        new ObjectMapper()
            .readValue(getClass().getResourceAsStream("/static/catalog.json"), Map.class);
    cfg = (Map<String, Object>) fixture.get("settings");
    data = (List<Map<String, Object>>) fixture.get("rows");
  }

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ · 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  @Test
  void boundaryCases() {
    var x =
        Algorithm.compute(
            cfg,
            List.of(
                Map.of("id", "1", "group", "shared", "text", "甲"),
                Map.of("id", "2", "group", "shared", "text", "乙")));
    var m = (Map<?, ?>) x.get("metrics");
    assertEquals(true, m.get("emptySplit"));
    assertEquals(1, m.get("components"));
  }

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ · 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  @Test
  void enterpriseEvidenceAndBoundaries() {
    var rows=List.<Map<String,Object>>of(Map.of("id","A","group","a","text","first"),Map.of("id","B","group","b","text","second"),Map.of("id","C","group","c","text","third"));
    var config=Map.<String,Object>of("train",98,"validation",1,"seed","demo");
    var result=Algorithm.compute(config,rows);var m=(Map<?,?>)result.get("metrics");
    assertEquals(false,m.get("emptySplit"));assertEquals(1,m.get("train"));assertEquals(1,m.get("validation"));assertEquals(1,m.get("test"));
    assertEquals(result,Algorithm.compute(config,rows.reversed()));
  }

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ · 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  @Test
  void knownResults() {
    var result = Algorithm.compute(cfg, data);
    var m = (Map<String, Object>) result.get("metrics");
    var out = (List<Map<String, Object>>) result.get("rows");
    var first = out.getFirst();
    assertEquals(3, ((Number) m.get("components")).intValue());
    var split = new HashMap<>();
    out.forEach(r -> split.put(r.get("id"), r.get("split")));
    assertEquals(split.get("D1"), split.get("D3"));
    var reverse = new ArrayList<>(data);
    Collections.reverse(reverse);
    assertEquals(result, Algorithm.compute(cfg, reverse));
  }

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ · 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  @Test
  void rejectsInvalidInput() {
    cfg.put("train", 99);
    assertThrows(Model.Failure.class, () -> Algorithm.compute(cfg, data));
  }
}
