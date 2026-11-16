/* 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ */
package cn.zhuatech.datasetpartition;

import static cn.zhuatech.datasetpartition.Checks.*;

import java.util.*;

/**
 * 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ 把同来源或内容相同的样本连为一组，再做可复现的训练、验证和测试集分配，并报告实际分布。
 * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
 */
final class Algorithm {
  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ · 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  static int root(int[] p, int i) {
    while (p[i] != i) {
      p[i] = p[p[i]];
      i = p[i];
    }
    return i;
  }

  /** 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ · 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。 */
  static Map<String, Object> compute(Map<String, Object> cfg, List<Map<String, Object>> input) {
    unique(input);
    int train = integer(cfg, "train", 1, 98), val = integer(cfg, "validation", 1, 98);
    must(train + val < 100, "测试集比例必须大于零");
    String seed = str(cfg, "seed", false);
    var rows = input.stream().sorted(Comparator.comparing(r -> (String) r.get("id"))).toList();
    int n = rows.size();
    int[] parent = new int[n];
    var first = new HashMap<String, Integer>();
    for (int i = 0; i < n; i++) {
      parent[i] = i;
      var r = rows.get(i);
      String norm =
          java.text.Normalizer.normalize(str(r, "text", false), java.text.Normalizer.Form.NFKC)
              .strip()
              .replaceAll("(?U)\\s+", " ")
              .toLowerCase(Locale.ROOT);
      for (String key : List.of("source:" + str(r, "group", false).strip(), "text:" + hash(norm))) {
        Integer prev = first.putIfAbsent(key, i);
        if (prev != null) parent[root(parent, i)] = root(parent, prev);
      }
    }
    var groups = new HashMap<Integer, List<Integer>>();
    for (int i = 0; i < n; i++)
      groups.computeIfAbsent(root(parent, i), v -> new ArrayList<>()).add(i);
    var sorted =
        groups.values().stream()
            .sorted(
                Comparator.<List<Integer>>comparingInt(List::size)
                    .reversed()
                    .thenComparing(g -> hash(seed + ":" + rows.get(g.getFirst()).get("id"))))
            .toList();
    int[] count = {0, 0, 0}, ratios = {train, val, 100 - train - val};
    String[] names = {"train", "validation", "test"};
    var out = new ArrayList<>();
    int remaining = sorted.size();
    for (var g : sorted) {
      int slot = 0;
      for (int i = 1; i < 3; i++)
        if (n * ratios[i] - 100 * count[i] > n * ratios[slot] - 100 * count[slot]) slot = i;
      // If enough independent groups exist, reserve one for each still-empty split.
      int empty = 0;
      for (int amount : count) if (amount == 0) empty++;
      if (sorted.size() >= 3 && remaining == empty) {
        slot = -1;
        for (int i = 0; i < 3; i++)
          if (count[i] == 0 && (slot < 0 || ratios[i] > ratios[slot])) slot = i;
      }
      remaining--;
      count[slot] += g.size();
      for (int i : g)
        out.add(
            Map.of(
                "id",
                rows.get(i).get("id"),
                "group",
                rows.get(i).get("group"),
                "split",
                names[slot],
                "component",
                rows.get(g.getFirst()).get("id")));
    }
    return result(
        Map.of(
            "components",
            groups.size(),
            "train",
            count[0],
            "validation",
            count[1],
            "test",
            count[2],
            "emptySplit",
            Arrays.stream(count).anyMatch(x -> x == 0)),
        out);
  }
}
