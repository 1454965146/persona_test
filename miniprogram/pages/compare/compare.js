var app = getApp();
var api = require("../../utils/api");

Page({
  data: {
    loading: true,
    loadingText: "正在加载对比分析...",
    errorMessage: "",
    comparisonStatus: "",
    nameA: "",
    nameB: "",
    typeA: "",
    typeB: "",
    scoresA: null,
    scoresB: null,
    dimCompare: null,
    compareHtml: "",
    matchScore: 0,
    matchDesc: ""
  },

  onLoad: function(o) {
    if (!app.isLoggedIn()) {
      wx.reLaunch({ url: "/pages/index/index" });
      return;
    }
    if (o.comparisonId) this.loadComparisonById(o.comparisonId);
    else if (o.reportCodeA && o.reportCodeB) this.loadComparison(o.reportCodeA, o.reportCodeB, o.relType || "FRIEND");
    else this.setData({ loading: false, errorMessage: "参数缺失" });
  },

  onUnload: function() {
    if (this.pollTimer) clearTimeout(this.pollTimer);
  },

  loadComparisonById: function(cid) {
    this.comparisonId = cid;
    this.fetchComparison();
  },

  fetchComparison: function() {
    var self = this;
    if (this.pollTimer) clearTimeout(this.pollTimer);
    api.getComparison(this.comparisonId).then(function(r) {
      self.setData({ comparisonStatus: r.status, errorMessage: r.errorMessage || "" });
      if (r.status === "COMPLETED") {
        self.renderResult(r);
      } else if (r.status === "FAILED") {
        self.setData({ loading: false, loadingText: "对比分析生成失败" });
      } else {
        self.setData({
          loading: true,
          loadingText: r.status === "PROCESSING" ? "AI 正在生成关系分析..." : "关系分析排队中..."
        });
        self.pollTimer = setTimeout(function() { self.fetchComparison(); }, 2000);
      }
    }).catch(function(e) {
      self.setData({ loading: false, errorMessage: e.message });
    });
  },

  retryComparison: function() {
    var self = this;
    if (!this.comparisonId) return;
    api.retryComparison(this.comparisonId).then(function() {
      self.setData({ loading: true, errorMessage: "", loadingText: "关系分析重新排队中..." });
      self.fetchComparison();
    }).catch(function(e) {
      self.setData({ errorMessage: e.message });
    });
  },

  loadComparison: function(rca, rcb, rel) {
    var self = this;
    self.setData({ loadingText: "正在分析两人的性格化学反应..." });
    api.generateComparison(rca, rcb, rel).then(function(r) {
      self.comparisonId = r.comparisonId;
      self.setData({ comparisonStatus: r.status });
      if (r.status === "COMPLETED") self.renderResult(r);
      else self.fetchComparison();
    }).catch(function(e) {
      self.setData({ loading: false, errorMessage: e.message });
    });
  },

  renderResult: function(r) {
    var h = (r.content || "");
    h = h.replace(/### (.*)/g, '<h3 style="color:#c9bdff;margin:24rpx 0 10rpx;font-size:30rpx;">$1</h3>');
    h = h.replace(/## (.*)/g, '<h2 style="color:#f5f5f7;margin:36rpx 0 16rpx;font-size:34rpx;">$1</h2>');
    h = h.replace(/> (.*)/g, '<blockquote style="color:#a8a8b8;border-left:4rpx solid #6c5ce7;padding-left:20rpx;margin:14rpx 0;">$1</blockquote>');
    h = h.replace(/\*\*(.*?)\*\*/g, "<b>$1</b>");
    h = h.replace(/\n\n/g, "<br/><br/>");
    this.setData({
      loading: false,
      nameA: r.nameA,
      nameB: r.nameB,
      typeA: r.typeA,
      typeB: r.typeB,
      compareHtml: h,
      scoresA: r.scoresA || null,
      scoresB: r.scoresB || null
    });
    if (r.scoresA && r.scoresB) {
      this.buildDimCompare(r.scoresA, r.scoresB);
    } else {
      this.loadDimensionReports(r.reportCodeA, r.reportCodeB);
    }
  },

  loadDimensionReports: function(reportCodeA, reportCodeB) {
    if (!reportCodeA || !reportCodeB) return;
    var self = this;
    Promise.all([api.getReport(reportCodeA), api.getReport(reportCodeB)]).then(function(reports) {
      var sa = (reports[0] && reports[0].dimensionScores) || {};
      var sb = (reports[1] && reports[1].dimensionScores) || {};
      self.setData({ scoresA: sa, scoresB: sb });
      self.buildDimCompare(sa, sb);
    }).catch(function() {});
  },

  buildDimCompare: function(sa, sb) {
    var dk = { EI: "外向/内向", SN: "感觉/直觉", TF: "思考/情感", JP: "判断/感知", EXTRA: "开放度" };
    var dims = [];
    var totalDiff = 0;
    var count = 0;
    for (var k in dk) {
      var va = sa[k] || 3;
      var vb = sb[k] || 3;
      dims.push({
        label: dk[k],
        valA: va.toFixed(1),
        valB: vb.toFixed(1),
        pctA: Math.round(va / 5 * 100),
        pctB: Math.round(vb / 5 * 100)
      });
      totalDiff += Math.abs(va - vb);
      count++;
    }
    var avgDiff = totalDiff / count;
    var score = Math.round((1 - avgDiff / 4) * 100);
    var desc = "";
    if (score >= 85) desc = "高度契合，价值观与节奏高度一致";
    else if (score >= 70) desc = "相当合拍，存在少量可磨合的差异";
    else if (score >= 55) desc = "互补型关系，差异是了解彼此的契机";
    else if (score >= 40) desc = "差异明显，需要更多沟通和理解";
    else desc = "差异较大，坦诚沟通是相处的关键";
    this.setData({ dimCompare: dims, matchScore: score, matchDesc: desc });
  }
});
