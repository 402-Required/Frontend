import { test } from "node:test";
import assert from "node:assert/strict";
import { existsSync, readFileSync } from "node:fs";
import { catalog, categories, logoPaths, matchesService } from "./catalog.js";
test("catalog covers all categories with unique ids and verified local icons", () => {
  assert.ok(catalog.length >= 120);
  assert.equal(new Set(catalog.map((s) => s.id)).size, catalog.length);
  for (const service of catalog) {
    assert.ok(categories.includes(service.category));
    assert.ok(logoPaths[service.id]);
    const path = new URL("../public" + logoPaths[service.id], import.meta.url);
    assert.ok(existsSync(path), service.id);
    assert.ok(readFileSync(path).length > 100, service.id);
  }
});
test("Korean, English, aliases and spacing all find the same brand", () => {
  for (const [query, id] of [
    ["쿠팡와우", "coupang"],
    ["티빙", "tving"],
    ["tving", "tving"],
    ["유튜브프리미엄", "youtube"],
    ["네이버 플러스", "naver-plus"],
    ["챗 GPT", "chatgpt"],
    ["디즈니 플러스", "disney"],
    ["게임패스", "xbox"],
    ["밀리의서재", "milli"],
    ["마이박스", "mybox"],
  ])
    assert.ok(
      catalog.filter((s) => matchesService(s, query)).some((s) => s.id === id),
      query,
    );
});
test("saved subscriptions inherit new catalog aliases", () => {
  assert.ok(
    matchesService({ name: "내 티빙 요금제", serviceId: "tving" }, "TVING"),
  );
  assert.ok(matchesService({ name: "직접 등록한 서비스" }, "직접등록"));
  assert.ok(!matchesService({ name: "직접 등록한 서비스" }, "넷플릭스"));
});
test("unknown prices remain empty until entered by the user", () => {
  assert.ok(catalog.every((s) => s.price === ""));
});
